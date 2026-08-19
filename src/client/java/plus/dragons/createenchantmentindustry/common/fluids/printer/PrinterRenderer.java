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

package plus.dragons.createenchantmentindustry.common.fluids.printer;

import static plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity.PROCESSING_TIME;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;

/** Printer renderer that snapshots tank and piston animation state during extraction. */
public final class PrinterRenderer
        extends SmartBlockEntityRenderer<PrinterBlockEntity, PrinterRenderer.PrinterRenderState> {
    private static final int PISTON_MOVING_TIME = 5;

    public PrinterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public PrinterRenderState createRenderState() {
        return new PrinterRenderState();
    }

    @Override
    public void extractRenderState(
            PrinterBlockEntity printer,
            PrinterRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(printer, state, tickProgress, cameraPos, crumblingOverlay);
        state.fluid = null;
        TankSegment tank = printer.tank.getPrimaryTank();
        FluidStack stack = tank.getRenderedFluid();
        float level = tank.getFluidLevel().getValue(tickProgress);
        if (!stack.isEmpty() && level != 0) {
            level = Math.max(level, 0.175F) * (11 / 16.0F);
            float min = 2.5F / 16.0F;
            float max = min + 11 / 16.0F;
            state.fluid = FluidRenderHelper.extractFluidRenderState(
                    null,
                    null,
                    Minecraft.getInstance().getModelManager().getFluidStateModelSet(),
                    stack.getFluid(),
                    stack.getComponentChanges(),
                    min,
                    min,
                    min,
                    max,
                    min + level,
                    max,
                    state.lightCoords,
                    false,
                    true);
        }

        float progress = getProgress(printer.processingTicks - tickProgress);
        state.machine = new MachineGeometry(
                CachedBuffers.partial(CEIPartialModels.PRINTER_NOZZLE_TOP, printer.getBlockState()),
                CachedBuffers.partial(CEIPartialModels.PRINTER_NOZZLE_BOTTOM, printer.getBlockState()),
                CachedBuffers.partial(CEIPartialModels.PRINTER_PISTON, printer.getBlockState()),
                progress,
                state.lightCoords);
    }

    @Override
    public void submit(
            PrinterRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.fluid != null) {
            state.fluid.submit(matrices, queue);
        }
        if (state.machine != null) {
            queue.submitCustomGeometry(matrices, RenderTypes.solidMovingBlock(), state.machine);
        }
    }

    public static float getProgress(float ticks) {
        if (ticks < 0) {
            return 0;
        }
        if (ticks < PISTON_MOVING_TIME) {
            return Mth.lerp(ticks / PISTON_MOVING_TIME, 0, 1);
        }
        if (ticks < PROCESSING_TIME - PISTON_MOVING_TIME) {
            return 1;
        }
        if (ticks < PROCESSING_TIME) {
            return Mth.lerp((PROCESSING_TIME - ticks) / PISTON_MOVING_TIME, 0, 1);
        }
        return 0;
    }

    public static final class PrinterRenderState extends SmartRenderState {
        private @Nullable FluidRenderHelper.FluidRenderState fluid;
        private @Nullable MachineGeometry machine;
    }

    private record MachineGeometry(
            SuperByteBuffer top,
            SuperByteBuffer bottom,
            SuperByteBuffer piston,
            float progress,
            int light) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose pose, VertexConsumer consumer) {
            top.light(light).renderInto(pose, consumer);
            pose.translate(0, 3 * progress / 32.0F, 0);
            bottom.light(light).renderInto(pose, consumer);
            piston.translate(0, -progress / 2.0F, 0).light(light).renderInto(pose, consumer);
        }
    }
}
