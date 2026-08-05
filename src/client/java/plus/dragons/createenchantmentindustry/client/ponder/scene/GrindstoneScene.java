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

package plus.dragons.createenchantmentindustry.client.ponder.scene;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.ponder.api.PonderPalette;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.api.scene.SceneBuildingUtil;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import plus.dragons.createenchantmentindustry.client.ponder.CEIPonderScenes;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public class GrindstoneScene {
    public static void basic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_grindstone.intro", "Introduction to Mechanical Grindstone");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(4, 1, 2, 0, 1, 2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(50)
                .text("Right-click an Item Drain with Mechanical Grindstone...")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(20);
        scene.overlay().showControls(util.vector().centerOf(2, 2, 2), Pointing.DOWN, 50).rightClick().withItem(CEIBlocks.MECHANICAL_GRINDSTONE.asStack());
        scene.idle(20);
        scene.world().setBlock(util.grid().at(2, 1, 2), CEIBlocks.GRINDSTONE_DRAIN.getDefaultState().setValue(HorizontalKineticBlock.HORIZONTAL_FACING, Direction.SOUTH), true);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 2), GrindstoneDrainBlockEntity.class, SmartBlockEntity::markVirtual);
        scene.idle(30);
        scene.overlay().showText(60)
                .text("This is a Grindstone Drain. Place another Mechanical Grindstone on top to use it")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(30);
        scene.world().showSection(util.select().position(2, 2, 2), Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.GREEN, util.select().fromTo(2, 1, 2, 2, 2, 2), util.select().fromTo(2, 1, 2, 2, 2, 2), 30);
        scene.idle(40);
        scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 2, 1)
                .add(util.select().fromTo(0, 1, 3, 4, 2, 4)), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().everywhere(), -64);
        scene.world().setKineticSpeed(util.select().fromTo(2, 2, 2, 2, 2, 3), 64);
        scene.overlay().showText(60)
                .text("Experience in item form can be crushed into liquid form by Mechanical Grindstone")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 2, 2));
        for (int i = 0; i < 4; i++) {
            scene.world().createItemOnBelt(util.grid().at(0, 1, 2), Direction.UP, new ItemStack(i < 2 ? AllItems.EXP_NUGGET : AllBlocks.EXPERIENCE_BLOCK));
            scene.idle(20);
        }
    }

    public static void extra(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_grindstone.extra", "Sanding with Mechanical Grindstone");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(60)
                .text("Mechanical Grindstone not only has all the features of a grindstone...")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 2, 2));
        scene.world().setKineticSpeed(util.select().everywhere(), 128);
        scene.world().setKineticSpeed(util.select().fromTo(0, 1, 0, 3, 1, 0).add(util.select().position(2, 2, 2)), -128);
        scene.idle(10);
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        CEIPonderScenes.enchant(scene, sword, Enchantments.SWEEPING_EDGE, 3);
        scene.world().createItemOnBelt(util.grid().at(0, 1, 0), Direction.UP, sword);
        scene.idle(60);

        scene.overlay().showText(60)
                .text("...but it also has the features of Sand Paper")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(10);
        scene.world().createItemOnBelt(
                util.grid().at(0, 1, 0), Direction.UP, AllItems.ROSE_QUARTZ.getDefaultInstance());
        scene.idle(60);

        scene.overlay().showText(60)
                .text("Items can be manually applied to Mechanical Grindstone")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(60);
    }
}
