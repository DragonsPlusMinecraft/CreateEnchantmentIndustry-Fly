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

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.materials.ExperienceBlock;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeMovementBehaviour;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlock;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlock;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternMovementBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindstoneBlock;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlock;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

/** Blocks are registered from {@link CEICreatePlugin#onBlockRegister()} during vanilla bootstrap. */
public final class CEIBlocks {
    public static final CEIRegistryEntry<FluidBlock> EXPERIENCE = register(
            "experience",
            properties -> new FluidBlock((FlowableFluid) CEIFluids.EXPERIENCE.getSource(), properties),
            BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .lightLevel(state -> 15));
    public static final CEIRegistryEntry<MechanicalGrindstoneBlock> MECHANICAL_GRINDSTONE = register(
            "mechanical_grindstone",
            MechanicalGrindstoneBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE).requiresCorrectToolForDrops());
    public static final CEIRegistryEntry<GrindstoneDrainBlock> GRINDSTONE_DRAIN = register(
            "grindstone_drain",
            properties -> new GrindstoneDrainBlock(MECHANICAL_GRINDSTONE.get(), properties),
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).requiresCorrectToolForDrops());
    public static final CEIRegistryEntry<ExperienceHatchBlock> EXPERIENCE_HATCH = register(
            "experience_hatch",
            ExperienceHatchBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK)
                    .mapColor(MapColor.COLOR_GREEN)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 12));
    public static final CEIRegistryEntry<PrinterBlock> PRINTER = register(
            "printer",
            PrinterBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).requiresCorrectToolForDrops());
    public static final CEIRegistryEntry<BlazeEnchanterBlock> BLAZE_ENCHANTER = register(
            "blaze_enchanter", BlazeEnchanterBlock::new, blazeProperties());
    public static final CEIRegistryEntry<BlazeForgerBlock> BLAZE_FORGER = register(
            "blaze_forger", BlazeForgerBlock::new, blazeProperties());
    public static final CEIRegistryEntry<ClassicBlazeEnchanterBlock> CLASSIC_BLAZE_ENCHANTER = register(
            "classic_blaze_enchanter", ClassicBlazeEnchanterBlock::new, blazeProperties());
    public static final CEIRegistryEntry<ExperienceBlock> SUPER_EXPERIENCE_BLOCK = register(
            "super_experience_block",
            ExperienceBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                    .mapColor(MapColor.DIAMOND)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 15));
    public static final CEIRegistryEntry<ExperienceLanternBlock> EXPERIENCE_LANTERN = register(
            "experience_lantern",
            ExperienceLanternBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> state.getValue(ExperienceLanternBlock.LIGHT)));

    public static final BlazeMovementBehaviour BLAZE_ENCHANTER_MOVEMENT = new BlazeMovementBehaviour();
    public static final BlazeMovementBehaviour BLAZE_FORGER_MOVEMENT = new BlazeMovementBehaviour();
    public static final BlazeMovementBehaviour CLASSIC_BLAZE_ENCHANTER_MOVEMENT = new BlazeMovementBehaviour();
    public static final ExperienceLanternMovementBehaviour EXPERIENCE_LANTERN_MOVEMENT = new ExperienceLanternMovementBehaviour();

    private static boolean initialized;

    private CEIBlocks() {}

    public static synchronized void register() {
        if (initialized) {
            return;
        }
        initialized = true;

        CEIFluids.attachBlock(EXPERIENCE.get());

        CEIConfig.stress().setImpact(MECHANICAL_GRINDSTONE.getId(), 4.0);
        CEIConfig.stress().setImpact(GRINDSTONE_DRAIN.getId(), 4.0);

        MovementBehaviour.REGISTRY.register(BLAZE_ENCHANTER.get(), BLAZE_ENCHANTER_MOVEMENT);
        MovementBehaviour.REGISTRY.register(BLAZE_FORGER.get(), BLAZE_FORGER_MOVEMENT);
        MovementBehaviour.REGISTRY.register(CLASSIC_BLAZE_ENCHANTER.get(), CLASSIC_BLAZE_ENCHANTER_MOVEMENT);
        MovementBehaviour.REGISTRY.register(EXPERIENCE_LANTERN.get(), EXPERIENCE_LANTERN_MOVEMENT);
    }

    private static BlockBehaviour.Properties blazeProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                .mapColor(MapColor.COLOR_GRAY)
                .requiresCorrectToolForDrops()
                .lightLevel(BlazeBlock::getLight);
    }

    private static <T extends Block> CEIRegistryEntry<T> register(
            String path, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        Identifier id = CEICommon.asResource(path);
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        T block = Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
        return new CEIRegistryEntry<>(id, block);
    }
}
