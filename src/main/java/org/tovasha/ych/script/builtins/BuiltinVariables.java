package org.tovasha.ych.script.builtins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.tovasha.ych.api.VariableRegistry;
import org.tovasha.ych.render.CpsTracker;
import org.tovasha.ych.render.TargetTracker;

public class BuiltinVariables implements ScriptNamespace {
    @Override
    public Object getProperty(String name) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        switch (name) {
            case "ping":
                if (player != null && mc.getConnection() != null) {
                    ClientPacketListener connection = mc.getConnection();
                    PlayerInfo info = connection.getPlayerInfo(player.getUUID());
                    if (info != null) {
                        return (double) info.getLatency();
                    }
                }
                return 0.0;
            case "fps":
                return (double) mc.getFps();
            case "speed":
                if (player != null) {
                    Vec3 delta = player.getDeltaMovement();
                    double len = Math.sqrt(delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
                    return Math.round(len * 20.0 * 100.0) / 100.0;
                }
                return 0.0;
            case "horizontalSpeed":
                if (player != null) {
                    Vec3 delta = player.getDeltaMovement();
                    double hLen = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                    return Math.round(hLen * 20.0 * 100.0) / 100.0;
                }
                return 0.0;
            case "verticalSpeed":
                if (player != null) {
                    double vLen = player.getDeltaMovement().y;
                    return Math.round(vLen * 20.0 * 100.0) / 100.0;
                }
                return 0.0;
            case "posX":
                return player != null ? Math.round(player.getX() * 100.0) / 100.0 : 0.0;
            case "posY":
                return player != null ? Math.round(player.getY() * 100.0) / 100.0 : 0.0;
            case "posZ":
                return player != null ? Math.round(player.getZ() * 100.0) / 100.0 : 0.0;
            case "biome":
                if (player != null && mc.level != null) {
                    return mc.level.getBiome(player.blockPosition()).unwrapKey()
                            .map(k -> k.identifier().getPath())
                            .orElse("unknown");
                }
                return "plains";
            case "player":
            case "nick":
            case "nickname":
            case "playerName":
            case "playerNick":
                if (player != null) {
                    return player.getName().getString();
                }
                if (mc.getUser() != null) {
                    return mc.getUser().getName();
                }
                return "Player";
            case "time":
            case "timeOfDay":
            case "timeName":
                if (mc.level != null) {
                    long t = (mc.level.getDayTime() % 24000L + 24000L) % 24000L;
                    boolean isRu = mc.options != null && "ru_ru".equalsIgnoreCase(mc.options.languageCode);
                    if (isRu) {
                        if (t < 1000 || t >= 23000) return "Утро";
                        if (t < 12000) return "День";
                        if (t < 13500) return "Закат";
                        return "Ночь";
                    } else {
                        if (t < 1000 || t >= 23000) return "Morning";
                        if (t < 12000) return "Day";
                        if (t < 13500) return "Sunset";
                        return "Night";
                    }
                }
                return "Day";
            case "isDay":
                if (mc.level != null) {
                    long t = (mc.level.getDayTime() % 24000L + 24000L) % 24000L;
                    return t < 12000;
                }
                return true;
            case "isNight":
                if (mc.level != null) {
                    long t = (mc.level.getDayTime() % 24000L + 24000L) % 24000L;
                    return t >= 13500 && t < 23000;
                }
                return false;
            case "rawTime":
            case "timeTicks":
                if (mc.level != null) {
                    return (double) ((mc.level.getDayTime() % 24000L + 24000L) % 24000L);
                }
                return 0.0;
            case "day":
            case "days":
            case "dayCount":
                if (mc.level != null) {
                    return (double) (mc.level.getDayTime() / 24000L);
                }
                return 0.0;
            case "weather":
            case "weatherName":
                if (mc.level != null) {
                    boolean isRu = mc.options != null && "ru_ru".equalsIgnoreCase(mc.options.languageCode);
                    if (mc.level.isThundering()) {
                        return isRu ? "Гроза" : "Thunder";
                    } else if (mc.level.isRaining()) {
                        return isRu ? "Дождь" : "Rain";
                    } else {
                        return isRu ? "Ясно" : "Clear";
                    }
                }
                return "Clear";
            case "isRaining":
                return mc.level != null && mc.level.isRaining();
            case "isThundering":
                return mc.level != null && mc.level.isThundering();
            case "isClear":
                return mc.level != null && !mc.level.isRaining() && !mc.level.isThundering();
            case "cps":
            case "lmbCps":
            case "cpsLmb":
                return (double) CpsTracker.getLmbCps();
            case "rmbCps":
            case "cpsRmb":
                return (double) CpsTracker.getRmbCps();
            case "target":
                return TargetTracker.hasTarget() ? TargetTracker.getTargetNamespace() : null;
            case "hasTarget":
            case "hastarget":
                return TargetTracker.hasTarget();
            default:
                if (VariableRegistry.has(name)) {
                    return VariableRegistry.get(name);
                }
                return null;
        }
    }

    @Override
    public void setProperty(String name, Object value) {
    }
}
