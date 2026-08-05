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

import com.zurrtum.create.AllItems;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import plus.dragons.createdragonsplus.common.advancements.criterion.StatTrigger;
import plus.dragons.createdragonsplus.registry.CDPItems;
import plus.dragons.createenchantmentindustry.util.CEIAdvancement;

/**
 * Runtime advancement handles and the Fabric datagen provider for the complete core tree.
 */
public final class CEIAdvancements extends AdvancementProvider {
    public static final List<CEIAdvancement> ENTRIES = new ArrayList<>();
    public static final CEIAdvancement START = null;

    public static final CEIAdvancement ROOT = entry(
            "root", "Welcome to Create: Enchantment Industry", "Road to master enchanting begins");
    public static final CEIAdvancement EXPERIENCED_ENGINEER = entry(
            "experienced_engineer", "Experienced Engineer", "Obtain some Nuggets of Experience");
    public static final CEIAdvancement SPIRIT_TAKING = entry(
            "spirit_taking", "Spirit-Taking", "Store your experience using an Experience Hatch");
    public static final CEIAdvancement SPIRITUAL_RETURN = entry(
            "spiritual_return", "Spiritual Return", "Retrieve some experience using an Experience Hatch");
    public static final CEIAdvancement A_SHOWER_EXPERIENCE = entry(
            "a_shower_experience", "A Shower \"Experience\"", "Break a Fluid Pipe and bathe in the leaked experience");
    public static final CEIAdvancement LUMEN_NEXUS = entry(
            "lumen_nexus", "Lumen Nexus", "Obtain an Experience Lantern");
    public static final CEIAdvancement GONE_WITH_THE_FOIL = entry(
            "gone_with_the_foil", "Gone with the Foil", "Watch an enchanted item be disenchanted by a Mechanical Grindstone");
    public static final CEIAdvancement GRIND_TO_POLISH = entry(
            "grind_to_polish", "Grind to Polish", "Sandpaper? I've got a better one");
    public static final CEIAdvancement EXPERIENCED_RECYCLER = entry(
            "experienced_recycler", "Experienced Recycler", "Obtain 1,000,000 mB of Liquid Experience from Mechanical Grindstones");
    public static final CEIAdvancement BLAZE_ENCHANTERY = entry(
            "blaze_enchantery", "Blaze Enchantry", "Blazes can do more than boil water. Obtain a Blaze Enchanter");
    public static final CEIAdvancement BLAZING_ENCHANTMENT = entry(
            "blazing_enchantment", "Blazing Enchantment", "Enchant an unenchanted item using a Blaze Enchanter");
    public static final CEIAdvancement SIGIL_FORGING = entry(
            "sigil_forging", "Sigil Forging", "Add a new enchantment to an Enchanting Template using a Blaze Enchanter");
    public static final CEIAdvancement THOUSAND_RUNES = entry(
            "thousand_runes", "Thousand Runes", "Use a Blaze Enchanter 1,000 times");
    public static final CEIAdvancement LEGACY_IGNITION = entry(
            "legacy_ignition", "Legacy Ignition", "It's from the last era. Obtain a Classic Blaze Enchanter");
    public static final CEIAdvancement THOUSANDFOLD_EVERCHANT = entry(
            "thousandfold_everchant", "Thousandfold Everchant", "Classic Blaze Enchanter enchants 1,000 times");
    public static final CEIAdvancement BORN_TALENT_OF_FIRE = entry(
            "born_talent_of_fire", "Born Talent of Fire", "Blazes were born for this. Obtain a Blaze Forger");
    public static final CEIAdvancement BLAZING_FUSION = entry(
            "blazing_fusion", "Blazing Fusion", "Merge two of the same item using a Blaze Forger");
    public static final CEIAdvancement SIGIL_CASTING = entry(
            "sigil_casting", "Sigil Casting", "Apply an Enchanting Template using a Blaze Forger");
    public static final CEIAdvancement MAGIC_UNBINDING = entry(
            "magic_unbinding", "Magic Unbinding", "Strip an item's enchantment off using a Blaze Forger");
    public static final CEIAdvancement BLAZING_CENTURION = entry(
            "blazing_centurion", "Blazing Centurion", "Use a Blaze Forger 1,000 times");
    public static final CEIAdvancement LIGHTNING_CATALYSIS = entry(
            "lightning_catalysis", "Lightning Catalysis", "Obtain Super Experience");
    public static final CEIAdvancement PROBABILITY_SPIKE = entry(
            "probability_spike", "Probability Spike", "How did all these treasures get here?");
    public static final CEIAdvancement TRANSCENDENT_OVERCLOCK = entry(
            "transcendent_overclock", "Transcendent Overclock", "How is this possible? Enchantment level caps don't exist?");
    public static final CEIAdvancement PARADOX_FUSION = entry(
            "paradox_fusion", "Paradox Fusion", "How is this possible? They shouldn't appear at the same time!");
    public static final CEIAdvancement OSHA_VIOLATION = entry(
            "osha_violation", "OSHA Violation", "You should NOT have let this happen!!!");
    public static final CEIAdvancement OMNI_ENCHANTER = entry(
            "omni_enchanter", "Omni-Enchanter", "Unbelievable! You've Super Enchanted 1,000 times! Now the number speaks for itself");
    public static final CEIAdvancement COPIABLE_MASTERPIECE = entry(
            "copiable_masterpiece", "Copiable Masterpiece", "Copy a Written Book using a Printer");
    public static final CEIAdvancement COPIABLE_MYSTERY = entry(
            "copiable_mystery", "Copiable Mystery", "Copy an Enchanted Book using a Printer");
    public static final CEIAdvancement BRAND_REGISTRY = entry(
            "brand_registry", "Brand Registry", "Use a Printer to rename an item");
    public static final CEIAdvancement SUPPLY_CHAIN_REFACTOR = entry(
            "supply_chain_refactor", "Supply Chain Refactor", "Use a Printer to change a package's address");
    public static final CEIAdvancement ASSEMBLY_AESTHETICS = entry(
            "assembly_aesthetics", "Assembly Aesthetics", "Use a Printer to change a package's pattern");
    public static final CEIAdvancement GREAT_PUBLISHER = entry(
            "great_publisher", "Great Publisher", "Use a Printer 1,000 times");

