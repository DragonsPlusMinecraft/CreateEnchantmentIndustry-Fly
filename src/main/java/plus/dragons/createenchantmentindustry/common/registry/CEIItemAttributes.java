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

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import java.util.function.BiPredicate;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.common.recipe.CDPRecipeAccess;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneHelper;

public final class CEIItemAttributes {
    public static final ItemAttributeType PROCESSABLE_BY_MECHANICAL_GRINDSTONE = attribute(
            "processable_by_mechanical_grindstone",
            (itemStack, level) -> {
                SingleRecipeInput input = new SingleRecipeInput(itemStack);
                if (CDPRecipeAccess.getFirst(level, CEIRecipes.GRINDING.getType(), input).isPresent()) {
                    return true;
                }
                if (CDPRecipeAccess.getFirst(level, AllRecipeTypes.SANDPAPER_POLISHING, input).isPresent()) {
                    return true;
                }
                return GrindstoneHelper.canItemBeGrinded(itemStack, ItemStack.EMPTY);
            });

    private CEIItemAttributes() {}

    public static void register() {
        // Class initialization performs registration.
    }

    private static ItemAttributeType attribute(String name, BiPredicate<ItemStack, Level> predicate) {
        Identifier id = CEICommon.asResource(name);
        ItemAttributeType type = new SingletonItemAttribute.Type(attributeType -> new SingletonItemAttribute(attributeType, predicate, CEICommon.ID + "." + name));
        return Registry.register(CreateRegistries.ITEM_ATTRIBUTE_TYPE, id, type);
    }
}
