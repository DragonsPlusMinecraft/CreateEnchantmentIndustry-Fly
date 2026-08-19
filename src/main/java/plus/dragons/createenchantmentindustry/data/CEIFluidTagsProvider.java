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
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public final class CEIFluidTagsProvider extends FabricTagsProvider.FluidTagsProvider {
    private static final TagKey<Fluid> BOTTOMLESS_DENY = TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("create", "bottomless/deny"));

    public CEIFluidTagsProvider(
            FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        builder(BOTTOMLESS_DENY)
                .add(
                        key(CEICommon.asResource("experience")),
                        key(CEICommon.asResource("flowing_experience")));
    }

    private static ResourceKey<Fluid> key(Identifier id) {
        return ResourceKey.create(Registries.FLUID, id);
    }
}
