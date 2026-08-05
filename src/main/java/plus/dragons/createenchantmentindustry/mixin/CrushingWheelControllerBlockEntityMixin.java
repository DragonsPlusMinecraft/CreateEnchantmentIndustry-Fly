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

import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.common.kinetics.crusher.CrushingWheelExperience;

@Mixin(CrushingWheelControllerBlockEntity.class)
public class CrushingWheelControllerBlockEntityMixin {
    @Shadow(remap = false)
    public Entity processingEntity;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z", shift = At.Shift.AFTER))
    private void tick$dropExperienceNuggets(CallbackInfo ci) {
        if (!processingEntity.isAlive() && processingEntity instanceof LivingEntity livingEntity)
            CrushingWheelExperience.dropForCrushedEntity((CrushingWheelControllerBlockEntity) (Object) this, livingEntity);
    }
}
