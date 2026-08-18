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

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderState;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderer;

/** Render-state renderer for the item orbiting a Blaze Enchanter. */
public final class BlazeEnchanterRenderer extends BlazeBlockRenderer<BlazeEnchanterBlockEntity> {
    private final ItemModelResolver itemModelResolver;

    public BlazeEnchanterRenderer(BlockEntityRendererProvider.Context context) {
        super(context, true);
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public EnchanterRenderState createRenderState() {
        return new EnchanterRenderState();
    }

    @Override
    protected void extractAdditionalRenderState(
            BlazeEnchanterBlockEntity blockEntity,
            BlazeBlockRenderState baseState,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        EnchanterRenderState state = (EnchanterRenderState) baseState;
        extractTemplateRenderState(blockEntity, state, cameraPos);
        state.item = null;
        if (blockEntity.heldItem.isEmpty() || blockEntity.getLevel() == null) {
            return;
        }
        ItemStackRenderState item = new ItemStackRenderState();
        itemModelResolver.appendItemLayers(
                item,
                blockEntity.heldItem,
                ItemDisplayContext.FIXED,
                blockEntity.getLevel(),
                null,
                blockEntity.hashCode());
        float renderTicks = AnimationTickHolder.getTicks(blockEntity.getLevel()) + tickProgress;
        float animation = blockEntity.processingTime == -1
                ? 0
                : Mth.sin((blockEntity.processingTime + tickProgress) / 20.0F);
        state.item = item;
        state.itemHeight = 1.25F + (1 + animation) * 0.25F;
        state.itemXRot = (renderTicks * 5 + blockEntity.getBlockPos().getX()) % 360;
        state.itemZRot = (renderTicks * 5 + blockEntity.getBlockPos().getZ()) % 360;
    }

    private void extractTemplateRenderState(
            BlazeEnchanterBlockEntity blockEntity, EnchanterRenderState state, Vec3 cameraPos) {
        state.template = null;
        state.templateSide = null;
        EnchanterBehaviour behaviour = blockEntity.getBehaviour(EnchanterBehaviour.TYPE);
        if (behaviour == null || behaviour.getTemplate().isEmpty() || blockEntity.getLevel() == null) {
            return;
        }
        if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult)
                || !hitResult.getBlockPos().equals(blockEntity.getBlockPos())) {
            return;
        }
        TemplateItemSlot transform = new TemplateItemSlot();
        transform.fromSide(hitResult.getDirection());
        Vec3 localHit = hitResult
                .getLocation()
                .subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()));
        if (!transform.testHit(
                blockEntity.getLevel(),
                blockEntity.getBlockPos(),
                state.blockState,
                localHit)) {
            return;
        }
        float maxDistance = AllConfigs.client().filterItemRenderDistance.getF();
        if (!blockEntity.isVirtual()
                && cameraPos.distanceToSqr(VecHelper.getCenterOf(blockEntity.getBlockPos())) > maxDistance * maxDistance) {
            return;
        }
        ItemStackRenderState template = new ItemStackRenderState();
        itemModelResolver.appendItemLayers(
                template,
                behaviour.getTemplate(),
                ItemDisplayContext.FIXED,
                blockEntity.getLevel(),
                null,
                blockEntity.hashCode());
        state.template = template;
        state.templateSide = hitResult.getDirection();
    }

    @Override
    protected void submitBeforeBlaze(
            BlazeBlockRenderState baseState,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        EnchanterRenderState state = (EnchanterRenderState) baseState;
        if (state.template != null && state.templateSide != null) {
            TemplateItemSlot transform = new TemplateItemSlot();
            transform.fromSide(state.templateSide);
            matrices.pushPose();
            transform.transform(state.blockState, matrices);
            ValueBoxRenderer.renderItemIntoValueBox(
                    state.template, queue, matrices, state.lightCoords, 0);
            matrices.popPose();
        }
        if (state.item != null) {
            matrices.pushPose();
            matrices.translate(0.5F, state.itemHeight, 0.5F);
            matrices.mulPose(Axis.XP.rotationDegrees(state.itemXRot));
            matrices.mulPose(Axis.ZP.rotationDegrees(state.itemZRot));
            matrices.scale(0.5F, 0.5F, 0.5F);
            state.item.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
    }

    public static final class EnchanterRenderState extends BlazeBlockRenderState {
        private @Nullable ItemStackRenderState template;
        private @Nullable Direction templateSide;
        private @Nullable ItemStackRenderState item;
        private float itemHeight;
        private float itemXRot;
        private float itemZRot;
    }

    private static final class TemplateItemSlot extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 12, 14.5);
        }

        @Override
        public boolean testHit(
                LevelAccessor level, BlockPos pos, BlockState state, Vec3 localHit) {
            if (!isSideActive(state, getSide()))
                return false;
            Vec3 location = VecHelper.voxelSpace(8, 8, 13.5);
            location = VecHelper.rotateCentered(
                    location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
            return localHit.distanceTo(location) < scale * 1.2;
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
