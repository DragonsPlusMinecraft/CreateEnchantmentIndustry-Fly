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

package plus.dragons.createenchantmentindustry.common.item;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEIFluidUnits;

/** Restores the upstream behavior where a placed bucket becomes experience instead of a fluid block. */
public final class ExperienceBucketItem extends FoilBucketItem {
    public ExperienceBucketItem(Fluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public boolean emptyContents(
            @Nullable LivingEntity entity,
            Level level,
            BlockPos pos,
            @Nullable BlockHitResult hitResult) {
        if (!CEIConfig.fluids().experienceVaporizeOnPlacement.get()) {
            return super.emptyContents(entity, level, pos, hitResult);
        }

        Player player = entity instanceof Player source ? source : null;
        level.playSound(player, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.5F, 1.0F);
        if (level instanceof ServerLevel serverLevel) {
            int experience = Math.toIntExact(CEIFluidUnits.toMillibuckets(FluidConstants.BUCKET));
            ExperienceOrb.award(serverLevel, pos.getCenter(), experience);
        }
        return true;
    }
}
