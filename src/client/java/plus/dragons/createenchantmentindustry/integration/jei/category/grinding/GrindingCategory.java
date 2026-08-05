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

package plus.dragons.createenchantmentindustry.integration.jei.category.grinding;

import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.renderer.IconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.util.CEILang;

/** JEI 27 category backed directly by synced {@link RecipeHolder}s. */
public final class GrindingCategory extends CreateCategory<RecipeHolder<GrindingRecipe>> {
    public static final IRecipeHolderType<GrindingRecipe> TYPE = IRecipeHolderType.create(CEIRecipes.GRINDING.getType());

    @Override
    public @NotNull IRecipeHolderType<GrindingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return CEILang.translate("recipe.grinding").component();
    }

    @Override
    public IDrawable getIcon() {
        return new IconRenderer(CEIBlocks.GRINDSTONE_DRAIN.asStack());
    }

    @Override
    public int getHeight() {
        return 70;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder, RecipeHolder<GrindingRecipe> entry, IFocusGroup focuses) {
        GrindingRecipe recipe = entry.value();
        builder.addInputSlot(27, 32).setBackground(SLOT, -1, -1).add(recipe.ingredient());

        if (!recipe.fluidIngredients().isEmpty())
            addFluidSlot(builder, 27, 51, recipe.fluidIngredients().getFirst())
                    .setBackground(SLOT, -1, -1);

        int index = 0;
        if (!recipe.fluidResults().isEmpty()) {
            addFluidSlot(builder, 130, 32, recipe.fluidResults().getFirst())
                    .setBackground(SLOT, -1, -1);
            index++;
        }
        List<ProcessingOutput> results = recipe.results();
        for (ProcessingOutput result : results) {
            int x = 130 + (index % 2) * 19;
            int y = 32 - (index / 2) * 19;
            addChanceSlot(builder, x, y, result);
            index++;
        }
    }

    @Override
    public void draw(
            RecipeHolder<GrindingRecipe> entry,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 115, 5);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 52, 35);
        AllGuiTextures.JEI_SHADOW.render(graphics, 61, 52);
    }
}
