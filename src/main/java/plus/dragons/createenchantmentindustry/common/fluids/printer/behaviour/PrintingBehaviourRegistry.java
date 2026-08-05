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

import com.mojang.serialization.DataResult;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Lifecycle-bound, deterministic registry for Printer behaviour providers. */
public final class PrintingBehaviourRegistry {
    public static final int DEFAULT_PRIORITY = 0;
    public static final int BUILTIN_PRIORITY = 1000;
    private static final Dispatcher DISPATCHER = new Dispatcher();

    private PrintingBehaviourRegistry() {}

    public static void register(Identifier id, PrintingBehaviour.Provider provider) {
        register(id, DEFAULT_PRIORITY, provider);
    }

    public static void register(Identifier id, int priority, PrintingBehaviour.Provider provider) {
        DISPATCHER.register(id, priority, provider);
    }

    /** Freezes provider order permanently. Called during the first server start. */
    public static void freeze() {
        DISPATCHER.freeze();
    }

    static DataResult<PrintingBehaviour> create(
            Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
        return DISPATCHER.create(level, tank, stack);
    }

    /** Package-visible instance form keeps ordering/freeze semantics independently testable. */
    static final class Dispatcher {
        private static final Comparator<Entry> ORDER = Comparator
                .comparingInt(Entry::priority)
                .reversed()
                .thenComparing(entry -> entry.id().toString());

        private final Map<Identifier, Entry> entries = new HashMap<>();
        private volatile List<Entry> ordered = List.of();
        private boolean frozen;

        synchronized void register(Identifier id, int priority, PrintingBehaviour.Provider provider) {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(provider, "provider");
            if (frozen) {
                throw new IllegalStateException(
                        "Printing behaviour registry is frozen; cannot register " + id);
            }
            Entry entry = new Entry(id, priority, provider);
            if (entries.putIfAbsent(id, entry) != null) {
                throw new IllegalArgumentException("Duplicate printing behaviour id " + id);
            }
            ordered = sortedEntries();
        }

        synchronized void freeze() {
            if (!frozen) {
                ordered = sortedEntries();
                frozen = true;
            }
        }

        private List<Entry> sortedEntries() {
            List<Entry> result = new ArrayList<>(entries.values());
            result.sort(ORDER);
            return List.copyOf(result);
        }

        DataResult<PrintingBehaviour> create(
                Level level, SmartFluidTankBehaviour tank, ItemStack stack) {
            for (Entry entry : ordered) {
                var claimed = Objects.requireNonNull(
                        entry.provider().create(level, tank, stack),
                        () -> "Printing behaviour provider " + entry.id() + " returned null");
                if (claimed.isPresent()) {
                    return claimed.get();
                }
            }
            return DataResult.success(new RecipePrintingBehaviour(stack));
        }

        List<Identifier> orderedIds() {
            return ordered.stream().map(Entry::id).toList();
        }

        boolean frozen() {
            return frozen;
        }
    }

    private record Entry(Identifier id, int priority, PrintingBehaviour.Provider provider) {
        private Entry {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(provider, "provider");
        }
    }
}
