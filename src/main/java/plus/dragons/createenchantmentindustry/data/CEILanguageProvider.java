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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;

/** Generates the English language file for the supported core feature set only. */
public final class CEILanguageProvider extends FabricLanguageProvider {
    private static final String INTERFACE_LANG = "/assets/create_enchantment_industry/lang/builtin/interface.json";
    private static final String PONDER_LANG = "/assets/create_enchantment_industry/lang/builtin/ponder.json";

    public CEILanguageProvider(
            FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, "en_us", registries);
    }

    @Override
    public void generateTranslations(
            HolderLookup.Provider registries, TranslationBuilder builder) {
        addBuiltInTranslations(builder, INTERFACE_LANG);
        addBuiltInTranslations(builder, PONDER_LANG);

        builder.add(CEIBlocks.EXPERIENCE.get(), "Liquid Experience");
        builder.add(CEIBlocks.MECHANICAL_GRINDSTONE.get(), "Mechanical Grindstone");
        builder.add(CEIBlocks.GRINDSTONE_DRAIN.get(), "Grindstone Drain");
        builder.add(CEIBlocks.EXPERIENCE_HATCH.get(), "Experience Hatch");
        builder.add(CEIBlocks.PRINTER.get(), "Printer");
        builder.add(CEIBlocks.BLAZE_ENCHANTER.get(), "Blaze Enchanter");
        builder.add(CEIBlocks.BLAZE_FORGER.get(), "Blaze Forger");
        builder.add(CEIBlocks.CLASSIC_BLAZE_ENCHANTER.get(), "Classic Blaze Enchanter");
        builder.add(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get(), "Block of Super Experience");
        builder.add(CEIBlocks.EXPERIENCE_LANTERN.get(), "Experience Lantern");

        builder.add(CEIItems.SUPER_EXPERIENCE_NUGGET.get(), "Nugget of Super Experience");
        builder.add(CEIItems.ENCHANTING_TEMPLATE.get(), "Enchanting Template");
        builder.add(CEIItems.SUPER_ENCHANTING_TEMPLATE.get(), "Super Enchanting Template");
        builder.add(CEIItems.BLAZES_ENCHANTING_HANDBOOK.get(), "Blaze's Enchanting Handbook");
        builder.add(CEIItems.EXPERIENCE_CAKE_BASE.get(), "Cake Base o' Enchanting");
        builder.add(CEIItems.EXPERIENCE_CAKE.get(), "Cake o' Enchanting");
        builder.add(CEIItems.EXPERIENCE_CAKE_SLICE.get(), "Cake Slice o' Enchanting");
        builder.add(CEIItems.EXPERIENCE_BUCKET.get(), "Bucket o' Enchanting");

        builder.add(
                "create.item_attributes.create_enchantment_industry.processable_by_mechanical_grindstone",
                "can be processed by Mechanical Grindstone");
        builder.add(
                "create.item_attributes.create_enchantment_industry.processable_by_mechanical_grindstone.inverted",
                "cannot be processed by Mechanical Grindstone");

        builder.add(
                "stat.create_enchantment_industry.mechanical_grindstone_experience",
                "Experience Produced (by Mechanical Grindstone)");
        builder.add("stat.create_enchantment_industry.super_enchant", "Super Enchant");
        builder.add("stat.create_enchantment_industry.print", "Printer Used");
        builder.add("stat.create_enchantment_industry.forge", "Blaze Forger Used");
        builder.add("stat.create_enchantment_industry.enchant", "Blaze Enchanter Used");
        builder.add("stat.create_enchantment_industry.classic_enchant", "Classic Blaze Enchanter Used");

        CEIAdvancements.provideLang(builder::add);
    }

    private static void addBuiltInTranslations(TranslationBuilder builder, String resource) {
        try (InputStream stream = CEILanguageProvider.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("Missing built-in language template " + resource);
            }
            JsonObject translations = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            for (var entry : translations.entrySet()) {
                JsonElement value = entry.getValue();
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                    throw new IllegalStateException("Translation value must be a string: " + entry.getKey());
                }
                builder.add(entry.getKey(), value.getAsString());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read built-in language template " + resource, exception);
        }
    }
}
