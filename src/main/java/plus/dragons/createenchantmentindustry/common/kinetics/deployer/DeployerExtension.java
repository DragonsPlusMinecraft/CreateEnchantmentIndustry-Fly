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

package plus.dragons.createenchantmentindustry.common.kinetics.deployer;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.kinetics.deployer.DeployerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public final class DeployerExtension {
    private static final ThreadLocal<DeployerPlayer> BLOCK_EXPERIENCE_CONTEXT = new ThreadLocal<>();

    private DeployerExtension() {}

    public static int handleKillExperience(DeployerPlayer deployer, int droppedExperience) {
        int experience = Mth.ceil(droppedExperience * CEIConfig.kinetics().deployerKillXpScale.getF());
        if (experience > 0 && CEIConfig.kinetics().deployerCollectXp.get()) {
            collectExperience(deployer, experience);
            return 0;
        }
        return experience;
    }

    public static int handleBlockExperience(DeployerPlayer deployer, int droppedExperience) {
        boolean dropXp = CEIConfig.kinetics().deployerMineDropXp.get();
        int experience = dropXp
                ? Mth.ceil(droppedExperience * CEIConfig.kinetics().deployerMineXpScale.getF())
                : 0;
        if (experience > 0 && CEIConfig.kinetics().deployerCollectXp.get()) {
            collectExperience(deployer, experience);
            return 0;
        }
        return experience;
    }

    public static void beginBlockExperience(DeployerPlayer deployer) {
        BLOCK_EXPERIENCE_CONTEXT.set(deployer);
    }

    public static void endBlockExperience() {
        BLOCK_EXPERIENCE_CONTEXT.remove();
    }

    public static int handleCurrentBlockExperience(int droppedExperience) {
        DeployerPlayer deployer = BLOCK_EXPERIENCE_CONTEXT.get();
        return deployer == null ? droppedExperience : handleBlockExperience(deployer, droppedExperience);
    }

    public static void collectExperience(DeployerPlayer deployer, int experience) {
        if (experience <= 0)
            return;
        var player = deployer.cast();
        ServerLevel level = (ServerLevel) player.level();
        if (CEIConfig.kinetics().deployerMendItem.get()) {
            ItemStack heldItem = player.getMainHandItem();
            if (ExperienceHelper.canRepairItem(heldItem))
                experience -= ExperienceHelper.repairItem(experience, level, heldItem, false);
        }
        if (experience <= 0)
            return;
        int nuggets = experience / 3;
        if (level.random.nextFloat() < (experience % 3) / 3f)
            nuggets++;
        if (nuggets > 0) {
            player.getInventory().placeItemBackInInventory(new ItemStack(AllItems.EXP_NUGGET, nuggets));
        }
    }
}
