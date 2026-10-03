package org.tovasha.ych.api;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;

public class FontRegistry {
    private static final Map<String, Identifier> FONTS = new ConcurrentHashMap<>();

    static {
        register("default", Identifier.fromNamespaceAndPath("minecraft", "default"));
        register("code", Identifier.fromNamespaceAndPath("ych", "code"));
        register("consolas", Identifier.fromNamespaceAndPath("ych", "code"));
        register("arial", Identifier.fromNamespaceAndPath("ych", "arial"));
        register("arialblack", Identifier.fromNamespaceAndPath("ych", "arialblack"));
        register("arial_black", Identifier.fromNamespaceAndPath("ych", "arialblack"));
        register("modern", Identifier.fromNamespaceAndPath("ych", "modern"));
        register("bahnschrift", Identifier.fromNamespaceAndPath("ych", "bahnschrift"));
        register("impact", Identifier.fromNamespaceAndPath("ych", "impact"));
        register("comic", Identifier.fromNamespaceAndPath("ych", "comic"));
        register("comicsans", Identifier.fromNamespaceAndPath("ych", "comic"));
        register("tahoma", Identifier.fromNamespaceAndPath("ych", "tahoma"));
        register("verdana", Identifier.fromNamespaceAndPath("ych", "verdana"));
        register("trebuchet", Identifier.fromNamespaceAndPath("ych", "trebuchet"));
        register("cascadia", Identifier.fromNamespaceAndPath("ych", "cascadia"));
        register("calibri", Identifier.fromNamespaceAndPath("ych", "calibri"));
        register("georgia", Identifier.fromNamespaceAndPath("ych", "georgia"));
    }

    public static void register(String id, Identifier location) {
        FONTS.put(id, location);
    }

    public static Identifier get(String id) {
        return FONTS.get(id);
    }

    public static boolean has(String id) {
        return FONTS.containsKey(id);
    }

    public static Map<String, Identifier> getAll() {
        return Collections.unmodifiableMap(FONTS);
    }
}
