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

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.level.storage.ValueInput;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

/**
 * CEI-only experience tank. It deliberately builds on Create Fly's tank behaviour instead of
 * reintroducing CDP's removed generic configurable-tank API.
 */
public final class CEIExperienceTankBehaviour extends SmartFluidTankBehaviour {
    private final boolean externalInsertion;
    private Runnable immediateFluidUpdateCallback = () -> {};
    private boolean creative;

    public CEIExperienceTankBehaviour(
            BehaviourType<SmartFluidTankBehaviour> type,
            SmartBlockEntity blockEntity,
            int capacity,
            boolean externalInsertion) {
        super(type, blockEntity, 1, capacity, false, Handler::new);
        this.externalInsertion = externalInsertion;
    }

    @Override
    public CEIExperienceTankBehaviour whenFluidUpdates(Runnable fluidUpdateCallback) {
        immediateFluidUpdateCallback = fluidUpdateCallback;
        super.whenFluidUpdates(this::notifyFluidUpdated);
        return this;
    }

    @Override
    public void initialize() {
        super.initialize();
        notifyFluidUpdated();
    }

    @Override
    public void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        notifyFluidUpdated();
    }

    /** Replaces the stored experience while preserving update and synchronization semantics. */
    public void setFluid(FluidStack stack) {
        if (!stack.isEmpty() && stack.getFluid() != CEIFluids.EXPERIENCE.getSource()) {
            throw new IllegalArgumentException("CEI experience tanks only accept liquid experience");
        }
        TankSegment tank = getPrimaryHandler();
        tank.setFluid(stack);
        tank.markDirty();
        notifyFluidUpdated();
    }

    public void setCreative(boolean creative) {
        this.creative = creative;
        TankSegment tank = getPrimaryHandler();
        setFluid(creative
                ? new FluidStack(CEIFluids.EXPERIENCE.getSource(), tank.getMaxAmountPerStack())
                : FluidStack.EMPTY);
    }

    public boolean isCreativeTank() {
        return creative;
    }

    /** Internal machine insertion; the special slot intentionally bypasses external restrictions. */
    public int insertExperience(FluidStack stack, boolean simulate) {
        if (stack.isEmpty() || stack.getFluid() != CEIFluids.EXPERIENCE.getSource()) {
            return 0;
        }
        if (creative) {
            return stack.getAmount();
        }
        TankSegment tank = getPrimaryHandler();
        FluidStack current = tank.getFluid();
        if (!current.isEmpty() && !FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(current, stack)) {
            return 0;
        }
        int inserted = Math.min(stack.getAmount(), tank.getMaxAmountPerStack() - current.getAmount());
        if (!simulate && inserted > 0) {
            tank.setFluid(current.isEmpty() ? stack.copyWithAmount(inserted) : current.copyWithAmount(current.getAmount() + inserted));
            tank.markDirty();
            notifyFluidUpdated();
        }
        return inserted;
    }

    /** Internal machine extraction with a non-mutating simulation path. */
    public int extractExperience(FluidStack stack, boolean simulate) {
        if (stack.isEmpty() || stack.getFluid() != CEIFluids.EXPERIENCE.getSource()) {
            return 0;
        }
        if (creative) {
            return stack.getAmount();
        }
        TankSegment tank = getPrimaryHandler();
        FluidStack current = tank.getFluid();
        if (current.isEmpty() || !FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(current, stack)) {
            return 0;
        }
        int extracted = Math.min(stack.getAmount(), current.getAmount());
        if (!simulate && extracted > 0) {
            int remainder = current.getAmount() - extracted;
            tank.setFluid(remainder == 0 ? FluidStack.EMPTY : current.copyWithAmount(remainder));
            tank.markDirty();
            notifyFluidUpdated();
        }
        return extracted;
    }

    private void notifyFluidUpdated() {
        immediateFluidUpdateCallback.run();
    }

    private static final class Handler extends InternalFluidHandler {
        private final CEIExperienceTankBehaviour owner;

        private Handler(SmartFluidTankBehaviour behaviour, boolean enforceVariety, Optional<Integer> max) {
            super(behaviour, enforceVariety, max);
            owner = (CEIExperienceTankBehaviour) behaviour;
        }

        @Override
        public boolean isValid(int slot, FluidStack stack) {
            return !stack.isEmpty() && stack.getFluid() == CEIFluids.EXPERIENCE.getSource();
        }

        @Override
        public boolean canInsert(int slot, FluidStack stack, Direction direction) {
            return owner.externalInsertion && super.canInsert(slot, stack, direction) && isValid(slot, stack);
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            if (owner.creative) {
                FluidStack current = getStack(slot);
                if (!current.isEmpty()) {
                    current.setAmount(getMaxAmountPerStack());
                    super.setStack(slot, current);
                    return;
                }
            }
            super.setStack(slot, stack);
        }

        @Override
        public void markDirty() {
            super.markDirty();
            owner.notifyFluidUpdated();
        }
    }
}
