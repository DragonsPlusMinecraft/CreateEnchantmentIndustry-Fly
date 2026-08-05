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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CEITransferTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void convertsMillibucketsWithoutLosingUnits() {
        assertEquals(FluidConstants.BUCKET, CEIFluidUnits.millibuckets(1_000));
        assertEquals(4_000, CEIFluidUnits.toMillibuckets(CEIFluidUnits.millibuckets(4_000)));
        assertEquals(0, CEIFluidUnits.millibuckets(0));
        assertThrows(ArithmeticException.class, () -> CEIFluidUnits.millibuckets(Long.MAX_VALUE));
    }

    @Test
    void transactionsCommitRollbackAndRequireExactAmounts() {
        TestStorage storage = new TestStorage(100);
        FluidVariant water = FluidVariant.of(Fluids.WATER);

        assertEquals(60, CEITransfer.insert(storage, water, 60, true));
        assertEquals(0, storage.amount);
        assertEquals(60, CEITransfer.insert(storage, water, 60, false));
        assertEquals(60, storage.amount);

        assertFalse(CEITransfer.insertExact(storage, water, 50, false));
        assertEquals(60, storage.amount);
        assertTrue(CEITransfer.insertExact(storage, water, 40, false));
        assertEquals(100, storage.amount);

        assertEquals(25, CEITransfer.extract(storage, water, 25, true));
        assertEquals(100, storage.amount);
        assertFalse(CEITransfer.extractExact(storage, water, 101, false));
        assertEquals(100, storage.amount);
        assertTrue(CEITransfer.extractExact(storage, water, 100, false));
        assertEquals(0, storage.amount);
    }

    private static final class TestStorage extends SingleVariantStorage<FluidVariant> {
        private final long capacity;

        private TestStorage(long capacity) {
            this.capacity = capacity;
        }

        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return capacity;
        }
    }
}
