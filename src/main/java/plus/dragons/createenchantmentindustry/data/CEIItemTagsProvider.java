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

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;

public final class CEIItemTagsProvider extends FabricTagProvider.ItemTagProvider {
    private static final TagKey<Item> STORAGE_BLOCKS = tag("c", "storage_blocks");
    private static final TagKey<Item> NUGGETS = tag("c", "nuggets");
    private static final TagKey<Item> BUCKETS = tag("c", "buckets");
    private static final TagKey<Item> UPRIGHT_ON_BELT = tag("create", "upright_on_belt");

    public CEIItemTagsProvider(
            FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        builder(STORAGE_BLOCKS).add(key(CEIBlocks.SUPER_EXPERIENCE_BLOCK.getId()));
        builder(NUGGETS).add(key(CEIItems.SUPER_EXPERIENCE_NUGGET.getId()));
        builder(BUCKETS).add(key(CEIItems.EXPERIENCE_BUCKET.getId()));
        builder(UPRIGHT_ON_BELT)
                .add(
                        key(CEIItems.EXPERIENCE_CAKE_BASE.getId()),
                        key(CEIItems.EXPERIENCE_CAKE.getId()));
    }

    private static TagKey<Item> tag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path));
    }

    private static ResourceKey<Item> key(Identifier id) {
        return ResourceKey.create(Registries.ITEM, id);
    }
}
