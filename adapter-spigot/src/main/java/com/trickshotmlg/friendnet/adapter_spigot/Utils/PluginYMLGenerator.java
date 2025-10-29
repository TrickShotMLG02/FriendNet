package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.core.permissions.Permission;
import com.trickshotmlg.friendnet.core.permissions.PermissionHolder;
import com.trickshotmlg.friendnet.core_api.interfaces.PermissionNode;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

public class PluginYMLGenerator {

    public static void generate(File output, Class<?> permissionHolderClass) throws IOException {
        Map<String, Object> pluginYml = new LinkedHashMap<>();

        // base plugin info — can be extended
        pluginYml.put("name", "FriendNet");
        pluginYml.put("version", "1.0.0");
        pluginYml.put("main", "com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin");
        pluginYml.put("api-version", "1.21");
        //pluginYml.put("author", "TrickshotMLG");
        pluginYml.put("load", "POSTWORLD");

        Map<String, Object> commands = new LinkedHashMap<>();
        commands.put("friend", null);

        // permissions
        Map<String, Object> permissions = new LinkedHashMap<>();

        for (Field field : permissionHolderClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == Permission.class) {
                try {
                    Permission perm = (Permission) field.get(null);
                    Map<String, Object> permData = new LinkedHashMap<>();
                    /*
                    if (!perm.getDescription().isEmpty())
                        permData.put("description", perm.getDescription());
                    */
                    permData.put("default", false);
                    //permData.put("default", perm.getDefaultValue());

                    // Add child permissions
                    if (!perm.getChildren().isEmpty()) {
                        Map<String, Boolean> children = new LinkedHashMap<>();
                        for (PermissionNode child : perm.getChildren()) {
                            children.put(child.getPermissionPrefixed(), true);
                        }
                        permData.put("children", children);
                    }

                    permissions.put(perm.getPermissionPrefixed(), permData);
                } catch (IllegalAccessException ignored) {}
            }
        }

        pluginYml.put("commands", commands);

        pluginYml.put("permissions", permissions);

        try (FileWriter writer = new FileWriter(output)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // block style
            options.setIndent(2);
            options.setPrettyFlow(true);

            new Yaml(options).dump(pluginYml, writer);
        }

        System.out.println("✅ Generated plugin.yml with " + permissions.size() + " permissions.");
    }


    public static void main(String[] args) throws IOException {
        File output = new File(args[0]);
        PluginYMLGenerator.generate(output, PermissionHolder.class);
    }
}
