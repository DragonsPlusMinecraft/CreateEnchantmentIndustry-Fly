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

package plus.dragons.createenchantmentindustry.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.config.ConfigValue;
import com.zurrtum.create.catnip.config.FloatValue;
import com.zurrtum.create.catnip.config.IntValue;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Immutable wire representation of the server config values needed by client-side tooltips and recipe viewers.
 * Applying a snapshot changes only the in-memory backing values and never saves the remote values to disk.
 */
public record CEIServerConfigSnapshot(Map<String, JsonElement> values) {
    private static final Field BACKING_VALUE = findBackingValueField();

    public CEIServerConfigSnapshot {
        TreeMap<String, JsonElement> copy = new TreeMap<>();
        values.forEach((path, value) -> copy.put(path, value.deepCopy()));
        values = Map.copyOf(copy);
    }

    public static CEIServerConfigSnapshot capture(CEIServerConfig config) {
        Map<String, JsonElement> result = new TreeMap<>();
        collect(config, "", result);
        return new CEIServerConfigSnapshot(result);
    }

    public void applyTo(CEIServerConfig config) {
        Map<String, ConfigBase.CValue<?>> targets = new TreeMap<>();
        List<StressTarget> stressTargets = new ArrayList<>();
        collectTargets(config, "", targets, stressTargets);

        Map<String, JsonElement> expected = new TreeMap<>();
        targets.forEach((path, value) -> expected.put(path, encode(value.get())));
        stressTargets.forEach(target -> target.config().collectSnapshot(target.path(), expected));
        if (!expected.keySet().equals(values.keySet())) {
            TreeMap<String, Boolean> missing = new TreeMap<>();
            expected.keySet().stream().filter(key -> !values.containsKey(key)).forEach(key -> missing.put(key, true));
            TreeMap<String, Boolean> unexpected = new TreeMap<>();
            values.keySet().stream().filter(key -> !expected.containsKey(key)).forEach(key -> unexpected.put(key, true));
            throw new IllegalArgumentException(
                    "Server config snapshot schema mismatch; missing=" + missing.keySet() + ", unexpected=" + unexpected.keySet());
        }

        Map<ConfigBase.CValue<?>, Object> decoded = new LinkedHashMap<>();
        targets.forEach((path, target) -> decoded.put(target, decode(path, values.get(path), target)));
        List<Runnable> stressUpdates = stressTargets.stream()
                .map(target -> target.config().decodeSnapshot(target.path(), values))
                .toList();

        decoded.forEach(CEIServerConfigSnapshot::setWithoutSaving);
        stressUpdates.forEach(Runnable::run);
    }

    private static void collect(ConfigBase config, String path, Map<String, JsonElement> output) {
        if (config instanceof CEIStressConfig stress) {
            stress.collectSnapshot(path, output);
            return;
        }
        for (Field field : publicFields(config)) {
            Object value = get(field, config);
            String childPath = child(path, field.getName());
            if (value instanceof ConfigBase.CValue<?> configValue) {
                output.put(childPath, encode(configValue.get()));
            } else if (value instanceof ConfigBase child) {
                collect(child, childPath, output);
            }
        }
    }

    private static void collectTargets(
            ConfigBase config,
            String path,
            Map<String, ConfigBase.CValue<?>> values,
            List<StressTarget> stressTargets) {
        if (config instanceof CEIStressConfig stress) {
            stressTargets.add(new StressTarget(path, stress));
            return;
        }
        for (Field field : publicFields(config)) {
            Object value = get(field, config);
            String childPath = child(path, field.getName());
            if (value instanceof ConfigBase.CValue<?> configValue) {
                values.put(childPath, configValue);
            } else if (value instanceof ConfigBase child) {
                collectTargets(child, childPath, values, stressTargets);
            }
        }
    }

    private static List<Field> publicFields(ConfigBase config) {
        return List.of(config.getClass().getFields()).stream()
                .sorted(Comparator.comparing(Field::getName))
                .toList();
    }

    private static Object get(Field field, Object owner) {
        try {
            return field.get(owner);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot read config field " + field, exception);
        }
    }

    private static String child(String parent, String name) {
        return parent.isEmpty() ? name : parent + "." + name;
    }

    private static JsonElement encode(Object value) {
        return switch (value) {
            case Boolean bool -> new JsonPrimitive(bool);
            case Integer integer -> new JsonPrimitive(integer);
            case Float number -> new JsonPrimitive(number);
            case Double number -> new JsonPrimitive(number);
            case String string -> new JsonPrimitive(string);
            case Enum<?> enumeration -> new JsonPrimitive(enumeration.name());
            default -> throw new IllegalStateException("Unsupported server config value " + value);
        };
    }

    private static Object decode(String path, JsonElement element, ConfigBase.CValue<?> target) {
        if (element == null || !element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Server config value " + path + " must be a primitive");
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        Object current = target.get();
        if (current instanceof Boolean) {
            if (!primitive.isBoolean()) {
                throw wrongType(path, "boolean");
            }
            return primitive.getAsBoolean();
        }
        if (current instanceof Integer) {
            if (!primitive.isNumber()) {
                throw wrongType(path, "integer");
            }
            int value;
            try {
                value = primitive.getAsBigDecimal().intValueExact();
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("Server config value " + path + " is not an exact integer", exception);
            }
            if (backing(target) instanceof IntValue bounded && (value < bounded.min || value > bounded.max)) {
                throw new IllegalArgumentException("Server config value " + path + " is outside its allowed range");
            }
            return value;
        }
        if (current instanceof Float) {
            if (!primitive.isNumber()) {
                throw wrongType(path, "number");
            }
            double raw = primitive.getAsDouble();
            if (!Double.isFinite(raw) || raw < -Float.MAX_VALUE || raw > Float.MAX_VALUE) {
                throw new IllegalArgumentException("Server config value " + path + " is not a finite float");
            }
            float value = (float) raw;
            if (backing(target) instanceof FloatValue bounded && (value < bounded.min || value > bounded.max)) {
                throw new IllegalArgumentException("Server config value " + path + " is outside its allowed range");
            }
            return value;
        }
        if (current instanceof String) {
            if (!primitive.isString()) {
                throw wrongType(path, "string");
            }
            return primitive.getAsString();
        }
        if (current instanceof Enum<?> enumeration) {
            if (!primitive.isString()) {
                throw wrongType(path, "enum name");
            }
            String name = primitive.getAsString();
            for (Object constant : enumeration.getDeclaringClass().getEnumConstants()) {
                if (((Enum<?>) constant).name().equals(name)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Unknown enum value " + name + " for server config value " + path);
        }
        throw new IllegalStateException("Unsupported server config target " + path + " of type " + current.getClass());
    }

    private static IllegalArgumentException wrongType(String path, String expected) {
        return new IllegalArgumentException("Server config value " + path + " must be a " + expected);
    }

    @SuppressWarnings("unchecked")
    private static void setWithoutSaving(ConfigBase.CValue<?> target, Object value) {
        ((ConfigValue<Object>) backing(target)).set(value);
    }

    private static ConfigValue<?> backing(ConfigBase.CValue<?> target) {
        try {
            ConfigValue<?> backing = (ConfigValue<?>) BACKING_VALUE.get(target);
            if (backing == null) {
                throw new IllegalStateException("Server config value " + target.getName() + " has not been registered");
            }
            return backing;
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot access synchronized server config value " + target.getName(), exception);
        }
    }

    private static Field findBackingValueField() {
        try {
            Field field = ConfigBase.CValue.class.getDeclaredField("value");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private record StressTarget(String path, CEIStressConfig config) {}
}
