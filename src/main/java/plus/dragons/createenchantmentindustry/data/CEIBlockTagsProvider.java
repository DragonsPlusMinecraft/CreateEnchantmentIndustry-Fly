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
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public final class CEIBlockTagsProvider extends FabricTagProvider.BlockTagProvider {
    private static final TagKey<Block> STORAGE_BLOCKS = tag("c", "storage_blocks");
    private static final TagKey<Block> FAN_TRANSPARENT = tag("create", "fan_transparent");
    private static final TagKey<Block> SMOKING_CATALYSTS = tag("create", "fan_processing_catalysts/smoking");

    public CEIBlockTagsProvider(
            FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        builder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(
                        key(CEIBlocks.MECHANICAL_GRINDSTONE.getId()),
                        key(CEIBlocks.GRINDSTONE_DRAIN.getId()),
                        key(CEIBlocks.EXPERIENCE_HATCH.getId()),
                        key(CEIBlocks.PRINTER.getId()),
                        key(CEIBlocks.BLAZE_ENCHANTER.getId()),
                        key(CEIBlocks.BLAZE_FORGER.getId()),
                        key(CEIBlocks.CLASSIC_BLAZE_ENCHANTER.getId()),
                        key(CEIBlocks.SUPER_EXPERIENCE_BLOCK.getId()),
                        key(CEIBlocks.EXPERIENCE_LANTERN.getId()));
        builder(BlockTags.BEACON_BASE_BLOCKS).add(key(CEIBlocks.SUPER_EXPERIENCE_BLOCK.getId()));
        builder(STORAGE_BLOCKS).add(key(CEIBlocks.SUPER_EXPERIENCE_BLOCK.getId()));
        builder(FAN_TRANSPARENT)
                .add(
                        key(CEIBlocks.BLAZE_ENCHANTER.getId()),
                        key(CEIBlocks.BLAZE_FORGER.getId()),
                        key(CEIBlocks.CLASSIC_BLAZE_ENCHANTER.getId()));
        builder(SMOKING_CATALYSTS)
                .add(
                        key(CEIBlocks.BLAZE_ENCHANTER.getId()),
                        key(CEIBlocks.BLAZE_FORGER.getId()),
                        key(CEIBlocks.CLASSIC_BLAZE_ENCHANTER.getId()));
    }

    private static TagKey<Block> tag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(namespace, path));
    }

    private static ResourceKey<Block> key(Identifier id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }
}
