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

package plus.dragons.createenchantmentindustry.common.item;

import com.zurrtum.create.content.logistics.box.PackageItem;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

/** Central bridge for the vanilla and Create data components used by core CEI content. */
public final class CEIItemData {
    private CEIItemData() {}

    public static Map<Holder<Enchantment>, Integer> getEnchantments(ItemStack stack) {
        return toMap(EnchantmentHelper.getEnchantmentsForCrafting(stack));
    }

    public static void setEnchantments(ItemStack stack, Map<Holder<Enchantment>, Integer> enchantments) {
        EnchantmentHelper.setEnchantments(stack, toComponent(enchantments));
    }

    public static Map<Holder<Enchantment>, Integer> getStoredEnchantments(ItemStack stack) {
        return toMap(stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY));
    }

    public static void setStoredEnchantments(ItemStack stack, Map<Holder<Enchantment>, Integer> enchantments) {
        ItemEnchantments component = toComponent(enchantments);
        if (component.isEmpty())
            stack.remove(DataComponents.STORED_ENCHANTMENTS);
        else
            stack.set(DataComponents.STORED_ENCHANTMENTS, component);
    }

    /** Returns stored enchantments for templates/books, otherwise the stack's normal enchantments. */
    public static Map<Holder<Enchantment>, Integer> getEnchantmentsForCrafting(ItemStack stack) {
        Map<Holder<Enchantment>, Integer> stored = getStoredEnchantments(stack);
        return stored.isEmpty() ? getEnchantments(stack) : stored;
    }

    public static int getRepairCost(ItemStack stack) {
        return stack.getOrDefault(DataComponents.REPAIR_COST, 0);
    }

    public static void setRepairCost(ItemStack stack, int cost) {
        if (cost <= 0)
            stack.remove(DataComponents.REPAIR_COST);
        else
            stack.set(DataComponents.REPAIR_COST, cost);
    }

    public static @Nullable Component getCustomName(ItemStack stack) {
        return stack.get(DataComponents.CUSTOM_NAME);
    }

    public static void setCustomName(ItemStack stack, @Nullable Component name) {
        if (name == null)
            stack.remove(DataComponents.CUSTOM_NAME);
        else
            stack.set(DataComponents.CUSTOM_NAME, name);
    }

    public static String getPackageAddress(ItemStack stack) {
        return PackageItem.getAddress(stack);
    }

    public static void setPackageAddress(ItemStack stack, String address) {
        if (address.isEmpty())
            PackageItem.clearAddress(stack);
        else
            PackageItem.addAddress(stack, address);
    }

    /** Creates a stack of another item while preserving the source data-component patch. */
    public static ItemStack transmuteCopy(ItemStack source, Item target) {
        return source.transmuteCopy(target, source.getCount());
    }

    private static Map<Holder<Enchantment>, Integer> toMap(ItemEnchantments enchantments) {
        Map<Holder<Enchantment>, Integer> result = new LinkedHashMap<>();
        enchantments.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getIntValue()));
        return Collections.unmodifiableMap(result);
    }

    private static ItemEnchantments toComponent(Map<Holder<Enchantment>, Integer> enchantments) {
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        enchantments.forEach((enchantment, level) -> {
            if (level > 0)
                mutable.set(enchantment, level);
        });
        return mutable.toImmutable();
    }
}
