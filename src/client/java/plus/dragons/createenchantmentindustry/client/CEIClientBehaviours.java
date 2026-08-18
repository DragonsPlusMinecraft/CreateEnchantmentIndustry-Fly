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

package plus.dragons.createenchantmentindustry.client;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.logistics.itemHatch.HatchFilterSlot;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlock;
import plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicEnchanterBehaviour;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchanterBehaviour;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerMode;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerModeBehaviour;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;
import plus.dragons.createenchantmentindustry.util.CEILang;

/** Client-only value boxes paired with the server-safe behaviours in the common source set. */
public final class CEIClientBehaviours {
    private CEIClientBehaviours() {}

    private static void faceSlotTowardsHit(ValueBoxTransform slot, BlockPos pos) {
        if (slot instanceof ValueBoxTransform.Sided sided
                && Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult
                && hitResult.getBlockPos().equals(pos)) {
            sided.fromSide(hitResult.getDirection());
        }
    }

    public static void register() {
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.BLAZE_ENCHANTER.get(), EnchanterClientBehaviour::new);
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.BLAZE_FORGER.get(), ForgerClientBehaviour::new);
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.EXPERIENCE_HATCH.get(), ExperienceHatchClientBehaviour::new);
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.PRINTER.get(),
                blockEntity -> new FilteringBehaviour<PrinterBehaviour>(
                        blockEntity,
                        new CenteredSideValueBoxTransform(
                                (state, direction) -> state.getValue(PrinterBlock.FACING) == direction)));
        BlockEntityBehaviour.addClient(
                CEIBlockEntities.CLASSIC_BLAZE_ENCHANTER.get(),
                blockEntity -> new FilteringBehaviour<ClassicEnchanterBehaviour>(
                        blockEntity, new HorizontalBlazeSlot()));
    }

    private static final class HorizontalBlazeSlot extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 13.5);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }

    private static final class EnchanterClientBehaviour
            extends ScrollValueBehaviour<BlazeEnchanterBlockEntity, EnchanterBehaviour> {
        private EnchanterClientBehaviour(BlazeEnchanterBlockEntity blockEntity) {
            super(
                    CEILang.translate("gui.blaze_enchanter.level").component(),
                    blockEntity,
                    new HorizontalBlazeSlot());
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            int max = blockEntity.getMaxEnchantLevel();
            return new ValueSettingsBoard(
                    label,
                    max,
                    Math.max(1, max / 6),
                    List.of(label),
                    new ValueSettingsFormatter());
        }

        @Override
        public boolean testHit(Vec3 hit) {
            faceSlotTowardsHit(getSlotPositioning(), getPos());
            return super.testHit(hit);
        }
    }

    private static final class ForgerClientBehaviour
            extends ScrollValueBehaviour<BlazeForgerBlockEntity, BlazeForgerModeBehaviour> {
        private ForgerClientBehaviour(BlazeForgerBlockEntity blockEntity) {
            super(
                    CEILang.translate("gui.blaze_forger.mode_selector").component(),
                    blockEntity,
                    new HorizontalBlazeSlot());
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            return new ValueSettingsBoard(
                    label,
                    BlazeForgerMode.values().length - 1,
                    1,
                    List.of(label),
                    new ValueSettingsFormatter(settings -> BlazeForgerModeBehaviour.modeName(
                            BlazeForgerMode.BY_ID.apply(settings.value()))));
        }

        @Override
        public String formatValue() {
            return BlazeForgerModeBehaviour.modeName(blockEntity.getMode()).getString();
        }

        @Override
        public boolean testHit(Vec3 hit) {
            faceSlotTowardsHit(getSlotPositioning(), getPos());
            return super.testHit(hit);
        }
    }

    private static final class ExperienceHatchClientBehaviour
            extends FilteringBehaviour<ExperienceHatchBehaviour> {
        private ExperienceHatchClientBehaviour(ExperienceHatchBlockEntity blockEntity) {
            super(blockEntity, new HatchFilterSlot());
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            return new ValueSettingsBoard(
                    CEILang.translate("gui.experience_hatch.exchange").component(),
                    100,
                    10,
                    List.of(CEILang.translate("gui.experience_hatch.points").component()),
                    new ValueSettingsFormatter(this::formatValue));
        }

        @Override
        public MutableComponent formatValue(
                com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings settings) {
            int count = settings.value();
            if (count == 0)
                return CEILang.translate("gui.experience_hatch.all").component();
            return Component.literal(
                    String.valueOf(count * ExperienceHatchBehaviour.POINTS_PER_SCROLL));
        }

        @Override
        public MutableComponent getCountLabelForValueBox() {
            int count = behaviour.getValueSettings().value();
            if (count == 0)
                return Component.literal("*");
            return Component.literal(
                    String.valueOf(count * ExperienceHatchBehaviour.POINTS_PER_SCROLL));
        }
    }
}
