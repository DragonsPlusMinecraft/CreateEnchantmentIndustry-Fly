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

import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternMountedFluidStorageType;

public final class CEIMountedStorageTypes {
    public static final CEIRegistryEntry<ExperienceLanternMountedFluidStorageType> EXPERIENCE_LANTERN = registerType(
            "experience_lantern", new ExperienceLanternMountedFluidStorageType());

    private static boolean blockAttached;

    private CEIMountedStorageTypes() {}

    public static synchronized void register() {
        if (blockAttached) {
            return;
        }
        blockAttached = true;
        MountedFluidStorageType.REGISTRY.register(CEIBlocks.EXPERIENCE_LANTERN.get(), EXPERIENCE_LANTERN.get());
    }

    private static <T extends MountedFluidStorageType<?>> CEIRegistryEntry<T> registerType(String path, T type) {
        Identifier id = CEICommon.asResource(path);
        ResourceKey<MountedFluidStorageType<?>> key = ResourceKey.create(
                CreateRegistryKeys.MOUNTED_FLUID_STORAGE_TYPE, id);
        Registry.register(CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE, key, type);
        return new CEIRegistryEntry<>(id, type);
    }
}
