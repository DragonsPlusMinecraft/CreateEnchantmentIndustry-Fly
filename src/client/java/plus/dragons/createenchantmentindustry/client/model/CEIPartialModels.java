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

package plus.dragons.createenchantmentindustry.client.model;

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public class CEIPartialModels {
    public static final PartialModel MECHANICAL_GRINDSTONE = block("mechanical_grindstone");
    public static final PartialModel PRINTER_NOZZLE_TOP = block("printer/nozzle_top");
    public static final PartialModel PRINTER_NOZZLE_BOTTOM = block("printer/nozzle_bottom");
    public static final PartialModel PRINTER_PISTON = block("printer/piston");
    public static final PartialModel BLAZE_ENCHANTER_HAT = block("blaze/enchanter_hat");
    public static final PartialModel BLAZE_ENCHANTER_HAT_SMALL = block("blaze/enchanter_hat_small");
    public static final PartialModel BLAZE_FORGER_HAT = block("blaze/forger_hat");
    public static final PartialModel BLAZE_FORGER_HAT_SMALL = block("blaze/forger_hat_small");

    public static void register() {}

    private static PartialModel block(String path) {
        return PartialModel.of(CEICommon.asResource("block/" + path));
    }
}
