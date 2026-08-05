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

import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.util.CEIFluidUnits;

/** Server-side experience-fluid filter and exchange amount. */
public class ExperienceHatchBehaviour extends ServerFilteringBehaviour {
    public static final int POINTS_PER_SCROLL = 10;

    public ExperienceHatchBehaviour(SmartBlockEntity blockEntity) {
        super(blockEntity);
        forFluids();
        count = 0;
    }

    public FluidStack getFluidToDrain() {
        Fluid fluid = filter.fluid(blockEntity.getLevel()).getFluid();
        int unit;
        if (Fluids.EMPTY.isSame(fluid)) {
            unit = 1;
            fluid = CEIFluids.EXPERIENCE.getSource();
        } else {
            unit = ExperienceHelper.getExperienceFluidUnit(fluid);
        }
        if (unit == 0)
            return FluidStack.EMPTY;
        int amount = count == 0
                ? Integer.MAX_VALUE
                : Math.toIntExact(CEIFluidUnits.millibuckets(Math.multiplyExact((long) count * POINTS_PER_SCROLL, unit)));
        return new FluidStack(fluid, amount);
    }

    public FluidStack getFluidToFill(int available) {
        if (available == 0)
            return FluidStack.EMPTY;
        Fluid fluid = filter.fluid(blockEntity.getLevel()).getFluid();
        int unit;
        if (Fluids.EMPTY.isSame(fluid)) {
            unit = 1;
            fluid = CEIFluids.EXPERIENCE.getSource();
        } else {
            unit = ExperienceHelper.getExperienceFluidUnit(fluid);
        }
        if (unit == 0)
            return FluidStack.EMPTY;
        long points = count == 0 ? available : Math.min((long) available, (long) count * POINTS_PER_SCROLL);
        int amount = Math.toIntExact(CEIFluidUnits.millibuckets(Math.multiplyExact(points, unit)));
        return new FluidStack(fluid, amount);
    }

    @Override
    public void write(ValueOutput output, boolean clientPacket) {
        output.store("Filter", FilterItemStack.CODEC, filter);
        output.putInt("Scroll", count);
    }

    @Override
    public void read(ValueInput input, boolean clientPacket) {
        filter = input.read("Filter", FilterItemStack.CODEC).orElseGet(FilterItemStack::empty);
        count = input.getIntOr("Scroll", 0);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
        if (getValueSettings().equals(settings))
            return;
        count = Math.max(0, settings.value());
        blockEntity.setChanged();
        blockEntity.sendData();
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, count);
    }

    @Override
    public boolean isCountVisible() {
        return true;
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        FilterItemStack candidate = FilterItemStack.of(stack.copy());
        if (!candidate.isEmpty()) {
            FluidStack fluid = candidate.fluid(blockEntity.getLevel());
            if (fluid.getFluid() != CEIFluids.EXPERIENCE.getSource()
                    && CEIDataMaps.FLUID_UNIT_EXPERIENCE.get(fluid.getFluid()) == null)
                return false;
        }
        filter = candidate;
        blockEntity.setChanged();
        blockEntity.sendData();
        return true;
    }

    @Override
    public String getClipboardKey() {
        return "ExperienceHatch";
    }
}
