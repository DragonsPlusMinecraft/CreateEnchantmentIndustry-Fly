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
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
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
        BlockTintSource tint = state -> 0xFF000000 | COLOR;
        FluidModel.Unbaked model = new FluidModel.Unbaked(
                new Material(STILL, true),
                new Material(FLOW, true),
                null,
                tint);
        FluidRenderingRegistry.register(source, flowing, model);
        AllFluidConfigs.FOG_COLOR.put(source, COLOR);
        AllFluidConfigs.FOG_COLOR.put(flowing, COLOR);
        AllFluidConfigs.FOG_DISTANCE.put(source, () -> 96.0F / 256.0F);
        AllFluidConfigs.FOG_DISTANCE.put(flowing, () -> 96.0F / 256.0F);
        AllFluidConfigs.TINT.put(source, (fluid, components) -> 0xFF000000 | COLOR);
        AllFluidConfigs.TINT.put(flowing, (fluid, components) -> 0xFF000000 | COLOR);
    }
}
