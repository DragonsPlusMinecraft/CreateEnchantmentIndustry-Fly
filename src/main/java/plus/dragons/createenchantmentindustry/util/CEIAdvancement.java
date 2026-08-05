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

package plus.dragons.createenchantmentindustry.util;

import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import plus.dragons.createdragonsplus.common.advancements.criterion.BuiltinTrigger;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** Runtime criterion handle and language metadata for a generated CEI advancement. */
public final class CEIAdvancement {
    private final Identifier id;
    private final String title;
    private final String description;
    private final BuiltinTrigger builtinTrigger = new BuiltinTrigger();

    public CEIAdvancement(String path, String title, String description) {
        this.id = CEICommon.asResource(path);
        this.title = title;
        this.description = description;
    }

    public Identifier id() {
        return id;
    }

    public BuiltinTrigger builtinTrigger() {
        return builtinTrigger;
    }

    public void awardTo(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            builtinTrigger.trigger(serverPlayer);
        }
    }

    public void provideLang(BiConsumer<String, String> consumer) {
        String key = "advancement." + id.getNamespace() + "." + id.getPath();
        consumer.accept(key, title);
        consumer.accept(key + ".desc", description);
    }
}
