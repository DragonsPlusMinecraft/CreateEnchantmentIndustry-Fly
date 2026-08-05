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

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.advancement.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlock;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class ClassicBlazeEnchanterBlock extends BlazeExperienceBlock<ClassicBlazeEnchanterBlockEntity> {
    public static final MapCodec<ClassicBlazeEnchanterBlock> CODEC = simpleCodec(ClassicBlazeEnchanterBlock::new);

    public ClassicBlazeEnchanterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        AdvancementBehaviour.setPlacedBy(level, pos, placer);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!CEIConfig.features().classicBlazeEnchanter.get())
            return InteractionResult.PASS;
        var blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null)
            return InteractionResult.PASS;
        ItemStack extracted = blockEntity.extractItem(true, false);
        if (!extracted.isEmpty()) {
            player.getInventory().placeItemBackInInventory(extracted);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (!CEIConfig.features().classicBlazeEnchanter.get())
            return InteractionResult.PASS;
        var result = super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        if (result != InteractionResult.PASS)
            return result;
        var blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null)
            return InteractionResult.PASS;
        var remainder = blockEntity.insertItem(stack, false);
        if (ItemStack.isSameItemSameComponents(stack, remainder) && remainder.getCount() == stack.getCount())
            return InteractionResult.PASS;
        player.setItemInHand(hand, remainder);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MapCodec<ClassicBlazeEnchanterBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<ClassicBlazeEnchanterBlockEntity> getBlockEntityClass() {
        return ClassicBlazeEnchanterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ClassicBlazeEnchanterBlockEntity> getBlockEntityType() {
        return CEIBlockEntities.CLASSIC_BLAZE_ENCHANTER.get();
    }
}
