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

import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import plus.dragons.createenchantmentindustry.common.kinetics.deployer.DeployerExtension;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract @Nullable Player getLastHurtByPlayer();

    @Shadow
    protected abstract int getExperienceReward(ServerLevel level, @Nullable Entity attacker);

    @Inject(method = "dropExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getExperienceReward(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)I"), cancellable = true)
    private void dropExperience$handleDeployer(ServerLevel level, Entity attacker, CallbackInfo ci) {
        if (!(getLastHurtByPlayer() instanceof DeployerPlayer deployer)
                || !CEIConfig.kinetics().deployerKillDropXp.get()) {
            return;
        }
        int experience = DeployerExtension.handleKillExperience(deployer, getExperienceReward(level, attacker));
        if (experience > 0) {
            ExperienceOrb.award(level, ((LivingEntity) (Object) this).position(), experience);
        }
        // This point precedes Create Fly's default suppression at ExperienceOrb.award.
        ci.cancel();
    }
}
