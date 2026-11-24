package org.example.stringParser;

/** The extracted text is invalid for the requested numeric type, or exceeds its range. */
public class NumberParsingException extends IllegalArgumentException {
    public NumberParsingException(String value, Class<?> targetType) {
        super("Cannot parse '" + value + "' as " + targetType.getSimpleName());
    }

    public NumberParsingException(String value, Class<?> targetType, Throwable cause) {
        super("Cannot parse '" + value + "' as " + targetType.getSimpleName(), cause);
    }
}
