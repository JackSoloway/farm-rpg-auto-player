package org.example.stringParser;

/** A missing or unconvertible value is null. Invalid numeric values throw instead. */
public record ParseResult<T>(T value, boolean found, boolean converted) {
    public ParseResult {
        if (converted && (!found || value == null)) {
            throw new IllegalArgumentException("A converted result must be found and have a value");
        }
        if (!converted && value != null) {
            throw new IllegalArgumentException("An unconverted result must not have a value");
        }
    }

    public boolean success() {
        return found && converted;
    }
}
