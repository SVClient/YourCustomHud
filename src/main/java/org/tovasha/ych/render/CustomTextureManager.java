package org.tovasha.ych.render;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.tovasha.ych.YourCustomHud;

public class CustomTextureManager {
    private static final Map<String, Identifier> TEXTURE_CACHE = new HashMap<>();

    public static Identifier getOrLoadTexture(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        path = path.trim();
        if ((path.startsWith("\"") && path.endsWith("\"")) || (path.startsWith("'") && path.endsWith("'"))) {
            if (path.length() >= 2) {
                path = path.substring(1, path.length() - 1).trim();
            }
        }

        if (TEXTURE_CACHE.containsKey(path)) {
            return TEXTURE_CACHE.get(path);
        }

        if (path.contains(":") && !path.contains("\\") && !path.contains("/")) {
            Identifier id = Identifier.tryParse(path);
            if (id != null) {
                TEXTURE_CACHE.put(path, id);
                return id;
            }
        }

        File file = resolveFile(path);
        if (file == null || !file.exists() || !file.isFile()) {
            Identifier id = Identifier.tryParse(path);
            if (id != null) {
                TEXTURE_CACHE.put(path, id);
                return id;
            }
            return null;
        }

        try (InputStream stream = new FileInputStream(file)) {
            NativeImage nativeImage = NativeImage.read(stream);
            DynamicTexture dynamicTexture = new DynamicTexture(() -> "ych_custom_texture", nativeImage);
            String nameOnly = file.getName();
            int dot = nameOnly.lastIndexOf('.');
            if (dot > 0) {
                nameOnly = nameOnly.substring(0, dot);
            }
            String cleanName = nameOnly.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
            if (cleanName.isEmpty()) {
                cleanName = "tex";
            }
            String hash = Integer.toHexString(Math.abs(file.getAbsolutePath().hashCode()));
            Identifier textureId = Identifier.fromNamespaceAndPath("ych", "custom/" + cleanName + "_" + hash);
            Minecraft.getInstance().getTextureManager().register(textureId, dynamicTexture);
            TEXTURE_CACHE.put(path, textureId);
            return textureId;
        } catch (Exception e) {
            YourCustomHud.LOGGER.error("Failed to load custom texture: " + path, e);
            return null;
        }
    }

    private static File resolveFile(String path) {
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            return file;
        }

        if (path.contains("\t")) {
            File fixed = new File(path.replace("\t", "\\t"));
            if (fixed.exists() && fixed.isFile()) {
                return fixed;
            }
        }

        if (path.contains("\r")) {
            File fixed = new File(path.replace("\r", "\\r"));
            if (fixed.exists() && fixed.isFile()) {
                return fixed;
            }
        }

        if (path.contains("\n")) {
            File fixed = new File(path.replace("\n", "\\n"));
            if (fixed.exists() && fixed.isFile()) {
                return fixed;
            }
        }

        if (path.contains("\\\\")) {
            File fixed = new File(path.replace("\\\\", "\\"));
            if (fixed.exists() && fixed.isFile()) {
                return fixed;
            }
        }

        File normalized = new File(path.replace("/", File.separator));
        if (normalized.exists() && normalized.isFile()) {
            return normalized;
        }

        return file;
    }
}
