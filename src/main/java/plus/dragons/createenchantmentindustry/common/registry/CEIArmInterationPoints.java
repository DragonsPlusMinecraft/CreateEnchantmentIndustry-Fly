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

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterArmInteractionPoint;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterArmInteractionPoint;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerArmInteractionPoint;

public final class CEIArmInterationPoints {
    public static final CEIRegistryEntry<BlazeEnchanterArmInteractionPoint.Type> BLAZE_ENCHANTER = register(
            "blaze_enchanter", new BlazeEnchanterArmInteractionPoint.Type());
    public static final CEIRegistryEntry<BlazeForgerArmInteractionPoint.Type> BLAZE_FORGER = register(
            "blaze_forger", new BlazeForgerArmInteractionPoint.Type());
    public static final CEIRegistryEntry<ClassicBlazeEnchanterArmInteractionPoint.Type> CLASSIC_BLAZE_ENCHANTER = register("classic_blaze_enchanter", new ClassicBlazeEnchanterArmInteractionPoint.Type());

    private CEIArmInterationPoints() {}

    public static void register() {
        // Class initialization performs registration before Create sorts interaction point types.
    }

    private static <T extends ArmInteractionPointType> CEIRegistryEntry<T> register(String path, T type) {
        Identifier id = CEICommon.asResource(path);
        Registry.register(CreateRegistries.ARM_INTERACTION_POINT_TYPE, id, type);
        return new CEIRegistryEntry<>(id, type);
    }
}
