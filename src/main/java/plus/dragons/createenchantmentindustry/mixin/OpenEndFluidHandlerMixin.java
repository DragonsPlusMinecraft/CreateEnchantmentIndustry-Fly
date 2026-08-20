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

package plus.dragons.createenchantmentindustry.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.fluids.OpenEndedPipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

@Mixin(targets = "com.zurrtum.create.content.fluids.OpenEndedPipe$OpenEndFluidHandler", remap = false)
public class OpenEndFluidHandlerMixin {
    @WrapOperation(method = { "getMaxAmount", "markDirty" }, at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/foundation/fluid/FluidHelper;hasBlockState(Lnet/minecraft/world/level/material/Fluid;)Z"))
    private boolean treatVaporizingExperienceAsEffectFluid(Fluid fluid, Operation<Boolean> original) {
        if (isVaporizingExperience(fluid))
            return false;
        return original.call(fluid);
    }

    @WrapOperation(method = "markDirty", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/OpenEndedPipe;provideFluidToSpace(Lcom/zurrtum/create/infrastructure/fluids/FluidStack;Z)Z"))
    private boolean consumeVaporizingExperience(
            OpenEndedPipe pipe, FluidStack fluid, boolean simulate, Operation<Boolean> original) {
        if (isVaporizingExperience(fluid.getFluid()))
            return true;
        return original.call(pipe, fluid, simulate);
    }

    private static boolean isVaporizingExperience(Fluid fluid) {
        if (!CEIConfig.fluids().experienceVaporizeOnPlacement.get())
            return false;
        return fluid == CEIFluids.EXPERIENCE.getSource() || fluid == CEIFluids.EXPERIENCE.getFlowing();
    }
}
