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

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.transfer.FluidInventoryStorage;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.util.BlazeLightningHelper;
import plus.dragons.createenchantmentindustry.util.CEIFluidUnits;
import plus.dragons.createenchantmentindustry.util.CEILang;

public abstract class BlazeExperienceBlockEntity extends BlazeBlockEntity {
    public static final String LIGHTNING_BOLT_EXPERIENCE_CHARGE_KEY = BlazeLightningHelper.LIGHTNING_BOLT_EXPERIENCE_CHARGE_KEY;
    public static final TagKey<Block> LIGHTNING_ROD_BLOCKS = BlazeLightningHelper.LIGHTNING_ROD_BLOCKS;
    public static final TagKey<PoiType> LIGHTNING_ROD_POINT_OF_INTEREST_TYPES = BlazeLightningHelper.LIGHTNING_ROD_POINT_OF_INTEREST_TYPES;

    private boolean creative;
    protected CEIExperienceTankBehaviour normalTank;
    protected CEIExperienceTankBehaviour specialTank;

    public BlazeExperienceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract int getExperienceTankCapacity();

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        int capacity = Math.toIntExact(CEIFluidUnits.millibuckets(getExperienceTankCapacity()));
        normalTank = new CEIExperienceTankBehaviour(SmartFluidTankBehaviour.INPUT, this, capacity, true);
        specialTank = new CEIExperienceTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, capacity, false);
        behaviours.add(normalTank);
        behaviours.add(specialTank);
    }

    @Override
    public boolean isCreative() {
        return creative;
    }

    @Override
    public HeatLevel getHeatLevel() {
        if (getSpecialExperience() > 0) {
            return HeatLevel.SEETHING;
        }
        TankSegment tank = getNormalTank();
        if (!tank.getFluid().isEmpty()) {
            double fill = tank.getFluid().getAmount() / (double) tank.getMaxAmountPerStack();
            return fill < 0.0125 ? HeatLevel.FADING : HeatLevel.KINDLED;
        }
        return HeatLevel.SMOULDERING;
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        output.putBoolean("isCreative", creative);
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        creative = input.getBooleanOr("isCreative", false);
        if (creative && normalTank != null && specialTank != null) {
            setCreativeTanks(getHeatLevelFromBlock());
        }
    }

    public TankSegment getNormalTank() {
        return normalTank.getPrimaryHandler();
    }

    public TankSegment getSpecialTank() {
        return specialTank.getPrimaryHandler();
    }

    public int getNormalExperience() {
        return Math.toIntExact(CEIFluidUnits.toMillibuckets(getNormalTank().getFluid().getAmount()));
    }

    public int getSpecialExperience() {
        return Math.toIntExact(CEIFluidUnits.toMillibuckets(getSpecialTank().getFluid().getAmount()));
    }

    public int getTotalExperience() {
        return getNormalExperience() + getSpecialExperience();
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (normalTank == null || specialTank == null) {
            return false;
        }
        CEILang.translateCreate("gui.goggles.fluid_container").forGoggles(tooltip);
        addTankToGoggleTooltip(tooltip, false, getNormalTank());
        addTankToGoggleTooltip(tooltip, true, getSpecialTank());
        return true;
    }

    private static void addTankToGoggleTooltip(List<Component> tooltip, boolean special, TankSegment tank) {
        CEILang.Builder mb = CEILang.translateCreate("generic.unit.millibuckets");
        CEILang.translate(special
                ? "gui.goggles.blaze_experience.super_experience"
                : "gui.goggles.blaze_experience.experience")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        CEILang.builder()
                .add(CEILang.number(CEIFluidUnits.toMillibuckets(tank.getFluid().getAmount()))
                        .add(mb)
                        .style(special ? ChatFormatting.BLUE : ChatFormatting.GOLD))
                .add(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
                .add(CEILang.number(CEIFluidUnits.toMillibuckets(tank.getMaxAmountPerStack()))
                        .add(mb)
                        .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 2);
    }

    public @Nullable Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
        if (normalTank == null || specialTank == null || isRemoved() || side != null && side != Direction.DOWN) {
            return null;
        }
        return new CombinedStorage<>(List.of(
                FluidInventoryStorage.of(normalTank.getCapability(), side),
                FluidInventoryStorage.of(specialTank.getCapability(), side)));
    }

    public boolean consumeExperience(int amount, boolean special, boolean simulate) {
        FluidStack fluid = CEIFluidUnits.stack(CEIFluids.EXPERIENCE.getSource(), amount);
        CEIExperienceTankBehaviour tank = special ? specialTank : normalTank;
        return tank.extractExperience(fluid, simulate) == fluid.getAmount();
    }

    public boolean applyExperienceFuel(ExperienceFuel fuel, boolean forceOverflow, boolean simulate) {
        if (level == null || creative) {
            return false;
        }
        CEIExperienceTankBehaviour tank = fuel.special() ? specialTank : normalTank;
        FluidStack experience = CEIFluidUnits.stack(CEIFluids.EXPERIENCE.getSource(), fuel.experience());
        int inserted = tank.insertExperience(experience, true);
        if (inserted == 0 || inserted != experience.getAmount() && !forceOverflow) {
            return false;
        }
        if (simulate) {
            return true;
        }
        tank.insertExperience(experience.copyWithAmount(inserted), false);
        if (level.isClientSide()) {
            spawnParticleBurst(fuel.special());
        }
        HeatLevel previous = getHeatLevelFromBlock();
        playSound();
        updateBlockState();
        if (previous != getHeatLevelFromBlock()) {
            level.playSound(
                    null,
                    worldPosition,
                    SoundEvents.BLAZE_AMBIENT,
                    SoundSource.BLOCKS,
                    .125f + level.getRandom().nextFloat() * .125f,
                    1.15f - level.getRandom().nextFloat() * .25f);
        }
        notifyUpdate();
        return true;
    }

    public void applyCreativeFuel() {
        if (level == null) {
            return;
        }
        creative = true;
        HeatLevel next = getHeatLevelFromBlock().nextActiveLevel();
        if (level.isClientSide()) {
            spawnParticleBurst(next.isAtLeast(HeatLevel.SEETHING));
            return;
        }
        playSound();
        if (next == HeatLevel.FADING) {
            next = next.nextActiveLevel();
        }
        setCreativeTanks(next);
        setBlockHeat(next);
        notifyUpdate();
    }

    protected void setCreativeTanks(HeatLevel heatLevel) {
        switch (heatLevel) {
            case KINDLED -> {
                normalTank.setCreative(true);
                specialTank.setCreative(false);
            }
            case SEETHING -> {
                normalTank.setCreative(true);
                specialTank.setCreative(true);
            }
            default -> {
                normalTank.setCreative(false);
                specialTank.setCreative(false);
            }
        }
    }

    protected @Nullable BlockPos getStrikePos() {
        return level == null ? null : BlazeLightningHelper.getStrikePos(level, worldPosition);
    }

    protected boolean strikeLightning(ServerLevel level, BlockPos strikePos) {
        return BlazeLightningHelper.strikeLightning(level, strikePos);
    }
}
