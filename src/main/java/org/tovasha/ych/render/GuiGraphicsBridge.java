package org.tovasha.ych.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiRenderState;

public interface GuiGraphicsBridge {
    GuiRenderState ych$getGuiRenderState();
    ScreenRectangle ych$getCurrentScissor();
}
