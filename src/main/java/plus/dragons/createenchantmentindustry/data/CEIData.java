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

package plus.dragons.createenchantmentindustry.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataProvider;
import plus.dragons.createenchantmentindustry.common.registry.CEIAdvancements;

/** Core-only Fabric data generation entry point. */
public final class CEIData implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider((DataProvider.Factory<CEIAssetProvider>) CEIAssetProvider::new);
        pack.addProvider(CEIGenerateEntriesProvider::new);
        pack.addProvider((DataProvider.Factory<CEIDataMapProvider>) CEIDataMapProvider::new);
        pack.addProvider(CEIRecipeProvider::new);
        pack.addProvider(CEIAdvancements::new);
        pack.addProvider(CEILanguageProvider::new);
        pack.addProvider(CEIEnchantmentTagsProvider::new);
        pack.addProvider(CEIBlockLootProvider::new);
        pack.addProvider(CEIBlockTagsProvider::new);
        pack.addProvider(CEIItemTagsProvider::new);
        pack.addProvider(CEIFluidTagsProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder) {
        CEIGenerateEntriesProvider.addBootstraps(builder);
    }
}
