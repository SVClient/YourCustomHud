package org.tovasha.ych.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.tovasha.ych.api.CustomEvent;

@Getter
@AllArgsConstructor
public class InputEvent implements CustomEvent {
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;
}
