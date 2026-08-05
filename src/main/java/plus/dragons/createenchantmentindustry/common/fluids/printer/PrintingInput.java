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

package plus.dragons.createenchantmentindustry.common.fluids.printer;

import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/** Immutable two-item recipe input with an additional fluid stack. */
public final class PrintingInput implements RecipeInput {
    private final ItemStack base;
    private final ItemStack template;
    private final FluidStack fluid;

    public PrintingInput(ItemStack base, ItemStack template, FluidStack fluid) {
        this.base = base;
        this.template = template;
        this.fluid = fluid;
    }

    public ItemStack base() {
        return base;
    }

    public ItemStack template() {
        return template;
    }

    public FluidStack fluid() {
        return fluid;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case 0 -> base;
            case 1 -> template;
            default -> ItemStack.EMPTY;
        };
    }
}
