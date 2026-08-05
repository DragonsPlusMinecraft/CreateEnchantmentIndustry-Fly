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

package plus.dragons.createenchantmentindustry.client.ponder;

import static com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags.ARM_TARGETS;
import static com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags.CONTRAPTION_ACTOR;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import plus.dragons.createdragonsplus.registry.CDPItems;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public class CEIPonderTags {
    public static final Identifier EXPERIENCE_APPLIANCES = CEICommon.asResource("experience_appliances");
    public static final Identifier SUPER_EXPERIENCE_APPLIANCES = CEICommon.asResource("super_experience_related");

    public static void register(PonderTagRegistrationHelper<Identifier> helper) {
        PonderTagRegistrationHelper<Item> itemHelper = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);

        helper.registerTag(EXPERIENCE_APPLIANCES)
                .addToIndex()
                .item(AllBlocks.EXPERIENCE_BLOCK, true, false)
                .title("Experience Related")
                .description("Components which will be used when processing and applying Experience")
                .register();

        helper.registerTag(SUPER_EXPERIENCE_APPLIANCES)
                .addToIndex()
                .item(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get(), true, false)
                .title("Super Experience Related")
                .description("Components which will be used when processing and applying Super Experience")
                .register();

        itemHelper.addToTag(EXPERIENCE_APPLIANCES)
                .add(AllBlocks.ITEM_DRAIN.asItem())
                .add(AllBlocks.SPOUT.asItem())
                .add(CEIBlocks.MECHANICAL_GRINDSTONE.asItem())
                .add(CDPItems.FLUID_HATCH.get())
                .add(CEIBlocks.EXPERIENCE_HATCH.asItem())
                .add(CEIBlocks.EXPERIENCE_LANTERN.asItem())
                .add(CEIBlocks.BLAZE_ENCHANTER.asItem())
                .add(CEIBlocks.BLAZE_FORGER.asItem())
                .add(CEIBlocks.PRINTER.asItem());

        itemHelper.addToTag(SUPER_EXPERIENCE_APPLIANCES)
                .add(CEIBlocks.BLAZE_ENCHANTER.asItem())
                .add(CEIBlocks.BLAZE_FORGER.asItem());

        itemHelper.addToTag(ARM_TARGETS)
                .add(CEIBlocks.BLAZE_ENCHANTER.asItem())
                .add(CEIBlocks.BLAZE_FORGER.asItem());

        if (System.getProperty("fabric-api.datagen") != null || CEIConfig.features().classicBlazeEnchanter.get()) {
            itemHelper.addToTag(EXPERIENCE_APPLIANCES)
                    .add(CEIBlocks.CLASSIC_BLAZE_ENCHANTER.asItem());
            itemHelper.addToTag(SUPER_EXPERIENCE_APPLIANCES)
                    .add(CEIBlocks.CLASSIC_BLAZE_ENCHANTER.asItem());
            itemHelper.addToTag(ARM_TARGETS)
                    .add(CEIBlocks.CLASSIC_BLAZE_ENCHANTER.asItem());
        }

        itemHelper.addToTag(CONTRAPTION_ACTOR)
                .add(CEIBlocks.EXPERIENCE_LANTERN.asItem());
    }
}
