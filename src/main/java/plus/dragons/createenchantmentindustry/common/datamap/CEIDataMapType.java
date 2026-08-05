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

package plus.dragons.createenchantmentindustry.common.datamap;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;

/**
 * A Fabric 1.21.11 representation of CEI's resource-pack data-map type.
 *
 * <p>The type only describes the registry, id and codec. Values live in the immutable snapshots managed by
 * {@link CEIDataMaps}; keeping those responsibilities separate prevents partially applied reloads.</p>
 */
public final class CEIDataMapType<K, V> {
    private final Identifier id;
    private final ResourceKey<? extends Registry<K>> registryKey;
    private final Registry<K> staticRegistry;
    private final Codec<V> codec;
    private final Identifier resource;

    public CEIDataMapType(Identifier id, Registry<K> registry, Codec<V> codec) {
        this(id, registry.key(), registry, codec);
    }

    public CEIDataMapType(
            Identifier id,
            ResourceKey<? extends Registry<K>> registryKey,
            Codec<V> codec) {
        this(id, registryKey, null, codec);
    }

    private CEIDataMapType(
            Identifier id,
            ResourceKey<? extends Registry<K>> registryKey,
            Registry<K> staticRegistry,
            Codec<V> codec) {
        this.id = Objects.requireNonNull(id);
        this.registryKey = Objects.requireNonNull(registryKey);
        this.staticRegistry = staticRegistry;
        this.codec = Objects.requireNonNull(codec);
        this.resource = Identifier.fromNamespaceAndPath(
                id.getNamespace(),
                "data_maps/" + registryKey.identifier().getPath() + "/" + id.getPath() + ".json");
    }

    public Identifier id() {
        return id;
    }

    public ResourceKey<? extends Registry<K>> registryKey() {
        return registryKey;
    }

    public Registry<K> staticRegistry() {
        return staticRegistry;
    }

    public Codec<V> codec() {
        return codec;
    }

    public Identifier resource() {
        return resource;
    }

    public V get(K key) {
        return CEIDataMaps.get(this, key);
    }

    public V get(Holder<K> holder) {
        return CEIDataMaps.get(this, holder);
    }

    public Stream<Pair<K, V>> entries() {
        return CEIDataMaps.entries(this);
    }
}
