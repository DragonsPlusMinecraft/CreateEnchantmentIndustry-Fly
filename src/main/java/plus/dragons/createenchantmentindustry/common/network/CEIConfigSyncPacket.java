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

package plus.dragons.createenchantmentindustry.common.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.config.CEIServerConfigSnapshot;

public record CEIConfigSyncPacket(CEIServerConfigSnapshot snapshot) {
    private static final int MAX_VALUE_COUNT = 128;
    private static final int MAX_PATH_LENGTH = 256;
    private static final int MAX_VALUE_LENGTH = 256;

    public static CEIConfigSyncPacket create() {
        return new CEIConfigSyncPacket(CEIConfig.captureServerSnapshot());
    }

    public static void encode(CEIConfigSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.snapshot.values().size());
        packet.snapshot.values().forEach((path, value) -> {
            buffer.writeUtf(path, MAX_PATH_LENGTH);
            buffer.writeUtf(value.toString(), MAX_VALUE_LENGTH);
        });
    }

    public static CEIConfigSyncPacket decode(FriendlyByteBuf buffer) {
        int count = buffer.readVarInt();
        if (count < 0 || count > MAX_VALUE_COUNT) {
            throw new IllegalArgumentException("CEI server config snapshot value count exceeds limit: " + count);
        }
        Map<String, JsonElement> values = new LinkedHashMap<>();
        for (int index = 0; index < count; index++) {
            String path = buffer.readUtf(MAX_PATH_LENGTH);
            JsonElement value = JsonParser.parseString(buffer.readUtf(MAX_VALUE_LENGTH));
            if (values.put(path, value) != null) {
                throw new IllegalArgumentException("CEI server config snapshot contains duplicate path " + path);
            }
        }
        return new CEIConfigSyncPacket(new CEIServerConfigSnapshot(values));
    }
}
