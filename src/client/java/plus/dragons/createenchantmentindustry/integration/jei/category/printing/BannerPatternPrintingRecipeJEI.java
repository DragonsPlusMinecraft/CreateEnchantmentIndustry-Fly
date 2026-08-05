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

import com.mojang.serialization.MapCodec;
import mezz.jei.api.fabric.constants.FabricTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.registry.CEIDataMaps;
import plus.dragons.createenchantmentindustry.util.CEIDyeFluids;
import plus.dragons.createenchantmentindustry.util.CEILang;

/** Dynamic JEI preview for copying one banner pattern with a dye fluid. */
public enum BannerPatternPrintingRecipeJEI implements PrintingRecipeJEI {
    INSTANCE;

    public static final Type TYPE = PrintingRecipeJEI.register(CEICommon.asResource("banner_pattern"), MapCodec.unit(INSTANCE));

    @Override
    public void setBase(IRecipeSlotBuilder slot) {
        net.minecraft.core.registries.BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.BANNERS)
                .forEach(item -> slot.add(item.value()));
        slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(CEILang
                .translate("recipe.printing.banner_pattern.base")
                .style(ChatFormatting.GRAY)
                .component()));
    }

    @Override
    public void setTemplate(IRecipeSlotBuilder slot) {
        var level = Minecraft.getInstance().level;
        if (level != null)
            level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN).listElements()
                    .map(pattern -> withPattern(Items.WHITE_BANNER.getDefaultInstance(), pattern, DyeColor.BLACK))
                    .forEach(slot::add);
        slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(CEILang
                .translate("recipe.printing.banner_pattern.template")
                .style(ChatFormatting.GRAY)
                .component()));
    }

    @Override
    public void setFluid(IRecipeSlotBuilder slot) {
        CEIDataMaps.getSourceFluidAmountEntries(CEIDataMaps.PRINTING_BANNER_PATTERN_INGREDIENT)
                .filter(pair -> CEIDyeFluids.color(pair.getFirst()).isPresent())
                .forEach(pair -> slot.add(pair.getFirst(), pair.getSecond()));
    }

    @Override
    public void setOutput(IRecipeSlotBuilder slot) {
        slot.add(Items.WHITE_BANNER);
        slot.addRichTooltipCallback((view, tooltip) -> tooltip.add(CEILang
                .translate("recipe.printing.banner_pattern.color_follow_dye")
                .style(ChatFormatting.GRAY)
                .component()));
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public void onDisplayedIngredientsUpdate(
            IRecipeSlotDrawable baseSlot,
            IRecipeSlotDrawable templateSlot,
            IRecipeSlotDrawable fluidSlot,
            IRecipeSlotDrawable outputSlot,
            IFocusGroup focuses) {
        var fluid = fluidSlot.getDisplayedIngredient(FabricTypes.FLUID_STACK);
        var base = baseSlot.getDisplayedItemStack();
        var template = templateSlot.getDisplayedItemStack();
        if (fluid.isEmpty() || base.isEmpty() || template.isEmpty())
            return;
        var color = CEIDyeFluids.color(fluid.get().getFluidVariant().getFluid());
        BannerPatternLayers layers = template.get().getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        if (color.isEmpty() || layers.layers().isEmpty())
            return;
        outputSlot.createDisplayOverrides()
                .add(withPattern(base.get(), layers.layers().getFirst().pattern(), color.get()));
    }

    private static ItemStack withPattern(ItemStack stack, Holder<BannerPattern> pattern, DyeColor color) {
        ItemStack result = stack.copy();
        result.set(
                DataComponents.BANNER_PATTERNS,
                new BannerPatternLayers.Builder().add(pattern, color).build());
        return result;
    }
}
