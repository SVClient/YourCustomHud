package org.tovasha.ych.script;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReturnException extends RuntimeException {
    private final Object value;
}
