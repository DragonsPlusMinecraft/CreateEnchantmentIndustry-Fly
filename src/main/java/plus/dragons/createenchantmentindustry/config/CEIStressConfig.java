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

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import plus.dragons.createdragonsplus.config.StressConfig;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public class CEIStressConfig extends StressConfig {
    public CEIStressConfig() {
        super(CEICommon.ID);
    }

    @Override
    protected int getVersion() {
        return 1;
    }

    synchronized void collectSnapshot(String path, Map<String, JsonElement> output) {
        impacts.forEach((id, value) -> output.put(key(path, "impact", id), new JsonPrimitive(value.get())));
        capacities.forEach((id, value) -> output.put(key(path, "capacity", id), new JsonPrimitive(value.get())));
    }

    synchronized Runnable decodeSnapshot(String path, Map<String, JsonElement> snapshot) {
        Map<Identifier, Double> decodedImpacts = decode(path, "impact", impacts.keySet(), snapshot);
        Map<Identifier, Double> decodedCapacities = decode(path, "capacity", capacities.keySet(), snapshot);
        return () -> {
            synchronized (CEIStressConfig.this) {
                decodedImpacts.forEach((id, value) -> impacts.get(id).set(value));
                decodedCapacities.forEach((id, value) -> capacities.get(id).set(value));
            }
        };
    }

    private static Map<Identifier, Double> decode(
            String path,
            String kind,
            Iterable<Identifier> ids,
            Map<String, JsonElement> snapshot) {
        Map<Identifier, Double> result = new LinkedHashMap<>();
        for (Identifier id : ids) {
            String key = key(path, kind, id);
            JsonElement element = snapshot.get(key);
            if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
                throw new IllegalArgumentException("Server stress value " + key + " must be a number");
            }
            double value = element.getAsDouble();
            if (!Double.isFinite(value) || value < 0) {
                throw new IllegalArgumentException("Server stress value " + key + " must be finite and non-negative");
            }
            result.put(id, value);
        }
        return result;
    }

    private static String key(String path, String kind, Identifier id) {
        return path + "." + kind + "." + id;
    }
}
