package org.tovasha.ych.render;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public class TpsTracker {
    private static final double[] SAMPLES = new double[20];
    private static int sampleIndex = 0;
    private static int sampleCount = 0;
    private static long lastPacketTime = -1;
    private static long lastGameTime = -1;

    static {
        Arrays.fill(SAMPLES, 20.0);
    }

    public static void onTimePacket(long gameTime) {
        long now = System.currentTimeMillis();
        if (lastPacketTime != -1 && lastGameTime != -1) {
            long timeDiff = now - lastPacketTime;
            long tickDiff = gameTime - lastGameTime;
            if (timeDiff > 0 && tickDiff > 0) {
                double tps = (tickDiff * 1000.0) / timeDiff;
                tps = Math.max(0.0, Math.min(20.0, tps));
                SAMPLES[sampleIndex] = tps;
                sampleIndex = (sampleIndex + 1) % SAMPLES.length;
                if (sampleCount < SAMPLES.length) {
                    sampleCount++;
                }
            }
        }
        lastPacketTime = now;
        lastGameTime = gameTime;
    }

    public static void reset() {
        lastPacketTime = -1;
        lastGameTime = -1;
        sampleIndex = 0;
        sampleCount = 0;
        Arrays.fill(SAMPLES, 20.0);
    }

    public static double getTps() {
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (server != null) {
            double mspt = server.getAverageTickTimeNanos() / 1_000_000.0;
            if (mspt <= 50.0) {
                return 20.0;
            }
            return Math.round((1000.0 / mspt) * 10.0) / 10.0;
        }

        if (sampleCount == 0) {
            return 20.0;
        }

        double sum = 0.0;
        for (int i = 0; i < sampleCount; i++) {
            sum += SAMPLES[i];
        }
        double avg = sum / sampleCount;
        return Math.round(avg * 10.0) / 10.0;
    }
}
