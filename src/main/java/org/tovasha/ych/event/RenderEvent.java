package org.tovasha.ych.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.tovasha.ych.api.CustomEvent;

@Getter
@AllArgsConstructor
public class RenderEvent implements CustomEvent {
    private final GuiGraphics graphics;
    private final DeltaTracker deltaTracker;
    private final float partialTick;
}
