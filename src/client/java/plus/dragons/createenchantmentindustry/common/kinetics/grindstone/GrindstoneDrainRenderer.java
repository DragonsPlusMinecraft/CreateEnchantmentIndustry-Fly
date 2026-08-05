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

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

/** Kinetic fallback plus render-state snapshots for drain items and experience fluid. */
public final class GrindstoneDrainRenderer extends KineticBlockEntityRenderer<GrindstoneDrainBlockEntity, GrindstoneDrainRenderer.DrainRenderState> {
    private final ItemModelResolver itemModelResolver;

    public GrindstoneDrainRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public DrainRenderState createRenderState() {
        return new DrainRenderState();
    }

    @Override
    public void extractRenderState(
            GrindstoneDrainBlockEntity drain,
            DrainRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(drain, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.support) {
            updateBaseRenderState(drain, state, drain.getLevel(), crumblingOverlay);
        }
        extractItems(drain, state, tickProgress);
        extractFluid(drain, state, tickProgress);
    }

    @Override
    public void submit(
            DrainRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (!state.items.isEmpty()) {
            submitItems(state, matrices, queue);
        }
        if (state.fluid != null) {
            queue.submitCustomGeometry(matrices, RenderTypes.translucentMovingBlock(), state.fluid);
        }
    }

    @Override
    protected BlockState getRenderedBlockState(GrindstoneDrainBlockEntity blockEntity) {
        return CEIBlocks.MECHANICAL_GRINDSTONE
                .get()
                .defaultBlockState()
                .setValue(RotatedPillarKineticBlock.AXIS, getRotationAxisOf(blockEntity));
    }

    private void extractItems(GrindstoneDrainBlockEntity drain, DrainRenderState state, float tickProgress) {
        state.items = List.of();
        if (drain.inventory.isEmpty() || drain.getLevel() == null) {
            return;
        }
        boolean alongZ = drain.getBlockState()
                .getValue(HorizontalKineticBlock.HORIZONTAL_FACING)
                .getAxis() == Direction.Axis.Z;
        float duration = drain.inventory.recipeDuration;
        boolean moving = duration != 0;
        float offset = moving ? drain.inventory.remainingTime / duration : 0;
        float processingSpeed = Mth.clamp(drain.getRelativeSpeed() / 32, 1, 128);
        if (moving) {
            offset = Mth.clamp(
                    offset + (-tickProgress + 0.5F) * processingSpeed / duration,
                    0.125F,
                    1.0F);
            if (!drain.inventory.appliedRecipe) {
                offset += 1;
            }
            offset /= 2;
        }
        if (drain.getSpeed() == 0) {
            offset = 0.5F;
        } else if ((drain.getSpeed() < 0) ^ alongZ) {
            offset = 1 - offset;
        }

        int outputCount = 0;
        for (int slot = 1; slot < drain.inventory.getContainerSize(); slot++) {
            if (!drain.inventory.getItem(slot).isEmpty()) {
                outputCount++;
            }
        }
        List<DrainItem> items = new ArrayList<>();
        int rendered = 0;
        for (int slot = 0; slot < drain.inventory.getContainerSize(); slot++) {
            ItemStack stack = drain.inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStackRenderState item = new ItemStackRenderState();
            itemModelResolver.appendItemLayers(
                    item,
                    stack,
                    ItemDisplayContext.FIXED,
                    drain.getLevel(),
                    null,
                    drain.hashCode() + slot);
            float xOffset = slot > 0 && outputCount > 1
                    ? (0.5F / (outputCount - 1)) * rendered
                    : 0;
            items.add(new DrainItem(item, xOffset, slot * 133));
            rendered++;
        }
        state.items = List.copyOf(items);
        state.alongZ = alongZ;
        state.beltOffset = offset;
        state.outputCount = outputCount;
        state.itemY = Mth.lerp(Mth.sin(offset * Mth.PI), 13 / 16.0F, 1.0F);
    }

    private void extractFluid(GrindstoneDrainBlockEntity drain, DrainRenderState state, float tickProgress) {
        state.fluid = null;
        if (drain.tank == null) {
            return;
        }
        TankSegment tank = drain.tank.getPrimaryTank();
        FluidStack stack = tank.getRenderedFluid();
        float level = tank.getFluidLevel().getValue(tickProgress);
        if (!stack.isEmpty() && level != 0) {
            float min = 2 / 16.0F;
            float max = min + 12 / 16.0F;
            float minY = 5 / 16.0F;
            state.fluid = new FluidGeometry(
                    stack.getFluid(),
                    stack.getComponentChanges(),
                    min,
                    max,
                    minY,
                    minY + level * (7 / 16.0F),
                    state.lightCoords);
        }
    }

    private static void submitItems(
            DrainRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        matrices.pushPose();
        if (state.alongZ) {
            matrices.mulPose(Axis.YP.rotationDegrees(90));
        }
        matrices.translate(state.outputCount <= 1 ? 0.5F : 0.25F, 0, 1 - state.beltOffset);
        matrices.translate(state.alongZ ? -1 : 0, 0, 0);
        for (DrainItem item : state.items) {
            matrices.pushPose();
            matrices.translate(item.xOffset, item.state.usesBlockLight() ? state.itemY + 0.1125F : state.itemY, 0);
            if (item.xOffset != 0) {
                TransformStack.of(matrices).nudge(item.nudgeSeed);
            }
            matrices.scale(0.5F, 0.5F, 0.5F);
            matrices.mulPose(Axis.XP.rotationDegrees(90));
            item.state.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
        matrices.popPose();
    }

    public static final class DrainRenderState extends KineticRenderState {
        private List<DrainItem> items = List.of();
        private boolean alongZ;
        private float beltOffset;
        private int outputCount;
        private float itemY;
        private @Nullable FluidGeometry fluid;
    }

    private record DrainItem(ItemStackRenderState state, float xOffset, int nudgeSeed) {}

    private record FluidGeometry(
            Fluid fluid,
            DataComponentPatch components,
            float min,
            float max,
            float minY,
            float maxY,
            int light) implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose pose, VertexConsumer consumer) {
            FluidRenderHelper.renderFluidBox(
                    fluid,
                    components,
                    min,
                    minY,
                    min,
                    max,
                    maxY,
                    max,
                    consumer,
                    pose,
                    light,
                    false,
                    false);
        }
    }
}
