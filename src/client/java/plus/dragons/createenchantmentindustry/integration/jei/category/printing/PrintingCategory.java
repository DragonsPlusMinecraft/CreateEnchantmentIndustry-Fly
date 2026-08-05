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

package plus.dragons.createenchantmentindustry.integration.jei.category.printing;

import com.zurrtum.create.client.compat.jei.CreateCategory;
import com.zurrtum.create.client.compat.jei.renderer.IconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.util.CEILang;

/** Shared layout for data-pack printing recipes and CEI's built-in printing behaviours. */
public final class PrintingCategory extends CreateCategory<PrintingRecipeJEI> {
    public static final IRecipeType<PrintingRecipeJEI> TYPE = IRecipeType.create(CEIRecipes.PRINTING.getId(), PrintingRecipeJEI.class);

    @Override
    public @NotNull IRecipeType<PrintingRecipeJEI> getRecipeType() {
        return TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return CEILang.translate("recipe.printing").component();
    }

    @Override
    public IDrawable getIcon() {
        return new IconRenderer(CEIBlocks.PRINTER.asStack());
    }

    @Override
    public int getHeight() {
        return 70;
    }

    @Override
    public Identifier getIdentifier(PrintingRecipeJEI recipe) {
        return recipe.getRegistryName();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PrintingRecipeJEI recipe, IFocusGroup focuses) {
        var base = builder.addInputSlot(27, 51).setBackground(SLOT, -1, -1);
        recipe.setBase(base);

        var template = builder.addInputSlot(51, 5).setBackground(SLOT, -1, -1);
        recipe.setTemplate(template);

        // A capacity of one intentionally renders every configured cost as a full, readable slot.
        var fluid = builder.addInputSlot(27, 32)
                .setBackground(SLOT, -1, -1)
                .setFluidRenderer(1, false, 16, 16);
        recipe.setFluid(fluid);

        var output = builder.addOutputSlot(132, 51).setBackground(SLOT, -1, -1);
        recipe.setOutput(output);
    }

    @Override
    public void onDisplayedIngredientsUpdate(
            PrintingRecipeJEI recipe, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
        if (recipeSlots.size() >= 4)
            recipe.onDisplayedIngredientsUpdate(
                    recipeSlots.get(0), recipeSlots.get(1), recipeSlots.get(2), recipeSlots.get(3), focuses);
    }

    @Override
    public void draw(
            PrintingRecipeJEI recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY) {
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 47, 27);
        AllGuiTextures.JEI_LONG_ARROW.render(graphics, 70, 54);
        AllGuiTextures.JEI_SHADOW.render(graphics, 62, 57);
    }
}
