package org.tovasha.ych.script;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class ScriptDiagnostic {
    private final String message;
    private final int line;
    private final int column;
    private final boolean error;
}
