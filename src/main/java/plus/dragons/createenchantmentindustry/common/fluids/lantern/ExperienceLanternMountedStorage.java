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

package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.contraption.storage.SyncedMountedStorage;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIMountedStorageTypes;

public class ExperienceLanternMountedStorage
        extends WrapperMountedFluidStorage<ExperienceLanternMountedStorage.Handler>
        implements SyncedMountedStorage {
    public static final MapCodec<ExperienceLanternMountedStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT
                    .fieldOf("capacity")
                    .forGetter(ExperienceLanternMountedStorage::getCapacity),
            FluidStack.OPTIONAL_CODEC
                    .fieldOf("fluid")
                    .forGetter(ExperienceLanternMountedStorage::getFluid))
            .apply(instance, ExperienceLanternMountedStorage::new));

    private boolean dirty;

    protected ExperienceLanternMountedStorage(MountedFluidStorageType<?> type, int capacity, FluidStack stack) {
        super(type);
        wrapped = new Handler(capacity, stack);
    }

    protected ExperienceLanternMountedStorage(int capacity, FluidStack stack) {
        this(CEIMountedStorageTypes.EXPERIENCE_LANTERN.get(), capacity, stack);
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity blockEntity) {
        if (blockEntity instanceof ExperienceLanternBlockEntity lantern) {
            var tank = lantern.getTank().getPrimaryHandler();
            tank.setFluid(getFluid().copy());
            tank.markDirty();
        }
    }

    public FluidStack getFluid() {
        return wrapped.getFluid();
    }

    public int getCapacity() {
        return wrapped.getMaxAmountPerStack();
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markClean() {
        dirty = false;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public void afterSync(Contraption contraption, BlockPos localPos) {
        BlockEntity blockEntity = AllClientHandle.INSTANCE.getBlockEntityClientSide(contraption, localPos);
        if (blockEntity instanceof ExperienceLanternBlockEntity lantern) {
            var tank = lantern.getTank().getPrimaryHandler();
            tank.setFluid(getFluid().copy());
            tank.markDirty();
        }
    }

    public static ExperienceLanternMountedStorage fromLantern(ExperienceLanternBlockEntity lantern) {
        var tank = lantern.getTank().getPrimaryHandler();
        return new ExperienceLanternMountedStorage(tank.getMaxAmountPerStack(), tank.getFluid().copy());
    }

    public final class Handler extends FluidTank {
        private Handler(int capacity, FluidStack stack) {
            super(capacity);
            setFluid(stack);
        }

        @Override
        public boolean isValid(int slot, FluidStack stack) {
            return !stack.isEmpty() && stack.getFluid() == CEIFluids.EXPERIENCE.getSource();
        }

        @Override
        public void markDirty() {
            dirty = true;
        }
    }
}
