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

import static com.zurrtum.create.client.compat.jei.CreateCategory.addFluidSlot;

import com.zurrtum.create.client.compat.jei.category.SequencedAssemblyCategory;
import java.util.Optional;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

/** Rendering bridge used when a grinding recipe appears as a sequenced-assembly step. */
public final class AssemblyGrindingCategory
        extends SequencedAssemblyCategory.SequencedRenderer<GrindingRecipe> {
    @Override
    public IRecipeSlotBuilder addSlot(
            IRecipeLayoutBuilder builder, int x, int y, GrindingRecipe recipe) {
        if (recipe.fluidIngredients().isEmpty())
            return null;
        return addFluidSlot(builder, x, y, recipe.fluidIngredients().getFirst());
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics, int index, int x, int y, Optional<IRecipeSlotView> slot) {
        graphics.item(CEIBlocks.GRINDSTONE_DRAIN.asStack(), x + 1, y + 23);
    }
}
