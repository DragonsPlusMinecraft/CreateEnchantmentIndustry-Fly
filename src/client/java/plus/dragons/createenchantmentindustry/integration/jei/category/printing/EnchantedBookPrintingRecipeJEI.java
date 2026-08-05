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

package plus.dragons.createenchantmentindustry.integration.jei.category.printing;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.item.CEIItemData;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.CEIEnchantmentHelper;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.common.registry.CEIEnchantments;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.util.CEIFluidUnits;
import plus.dragons.createenchantmentindustry.util.CEIIntIntPair;

/** One JEI display per printable enchantment level. */
public final class EnchantedBookPrintingRecipeJEI implements PrintingRecipeJEI {
    public static final Type TYPE = PrintingRecipeJEI.register(CEICommon.asResource("enchanted_book"));

    private final Identifier id;
    private final Holder<Enchantment> enchantment;
    private final int level;

    private EnchantedBookPrintingRecipeJEI(
            Identifier enchantmentId, Holder<Enchantment> enchantment, int level) {
        this.id = PrintingRecipeJEI.super.getRegistryName()
                .withSuffix("/" + enchantmentId.getNamespace() + "/" + enchantmentId.getPath() + "/" + level);
        this.enchantment = enchantment;
        this.level = level;
    }

    public static List<PrintingRecipeJEI> listAll() {
        var level = Minecraft.getInstance().level;
        if (level == null)
            return List.of();
        var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return registry.listElements()
                .filter(holder -> !holder.is(CEIEnchantments.MOD_TAGS.printingDeny))
                .flatMap(holder -> IntStream
                        .rangeClosed(holder.value().getMinLevel(), CEIEnchantmentHelper.maxLevel(holder))
                        .mapToObj(enchantmentLevel -> (PrintingRecipeJEI) new EnchantedBookPrintingRecipeJEI(
                                holder.key().identifier(), holder, enchantmentLevel)))
                .toList();
    }

    private ItemStack createEnchantmentBook() {
        ItemStack book = Items.ENCHANTED_BOOK.getDefaultInstance();
        CEIItemData.setStoredEnchantments(book, Map.of(enchantment, level));
        return book;
    }

    private OptionalInt getCost() {
        CEIIntIntPair custom = null;
        var customCosts = CEIDataMaps.PRINTING_ENCHANTED_BOOK_COST.get(enchantment.value());
        if (customCosts != null)
            custom = customCosts.stream().filter(pair -> pair.level() == level).findFirst().orElse(null);
        int baseCost = custom == null
                ? CEIEnchantmentHelper.getEnchantmentCost(enchantment, level)
                : custom.value();
        return OptionalInt.of((int) (baseCost * CEIConfig.fluids().printingEnchantedBookCostMultiplier.get()));
    }

    @Override
    public void setBase(IRecipeSlotBuilder slot) {
        slot.add(Items.BOOK);
    }

    @Override
    public void setTemplate(IRecipeSlotBuilder slot) {
        slot.add(createEnchantmentBook());
    }

    @Override
    public void setFluid(IRecipeSlotBuilder slot) {
        getCost().ifPresent(cost -> {
            slot.add(CEIFluids.EXPERIENCE.getSource(), CEIFluidUnits.millibuckets(cost));
            CEIDataMaps.getSourceFluidEntries(CEIDataMaps.FLUID_UNIT_EXPERIENCE)
                    .forEach(pair -> slot.add(
                            pair.getFirst(),
                            CEIFluidUnits.millibuckets(
                                    Math.multiplyExact((long) pair.getSecond(), cost))));
        });
    }

    @Override
    public void setOutput(IRecipeSlotBuilder slot) {
        slot.add(createEnchantmentBook());
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public Identifier getRegistryName() {
        return id;
    }
}
