package org.tovasha.ych.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.tovasha.ych.YourCustomHud;
import org.tovasha.ych.api.HudElement;
import org.tovasha.ych.api.HudRegistry;

public class StorageManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    @Setter
    public static class PresetManifest {
        private String name;
        private List<String> elementFiles = new ArrayList<>();
    }

    public static File getBaseDir() {
        File dir = new File(Minecraft.getInstance().gameDirectory, "yourcustomhud");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getPresetsDir() {
        File dir = new File(getBaseDir(), "presets");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getElementsDir() {
        File dir = new File(getBaseDir(), "elements");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static List<String> listPresets() {
        List<String> list = new ArrayList<>();
        File presetsDir = getPresetsDir();
        File[] files = presetsDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    list.add(file.getName());
                } else if (file.getName().endsWith(".svhud")) {
                    String name = file.getName().substring(0, file.getName().length() - 6);
                    if (!list.contains(name)) {
                        list.add(name);
                    }
                }
            }
        }
        if (list.isEmpty()) {
            list.add("default");
        }
        return list;
    }

    public static void initDefaultsIfEmpty() {
        List<String> presets = listPresets();
        File defaultDir = new File(getPresetsDir(), "default");
        if (!defaultDir.exists() || defaultDir.listFiles() == null || defaultDir.listFiles().length == 0) {
            createDefaultPreset(defaultDir);
        }
    }

    private static void createDefaultPreset(File presetDir) {
        presetDir.mkdirs();

        HudElement watermark = new HudElement("watermark", "Watermark",
                "let bg = 0x99111115;\n" +
                "let radius = 6;\n" +
                "fn main() {\n" +
                "    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, radius, bg);\n" +
                "    Render.drawRoundedOutline(Params.x, Params.y, Params.width, Params.height, radius, 1, Params.rainbowColor1);\n" +
                "    Render.drawText(\"YourCustomHud  |  FPS: \" + Variables.fps, Params.x + 8, Params.y + 7, 0xFFFFFFFF);\n" +
                "}\n"
        );
        watermark.setX(10);
        watermark.setY(10);
        watermark.setWidth(140);
        watermark.setHeight(22);

        HudElement coords = new HudElement("coords", "Coordinates",
                "let bg = 0x99111115;\n" +
                "fn main() {\n" +
                "    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 5, bg);\n" +
                "    Render.drawText(\"XYZ: \" + Variables.posX + \", \" + Variables.posY + \", \" + Variables.posZ, Params.x + 8, Params.y + 6, 0xFFFFFFFF);\n" +
                "    Render.drawText(\"Biome: \" + Variables.biome, Params.x + 8, Params.y + 17, 0xFFAAAAAA);\n" +
                "}\n"
        );
        coords.setX(10);
        coords.setY(36);
        coords.setWidth(160);
        coords.setHeight(30);

        HudElement speed = new HudElement("speed", "Speedometer",
                "let bg = 0x99111115;\n" +
                "fn main() {\n" +
                "    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 5, bg);\n" +
                "    Render.drawText(\"Speed: \" + Variables.speed + \" m/s\", Params.x + 8, Params.y + 6, 0xFF55FF55);\n" +
                "    Render.drawText(\"Ping: \" + Variables.ping + \" ms\", Params.x + 8, Params.y + 17, 0xFFFFFF55);\n" +
                "}\n"
        );
        speed.setX(10);
        speed.setY(70);
        speed.setWidth(120);
        speed.setHeight(30);

        HudElement controls = new HudElement("controls", "Controls",
                "let bgNormal = 0x66000000;\n" +
                "let bgPressed = 0xCC3B82F6;\n" +
                "let textNormal = 0xFFFFFFFF;\n" +
                "Params.width = 72;\n" +
                "Params.height = 74;\n" +
                "\n" +
                "fn drawKey(label, kx, ky, kw, kh, isPressed) {\n" +
                "    let bg = bgNormal;\n" +
                "    if (isPressed) {\n" +
                "        bg = bgPressed;\n" +
                "    }\n" +
                "    Render.drawRoundedRect(kx, ky, kw, kh, 4, bg);\n" +
                "    Render.drawRoundedOutline(kx, ky, kw, kh, 4, 1, 0x44FFFFFF);\n" +
                "    let tw = Font.textWidth(label);\n" +
                "    let th = Font.fontHeight;\n" +
                "    let tx = kx + (kw - tw) / 2;\n" +
                "    let ty = ky + (kh - th) / 2;\n" +
                "    Render.drawText(label, tx, ty, textNormal);\n" +
                "}\n" +
                "\n" +
                "fn main() {\n" +
                "    let kw = 22;\n" +
                "    let kh = 22;\n" +
                "    let gap = 3;\n" +
                "    let x0 = Params.x;\n" +
                "    let y0 = Params.y;\n" +
                "\n" +
                "    drawKey(\"W\", x0 + kw + gap, y0, kw, kh, Key.w);\n" +
                "\n" +
                "    let r2y = y0 + kh + gap;\n" +
                "    drawKey(\"A\", x0, r2y, kw, kh, Key.a);\n" +
                "    drawKey(\"S\", x0 + kw + gap, r2y, kw, kh, Key.s);\n" +
                "    drawKey(\"D\", x0 + (kw + gap) * 2, r2y, kw, kh, Key.d);\n" +
                "\n" +
                "    let r3y = r2y + kh + gap;\n" +
                "    let btnW = (kw * 3 + gap * 2 - gap) / 2;\n" +
                "    drawKey(\"LMB\", x0, r3y, btnW, kh, Key.lmb);\n" +
                "    drawKey(\"RMB\", x0 + btnW + gap, r3y, btnW, kh, Key.rmb);\n" +
                "}\n"
        );
        controls.setX(10);
        controls.setY(110);
        controls.setWidth(72);
        controls.setHeight(74);

        HudElement targetHud = new HudElement("target", "TargetHUD",
                "Params.width = 150;\n" +
                "Params.height = 42;\n" +
                "Params.font = Font.bahnschrift;\n" +
                "\n" +
                "fn main() {\n" +
                "    if (!Variables.hasTarget) {\n" +
                "        return;\n" +
                "    }\n" +
                "\n" +
                "    let bg = 0xD0121214;\n" +
                "    let border = 0xFF2A2A32;\n" +
                "    let healthBg = 0xAA2A2A2E;\n" +
                "    let healthCol = 0xFFEF4444;\n" +
                "\n" +
                "    Render.drawRoundedRect(Params.x, Params.y, Params.width, Params.height, 5, bg);\n" +
                "    Render.drawRoundedOutline(Params.x, Params.y, Params.width, Params.height, 5, 1.0, border);\n" +
                "\n" +
                "    let headTex = Target.getHeadTexture();\n" +
                "    Render.drawImage(headTex, Params.x + 6, Params.y + 6, 30, 30);\n" +
                "    Render.drawRoundedOutline(Params.x + 6, Params.y + 6, 30, 30, 2, 1.0, 0xFF404040);\n" +
                "\n" +
                "    let targetName = Target.getName();\n" +
                "    Render.drawText(targetName, Params.x + 42, Params.y + 6, Font.bahnschrift, 9, 0xFFFFFFFF);\n" +
                "\n" +
                "    let hp = Target.getHealth();\n" +
                "    let maxHp = Target.getMaxHealth();\n" +
                "    if (maxHp <= 0) { maxHp = 20; }\n" +
                "    let barW = 75;\n" +
                "    let filledW = (hp / maxHp) * barW;\n" +
                "    if (filledW > barW) { filledW = barW; }\n" +
                "    if (filledW < 0) { filledW = 0; }\n" +
                "\n" +
                "    Render.drawRoundedRect(Params.x + 42, Params.y + 24, barW, 8, 2, healthBg);\n" +
                "    if (filledW > 0) {\n" +
                "        Render.drawRoundedRect(Params.x + 42, Params.y + 24, filledW, 8, 2, healthCol);\n" +
                "    }\n" +
                "    Render.drawRoundedOutline(Params.x + 42, Params.y + 24, barW, 8, 2, 1.0, 0xFF444444);\n" +
                "    Render.drawText(hp + \" HP\", Params.x + 42 + barW + 5, Params.y + 24, Font.bahnschrift, 8, 0xFFFF8888);\n" +
                "\n" +
                "    let rightItem = Target.getRightHandItemTexture();\n" +
                "    if (rightItem != \"\") {\n" +
                "        Render.drawImage(rightItem, Params.x + 128, Params.y + 5, 16, 16);\n" +
                "    }\n" +
                "}\n"
        );
        targetHud.setX(200);
        targetHud.setY(150);
        targetHud.setWidth(150);
        targetHud.setHeight(42);

        saveElementToFile(new File(presetDir, "watermark.svhe"), watermark);
        saveElementToFile(new File(presetDir, "coords.svhe"), coords);
        saveElementToFile(new File(presetDir, "speed.svhe"), speed);
        saveElementToFile(new File(presetDir, "controls.svhe"), controls);
        saveElementToFile(new File(presetDir, "target.svhe"), targetHud);

        PresetManifest manifest = new PresetManifest();
        manifest.setName("default");
        manifest.getElementFiles().add("watermark.svhe");
        manifest.getElementFiles().add("coords.svhe");
        manifest.getElementFiles().add("speed.svhe");
        manifest.getElementFiles().add("controls.svhe");
        manifest.getElementFiles().add("target.svhe");

        try (FileWriter writer = new FileWriter(new File(presetDir, "preset.json"), StandardCharsets.UTF_8)) {
            GSON.toJson(manifest, writer);
        } catch (IOException e) {
            YourCustomHud.LOGGER.error("Failed to write default preset manifest", e);
        }
    }

    public static void loadPreset(String presetName) {
        HudRegistry.clear();
        File presetDir = new File(getPresetsDir(), presetName);
        File zipFile = new File(getPresetsDir(), presetName + ".svhud");

        if (!presetDir.exists() && zipFile.exists()) {
            importZip(zipFile, presetDir);
        }

        if (!presetDir.exists()) {
            initDefaultsIfEmpty();
            if ("default".equals(presetName)) {
                presetDir = new File(getPresetsDir(), "default");
            }
        }

        File manifestFile = new File(presetDir, "preset.json");
        if (manifestFile.exists()) {
            try (FileReader reader = new FileReader(manifestFile, StandardCharsets.UTF_8)) {
                PresetManifest manifest = GSON.fromJson(reader, PresetManifest.class);
                if (manifest != null && manifest.getElementFiles() != null) {
                    for (String fileName : manifest.getElementFiles()) {
                        File elFile = new File(presetDir, fileName);
                        if (elFile.exists()) {
                            HudElement el = loadElementFromFile(elFile);
                            if (el != null) {
                                HudRegistry.register(el);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                YourCustomHud.LOGGER.error("Failed to load preset manifest", e);
            }
        } else {
            File[] files = presetDir.listFiles((dir, name) -> name.endsWith(".svhe"));
            if (files != null) {
                for (File file : files) {
                    HudElement el = loadElementFromFile(file);
                    if (el != null) {
                        HudRegistry.register(el);
                    }
                }
            }
        }

        if (HudRegistry.getElements().isEmpty() && "default".equals(presetName)) {
            createDefaultPreset(presetDir);
            File[] files = presetDir.listFiles((dir, name) -> name.endsWith(".svhe"));
            if (files != null) {
                for (File file : files) {
                    HudElement el = loadElementFromFile(file);
                    if (el != null) {
                        HudRegistry.register(el);
                    }
                }
            }
        }
    }

    public static void savePreset(String presetName) {
        File presetDir = new File(getPresetsDir(), presetName);
        presetDir.mkdirs();

        PresetManifest manifest = new PresetManifest();
        manifest.setName(presetName);

        for (HudElement element : HudRegistry.getElements()) {
            String fileName = element.getId() + ".svhe";
            saveElementToFile(new File(presetDir, fileName), element);
            manifest.getElementFiles().add(fileName);
        }

        File[] existing = presetDir.listFiles((dir, name) -> name.endsWith(".svhe"));
        if (existing != null) {
            for (File file : existing) {
                if (!manifest.getElementFiles().contains(file.getName())) {
                    file.delete();
                }
            }
        }

        try (FileWriter writer = new FileWriter(new File(presetDir, "preset.json"), StandardCharsets.UTF_8)) {
            GSON.toJson(manifest, writer);
        } catch (IOException e) {
            YourCustomHud.LOGGER.error("Failed to save preset manifest", e);
        }
    }

    public static void deleteElement(String presetName, String elementId) {
        File presetDir = new File(getPresetsDir(), presetName);
        if (presetDir.exists()) {
            File elFile = new File(presetDir, elementId + ".svhe");
            if (elFile.exists()) {
                elFile.delete();
            }
        }
        savePreset(presetName);
    }

    public static HudElement importElement(File sourceFile, String presetName) {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        HudElement el = loadElementFromFile(sourceFile);
        if (el == null) {
            return null;
        }

        File presetDir = new File(getPresetsDir(), presetName);
        presetDir.mkdirs();

        String id = el.getId();
        if (id == null || id.isEmpty()) {
            String name = sourceFile.getName();
            if (name.endsWith(".svhe")) {
                name = name.substring(0, name.length() - 5);
            }
            id = name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
            el.setId(id);
        }

        if (HudRegistry.get(id) != null) {
            id = id + "_" + (System.currentTimeMillis() % 10000);
            el.setId(id);
        }

        File targetFile = new File(presetDir, id + ".svhe");
        saveElementToFile(targetFile, el);
        return el;
    }

    public static void deletePreset(String presetName) {
        File presetDir = new File(getPresetsDir(), presetName);
        if (presetDir.exists()) {
            deleteRecursively(presetDir);
        }
        File zipFile = new File(getPresetsDir(), presetName + ".svhud");
        if (zipFile.exists()) {
            zipFile.delete();
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File c : children) {
                    deleteRecursively(c);
                }
            }
        }
        file.delete();
    }

    public static HudElement loadElementFromFile(File file) {
        if (!file.exists()) {
            return null;
        }

        String id = file.getName().replace(".svhe", "");
        String name = id;
        boolean enabled = true;
        float x = 10;
        float y = 10;
        float width = 100;
        float height = 30;
        String font = "default";
        StringBuilder codeBuilder = new StringBuilder();

        boolean inHeaders = true;

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (inHeaders) {
                    String trimmed = line.trim();
                    if (trimmed.equals("---")) {
                        inHeaders = false;
                        continue;
                    }
                    if (trimmed.startsWith("@")) {
                        int colon = trimmed.indexOf(':');
                        if (colon > 0) {
                            String key = trimmed.substring(1, colon).trim();
                            String val = trimmed.substring(colon + 1).trim();
                            switch (key) {
                                case "name": name = val; break;
                                case "enabled": enabled = Boolean.parseBoolean(val); break;
                                case "x": x = Float.parseFloat(val); break;
                                case "y": y = Float.parseFloat(val); break;
                                case "width": width = Float.parseFloat(val); break;
                                case "height": height = Float.parseFloat(val); break;
                                case "font": font = val; break;
                            }
                            continue;
                        }
                    }
                    inHeaders = false;
                }
                codeBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            YourCustomHud.LOGGER.error("Failed to read element file: " + file.getName(), e);
            return null;
        }

        HudElement element = new HudElement(id, name, codeBuilder.toString());
        element.setEnabled(enabled);
        element.setX(x);
        element.setY(y);
        element.setWidth(width);
        element.setHeight(height);
        element.setFont(font);
        element.compile();
        return element;
    }

    public static void saveElementToFile(File file, HudElement element) {
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write("@name: " + element.getName() + "\n");
            writer.write("@enabled: " + element.isEnabled() + "\n");
            writer.write("@x: " + element.getX() + "\n");
            writer.write("@y: " + element.getY() + "\n");
            writer.write("@width: " + element.getWidth() + "\n");
            writer.write("@height: " + element.getHeight() + "\n");
            writer.write("@font: " + element.getFont() + "\n");
            writer.write("---\n");
            writer.write(element.getCode());
        } catch (IOException e) {
            YourCustomHud.LOGGER.error("Failed to save element file: " + file.getName(), e);
        }
    }

    public static void exportZip(String presetName, File targetZip) {
        File presetDir = new File(getPresetsDir(), presetName);
        if (!presetDir.exists()) return;

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetZip))) {
            File[] files = presetDir.listFiles();
            if (files != null) {
                byte[] buffer = new byte[4096];
                for (File f : files) {
                    if (f.isFile()) {
                        zos.putNextEntry(new ZipEntry(f.getName()));
                        try (FileInputStream fis = new FileInputStream(f)) {
                            int len;
                            while ((len = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                        }
                        zos.closeEntry();
                    }
                }
            }
        } catch (IOException e) {
            YourCustomHud.LOGGER.error("Failed to export preset zip", e);
        }
    }

    public static void importZip(File zipFile, File targetDir) {
        targetDir.mkdirs();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());
                if (!entry.isDirectory()) {
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            YourCustomHud.LOGGER.error("Failed to import preset zip", e);
        }
    }
}
