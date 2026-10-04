package org.tovasha.ych.render;

import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.tovasha.ych.script.builtins.BuiltinTarget;

public class TargetTracker {
    private static LivingEntity currentTarget = null;
    private static final BuiltinTarget TARGET_NAMESPACE = new BuiltinTarget();

    public static BuiltinTarget getTargetNamespace() {
        return TARGET_NAMESPACE;
    }

    public static LivingEntity getCurrentTarget() {
        update();
        return currentTarget;
    }

    public static boolean hasTarget() {
        update();
        return currentTarget != null;
    }

    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            currentTarget = null;
            return;
        }

        Entity crosshair = mc.crosshairPickEntity;
        if (crosshair instanceof LivingEntity living && living != mc.player && living.isAlive() && !(living instanceof ArmorStand)) {
            currentTarget = living;
        } else {
            Vec3 eyePos = mc.player.getEyePosition(1.0f);
            Vec3 lookVec = mc.player.getViewVector(1.0f);
            Vec3 reachVec = eyePos.add(lookVec.scale(6.0));
            double bestDist = 6.0;
            LivingEntity found = null;

            List<LivingEntity> entities = mc.level.getEntitiesOfClass(LivingEntity.class,
                    mc.player.getBoundingBox().inflate(6.0),
                    other -> other != mc.player && other.isAlive() && !(other instanceof ArmorStand));
            for (LivingEntity other : entities) {
                if (other.distanceTo(mc.player) <= 6.0) {
                    AABB box = other.getBoundingBox().inflate(0.3);
                    if (box.clip(eyePos, reachVec).isPresent()) {
                        double d = other.distanceTo(mc.player);
                        if (d < bestDist) {
                            bestDist = d;
                            found = other;
                        }
                    }
                }
            }

            if (found != null) {
                currentTarget = found;
            }
        }

        if (currentTarget != null) {
            if (!currentTarget.isAlive() || currentTarget.level() != mc.player.level() || mc.player.distanceTo(currentTarget) > 6.0) {
                currentTarget = null;
            }
        }
    }

    public static double[] computeScreenPos(Entity target) {
        Minecraft mc = Minecraft.getInstance();
        if (target == null || mc == null || mc.gameRenderer == null) {
            return new double[]{-9999.0, -9999.0};
        }
        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) {
            return new double[]{-9999.0, -9999.0};
        }

        Vec3 camPos = camera.position();
        double dx = target.getX() - camPos.x;
        double dy = (target.getY() + target.getBbHeight() * 0.75) - camPos.y;
        double dz = target.getZ() - camPos.z;

        Vector3fc forward = camera.forwardVector();
        Vector3fc up = camera.upVector();
        Vector3fc left = camera.leftVector();

        double camZ = dx * forward.x() + dy * forward.y() + dz * forward.z();
        if (camZ <= 0.05) {
            return new double[]{-9999.0, -9999.0};
        }

        double camX = -(dx * left.x() + dy * left.y() + dz * left.z());
        double camY = dx * up.x() + dy * up.y() + dz * up.z();

        double fov = mc.options != null ? mc.options.fov().get() : 70.0;
        double halfFovRad = Math.toRadians(fov) / 2.0;
        double focal = 1.0 / Math.tan(halfFovRad);

        int screenWidth = mc.getWindow() != null ? mc.getWindow().getGuiScaledWidth() : 854;
        int screenHeight = mc.getWindow() != null ? mc.getWindow().getGuiScaledHeight() : 480;
        double aspect = (double) screenWidth / (double) Math.max(1, screenHeight);

        double projX = (camX / camZ) * (focal / aspect);
        double projY = (camY / camZ) * focal;

        double screenX = (screenWidth / 2.0) + projX * (screenWidth / 2.0);
        double screenY = (screenHeight / 2.0) - projY * (screenHeight / 2.0);

        return new double[]{Math.round(screenX * 10.0) / 10.0, Math.round(screenY * 10.0) / 10.0};
    }

    public static PlayerSkin getTargetSkin() {
        LivingEntity target = getCurrentTarget();
        if (target instanceof AbstractClientPlayer clientPlayer) {
            return clientPlayer.getSkin();
        }
        if (target instanceof Player player) {
            return DefaultPlayerSkin.get(player.getUUID());
        }
        return DefaultPlayerSkin.getDefaultSkin();
    }

    public static ItemStack getTargetLeftHandItem() {
        LivingEntity target = getCurrentTarget();
        if (target == null) return ItemStack.EMPTY;
        return target.getMainArm() == HumanoidArm.RIGHT ? target.getOffhandItem() : target.getMainHandItem();
    }

    public static ItemStack getTargetRightHandItem() {
        LivingEntity target = getCurrentTarget();
        if (target == null) return ItemStack.EMPTY;
        return target.getMainArm() == HumanoidArm.RIGHT ? target.getMainHandItem() : target.getOffhandItem();
    }
}
