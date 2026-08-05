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

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.fluids.spout.FillingBySpout;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;

@Mixin(value = FillingBySpout.class, remap = false)
public class FillingBySpoutMixin {
    @Inject(method = "canItemBeFilled", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/transfer/GenericItemFilling;canItemBeFilled(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Z"), cancellable = true)
    private static void canItemBeFilled$mending(Level level, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ExperienceHelper.canRepairItem(stack))
            cir.setReturnValue(true);
    }

    @Inject(method = "getRequiredAmountForItem", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElseGet(Ljava/util/function/Supplier;)Ljava/lang/Object;"), cancellable = true)
    private static void getRequiredAmountForItem$mending(
            ServerLevel level,
            ItemStack stack,
            FluidStack availableFluid,
            CallbackInfoReturnable<Integer> cir,
            @Local Optional<?> recipe) {
        if (recipe.isPresent() || !ExperienceHelper.canRepairItem(stack)) {
            return;
        }
        int availableXp = ExperienceHelper.getExperienceFromFluid(availableFluid);
        if (availableXp == 0)
            return;
        int requiredXp = ExperienceHelper.repairItem(availableXp, level, stack, true);
        long requiredFluid = ExperienceHelper.getFluidFromExperience(availableFluid, requiredXp);
        if (requiredFluid > 0)
            cir.setReturnValue(Math.toIntExact(requiredFluid));
    }

    @Inject(method = "fillItem", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/transfer/GenericItemFilling;fillItem(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/item/ItemStack;Lcom/zurrtum/create/infrastructure/fluids/FluidStack;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private static void fillItem$mending(ServerLevel level, int requiredAmount, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
        if (ExperienceHelper.canRepairItem(stack)) {
            int availableXp = ExperienceHelper.getExperienceFromFluid(availableFluid);
            if (availableXp == 0)
                return;
            var result = stack.copy();
            stack.shrink(1);
            ExperienceHelper.repairItem(availableXp, level, result, false);
            availableFluid.decrement(requiredAmount);
            cir.setReturnValue(result);
        }
    }
}
