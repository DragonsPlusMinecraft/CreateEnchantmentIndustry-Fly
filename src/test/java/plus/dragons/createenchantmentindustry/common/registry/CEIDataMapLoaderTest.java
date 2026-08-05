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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import plus.dragons.createenchantmentindustry.common.datamap.CEIDataMapType;

class CEIDataMapLoaderTest {
    private static CEIDataMapType<net.minecraft.world.item.Item, Integer> type;
    private static final Identifier STONE = Identifier.withDefaultNamespace("stone");
    private static final Identifier DIRT = Identifier.withDefaultNamespace("dirt");

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        new ResourceConditionsImpl().onInitialize();
        type = new CEIDataMapType<>(
                Identifier.fromNamespaceAndPath("create_enchantment_industry", "test"),
                BuiltInRegistries.ITEM,
                Codec.INT);
    }

    @Test
    void higherPriorityResourcesOverrideAndReplaceLowerPacks() {
        Map<Identifier, Object> values = new LinkedHashMap<>();
        apply(values, """
                {
                  "values": {
                    "minecraft:stone": 1,
                    "minecraft:dirt": 2
                  }
                }
                """);
        apply(values, """
                {
                  "values": {
                    "minecraft:stone": 3
                  }
                }
                """);

        assertEquals(Map.of(STONE, 3, DIRT, 2), values);

        apply(values, """
                {
                  "replace": true,
                  "values": {
                    "minecraft:dirt": 4
                  }
                }
                """);
        assertEquals(Map.of(DIRT, 4), values);
    }

    @Test
    void removeEntriesAreAppliedAfterValues() {
        Map<Identifier, Object> values = new LinkedHashMap<>();
        apply(values, """
                {
                  "values": {
                    "minecraft:stone": 1,
                    "minecraft:dirt": 2
                  },
                  "remove": ["minecraft:stone"]
                }
                """);

        assertEquals(Map.of(DIRT, 2), values);
    }

    @Test
    void falseFabricConditionSkipsValueBeforeRegistryResolution() {
        Map<Identifier, Object> values = new LinkedHashMap<>();
        apply(values, """
                {
                  "values": {
                    "missing_mod:missing_item": {
                      "fabric:load_conditions": {
                        "condition": "fabric:all_mods_loaded",
                        "values": ["cei_test_mod_that_is_not_installed"]
                      },
                      "replace": false,
                      "value": 9
                    }
                  }
                }
                """);

        assertFalse(values.containsKey(Identifier.fromNamespaceAndPath("missing_mod", "missing_item")));
    }

    @Test
    void malformedValuesAndMissingRegistryIdsFailAtomically() {
        Map<Identifier, Object> values = new LinkedHashMap<>();
        assertThrows(IllegalArgumentException.class, () -> apply(values, "{\"values\": []}"));
        assertThrows(
                IllegalArgumentException.class,
                () -> apply(values, "{\"values\": {\"minecraft:stone\": \"not-an-integer\"}}"));
        assertThrows(
                IllegalArgumentException.class,
                () -> apply(values, "{\"values\": {\"missing_mod:missing_item\": 1}}"));
        assertEquals(Map.of(), values);
    }

    private static void apply(Map<Identifier, Object> values, String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        CEIDataMaps.applyJsonResource(type, values, root, "unit-test pack");
    }
}
