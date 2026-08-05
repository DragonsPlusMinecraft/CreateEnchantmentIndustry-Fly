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

import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlock;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class ClassicBlazeEnchanterArmInteractionPoint extends ArmInteractionPoint {
    public ClassicBlazeEnchanterArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    @Override
    public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
        if (!CEIConfig.features().classicBlazeEnchanter.get())
            return stack;
        try (Transaction transaction = Transaction.openOuter()) {
            if (!(level.getBlockEntity(pos) instanceof ClassicBlazeEnchanterBlockEntity enchanter)) {
                return stack;
            }
            ItemStack input = stack.copy();
            ItemStack fuelRemainder = BlazeExperienceBlock.applyFuel(cachedState, level, pos, input, transaction);
            ItemStack remainder = fuelRemainder != null
                    ? fuelRemainder
                    : enchanter.insertAutomationItem(input, transaction);
            if (!simulate)
                transaction.commit();
            return remainder;
        }
    }

    @Override
    public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
        if (!CEIConfig.features().classicBlazeEnchanter.get())
            return ItemStack.EMPTY;
        try (Transaction transaction = Transaction.openOuter()) {
            if (level.getBlockEntity(pos) instanceof ClassicBlazeEnchanterBlockEntity enchanter) {
                ItemStack extracted = enchanter.extractAutomationItem(amount, transaction);
                if (!simulate)
                    transaction.commit();
                return extracted;
            }
            return ItemStack.EMPTY;
        }
    }

    public static class Type extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return CEIConfig.features().classicBlazeEnchanter.get() && CEIBlocks.CLASSIC_BLAZE_ENCHANTER.has(state);
        }

        @Nullable
        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ClassicBlazeEnchanterArmInteractionPoint(this, level, pos, state);
        }
    }
}
