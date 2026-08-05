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

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public final class CEIStats {
    public static final CEIRegistryEntry<Identifier> GRINDSTONE_EXPERIENCE = register(
            "mechanical_grindstone_experience");
    public static final CEIRegistryEntry<Identifier> SUPER_ENCHANT = register("super_enchant");
    public static final CEIRegistryEntry<Identifier> PRINT = register("print");
    public static final CEIRegistryEntry<Identifier> FORGE = register("forge");
    public static final CEIRegistryEntry<Identifier> ENCHANT = register("enchant");
    public static final CEIRegistryEntry<Identifier> CLASSIC_ENCHANT = register("classic_enchant");

    private CEIStats() {}

    public static void register() {
        // Class initialization performs registration.
    }

    private static CEIRegistryEntry<Identifier> register(String path) {
        Identifier id = CEICommon.asResource(path);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
        Stats.CUSTOM.get(id, StatFormatter.DEFAULT);
        return new CEIRegistryEntry<>(id, id);
    }
}
