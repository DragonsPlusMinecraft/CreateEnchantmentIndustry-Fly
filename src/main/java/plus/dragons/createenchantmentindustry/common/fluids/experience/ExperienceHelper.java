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

import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.util.CEIFluidUnits;

public class ExperienceHelper {
    private static final int MAX_FLUID_CONVERTIBLE_EXPERIENCE = Math.toIntExact(CEIFluidUnits.toMillibuckets(Integer.MAX_VALUE));

    public static int getExperienceForNextLevel(int level) {
        if (level >= 30)
            return 9 * level - 158;
        if (level >= 15)
            return 5 * level - 38;
        return 2 * level + 7;
    }

    public static int getExperienceForTotalLevel(int level) {
        if (level == 0)
            return 0;
        if (level >= 31)
            return (9 * level * level - 325 * level) / 2 + 2220;
        if (level >= 16)
            return (5 * level * level - 81 * level) / 2 + 360;
        return level * level + 6 * level;
    }

    public static int getExperienceForPlayer(Player player) {
        int experience = getExperienceForTotalLevel(player.experienceLevel);
        experience += Math.round(player.experienceProgress * getExperienceForNextLevel(player.experienceLevel));
        return experience;
    }

    public static int getExperienceFromFluid(FluidStack fluid) {
        if (fluid.isEmpty()) return 0;
        long amount = CEIFluidUnits.toMillibuckets(fluid.getAmount());
        if (fluid.getFluid() == CEIFluids.EXPERIENCE.getSource()) return Math.toIntExact(amount);
        Integer unit = CEIDataMaps.FLUID_UNIT_EXPERIENCE.get(fluid.getFluid());
        if (unit == null)
            return 0;
        return Math.toIntExact(amount / unit);
    }

    public static long getFluidFromExperience(FluidStack fluid, int amount) {
        return getFluidFromExperience(fluid.getFluid(), amount);
    }

    public static long getFluidFromExperience(Fluid fluid, int amount) {
        return CEIFluidUnits.millibuckets(Math.multiplyExact((long) getExperienceFluidUnit(fluid), amount));
    }

    public static int getExperienceFluidUnit(Fluid fluid) {
        if (CEIFluids.EXPERIENCE.getSource().isSame(fluid))
            return 1;
        Integer unit = CEIDataMaps.FLUID_UNIT_EXPERIENCE.get(fluid);
        return unit == null ? 0 : unit;
    }

    public static void award(int amount, ServerPlayer player) {
        amount = repairPlayerItems(player, amount);
        player.giveExperiencePoints(amount);
    }

    public static boolean canRepairItem(ItemStack stack) {
        if (!stack.isDamaged())
            return false;
        return EnchantmentHelper.has(stack, EnchantmentEffectComponents.REPAIR_WITH_XP);
    }

    public static int repairItem(int amount, ServerLevel level, ItemStack stack, boolean simulate) {
        int repairing = amount * 2;
        int repaired = Math.min(repairing, stack.getDamageValue());
        if (repaired == 0)
            return 0;
        if (!simulate) {
            stack.setDamageValue(stack.getDamageValue() - repaired);
        }
        return Math.max(1, repaired * amount / repairing);
    }

    public static int repairPlayerItems(ServerPlayer player, int amount) {
        var entry = EnchantmentHelper.getRandomItemWith(
                EnchantmentEffectComponents.REPAIR_WITH_XP, player, ItemStack::isDamaged);
        if (entry.isPresent()) {
            ItemStack stack = entry.get().itemStack();
            int consumed = repairItem(amount, player.level(), stack, false);
            return amount > consumed
                    ? repairPlayerItems(player, amount - consumed)
                    : 0;
        }
        return amount;
    }

    public static long getTotalExperience(ExperienceOrb orb) {
        return Math.multiplyExact((long) orb.getValue(), orb.count);
    }

    /** Caps a grouped orb to the largest value representable by Create Fly's int-backed FluidStack. */
    public static int getFluidConvertibleExperience(ExperienceOrb orb) {
        return Math.toIntExact(Math.min(getTotalExperience(orb), MAX_FLUID_CONVERTIBLE_EXPERIENCE));
    }

    /**
     * Removes an exact amount from a potentially merged orb without losing its private count. A non-divisible
     * remainder becomes a second, differently-valued orb and therefore cannot merge straight back.
     */
    public static void consumeExperience(ExperienceOrb orb, int consumed) {
        if (consumed <= 0)
            return;
        long total = getTotalExperience(orb);
        if (consumed >= total) {
            orb.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        int unit = orb.getValue();
        long remaining = total - consumed;
        int fullCount = Math.toIntExact(remaining / unit);
        int remainder = Math.toIntExact(remaining % unit);
        if (fullCount == 0) {
            orb.count = 1;
            orb.setValue(remainder);
            return;
        }

        orb.count = fullCount;
        if (remainder != 0) {
            ExperienceOrb fragment = new ExperienceOrb(
                    orb.level(), orb.position(), orb.getDeltaMovement(), remainder);
            orb.level().addFreshEntity(fragment);
        }
    }
}
