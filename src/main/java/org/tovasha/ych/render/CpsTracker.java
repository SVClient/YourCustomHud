package org.tovasha.ych.render;

import java.util.ArrayDeque;
import java.util.Deque;

public class CpsTracker {
    private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();

    public static synchronized void onClick(int button) {
        long now = System.currentTimeMillis();
        if (button == 0) {
            LEFT_CLICKS.addLast(now);
        } else if (button == 1) {
            RIGHT_CLICKS.addLast(now);
        }
    }

    public static synchronized int getLmbCps() {
        cleanOld(LEFT_CLICKS);
        return LEFT_CLICKS.size();
    }

    public static synchronized int getRmbCps() {
        cleanOld(RIGHT_CLICKS);
        return RIGHT_CLICKS.size();
    }

    private static void cleanOld(Deque<Long> clicks) {
        long threshold = System.currentTimeMillis() - 1000L;
        while (!clicks.isEmpty() && clicks.peekFirst() < threshold) {
            clicks.pollFirst();
        }
    }
}
