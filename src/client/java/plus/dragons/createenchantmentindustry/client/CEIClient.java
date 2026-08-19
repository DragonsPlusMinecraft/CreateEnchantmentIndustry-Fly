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

import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import plus.dragons.createdragonsplus.client.processing.blaze.BlazeRenderModelProvider;
import plus.dragons.createdragonsplus.client.processing.blaze.BlazeRenderModels;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockVisual;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeMovementRenderBehaviour;
import plus.dragons.createenchantmentindustry.client.model.BlazeDeviceItemModel;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createenchantmentindustry.client.ponder.CEIPonderPlugin;
import plus.dragons.createenchantmentindustry.client.tooltip.CEITooltipBehaviours;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterRenderer;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainRenderer;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterRenderer;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterVisual;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterRenderer;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerRenderer;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

/** Client-only registration for rendering, Ponder and synchronized data-map state. */
public final class CEIClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CEIConfig.initializeClient();
        CEIClientNetwork.register();
        AllModels.register(BlazeDeviceItemModel.ID, BlazeDeviceItemModel.Unbaked.CODEC);
        CEIPartialModels.register();
        CEIFluidRenderers.register();
        CEIClientBehaviours.register();
        registerBlazeModelsAndMovement();
        CEITooltipBehaviours.register();
        registerBlockEntityRenderers();
        PonderIndex.addPlugin(new CEIPonderPlugin());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CEIDataMaps.clearClientSnapshot();
            CEIConfig.clearServerSnapshot();
        });
    }

    private static void registerBlazeModelsAndMovement() {
        BlazeRenderModels.register(
                CEIBlocks.BLAZE_ENCHANTER.get(),
                BlazeRenderModelProvider.hat(context -> context.heatLevel().isAtLeast(
                        com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.FADING)
                                ? CEIPartialModels.BLAZE_ENCHANTER_HAT
                                : CEIPartialModels.BLAZE_ENCHANTER_HAT_SMALL));
        BlazeRenderModels.register(
                CEIBlocks.BLAZE_FORGER.get(),
                BlazeRenderModelProvider.hat(context -> context.heatLevel().isAtLeast(
                        com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel.FADING)
                                ? CEIPartialModels.BLAZE_FORGER_HAT
                                : CEIPartialModels.BLAZE_FORGER_HAT_SMALL));
        BlazeRenderModels.register(
                CEIBlocks.CLASSIC_BLAZE_ENCHANTER.get(), new BlazeRenderModelProvider() {});

        BlazeMovementRenderBehaviour.attachTo(CEIBlocks.BLAZE_ENCHANTER_MOVEMENT);
        BlazeMovementRenderBehaviour.attachTo(CEIBlocks.BLAZE_FORGER_MOVEMENT);
        BlazeMovementRenderBehaviour.attachTo(CEIBlocks.CLASSIC_BLAZE_ENCHANTER_MOVEMENT);
    }

    private static void registerBlockEntityRenderers() {
        AllBlockEntityRenders.visual(
                CEIBlockEntities.MECHANICAL_GRINDSTONE.get(),
                KineticBlockEntityRenderer::new,
                SingleAxisRotatingVisual.of(CEIPartialModels.MECHANICAL_GRINDSTONE));
        AllBlockEntityRenders.normal(
                CEIBlockEntities.GRINDSTONE_DRAIN.get(),
                GrindstoneDrainRenderer::new,
                SingleAxisRotatingVisual.of(CEIPartialModels.MECHANICAL_GRINDSTONE));
        BlockEntityRenderers.register(
                CEIBlockEntities.EXPERIENCE_HATCH.get(), SmartBlockEntityRenderer::new);
        BlockEntityRenderers.register(CEIBlockEntities.PRINTER.get(), PrinterRenderer::new);
        AllBlockEntityRenders.normal(
                CEIBlockEntities.BLAZE_ENCHANTER.get(),
                BlazeEnchanterRenderer::new,
                BlazeBlockVisual::new);
        AllBlockEntityRenders.normal(
                CEIBlockEntities.BLAZE_FORGER.get(),
                BlazeForgerRenderer::new,
                BlazeBlockVisual::new);
        AllBlockEntityRenders.normal(
                CEIBlockEntities.CLASSIC_BLAZE_ENCHANTER.get(),
                ClassicBlazeEnchanterRenderer::new,
                ClassicBlazeEnchanterVisual::new);
        BlockEntityRenderers.register(
                CEIBlockEntities.EXPERIENCE_LANTERN.get(), SmartBlockEntityRenderer::new);
    }
}
