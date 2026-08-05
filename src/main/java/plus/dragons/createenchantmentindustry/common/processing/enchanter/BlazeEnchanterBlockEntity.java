/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.advancement.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.BlazeLightningHelper;

public class BlazeEnchanterBlockEntity extends BlazeExperienceBlockEntity implements Clearable {
    public static final int ENCHANTING_TIME = 200;
    protected EnchanterBehaviour enchanter;
    protected boolean special;
    protected boolean cursed;
    protected Long seed;
    protected int processingTime = -1;
    protected ItemStack heldItem = ItemStack.EMPTY;
    protected AdvancementBehaviour advancement;
    protected @Nullable ActiveEnchanting activeEnchanting;
    private final SnapshotParticipant<AutomationState> automationState = new SnapshotParticipant<>() {
        @Override
        protected AutomationState createSnapshot() {
            return new AutomationState(heldItem.copy(), seed, processingTime, activeEnchanting);
        }

        @Override
        protected void readSnapshot(AutomationState snapshot) {
            heldItem = snapshot.heldItem().copy();
            seed = snapshot.seed();
            processingTime = snapshot.processingTime();
            activeEnchanting = snapshot.activeEnchanting();
            if (enchanter != null)
                enchanter.update(heldItem);
        }

        @Override
        protected void onFinalCommit() {
            if (enchanter != null)
                enchanter.update(heldItem);
            notifyUpdate();
        }
    };

