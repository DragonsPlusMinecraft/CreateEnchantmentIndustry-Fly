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

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.fluids.pipes.FluidPipeBlock;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.block.IBE;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.advancement.AdvancementBehaviour;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;

public class GrindstoneDrainBlock extends HorizontalKineticBlock implements IBE<GrindstoneDrainBlockEntity>, SpecialBlockItemRequirement {
    protected static VoxelShape SHAPE = new AllShapes.Builder(AllShapes.CASING_13PX.get(Direction.UP))
            .add(3, 3, 3, 13, 13, 13)
            .build();
    final MechanicalGrindstoneBlock grindstone;

    public GrindstoneDrainBlock(MechanicalGrindstoneBlock grindstone, Properties properties) {
        super(properties);
        this.grindstone = grindstone;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (hitResult.getDirection() == Direction.UP)
            return this.grindstone.interact(
                    state, level, pos, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, hitResult);
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        if (hitResult.getDirection() == Direction.UP)
            return this.grindstone.interact(state, level, pos, player, hand, stack, hitResult);
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter worldIn, Entity entityIn) {
        super.updateEntityMovementAfterFallOn(worldIn, entityIn);

        if (entityIn.level().isClientSide())
            return;
        if (!(entityIn instanceof ItemEntity itemEntity))
            return;
        if (!entityIn.isAlive())
            return;
        GrindstoneDrainBlockEntity drain = getBlockEntity(worldIn, entityIn.blockPosition());
        if (drain == null)
            return;

        Storage<ItemVariant> capability = drain.getItemStorage(null);
        if (capability == null)
            return;

        ItemStack original = itemEntity.getItem();
        long inserted;
        try (Transaction transaction = Transaction.openOuter()) {
            inserted = capability.insert(ItemVariant.of(original), original.getCount(), transaction);
            if (inserted == 0)
                return;
            transaction.commit();
        }
        ItemStack remainder = original.copy();
        remainder.shrink(Math.toIntExact(inserted));
        if (remainder.isEmpty()) itemEntity.discard();
        else itemEntity.setItem(remainder);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(HORIZONTAL_FACING) == face;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        AdvancementBehaviour.setPlacedBy(worldIn, pos, placer);
    }

    @Override
    public @Nullable Direction getPreferredHorizontalFacing(BlockPlaceContext context) {
        Direction prefferedSide = super.getPreferredHorizontalFacing(context);
        if (prefferedSide != null)
            return prefferedSide;

        for (Direction facing : Iterate.horizontalDirections) {
            BlockPos pos = context.getClickedPos().relative(facing);
            BlockState blockState = context.getLevel().getBlockState(pos);
            if (FluidPipeBlock.canConnectTo(context.getLevel(), pos, blockState, facing))
                if (prefferedSide != null && prefferedSide.getAxis() != facing.getAxis()) {
                    prefferedSide = null;
                    break;
                } else
                    prefferedSide = facing;
        }
        return prefferedSide == null ? null : prefferedSide.getOpposite();
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(AllBlocks.ITEM_DRAIN);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<GrindstoneDrainBlockEntity> getBlockEntityClass() {
        return GrindstoneDrainBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GrindstoneDrainBlockEntity> getBlockEntityType() {
        return CEIBlockEntities.GRINDSTONE_DRAIN.get();
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, @Nullable BlockEntity blockEntity) {
        return new ItemRequirement(List.of(
                new ItemRequirement.StackRequirement(new ItemStack(grindstone), ItemRequirement.ItemUseType.CONSUME),
                new ItemRequirement.StackRequirement(
                        new ItemStack(AllBlocks.ITEM_DRAIN), ItemRequirement.ItemUseType.CONSUME)));
    }
}
