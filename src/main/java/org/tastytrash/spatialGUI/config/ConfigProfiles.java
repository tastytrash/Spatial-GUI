package org.tastytrash.spatialGUI.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.tastytrash.spatialGUI.SpatialGUI;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ConfigProfiles {
    public static final List<String> PROFILE_FIELDS = List.of(
            "viewMode",
            "firstPersonScreenScale", "screenScale",
            "firstPersonScreenSideOffset", "screenSideOffset",
            "firstPersonScreenHeightOffset", "screenHeightOffset",
            "firstPersonScreenDistance", "screenDistance",
            "curvedScreenEnabled", "curvedScreenArcDegrees");

    private static final Gson GSON = new Gson();
    private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, Profile> profiles;
    private final Rules rules;
    private String linked;

    record Profile(Map<Field, Object> setting) {
        void applyTo(SpatialGUIConfig config) throws IllegalAccessException {
            for (var e : setting.entrySet()) e.getKey().set(config, e.getValue());
        }
    }

    record Rules(Map<String, String> rules) {
        String profileFor(String screenClass, String category) {
            String name = rules.get(screenClass);
            if (name == null) name = rules.get(category);
            return name != null ? name : rules.get("#all");
        }

        boolean isEmpty() {
            return rules.isEmpty();
        }
    }

    private ConfigProfiles(Map<String, Profile> profiles, Rules rules) {
        this.profiles = new LinkedHashMap<>(profiles);
        this.rules = new Rules(new LinkedHashMap<>(rules.rules()));
    }

    public static ConfigProfiles empty() {
        return new ConfigProfiles(Map.of(), new Rules(Map.of()));
    }

    public boolean isEmpty() {
        return profiles.isEmpty() || rules.isEmpty();
    }

    public String summary() {
        return profiles.size() + " profile(s)";
    }

    public List<String> names() {
        return new ArrayList<>(profiles.keySet());
    }

    public String addNext(SpatialGUIConfig source) {
        int number = profiles.size() + 1;
        while (profiles.containsKey("Profile " + number)) number++;
        String name = "Profile " + number;
        profiles.put(name, new Profile(profileSettingsOf(source)));
        return name;
    }

    public boolean addDefaultIfMissing(SpatialGUIConfig global) {
        if (global == null || (linked != null && profiles.containsKey(linked))) return false;
        setLinked(addNext(global));
        return true;
    }

    public boolean rename(String oldName, String newName) {
        if (!profiles.containsKey(oldName)) return false;
        if (oldName.equals(newName)) return true;
        if (profiles.containsKey(newName)) return false;

        Map<String, Profile> reordered = new LinkedHashMap<>();
        for (var e : profiles.entrySet()) {
            reordered.put(e.getKey().equals(oldName) ? newName : e.getKey(), e.getValue());
        }
        profiles.clear();
        profiles.putAll(reordered);
        rules.rules().replaceAll((target, profile) -> profile.equals(oldName) ? newName : profile);
        if (oldName.equals(linked)) linked = newName;
        return true;
    }

    public boolean remove(String name) {
        if (name.equals(linked) || !profiles.containsKey(name)) return false;
        profiles.remove(name);
        rules.rules().values().removeIf(name::equals);
        return true;
    }

    public String linked() {
        return linked;
    }

    public void setLinked(String name) {
        linked = name;
        if (name == null) rules.rules().remove("#all");
        else rules.rules().put("#all", name);
    }

    public void refreshProfile(String name, SpatialGUIConfig config) {
        Profile profile = profiles.get(name);
        if (profile != null) profile.setting().putAll(profileSettingsOf(config));
    }

    public String getScreenProfile(String screenClass) {
        return rules.rules().get(screenClass);
    }

    public void setScreenProfile(String screenClass, String profile) {
        if (profile == null) rules.rules().remove(screenClass);
        else rules.rules().put(screenClass, profile);
    }

    public static void copyProfileSettings(SpatialGUIConfig from, SpatialGUIConfig to) {
        try {
            for (String name : PROFILE_FIELDS) {
                Field field = SpatialGUIConfig.class.getField(name);
                field.set(to, field.get(from));
            }
        } catch (ReflectiveOperationException e) {
            SpatialGUI.LOGGER.error("Couldn't copy layout settings: {}", e.getMessage());
        }
    }

    public SpatialGUIConfig getProfileConfig(String name, SpatialGUIConfig global) {
        if (name.equals(linked) && profiles.containsKey(name)) return global;
        Profile values = profiles.get(name);
        return values == null ? null : withProfile(global, values);
    }

    public SpatialGUIConfig getProfileForScreen(String screenClass, String category, SpatialGUIConfig global) {
        String name = rules.profileFor(screenClass, category);
        if (name == null) return null;
        return getProfileConfig(name, global);
    }

    private static SpatialGUIConfig withProfile(SpatialGUIConfig global, Profile values) {
        SpatialGUIConfig copy = new SpatialGUIConfig();
        try {
            for (Field f : SpatialGUIConfig.class.getFields()) {
                // HANDLER is static & final, modifying it would throw
                if (f.getName().equals("HANDLER")) continue;
                f.set(copy, f.get(global));
            }
            values.applyTo(copy);
        } catch (IllegalAccessException e) {
            return null;
        }
        return copy;
    }

    private static Map<Field, Object> profileSettingsOf(SpatialGUIConfig source) {
        Map<Field, Object> values = new LinkedHashMap<>();
        try {
            for (String name : PROFILE_FIELDS) {
                Field field = SpatialGUIConfig.class.getField(name);
                values.put(field, field.get(source));
            }
        } catch (ReflectiveOperationException e) {
            SpatialGUI.LOGGER.error("Couldn't read layout settings: {}", e.getMessage());
        }
        return values;
    }

    public static ConfigProfiles load(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            Files.writeString(path, TEMPLATE);
        }
        return parse(Files.readString(path));
    }

    public void save(Path path) throws IOException {
        JsonObject profilesObj = new JsonObject();
        for (var profile : profiles.entrySet()) {
            JsonObject values = new JsonObject();
            for (var setting : profile.getValue().setting().entrySet()) {
                values.add(setting.getKey().getName(), GSON.toJsonTree(setting.getValue()));
            }
            profilesObj.add(profile.getKey(), values);
        }

        JsonObject rulesObj = new JsonObject();
        rules.rules().forEach(rulesObj::addProperty);

        JsonObject file = new JsonObject();
        file.add("profiles", profilesObj);
        file.add("rules", rulesObj);
        if (linked != null) file.addProperty("default", linked);

        Files.createDirectories(path.getParent());
        Files.writeString(path, HEADER + PRETTY_GSON.toJson(file) + "\n");
    }

    public static ConfigProfiles parse(String text) {
        JsonObject obj = JsonParser.parseString(text).getAsJsonObject();

        Map<String, Profile> profiles = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : obj.getAsJsonObject("profiles").entrySet()) {
            profiles.put(e.getKey(), new Profile(convert(e.getKey(), e.getValue().getAsJsonObject())));
        }

        Map<String, String> rules = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : obj.getAsJsonObject("rules").entrySet()) {
            String profileName = e.getValue().getAsString();
            if (!profiles.containsKey(profileName)) {
                SpatialGUI.LOGGER.warn("'{}' is assigned to unknown profile '{}'", e.getKey(), profileName);
            }
            rules.put(e.getKey(), profileName);
        }

        String linked = obj.has("default") ? obj.get("default").getAsString() : null;
        if (linked != null && !profiles.containsKey(linked)) {
            SpatialGUI.LOGGER.warn("default profile '{}' doesn't exist", linked);
            linked = null;
        }
        if (linked != null) rules.put("#all", linked);

        ConfigProfiles result = new ConfigProfiles(profiles, new Rules(rules));
        result.linked = linked;
        return result;
    }

    private static Map<Field, Object> convert(String profile, JsonObject profileObj) {
        if (profileObj.has("guiScale") && !profileObj.has("overrideGuiScale")) {
            profileObj.addProperty("overrideGuiScale", true);
        }

        Map<Field, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> e : profileObj.entrySet()) {
            String key = e.getKey();

            try {
                Field field = SpatialGUIConfig.class.getField(key);
                // HANDLER is static & final, modifying it would throw
                if (field.getName().equals("HANDLER")) continue;

                Object value = GSON.fromJson(e.getValue(), field.getType());
                if (value == null) {
                    SpatialGUI.LOGGER.warn("profile '{}': no usable value for '{}'", profile, key);
                    continue;
                }
                values.put(field, value);
            } catch (NoSuchFieldException ex) {
                SpatialGUI.LOGGER.warn("profile '{}': unknown setting '{}'", profile, key);
            } catch (RuntimeException ex) {
                SpatialGUI.LOGGER.warn("profile '{}': couldn't read value for '{}'", profile, key);
            }
        }
        return values;
    }

    private static final String HEADER = """
            // Spatial GUI screen profiles
            //
            // A profile is a group of settings overrides. This is mainly for modpack creators or advanced users.
            // Any other setting not included in a profile uses your normal Spatial GUI config settings.
            //
            // "rules" decides which screens use which profile. For each screen, the first match overrides:
            //   1. exact class name   "net.minecraft.client.gui.screens.inventory.CraftingScreen"
            //   2. category           "#inventory", "#containers" or "#pause"
            //   3. "#all"             every screen not included above
            //
            // Setting names are the same as in spatial-gui.json5. Setting "guiScale" automatically turns on "overrideGuiScale".
            //
            // You can find exact class names in the 'seenScreens' list in the config,
            // either in game under the 'Screens' tab or in spatial-gui.json5
            //
            // Example, remove the // to use:
            // {
            //   profiles: {
            //     compact: { guiScale: 3, firstPersonScreenScale: 1.2 },
            //     wide:    { autoDetectCameraMode: true, guiScale: 4, curvedScreenEnabled: true, curvedScreenArcDegrees: 60 }
            //   },
            //   rules: {
            //     "#containers": "compact",
            //     "net.minecraft.client.gui.screens.inventory.CraftingScreen": "wide"
            //   }
            // }

            """;

    private static final String TEMPLATE = HEADER + """
            {
              profiles: {},
              rules: {}
            }
            """;
}