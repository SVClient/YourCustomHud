package org.tovasha.ych.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.tovasha.ych.api.CustomEvent;
import org.tovasha.ych.script.builtins.BuiltinTarget;

@Getter
@AllArgsConstructor
public class AttackEvent implements CustomEvent {
    private final BuiltinTarget target;
}
