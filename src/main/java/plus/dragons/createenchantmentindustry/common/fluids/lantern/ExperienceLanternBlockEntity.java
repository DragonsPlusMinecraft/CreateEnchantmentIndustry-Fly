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

package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import static net.minecraft.world.level.block.DirectionalBlock.FACING;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.transfer.FluidInventoryStorage;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.experience.CEIExperienceTankBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHelper;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEIFluidUnits;

public class ExperienceLanternBlockEntity extends SmartBlockEntity {
    protected CEIExperienceTankBehaviour tank;
    protected AABB effectiveAABB;
    protected int rate;

    public ExperienceLanternBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        effectiveAABB = new AABB(getBlockPos()).inflate(0.5);
        rate = CEIConfig.fluids().experienceLanternDrainRate.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide() && level.getGameTime() % 10 == 0) {
            drainExp();
        }
        if (!level.isClientSide() && CEIConfig.fluids().experienceLanternPullToggle.get()) {
            pullExp();
        }
    }

    public CEIExperienceTankBehaviour getTank() {
        return tank;
    }

    protected void drainExp() {
        List<Player> players = level.getEntitiesOfClass(Player.class, effectiveAABB, player -> player.isAlive() && !player.isSpectator());
        if (!players.isEmpty()) {
            AtomicInteger sum = new AtomicInteger();
            players.forEach(player -> {
                var playerExp = ExperienceHelper.getExperienceForPlayer(player);
                if (playerExp >= rate) sum.addAndGet(rate);
                else if (playerExp != 0) sum.addAndGet(playerExp);
            });
            if (sum.get() != 0) {
                long insertedUnits = tank.insertExperience(
                        CEIFluidUnits.stack(CEIFluids.EXPERIENCE.getSource(), sum.get()), false);
                int inserted = Math.toIntExact(CEIFluidUnits.toMillibuckets(insertedUnits));
                if (inserted != 0) {
                    for (var player : players) {
                        var total = ExperienceHelper.getExperienceForPlayer(player);
                        if (inserted >= rate) {
                            if (total >= rate) {
                                player.giveExperiencePoints(-rate);
                                inserted -= rate;
                            } else if (total != 0) {
                                inserted -= total;
                                player.giveExperiencePoints(-total);
                            }
                        } else if (inserted > 0) {
                            if (total >= inserted) {
                                player.giveExperiencePoints(-inserted);
                                inserted = 0;
                            } else {
                                inserted -= total;
                                player.giveExperiencePoints(-total);
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(ExperienceOrb.class, effectiveAABB);
        if (!experienceOrbs.isEmpty()) {
            for (var orb : experienceOrbs) {
                int amount = ExperienceHelper.getFluidConvertibleExperience(orb);
                var fluidStack = CEIFluidUnits.stack(CEIFluids.EXPERIENCE.getSource(), amount);
                long insertedUnits = tank.insertExperience(fluidStack, false);
                int inserted = Math.toIntExact(CEIFluidUnits.toMillibuckets(insertedUnits));
                ExperienceHelper.consumeExperience(orb, inserted);
                if (!orb.isRemoved()) {
                    break;
                }
            }
        }
    }

    protected void pullExp() {
        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(ExperienceOrb.class, effectiveAABB.inflate(CEIConfig.fluids().experienceLanternPullRadius.get()));
        if (!experienceOrbs.isEmpty()) {
            for (var orb : experienceOrbs) {
                if (orb.getDeltaMovement().length() <= .5) {
                    var pushForce = CEIConfig.fluids().experienceLanternPullForceMultiplier.get() * 1 / orb.position().distanceTo(getBlockPos().getCenter());
                    var directionToLantern = getBlockPos().getCenter().subtract(orb.position()).normalize().multiply(pushForce, pushForce, pushForce);
                    orb.push(directionToLantern.x, directionToLantern.y, directionToLantern.z);
                }
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        int capacity = Math.toIntExact(
                CEIFluidUnits.millibuckets(CEIConfig.fluids().experienceLanternFluidCapacity.get()));
        tank = new CEIExperienceTankBehaviour(SmartFluidTankBehaviour.INPUT, this, capacity, true)
                .whenFluidUpdates(this::onFluidStackChanged);
        behaviours.add(tank);
    }

    protected void onFluidStackChanged() {
        var segment = tank.getPrimaryHandler();
        int light = (int) (segment.getFluid().getAmount() / (float) segment.getMaxAmountPerStack() * 15f);
        light = Math.min(Math.max(0, light), 15);
        if (level != null && getBlockState().getValue(ExperienceLanternBlock.LIGHT) != light)
            level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(ExperienceLanternBlock.LIGHT, light));
    }

    public @Nullable Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
        return tank != null && (side == null || side.getOpposite() == getBlockState().getValue(FACING))
                ? FluidInventoryStorage.of(tank.getCapability(), side)
                : null;
    }
}
