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

import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import com.zurrtum.create.infrastructure.fluids.FluidEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createdragonsplus.api.registry.CDPFluidEntry;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceEffectHandler;

/** Experience source/flowing pair registered from Create Fly's early fluid hook. */
public final class CEIFluids {
    private static FluidEntry internal;
    public static CDPFluidEntry<FlowableFluid> EXPERIENCE;
    /** Source-compatible alias retained for integrations that previously referenced the flowing entry. */
    public static CDPFluidEntry<FlowableFluid> EXPERIENCE_FLOWING;

    private static boolean registered;
    private static boolean initialized;

    private CEIFluids() {}

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;

        Identifier id = CEICommon.asResource("experience");
        Identifier flowingId = id.withPrefix("flowing_");
        ResourceKey<Fluid> sourceKey = ResourceKey.create(Registries.FLUID, id);
        ResourceKey<Fluid> flowingKey = ResourceKey.create(Registries.FLUID, flowingId);

        internal = new FluidEntry();
        internal.still = Registry.register(BuiltInRegistries.FLUID, sourceKey, new FlowableFluid.Still(internal));
        internal.flowing = Registry.register(BuiltInRegistries.FLUID, flowingKey, new FlowableFluid.Flowing(internal));
        EXPERIENCE = new CDPFluidEntry<>(id, internal.still, internal.flowing, () -> internal.bucket, () -> internal.block);
        EXPERIENCE_FLOWING = EXPERIENCE;
    }

    static void attachBlock(FluidBlock block) {
        requireRegistered();
        if (internal.block != null) {
            throw new IllegalStateException("Experience fluid block was already attached");
        }
        internal.block = block;
    }

    static void attachBucket(BucketItem bucket) {
        requireRegistered();
        if (internal.bucket != null) {
            throw new IllegalStateException("Experience bucket was already attached");
        }
        internal.bucket = bucket;
    }

    public static synchronized void initialize() {
        requireRegistered();
        if (initialized) {
            return;
        }
        initialized = true;
        ExperienceEffectHandler handler = new ExperienceEffectHandler();
        OpenPipeEffectHandler.REGISTRY.register(EXPERIENCE.getSource(), handler);
        OpenPipeEffectHandler.REGISTRY.register(EXPERIENCE.getFlowing(), handler);
    }

    private static void requireRegistered() {
        if (!registered || EXPERIENCE == null) {
            throw new IllegalStateException("Experience fluid was not registered by CreateRegisterPlugin");
        }
    }
}
