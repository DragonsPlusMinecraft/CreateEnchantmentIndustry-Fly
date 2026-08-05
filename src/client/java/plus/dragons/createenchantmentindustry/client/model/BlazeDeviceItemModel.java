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

package plus.dragons.createenchantmentindustry.client.model;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/**
 * 1.21.11 item-model implementation for the three CEI blaze devices.
 *
 * <p>The base and optional hat are baked as regular item layers. The classic
 * enchanter book remains custom geometry so it uses the vanilla animated book
 * model without falling back to the removed custom item renderer API.
 */
public final class BlazeDeviceItemModel
        implements ItemModel, SpecialModelRenderer<BlazeDeviceItemModel.RenderData> {
    public static final Identifier ID = CEICommon.asResource("model/blaze_device");
    private static final Identifier BOOK_TEXTURE = CEICommon.asResource("textures/block/blaze_enchanter_book.png");

    private final BakedPart base;
    private final @Nullable BakedPart hat;
    private final boolean book;
    private final Supplier<BookModel> bookModel = Suppliers.memoize(() -> new BookModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BOOK)));

    private BlazeDeviceItemModel(BakedPart base, @Nullable BakedPart hat, boolean book) {
        this.base = base;
        this.hat = hat;
        this.book = book;
    }

    @Override
    public void update(
            ItemStackRenderState state,
            ItemStack stack,
            ItemModelResolver resolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        ItemStackRenderState.FoilType foil = stack.hasFoil()
                ? ItemStackRenderState.FoilType.STANDARD
                : ItemStackRenderState.FoilType.NONE;
        if (foil != ItemStackRenderState.FoilType.NONE) {
            state.appendModelIdentityElement(foil);
        }

        addLayer(state, displayContext, base, base.properties(), foil, null);
        if (hat != null) {
            addLayer(state, displayContext, hat, base.properties(), foil, RenderKind.HAT);
        }
        if (book) {
            LayerRenderState layer = state.newLayer();
            layer.setRenderType(Sheets.translucentItemSheet());
            layer.setExtents(base.extents());
            base.properties().applyToLayer(layer, displayContext);
            layer.setupSpecialModel(
                    this,
                    new RenderData(
                            RenderKind.BOOK,
                            List.of(),
                            Sheets.translucentItemSheet(),
                            ItemStackRenderState.FoilType.NONE));
        }
    }

    private void addLayer(
            ItemStackRenderState state,
            ItemDisplayContext displayContext,
            BakedPart part,
            ModelRenderProperties properties,
            ItemStackRenderState.FoilType foil,
            @Nullable RenderKind special) {
        LayerRenderState layer = state.newLayer();
        layer.setRenderType(Sheets.translucentItemSheet());
        layer.setExtents(part.extents());
        properties.applyToLayer(layer, displayContext);
        layer.prepareQuadList().addAll(part.quads());
        layer.setFoilType(foil);
        if (special != null) {
            layer.setupSpecialModel(
                    this,
                    new RenderData(special, part.quads(), Sheets.translucentItemSheet(), foil));
        }
    }

    @Override
    public void submit(
            RenderData data,
            ItemDisplayContext displayContext,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int light,
            int overlay,
            boolean glint,
            int packedColor) {
        if (data.kind() == RenderKind.HAT) {
            matrices.pushPose();
            matrices.translate(0.5F, 0.75F, 0.5F);
            queue.submitItem(
                    matrices,
                    displayContext,
                    light,
                    overlay,
                    packedColor,
                    new int[0],
                    data.quads(),
                    data.renderType(),
                    data.foil());
            matrices.popPose();
            return;
        }

        matrices.pushPose();
        matrices.translate(0.0F, -0.3F, 0.0F);
        matrices.mulPose(Axis.ZP.rotationDegrees(90.0F));
        matrices.mulPose(Axis.XP.rotationDegrees(-90.0F));
        matrices.translate(0.0F, 0.05F, 0.0F);
        matrices.scale(1.2F, 1.2F, 1.2F);
        float page0 = Mth.clamp(Mth.frac(0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float page1 = Mth.clamp(Mth.frac(0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        queue.submitCustomGeometry(
                matrices,
                RenderTypes.entitySolid(BOOK_TEXTURE),
                new BookGeometry(bookModel.get(), new BookModel.State(0.0F, page0, page1, 1.0F)));
        matrices.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        throw new UnsupportedOperationException("Extents are supplied by the baked item layers");
    }

    @Override
    public RenderData extractArgument(ItemStack stack) {
        throw new UnsupportedOperationException("Render data is supplied during item-model update");
    }

    private enum RenderKind {
        HAT,
        BOOK
    }

    public record RenderData(
            RenderKind kind,
            List<BakedQuad> quads,
            RenderType renderType,
            ItemStackRenderState.FoilType foil) {}

    private record BakedPart(
            List<BakedQuad> quads,
            ModelRenderProperties properties,
            Supplier<Vector3fc[]> extents) {}

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

    public record Unbaked(Identifier model, Optional<Identifier> hat, boolean book)
            implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
                Identifier.CODEC.optionalFieldOf("hat").forGetter(Unbaked::hat),
                Codec.BOOL.optionalFieldOf("book", false).forGetter(Unbaked::book))
                .apply(instance, Unbaked::new));
        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(model);
            hat.ifPresent(resolver::markDependency);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            BakedPart bakedBase = bake(baker, model);
            return new BlazeDeviceItemModel(
                    bakedBase, hat.map(id -> bake(baker, id)).orElse(null), book);
        }

        private static BakedPart bake(ModelBaker baker, Identifier id) {
            ResolvedModel model = baker.getModel(id);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            Supplier<Vector3fc[]> extents = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(quads));
            return new BakedPart(quads, properties, extents);
        }
    }
}
