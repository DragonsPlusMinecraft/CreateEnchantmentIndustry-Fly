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

import com.zurrtum.create.AllBlocks;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

/** Core-only block drops. */
public final class CEIBlockLootProvider extends FabricBlockLootSubProvider {
    public CEIBlockLootProvider(
            FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generate() {
        dropSelf(CEIBlocks.MECHANICAL_GRINDSTONE.get());
        dropSelf(CEIBlocks.EXPERIENCE_HATCH.get());
        dropSelf(CEIBlocks.PRINTER.get());
        dropSelf(CEIBlocks.BLAZE_ENCHANTER.get());
        dropSelf(CEIBlocks.BLAZE_FORGER.get());
        dropSelf(CEIBlocks.CLASSIC_BLAZE_ENCHANTER.get());
        dropSelf(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get());
        dropSelf(CEIBlocks.EXPERIENCE_LANTERN.get());
        excludeFromStrictValidation(CEIBlocks.EXPERIENCE.get());

        add(
                CEIBlocks.GRINDSTONE_DRAIN.get(),
                LootTable.lootTable()
                        .withPool(applyExplosionCondition(
                                CEIBlocks.MECHANICAL_GRINDSTONE.get(),
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(LootItem.lootTableItem(
                                                CEIBlocks.MECHANICAL_GRINDSTONE.get()))))
                        .withPool(applyExplosionCondition(
                                AllBlocks.ITEM_DRAIN,
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(LootItem.lootTableItem(AllBlocks.ITEM_DRAIN)))));
    }
}
