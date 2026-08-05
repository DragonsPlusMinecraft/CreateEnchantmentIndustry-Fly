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

package plus.dragons.createenchantmentindustry.common.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingInput;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingRecipe;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;

class CEIRecipeCodecTest {
    private static RegistryAccess registryAccess;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void grindingJsonAndNetworkCodecsRoundTrip() {
        GrindingRecipe expected = new GrindingRecipe(
                40,
                Ingredient.of(Items.IRON_INGOT),
                List.of(new ProcessingOutput(Items.GOLD_NUGGET, 2)),
                List.of(),
                List.of(new FluidStack(Fluids.WATER, 81)));

        var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        JsonElement json = GrindingRecipe.Serializer.CODEC.codec()
                .encodeStart(ops, expected)
                .getOrThrow();
        GrindingRecipe fromJson = GrindingRecipe.Serializer.CODEC.codec()
                .parse(ops, json)
                .getOrThrow();
        assertGrindingRecipe(fromJson);

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        try {
            GrindingRecipe.Serializer.STREAM_CODEC.encode(buffer, expected);
            assertGrindingRecipe(GrindingRecipe.Serializer.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void printingJsonNetworkAndInputMatchingRoundTrip() {
        PrintingRecipe expected = new PrintingRecipe(
                List.of(Ingredient.of(Items.PAPER), Ingredient.of(Items.NAME_TAG)),
                List.of(new FluidStackIngredient(Fluids.WATER, DataComponentPatch.EMPTY, 810)),
                List.of(new ProcessingOutput(Items.MAP, 1)),
                SoundEvents.ENCHANTMENT_TABLE_USE,
                0.8F,
                0.7F,
                1.2F);

        var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        JsonElement json = PrintingRecipe.Serializer.CODEC.codec()
                .encodeStart(ops, expected)
                .getOrThrow();
        PrintingRecipe fromJson = PrintingRecipe.Serializer.CODEC.codec()
                .parse(ops, json)
                .getOrThrow();
        assertPrintingRecipe(fromJson);

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        try {
            PrintingRecipe.Serializer.STREAM_CODEC.encode(buffer, expected);
            assertPrintingRecipe(PrintingRecipe.Serializer.STREAM_CODEC.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    private static void assertGrindingRecipe(GrindingRecipe recipe) {
        assertEquals(40, recipe.time());
        assertTrue(recipe.ingredient().test(new ItemStack(Items.IRON_INGOT)));
        assertEquals(2, recipe.results().getFirst().count());
        assertEquals(Fluids.WATER, recipe.fluidResults().getFirst().getFluid());
        assertEquals(81, recipe.fluidResults().getFirst().getAmount());
        assertTrue(recipe.matches(new SingleRecipeInput(new ItemStack(Items.IRON_INGOT)), null));
        assertFalse(recipe.matches(new SingleRecipeInput(new ItemStack(Items.DIAMOND)), null));
    }

    private static void assertPrintingRecipe(PrintingRecipe recipe) {
        PrintingInput valid = new PrintingInput(
                new ItemStack(Items.PAPER), new ItemStack(Items.NAME_TAG), new FluidStack(Fluids.WATER, 810));
        PrintingInput invalid = new PrintingInput(
                new ItemStack(Items.DIAMOND), new ItemStack(Items.NAME_TAG), new FluidStack(Fluids.WATER, 810));
        assertTrue(recipe.matches(valid, null));
        assertFalse(recipe.matches(invalid, null));
        assertEquals(Items.MAP, recipe.assemble(valid, RandomSource.create(1)).getFirst().getItem());
        assertEquals(SoundEvents.ENCHANTMENT_TABLE_USE, recipe.sound());
        assertEquals(0.8F, recipe.volume());
        assertEquals(0.7F, recipe.minimumPitch());
        assertEquals(1.2F, recipe.maximumPitch());
    }
}
