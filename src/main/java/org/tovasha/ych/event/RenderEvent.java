package org.tovasha.ych.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.tovasha.ych.api.CustomEvent;

@Getter
@AllArgsConstructor
public class RenderEvent implements CustomEvent {
    private final GuiGraphicsExtractor graphics;
    private final DeltaTracker deltaTracker;
    private final float partialTick;
}
