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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.CreateRollableRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.common.recipe.BaseRecipeBuilder;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.util.CEILang;

/** Two-item and one-fluid printer recipe with fully codec-backed sound settings. */
public record PrintingRecipe(
        List<Ingredient> ingredients,
        List<FluidIngredient> fluidIngredients,
        List<ProcessingOutput> results,
        SoundEvent sound,
        float volume,
        float minimumPitch,
        float maximumPitch)
        implements CreateRollableRecipe<PrintingInput> {
    public PrintingRecipe {
        ingredients = List.copyOf(ingredients);
        fluidIngredients = List.copyOf(fluidIngredients);
        results = List.copyOf(results);
        if (ingredients.size() != 2)
            throw new IllegalArgumentException("Printing recipes require exactly two item ingredients");
        if (fluidIngredients.size() != 1)
            throw new IllegalArgumentException("Printing recipes require exactly one fluid ingredient");
        if (results.size() != 1)
            throw new IllegalArgumentException("Printing recipes require exactly one item result");
        if (volume < 0 || minimumPitch < 0 || maximumPitch < minimumPitch)
            throw new IllegalArgumentException("Invalid printing sound range");
    }

    @Override
    public boolean matches(PrintingInput input, Level level) {
        return ingredients.getFirst().test(input.base())
                && ingredients.get(1).test(input.template())
                && (input.fluid().isEmpty() || fluidIngredients.getFirst().test(input.fluid()));
    }

    @Override
    public List<ItemStack> assemble(PrintingInput input, RandomSource random) {
        List<ItemStack> output = new ArrayList<>(1);
        ProcessingOutput.rollOutput(random, results, output::add);
        return output;
    }

    @Override
    public RecipeSerializer<PrintingRecipe> getSerializer() {
        return CEIRecipes.PRINTING.getSerializer();
    }

    @Override
    public RecipeType<PrintingRecipe> getType() {
        return CEIRecipes.PRINTING.getType();
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<FluidIngredient> getFluidIngredients() {
        return fluidIngredients;
    }

    public List<ProcessingOutput> getRollableResults() {
        return results;
    }

    public void playSound(Level level, BlockPos pos, SoundSource source) {
        float pitch = minimumPitch + level.getRandom().nextFloat() * (maximumPitch - minimumPitch);
        level.playSound(null, pos, sound, source, volume, pitch);
    }

    public Component getDescriptionForAssembly() {
        ItemStack[] matchingStacks = ingredients.get(1).items()
                .map(ItemStack::new)
                .toArray(ItemStack[]::new);
        List<FluidStack> matchingFluids = fluidIngredients.getFirst().getMatchingFluidStacks();
        if (matchingStacks.length == 0 || matchingFluids.isEmpty())
            return Component.literal("Invalid");
        return CEILang.translate(
                "recipe.assembly.printing",
                matchingStacks[0].getHoverName(),
                matchingFluids.getFirst().getName()).component();
    }

    public void addRequiredMachines(Set<ItemLike> required) {
        required.add(CEIBlocks.PRINTER.get());
    }

    public void addAssemblyIngredients(List<Ingredient> list) {
        list.add(ingredients.get(1));
    }

    public void addAssemblyFluidIngredients(List<FluidIngredient> list) {
        list.add(fluidIngredients.getFirst());
    }

    public static Builder builder(Identifier id) {
        return new Builder(id, SoundEvents.ENCHANTMENT_TABLE_USE);
    }

    public static Builder builder(Identifier id, SoundEvent sound) {
        return new Builder(id, sound);
    }

    public static final class Builder extends BaseRecipeBuilder<PrintingRecipe, Builder> {
        private final List<Ingredient> ingredients = new ArrayList<>();
        private final List<FluidIngredient> fluidIngredients = new ArrayList<>();
        private final List<ProcessingOutput> results = new ArrayList<>();
        private SoundEvent sound;
        private float volume = 1;
        private float minimumPitch = .9F;
        private float maximumPitch = 1.1F;

        private Builder(Identifier id, SoundEvent sound) {
            super("printing");
            this.id = id;
            this.sound = sound;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder require(Ingredient ingredient) {
            ingredients.add(ingredient);
            return this;
        }

        public Builder require(FluidIngredient ingredient) {
            fluidIngredients.add(ingredient);
            return this;
        }

        public Builder output(ItemStack stack) {
            results.add(new ProcessingOutput(stack));
            return this;
        }

        public Builder sound(SoundEvent sound, float volume, float minimumPitch, float maximumPitch) {
            this.sound = sound;
            this.volume = volume;
            this.minimumPitch = minimumPitch;
            this.maximumPitch = maximumPitch;
            return this;
        }

        @Override
        public RecipeHolder<PrintingRecipe> build() {
            if (id == null)
                throw new IllegalStateException("Printing recipe id is required");
            PrintingRecipe recipe = new PrintingRecipe(
                    ingredients, fluidIngredients, results, sound, volume, minimumPitch, maximumPitch);
            return new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe);
        }
    }

    public static final class Serializer {
        private static final StreamCodec<RegistryFriendlyByteBuf, List<Ingredient>> INGREDIENTS_STREAM_CODEC = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list());
        private static final StreamCodec<RegistryFriendlyByteBuf, List<FluidIngredient>> FLUIDS_STREAM_CODEC = FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs.list());
        private static final StreamCodec<RegistryFriendlyByteBuf, List<ProcessingOutput>> RESULTS_STREAM_CODEC = ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list());
        private static final StreamCodec<RegistryFriendlyByteBuf, SoundEvent> SOUND_STREAM_CODEC = ByteBufCodecs.registry(Registries.SOUND_EVENT);

        public static final MapCodec<PrintingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf(2, 2).fieldOf("ingredients").forGetter(PrintingRecipe::ingredients),
                FluidIngredient.CODEC.listOf(1, 1).fieldOf("fluid_ingredients").forGetter(PrintingRecipe::fluidIngredients),
                ProcessingOutput.CODEC.listOf(1, 1).fieldOf("results").forGetter(PrintingRecipe::results),
                BuiltInRegistries.SOUND_EVENT.byNameCodec().optionalFieldOf("sound", SoundEvents.ENCHANTMENT_TABLE_USE)
                        .forGetter(PrintingRecipe::sound),
                Codec.FLOAT.optionalFieldOf("volume", 1F).forGetter(PrintingRecipe::volume),
                Codec.FLOAT.optionalFieldOf("minimum_pitch", .9F).forGetter(PrintingRecipe::minimumPitch),
                Codec.FLOAT.optionalFieldOf("maximum_pitch", 1.1F).forGetter(PrintingRecipe::maximumPitch))
                .apply(instance, PrintingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PrintingRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public PrintingRecipe decode(RegistryFriendlyByteBuf buffer) {
                return new PrintingRecipe(
                        INGREDIENTS_STREAM_CODEC.decode(buffer),
                        FLUIDS_STREAM_CODEC.decode(buffer),
                        RESULTS_STREAM_CODEC.decode(buffer),
                        SOUND_STREAM_CODEC.decode(buffer),
                        ByteBufCodecs.FLOAT.decode(buffer),
                        ByteBufCodecs.FLOAT.decode(buffer),
                        ByteBufCodecs.FLOAT.decode(buffer));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, PrintingRecipe recipe) {
                INGREDIENTS_STREAM_CODEC.encode(buffer, recipe.ingredients());
                FLUIDS_STREAM_CODEC.encode(buffer, recipe.fluidIngredients());
                RESULTS_STREAM_CODEC.encode(buffer, recipe.results());
                SOUND_STREAM_CODEC.encode(buffer, recipe.sound());
                ByteBufCodecs.FLOAT.encode(buffer, recipe.volume());
                ByteBufCodecs.FLOAT.encode(buffer, recipe.minimumPitch());
                ByteBufCodecs.FLOAT.encode(buffer, recipe.maximumPitch());
            }
        };
        public static final RecipeSerializer<PrintingRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        private Serializer() {}
    }
}
