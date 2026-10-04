package org.tovasha.ych.script.builtins;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.tovasha.ych.api.VariableRegistry;
import org.tovasha.ych.render.CpsTracker;
import org.tovasha.ych.render.TargetTracker;
import org.tovasha.ych.render.TpsTracker;

public class BuiltinVariables implements ScriptNamespace {
    @Override
    public Object getProperty(String name) {
        if (name == null) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        String key = name.toLowerCase();

        switch (key) {
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
            case "tps":
            case "servertps":
            case "server_tps":
                return TpsTracker.getTps();
            case "speed":
                if (player != null) {
                    Vec3 delta = player.getDeltaMovement();
                    double len = Math.sqrt(delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
                    return Math.round(len * 20.0 * 100.0) / 100.0;
                }
                return 0.0;
            case "horizontalspeed":
            case "horizontal_speed":
                if (player != null) {
                    Vec3 delta = player.getDeltaMovement();
                    double hLen = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                    return Math.round(hLen * 20.0 * 100.0) / 100.0;
                }
                return 0.0;
            case "verticalspeed":
            case "vertical_speed":
                if (player != null) {
                    double vLen = player.getDeltaMovement().y;
                    return Math.round(vLen * 20.0 * 100.0) / 100.0;
                }
                return 0.0;
            case "posx":
            case "x":
                return player != null ? (double) Math.round(player.getX()) : 0.0;
            case "posy":
            case "y":
                return player != null ? (double) Math.round(player.getY()) : 0.0;
            case "posz":
            case "z":
                return player != null ? (double) Math.round(player.getZ()) : 0.0;
            case "pitch":
                return player != null ? (double) Math.round(player.getXRot() * 10.0) / 10.0 : 0.0;
            case "yaw":
                return player != null ? (double) Math.round(Mth.wrapDegrees(player.getYRot()) * 10.0) / 10.0 : 0.0;
            case "direction":
            case "facing":
            case "cardinal":
            case "facingdirection":
            case "facing_direction":
                if (player != null) {
                    Direction dir = player.getDirection();
                    boolean isRu = mc.options != null && "ru_ru".equalsIgnoreCase(mc.options.languageCode);
                    switch (dir) {
                        case NORTH: return isRu ? "Север" : "North";
                        case SOUTH: return isRu ? "Юг" : "South";
                        case WEST: return isRu ? "Запад" : "West";
                        case EAST: return isRu ? "Восток" : "East";
                        default: return dir.getName();
                    }
                }
                return "";
            case "directionshort":
            case "facingshort":
            case "direction_short":
            case "facing_short":
                if (player != null) {
                    Direction dir = player.getDirection();
                    boolean isRu = mc.options != null && "ru_ru".equalsIgnoreCase(mc.options.languageCode);
                    switch (dir) {
                        case NORTH: return isRu ? "С" : "N";
                        case SOUTH: return isRu ? "Ю" : "S";
                        case WEST: return isRu ? "З" : "W";
                        case EAST: return isRu ? "В" : "E";
                        default: return dir.getName().substring(0, 1).toUpperCase();
                    }
                }
                return "";
            case "oppositex":
            case "otherx":
            case "otherdimx":
            case "opposite_x":
            case "other_x":
                if (player != null && mc.level != null) {
                    if (mc.level.dimension() == Level.NETHER) {
                        return (double) Math.round(player.getX() * 8.0);
                    } else {
                        return (double) Math.round(player.getX() / 8.0);
                    }
                }
                return 0.0;
            case "oppositey":
            case "othery":
            case "otherdimy":
            case "opposite_y":
            case "other_y":
                return player != null ? (double) Math.round(player.getY()) : 0.0;
            case "oppositez":
            case "otherz":
            case "otherdimz":
            case "opposite_z":
            case "other_z":
                if (player != null && mc.level != null) {
                    if (mc.level.dimension() == Level.NETHER) {
                        return (double) Math.round(player.getZ() * 8.0);
                    } else {
                        return (double) Math.round(player.getZ() / 8.0);
                    }
                }
                return 0.0;
            case "netherx":
            case "nether_x":
                if (player != null && mc.level != null) {
                    return mc.level.dimension() == Level.NETHER
                            ? (double) Math.round(player.getX())
                            : (double) Math.round(player.getX() / 8.0);
                }
                return 0.0;
            case "nethery":
            case "nether_y":
                return player != null ? (double) Math.round(player.getY()) : 0.0;
            case "netherz":
            case "nether_z":
                if (player != null && mc.level != null) {
                    return mc.level.dimension() == Level.NETHER
                            ? (double) Math.round(player.getZ())
                            : (double) Math.round(player.getZ() / 8.0);
                }
                return 0.0;
            case "overworldx":
            case "overworld_x":
                if (player != null && mc.level != null) {
                    return mc.level.dimension() == Level.NETHER
                            ? (double) Math.round(player.getX() * 8.0)
                            : (double) Math.round(player.getX());
                }
                return 0.0;
            case "overworldy":
            case "overworld_y":
                return player != null ? (double) Math.round(player.getY()) : 0.0;
            case "overworldz":
            case "overworld_z":
                if (player != null && mc.level != null) {
                    return mc.level.dimension() == Level.NETHER
                            ? (double) Math.round(player.getZ() * 8.0)
                            : (double) Math.round(player.getZ());
                }
                return 0.0;
            case "oppositedimension":
            case "oppositedim":
            case "opposite_dimension":
            case "opposite_dim":
                if (mc.level != null) {
                    boolean isRu = mc.options != null && "ru_ru".equalsIgnoreCase(mc.options.languageCode);
                    if (mc.level.dimension() == Level.NETHER) {
                        return isRu ? "Верхний мир" : "Overworld";
                    } else {
                        return isRu ? "Нижний мир" : "Nether";
                    }
                }
                return "";
            case "dimension":
            case "dim":
                if (mc.level != null) {
                    boolean isRu = mc.options != null && "ru_ru".equalsIgnoreCase(mc.options.languageCode);
                    if (mc.level.dimension() == Level.NETHER) {
                        return isRu ? "Нижний мир" : "Nether";
                    } else if (mc.level.dimension() == Level.END) {
                        return isRu ? "Край" : "The End";
                    } else {
                        return isRu ? "Верхний мир" : "Overworld";
                    }
                }
                return "";
            case "biome":
                if (player != null && mc.level != null) {
                    return mc.level.getBiome(player.blockPosition()).unwrapKey()
                            .map(k -> k.identifier().getPath())
                            .orElse("unknown");
                }
                return "plains";
            case "nick":
            case "nickname":
            case "playernick":
                if (player != null) {
                    return player.getName().getString();
                }
                if (mc.getUser() != null) {
                    return mc.getUser().getName();
                }
                return "Player";
            case "time":
            case "timeofday":
            case "timename":
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
            case "isday":
                if (mc.level != null) {
                    long t = (mc.level.getDayTime() % 24000L + 24000L) % 24000L;
                    return t < 12000;
                }
                return true;
            case "isnight":
                if (mc.level != null) {
                    long t = (mc.level.getDayTime() % 24000L + 24000L) % 24000L;
                    return t >= 13500 && t < 23000;
                }
                return false;
            case "rawtime":
            case "timeticks":
                if (mc.level != null) {
                    return (double) ((mc.level.getDayTime() % 24000L + 24000L) % 24000L);
                }
                return 0.0;
            case "day":
            case "days":
            case "daycount":
                if (mc.level != null) {
                    return (double) (mc.level.getDayTime() / 24000L);
                }
                return 0.0;
            case "weather":
            case "weathername":
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
            case "israining":
                return mc.level != null && mc.level.isRaining();
            case "isthundering":
                return mc.level != null && mc.level.isThundering();
            case "isclear":
                return mc.level != null && !mc.level.isRaining() && !mc.level.isThundering();
            case "cps":
            case "lmbcps":
            case "cpslmb":
                return (double) CpsTracker.getLmbCps();
            case "rmbcps":
            case "cpsrmb":
                return (double) CpsTracker.getRmbCps();
            case "target":
                return TargetTracker.getTargetNamespace();
            case "hastarget":
                return TargetTracker.hasTarget();
            case "potions":
            case "potionlist":
            case "effects":
            case "potioneffects":
            case "activepotions":
            case "activeeffects":
                return getPotions();
            case "minecraft_version":
            case "minecraftversion":
            case "mcversion":
                return SharedConstants.getCurrentVersion() != null ? SharedConstants.getCurrentVersion().name() : "1.21.11";
            case "inventory":
            case "playerinventory":
            case "inv":
                return getInventory();
            default:
                if (VariableRegistry.has(name)) {
                    return VariableRegistry.get(name);
                }
                return null;
        }
    }

    public List<Slot> getInventory() {
        List<Slot> list = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            Inventory inv = mc.player.getInventory();
            int size = inv.getContainerSize();
            for (int i = 0; i < size; i++) {
                list.add(new Slot(inv.getItem(i), i));
            }
        }
        return list;
    }

    public List<Potion> getPotions() {
        List<Potion> list = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            for (MobEffectInstance effect : mc.player.getActiveEffects()) {
                list.add(new Potion(effect));
            }
        }
        return list;
    }

    @Override
    public void setProperty(String name, Object value) {
    }
}
