package org.example.stringParser;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts the first template match and converts it to the type supplied at construction. */
public final class StringParser<T> {
    private static final Set<Class<?>> SUPPORTED_TYPES = Set.of(
            String.class, Integer.class, Long.class, Double.class, BigDecimal.class, Boolean.class);
    private static final Pattern INTEGER_TEXT = Pattern.compile("[0-9.,]+");
    private static final Pattern DECIMAL_TEXT = Pattern.compile("(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)");
    private final Class<T> targetType;

    public StringParser(Class<T> targetType) {
        this.targetType = Objects.requireNonNull(targetType, "targetType");
        if (!SUPPORTED_TYPES.contains(targetType)) {
            throw new IllegalArgumentException("Unsupported type: " + targetType.getTypeName());
        }
    }

    /**
     * The template is literal, including whitespace; valueRegex matches only the value.
     * Use non-capturing groups and avoid numeric backreferences in valueRegex: extraction
     * adds an enclosing capture group. Invalid regex syntax propagates to the caller.
     * Invalid numeric text (including signs and whitespace) or overflow throws
     * NumberParsingException. Boolean accepts only true/false, ignoring case;
     * other boolean text returns found=true, converted=false.
     */
    public ParseResult<T> tryGetValue(String text, StringTemplate template, String valueRegex) {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(valueRegex, "valueRegex");
        Pattern pattern = Pattern.compile(Pattern.quote(template.prefix())
                + "(" + valueRegex + ")" + Pattern.quote(template.suffix()));
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return new ParseResult<>(null, false, false);
        }
        String raw = matcher.group(1);
        Object value;
        if (targetType == String.class) {
            value = raw;
        } else if (targetType == Boolean.class) {
            if (!raw.equalsIgnoreCase("true") && !raw.equalsIgnoreCase("false")) {
                return new ParseResult<>(null, true, false);
            }
            value = Boolean.valueOf(raw);
        } else {
            value = parseNumber(raw);
        }
        return new ParseResult<>(targetType.cast(value), true, true);
    }

    private Object parseNumber(String raw) {
        boolean integral = targetType == Integer.class || targetType == Long.class;
        if (!(integral ? INTEGER_TEXT : DECIMAL_TEXT).matcher(raw).matches()) {
            throw new NumberParsingException(raw, targetType);
        }
        String normalized = integral
                ? raw.replace(".", "").replace(",", "")
                : raw.replace(',', '.');
        try {
            if (targetType == Integer.class) {
                return Integer.valueOf(normalized);
            }
            if (targetType == Long.class) {
                return Long.valueOf(normalized);
            }
            if (targetType == BigDecimal.class) {
                return new BigDecimal(normalized);
            }
            double value = Double.parseDouble(normalized);
            if (!Double.isFinite(value)) {
                throw new NumberParsingException(raw, targetType);
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new NumberParsingException(raw, targetType, exception);
        }
    }
}
