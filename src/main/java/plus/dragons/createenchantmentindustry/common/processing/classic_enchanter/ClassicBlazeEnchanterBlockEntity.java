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

package plus.dragons.createenchantmentindustry.common.processing.classic_enchanter;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.advancement.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.CEIEnchantmentHelper;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class ClassicBlazeEnchanterBlockEntity extends BlazeExperienceBlockEntity implements Clearable {
    protected static final int ENCHANTING_TIME = 200;
    protected ItemStack heldItem = ItemStack.EMPTY;
    protected int processingTime = -1;
    protected boolean special;
    protected boolean cursed;
    protected ClassicEnchanterBehaviour enchanter;
    protected AdvancementBehaviour advancement;
    protected DirectBeltInputBehaviour beltInput;
    protected @Nullable ActiveEnchanting activeEnchanting;
    private final SnapshotParticipant<AutomationState> automationState = new SnapshotParticipant<>() {
        @Override
        protected AutomationState createSnapshot() {
            return new AutomationState(heldItem.copy(), processingTime, activeEnchanting);
        }

        @Override
        protected void readSnapshot(AutomationState snapshot) {
            heldItem = snapshot.heldItem().copy();
            processingTime = snapshot.processingTime();
            activeEnchanting = snapshot.activeEnchanting();
        }

        @Override
        protected void onFinalCommit() {
            notifyUpdate();
        }
    };
    float flip;
    float oFlip;
    float flipT;
    float flipA;

    public ClassicBlazeEnchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public LerpedFloat headAngle() {
        return this.headAngle;
    }

    @Override
    protected int getExperienceTankCapacity() {
        return CEIConfig.processing().classicBlazeEnchanterFluidCapacity.get();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        enchanter = new ClassicEnchanterBehaviour(this);
        advancement = new AdvancementBehaviour(this);
        behaviours.add(enchanter);
        behaviours.add(advancement);
        beltInput = new DirectBeltInputBehaviour(this)
                .onlyInsertWhen(side -> heldItem.isEmpty())
                .setInsertionHandler(((transportedItemStack, side, simulate) -> this.insertItem(transportedItemStack.stack, simulate)))
                .allowingBeltFunnels();
        behaviours.add(beltInput);
    }

    @Override
    public boolean isActive() {
        return processingTime > 0;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null)
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), heldItem);
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        assert level != null;
        if (!CEIConfig.features().classicBlazeEnchanter.get())
            return stack;
        if (!heldItem.isEmpty())
            return stack;
        var input = stack.copy();
        var inserted = input.split(1);
        if (!enchanter.canProcess(inserted)) {
            return stack;
        }
        if (simulate)
            return input;
        heldItem = inserted;
        notifyUpdate();
        return input;
    }

    public ItemStack extractItem(boolean forced, boolean simulate) {
        assert level != null;
        ItemStack extracted = ItemStack.EMPTY;
        if (forced || isOutputReady()) {
            extracted = heldItem.copy();
            if (!simulate) {
                heldItem = ItemStack.EMPTY;
                finishProcessing();
                notifyUpdate();
            }
        }
        return extracted;
    }

    public ItemStack insertAutomationItem(ItemStack stack, TransactionContext transaction) {
        assert level != null;
        if (!CEIConfig.features().classicBlazeEnchanter.get() || !heldItem.isEmpty())
            return stack;
        ItemStack input = stack.copy();
        ItemStack inserted = input.split(1);
        if (!enchanter.canProcess(inserted))
            return stack;
        automationState.updateSnapshots(transaction);
        heldItem = inserted;
        return input;
    }

    public ItemStack extractAutomationItem(int amount, TransactionContext transaction) {
        assert level != null;
        if (!CEIConfig.features().classicBlazeEnchanter.get()
                || amount <= 0
                || !isOutputReady())
            return ItemStack.EMPTY;
        automationState.updateSnapshots(transaction);
        int extractedCount = Math.min(amount, heldItem.getCount());
        ItemStack extracted = heldItem.copy();
        extracted.setCount(extractedCount);
        heldItem.shrink(extractedCount);
        if (heldItem.isEmpty())
            finishProcessing();
        return extracted;
    }

    public boolean isOutputReady() {
        return activeEnchanting == null
                && !heldItem.isEmpty()
                && processingTime <= 0
                && !enchanter.canProcess(heldItem);
    }

    @Override
    public void tick() {
        super.tick();
        if (!CEIConfig.features().classicBlazeEnchanter.get())
            return;
        boolean special = getHeatLevelFromBlock() == BlazeBurnerBlock.HeatLevel.SEETHING;
        if (this.special != special) {
            this.special = special;
        }
        var strikePos = getStrikePos();
        boolean cursed = special && !worldPosition.equals(strikePos);
        if (this.cursed != cursed) {
            this.cursed = cursed;
        }
        bookTick();
        if (heldItem.isEmpty()) {
            cancelProcessing();
            return;
        }
        if (level.isClientSide() && isVirtual()) {
            tickVirtual();
            return;
        }
        if (!(level instanceof ServerLevel serverLevel))
            return;
        if (activeEnchanting == null) {
            if (enchanter.canProcess(heldItem)) {
                startProcessing(ENCHANTING_TIME, true);
                return;
            }
            tryExport();
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
        if (active.strikeLightning() && strikePos != null && strikeLightning(serverLevel, strikePos)) {
            serverLevel.destroyBlock(worldPosition, false);
            serverLevel.setBlockAndUpdate(worldPosition, AllBlocks.LIT_BLAZE_BURNER.defaultBlockState());
            return;
        }
        if (!consumeExperience(active.cost(), active.special(), false))
            return;
        heldItem = active.result().copy();
        if (active.transcendent()) {
            advancement.trigger(CEIAdvancements.TRANSCENDENT_OVERCLOCK.builtinTrigger());
            advancement.awardStat(CEIStats.SUPER_ENCHANT.get(), 1);
        }
        advancement.awardStat(CEIStats.CLASSIC_ENCHANT.get(), 1);
        finishProcessing();
        notifyUpdate();
        level.playSound(null, worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F);
        spawnEnchantParticles();
    }

    private void tickVirtual() {
        if (activeEnchanting == null) {
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
        consumeExperience(active.cost(), active.special(), false);
        finishProcessing();
    }

    private boolean startProcessing(int duration, boolean requireExperience) {
        if (!enchanter.canProcess(heldItem))
            return false;
        int cost = enchanter.getExperienceCost(heldItem);
        if (cost <= 0 || requireExperience && !consumeExperience(cost, special, true))
            return false;
        ItemStack input = heldItem.copy();
        ItemStack result = enchanter.getResult(input);
        if (ItemStack.isSameItemSameComponents(input, result) && input.getCount() == result.getCount())
            return false;
        activeEnchanting = new ActiveEnchanting(
                input,
                result.copy(),
                cost,
                special,
                special && !cursed,
                isTranscendent(input, result));
        processingTime = duration;
        notifyUpdate();
        return true;
    }

    private static boolean isTranscendent(ItemStack input, ItemStack result) {
        var before = CEIItemData.getEnchantments(input);
        return CEIItemData.getEnchantments(result).entrySet().stream()
                .anyMatch(entry -> entry.getValue() > before.getOrDefault(entry.getKey(), 0)
                        && entry.getValue() > CEIEnchantmentHelper.maxLevel(entry.getKey()));
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

    protected void tryExport() {
        ItemStack funnelRemainder = beltInput.tryExportingToBeltFunnel(heldItem, null, false);
        if (funnelRemainder != null) {
            if (funnelRemainder.getCount() != heldItem.getCount()) {
                heldItem = funnelRemainder;
                notifyUpdate();
            }
            return;
        }
        for (var side : Direction.Plane.HORIZONTAL) {
            BlockPos nextPosition = worldPosition.relative(side);
            DirectBeltInputBehaviour directBeltInputBehaviour = BlockEntityBehaviour.get(level, nextPosition, DirectBeltInputBehaviour.TYPE);
            if (directBeltInputBehaviour != null && directBeltInputBehaviour.canInsertFromSide(side)) {
                ItemStack returned = directBeltInputBehaviour.handleInsertion(heldItem.copy(), side, false);
                if (returned.isEmpty()) {
                    heldItem = ItemStack.EMPTY;
                    notifyUpdate();
                    return;
                } else if (returned.getCount() != heldItem.getCount()) {
                    heldItem = returned.copy();
                    notifyUpdate();
                    return;
                }
            }
        }
    }

    protected void bookTick() {
        if (level.random.nextInt(40) == 0) {
            float oFlipT = flipT;
            while (oFlipT == flipT) {
                flipT += (level.random.nextInt(4) - level.random.nextInt(4));
            }
        }
        oFlip = flip;
        float flipDiff = (flipT - flip) * 0.4F;
        flipDiff = Mth.clamp(flipDiff, -0.2F, 0.2F);
        flipA += (flipDiff - flipA) * 0.9F;
        flip += flipA;
    }

    protected void spawnEnchantParticles() {
        if (isVirtual())
            return;
        Vec3 vec = VecHelper.getCenterOf(worldPosition);
        vec = vec.add(0, 1, 0);
        ParticleOptions particle = ParticleTypes.ENCHANT;
        for (int i = 0; i < 20; i++) {
            Vec3 m = VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1f);
            m = new Vec3(m.x, Math.abs(m.y), m.z);
            level.addAlwaysVisibleParticle(particle, vec.x, vec.y, vec.z, m.x, m.y, m.z);
        }
        level.playLocalSound(vec.x, vec.y, vec.z, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1f, level.random.nextFloat() * .1f + .9f, true);
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        added |= enchanter.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return added;
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        output.putInt("ProcessingTime", this.processingTime);
        output.store("HeldItem", ItemStack.OPTIONAL_CODEC, this.heldItem);
        if (activeEnchanting != null)
            activeEnchanting.write(output.child("ActiveEnchanting"));
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        this.processingTime = input.getIntOr("ProcessingTime", -1);
        this.heldItem = input.read("HeldItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.activeEnchanting = input.child("ActiveEnchanting").map(ActiveEnchanting::load).orElse(null);
        if (processingTime >= 0 && activeEnchanting == null)
            processingTime = -1;
    }

    public LerpedFloat headAnimation() {
        return this.headAnimation;
    }

    @Override
    public void clearContent() {
        heldItem = ItemStack.EMPTY;
        finishProcessing();
    }

    private record AutomationState(
            ItemStack heldItem,
            int processingTime,
            @Nullable ActiveEnchanting activeEnchanting) {}

    protected record ActiveEnchanting(
            ItemStack input,
            ItemStack result,
            int cost,
            boolean special,
            boolean strikeLightning,
            boolean transcendent) {
        void write(ValueOutput output) {
            output.store("Input", ItemStack.CODEC, input);
            output.store("Result", ItemStack.CODEC, result);
            output.putInt("Cost", cost);
            output.putBoolean("Special", special);
            output.putBoolean("StrikeLightning", strikeLightning);
            output.putBoolean("Transcendent", transcendent);
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
                    inputView.getBooleanOr("StrikeLightning", false),
                    inputView.getBooleanOr("Transcendent", false));
        }

        boolean matches(ItemStack stack) {
            return input.getCount() == stack.getCount() && ItemStack.isSameItemSameComponents(input, stack);
        }
    }
}
