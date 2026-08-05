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

package plus.dragons.createenchantmentindustry.common.processing.forger;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderState;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderer;

/** Render-state renderer for the four item slots orbiting a Blaze Forger. */
public final class BlazeForgerRenderer extends BlazeBlockRenderer<BlazeForgerBlockEntity> {
    private final ItemModelResolver itemModelResolver;

    public BlazeForgerRenderer(BlockEntityRendererProvider.Context context) {
        super(context, true);
        itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ForgerRenderState createRenderState() {
        return new ForgerRenderState();
    }

    @Override
    protected void extractAdditionalRenderState(
            BlazeForgerBlockEntity blockEntity,
            BlazeBlockRenderState baseState,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        ForgerRenderState state = (ForgerRenderState) baseState;
        List<OrbitingItem> items = new ArrayList<>(4);
        if (blockEntity.getLevel() == null) {
            state.items = List.of();
            return;
        }
        float renderTicks = AnimationTickHolder.getTicks(blockEntity.getLevel()) + tickProgress;
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = blockEntity.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStackRenderState item = new ItemStackRenderState();
            itemModelResolver.appendItemLayers(
                    item,
                    stack,
                    ItemDisplayContext.FIXED,
                    blockEntity.getLevel(),
                    null,
                    blockEntity.hashCode() + slot);
            float animation = blockEntity.processingTime == -1
                    ? Mth.sin(slot * Mth.PI / -2.0F)
                    : Mth.sin((blockEntity.processingTime + tickProgress) / 20.0F + slot * Mth.PI);
            items.add(new OrbitingItem(
                    item,
                    1.25F + (1 + animation) * 0.25F,
                    (renderTicks * 5 + blockEntity.getBlockPos().getX() + slot * 180) % 360,
                    (renderTicks * 5 + blockEntity.getBlockPos().getZ() + slot * 180) % 360));
        }
        state.items = List.copyOf(items);
    }

    @Override
    protected void submitBeforeBlaze(
            BlazeBlockRenderState baseState,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        ForgerRenderState state = (ForgerRenderState) baseState;
        for (OrbitingItem item : state.items) {
            matrices.pushPose();
            matrices.translate(0.5F, item.height, 0.5F);
            matrices.mulPose(Axis.XP.rotationDegrees(item.xRot));
            matrices.mulPose(Axis.ZP.rotationDegrees(item.zRot));
            matrices.scale(0.5F, 0.5F, 0.5F);
            item.state.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
    }

    public static final class ForgerRenderState extends BlazeBlockRenderState {
        private List<OrbitingItem> items = List.of();
    }

    private record OrbitingItem(ItemStackRenderState state, float height, float xRot, float zRot) {}
}
