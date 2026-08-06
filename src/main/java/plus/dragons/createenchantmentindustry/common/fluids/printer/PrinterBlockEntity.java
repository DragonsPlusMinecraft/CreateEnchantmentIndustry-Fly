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

package plus.dragons.createenchantmentindustry.common.fluids.printer;

import static com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult.PASS;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.transfer.FluidInventoryStorage;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.advancement.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.AddressPrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.CustomNamePrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PackagePatternPrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEIFluidUnits;

public class PrinterBlockEntity extends SmartBlockEntity {
    public static final int PROCESSING_TIME = 50;
    private static final int COMPLETION_TICKS = 5;
    protected SmartFluidTankBehaviour tank;
    private PrinterBehaviour printer;
    public int processingTicks = -1;
    private AdvancementBehaviour advancement;
    private @Nullable ActivePrinting activePrinting;

    public PrinterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        tank = SmartFluidTankBehaviour.single(
                this,
                Math.toIntExact(CEIFluidUnits.millibuckets(CEIConfig.fluids().printerFluidCapacity.get())));
        printer = new PrinterBehaviour(this, tank);
        BeltProcessingBehaviour processing = new BeltProcessingBehaviour(this)
                .whenItemEnters(this::onItemEnters)
                .whileItemHeld(this::onItemHeld);
        advancement = new AdvancementBehaviour(this);
        behaviours.add(tank);
        behaviours.add(printer);
        behaviours.add(processing);
        behaviours.add(advancement);
    }

    public @Nullable Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
        return tank != null && side != Direction.DOWN
                ? FluidInventoryStorage.of(tank.getCapability(), side)
                : null;
    }

    private FluidStack getFluidInTank() {
        return tank.getPrimaryHandler().getFluid();
    }

    private void setFluidInTank(FluidStack fluidStack) {
        tank.getPrimaryHandler().setFluid(fluidStack);
    }

    public ProcessingResult onItemEnters(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        Level level = this.level;
        assert level != null;

        if (handler.blockEntity.isVirtual())
            return PASS;

        var printing = printer.getPrintingBehaviour();
        if (!printing.isValid())
            return PASS;

        int requiredItem = printing.getRequiredItemCount(level, transported.stack);
        if (requiredItem <= 0 || transported.stack.getCount() < requiredItem)
            return PASS;

        var fluidStack = getFluidInTank();
        if (fluidStack.isEmpty())
            return HOLD;
        if (printing.getRequiredFluidAmount(level, transported.stack, fluidStack) <= 0)
            return PASS;

        return HOLD;
    }

    public ProcessingResult onItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        Level level = this.level;
        assert level != null;

        if (processingTicks > COMPLETION_TICKS) {
            if (activePrinting == null || !activePrinting.matchesInput(transported.stack)) {
                cancelProcessing();
                return startProcessing(transported);
            }
            return HOLD;
        }

        if (processingTicks == -1)
            return startProcessing(transported);

        ActivePrinting active = activePrinting;
        if (active == null) {
            cancelProcessing();
            return startProcessing(transported);
        }
        if (!active.matchesInput(transported.stack)) {
            cancelProcessing();
            return startProcessing(transported);
        }

        var fluidStack = getFluidInTank();
        if (!active.matchesFluid(fluidStack) || fluidStack.getAmount() < active.requiredFluidAmount())
            return HOLD;

        var completedPrinting = PrintingBehaviour.create(level, tank, active.template()).result();
        if (completedPrinting.isEmpty()) {
            cancelProcessing();
            return PASS;
        }

        transported.clearFanProcessingData();
        TransportedItemStack output = transported.copy();
        output.stack = active.result().copy();
        TransportedItemStack remains = null;
        if (transported.stack.getCount() > active.requiredItemCount()) {
            remains = transported.copy();
            remains.stack.shrink(active.requiredItemCount());
        }
        handler.handleProcessingOnItem(
                transported,
                TransportedResult.convertToAndLeaveHeld(List.of(output), remains));

        fluidStack.decrement(active.requiredFluidAmount());
        setFluidInTank(fluidStack);
        PrintingBehaviour printing = completedPrinting.get();
        printing.getResult(level, active.input().copy(), active.fluid().copy());
        printing.onFinished(level, worldPosition, this);
        awardPrintingAdvancements(active.result(), printing);
        advancement.awardStat(CEIStats.PRINT.get(), 1);
        finishProcessing();
        notifyUpdate();
        return HOLD;
    }

    private ProcessingResult startProcessing(TransportedItemStack transported) {
        Level level = this.level;
        assert level != null;

        var printing = printer.getPrintingBehaviour();
        if (!printing.isValid())
            return PASS;

        var requiredItem = printing.getRequiredItemCount(level, transported.stack);
        if (requiredItem <= 0 || transported.stack.getCount() < requiredItem)
            return PASS;

        var fluidStack = getFluidInTank();
        var requiredFluid = printing.getRequiredFluidAmount(level, transported.stack, fluidStack);
        if (requiredFluid <= 0)
            return PASS;
        if (fluidStack.getAmount() < requiredFluid)
            return HOLD;

        ItemStack input = transported.stack.copy();
        input.setCount(requiredItem);
        ItemStack resultItem = printing.getResult(level, input.copy(), fluidStack.copy());
        if (resultItem.isEmpty())
            return PASS;
        FluidStack fluidCost = fluidStack.copy();
        fluidCost.setAmount(requiredFluid);
        activePrinting = new ActivePrinting(
                input,
                printer.getFilter().copy(),
                fluidCost,
                resultItem.copy(),
                requiredItem,
                requiredFluid);
        processingTicks = PROCESSING_TIME;
        notifyUpdate();
        AllSoundEvents.SPOUTING.playOnServer(level, worldPosition, 0.75f, 0.9f + 0.2f * level.random.nextFloat());
        return HOLD;
    }

    private void awardPrintingAdvancements(ItemStack resultItem, PrintingBehaviour printing) {
        if (printing instanceof CustomNamePrintingBehaviour) advancement.trigger(CEIAdvancements.BRAND_REGISTRY.builtinTrigger());
        else if (resultItem.is(Items.WRITTEN_BOOK)) advancement.trigger(CEIAdvancements.COPIABLE_MASTERPIECE.builtinTrigger());
        else if (resultItem.is(Items.ENCHANTED_BOOK)) advancement.trigger(CEIAdvancements.COPIABLE_MYSTERY.builtinTrigger());
        else if (printing instanceof PackagePatternPrintingBehaviour) advancement.trigger(CEIAdvancements.ASSEMBLY_AESTHETICS.builtinTrigger());
        else if (printing instanceof AddressPrintingBehaviour) advancement.trigger(CEIAdvancements.SUPPLY_CHAIN_REFACTOR.builtinTrigger());
    }

    private void finishProcessing() {
        processingTicks = -1;
        activePrinting = null;
    }

    private void cancelProcessing() {
        if (processingTicks != -1 || activePrinting != null) {
            finishProcessing();
            notifyUpdate();
        }
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        output.putInt("ProcessingTicks", processingTicks);
        if (activePrinting != null)
            activePrinting.write(output.child("ActivePrinting"));
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        processingTicks = input.getIntOr("ProcessingTicks", -1);
        activePrinting = input.child("ActivePrinting").map(ActivePrinting::load).orElse(null);
        if (processingTicks >= 0 && activePrinting == null)
            processingTicks = -1;
    }

    @Override
    public void tick() {
        super.tick();
        if (processingTicks > COMPLETION_TICKS) {
            processingTicks--;
        }
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -2, 0);
    }

    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        assert level != null;
        return printer.getPrintingBehaviour().addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    public SmartFluidTankBehaviour getTank() {
        return tank;
    }

    private record ActivePrinting(
            ItemStack input,
            ItemStack template,
            FluidStack fluid,
            ItemStack result,
            int requiredItemCount,
            int requiredFluidAmount) {
        void write(ValueOutput output) {
            output.store("Input", ItemStack.CODEC, input);
            output.store("Template", ItemStack.CODEC, template);
            output.store("Fluid", FluidStack.CODEC, fluid);
            output.store("Result", ItemStack.CODEC, result);
            output.putInt("RequiredItemCount", requiredItemCount);
            output.putInt("RequiredFluidAmount", requiredFluidAmount);
        }

        static @Nullable ActivePrinting load(ValueInput inputView) {
            ItemStack input = inputView.read("Input", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            ItemStack template = inputView.read("Template", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            FluidStack fluid = inputView.read("Fluid", FluidStack.CODEC).orElse(FluidStack.EMPTY);
            ItemStack result = inputView.read("Result", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            int requiredItemCount = inputView.getIntOr("RequiredItemCount", 0);
            int requiredFluidAmount = inputView.getIntOr("RequiredFluidAmount", 0);
            if (input.isEmpty()
                    || template.isEmpty()
                    || fluid.isEmpty()
                    || result.isEmpty()
                    || requiredItemCount <= 0
                    || requiredFluidAmount <= 0
                    || input.getCount() != requiredItemCount
                    || fluid.getAmount() != requiredFluidAmount)
                return null;
            return new ActivePrinting(
                    input,
                    template,
                    fluid,
                    result,
                    requiredItemCount,
                    requiredFluidAmount);
        }

        boolean matchesInput(ItemStack stack) {
            return stack.getCount() >= requiredItemCount && ItemStack.isSameItemSameComponents(input, stack);
        }

        boolean matchesFluid(FluidStack stack) {
            return FluidStack.areFluidsAndComponentsEqualIgnoreCapacity(fluid, stack);
        }
    }
}
