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

package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ExperienceLanternMountedFluidStorageType extends MountedFluidStorageType<ExperienceLanternMountedStorage> {
    public ExperienceLanternMountedFluidStorageType() {
        super(ExperienceLanternMountedStorage.CODEC);
    }

    @Override
    public @Nullable ExperienceLanternMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof ExperienceLanternBlockEntity lantern)
            return ExperienceLanternMountedStorage.fromLantern(lantern);
        return null;
    }
}
