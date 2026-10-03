package org.tovasha.ych.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.gui.GuiGraphics;

public class HudRegistry {
    private static final Map<String, HudElement> ELEMENTS = new ConcurrentHashMap<>();
    private static final List<HudElement> ORDERED_ELEMENTS = new CopyOnWriteArrayList<>();

    public static void register(HudElement element) {
        if (element == null) return;
        ELEMENTS.put(element.getId(), element);
        if (!ORDERED_ELEMENTS.contains(element)) {
            ORDERED_ELEMENTS.add(element);
        }
    }

    public static void unregister(String id) {
        HudElement el = ELEMENTS.remove(id);
        if (el != null) {
            ORDERED_ELEMENTS.remove(el);
        }
    }

    public static HudElement get(String id) {
        return ELEMENTS.get(id);
    }

    public static List<HudElement> getElements() {
        return Collections.unmodifiableList(ORDERED_ELEMENTS);
    }

    public static void renderAll(GuiGraphics graphics, float deltaTick) {
        for (HudElement element : ORDERED_ELEMENTS) {
            element.render(graphics, deltaTick);
        }
    }

    public static void tickAll() {
        for (HudElement element : ORDERED_ELEMENTS) {
            element.tick();
        }
    }

    public static void keyPressedAll(int key, int action) {
        for (HudElement element : ORDERED_ELEMENTS) {
            element.keyPressed(key, action);
        }
    }

    public static void clear() {
        ELEMENTS.clear();
        ORDERED_ELEMENTS.clear();
    }
}