    public BlazeEnchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        this.enchanter = new EnchanterBehaviour(this);
        this.advancement = new AdvancementBehaviour(this);
        behaviours.add(this.enchanter);
        behaviours.add(this.advancement);
    }

    @Override
    protected int getExperienceTankCapacity() {
        return CEIConfig.fluids().blazeEnchanterFluidCapacity.get();
    }

    @Override
    public boolean isActive() {
        return processingTime > 0;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (seed == null) {
            nextSeed();
            setChanged();
        }
        enchanter.update(heldItem);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), heldItem);
        }
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        if (seed != null)
            output.putLong("Seed", seed);
        output.putInt("ProcessingTime", processingTime);
        output.store("HeldItem", ItemStack.OPTIONAL_CODEC, heldItem);
        if (activeEnchanting != null)
            activeEnchanting.write(output.child("ActiveEnchanting"));
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        seed = input.getLong("Seed").orElse(null);
        processingTime = input.getIntOr("ProcessingTime", -1);
        heldItem = input.read("HeldItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        activeEnchanting = input.child("ActiveEnchanting").map(ActiveEnchanting::load).orElse(null);
        if (processingTime >= 0 && activeEnchanting == null)
            processingTime = -1;
        updateEnchanterIfLevelReady();
    }

    @Override
    public void tick() {
        super.tick();
        boolean update = false;
        boolean special = getHeatLevelFromBlock() == HeatLevel.SEETHING;
        if (this.special != special) {
            this.special = special;
            update = true;
        }
        var strikePos = getStrikePos();
        boolean cursed = special && BlazeLightningHelper.isStrikeBlocked(worldPosition, strikePos);
        if (this.cursed != cursed) {
            this.cursed = cursed;
            update = true;
        }
        if (level.isClientSide() && isVirtual()) {
            if (update) enchanter.update(heldItem);
            tickVirtual();
            return;
        }
        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (update) {
            enchanter.update(heldItem);
        }
        if (heldItem.isEmpty()) {
            cancelProcessing();
            return;
        }
        if (activeEnchanting == null) {
            if (enchanter.canProcess(heldItem))
                startProcessing(ENCHANTING_TIME, true);
            else if (processingTime != -1)
                cancelProcessing();
            return;
        }
        ActiveEnchanting active = activeEnchanting;
        if (!active.matches(heldItem)) {
            cancelProcessing();
            return;
        }
        if (!consumeExperience(active.cost(), active.special(), true))
            return;
        if (processingTime > 0) {
            processingTime--;
            notifyUpdate();
            return;
        }
        if (active.strikeLightning() && strikeLightning(serverLevel, strikePos)) {
            advancement.trigger(CEIAdvancements.OSHA_VIOLATION.builtinTrigger());
            serverLevel.destroyBlock(worldPosition, false);
            serverLevel.setBlockAndUpdate(worldPosition, AllBlocks.LIT_BLAZE_BURNER.defaultBlockState());
            this.setRemoved();
            return;
        }
        if (!consumeExperience(active.cost(), active.special(), false))
            return;
        heldItem = active.result().copy();
        advancement.awardStat(CEIStats.ENCHANT.get(), 1);
        if (heldItem.getItem() instanceof EnchantingTemplateItem) {
            advancement.trigger(CEIAdvancements.SIGIL_FORGING.builtinTrigger());
        } else {
            advancement.trigger(CEIAdvancements.BLAZING_ENCHANTMENT.builtinTrigger());
        }
        if (active.special()) {
            advancement.awardStat(CEIStats.SUPER_ENCHANT.get(), 1);
            boolean treasure = CEIItemData.getEnchantmentsForCrafting(heldItem).keySet().stream()
                    .anyMatch(enchantment -> enchantment.is(EnchantmentTags.TREASURE));
            if (treasure)
                advancement.trigger(CEIAdvancements.PROBABILITY_SPIKE.builtinTrigger());
        }
        finishProcessing();
        nextSeed();
        enchanter.update(heldItem);
        notifyUpdate();
        level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
    }

    private void tickVirtual() {
        if (heldItem.isEmpty()) {
            cancelProcessing();
            return;
        }
        if (activeEnchanting == null) {
            if (enchanter.canProcess(heldItem))
                startProcessing(ENCHANTING_TIME / 4, false);
            return;
        }
        ActiveEnchanting active = activeEnchanting;
        if (!active.matches(heldItem)) {
            cancelProcessing();
            return;
        }
        if (processingTime > 0) {
            processingTime--;
            return;
        }
        heldItem = active.result().copy();
        finishProcessing();
        nextSeed();
        enchanter.update(heldItem);
    }

    private boolean startProcessing(int duration, boolean requireExperience) {
        int cost = enchanter.getExperienceCost();
        if (cost <= 0 || requireExperience && !consumeExperience(cost, special, true))
            return false;
        ItemStack input = heldItem.copy();
        ItemStack result = enchanter.getResult(input);
        if (result.isEmpty()
                || ItemStack.isSameItemSameComponents(input, result) && input.getCount() == result.getCount())
            return false;
        activeEnchanting = new ActiveEnchanting(
                input,
                result.copy(),
                cost,
                special,
                special && !cursed);
        processingTime = duration;
        notifyUpdate();
        return true;
    }

    private void finishProcessing() {
        processingTime = -1;
        activeEnchanting = null;
    }

    private void cancelProcessing() {
        if (processingTime != -1 || activeEnchanting != null) {
            finishProcessing();
            notifyUpdate();
        }
    }

    public RandomSource getRandom() {
        return RandomSource.create(seed != null ? seed : worldPosition.asLong());
    }

    public void nextSeed() {
        assert level != null;
        seed = level.random.nextLong();
    }

    private void updateEnchanterIfLevelReady() {
        if (level != null)
            enchanter.update(heldItem);
    }

    public int getMaxEnchantLevel() {
        return getMaxEnchantLevel(getHeatLevel() == HeatLevel.SEETHING);
    }

    public int getMaxEnchantLevel(boolean special) {
        int max = CEIConfig.enchantments().blazeEnchanterMaxEnchantLevel.get();
        int maxSuper = CEIConfig.enchantments().blazeEnchanterMaxSuperEnchantLevel.get();
        return special ? Math.max(max, maxSuper) : Mth.clamp(max, 0, maxSuper);
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        assert level != null;
        if (!heldItem.isEmpty())
            return stack;
        var input = stack.copy();
        var inserted = input.split(1);
        enchanter.update(inserted);
        boolean canProcess = enchanter.canProcess(inserted);
        enchanter.update(heldItem);
        if (!canProcess) {
            return stack;
        }
        if (simulate)
            return input;
        heldItem = inserted;
        enchanter.update(heldItem);
        notifyUpdate();
        return input;
    }

    public ItemStack extractItem(boolean forced, boolean simulate) {
        assert level != null;
        ItemStack extracted = ItemStack.EMPTY;
        if (forced || activeEnchanting == null && processingTime <= 0) {
            extracted = heldItem.copy();
            if (!simulate) {
                heldItem = ItemStack.EMPTY;
                finishProcessing();
                enchanter.update(heldItem);
                notifyUpdate();
            }
        }
        return extracted;
    }

    public ItemStack insertAutomationItem(ItemStack stack, TransactionContext transaction) {
        assert level != null;
        if (heldItem.isEmpty()) {
            ItemStack input = stack.copy();
            ItemStack inserted = input.split(1);
            enchanter.update(inserted);
            boolean canProcess = enchanter.canProcess(inserted);
            enchanter.update(heldItem);
            if (canProcess) {
                automationState.updateSnapshots(transaction);
                heldItem = inserted;
                enchanter.update(heldItem);
                return input;
            }
        }
        return stack;
    }

    public ItemStack extractAutomationItem(int amount, TransactionContext transaction) {
        assert level != null;
        if (amount <= 0 || !(activeEnchanting == null && processingTime <= 0) || heldItem.isEmpty())
            return ItemStack.EMPTY;
        automationState.updateSnapshots(transaction);
        int extractedCount = Math.min(amount, heldItem.getCount());
        ItemStack extracted = heldItem.copy();
        extracted.setCount(extractedCount);
        heldItem.shrink(extractedCount);
        if (heldItem.isEmpty())
            finishProcessing();
        enchanter.update(heldItem);
        return extracted;
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = false;
        added |= enchanter.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return added;
    }

    @Override
    public void clearContent() {
        heldItem = ItemStack.EMPTY;
        finishProcessing();
        enchanter.update(heldItem);
    }

    private record AutomationState(
            ItemStack heldItem,
            @Nullable Long seed,
            int processingTime,
            @Nullable ActiveEnchanting activeEnchanting) {}

    protected record ActiveEnchanting(
            ItemStack input,
            ItemStack result,
            int cost,
            boolean special,
            boolean strikeLightning) {
        void write(ValueOutput output) {
            output.store("Input", ItemStack.CODEC, input);
            output.store("Result", ItemStack.CODEC, result);
            output.putInt("Cost", cost);
            output.putBoolean("Special", special);
            output.putBoolean("StrikeLightning", strikeLightning);
        }

        static @Nullable ActiveEnchanting load(ValueInput inputView) {
            ItemStack input = inputView.read("Input", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            ItemStack result = inputView.read("Result", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            int cost = inputView.getIntOr("Cost", 0);
            if (input.isEmpty() || result.isEmpty() || cost <= 0)
                return null;
            return new ActiveEnchanting(
                    input,
                    result,
                    cost,
                    inputView.getBooleanOr("Special", false),
                    inputView.getBooleanOr("StrikeLightning", false));
        }

        boolean matches(ItemStack stack) {
            return input.getCount() == stack.getCount() && ItemStack.isSameItemSameComponents(input, stack);
        }
    }
}
