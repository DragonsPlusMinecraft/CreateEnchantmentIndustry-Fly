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

package plus.dragons.createenchantmentindustry.config;

import com.zurrtum.create.catnip.config.Builder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** Owns the process-wide config instances used by Create Fly's JSON config builder. */
public final class CEIConfig {
    private static final CEICommonConfig COMMON_CONFIG = new CEICommonConfig();
    private static final CEIServerConfig SERVER_CONFIG = new CEIServerConfig();
    private static CEIClientConfig clientConfig;
    private static CEIServerConfigSnapshot localServerSnapshot;
    private static boolean registered;

    private CEIConfig() {}

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        Builder.create(() -> COMMON_CONFIG, CEICommon.ID, "common");
        reloadServer();
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            if (success) {
                reloadServer();
            }
        });
    }

    /** Called exclusively by the Fabric client entrypoint. */
    public static synchronized CEIClientConfig initializeClient() {
        if (clientConfig == null) {
            clientConfig = new CEIClientConfig();
            Builder.create(() -> clientConfig, CEICommon.ID, "client");
        }
        return clientConfig;
    }

    public static synchronized void reloadServer() {
        // Re-registering the same object is required by p2 StressConfig's provider identity guard.
        SERVER_CONFIG.prepareReload();
        Builder.create(() -> SERVER_CONFIG, CEICommon.ID, "server");
    }

    public static synchronized CEIServerConfigSnapshot captureServerSnapshot() {
        return CEIServerConfigSnapshot.capture(SERVER_CONFIG);
    }

    /** Applies remote values in memory without replacing or writing the client's local server config. */
    public static synchronized void applyServerSnapshot(CEIServerConfigSnapshot snapshot) {
        if (localServerSnapshot == null) {
            localServerSnapshot = captureServerSnapshot();
        }
        snapshot.applyTo(SERVER_CONFIG);
    }

    public static synchronized void clearServerSnapshot() {
        if (localServerSnapshot != null) {
            localServerSnapshot.applyTo(SERVER_CONFIG);
            localServerSnapshot = null;
        }
    }

    public static CEICommonConfig common() {
        return COMMON_CONFIG;
    }

    public static CEIClientConfig client() {
        if (clientConfig == null) {
            throw new IllegalStateException("Client config is unavailable before CEIClient initialization");
        }
        return clientConfig;
    }

    public static CEIServerConfig server() {
        return SERVER_CONFIG;
    }

    public static CEIKineticsConfig kinetics() {
        return SERVER_CONFIG.kinetics;
    }

    public static CEIStressConfig stress() {
        return SERVER_CONFIG.kinetics.stressValues;
    }

    public static CEIFluidsConfig fluids() {
        return SERVER_CONFIG.fluids;
    }

    public static CEIEnchantmentsConfig enchantments() {
        return SERVER_CONFIG.enchantments;
    }

    public static CEIProcessingConfig processing() {
        return SERVER_CONFIG.processing;
    }

    public static CEIFeaturesConfig features() {
        return COMMON_CONFIG.features;
    }
}
