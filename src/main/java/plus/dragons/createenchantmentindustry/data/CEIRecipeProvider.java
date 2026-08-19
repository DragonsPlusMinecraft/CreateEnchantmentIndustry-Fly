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

package plus.dragons.createenchantmentindustry.data;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.fluids.transfer.EmptyingRecipe;
import com.zurrtum.create.content.fluids.transfer.FillingRecipe;
import com.zurrtum.create.content.kinetics.crusher.CrushingRecipe;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationRecipe;
import com.zurrtum.create.content.kinetics.mixer.CompactingRecipe;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SmithingTransformRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import plus.dragons.createdragonsplus.registry.CDPBlocks;
import plus.dragons.createdragonsplus.registry.CDPItems;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;
import plus.dragons.createenchantmentindustry.util.CEIFluidUnits;

/** Generates the core-only recipe set through the live 26.1.2 recipe codecs. */
public final class CEIRecipeProvider extends FabricRecipeProvider {
    public CEIRecipeProvider(
            FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(
            HolderLookup.Provider registryLookup, RecipeOutput exporter) {
        return new Provider(registryLookup, exporter);
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Recipes";
    }

    private static final class Provider extends RecipeProvider {
        private static final TagKey<Item> EGGS = itemTag("c", "eggs");
        private static final TagKey<Item> BRASS_PLATES = itemTag("c", "plates/brass");
        private static final TagKey<Item> STORAGE_BLOCKS_IRON = itemTag("c", "storage_blocks/iron");

        private Provider(HolderLookup.Provider registries, RecipeOutput output) {
            super(registries, output);
        }

        @Override
        public void buildRecipes() {
            buildMachineRecipes();
            buildMaterialRecipes();
            buildExperienceRecipes();
        }

        private void buildMachineRecipes() {
            shaped(RecipeCategory.MISC, CEIBlocks.MECHANICAL_GRINDSTONE.get())
                    .define('a', AllItems.ANDESITE_ALLOY)
                    .define('s', AllItems.SHAFT)
                    .pattern("aaa")
                    .pattern("asa")
                    .pattern("aaa")
                    .unlockedBy("has_andesite_alloy", has(AllItems.ANDESITE_ALLOY))
                    .save(output, key("crafting/mechanical_grindstone"));

            save(
                    "item_application/experience_hatch",
                    new ManualApplicationRecipe(
                            List.of(new ProcessingOutput(CEIBlocks.EXPERIENCE_HATCH.get(), 1)),
                            false,
                            Ingredient.of(CDPBlocks.FLUID_HATCH.get()),
                            Ingredient.of(AllBlocks.EXPERIENCE_BLOCK)));

            shaped(RecipeCategory.MISC, CEIBlocks.PRINTER.get())
                    .define('-', BRASS_PLATES)
                    .define('o', AllBlocks.SPOUT)
                    .define('=', STORAGE_BLOCKS_IRON)
                    .pattern("-")
                    .pattern("o")
                    .pattern("=")
                    .unlockedBy("has_brass_ingot", has(AllItems.BRASS_INGOT))
                    .save(output, key("crafting/printer"));

            shaped(RecipeCategory.MISC, CEIBlocks.EXPERIENCE_LANTERN.get())
                    .define('a', AllBlocks.EXPERIENCE_BLOCK)
                    .define('s', Blocks.SPONGE)
                    .define('c', AllBlocks.COPPER_CASING)
                    .pattern("a")
                    .pattern("s")
                    .pattern("c")
                    .unlockedBy("has_copper_casing", has(AllBlocks.COPPER_CASING))
                    .save(output, key("crafting/experience_lantern"));

            smithing(
                    "smithing/blaze_enchanter",
                    Items.ENCHANTING_TABLE,
                    CEIBlocks.BLAZE_ENCHANTER.get().asItem());
            smithing("smithing/blaze_forger", Items.ANVIL, CEIBlocks.BLAZE_FORGER.get().asItem());
            smithing(
                    "smithing/classic_blaze_enchanter",
                    CEIItems.BLAZES_ENCHANTING_HANDBOOK.get(),
                    CEIBlocks.CLASSIC_BLAZE_ENCHANTER.get().asItem());
        }

        private void buildMaterialRecipes() {
            shapeless(RecipeCategory.MISC, CEIItems.SUPER_EXPERIENCE_NUGGET.get(), 9)
                    .requires(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get())
                    .unlockedBy(
                            "has_super_experience_block",
                            has(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get()))
                    .save(output, key("crafting/super_experience_nugget"));

            shaped(RecipeCategory.MISC, CEIBlocks.SUPER_EXPERIENCE_BLOCK.get())
                    .define('n', CEIItems.SUPER_EXPERIENCE_NUGGET.get())
                    .pattern("nnn")
                    .pattern("nnn")
                    .pattern("nnn")
                    .unlockedBy(
                            "has_super_experience_nugget",
                            has(CEIItems.SUPER_EXPERIENCE_NUGGET.get()))
                    .save(output, key("crafting/super_experience_block"));

            save(
                    "pressing/enchanting_template",
                    new PressingRecipe(
                            List.of(new ProcessingOutput(CEIItems.ENCHANTING_TEMPLATE.get(), 1)),
                            Ingredient.of(AllBlocks.EXPERIENCE_BLOCK)));
            save(
                    "pressing/super_enchanting_template",
                    new PressingRecipe(
                            List.of(new ProcessingOutput(CEIItems.SUPER_ENCHANTING_TEMPLATE.get(), 1)),
                            Ingredient.of(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get())));

            shapeless(RecipeCategory.MISC, CEIItems.BLAZES_ENCHANTING_HANDBOOK.get())
                    .requires(CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE.get())
                    .requires(AllItems.STURDY_SHEET, 2)
                    .requires(Items.EXPERIENCE_BOTTLE, 2)
                    .requires(Blocks.MAGMA_BLOCK)
                    .unlockedBy("has_blaze_burner", has(AllBlocks.BLAZE_BURNER))
                    .save(output, key("crafting/blazes_enchanting_handbook"));

            save(
                    "compacting/experience_cake_base",
                    compacting(
                            List.of(new ProcessingOutput(CEIItems.EXPERIENCE_CAKE_BASE.get(), 1)),
                            List.of(
                                    sized(tag(EGGS), 1),
                                    sized(Ingredient.of(Items.SUGAR), 1),
                                    sized(Ingredient.of(Items.LAPIS_LAZULI), 1)),
                            List.of()));
            save(
                    "filling/experience_cake",
                    new FillingRecipe(
                            new ItemStackTemplate(CEIItems.EXPERIENCE_CAKE.get()),
                            Ingredient.of(CEIItems.EXPERIENCE_CAKE_BASE.get()),
                            experienceIngredient(1000)));
            save(
                    "cutting/experience_cake_slice",
                    new CuttingRecipe(
                            50,
                            List.of(new ProcessingOutput(CEIItems.EXPERIENCE_CAKE_SLICE.get(), 4)),
                            Ingredient.of(CEIItems.EXPERIENCE_CAKE.get())));
        }

        private void buildExperienceRecipes() {
            save(
                    "compacting/experience_block",
                    compacting(
                            List.of(new ProcessingOutput(AllBlocks.EXPERIENCE_BLOCK, 1)),
                            List.of(),
                            List.of(experienceIngredient(27))));
            save(
                    "filling/experience_bottle",
                    new FillingRecipe(
                            new ItemStackTemplate(Items.EXPERIENCE_BOTTLE),
                            Ingredient.of(Items.GLASS_BOTTLE),
                            experienceIngredient(10)));
            save(
                    "emptying/experience_bottle",
                    new EmptyingRecipe(
                            new ItemStackTemplate(Items.GLASS_BOTTLE),
                            experienceStack(10),
                            Ingredient.of(Items.EXPERIENCE_BOTTLE)));

            save(
                    "grinding/experience_nugget",
                    GrindingRecipe.builder(CEICommon.asResource("experience_nugget"))
                            .require(AllItems.EXP_NUGGET)
                            .output(CEIFluids.EXPERIENCE.getSource(), fluidAmount(3))
                            .build()
                            .value());
            save(
                    "grinding/experience_block",
                    GrindingRecipe.builder(CEICommon.asResource("experience_block"))
                            .require(AllBlocks.EXPERIENCE_BLOCK)
                            .output(CEIFluids.EXPERIENCE.getSource(), fluidAmount(27))
                            .build()
                            .value());
            save(
                    "grinding/super_experience_nugget",
                    GrindingRecipe.builder(CEIItems.SUPER_EXPERIENCE_NUGGET.getId())
                            .require(CEIItems.SUPER_EXPERIENCE_NUGGET.get())
                            .output(CEIFluids.EXPERIENCE.getSource(), fluidAmount(3))
                            .build()
                            .value());
            save(
                    "grinding/super_experience_block",
                    GrindingRecipe.builder(CEIBlocks.SUPER_EXPERIENCE_BLOCK.getId())
                            .require(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get())
                            .output(CEIFluids.EXPERIENCE.getSource(), fluidAmount(27))
                            .build()
                            .value());

            save(
                    "crushing/infested_cobblestone",
                    new CrushingRecipe(
                            100,
                            List.of(
                                    new ProcessingOutput(Blocks.GRAVEL, 1),
                                    new ProcessingOutput(AllItems.EXP_NUGGET, 1, .5F)),
                            Ingredient.of(Blocks.INFESTED_COBBLESTONE)));
            save(
                    "compacting/infested_stone",
                    compacting(
                            List.of(
                                    new ProcessingOutput(Blocks.STONE_BRICKS, 1),
                                    new ProcessingOutput(AllItems.EXP_NUGGET, 1)),
                            List.of(sized(Ingredient.of(Blocks.INFESTED_STONE), 4)),
                            List.of()));
        }

        private void smithing(String path, ItemLike addition, Item result) {
            SmithingTransformRecipeBuilder.smithing(
                    Ingredient.of(CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE.get()),
                    Ingredient.of(AllBlocks.BLAZE_BURNER),
                    Ingredient.of(addition),
                    RecipeCategory.MISC,
                    result)
                    .unlocks("has_blaze_burner", has(AllBlocks.BLAZE_BURNER))
                    .save(output, key(path));
        }

        private void save(String path, Recipe<?> recipe) {
            output.accept(key(path), recipe, null);
        }

        private static CompactingRecipe compacting(
                List<ProcessingOutput> results,
                List<SizedIngredient> ingredients,
                List<FluidIngredient> fluids) {
            return new CompactingRecipe(results, HeatCondition.NONE, fluids, ingredients);
        }

        private static SizedIngredient sized(Ingredient ingredient, int count) {
            return new SizedIngredient(ingredient, count);
        }

        private static FluidStackIngredient experienceIngredient(long millibuckets) {
            return new FluidStackIngredient(
                    CEIFluids.EXPERIENCE.getSource(), DataComponentPatch.EMPTY, Math.toIntExact(CEIFluidUnits.millibuckets(millibuckets)));
        }

        private static FluidStack experienceStack(long millibuckets) {
            return new FluidStack(
                    CEIFluids.EXPERIENCE.getSource(), Math.toIntExact(CEIFluidUnits.millibuckets(millibuckets)));
        }

        private static int fluidAmount(long millibuckets) {
            return Math.toIntExact(CEIFluidUnits.millibuckets(millibuckets));
        }

        private static ResourceKey<Recipe<?>> key(String path) {
            return ResourceKey.create(Registries.RECIPE, CEICommon.asResource(path));
        }

        private static TagKey<Item> itemTag(String namespace, String path) {
            return TagKey.create(
                    Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
        }
    }
}
