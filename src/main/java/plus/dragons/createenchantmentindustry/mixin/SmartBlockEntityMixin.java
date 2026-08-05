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

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceFluidDropContext;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;

@Mixin(value = SmartBlockEntity.class, remap = false)
public abstract class SmartBlockEntityMixin extends CachedRenderBBBlockEntity {
    @Shadow
    public abstract <T extends BlockEntityBehaviour> @Nullable T getBehaviour(BehaviourType<T> type);

    @Shadow
    public abstract Collection<BlockEntityBehaviour> getAllBehaviours();

    public SmartBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "destroy", at = @At(value = "HEAD"), remap = false)
    private void setRemoved$dropExperienceFluid(CallbackInfo ci) {
        if (!(this.level instanceof ServerLevel serverLevel))
            return;
        var state = this.getBlockState();
        for (var behaviour : this.getAllBehaviours()) {
            if (!(behaviour instanceof SmartFluidTankBehaviour tank))
                continue;
            for (FluidStack fluid : tank.getCapability()) {
                if (fluid.isEmpty())
                    continue;
                int experience = ExperienceHelper.getExperienceFromFluid(fluid);
                ExperienceFluidDropContext.dropExperience(serverLevel, state, this.worldPosition, experience);
            }
        }
    }
}
