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

package plus.dragons.createenchantmentindustry.client.tooltip;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;

/** Client-only adapters for Create Fly's behaviour-based goggle overlay API. */
public final class CEITooltipBehaviours {
    private CEITooltipBehaviours() {}

    public static void register() {
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.MECHANICAL_GRINDSTONE.get(), KineticTooltipBehaviour::new);
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.GRINDSTONE_DRAIN.get(), GrindstoneDrainTooltipBehaviour::new);
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.PRINTER.get(),
                blockEntity -> new GoggleTooltipBehaviour<>(
                        blockEntity,
                        printer -> printer.getTank().getCapability(),
                        (printer, tooltip, sneaking) -> printer.addToGoggleTooltip(tooltip, sneaking)));
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.BLAZE_ENCHANTER.get(),
                blockEntity -> new GoggleTooltipBehaviour<>(
                        blockEntity,
                        (enchanter, tooltip, sneaking) -> enchanter.addToGoggleTooltip(tooltip, sneaking)));
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.BLAZE_FORGER.get(),
                blockEntity -> new GoggleTooltipBehaviour<>(
                        blockEntity,
                        (forger, tooltip, sneaking) -> forger.addToGoggleTooltip(tooltip, sneaking)));
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.CLASSIC_BLAZE_ENCHANTER.get(),
                blockEntity -> new GoggleTooltipBehaviour<>(
                        blockEntity,
                        (enchanter, tooltip, sneaking) -> enchanter.addToGoggleTooltip(tooltip, sneaking)));
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.EXPERIENCE_LANTERN.get(),
                blockEntity -> new GoggleTooltipBehaviour<>(
                        blockEntity, lantern -> lantern.getTank().getCapability(), null));
    }

    private static final class GoggleTooltipBehaviour<T extends SmartBlockEntity> extends TooltipBehaviour<T>
            implements IHaveGoggleInformation {
        private final @Nullable Function<T, FluidInventory> fluidInventory;
        private final @Nullable TooltipAppender<T> appender;

        private GoggleTooltipBehaviour(T blockEntity, TooltipAppender<T> appender) {
            this(blockEntity, null, appender);
        }

        private GoggleTooltipBehaviour(
                T blockEntity,
                @Nullable Function<T, FluidInventory> fluidInventory,
                @Nullable TooltipAppender<T> appender) {
            super(blockEntity);
            this.fluidInventory = fluidInventory;
            this.appender = appender;
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            boolean added = fluidInventory != null
                    && containedFluidTooltip(tooltip, isPlayerSneaking, fluidInventory.apply(blockEntity));
            if (appender != null) {
                added |= appender.add(blockEntity, tooltip, isPlayerSneaking);
            }
            return added;
        }
    }

    private static final class GrindstoneDrainTooltipBehaviour
            extends KineticTooltipBehaviour<GrindstoneDrainBlockEntity> {
        private GrindstoneDrainTooltipBehaviour(GrindstoneDrainBlockEntity blockEntity) {
            super(blockEntity);
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
            boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
            added |= containedFluidTooltip(
                    tooltip, isPlayerSneaking, blockEntity.getTank().getCapability());
            return added;
        }
    }

    @FunctionalInterface
    private interface TooltipAppender<T extends SmartBlockEntity> {
        boolean add(T blockEntity, List<Component> tooltip, boolean isPlayerSneaking);
    }
}
