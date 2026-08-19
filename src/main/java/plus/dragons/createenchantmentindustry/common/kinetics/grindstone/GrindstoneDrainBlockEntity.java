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

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.processing.recipe.ProcessingInventory;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.transfer.FluidInventoryStorage;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.advancement.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;
import plus.dragons.createenchantmentindustry.common.registry.CEIStats;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEIFluidUnits;

public class GrindstoneDrainBlockEntity extends KineticBlockEntity implements Clearable {
    public static final int GRINDING_TIME = 20;
    public ProcessingInventory inventory;
    private ItemStack processedItem = ItemStack.EMPTY;
    protected SmartFluidTankBehaviour tank;
    private DirectBeltInputBehaviour beltInput;
    private AdvancementBehaviour advancement;

    public GrindstoneDrainBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new ProcessingInventory(this::start, side -> true) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return super.canPlaceItem(slot, stack) && canAcceptInput(stack);
            }

            @Override
            public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
                return super.canPlaceItemThroughFace(slot, stack, side) && canAcceptInput(stack);
            }

            private boolean canAcceptInput(ItemStack stack) {
                if (stack.isEmpty() || tank == null)
                    return false;
                var segment = tank.getPrimaryHandler();
                var space = segment.getMaxAmountPerStack() - segment.getFluid().getAmount();
                long a = CEIFluidUnits.millibuckets(GrindstoneHelper.getExperienceFromItem(stack));
                long b = CEIFluidUnits.millibuckets(GrindstoneHelper.getExperienceFromGrindingRecipe(level, stack));
                return a <= space && b <= space;
            }
        }.withSlotLimit(true);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        tank = SmartFluidTankBehaviour.single(
                this,
                Math.toIntExact(
                        CEIFluidUnits.millibuckets(CEIConfig.fluids().mechanicalGrindstoneFluidCapacity.get())));
        beltInput = new DirectBeltInputBehaviour(this).allowingBeltFunnels();
        advancement = new AdvancementBehaviour(this);
        behaviours.add(tank);
        behaviours.add(beltInput);
        behaviours.add(advancement);
    }

    public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
        return side != Direction.DOWN ? ContainerStorage.of(inventory, side) : null;
    }

    public @Nullable Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
        return tank != null
                && (side == null
                        || side == getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getOpposite())
                                ? FluidInventoryStorage.of(tank.getCapability(), side)
                                : null;
    }

    private Direction getOutputSide() {
        var facing = getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
        var speed = facing == Direction.WEST || facing == Direction.NORTH ? getSpeed() * -1 : getSpeed();
        return speed > 0 ? facing.getClockWise() : facing.getCounterClockWise();
    }

    public float getRelativeSpeed() {
        assert level != null;
        float speed = getSpeed();
        if (speed == 0f)
            return 0f;
        var above = worldPosition.above();
        var aboveState = level.getBlockState(above);
        if (!(aboveState.getBlock() instanceof MechanicalGrindstoneBlock grinderWheel))
            return 0f;
        var facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (grinderWheel.getRotationAxis(aboveState) != facing.getAxis())
            return 0f;
        float aboveSpeed = grinderWheel.getBlockEntityOptional(level, above)
                .map(KineticBlockEntity::getSpeed).orElse(0f);
        if (speed > 0f) {
            return aboveSpeed < 0f ? Math.min(speed, -aboveSpeed) : 0f;
        } else {
            return aboveSpeed > 0f ? Math.min(-speed, aboveSpeed) : 0f;
        }
    }

    private int getProcessDuration(ItemStack inputStack) {
        assert level != null;
        if (!(level instanceof ServerLevel serverLevel))
            return 50;
        var recipeManager = serverLevel.recipeAccess();
        var input = new SingleRecipeInput(inputStack);
        var sizeModifier = Math.max(1, (inputStack.getCount() / 5));
        RecipeType<GrindingRecipe> grindingType = CEIRecipes.GRINDING.getType();
        var grinding = recipeManager.getRecipeFor(grindingType, input, level);
        if (grinding.isPresent()) {
            return grinding.get().value().getProcessingDuration() * sizeModifier;
        }
        if (recipeManager
                .getRecipeFor(
                        AllRecipeTypes.SANDPAPER_POLISHING,
                        new SingleRecipeInput(inputStack),
                        level)
                .isPresent()) {
            return 50 * sizeModifier;
        }
        if (GrindstoneHelper.canItemBeGrinded(inputStack, ItemStack.EMPTY)) {
            return 50 * sizeModifier;
        }
        return 10;
    }

    private boolean fill(FluidStack fluid) {
        return tank.getPrimaryHandler().preciseInsert(fluid);
    }

    private boolean drain(FluidIngredient fluidIngredient) {
        FluidStack fluid = tank.getPrimaryHandler().getFluid();
        int required = fluidIngredient.amount();
        if (fluidIngredient.test(fluid) && fluid.getAmount() >= required) {
            fluid.decrement(required);
            tank.getPrimaryHandler().setFluid(fluid);
            return true;
        }
        return false;
    }

    private boolean applyGrindingFluidOperation(GrindingRecipe recipe) {
        var fluidIngredients = recipe.getFluidIngredients();
        if (!fluidIngredients.isEmpty())
            return drain(fluidIngredients.get(0));

        var fluidResults = recipe.getFluidResults();
        if (!fluidResults.isEmpty())
            return fill(fluidResults.get(0));

        return true;
    }

    private void awardGrindingFluidStats(GrindingRecipe recipe) {
        var fluidResults = recipe.getFluidResults();
        if (fluidResults.isEmpty())
            return;

        var fluidResult = fluidResults.get(0);
        if (fluidResult.getFluid() == CEIFluids.EXPERIENCE.getSource())
            advancement.awardStat(
                    CEIStats.GRINDSTONE_EXPERIENCE.get(),
                    Math.toIntExact(CEIFluidUnits.toMillibuckets(fluidResult.getAmount())));
    }

    private void start(ItemStack inputStack) {
        assert level != null;
        if (inventory.isEmpty())
            return;
        if (level.isClientSide() && !isVirtual())
            return;
        inventory.remainingTime = inventory.recipeDuration = getProcessDuration(inputStack);
        inventory.appliedRecipe = false;
        sendData();
    }

    private void applyRecipe() {
        assert level != null;
        if (!(level instanceof ServerLevel serverLevel))
            return;
        var recipeManager = serverLevel.recipeAccess();
        var inputStack = inventory.getItem(0);
        var input = new SingleRecipeInput(inputStack);
        // Grinding
        RecipeType<GrindingRecipe> grindingType = CEIRecipes.GRINDING.getType();
        var grinding = recipeManager.getRecipeFor(grindingType, input, level);
        if (grinding.isPresent()) {
            var recipe = grinding.get().value();
            if (applyGrindingFluidOperation(recipe)) {
                awardGrindingFluidStats(recipe);
                inventory.clearContent();
                var grinded = recipe.rollResults();
                for (int i = 0; i < grinded.size(); i++)
                    inventory.setItem(i + 1, grinded.get(i));
                return;
            }
        }
        // Sand Paper Polishing
        Optional<RecipeHolder<SandPaperPolishingRecipe>> polishing = recipeManager
                .getRecipeFor(
                        AllRecipeTypes.SANDPAPER_POLISHING,
                        new SingleRecipeInput(inputStack),
                        level);
        if (polishing.isPresent() && AllRecipeTypes.CAN_BE_AUTOMATED.test(polishing.get())) {
            var polished = polishing.get().value().result().create();
            advancement.trigger(CEIAdvancements.GRIND_TO_POLISH.builtinTrigger());
            inventory.clearContent();
            inventory.setItem(1, polished);
            return;
        }
        // Grind Stone
        var grindstone = GrindstoneHelper.grindItem(level, inputStack, ItemStack.EMPTY);
        if (grindstone.isPresent()) {
            var result = grindstone.get();
            var fluid = CEIFluidUnits.stack(CEIFluids.EXPERIENCE.getSource(), result.experience());
            if (fill(fluid)) {
                advancement.trigger(CEIAdvancements.GONE_WITH_THE_FOIL.builtinTrigger());
                advancement.awardStat(CEIStats.GRINDSTONE_EXPERIENCE.get(), result.experience());
                inventory.clearContent();
                inventory.setItem(0, result.top());
                inventory.setItem(1, result.bottom());
                inventory.setItem(2, result.output());
            }
        }
    }

    private void spawnProcessedParticles(ItemStack stack) {
        assert level != null;
        if (stack.isEmpty())
            return;

        ParticleOptions particleData;
        if (stack.getItem() instanceof BlockItem blockItem)
            particleData = new BlockParticleOption(ParticleTypes.BLOCK, blockItem.getBlock().defaultBlockState());
        else
            particleData = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack));

        Vec3 pos = Vec3.atBottomCenterOf(this.worldPosition).add(0, 1, 0);
        for (int i = 0; i < 10; i++) {
            Vec3 motion = VecHelper.offsetRandomly(new Vec3(0, 0.25f, 0), level.getRandom(), .125f);
            level.addParticle(particleData, pos.x, pos.y, pos.z, motion.x, motion.y, motion.y);
        }
    }

    private void spawnProcessingParticles(ItemStack stack) {
        assert level != null;
        if (stack.isEmpty())
            return;

        float speed;
        ParticleOptions particleData;
        if (stack.getItem() instanceof BlockItem blockItem) {
            particleData = new BlockParticleOption(ParticleTypes.BLOCK, blockItem.getBlock().defaultBlockState());
            speed = 1f;
        } else {
            particleData = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack));
            speed = .125f;
        }

        Vec3 pos = Vec3.atBottomCenterOf(worldPosition).add(0, 1, 0);
        Direction inputSide = getOutputSide().getOpposite();
        float offset = inventory.recipeDuration != 0 ? inventory.remainingTime / inventory.recipeDuration : 0;
        offset /= 2;
        if (inventory.appliedRecipe)
            offset -= .5f;
        level.addParticle(particleData,
                pos.x + inputSide.getStepX() * offset,
                pos.y,
                pos.z + inputSide.getStepZ() * offset,
                inputSide.getStepX() * speed,
                level.getRandom().nextFloat() * speed,
                inputSide.getStepZ() * speed);
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        inventory.write(output.child("Inventory"));
        if (clientPacket && !processedItem.isEmpty()) {
            output.store("ProcessedItem", ItemStack.CODEC, processedItem);
            processedItem = ItemStack.EMPTY;
        }
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        inventory.read(input.childOrEmpty("Inventory"));
        processedItem = input.read("ProcessedItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    public void destroy() {
        super.destroy();
        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), processedItem);
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
            Containers.dropItemStack(
                    level,
                    worldPosition.getX(),
                    worldPosition.getY(),
                    worldPosition.getZ(),
                    inventory.removeItemNoUpdate(slot));
    }

    @Override
    public void tick() {
        assert level != null;
        super.tick();

        if (level.isClientSide() && getSpeed() != 0 && !processedItem.isEmpty()) {
            spawnProcessedParticles(processedItem);
            processedItem = ItemStack.EMPTY;
            level.levelEvent(1042, worldPosition, 0);
        }

        float processingSpeed = getRelativeSpeed();
        if (processingSpeed == 0)
            return;
        if (inventory.remainingTime == -1) {
            if (!inventory.isEmpty() && !inventory.appliedRecipe)
                start(inventory.getItem(0));
            return;
        }

        processingSpeed = Mth.clamp(processingSpeed / 24, 1, 128);
        inventory.remainingTime -= processingSpeed;

        if (inventory.remainingTime > 0)
            spawnProcessingParticles(inventory.getItem(0));

        if (inventory.remainingTime < 5 && !inventory.appliedRecipe) {
            if (level.isClientSide() && !isVirtual())
                return;
            processedItem = inventory.getItem(0);
            applyRecipe();
            inventory.appliedRecipe = true;
            inventory.recipeDuration = 20;
            inventory.remainingTime = 20;
            sendData();
            return;
        }

        Direction outputSide = getOutputSide();
        if (inventory.remainingTime > 0)
            return;
        inventory.remainingTime = 0;

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty())
                continue;
            ItemStack toFunnel = beltInput.tryExportingToBeltFunnel(stack, outputSide.getOpposite(), false);
            if (toFunnel != null) {
                if (toFunnel.getCount() != stack.getCount()) {
                    inventory.setItem(slot, toFunnel);
                    notifyUpdate();
                    return;
                }
                if (!toFunnel.isEmpty())
                    return;
            }
        }

        BlockPos outputPos = worldPosition.relative(outputSide);
        DirectBeltInputBehaviour outputTarget = BlockEntityBehaviour.get(level, outputPos, DirectBeltInputBehaviour.TYPE);
        if (outputTarget != null) {
            boolean changed = false;
            if (!outputTarget.canInsertFromSide(outputSide))
                return;
            if (level.isClientSide() && !isVirtual())
                return;
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty())
                    continue;
                ItemStack remainder = outputTarget.handleInsertion(stack, outputSide, false);
                if (ItemStack.matches(remainder, stack))
                    continue;
                inventory.setItem(slot, remainder);
                changed = true;
            }
            if (changed) {
                setChanged();
                sendData();
            }
            return;
        }

        Vec3 itemMovement = Vec3.atLowerCornerOf(outputSide.getUnitVec3i());
        Vec3 outPos = VecHelper.getCenterOf(worldPosition).add(itemMovement.scale(.5f).add(0, .5, 0));
        Vec3 outMotion = itemMovement.scale(.0625).add(0, .125, 0);
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty())
                continue;
            ItemEntity entityIn = new ItemEntity(level, outPos.x, outPos.y, outPos.z, stack);
            entityIn.setDeltaMovement(outMotion);
            level.addFreshEntity(entityIn);
        }
        inventory.clearContent();
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        inventory.remainingTime = -1;
        sendData();
    }

    public SmartFluidTankBehaviour getTank() {
        return tank;
    }

    @Override
    public void clearContent() {
        inventory.clearContent();
        processedItem = ItemStack.EMPTY;
    }

    public ItemStack takeAllItems() {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.removeItemNoUpdate(slot);
            if (!stack.isEmpty()) {
                notifyUpdate();
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
