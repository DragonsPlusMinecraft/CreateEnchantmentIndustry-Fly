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

package plus.dragons.createenchantmentindustry.integration.jei.category;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.category.SequencedAssemblyCategory;
import com.zurrtum.create.client.compat.jei.display.MysteriousItemConversionDisplay;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.common.Internal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jetbrains.annotations.NotNull;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindStoneItem;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.integration.jei.category.assembly.AssemblyGrindingCategory;
import plus.dragons.createenchantmentindustry.integration.jei.category.assembly.AssemblyPrintingCategory;
import plus.dragons.createenchantmentindustry.integration.jei.category.grinding.GrindingCategory;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.AddressPrintingRecipeJEI;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.BannerPatternPrintingRecipeJEI;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.CopyPrintingRecipeJEI;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.CustomNamePrintingRecipeJEI;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.EnchantedBookPrintingRecipeJEI;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.PatternPrintingRecipeJEI;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.PrintingCategory;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.PrintingRecipeJEI;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.StandardPrintingRecipeJEI;
import plus.dragons.createenchantmentindustry.integration.jei.category.printing.WrittenBookPrintingRecipeJEI;

/** Optional JEI 27 plugin. No class in this package is loaded when JEI is absent. */
@JeiPlugin
public final class CEIJeiPlugin implements IModPlugin {
    private static final Identifier ID = CEICommon.asResource("jei_plugin");

    @Override
    public @NotNull Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        SequencedAssemblyCategory.registerRenderer(
                CEIRecipes.GRINDING.getType(), new AssemblyGrindingCategory());
        SequencedAssemblyCategory.registerRenderer(
                CEIRecipes.PRINTING.getType(), new AssemblyPrintingCategory());
        registration.addRecipeCategories(new PrintingCategory(), new GrindingCategory());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeMap recipes = Internal.getClientSyncedRecipes();

        List<PrintingRecipeJEI> printing = new ArrayList<>();
        recipes.byType(CEIRecipes.PRINTING.getType()).stream()
                .map(StandardPrintingRecipeJEI::new)
                .forEach(printing::add);
        addBuiltInPrintingRecipes(printing);
        registration.addRecipes(PrintingCategory.TYPE, printing);

        List<RecipeHolder<GrindingRecipe>> grinding = new ArrayList<>(recipes.byType(CEIRecipes.GRINDING.getType()));
        recipes.byType(AllRecipeTypes.SANDPAPER_POLISHING).stream()
                .map(GrindingRecipe::fromPolishing)
                .flatMap(java.util.Optional::stream)
                .forEach(grinding::add);
        registration.addRecipes(GrindingCategory.TYPE, grinding);

        RecipeHolder<ManualApplicationRecipe> grindstoneApplication = new RecipeHolder<>(
                ResourceKey.create(
                        Registries.RECIPE, CEICommon.asResource("mechanical_grindstone_application")),
                MechanicalGrindStoneItem.createRecipe());
        registration.addRecipes(JeiClientPlugin.ITEM_APPLICATION, List.of(grindstoneApplication));

        registration.addRecipes(
                JeiClientPlugin.MYSTERY_CONVERSION,
                List.of(new MysteriousItemConversionDisplay(
                        CEICommon.asResource("super_experience_block"),
                        AllBlocks.EXPERIENCE_BLOCK.asItem(),
                        CEIBlocks.SUPER_EXPERIENCE_BLOCK.get().asItem())));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(PrintingCategory.TYPE, CEIBlocks.PRINTER.get());
        registration.addCraftingStation(
                GrindingCategory.TYPE,
                CEIBlocks.MECHANICAL_GRINDSTONE.get(),
                CEIBlocks.GRINDSTONE_DRAIN.get());
    }

    private static void addBuiltInPrintingRecipes(List<PrintingRecipeJEI> recipes) {
        if (CEIConfig.fluids().enablePackageAddressPrinting.get())
            recipes.add(AddressPrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enablePackagePatternPrinting.get())
            recipes.add(PatternPrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enableCreateCopiableItemPrinting.get())
            recipes.add(CopyPrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enableCustomNamePrinting.get())
            recipes.add(CustomNamePrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enableWrittenBookPrinting.get())
            recipes.add(WrittenBookPrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enableBannerPatternPrinting.get())
            recipes.add(BannerPatternPrintingRecipeJEI.INSTANCE);
        if (CEIConfig.fluids().enableEnchantedBookPrinting.get())
            recipes.addAll(EnchantedBookPrintingRecipeJEI.listAll());
    }
}
