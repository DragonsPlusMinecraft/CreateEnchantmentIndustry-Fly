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

package plus.dragons.createenchantmentindustry.data;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import plus.dragons.createenchantmentindustry.common.CEICommon;

/** Generates the core client assets without depending on Registrate datagen. */
public final class CEIAssetProvider implements DataProvider {
    private static final String NAMESPACE = CEICommon.ID;
    private static final String CREATE_BLAZE_MODEL = "create:block/blaze_burner/block";
    private static final String CREATE_BLAZE_ITEM_MODEL = "create:block/blaze_burner/block_with_blaze";
    private static final List<String> STANDARD_ITEMS = List.of(
            "mechanical_grindstone",
            "grindstone_drain",
            "experience_hatch",
            "printer",
            "super_experience_block",
            "experience_lantern",
            "super_experience_nugget",
            "enchanting_template",
            "super_enchanting_template",
            "blazes_enchanting_handbook",
            "experience_cake_base",
            "experience_cake",
            "experience_cake_slice",
            "experience_bucket");

    private final Path root;

    public CEIAssetProvider(PackOutput output) {
        root = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(NAMESPACE);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        writeBlockStates(writes, output);
        writeModels(writes, output);
        writeItemDefinitions(writes, output);
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private void writeBlockStates(List<CompletableFuture<?>> writes, CachedOutput output) {
        JsonObject blaze = blazeBlockState();
        save(writes, output, "blockstates/blaze_enchanter.json", blaze);
        save(writes, output, "blockstates/blaze_forger.json", blaze);
        save(writes, output, "blockstates/classic_blaze_enchanter.json", blaze);
        save(writes, output, "blockstates/experience.json", simpleBlockState(model("block/experience")));
        save(writes, output, "blockstates/experience_hatch.json", experienceHatchBlockState());
        save(writes, output, "blockstates/experience_lantern.json", experienceLanternBlockState());
        save(
                writes,
                output,
                "blockstates/grindstone_drain.json",
                horizontalBlockState(model("block/grindstone_drain/block")));
        save(writes, output, "blockstates/mechanical_grindstone.json", axisBlockState());
        save(
                writes,
                output,
                "blockstates/printer.json",
                horizontalBlockState(model("block/printer/block")));
        save(
                writes,
                output,
                "blockstates/super_experience_block.json",
                simpleBlockState(model("block/super_experience_block")));
    }

    private void writeModels(List<CompletableFuture<?>> writes, CachedOutput output) {
        JsonObject experience = new JsonObject();
        JsonObject experienceTextures = new JsonObject();
        experienceTextures.addProperty("particle", model("fluid/experience_still"));
        experience.add("textures", experienceTextures);
        save(writes, output, "models/block/experience.json", experience);

        JsonObject superExperience = parentModel("create:block/experience_block");
        JsonObject superTextures = new JsonObject();
        superTextures.addProperty("all", model("block/super_experience_block"));
        superTextures.addProperty("particle", model("block/super_experience_block"));
        superExperience.add("textures", superTextures);
        save(writes, output, "models/block/super_experience_block.json", superExperience);

        save(writes, output, "models/item/mechanical_grindstone.json", parentModel(model("block/mechanical_grindstone")));
        save(writes, output, "models/item/grindstone_drain.json", parentModel(model("block/grindstone_drain/item")));
        save(writes, output, "models/item/experience_hatch.json", parentModel(model("block/experience_hatch")));
        save(writes, output, "models/item/printer.json", parentModel(model("block/printer/item")));
        save(writes, output, "models/item/super_experience_block.json", parentModel(model("block/super_experience_block")));
        save(writes, output, "models/item/experience_lantern.json", parentModel(model("block/experience_lantern")));
        save(writes, output, "models/item/blaze_enchanter.json", parentModel(CREATE_BLAZE_ITEM_MODEL));
        save(writes, output, "models/item/blaze_forger.json", parentModel(CREATE_BLAZE_ITEM_MODEL));
        save(writes, output, "models/item/classic_blaze_enchanter.json", parentModel(CREATE_BLAZE_ITEM_MODEL));

        for (String item : List.of(
                "super_experience_nugget",
                "enchanting_template",
                "super_enchanting_template",
                "blazes_enchanting_handbook",
                "experience_cake_base",
                "experience_cake",
                "experience_cake_slice",
                "experience_bucket")) {
            JsonObject generated = parentModel("minecraft:item/generated");
            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", model("item/" + item));
            generated.add("textures", textures);
            save(writes, output, "models/item/" + item + ".json", generated);
        }
    }

    private void writeItemDefinitions(List<CompletableFuture<?>> writes, CachedOutput output) {
        for (String item : STANDARD_ITEMS) {
            JsonObject definition = new JsonObject();
            JsonObject itemModel = new JsonObject();
            itemModel.addProperty("type", "minecraft:model");
            itemModel.addProperty("model", model("item/" + item));
            definition.add("model", itemModel);
            save(writes, output, "items/" + item + ".json", definition);
        }
        save(
                writes,
                output,
                "items/blaze_enchanter.json",
                blazeItemDefinition("blaze_enchanter", "block/blaze/enchanter_hat"));
        save(
                writes,
                output,
                "items/blaze_forger.json",
                blazeItemDefinition("blaze_forger", "block/blaze/forger_hat"));
        JsonObject classic = blazeItemDefinition("classic_blaze_enchanter", null);
        classic.getAsJsonObject("model").addProperty("book", true);
        save(writes, output, "items/classic_blaze_enchanter.json", classic);
    }

    private static JsonObject blazeItemDefinition(String item, String hat) {
        JsonObject definition = new JsonObject();
        JsonObject itemModel = new JsonObject();
        itemModel.addProperty("type", model("model/blaze_device"));
        itemModel.addProperty("model", model("item/" + item));
        if (hat != null) {
            itemModel.addProperty("hat", model(hat));
        }
        definition.add("model", itemModel);
        return definition;
    }

    private static JsonObject blazeBlockState() {
        JsonObject variants = new JsonObject();
        for (String blaze : List.of("smouldering", "fading", "kindled", "seething")) {
            for (DirectionRotation direction : DirectionRotation.HORIZONTAL) {
                variants.add(
                        "blaze=" + blaze + ",facing=" + direction.name,
                        variant(CREATE_BLAZE_MODEL, 0, direction.y));
            }
        }
        return variants(variants);
    }

    private static JsonObject experienceHatchBlockState() {
        JsonObject variants = new JsonObject();
        for (DirectionRotation direction : DirectionRotation.HORIZONTAL) {
            for (boolean waterlogged : List.of(false, true)) {
                variants.add(
                        "facing=" + direction.name + ",waterlogged=" + waterlogged,
                        variant(model("block/experience_hatch"), 0, direction.y));
            }
        }
        return variants(variants);
    }

    private static JsonObject experienceLanternBlockState() {
        JsonObject variants = new JsonObject();
        for (DirectionRotation direction : DirectionRotation.ALL) {
            for (int light = 0; light <= 15; light++) {
                variants.add(
                        "facing=" + direction.name + ",light=" + light,
                        variant(model("block/experience_lantern"), direction.x, direction.y));
            }
        }
        return variants(variants);
    }

    private static JsonObject horizontalBlockState(String model) {
        JsonObject variants = new JsonObject();
        for (DirectionRotation direction : DirectionRotation.HORIZONTAL) {
            variants.add("facing=" + direction.name, variant(model, 0, direction.y));
        }
        return variants(variants);
    }

    private static JsonObject axisBlockState() {
        JsonObject variants = new JsonObject();
        variants.add("axis=x", variant(model("block/mechanical_grindstone"), 90, 90));
        variants.add("axis=y", variant(model("block/mechanical_grindstone"), 0, 0));
        variants.add("axis=z", variant(model("block/mechanical_grindstone"), 90, 180));
        return variants(variants);
    }

    private static JsonObject simpleBlockState(String model) {
        JsonObject variants = new JsonObject();
        variants.add("", variant(model, 0, 0));
        return variants(variants);
    }

    private static JsonObject variants(JsonObject variants) {
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root;
    }

    private static JsonObject variant(String model, int x, int y) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", model);
        if (x != 0) {
            variant.addProperty("x", x);
        }
        if (y != 0) {
            variant.addProperty("y", y);
        }
        return variant;
    }

    private static JsonObject parentModel(String parent) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", parent);
        return model;
    }

    private static String model(String path) {
        return NAMESPACE + ":" + path;
    }

    private void save(
            List<CompletableFuture<?>> writes,
            CachedOutput output,
            String relativePath,
            JsonObject json) {
        writes.add(DataProvider.saveStable(output, json, root.resolve(relativePath)));
    }

    @Override
    public String getName() {
        return "Create: Enchantment Industry Core Assets";
    }

    private record DirectionRotation(String name, int x, int y) {
        private static final List<DirectionRotation> HORIZONTAL = List.of(
                new DirectionRotation("north", 0, 0),
                new DirectionRotation("east", 0, 90),
                new DirectionRotation("south", 0, 180),
                new DirectionRotation("west", 0, 270));
        private static final List<DirectionRotation> ALL = List.of(
                new DirectionRotation("down", 180, 0),
                new DirectionRotation("up", 0, 0),
                new DirectionRotation("north", 90, 0),
                new DirectionRotation("east", 90, 90),
                new DirectionRotation("south", 90, 180),
                new DirectionRotation("west", 90, 270));
    }
}
