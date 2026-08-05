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

package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import com.mojang.serialization.DataResult;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;

public interface PrintingBehaviour {
    static void register(Identifier id, Provider provider) {
        PrintingBehaviourRegistry.register(id, provider);
    }

    static void register(Identifier id, int priority, Provider provider) {
        PrintingBehaviourRegistry.register(id, priority, provider);
    }

    static DataResult<PrintingBehaviour> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
        return PrintingBehaviourRegistry.create(level, tank, stack);
    }

    default boolean isValid() {
        return true;
    }

    default boolean isSafeNBT() {
        return true;
    }

    default boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return false;
    }

    int getRequiredItemCount(Level level, ItemStack stack);

    int getRequiredFluidAmount(Level level, ItemStack stack, FluidStack fluidStack);

    ItemStack getResult(Level level, ItemStack stack, FluidStack fluidStack);

    void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer);

    @FunctionalInterface
    interface Provider {
        /**
         * Returns an empty optional when this provider does not handle the template. A present result claims the
         * template and stops provider lookup, regardless of whether that result is a success or an error.
         */
        Optional<DataResult<PrintingBehaviour>> create(Level level, SmartFluidTankBehaviour tank, ItemStack stack);
    }
}
