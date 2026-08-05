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

package plus.dragons.createenchantmentindustry.common.advancement;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createdragonsplus.common.advancements.criterion.BuiltinTrigger;

/** Tracks the placing player for CEI machines and awards their runtime triggers/statistics. */
public final class AdvancementBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
    public static final BehaviourType<AdvancementBehaviour> TYPE = new BehaviourType<>();
    private static final int MAX_DISTANCE = 16;

    private UUID playerId;

    public AdvancementBehaviour(SmartBlockEntity blockEntity) {
        super(blockEntity);
    }

    public void trigger(BuiltinTrigger trigger) {
        ServerPlayer player = getPlayerIfNear();
        if (player != null) {
            trigger.trigger(player);
        }
    }

    public void awardStat(Identifier stat, int amount) {
        ServerPlayer player = getPlayerIfNear();
        if (player != null && amount != 0) {
            player.awardStat(Stats.CUSTOM.get(stat), amount);
        }
    }

    private ServerPlayer getPlayerIfNear() {
        if (playerId == null || !blockEntity.hasLevel()) {
            return null;
        }
        var player = blockEntity.getLevel().getPlayerByUUID(playerId);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return null;
        }
        return player.distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos())) <= MAX_DISTANCE * MAX_DISTANCE
                ? serverPlayer
                : null;
    }

    @Override
    public void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        if (playerId != null) {
            output.store("Owner", UUIDUtil.CODEC, playerId);
        }
    }

    @Override
    public void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        playerId = input.read("Owner", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public static void setPlacedBy(Level level, BlockPos pos, LivingEntity placer) {
        if (level.isClientSide() || !(placer instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AdvancementBehaviour behaviour = get(level, pos);
        if (behaviour != null) {
            behaviour.playerId = serverPlayer.getUUID();
            behaviour.blockEntity.setChanged();
        }
    }

    private static AdvancementBehaviour get(BlockGetter level, BlockPos pos) {
        return BlockEntityBehaviour.get(level, pos, TYPE);
    }
}
