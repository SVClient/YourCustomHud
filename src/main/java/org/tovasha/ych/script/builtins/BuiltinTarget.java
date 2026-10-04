package org.tovasha.ych.script.builtins;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import org.tovasha.ych.render.TargetTracker;
import org.tovasha.ych.script.Interpreter;
import org.tovasha.ych.script.ScriptCallable;

public class BuiltinTarget implements ScriptNamespace {
    private final Entity entity;
    private final Map<String, ScriptCallable> methods = new HashMap<>();

    public BuiltinTarget() {
        this(null);
    }

    public BuiltinTarget(Entity entity) {
        this.entity = entity;

        registerMethod("getname", args -> getName());
        registerMethod("name", args -> getName());

        registerMethod("getheadtexture", args -> getHeadTexture());
        registerMethod("headtexture", args -> getHeadTexture());
        registerMethod("head", args -> getHeadTexture());

        registerMethod("getscreenx", args -> getScreenX());
        registerMethod("screenx", args -> getScreenX());

        registerMethod("getscreeny", args -> getScreenY());
        registerMethod("screeny", args -> getScreenY());

        registerMethod("gethealth", args -> getHealth());
        registerMethod("health", args -> getHealth());

        registerMethod("getmaxhealth", args -> getMaxHealth());
        registerMethod("maxhealth", args -> getMaxHealth());

        registerMethod("getdistance", args -> getDistance());
        registerMethod("distance", args -> getDistance());

        registerMethod("getlefthanditemtexture", args -> getLeftHandItemTexture());
        registerMethod("lefthanditemtexture", args -> getLeftHandItemTexture());
        registerMethod("offhandtexture", args -> getLeftHandItemTexture());

        registerMethod("getrighthanditemtexture", args -> getRightHandItemTexture());
        registerMethod("righthanditemtexture", args -> getRightHandItemTexture());
        registerMethod("mainhandtexture", args -> getRightHandItemTexture());

        registerMethod("exists", args -> exists());
        registerMethod("isvalid", args -> exists());
    }

    private void registerMethod(String name, Function<List<Object>, Object> func) {
        methods.put(name.toLowerCase(), new ScriptCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return func.apply(arguments);
            }

            @Override
            public String toString() {
                return String.valueOf(func.apply(Collections.emptyList()));
            }
        });
    }

    private Entity getTargetEntity() {
        if (entity != null) {
            return entity;
        }
        return TargetTracker.getCurrentTarget();
    }

    @Override
    public Object getProperty(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase();
        if (methods.containsKey(lower)) {
            return methods.get(lower);
        }
        switch (lower) {
            case "name":
                return getName();
            case "health":
                return getHealth();
            case "maxhealth":
                return getMaxHealth();
            case "screenx":
                return getScreenX();
            case "screeny":
                return getScreenY();
            case "headtexture":
                return getHeadTexture();
            case "lefthanditemtexture":
                return getLeftHandItemTexture();
            case "righthanditemtexture":
                return getRightHandItemTexture();
            case "distance":
                return getDistance();
            case "exists":
            case "hastarget":
            case "isvalid":
                return exists();
            default:
                return null;
        }
    }

    public boolean exists() {
        Entity target = getTargetEntity();
        return target != null && target.isAlive();
    }

    public String getName() {
        Entity target = getTargetEntity();
        if (target == null) return "";
        return target.getName().getString();
    }

    public String getHeadTexture() {
        Entity target = getTargetEntity();
        if (target == null) return "";
        if (target instanceof Player player) {
            PlayerSkin skin = player instanceof AbstractClientPlayer clientPlayer
                    ? clientPlayer.getSkin()
                    : DefaultPlayerSkin.get(player.getUUID());
            return skin.body().id().toString();
        }
        return DefaultPlayerSkin.getDefaultTexture().toString();
    }

    public double getScreenX() {
        Entity target = getTargetEntity();
        if (target == null) return -9999.0;
        return TargetTracker.computeScreenPos(target)[0];
    }

    public double getScreenY() {
        Entity target = getTargetEntity();
        if (target == null) return -9999.0;
        return TargetTracker.computeScreenPos(target)[1];
    }

    public double getHealth() {
        Entity target = getTargetEntity();
        if (target instanceof LivingEntity living) {
            return Math.round(living.getHealth() * 10.0) / 10.0;
        }
        return 0.0;
    }

    public double getMaxHealth() {
        Entity target = getTargetEntity();
        if (target instanceof LivingEntity living) {
            return Math.round(living.getMaxHealth() * 10.0) / 10.0;
        }
        return 20.0;
    }

    public double getDistance() {
        Entity target = getTargetEntity();
        Minecraft mc = Minecraft.getInstance();
        if (target == null || mc.player == null) return 0.0;
        return Math.round(mc.player.distanceTo(target) * 10.0) / 10.0;
    }

    public String getLeftHandItemTexture() {
        Entity target = getTargetEntity();
        if (target instanceof LivingEntity living) {
            ItemStack item = living.getMainArm() == HumanoidArm.RIGHT ? living.getOffhandItem() : living.getMainHandItem();
            if (item.isEmpty()) return "";
            return BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
        }
        return "";
    }

    public String getRightHandItemTexture() {
        Entity target = getTargetEntity();
        if (target instanceof LivingEntity living) {
            ItemStack item = living.getMainArm() == HumanoidArm.RIGHT ? living.getMainHandItem() : living.getOffhandItem();
            if (item.isEmpty()) return "";
            return BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
        }
        return "";
    }

    public Entity getEntity() {
        return getTargetEntity();
    }

    @Override
    public void setProperty(String name, Object value) {
    }

    @Override
    public String toString() {
        return getName();
    }
}
