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

package plus.dragons.createenchantmentindustry.common.processing.classic_enchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderState;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderer;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** Render-state renderer for the classic enchanter's animated book and held item. */
public final class ClassicBlazeEnchanterRenderer
        extends BlazeBlockRenderer<ClassicBlazeEnchanterBlockEntity> {
    private static final Identifier BOOK_TEXTURE = CEICommon.asResource("textures/block/blaze_enchanter_book.png");
    private final ItemModelResolver itemModelResolver;
    private final BookModel bookModel;

    public ClassicBlazeEnchanterRenderer(BlockEntityRendererProvider.Context context) {
        super(context, true);
        itemModelResolver = context.itemModelResolver();
        bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public ClassicRenderState createRenderState() {
        return new ClassicRenderState();
    }

    @Override
    protected void extractAdditionalRenderState(
            ClassicBlazeEnchanterBlockEntity blockEntity,
            BlazeBlockRenderState baseState,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        ClassicRenderState state = (ClassicRenderState) baseState;
        float time = AnimationTickHolder.getRenderTime(blockEntity.getLevel());
        float flip = Mth.lerp(tickProgress, blockEntity.oFlip, blockEntity.flip);
        float page0 = Mth.clamp(Mth.frac(flip + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float page1 = Mth.clamp(Mth.frac(flip + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        state.book = new BookGeometry(bookModel, new BookModel.State(time, page0, page1, 1.0F));
        state.bookBob = 0.1F + Mth.sin(time * 0.1F) * 0.01F;
        state.bookAngle = AngleHelper.rad(blockEntity.headAngle.getValue(tickProgress));
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
        ClassicRenderState state = (ClassicRenderState) baseState;
        if (state.book != null) {
            matrices.pushPose();
            matrices.translate(0.5, 0.25 + state.bookBob, 0.5);
            matrices.mulPose(Axis.YP.rotation(state.bookAngle + Mth.HALF_PI));
            matrices.mulPose(Axis.ZP.rotationDegrees(80.0F));
            matrices.scale(1.2F, 1.2F, 1.2F);
            queue.submitCustomGeometry(matrices, RenderTypes.entitySolid(BOOK_TEXTURE), state.book);
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

    @Override
    protected void transformBlaze(BlazeBlockRenderState state, PoseStack matrices) {
        matrices.translate(0, 0.2F, 0);
    }

    public static final class ClassicRenderState extends BlazeBlockRenderState {
        private @Nullable BookGeometry book;
        private float bookBob;
        private float bookAngle;
        private @Nullable ItemStackRenderState item;
        private float itemHeight;
        private float itemXRot;
        private float itemZRot;
    }

    private record BookGeometry(BookModel model, BookModel.State animation)
            implements SubmitNodeCollector.CustomGeometryRenderer {
        @Override
        public void render(PoseStack.Pose pose, VertexConsumer consumer) {
            model.setupAnim(animation);
            PoseStack matrices = new PoseStack();
            matrices.last().pose().set(pose.pose());
            matrices.last().normal().set(pose.normal());
            model.renderToBuffer(
                    matrices,
                    consumer,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY);
        }
    }
}
