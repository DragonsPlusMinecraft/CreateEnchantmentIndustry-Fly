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

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlockEntity;

/** Server-safe block-entity type registry. Rendering and visuals are installed by CEIClient. */
public final class CEIBlockEntities {
    public static final CEIRegistryEntry<BlockEntityType<KineticBlockEntity>> MECHANICAL_GRINDSTONE = register(
            "mechanical_grindstone", KineticBlockEntity::new, CEIBlocks.MECHANICAL_GRINDSTONE.get());
    public static final CEIRegistryEntry<BlockEntityType<GrindstoneDrainBlockEntity>> GRINDSTONE_DRAIN = register(
            "grindstone_drain", GrindstoneDrainBlockEntity::new, CEIBlocks.GRINDSTONE_DRAIN.get());
    public static final CEIRegistryEntry<BlockEntityType<ExperienceHatchBlockEntity>> EXPERIENCE_HATCH = register(
            "experience_hatch", ExperienceHatchBlockEntity::new, CEIBlocks.EXPERIENCE_HATCH.get());
    public static final CEIRegistryEntry<BlockEntityType<PrinterBlockEntity>> PRINTER = register(
            "printer", PrinterBlockEntity::new, CEIBlocks.PRINTER.get());
    public static final CEIRegistryEntry<BlockEntityType<BlazeEnchanterBlockEntity>> BLAZE_ENCHANTER = register(
            "blaze_enchanter", BlazeEnchanterBlockEntity::new, CEIBlocks.BLAZE_ENCHANTER.get());
    public static final CEIRegistryEntry<BlockEntityType<BlazeForgerBlockEntity>> BLAZE_FORGER = register(
            "blaze_forger", BlazeForgerBlockEntity::new, CEIBlocks.BLAZE_FORGER.get());
    public static final CEIRegistryEntry<BlockEntityType<ClassicBlazeEnchanterBlockEntity>> CLASSIC_BLAZE_ENCHANTER = register(
            "classic_blaze_enchanter",
            ClassicBlazeEnchanterBlockEntity::new,
            CEIBlocks.CLASSIC_BLAZE_ENCHANTER.get());
    public static final CEIRegistryEntry<BlockEntityType<ExperienceLanternBlockEntity>> EXPERIENCE_LANTERN = register(
            "experience_lantern", ExperienceLanternBlockEntity::new, CEIBlocks.EXPERIENCE_LANTERN.get());

    private static boolean storageProvidersRegistered;

    private CEIBlockEntities() {}

    public static void register() {
        // Class initialization performs the ordered vanilla registrations above.
    }

    public static synchronized void registerStorageProviders() {
        if (storageProvidersRegistered) {
            return;
        }
        storageProvidersRegistered = true;
        FluidStorage.SIDED.registerForBlockEntity(GrindstoneDrainBlockEntity::getFluidStorage, GRINDSTONE_DRAIN.get());
        ItemStorage.SIDED.registerForBlockEntity(GrindstoneDrainBlockEntity::getItemStorage, GRINDSTONE_DRAIN.get());
        FluidStorage.SIDED.registerForBlockEntity(PrinterBlockEntity::getFluidStorage, PRINTER.get());
        FluidStorage.SIDED.registerForBlockEntity(BlazeEnchanterBlockEntity::getFluidStorage, BLAZE_ENCHANTER.get());
        FluidStorage.SIDED.registerForBlockEntity(BlazeForgerBlockEntity::getFluidStorage, BLAZE_FORGER.get());
        FluidStorage.SIDED.registerForBlockEntity(
                ClassicBlazeEnchanterBlockEntity::getFluidStorage, CLASSIC_BLAZE_ENCHANTER.get());
        FluidStorage.SIDED.registerForBlockEntity(
                ExperienceLanternBlockEntity::getFluidStorage, EXPERIENCE_LANTERN.get());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T extends BlockEntity> CEIRegistryEntry<BlockEntityType<T>> register(
            String path, Factory<T> factory, Block... blocks) {
        Identifier id = CEICommon.asResource(path);
        BlockEntityType<T>[] holder = new BlockEntityType[1];
        BlockEntityType<T> type = FabricBlockEntityTypeBuilder
                .create((pos, state) -> factory.create(holder[0], pos, state), blocks)
                .build();
        holder[0] = type;
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
        return new CEIRegistryEntry<>(id, type);
    }

    @FunctionalInterface
    private interface Factory<T extends BlockEntity> {
        T create(BlockEntityType<?> type, BlockPos pos, BlockState state);
    }
}
