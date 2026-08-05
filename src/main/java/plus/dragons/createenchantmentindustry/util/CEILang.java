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

package plus.dragons.createenchantmentindustry.util;

import com.zurrtum.create.infrastructure.fluids.FluidStack;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Server-safe language builder used by common gameplay and client tooltips alike. */
public final class CEILang {
    private CEILang() {}

    public static Builder builder() {
        return new Builder(Component.empty());
    }

    public static Builder number(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
        format.setMaximumFractionDigits(3);
        return text(format.format(value));
    }

    public static Builder text(String text) {
        return new Builder(Component.literal(text));
    }

    public static Builder translate(String key, Object... args) {
        return translateKey("create_enchantment_industry." + key, args);
    }

    public static Builder translateCreate(String key, Object... args) {
        return translateKey("create." + key, args);
    }

    private static Builder translateKey(String key, Object... args) {
        Object[] converted = new Object[args.length];
        for (int i = 0; i < args.length; i++)
            converted[i] = args[i] instanceof Builder builder ? builder.component() : args[i];
        return new Builder(Component.translatable(key, converted));
    }

    public static Builder description(String category, Identifier location, Object... args) {
        return new Builder(Component.translatable(Util.makeDescriptionId(category, location), args));
    }

    public static Builder description(String category, Identifier location, String suffix, Object... args) {
        return new Builder(Component.translatable(Util.makeDescriptionId(category, location) + "." + suffix, args));
    }

    public static Builder description(Holder<?> holder, Object... args) {
        var key = holder.unwrapKey().orElseThrow(
                () -> new IllegalArgumentException("Cannot build description for unregistered object: " + holder));
        return description(key.registry().getPath(), key.identifier(), args);
    }

    public static Builder description(Holder<?> holder, String suffix, Object... args) {
        var key = holder.unwrapKey().orElseThrow(
                () -> new IllegalArgumentException("Cannot build description for unregistered object: " + holder));
        return description(key.registry().getPath(), key.identifier(), suffix, args);
    }

    public static Builder block(BlockState state) {
        return new Builder(state.getBlock().getName().copy());
    }

    public static Builder item(ItemStack stack) {
        return new Builder(stack.getHoverName().copy());
    }

    public static Builder fluid(FluidStack stack) {
        return new Builder(stack.getName().copy());
    }

    public static final class Builder {
        private final MutableComponent component;

        private Builder(MutableComponent component) {
            this.component = component;
        }

        public Builder add(Builder other) {
            component.append(other.component());
            return this;
        }

        public Builder add(Component other) {
            component.append(other);
            return this;
        }

        public Builder add(String text) {
            component.append(text);
            return this;
        }

        public Builder style(ChatFormatting... formatting) {
            component.withStyle(formatting);
            return this;
        }

        public MutableComponent component() {
            return component;
        }

        public String getString() {
            return component.getString();
        }

        public void forGoggles(List<Component> tooltip) {
            forGoggles(tooltip, 0);
        }

        public void forGoggles(List<Component> tooltip, int indent) {
            MutableComponent line = Component.empty();
            if (indent > 0)
                line.append(Component.literal("  ".repeat(indent)));
            line.append(component);
            tooltip.add(line);
        }
    }
}
