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

package plus.dragons.createenchantmentindustry.config;

import com.zurrtum.create.catnip.config.ConfigBase;

public class CEIEnchantmentsConfig extends ConfigBase {
    public final ConfigInt blazeEnchanterMaxEnchantLevel = i(30, 0,
            "blazeEnchanterMaxEnchantLevel",
            Comments.blazeEnchanterMaxEnchantLevel);
    public final ConfigInt blazeEnchanterMaxSuperEnchantLevel = i(60, 0,
            "blazeEnchanterMaxSuperEnchantLevel",
            Comments.blazeEnchanterMaxSuperEnchantLevel);
    public final ConfigFloat blazeEnchanterBlockedLightningCurseChance = f(0.5f, 0.0f, 1.0f,
            "blazeEnchanterBlockedLightningCurseChance",
            Comments.blazeEnchanterBlockedLightningCurseChance);
    public final ConfigInt blazeEnchanterBlockedLightningCurseCount = i(1, 0, 16,
            "blazeEnchanterBlockedLightningCurseCount",
            Comments.blazeEnchanterBlockedLightningCurseCount);
    public final ConfigInt blazeEnchanterBlockedLightningCurseMaxLevel = i(1, 1, 255,
            "blazeEnchanterBlockedLightningCurseMaxLevel",
            Comments.blazeEnchanterBlockedLightningCurseMaxLevel);
    public final ConfigInt blazeEnchanterMaxLevelExtension = i(1, 0, 255,
            "blazeEnchanterMaxLevelExtension",
            Comments.blazeEnchanterMaxLevelExtension);
    public final ConfigInt blazeForgerMaxLevelExtension = i(1, 0, 255,
            "blazeForgerMaxLevelExtension",
            Comments.blazeForgerMaxLevelExtension);
    public final ConfigBool ignoreEnchantmentCompatibility = b(true,
            "ignoreEnchantmentCompatibility",
            Comments.ignoreEnchantmentCompatibility);
    public final ConfigBool extractEnchantmentRespectLevelExtension = b(false,
            "extractEnchantmentRespectLevelExtension",
            Comments.extractEnchantmentRespectLevelExtension);

    @Override
    public String getName() {
        return "enchantments";
    }

    static class Comments {
        static final String blazeEnchanterMaxEnchantLevel = "The max experience level a Blaze Enchanter can use in Regular Enchanting";
        static final String blazeEnchanterMaxSuperEnchantLevel = "The max experience level a Blaze Enchanter can use in Super Enchanting";
        static final String blazeEnchanterBlockedLightningCurseChance = "Chance per curse roll for blocked-lightning Super Enchanting to add an applicable curse to the result";
        static final String blazeEnchanterBlockedLightningCurseCount = "Maximum curse rolls for blocked-lightning Super Enchanting";
        static final String blazeEnchanterBlockedLightningCurseMaxLevel = "Maximum curse level blocked-lightning Super Enchanting can add";
        static final String blazeEnchanterMaxLevelExtension = "Max enchantment level in Super Enchanting will be extended by this value when no per-enchantment processing rule exists";
        static final String blazeForgerMaxLevelExtension = "Max enchantment level in Super Forging will be extended by this value when no per-enchantment processing rule exists";
        static final String ignoreEnchantmentCompatibility = "If Super Enchanting and Super Forging ignores enchantment compatibility";
        static final String extractEnchantmentRespectLevelExtension = "If Enchantment extraction respects over-capped level";
    }
}
