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

package plus.dragons.createenchantmentindustry.common.processing.forger;

import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;

/** Server-safe forging mode state. Its value box and formatting live in the client source set. */
public class BlazeForgerModeBehaviour extends ServerScrollValueBehaviour {
    private final BlazeForgerBlockEntity forger;

    public BlazeForgerModeBehaviour(BlazeForgerBlockEntity forger) {
        super(forger);
        this.forger = forger;
        between(0, BlazeForgerMode.values().length - 1);
    }

    @Override
    public void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        forger.mode = BlazeForgerMode.BY_ID.apply(value);
    }

    @Override
    public void setValue(int value) {
        this.value = Mth.clamp(value, 0, BlazeForgerMode.values().length - 1);
        forger.setMode(BlazeForgerMode.BY_ID.apply(this.value));
    }

    void syncFromMode(BlazeForgerMode mode) {
        value = mode.ordinal();
    }

    @Override
    public int getValue() {
        return forger.getMode().ordinal();
    }

    public static MutableComponent modeName(BlazeForgerMode mode) {
        return Component.translatable("create_enchantment_industry.gui.blaze_forger.mode." + mode.getSerializedName());
    }
}
