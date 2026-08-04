package net.phoenixvine.essentials.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.phoenixvine.essentials.PhoenixEssentials;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EssentialsConfigOverrides {

    private static final String OVERRIDE_FILE_NAME = "phoenix_essentials-server-overrides.toml";

    private static final String GENERATED_FILE_HEADER = """
            # phoenix_essentials-server-overrides.toml
            #
            # Auto-generated on first load with every Phoenix Essentials server-config key set to
            # its CURRENT SHIPPED DEFAULT. Nothing here is an active override yet.
            #
            # To force a setting across every world/server this modpack is deployed to, edit the
            # value below away from its default and save the file. Only keys whose value differs
            # from this mod's current default are treated as active overrides - a line left exactly
            # as generated is ignored, so upgrading the mod and getting a new default for a key you
            # never touched works as expected.
            #
            # This file is re-applied every time the server config (re)loads, before any command or
            # feature reads a config value. It is never written to by the mod after this initial
            # generation, and the per-world serverconfig/phoenix_essentials-server.toml is never
            # touched by this mechanism either.
            """;

    private EssentialsConfigOverrides() {}

    public static void onLoading(ModConfigEvent.Loading event) {
        apply(event);
    }

    public static void onReloading(ModConfigEvent.Reloading event) {
        apply(event);
    }

    private static void apply(ModConfigEvent event) {
        if (event.getConfig().getSpec() != EssentialsServerConfig.SPEC) {
            return;
        }

        Path overridePath = FMLPaths.CONFIGDIR.get().resolve(OVERRIDE_FILE_NAME);
        ensureOverrideFileExists(overridePath);
        if (!Files.exists(overridePath)) {
            return;
        }

        Map<String, Object> flattened;
        try (CommentedFileConfig overrides = CommentedFileConfig.of(overridePath)) {
            overrides.load();
            flattened = new LinkedHashMap<>();
            flatten("", overrides, flattened);
        } catch (Exception e) {
            PhoenixEssentials.LOGGER.warn(
                "[{}] Could not read config override file {}: {}",
                PhoenixEssentials.MOD_ID, OVERRIDE_FILE_NAME, e.toString());
            return;
        }

        if (flattened.isEmpty()) {
            return;
        }

        Map<String, ForgeConfigSpec.ConfigValue<?>> valuesByPath = collectConfigValues();

        for (Map.Entry<String, Object> entry : flattened.entrySet()) {
            String path = entry.getKey();
            Object rawValue = entry.getValue();
            ForgeConfigSpec.ConfigValue<?> configValue = valuesByPath.get(path);
            if (configValue == null) {
                PhoenixEssentials.LOGGER.warn(
                    "[{}] Config override file {} references unknown config key '{}' - ignoring. Check for typos.",
                    PhoenixEssentials.MOD_ID, OVERRIDE_FILE_NAME, path);
                continue;
            }

            try {
                Object coerced = coerce(configValue, rawValue);
                Object currentDefault = configValue.getDefault();
                if (Objects.deepEquals(coerced, currentDefault)) {

                    continue;
                }

                setValue(configValue, coerced);
                PhoenixEssentials.LOGGER.info(
                    "[{}] Applied global config override: {} = {}", PhoenixEssentials.MOD_ID, path, coerced);
            } catch (Exception e) {
                PhoenixEssentials.LOGGER.warn(
                    "[{}] Failed to apply config override for '{}' from {}: {}",
                    PhoenixEssentials.MOD_ID, path, OVERRIDE_FILE_NAME, e.toString());
            }
        }
    }

    private static void ensureOverrideFileExists(Path overridePath) {
        if (Files.exists(overridePath)) {
            return;
        }

        try {
            Path parent = overridePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Map<String, ForgeConfigSpec.ConfigValue<?>> valuesByPath = collectConfigValues();
            try (CommentedFileConfig generated = CommentedFileConfig.of(overridePath)) {
                for (Map.Entry<String, ForgeConfigSpec.ConfigValue<?>> entry : valuesByPath.entrySet()) {
                    List<String> path = splitPath(entry.getKey());
                    ForgeConfigSpec.ConfigValue<?> configValue = entry.getValue();
                    generated.set(path, toTomlValue(configValue, configValue.getDefault()));

                    String comment = commentFor(path);
                    if (comment != null && !comment.isBlank()) {
                        generated.setComment(path, comment);
                    }
                }
                generated.save();
            }

            String body = Files.readString(overridePath, StandardCharsets.UTF_8);
            Files.writeString(overridePath, GENERATED_FILE_HEADER + "\n" + body, StandardCharsets.UTF_8);

            PhoenixEssentials.LOGGER.info(
                "[{}] Generated default config override file at {}", PhoenixEssentials.MOD_ID, overridePath);
        } catch (Exception e) {
            PhoenixEssentials.LOGGER.warn(
                "[{}] Could not generate default config override file {}: {}",
                PhoenixEssentials.MOD_ID, OVERRIDE_FILE_NAME, e.toString());
        }
    }

    private static Object toTomlValue(ForgeConfigSpec.ConfigValue<?> configValue, Object defaultValue) {
        if (configValue instanceof ForgeConfigSpec.EnumValue<?> && defaultValue instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return defaultValue;
    }

    private static String commentFor(List<String> path) {
        Object rawSpecEntry = EssentialsServerConfig.SPEC.getSpec().get(path);
        if (rawSpecEntry instanceof ForgeConfigSpec.ValueSpec valueSpec) {
            return valueSpec.getComment();
        }
        return null;
    }

    private static List<String> splitPath(String dottedPath) {
        return List.of(dottedPath.split("\\."));
    }

    private static void flatten(String prefix, UnmodifiableConfig config, Map<String, Object> out) {
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof UnmodifiableConfig nested) {
                flatten(key, nested, out);
            } else {
                out.put(key, value);
            }
        }
    }

    private static Map<String, ForgeConfigSpec.ConfigValue<?>> collectConfigValues() {
        Map<String, ForgeConfigSpec.ConfigValue<?>> map = new LinkedHashMap<>();
        collectConfigValues("", EssentialsServerConfig.SPEC.getValues(), map);
        return map;
    }

    private static void collectConfigValues(
            String prefix, UnmodifiableConfig values, Map<String, ForgeConfigSpec.ConfigValue<?>> out) {
        for (UnmodifiableConfig.Entry entry : values.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
                out.put(key, configValue);
            } else if (value instanceof UnmodifiableConfig nested) {
                collectConfigValues(key, nested, out);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void setValue(ForgeConfigSpec.ConfigValue<?> configValue, Object coerced) {
        ((ForgeConfigSpec.ConfigValue<Object>) configValue).set(coerced);
    }

    private static Object coerce(ForgeConfigSpec.ConfigValue<?> configValue, Object rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("override value is null");
        }

        if (configValue instanceof ForgeConfigSpec.BooleanValue) {
            if (rawValue instanceof Boolean b) {
                return b;
            }
            if (rawValue instanceof String s) {
                return Boolean.parseBoolean(s);
            }
            throw new IllegalArgumentException("expected a boolean, got " + rawValue.getClass().getSimpleName());
        }

        if (configValue instanceof ForgeConfigSpec.IntValue) {
            if (rawValue instanceof Number n) {
                return n.intValue();
            }
            throw new IllegalArgumentException("expected an integer, got " + rawValue.getClass().getSimpleName());
        }

        if (configValue instanceof ForgeConfigSpec.LongValue) {
            if (rawValue instanceof Number n) {
                return n.longValue();
            }
            throw new IllegalArgumentException("expected a long, got " + rawValue.getClass().getSimpleName());
        }

        if (configValue instanceof ForgeConfigSpec.DoubleValue) {
            if (rawValue instanceof Number n) {
                return n.doubleValue();
            }
            throw new IllegalArgumentException("expected a double, got " + rawValue.getClass().getSimpleName());
        }

        if (configValue instanceof ForgeConfigSpec.EnumValue<?> enumValue) {
            Enum<?> def = (Enum<?>) enumValue.getDefault();
            if (rawValue instanceof String s) {
                for (Object constant : def.getDeclaringClass().getEnumConstants()) {
                    if (((Enum<?>) constant).name().equalsIgnoreCase(s)) {
                        return constant;
                    }
                }
            }
            throw new IllegalArgumentException("no enum constant matching '" + rawValue + "'");
        }

        return rawValue;
    }
}
