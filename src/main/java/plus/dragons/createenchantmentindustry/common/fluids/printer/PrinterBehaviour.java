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

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.logistics.filter.FilterItem;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.RecipePrintingBehaviour;

public class PrinterBehaviour extends ServerFilteringBehaviour {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final BehaviourType<PrinterBehaviour> TYPE = (BehaviourType) ServerFilteringBehaviour.TYPE;
    public static final String TEMPLATE = "PrintingTemplate";
    private final SmartFluidTankBehaviour tank;
    private PrintingBehaviour printing = new RecipePrintingBehaviour(ItemStack.EMPTY);

    public PrinterBehaviour(SmartBlockEntity be, SmartFluidTankBehaviour tank) {
        super(be);
        this.tank = tank;
    }

    public PrintingBehaviour getPrintingBehaviour() {
        return printing;
    }

    public boolean setFilter(ItemStack stack, @Nullable Player player) {
        var result = PrintingBehaviour.create(blockEntity.getLevel(), tank, stack)
                .resultOrPartial(message -> {
                    if (player != null)
                        player.sendOverlayMessage(Component.translatable(message));
                });
        if (result.isPresent() && super.setFilter(stack)) {
            printing = result.get();
            return true;
        }
        return false;
    }

    @Override
    public boolean setFilter(ItemStack stack) {
        return setFilter(stack, null);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @Override
    public void write(ValueOutput output, boolean clientPacket) {
        output.store(TEMPLATE, ItemStack.OPTIONAL_CODEC, getFilter());
    }

    @Override
    public void writeSafe(ValueOutput output) {
        if (printing.isSafeNBT())
            output.store(TEMPLATE, ItemStack.OPTIONAL_CODEC, getFilter());
    }

    @Override
    public void read(ValueInput input, boolean clientPacket) {
        var filter = FilterItemStack.of(input.read(TEMPLATE, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        var printing = PrintingBehaviour.create(blockEntity.getLevel(), tank, filter.item()).result();
        if (printing.isPresent()) {
            this.filter = filter;
            this.printing = printing.get();
        } else {
            this.filter = FilterItemStack.empty();
            this.printing = RecipePrintingBehaviour.EMPTY;
        }
    }

    @Override
    public String getClipboardKey() {
        return "Printer";
    }

    @Override
    public boolean writeToClipboard(ValueOutput output, Direction side) {
        output.store(TEMPLATE, ItemStack.OPTIONAL_CODEC, getFilter());
        return true;
    }

    @Override
    public boolean readFromClipboard(ValueInput input, Player player, Direction side, boolean simulate) {
        var template = input.read(TEMPLATE, ItemStack.OPTIONAL_CODEC);
        if (template.isEmpty())
            return false;
        if (simulate)
            return true;
        return setFilter(template.get(), player);
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        Level level = blockEntity.getLevel();
        BlockPos pos = getPos();
        ItemStack itemInHand = player.getItemInHand(hand);
        ItemStack toApply = itemInHand.copy();

        if (!canShortInteract(toApply))
            return;
        if (level.isClientSide())
            return;

        if (getFilter().getItem() instanceof FilterItem) {
            if (!player.isCreative() || !player.getInventory().contains(getFilter(side)))
                player.getInventory().placeItemBackInInventory(getFilter(side).copy());
        }

        if (toApply.getItem() instanceof FilterItem)
            toApply.setCount(1);

        if (!setFilter(toApply, player)) {
            AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
            return;
        }

        if (!player.isCreative()) {
            if (toApply.getItem() instanceof FilterItem) {
                if (itemInHand.getCount() == 1)
                    player.setItemInHand(hand, ItemStack.EMPTY);
                else
                    itemInHand.shrink(1);
            }
        }

        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .25f, .1f);
    }
}
