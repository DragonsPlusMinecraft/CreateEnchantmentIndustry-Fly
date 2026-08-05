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

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import com.zurrtum.create.foundation.recipe.TimedRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createdragonsplus.common.recipe.BaseRecipeBuilder;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.util.CEILang;

/** A Create Fly-native, rollable single-item recipe with an optional fluid operation. */
public record GrindingRecipe(
        int time,
        Ingredient ingredient,
        List<ProcessingOutput> results,
        List<FluidIngredient> fluidIngredients,
        List<FluidStack> fluidResults)
        implements CreateSingleStackRollableRecipe, TimedRecipe {

    public static final int DEFAULT_TIME = 100;
    public GrindingRecipe {
        results = List.copyOf(results);
        fluidIngredients = List.copyOf(fluidIngredients);
        fluidResults = List.copyOf(fluidResults);
        if (results.size() > 4)
            throw new IllegalArgumentException("Grinding recipes support at most four item results");
        if (fluidIngredients.size() + fluidResults.size() > 1)
            throw new IllegalArgumentException("Grinding recipes support either one fluid input or one fluid result");
    }

    @Override
    public RecipeSerializer<GrindingRecipe> getSerializer() {
        return CEIRecipes.GRINDING.getSerializer();
    }

    @Override
    public RecipeType<GrindingRecipe> getType() {
        return CEIRecipes.GRINDING.getType();
    }

    public List<Ingredient> getIngredients() {
        return List.of(ingredient);
    }

    public List<ProcessingOutput> getRollableResults() {
        return results;
    }

    public List<FluidIngredient> getFluidIngredients() {
        return fluidIngredients;
    }

    public List<FluidStack> getFluidResults() {
        return fluidResults;
    }

    public int getProcessingDuration() {
        return time;
    }

    public List<ItemStack> rollResults(RandomSource random) {
        return assemble(new SingleRecipeInput(ItemStack.EMPTY), random);
    }

    public List<ItemStack> rollResults() {
        return rollResults(RandomSource.create());
    }

    public Component getDescriptionForAssembly() {
        if (fluidIngredients.isEmpty())
            return CEILang.translate("recipe.assembly.grinding").component();
        List<FluidStack> matching = fluidIngredients.getFirst().getMatchingFluidStacks();
        return matching.isEmpty()
                ? Component.literal("Invalid")
                : CEILang.translate("recipe.assembly.grinding.needs_fluid", matching.getFirst().getName()).component();
    }

    public void addRequiredMachines(Set<ItemLike> required) {
        required.add(CEIBlocks.MECHANICAL_GRINDSTONE.get());
        required.add(AllBlocks.ITEM_DRAIN);
    }

    public static Optional<RecipeHolder<GrindingRecipe>> fromPolishing(
            RecipeHolder<SandPaperPolishingRecipe> polishing) {
        if (!AllRecipeTypes.CAN_BE_AUTOMATED.test(polishing))
            return Optional.empty();
        Identifier id = polishing.id().identifier().withSuffix("_using_grindstone");
        GrindingRecipe recipe = new GrindingRecipe(
                DEFAULT_TIME,
                polishing.value().ingredient(),
                List.of(new ProcessingOutput(polishing.value().result())),
                List.of(),
                List.of());
        return Optional.of(new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe));
    }

    public static Builder builder(Identifier id) {
        return new Builder(id);
    }

    public static final class Builder extends BaseRecipeBuilder<GrindingRecipe, Builder> {
        private int time = DEFAULT_TIME;
        private Ingredient ingredient;
        private final List<ProcessingOutput> results = new ArrayList<>();
        private final List<FluidIngredient> fluidIngredients = new ArrayList<>();
        private final List<FluidStack> fluidResults = new ArrayList<>();

        private Builder(Identifier id) {
            super("grinding");
            this.id = id;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder duration(int ticks) {
            this.time = ticks;
            return this;
        }

        public Builder require(ItemLike item) {
            return require(Ingredient.of(item));
        }

        public Builder require(Ingredient ingredient) {
            this.ingredient = ingredient;
            return this;
        }

        public Builder require(FluidIngredient ingredient) {
            fluidIngredients.add(ingredient);
            return this;
        }

        public Builder output(ItemLike item) {
            return output(item, 1);
        }

        public Builder output(ItemLike item, int count) {
            results.add(new ProcessingOutput(item.asItem(), count));
            return this;
        }

        public Builder output(float chance, ItemStack stack) {
            results.add(new ProcessingOutput(stack.getItem(), stack.getCount(), chance));
            return this;
        }

        public Builder output(Fluid fluid, int amount) {
            fluidResults.add(new FluidStack(fluid, amount));
            return this;
        }

        public Builder whenModLoaded(String modId) {
            return withMod(modId);
        }

        @Override
        public RecipeHolder<GrindingRecipe> build() {
            if (id == null)
                throw new IllegalStateException("Grinding recipe id is required");
            if (ingredient == null)
                throw new IllegalStateException("Grinding recipe ingredient is required");
            GrindingRecipe recipe = new GrindingRecipe(time, ingredient, results, fluidIngredients, fluidResults);
            return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe);
        }
    }

    public static final class Serializer implements RecipeSerializer<GrindingRecipe> {
        private static final StreamCodec<RegistryFriendlyByteBuf, List<ProcessingOutput>> OUTPUTS_STREAM_CODEC = ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list());
        private static final StreamCodec<RegistryFriendlyByteBuf, List<FluidIngredient>> FLUID_INGREDIENTS_STREAM_CODEC = FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs.list());
        private static final StreamCodec<RegistryFriendlyByteBuf, List<FluidStack>> FLUID_RESULTS_STREAM_CODEC = FluidStack.PACKET_CODEC.apply(ByteBufCodecs.list());

        public static final MapCodec<GrindingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.optionalFieldOf("processing_time", DEFAULT_TIME).forGetter(GrindingRecipe::time),
                Ingredient.CODEC.listOf(1, 1).fieldOf("ingredients").xmap(List::getFirst, List::of)
                        .forGetter(GrindingRecipe::ingredient),
                ProcessingOutput.CODEC.listOf(0, 4).optionalFieldOf("results", List.of())
                        .forGetter(GrindingRecipe::results),
                FluidIngredient.CODEC.listOf(0, 1).optionalFieldOf("fluid_ingredients", List.of())
                        .forGetter(GrindingRecipe::fluidIngredients),
                FluidStack.CODEC.listOf(0, 1).optionalFieldOf("fluid_results", List.of())
                        .forGetter(GrindingRecipe::fluidResults))
                .apply(instance, GrindingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public GrindingRecipe decode(RegistryFriendlyByteBuf buffer) {
                return new GrindingRecipe(
                        ByteBufCodecs.VAR_INT.decode(buffer),
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buffer),
                        OUTPUTS_STREAM_CODEC.decode(buffer),
                        FLUID_INGREDIENTS_STREAM_CODEC.decode(buffer),
                        FLUID_RESULTS_STREAM_CODEC.decode(buffer));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, GrindingRecipe recipe) {
                ByteBufCodecs.VAR_INT.encode(buffer, recipe.time());
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient());
                OUTPUTS_STREAM_CODEC.encode(buffer, recipe.results());
                FLUID_INGREDIENTS_STREAM_CODEC.encode(buffer, recipe.fluidIngredients());
                FLUID_RESULTS_STREAM_CODEC.encode(buffer, recipe.fluidResults());
            }
        };

        @Override
        public MapCodec<GrindingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
