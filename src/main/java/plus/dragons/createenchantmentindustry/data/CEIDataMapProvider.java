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
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import plus.dragons.createenchantmentindustry.common.datamap.CEIDataMapType;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceFuel;
import plus.dragons.createenchantmentindustry.common.processing.EnchantmentProcessingRule;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;
import plus.dragons.createenchantmentindustry.util.CEIDyeFluids;

/** Writes the default resources consumed by the Forge 1.20.1 data-map backport. */
public class CEIDataMapProvider implements DataProvider {
    private final PackOutput output;

    public CEIDataMapProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> writes = new ArrayList<>();

        var experienceFuel = builder(CEIDataMaps.EXPERIENCE_FUEL);
        experienceFuel.add(
                id(BuiltInRegistries.ITEM, CEIItems.EXPERIENCE_BUCKET.get()),
                ExperienceFuel.normal(1000, Items.BUCKET.getDefaultInstance()));
        experienceFuel.add(CEIItems.EXPERIENCE_CAKE.getId(), ExperienceFuel.special(1000));
        experienceFuel.add(CEIItems.EXPERIENCE_CAKE_SLICE.getId(), ExperienceFuel.special(250));
        experienceFuel.add(CEIBlocks.SUPER_EXPERIENCE_BLOCK.getId(), ExperienceFuel.special(27));
        experienceFuel.add(CEIItems.SUPER_EXPERIENCE_NUGGET.getId(), ExperienceFuel.special(3));
        experienceFuel.add(id(BuiltInRegistries.BLOCK, AllBlocks.EXPERIENCE_BLOCK), ExperienceFuel.normal(27));
        experienceFuel.add(id(BuiltInRegistries.ITEM, AllItems.EXP_NUGGET), ExperienceFuel.normal(3));
        experienceFuel.add(mod("create_sa", "heap_of_experience"), ExperienceFuel.normal(12), "create_sa");
        experienceFuel.add(mod("ars_nouveau", "experience_gem"), ExperienceFuel.normal(3), "ars_nouveau");
        experienceFuel.add(
                mod("ars_nouveau", "greater_experience_gem"), ExperienceFuel.normal(12), "ars_nouveau");
        experienceFuel.add(
                mod("mysticalagriculture", "experience_droplet"),
                ExperienceFuel.normal(10),
                "mysticalagriculture");
        writes.add(save(cachedOutput, experienceFuel));

        var fluidExperience = builder(CEIDataMaps.FLUID_UNIT_EXPERIENCE);
        fluidExperience.add(mod("cofh_core", "experience"), 25, "cofh_core");
        fluidExperience.add(mod("cyclic", "xpjuice"), 20, "cyclic");
        fluidExperience.add(mod("enderio", "xpjuice"), 20, "enderio");
        fluidExperience.add(mod("industrialforegoing", "essence"), 20, "industrialforegoing");
        fluidExperience.add(mod("mob_grinding_utils", "fluid_xp"), 20, "mob_grinding_utils");
        fluidExperience.add(mod("pneumaticcraft", "memory_essence"), 20, "pneumaticcraft");
        fluidExperience.add(mod("reliquary", "xp_juice_still"), 20, "reliquary");
        fluidExperience.add(mod("sophisticatedcore", "xp_still"), 20, "sophisticatedcore");
        fluidExperience.add(mod("justdirethings", "xp_fluid_source"), 20, "justdirethings");
        writes.add(save(cachedOutput, fluidExperience));

        TagKey<net.minecraft.world.level.material.Fluid> blackDye = CEIDyeFluids.tag(DyeColor.BLACK);
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_ADDRESS_INGREDIENT).add(blackDye, 10)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_PATTERN_INGREDIENT).add(blackDye, 100)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_COPY_INGREDIENT).add(blackDye, 10)));
        TagKey<net.minecraft.world.level.material.Fluid> allDyes = TagKey.create(net.minecraft.core.registries.Registries.FLUID, mod("c", "dyes"));
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_CUSTOM_NAME_INGREDIENT)
                .add(id(BuiltInRegistries.FLUID, CEIFluids.EXPERIENCE.getSource()), 10)
                .add(allDyes, 250)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_WRITTEN_BOOK_INGREDIENT).add(blackDye, 10)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_BANNER_PATTERN_INGREDIENT)
                .add(allDyes, 100)));

        var customNameStyles = builder(CEIDataMaps.PRINTING_CUSTOM_NAME_STYLE);
        for (DyeColor color : DyeColor.values()) {
            customNameStyles.add(CEIDyeFluids.tag(color), Style.EMPTY.withColor(color.getTextColor()));
        }
        writes.add(save(cachedOutput, customNameStyles));

        writes.add(save(cachedOutput, builder(CEIDataMaps.PRINTING_ENCHANTED_BOOK_COST)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.FORGING_COST_MULTIPLIER)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.SPLITTING_COST_MULTIPLIER)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.SUPER_ENCHANTING_LEVEL_EXTENSION)));
        writes.add(save(cachedOutput, builder(CEIDataMaps.ENCHANTMENT_PROCESSING_RULES)
                .add(Enchantments.MENDING.identifier(),
                        EnchantmentProcessingRule.enchanterAndForgerExtension(0, 0))
                .add(Enchantments.INFINITY.identifier(),
                        EnchantmentProcessingRule.enchanterAndForgerExtension(0, 0))));

        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> save(CachedOutput cachedOutput, MapBuilder<?, ?> builder) {
        Identifier resource = builder.type.resource();
        Path path = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(resource.getNamespace())
                .resolve(resource.getPath());
        return DataProvider.saveStable(cachedOutput, builder.root(), path);
    }

    private static <K, V> MapBuilder<K, V> builder(CEIDataMapType<K, V> type) {
        return new MapBuilder<>(type);
    }

    private static Identifier mod(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    private static <T> Identifier id(Registry<T> registry, T value) {
        Identifier id = registry.getKey(value);
        if (id == null)
            throw new IllegalStateException("Unregistered data-map value " + value);
        return id;
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Data Maps";
    }

    private static final class MapBuilder<K, V> {
        private final CEIDataMapType<K, V> type;
        private final JsonObject values = new JsonObject();

        private MapBuilder(CEIDataMapType<K, V> type) {
            this.type = type;
        }

        private MapBuilder<K, V> add(Identifier key, V value) {
            values.add(key.toString(), encode(value));
            return this;
        }

        private MapBuilder<K, V> add(TagKey<K> key, V value) {
            values.add("#" + key.location(), encode(value));
            return this;
        }

        private MapBuilder<K, V> add(Identifier key, V value, String requiredMod) {
            JsonObject wrapped = new JsonObject();
            JsonElement condition = ResourceCondition.CONDITION_CODEC
                    .encodeStart(JsonOps.INSTANCE, ResourceConditions.allModsLoaded(requiredMod))
                    .getOrThrow(message -> new IllegalStateException(
                            "Failed to encode Fabric resource condition: " + message));
            wrapped.add(ResourceConditions.CONDITIONS_KEY, condition);
            wrapped.addProperty("replace", false);
            wrapped.add("value", encode(value));
            values.add(key.toString(), wrapped);
            return this;
        }

        private JsonElement encode(V value) {
            return type.codec().encodeStart(JsonOps.INSTANCE, value).getOrThrow(message -> new IllegalStateException("Failed to encode data map " + type.id() + ": " + message));
        }

        private JsonObject root() {
            JsonObject root = new JsonObject();
            root.add("values", values);
            return root;
        }
    }
}
