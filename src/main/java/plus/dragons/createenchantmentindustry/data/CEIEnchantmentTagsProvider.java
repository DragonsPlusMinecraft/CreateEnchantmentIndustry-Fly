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

package plus.dragons.createenchantmentindustry.data;

import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import plus.dragons.createenchantmentindustry.common.registry.CEIEnchantments;

/** Generates CEI's semantic enchantment tags from the 1.21.11 vanilla tags. */
public final class CEIEnchantmentTagsProvider extends FabricTagProvider<Enchantment> {
    public CEIEnchantmentTagsProvider(
            FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENCHANTMENT, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        builder(CEIEnchantments.MOD_TAGS.enchanting)
                .forceAddTag(EnchantmentTags.IN_ENCHANTING_TABLE);
        builder(CEIEnchantments.MOD_TAGS.enchantingExclusive);

        // Fabric tags do not support the legacy loader's subtractive `remove` entries. Keep the
        // vanilla treasure-minus-curse set explicit; the runtime path separately filters
        // enchantingExclusive for addon-provided normal-only enchantments.
        builder(CEIEnchantments.MOD_TAGS.superEnchantingExclusive)
                .add(
                        Enchantments.FROST_WALKER,
                        Enchantments.MENDING,
                        Enchantments.SOUL_SPEED,
                        Enchantments.SWIFT_SNEAK,
                        Enchantments.WIND_BURST);

        builder(CEIEnchantments.MOD_TAGS.penaltyCurses)
                .forceAddTag(EnchantmentTags.IN_ENCHANTING_TABLE)
                .forceAddTag(EnchantmentTags.TRADEABLE)
                .forceAddTag(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)
                .forceAddTag(EnchantmentTags.ON_TRADED_EQUIPMENT)
                .forceAddTag(EnchantmentTags.ON_RANDOM_LOOT);
        builder(CEIEnchantments.MOD_TAGS.penaltyCursesDeny);
        builder(CEIEnchantments.MOD_TAGS.printingDeny);
        builder(CEIEnchantments.MOD_TAGS.superEnchanting)
                .addTag(CEIEnchantments.MOD_TAGS.enchanting)
                .addTag(CEIEnchantments.MOD_TAGS.superEnchantingExclusive);
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Enchantment Tags";
    }
}
