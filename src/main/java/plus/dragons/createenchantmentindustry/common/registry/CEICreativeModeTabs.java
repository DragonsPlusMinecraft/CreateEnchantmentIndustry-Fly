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

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import plus.dragons.createdragonsplus.registry.CDPItems;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public final class CEICreativeModeTabs {
    public static final ResourceKey<CreativeModeTab> BASE_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, CEICommon.asResource("base"));
    public static CreativeModeTab BASE;

    private CEICreativeModeTabs() {}

    public static synchronized void register() {
        if (BASE != null) {
            return;
        }
        BASE = Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                BASE_KEY,
                CreativeModeTab.builder(null, -1)
                        .title(Component.translatable("itemGroup.create_enchantment_industry.base"))
                        .icon(CEIBlocks.BLAZE_ENCHANTER::asStack)
                        .displayItems(CEICreativeModeTabs::buildBaseContents)
                        .build());
    }

    private static void buildBaseContents(
            CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        output.accept(CEIBlocks.MECHANICAL_GRINDSTONE.asItem());
        output.accept(CEIBlocks.EXPERIENCE_HATCH.asItem());
        output.accept(CEIBlocks.EXPERIENCE_LANTERN.asItem());
        output.accept(CEIBlocks.PRINTER.asItem());
        output.accept(CEIBlocks.BLAZE_ENCHANTER.asItem());
        output.accept(CEIBlocks.BLAZE_FORGER.asItem());
        if (CEIConfig.features().classicBlazeEnchanter.get()) {
            output.accept(CEIBlocks.CLASSIC_BLAZE_ENCHANTER.asItem());
        }
        output.accept(AllBlocks.EXPERIENCE_BLOCK);
        output.accept(CEIBlocks.SUPER_EXPERIENCE_BLOCK.asItem());
        output.accept(AllItems.EXP_NUGGET);
        output.accept(CEIItems.SUPER_EXPERIENCE_NUGGET.get());
        output.accept(CEIItems.ENCHANTING_TEMPLATE.get());
        output.accept(CEIItems.SUPER_ENCHANTING_TEMPLATE.get());
        output.accept(CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE.get());
        if (CEIConfig.features().classicBlazeEnchanter.get()) {
            output.accept(CEIItems.BLAZES_ENCHANTING_HANDBOOK.get());
        }
        output.accept(CEIItems.EXPERIENCE_CAKE_BASE.get(), TabVisibility.SEARCH_TAB_ONLY);
        output.accept(CEIItems.EXPERIENCE_CAKE.get());
        output.accept(CEIItems.EXPERIENCE_CAKE_SLICE.get());
        output.accept(CEIItems.EXPERIENCE_BUCKET.get());
    }
}
