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

package plus.dragons.createenchantmentindustry.common.registry;

import java.util.function.Supplier;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import plus.dragons.createdragonsplus.api.recipe.CDPRecipeTypeInfo;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingRecipe;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;

/** Recipe type/serializer pairs registered without Registrate. */
public final class CEIRecipes {
    private static RecipeType<PrintingRecipe> printingType;
    private static RecipeSerializer<PrintingRecipe> printingSerializer;
    private static RecipeType<GrindingRecipe> grindingType;
    private static RecipeSerializer<GrindingRecipe> grindingSerializer;

    public static final CDPRecipeTypeInfo<PrintingRecipe> PRINTING = info(
            "printing", () -> printingType, () -> printingSerializer);
    public static final CDPRecipeTypeInfo<GrindingRecipe> GRINDING = info(
            "grinding", () -> grindingType, () -> grindingSerializer);

    private static boolean registered;

    private CEIRecipes() {}

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        printingType = registerType(PRINTING.getId());
        grindingType = registerType(GRINDING.getId());
        printingSerializer = registerSerializer(PRINTING.getId(), new PrintingRecipe.Serializer());
        grindingSerializer = registerSerializer(GRINDING.getId(), new GrindingRecipe.Serializer());
        RecipeSynchronization.synchronizeRecipeSerializer(printingSerializer);
        RecipeSynchronization.synchronizeRecipeSerializer(grindingSerializer);
    }

    private static <R extends Recipe<?>> CDPRecipeTypeInfo<R> info(
            String path,
            Supplier<? extends RecipeType<R>> type,
            Supplier<? extends RecipeSerializer<R>> serializer) {
        return new CDPRecipeTypeInfo<>(CEICommon.asResource(path), type, serializer);
    }

    private static <R extends Recipe<?>> RecipeType<R> registerType(Identifier id) {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<R>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    private static <R extends Recipe<?>> RecipeSerializer<R> registerSerializer(
            Identifier id, RecipeSerializer<R> serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, serializer);
    }
}
