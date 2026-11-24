package org.example.stringParser;

/** Literal text containing exactly one {@code ***} marker for the value to extract. */
public record StringTemplate(String template) {
    public static final String VALUE_MARKER = "***";

    public StringTemplate {
        if (template == null) {
            throw new IllegalArgumentException("Template must not be null");
        }
        int marker = template.indexOf(VALUE_MARKER);
        if (marker < 0 || template.indexOf(VALUE_MARKER, marker + 1) >= 0) {
            throw new IllegalArgumentException("Template must contain exactly one *** marker");
        }
    }

    public String prefix() {
        return template.substring(0, template.indexOf(VALUE_MARKER));
    }

    public String suffix() {
        return template.substring(template.indexOf(VALUE_MARKER) + VALUE_MARKER.length());
    }
}