    private static boolean registered;

    public CEIAdvancements(
            PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, List.of(CEIAdvancements::generate));
    }

    private static CEIAdvancement entry(String id, String title, String description) {
        CEIAdvancement advancement = new CEIAdvancement(id, title, description);
        ENTRIES.add(advancement);
        return advancement;
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        for (CEIAdvancement advancement : ENTRIES) {
            Registry.register(BuiltInRegistries.TRIGGER_TYPES, advancement.id(), advancement.builtinTrigger());
        }
    }

    public static void provideLang(BiConsumer<String, String> consumer) {
        ENTRIES.forEach(advancement -> advancement.provideLang(consumer));
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Advancements";
    }

    private static void generate(
            HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
        save(output, ROOT, null, CEIItems.EXPERIENCE_BUCKET.get(), AdvancementType.TASK, false, false, false, inventory());
        save(output, EXPERIENCED_ENGINEER, ROOT, AllItems.EXP_NUGGET, AdvancementType.TASK, true, false, false, inventory(AllItems.EXP_NUGGET));
        save(output, SPIRIT_TAKING, EXPERIENCED_ENGINEER, AllItems.EXP_NUGGET, AdvancementType.TASK, true, false, false, builtin(SPIRIT_TAKING));
        save(output, SPIRITUAL_RETURN, SPIRIT_TAKING, AllItems.EXP_NUGGET, AdvancementType.TASK, true, false, false, builtin(SPIRITUAL_RETURN));
        save(output, A_SHOWER_EXPERIENCE, SPIRITUAL_RETURN, AllItems.EXP_NUGGET, AdvancementType.GOAL, true, true, true, builtin(A_SHOWER_EXPERIENCE));
        save(output, LUMEN_NEXUS, SPIRITUAL_RETURN, CEIBlocks.EXPERIENCE_LANTERN.get(), AdvancementType.TASK, true, false, false, inventory(CEIBlocks.EXPERIENCE_LANTERN.get()));

        save(output, GONE_WITH_THE_FOIL, EXPERIENCED_ENGINEER, CEIBlocks.MECHANICAL_GRINDSTONE.get(), AdvancementType.TASK, true, false, false, builtin(GONE_WITH_THE_FOIL));
        save(output, GRIND_TO_POLISH, GONE_WITH_THE_FOIL, AllItems.SAND_PAPER, AdvancementType.TASK, true, true, false, builtin(GRIND_TO_POLISH));
        save(output, EXPERIENCED_RECYCLER, GONE_WITH_THE_FOIL, CEIBlocks.GRINDSTONE_DRAIN.get(), AdvancementType.GOAL, true, true, false, stat(CEIStats.GRINDSTONE_EXPERIENCE.getId(), 1_000_000));

        save(output, BLAZE_ENCHANTERY, EXPERIENCED_ENGINEER, CEIBlocks.BLAZE_ENCHANTER.get(), AdvancementType.TASK, true, false, false, inventory(CEIBlocks.BLAZE_ENCHANTER.get()));
        save(output, BLAZING_ENCHANTMENT, BLAZE_ENCHANTERY, Items.GOLDEN_HELMET, AdvancementType.TASK, true, false, false, builtin(BLAZING_ENCHANTMENT));
        save(output, SIGIL_FORGING, BLAZING_ENCHANTMENT, CEIItems.ENCHANTING_TEMPLATE.get(), AdvancementType.TASK, true, false, false, builtin(SIGIL_FORGING));
        save(output, THOUSAND_RUNES, SIGIL_FORGING, CEIBlocks.BLAZE_ENCHANTER.get(), AdvancementType.GOAL, true, true, false, stat(CEIStats.ENCHANT.getId(), 1_000));
        save(output, LEGACY_IGNITION, BLAZE_ENCHANTERY, CEIBlocks.CLASSIC_BLAZE_ENCHANTER.get(), AdvancementType.TASK, true, false, false, inventory(CEIBlocks.CLASSIC_BLAZE_ENCHANTER.get()));
        save(output, THOUSANDFOLD_EVERCHANT, LEGACY_IGNITION, CEIBlocks.CLASSIC_BLAZE_ENCHANTER.get(), AdvancementType.GOAL, true, true, false, stat(CEIStats.CLASSIC_ENCHANT.getId(), 1_000));

        save(output, BORN_TALENT_OF_FIRE, EXPERIENCED_ENGINEER, CEIBlocks.BLAZE_FORGER.get(), AdvancementType.TASK, true, false, false, inventory(CEIBlocks.BLAZE_FORGER.get()));
        save(output, BLAZING_FUSION, BORN_TALENT_OF_FIRE, Items.GOLDEN_SWORD, AdvancementType.TASK, true, false, false, builtin(BLAZING_FUSION));
        save(output, SIGIL_CASTING, BLAZING_FUSION, CEIItems.ENCHANTING_TEMPLATE.get(), AdvancementType.TASK, true, false, false, builtin(SIGIL_CASTING));
        save(output, MAGIC_UNBINDING, SIGIL_CASTING, CEIItems.ENCHANTING_TEMPLATE.get(), AdvancementType.TASK, true, false, false, builtin(MAGIC_UNBINDING));
        save(output, BLAZING_CENTURION, MAGIC_UNBINDING, CEIBlocks.BLAZE_FORGER.get(), AdvancementType.GOAL, true, true, false, stat(CEIStats.FORGE.getId(), 1_000));

        save(output, LIGHTNING_CATALYSIS, EXPERIENCED_ENGINEER, CEIBlocks.BLAZE_FORGER.get(), AdvancementType.GOAL, true, true, false, inventory(CEIBlocks.SUPER_EXPERIENCE_BLOCK.get()));
        save(output, PROBABILITY_SPIKE, LIGHTNING_CATALYSIS, Items.DIAMOND, AdvancementType.GOAL, true, true, false, builtin(PROBABILITY_SPIKE));
        save(output, TRANSCENDENT_OVERCLOCK, PROBABILITY_SPIKE, Items.EMERALD, AdvancementType.GOAL, true, true, false, builtin(TRANSCENDENT_OVERCLOCK));
        save(output, PARADOX_FUSION, TRANSCENDENT_OVERCLOCK, Items.REDSTONE, AdvancementType.GOAL, true, true, false, builtin(PARADOX_FUSION));
        save(output, OSHA_VIOLATION, LIGHTNING_CATALYSIS, Items.BARRIER, AdvancementType.GOAL, true, true, true, builtin(OSHA_VIOLATION));
        save(output, OMNI_ENCHANTER, PARADOX_FUSION, Items.NETHER_STAR, AdvancementType.GOAL, true, true, false, stat(CEIStats.SUPER_ENCHANT.getId(), 100));

        save(output, COPIABLE_MASTERPIECE, EXPERIENCED_ENGINEER, Items.WRITTEN_BOOK, AdvancementType.TASK, true, false, false, builtin(COPIABLE_MASTERPIECE));
        save(output, COPIABLE_MYSTERY, COPIABLE_MASTERPIECE, Items.ENCHANTED_BOOK, AdvancementType.TASK, true, false, false, builtin(COPIABLE_MYSTERY));
        save(output, BRAND_REGISTRY, COPIABLE_MYSTERY, Items.NAME_TAG, AdvancementType.TASK, true, false, false, builtin(BRAND_REGISTRY));
        save(output, SUPPLY_CHAIN_REFACTOR, BRAND_REGISTRY, AllItems.CARDBOARD_PACKAGE_12X12, AdvancementType.TASK, true, false, false, builtin(SUPPLY_CHAIN_REFACTOR));
        save(output, ASSEMBLY_AESTHETICS, SUPPLY_CHAIN_REFACTOR, CDPItems.RARE_MARBLE_GATE_PACKAGE.get(), AdvancementType.TASK, true, false, false, builtin(ASSEMBLY_AESTHETICS));
        save(output, GREAT_PUBLISHER, BRAND_REGISTRY, CEIBlocks.PRINTER.get(), AdvancementType.GOAL, true, true, false, stat(CEIStats.PRINT.getId(), 1_000));
    }

    private static void save(
            Consumer<AdvancementHolder> output,
            CEIAdvancement advancement,
            CEIAdvancement parent,
            ItemLike icon,
            AdvancementType type,
            boolean showToast,
            boolean announceChat,
            boolean hidden,
            Criterion<?> criterion) {
        String translation = "advancement." + advancement.id().getNamespace() + "." + advancement.id().getPath();
        Advancement.Builder builder = Advancement.Builder.advancement();
        if (parent != null) {
            builder.parent(AdvancementSubProvider.createPlaceholder(parent.id().toString()));
        }
        builder.display(
                icon,
                Component.translatable(translation),
                Component.translatable(translation + ".desc")
                        .withStyle(Style.EMPTY.withColor(0xDBA213)),
                advancement == ROOT
                        ? Identifier.fromNamespaceAndPath(
                                CEIItems.EXPERIENCE_BUCKET.getId().getNamespace(),
                                "textures/block/super_experience_block.png")
                        : null,
                type,
                showToast,
                announceChat,
                hidden)
                .addCriterion("0", criterion)
                .sendsTelemetryEvent();
        output.accept(builder.build(advancement.id()));
    }

    private static Criterion<?> builtin(CEIAdvancement advancement) {
        return advancement.builtinTrigger().createCriterion(advancement.builtinTrigger());
    }

    private static Criterion<?> inventory(ItemLike... items) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    private static Criterion<?> stat(Identifier id, int minimum) {
        return StatTrigger.Instance.of(id, MinMaxBounds.Ints.atLeast(minimum));
    }
}
