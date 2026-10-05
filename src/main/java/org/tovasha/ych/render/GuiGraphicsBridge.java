package org.tovasha.ych.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

public interface GuiGraphicsBridge {
    GuiRenderState ych$getGuiRenderState();
    ScreenRectangle ych$getCurrentScissor();
}
