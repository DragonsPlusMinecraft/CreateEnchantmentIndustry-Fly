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

package plus.dragons.createenchantmentindustry.common.registry;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small stable handle used by CEI registries after removing Registrate.
 *
 * <p>The convenience methods intentionally cover only the operations used by CEI. Registry construction and
 * datagen remain explicit, rather than rebuilding a general-purpose registration DSL.</p>
 */
public final class CEIRegistryEntry<T> implements Supplier<T> {
    private final Identifier id;
    private final T value;

    public CEIRegistryEntry(Identifier id, T value) {
        this.id = Objects.requireNonNull(id, "id");
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public T get() {
        return value;
    }

    public Identifier getId() {
        return id;
    }

    public boolean is(T candidate) {
        return value == candidate;
    }

    public boolean has(BlockState state) {
        return state.is(asBlock());
    }

    public BlockState getDefaultState() {
        return asBlock().defaultBlockState();
    }

    public Item asItem() {
        if (value instanceof Item item) {
            return item;
        }
        if (value instanceof Block block) {
            return block.asItem();
        }
        throw new IllegalStateException(id + " is neither an item nor a block");
    }

    public ItemStack asStack() {
        return new ItemStack(asItem());
    }

    private Block asBlock() {
        if (value instanceof Block block) {
            return block;
        }
        throw new IllegalStateException(id + " is not a block");
    }
}
