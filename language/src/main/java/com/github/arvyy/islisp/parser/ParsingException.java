package com.github.arvyy.islisp.parser;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.source.SourceSection;

/**
 * Indicates error during parsing stage.
 */
public class ParsingException extends RuntimeException {

    private final SourceSection sourceSection;
    private final String reason;

    /**
     * Create parsing exception.
     *
     * @param sourceSection violating source section
     * @param reason reason description
     */
    public ParsingException(SourceSection sourceSection, String reason) {
        super(makeMessage(sourceSection, reason));
        this.sourceSection = sourceSection;
        this.reason = reason;
    }

    @CompilerDirectives.TruffleBoundary
    private static String makeMessage(SourceSection sourceSection, String reason) {
        if (sourceSection == null) {
            return String.format("Failed to parse (source information unavailable). %s", reason);
        }
        return String.format(
                "Failed to parse at %s:%d:%d. %s",
                sourceSection.getSource().getName(),
                sourceSection.getStartLine(),
                sourceSection.getStartColumn(),
                reason);
    }

}
