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
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.behaviour.EnchantingBehaviour;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.behaviour.TemplateEnchantingBehaviour;
import plus.dragons.createenchantmentindustry.util.CEILang;

public class EnchanterBehaviour extends ServerScrollValueBehaviour {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final BehaviourType<EnchanterBehaviour> TYPE = (BehaviourType) ServerScrollValueBehaviour.TYPE;
    public static final String LEVEL = "EnchantingLevel";
    public static final String TEMPLATE = "EnchantingTemplate";
    private final BlazeEnchanterBlockEntity enchanter;
    private ItemStack template = ItemStack.EMPTY;
    private EnchantingBehaviour enchanting = new EnchantingBehaviour();

    public EnchanterBehaviour(BlazeEnchanterBlockEntity enchanter) {
        super(enchanter);
        this.enchanter = enchanter;
    }

    public boolean canProcess(ItemStack stack) {
        return enchanting.canProcess(blockEntity.getLevel(), stack, enchanter.special);
    }

    public void update(ItemStack stack) {
        enchanting.update(
                blockEntity.getLevel(), stack, value, enchanter.special, enchanter.cursed, enchanter.getRandom());
    }

    public ItemStack getResult(ItemStack stack) {
        return enchanting.getResult(blockEntity.getLevel(), stack, enchanter.getRandom(), enchanter.special);
    }

    public int getExperienceCost() {
        return enchanting.getExperienceCost(enchanter.special, isTemplateMode());
    }

    public ItemStack getTemplate() {
        return template;
    }

    public boolean isTemplateMode() {
        return !template.isEmpty();
    }

    public boolean setTemplate(ItemStack stack) {
        if (!loadTemplate(stack))
            return false;
        update(enchanter.heldItem);
        if (!blockEntity.getLevel().isClientSide()) {
            blockEntity.setChanged();
            blockEntity.sendData();
        }
        return true;
    }

