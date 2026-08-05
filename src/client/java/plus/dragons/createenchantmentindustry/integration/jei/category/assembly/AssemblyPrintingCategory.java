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

package plus.dragons.createenchantmentindustry.integration.jei.category.assembly;

import static com.zurrtum.create.client.compat.jei.CreateCategory.EMPTY;
import static com.zurrtum.create.client.compat.jei.CreateCategory.addFluidSlot;

import com.zurrtum.create.client.compat.jei.category.SequencedAssemblyCategory;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.client.gui.GuiGraphics;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingRecipe;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

/** Rendering bridge used when a printing recipe appears as a sequenced-assembly step. */
public final class AssemblyPrintingCategory
        extends SequencedAssemblyCategory.SequencedRenderer<PrintingRecipe> {
    @Override
    public IRecipeSlotBuilder addSlot(
            IRecipeLayoutBuilder builder, int x, int y, PrintingRecipe recipe) {
        IRecipeSlotBuilder template = builder.addInputSlot(x - 9, y)
                .setBackground(EMPTY, 0, 0)
                .add(recipe.ingredients().get(1));
        addFluidSlot(builder, x + 9, y, recipe.fluidIngredients().getFirst());
        return template;
    }

    @Override
    public void render(
            GuiGraphics graphics, int index, int x, int y, Optional<IRecipeSlotView> slot) {
        graphics.renderItem(CEIBlocks.PRINTER.asStack(), x + 1, y + 23);
    }
}
