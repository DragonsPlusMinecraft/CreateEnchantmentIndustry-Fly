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
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
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

    @Override
    protected void submitBeforeBlaze(
            BlazeBlockRenderState baseState,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        EnchanterRenderState state = (EnchanterRenderState) baseState;
        if (state.item == null) {
            return;
        }
        matrices.pushPose();
        matrices.translate(0.5F, state.itemHeight, 0.5F);
        matrices.mulPose(Axis.XP.rotationDegrees(state.itemXRot));
        matrices.mulPose(Axis.ZP.rotationDegrees(state.itemZRot));
        matrices.scale(0.5F, 0.5F, 0.5F);
        state.item.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();
    }

    public static final class EnchanterRenderState extends BlazeBlockRenderState {
        private @Nullable ItemStackRenderState item;
        private float itemHeight;
        private float itemXRot;
        private float itemZRot;
    }
}
