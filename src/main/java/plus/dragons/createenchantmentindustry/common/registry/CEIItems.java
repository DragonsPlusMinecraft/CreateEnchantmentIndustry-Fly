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

import com.zurrtum.create.content.materials.ExperienceNuggetItem;
import com.zurrtum.create.content.processing.AssemblyOperatorBlockItem;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.item.ExperienceBucketItem;
import plus.dragons.createenchantmentindustry.common.item.FoilItem;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindStoneItem;
import plus.dragons.createenchantmentindustry.common.processing.BlazeCustomRenderedBlockItem;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;

/** Explicit item registration performed from the common Fabric initializer. */
public final class CEIItems {
    private static final CEIRegistryEntry<MechanicalGrindStoneItem> MECHANICAL_GRINDSTONE = registerBlockItem(
            CEIBlocks.MECHANICAL_GRINDSTONE, MechanicalGrindStoneItem::new, new Item.Properties());
    private static final CEIRegistryEntry<BlockItem> GRINDSTONE_DRAIN = registerBlockItem(
            CEIBlocks.GRINDSTONE_DRAIN, BlockItem::new, new Item.Properties());
    private static final CEIRegistryEntry<BlockItem> EXPERIENCE_HATCH = registerBlockItem(
            CEIBlocks.EXPERIENCE_HATCH, BlockItem::new, new Item.Properties());
    private static final CEIRegistryEntry<AssemblyOperatorBlockItem> PRINTER = registerBlockItem(
            CEIBlocks.PRINTER, AssemblyOperatorBlockItem::new, new Item.Properties());
    private static final CEIRegistryEntry<BlazeCustomRenderedBlockItem.Enchanter> BLAZE_ENCHANTER = registerBlockItem(
            CEIBlocks.BLAZE_ENCHANTER, BlazeCustomRenderedBlockItem.Enchanter::new, new Item.Properties());
    private static final CEIRegistryEntry<BlazeCustomRenderedBlockItem.Forger> BLAZE_FORGER = registerBlockItem(
            CEIBlocks.BLAZE_FORGER, BlazeCustomRenderedBlockItem.Forger::new, new Item.Properties());
    private static final CEIRegistryEntry<BlazeCustomRenderedBlockItem.ClassicEnchanter> CLASSIC_BLAZE_ENCHANTER = registerBlockItem(
            CEIBlocks.CLASSIC_BLAZE_ENCHANTER,
            BlazeCustomRenderedBlockItem.ClassicEnchanter::new,
            new Item.Properties());
    private static final CEIRegistryEntry<BlockItem> SUPER_EXPERIENCE_BLOCK = registerBlockItem(
            CEIBlocks.SUPER_EXPERIENCE_BLOCK, BlockItem::new, new Item.Properties().rarity(Rarity.RARE));
    private static final CEIRegistryEntry<BlockItem> EXPERIENCE_LANTERN = registerBlockItem(
            CEIBlocks.EXPERIENCE_LANTERN, BlockItem::new, new Item.Properties());

    public static final CEIRegistryEntry<ExperienceNuggetItem> SUPER_EXPERIENCE_NUGGET = register(
            "super_experience_nugget", ExperienceNuggetItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final CEIRegistryEntry<EnchantingTemplateItem> ENCHANTING_TEMPLATE = register(
            "enchanting_template", EnchantingTemplateItem::normal, new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final CEIRegistryEntry<EnchantingTemplateItem> SUPER_ENCHANTING_TEMPLATE = register(
            "super_enchanting_template", EnchantingTemplateItem::special, new Item.Properties().rarity(Rarity.RARE));
    public static final CEIRegistryEntry<Item> BLAZES_ENCHANTING_HANDBOOK = register(
            "blazes_enchanting_handbook", Item::new, new Item.Properties());
    public static final CEIRegistryEntry<Item> EXPERIENCE_CAKE_BASE = register(
            "experience_cake_base", Item::new, new Item.Properties());
    public static final CEIRegistryEntry<FoilItem> EXPERIENCE_CAKE = register(
            "experience_cake", FoilItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final CEIRegistryEntry<FoilItem> EXPERIENCE_CAKE_SLICE = register(
            "experience_cake_slice", FoilItem::new, new Item.Properties().rarity(Rarity.RARE));
    public static final CEIRegistryEntry<ExperienceBucketItem> EXPERIENCE_BUCKET = register(
            "experience_bucket",
            properties -> new ExperienceBucketItem(CEIFluids.EXPERIENCE.getSource(), properties),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).rarity(Rarity.UNCOMMON));

    static {
        CEIFluids.attachBucket(EXPERIENCE_BUCKET.get());
    }

    private CEIItems() {}

    public static void register() {
        // Class initialization performs the ordered vanilla registrations above.
    }

    private static <B extends Block, I extends BlockItem> CEIRegistryEntry<I> registerBlockItem(
            CEIRegistryEntry<B> block,
            BiFunction<B, Item.Properties, I> factory,
            Item.Properties properties) {
        return register(block.getId(), itemProperties -> factory.apply(block.get(), itemProperties), properties.useBlockDescriptionPrefix());
    }

    private static <T extends Item> CEIRegistryEntry<T> register(
            String path, Function<Item.Properties, T> factory, Item.Properties properties) {
        return register(CEICommon.asResource(path), factory, properties);
    }

    private static <T extends Item> CEIRegistryEntry<T> register(
            Identifier id, Function<Item.Properties, T> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        T item = factory.apply(properties.setId(key));
        if (item instanceof BlockItem blockItem) {
            blockItem.registerBlocks(Item.BY_BLOCK, item);
        }
        item = Registry.register(BuiltInRegistries.ITEM, key, item);
        return new CEIRegistryEntry<>(id, item);
    }
}
