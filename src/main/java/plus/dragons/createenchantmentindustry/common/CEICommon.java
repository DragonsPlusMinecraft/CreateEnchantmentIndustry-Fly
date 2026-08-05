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

package plus.dragons.createenchantmentindustry.common;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.CEIPrintingBehaviours;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviourRegistry;
import plus.dragons.createenchantmentindustry.common.processing.EnchantmentProcessingRules;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIArmInterationPoints;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEICreativeModeTabs;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIEnchantments;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIItemAttributes;
import plus.dragons.createenchantmentindustry.common.registry.CEIItems;
import plus.dragons.createenchantmentindustry.common.registry.CEIMountedStorageTypes;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

/** Common, dedicated-server-safe Fabric entrypoint. */
public final class CEICommon implements ModInitializer {
    public static final String ID = "create_enchantment_industry";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        // Blocks and fluids were registered earlier through CEICreatePlugin. Calling these is an idempotent assertion
        // and ensures stress defaults exist before the config builders freeze.
        CEIFluids.register();
        CEIBlocks.register();

        CEIItems.register();
        CEIBlockEntities.register();
        CEIRecipes.register();
        CEIEnchantments.register();
        CEIArmInterationPoints.register();
        CEIMountedStorageTypes.register();
        CEIStats.register();
        CEIItemAttributes.register();
        CEICreativeModeTabs.register();

        CEIConfig.register();
        CEIPrintingBehaviours.register();
        CEIDataMaps.register();
        CEIFluids.initialize();
        CEIBlockEntities.registerStorageProviders();
        CEIAdvancements.register();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> PrintingBehaviourRegistry.freeze());
        ServerLifecycleEvents.SERVER_STARTED.register(EnchantmentProcessingRules::warnLegacyDataMaps);
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public static String asLocalization(String key) {
        return ID + "." + key;
    }
}
