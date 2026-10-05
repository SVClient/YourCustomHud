package org.tovasha.ych;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tovasha.ych.api.HudRegistry;
import org.tovasha.ych.config.MainConfig;
import org.tovasha.ych.event.AttackEvent;
import org.tovasha.ych.event.EventBus;
import org.tovasha.ych.event.TickEvent;
import org.tovasha.ych.gui.HudEditorScreen;
import org.tovasha.ych.render.TargetTracker;
import org.tovasha.ych.script.builtins.BuiltinTarget;
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

        editorKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.yourcustomhud.open_editor",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC
        ));

        StorageManager.initDefaultsIfEmpty();
        StorageManager.loadPreset(CONFIG.getActivePreset());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (editorKeyBinding.consumeClick()) {
                client.gui.setScreen(new HudEditorScreen());
            }
            TargetTracker.update();
            EVENT_BUS.post(new TickEvent());
            HudRegistry.tickAll();
        });

        AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            Minecraft mc = Minecraft.getInstance();
            if (level.isClientSide() && mc.player != null && (player == mc.player || player.getUUID().equals(mc.player.getUUID()))) {
                BuiltinTarget target = new BuiltinTarget(entity);
                EVENT_BUS.post(new AttackEvent(target));
                HudRegistry.attackAll(target);
            }
            return InteractionResult.PASS;
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void saveConfig() {
        try {
            AutoConfig.getConfigHolder(MainConfig.class).save();
        } catch (Exception ignored) {
        }
    }
}