    private boolean loadTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            template = ItemStack.EMPTY;
            enchanting = new EnchantingBehaviour();
        } else if (stack.isEnchantable()) {
            template = stack;
            enchanting = new TemplateEnchantingBehaviour(template);
        } else return false;
        return true;
    }

    @Override
    public void setValue(int value) {
        value = Mth.clamp(value, 0, enchanter.getMaxEnchantLevel());
        if (value == this.value)
            return;
        this.value = value;
        update(enchanter.heldItem);
        blockEntity.setChanged();
        blockEntity.sendData();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        var stack = player.getItemInHand(hand);
        if (stack.is(AllItems.WRENCH))
            return;
        if (stack.is(AllBlocks.MECHANICAL_ARM.asItem()))
            return;
        var level = blockEntity.getLevel();
        var pos = getPos();
        if (stack.isEmpty()) {
            setTemplate(ItemStack.EMPTY);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, .25f, .1f);
            return;
        }
        ItemStack template = stack.copy();
        template.setCount(1);
        if (!setTemplate(template)) {
            player.displayClientMessage(CEILang.translate("gui.blaze_enchanter.template.invalid").component(), true);
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }

    @Override
    public void write(ValueOutput output, boolean clientPacket) {
        output.putInt(LEVEL, value);
        output.store(TEMPLATE, ItemStack.OPTIONAL_CODEC, template);
    }

    @Override
    public void writeSafe(ValueOutput output) {
        output.putInt(LEVEL, value);
    }

    @Override
    public void read(ValueInput input, boolean clientPacket) {
        value = Mth.clamp(input.getIntOr(LEVEL, 0), 0, enchanter.getMaxEnchantLevel());
        if (!loadTemplate(input.read(TEMPLATE, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY))) {
            value = 0;
            loadTemplate(ItemStack.EMPTY);
        }
    }

    @Override
    public void initialize() {
        loadTemplate(template);
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = true;
        var style = enchanter.special
                ? (enchanter.cursed ? ChatFormatting.RED : ChatFormatting.BLUE)
                : ChatFormatting.GOLD;
        CEILang.translate(
                "gui.goggles.enchanting.blaze_mode",
                CEILang.translate("gui.blaze_enchanter.blaze_mode." + (enchanter.special ? "super" : "normal")).style(style))
                .forGoggles(tooltip);
        CEILang.translate(
                "gui.goggles.enchanting.mode",
                CEILang.translate("gui.blaze_enchanter.mode." + (isTemplateMode() ? "template" : "direct")).style(ChatFormatting.AQUA))
                .forGoggles(tooltip);
        if (!template.isEmpty()) {
            CEILang.translate("gui.goggles.enchanting.template").forGoggles(tooltip);
            CEILang.item(template).style(ChatFormatting.GRAY).forGoggles(tooltip, 1);
        }
        if (value > 0) {
            CEILang.translate("gui.goggles.enchanting.level", CEILang.number(value).style(style))
                    .forGoggles(tooltip);
        } else {
            CEILang.translate("gui.goggles.enchanting.level.not_set").style(ChatFormatting.RED).forGoggles(tooltip);
        }
        if (enchanter.special && enchanter.cursed) {
            CEILang.translate("gui.goggles.enchanting.blocked_super_penalty")
                    .style(ChatFormatting.RED)
                    .forGoggles(tooltip);
        }
        if (enchanter.heldItem.isEmpty()) {
            addModeHelp(tooltip);
            return true;
        }
        if (enchanter.processingTime == -1
                && (!CEIItemData.getEnchantments(enchanter.heldItem).isEmpty()
                        || !CEIItemData.getStoredEnchantments(enchanter.heldItem).isEmpty())) {
            CEILang.translate("gui.goggles.enchanting.completed").style(ChatFormatting.GREEN).forGoggles(tooltip);
            return true;
        }
        boolean canProcess = canProcess(enchanter.heldItem);
        int cost = canProcess ? getExperienceCost() : 0;
        if (canProcess && cost > 0) {
            CEILang.Builder mb = CEILang.translateCreate("generic.unit.millibuckets");
            CEILang.translate("gui.goggles.enchanting.cost", CEILang.number(cost).add(mb).style(style))
                    .forGoggles(tooltip);
            addAvailableEnchantments(tooltip, isPlayerSneaking);
            int experience = enchanter.special ? enchanter.getSpecialExperience() : enchanter.getTotalExperience();
            if (experience < cost) {
                CEILang.translate(
                        enchanter.special ? "gui.goggles.enchanting.insufficient_super_experience" : "gui.goggles.enchanting.insufficient_experience",
                        CEILang.number(experience).add(mb).style(style),
                        CEILang.number(cost).add(mb).style(style))
                        .style(ChatFormatting.RED)
                        .forGoggles(tooltip);
            }
        } else {
            CEILang.translate("gui.goggles.enchanting.invalid_item").style(ChatFormatting.RED).forGoggles(tooltip);
        }
        return added;
    }

    private void addModeHelp(List<Component> tooltip) {
        String mode = isTemplateMode() ? "template" : "direct";
        CEILang.translate("gui.goggles.enchanting.mode_help." + mode)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        CEILang.translate("gui.goggles.enchanting.requires").forGoggles(tooltip);
        CEILang.translate("gui.goggles.enchanting.requires." + mode)
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
    }

    private void addAvailableEnchantments(List<Component> tooltip, boolean isPlayerSneaking) {
        List<EnchantmentInstance> available = enchanting.getPreviewEnchantments();
        if (available.isEmpty())
            return;
        CEILang.translate("gui.goggles.enchanting.available_targets").forGoggles(tooltip);
        int limit = isPlayerSneaking ? available.size() : Math.min(available.size(), 6);
        for (int i = 0; i < limit; i++) {
            EnchantmentInstance instance = available.get(i);
            var name = Enchantment.getFullname(instance.enchantment(), instance.level()).copy();
            if (instance.enchantment().is(EnchantmentTags.CURSE)) {
                name.append(" ?");
            }
            ChatFormatting style = instance.enchantment().is(EnchantmentTags.CURSE)
                    ? ChatFormatting.RED
                    : ChatFormatting.GRAY;
            CEILang.builder().add(name).style(style).forGoggles(tooltip, 1);
        }
        if (!isPlayerSneaking && available.size() > limit) {
            CEILang.translate("gui.goggles.enchanting.available_targets.more", available.size() - limit)
                    .style(ChatFormatting.DARK_GRAY)
                    .forGoggles(tooltip, 1);
        }
    }
}
