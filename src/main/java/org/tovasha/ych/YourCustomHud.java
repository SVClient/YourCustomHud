package org.tovasha.ych;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tovasha.ych.api.HudRegistry;
import org.tovasha.ych.config.MainConfig;
import org.tovasha.ych.event.EventBus;
import org.tovasha.ych.event.TickEvent;
import org.tovasha.ych.gui.HudEditorScreen;
import org.tovasha.ych.storage.StorageManager;

public class YourCustomHud implements ClientModInitializer {
    public static final String MOD_ID = "ych";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final EventBus EVENT_BUS = new EventBus();

    public static MainConfig CONFIG;
    public static KeyMapping editorKeyBinding;

    @Override
    public void onInitializeClient() {
        AutoConfig.register(MainConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(MainConfig.class).getConfig();

        editorKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.yourcustomhud.open_editor",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC
        ));

        StorageManager.initDefaultsIfEmpty();
        StorageManager.loadPreset(CONFIG.getActivePreset());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (editorKeyBinding.consumeClick()) {
                client.setScreen(new HudEditorScreen());
            }
            EVENT_BUS.post(new TickEvent());
            HudRegistry.tickAll();
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
