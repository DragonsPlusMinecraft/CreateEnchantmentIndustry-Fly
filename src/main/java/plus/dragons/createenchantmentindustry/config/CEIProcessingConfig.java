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

public class CEIProcessingConfig extends ConfigBase {
    public final ConfigFloat regularLightningStrikeTransformXpBlockChance = f(1, 0, 1,
            "regularLightningStrikeTransformXpBlockChance",
            CEIProcessingConfig.Comments.regularLightningStrikeTransformXpBlockChance);
    public final ConfigInt classicBlazeEnchanterFluidCapacity = i(4000, 1000,
            "classicBlazeEnchanterFluidCapacity",
            Comments.classicBlazeEnchanterFluidCapacity);
    public final ConfigFloat classicBlazeEnchanterNormalEnchantingCostCoefficient = f(1.0f, 0.01f,
            "classicBlazeEnchanterNormalEnchantingCostCoefficient",
            Comments.classicBlazeEnchanterNormalEnchantingCostCoefficient);
    public final ConfigFloat classicBlazeEnchanterSuperEnchantingCostCoefficient = f(1.0f, 0.01f,
            "classicBlazeEnchanterSuperEnchantingCostCoefficient",
            Comments.classicBlazeEnchanterSuperEnchantingCostCoefficient);
    public final ConfigFloat classicBlazeEnchanterSuperEnchantingCurseLevelDroppingRate = f(0.25f, 0.01f,
            "classicBlazeEnchanterSuperEnchantingCurseLevelDroppingRate",
            Comments.classicBlazeEnchanterSuperEnchantingCurseLevelDroppingRate);
    public final ConfigFloat blazeEnchanterNormalEnchantingCostMultiplier = f(1.0f, 0.0f,
            "blazeEnchanterNormalEnchantingCostMultiplier",
            Comments.blazeEnchanterNormalEnchantingCostMultiplier);
    public final ConfigFloat blazeEnchanterSuperEnchantingCostMultiplier = f(1.0f, 0.0f,
            "blazeEnchanterSuperEnchantingCostMultiplier",
            Comments.blazeEnchanterSuperEnchantingCostMultiplier);
    public final ConfigFloat blazeEnchanterDirectEnchantingCostMultiplier = f(1.0f, 0.0f,
            "blazeEnchanterDirectEnchantingCostMultiplier",
            Comments.blazeEnchanterDirectEnchantingCostMultiplier);
    public final ConfigFloat blazeEnchanterTemplateEnchantingCostMultiplier = f(1.0f, 0.0f,
            "blazeEnchanterTemplateEnchantingCostMultiplier",
            Comments.blazeEnchanterTemplateEnchantingCostMultiplier);
    public final ConfigFloat blazeForgerNormalForgingCostMultiplier = f(1.0f, 0.0f,
            "blazeForgerNormalForgingCostMultiplier",
            Comments.blazeForgerNormalForgingCostMultiplier);
    public final ConfigFloat blazeForgerSuperForgingCostMultiplier = f(1.0f, 0.0f,
            "blazeForgerSuperForgingCostMultiplier",
            Comments.blazeForgerSuperForgingCostMultiplier);
    public final ConfigFloat blazeForgerMergeCostMultiplier = f(1.0f, 0.0f,
            "blazeForgerMergeCostMultiplier",
            Comments.blazeForgerMergeCostMultiplier);
    public final ConfigFloat blazeForgerApplyCostMultiplier = f(1.0f, 0.0f,
            "blazeForgerApplyCostMultiplier",
            Comments.blazeForgerApplyCostMultiplier);
    public final ConfigFloat blazeForgerExtractCostMultiplier = f(1.0f, 0.0f,
            "blazeForgerExtractCostMultiplier",
            Comments.blazeForgerExtractCostMultiplier);
    public final ConfigInt blazeForgerConflictExtraLevelCost = i(1, 0,
            "blazeForgerConflictExtraLevelCost",
            Comments.blazeForgerConflictExtraLevelCost);
    public final ConfigInt blazeForgerDurabilityRepairLevelCost = i(2, 0,
            "blazeForgerDurabilityRepairLevelCost",
            Comments.blazeForgerDurabilityRepairLevelCost);

    @Override
    public String getName() {
        return "processing";
    }

    static class Comments {
        static final String regularLightningStrikeTransformXpBlockChance = "Probability of natural lightning strikes transforming Blocks of Experience.";
        static final String classicBlazeEnchanterFluidCapacity = "The amount of liquid a Classic Blaze Enchanter can hold (mB).";
        static final String classicBlazeEnchanterNormalEnchantingCostCoefficient = "Experience cost coefficient of Classic Blaze Enchanter regular enchanting.";
        static final String classicBlazeEnchanterSuperEnchantingCostCoefficient = "Experience cost coefficient of Classic Blaze Enchanter super enchanting.";
        static final String classicBlazeEnchanterSuperEnchantingCurseLevelDroppingRate = "Probability that a cursed Classic Blaze Enchanter super enchants resulting in a drop in enchantment level.";
        static final String blazeEnchanterNormalEnchantingCostMultiplier = "Global experience cost multiplier for regular Blaze Enchanter operations.";
        static final String blazeEnchanterSuperEnchantingCostMultiplier = "Global experience cost multiplier for Super Blaze Enchanter operations.";
        static final String blazeEnchanterDirectEnchantingCostMultiplier = "Experience cost multiplier for direct Blaze Enchanter operations on items and books.";
        static final String blazeEnchanterTemplateEnchantingCostMultiplier = "Experience cost multiplier for Blaze Enchanter operations that write enchantments into templates.";
        static final String blazeForgerNormalForgingCostMultiplier = "Global experience cost multiplier for regular Blaze Forger operations.";
        static final String blazeForgerSuperForgingCostMultiplier = "Global experience cost multiplier for Super Blaze Forger operations.";
        static final String blazeForgerMergeCostMultiplier = "Experience cost multiplier for Blaze Forger merge operations.";
        static final String blazeForgerApplyCostMultiplier = "Experience cost multiplier for Blaze Forger apply operations.";
        static final String blazeForgerExtractCostMultiplier = "Experience cost multiplier for Blaze Forger extract operations.";
        static final String blazeForgerConflictExtraLevelCost = "Extra level cost added when Super Forging ignores an enchantment conflict.";
        static final String blazeForgerDurabilityRepairLevelCost = "Level cost added when Blaze Forger repairs item durability during merge.";
    }
}
