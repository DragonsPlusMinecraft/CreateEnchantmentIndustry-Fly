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

package plus.dragons.createenchantmentindustry.client;

import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.infrastructure.fluid.FluidConfig;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

/** Fabric and Create Fly render metadata for CEI's experience fluid. */
public final class CEIFluidRenderers {
    private static final Identifier STILL = CEICommon.asResource("fluid/experience_still");
    private static final Identifier FLOW = CEICommon.asResource("fluid/experience_flow");
    private static final int COLOR = 0xB2FF9A;

    private CEIFluidRenderers() {}

    public static void register() {
        Fluid source = CEIFluids.EXPERIENCE.getSource();
        Fluid flowing = CEIFluids.EXPERIENCE.getFlowing();
        SimpleFluidRenderHandler handler = new SimpleFluidRenderHandler(STILL, FLOW, 0xFF000000 | COLOR);
        FluidRenderHandlerRegistry.INSTANCE.register(source, flowing, handler);
        BlockRenderLayerMap.putFluids(ChunkSectionLayer.TRANSLUCENT, source, flowing);
        FluidConfig config = new FluidConfig(
                () -> handler.getFluidSprites(null, null, source.defaultFluidState())[0],
                () -> handler.getFluidSprites(null, null, source.defaultFluidState())[1],
                components -> 0xFF000000 | COLOR,
                () -> 96.0F / 256.0F,
                COLOR);
        AllFluidConfigs.ALL.put(source, config);
        AllFluidConfigs.ALL.put(flowing, config);
    }
}
