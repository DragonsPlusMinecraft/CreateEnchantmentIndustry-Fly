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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import plus.dragons.createenchantmentindustry.config.CEIServerConfigSnapshot;

class CEISyncPacketTest {
    @Test
    void dataMapSnapshotRoundTrips() {
        Identifier type = id("map");
        Identifier key = id("entry");
        CEIDataMapSyncPacket expected = new CEIDataMapSyncPacket(
                Map.of(type, Map.of(key, JsonParser.parseString("{\"value\":12}"))));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            CEIDataMapSyncPacket.encode(expected, buffer);
            assertEquals(expected, CEIDataMapSyncPacket.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void dataMapSnapshotRejectsDuplicateKeys() {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeVarInt(1);
            buffer.writeIdentifier(id("map"));
            buffer.writeVarInt(2);
            buffer.writeIdentifier(id("duplicate"));
            buffer.writeUtf("1");
            buffer.writeIdentifier(id("duplicate"));
            buffer.writeUtf("2");
            assertThrows(IllegalArgumentException.class, () -> CEIDataMapSyncPacket.decode(buffer));
        } finally {
            buffer.release();
        }
    }

    @Test
    void configSnapshotRoundTripsAndRejectsDuplicatePaths() {
        Map<String, com.google.gson.JsonElement> values = new LinkedHashMap<>();
        values.put("fluids.capacity", JsonParser.parseString("4000"));
        values.put("fluids.enabled", JsonParser.parseString("true"));
        CEIConfigSyncPacket expected = new CEIConfigSyncPacket(new CEIServerConfigSnapshot(values));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            CEIConfigSyncPacket.encode(expected, buffer);
            assertEquals(expected, CEIConfigSyncPacket.decode(buffer));
        } finally {
            buffer.release();
        }

        FriendlyByteBuf duplicate = new FriendlyByteBuf(Unpooled.buffer());
        try {
            duplicate.writeVarInt(2);
            duplicate.writeUtf("same");
            duplicate.writeUtf("1");
            duplicate.writeUtf("same");
            duplicate.writeUtf("2");
            assertThrows(IllegalArgumentException.class, () -> CEIConfigSyncPacket.decode(duplicate));
        } finally {
            duplicate.release();
        }
    }

    @Test
    void packetCountsAreBounded() {
        FriendlyByteBuf dataMaps = new FriendlyByteBuf(Unpooled.buffer());
        try {
            dataMaps.writeVarInt(65);
            assertThrows(IllegalArgumentException.class, () -> CEIDataMapSyncPacket.decode(dataMaps));
        } finally {
            dataMaps.release();
        }

        FriendlyByteBuf config = new FriendlyByteBuf(Unpooled.buffer());
        try {
            config.writeVarInt(129);
            assertThrows(IllegalArgumentException.class, () -> CEIConfigSyncPacket.decode(config));
        } finally {
            config.release();
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }
}
