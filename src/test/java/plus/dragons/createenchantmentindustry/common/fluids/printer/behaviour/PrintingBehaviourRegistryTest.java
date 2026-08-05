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

package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PrintingBehaviourRegistryTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void ordersByDescendingPriorityThenIdentifier() {
        PrintingBehaviourRegistry.Dispatcher dispatcher = new PrintingBehaviourRegistry.Dispatcher();
        dispatcher.register(id("z_default"), PrintingBehaviourRegistry.DEFAULT_PRIORITY, empty());
        dispatcher.register(id("z_builtin"), PrintingBehaviourRegistry.BUILTIN_PRIORITY, empty());
        dispatcher.register(id("a_builtin"), PrintingBehaviourRegistry.BUILTIN_PRIORITY, empty());
        dispatcher.register(id("external"), 2_000, empty());

        assertEquals(
                List.of(id("external"), id("a_builtin"), id("z_builtin"), id("z_default")),
                dispatcher.orderedIds());
    }

    @Test
    void presentErrorClaimsTemplateAndStopsLookup() {
        PrintingBehaviourRegistry.Dispatcher dispatcher = new PrintingBehaviourRegistry.Dispatcher();
        List<String> calls = new ArrayList<>();
        dispatcher.register(id("first"), 20, (level, tank, stack) -> {
            calls.add("first");
            return Optional.empty();
        });
        dispatcher.register(id("claim"), 10, (level, tank, stack) -> {
            calls.add("claim");
            return Optional.of(DataResult.error(() -> "claimed"));
        });
        dispatcher.register(id("never"), 0, (level, tank, stack) -> {
            calls.add("never");
            return Optional.empty();
        });

        DataResult<PrintingBehaviour> result = dispatcher.create(null, null, ItemStack.EMPTY);
        assertTrue(result.error().isPresent());
        assertEquals(List.of("first", "claim"), calls);
    }

    @Test
    void recipePrintingIsFinalFallback() {
        PrintingBehaviourRegistry.Dispatcher dispatcher = new PrintingBehaviourRegistry.Dispatcher();
        dispatcher.register(id("empty"), 100, empty());

        PrintingBehaviour result = dispatcher.create(null, null, ItemStack.EMPTY).getOrThrow();
        assertInstanceOf(RecipePrintingBehaviour.class, result);
    }

    @Test
    void rejectsDuplicateNullAndPostFreezeRegistration() {
        PrintingBehaviourRegistry.Dispatcher dispatcher = new PrintingBehaviourRegistry.Dispatcher();
        dispatcher.register(id("same"), 0, empty());
        assertThrows(IllegalArgumentException.class, () -> dispatcher.register(id("same"), 1, empty()));

        dispatcher.register(id("null_result"), 0, (level, tank, stack) -> null);
        assertThrows(NullPointerException.class, () -> dispatcher.create(null, null, ItemStack.EMPTY));

        dispatcher.freeze();
        assertTrue(dispatcher.frozen());
        assertThrows(IllegalStateException.class, () -> dispatcher.register(id("late"), 0, empty()));
    }

    private static PrintingBehaviour.Provider empty() {
        return (level, tank, stack) -> Optional.empty();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }
}
