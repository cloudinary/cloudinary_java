package com.cloudinary.metadata;

import com.cloudinary.utils.ObjectUtils;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Represents the base class for metadata fields validation mechanisms.
 */
public abstract class MetadataValidation extends JSONObject {

    public static final String TYPE = "type";
    public static final String MIN = "min";
    public static final String MAX = "max";
    public static final String STRLEN = "strlen";
    public static final String EQUALS = "equals";
    public static final String GREATER_THAN = "greater_than";
    public static final String LESS_THAN = "less_than";
    public static final String VALUE = "value";

    /**
     * An 'And' rule validation used to combine other rules with an 'AND' logic relation between them.
     */
    public static class AndValidator extends MetadataValidation {

        public static final String AND = "and";

        /**
         * Create a new instance of the validator with the given rules.
         * @param rules The rules to use.
         */
        public AndValidator(List<MetadataValidation> rules) {
            put(TYPE, AND);
            put("rules", new JSONArray(rules.toArray()));
        }
    }

    /**
     * A validator to validate string lengths
     */
    public static class StringLength extends MetadataValidation {
        /**
         * Create a new instance with the given min and max.
         * @param min Minimum valid string length.
         * @param max Maximum valid string length.
         */
        public StringLength(Integer min, Integer max) {
            put(TYPE, STRLEN);
            put(MIN, min);
            put(MAX, max);
        }
    }

    /**
     * Base class for all comparison (greater than/less than) validation rules.
     * @param <T>
     */
    abstract static class ComparisonRule<T> extends MetadataValidation {
        ComparisonRule(String type, T value) {
            this(type, value, null);
        }

        ComparisonRule(String type, T value, Boolean equals) {
            put(TYPE, type);
            putValue(value);
            if (equals != null) {
                put(EQUALS, equals);
            }
        }

        protected void putValue(T value) {
            put(VALUE, value);
        }
    }

    /**
     * Great-than rule for integers.
     */
    public static class IntGreaterThan extends ComparisonRule<Integer> {
        /**
         * Create a new rule with the given integer.
         * @param value The integer to reference in the rule
         */
        public IntGreaterThan(Integer value) {
            super(GREATER_THAN, value);
        }

        /**
         * Create a new rule with the given integer.
         * @param value The integer to reference in the rule.
         * @param equals Whether a field value equal to the rule value is considered valid.
         */
        public IntGreaterThan(Integer value, Boolean equals) {
            super(GREATER_THAN, value, equals);
        }
    }

    /**
     * Great-than rule for dates.
     */
    public static class DateGreaterThan extends ComparisonRule<Date> {
        /**
         * Create a new rule with the given date.
         * @param value The integer to reference in the rule
         */
        public DateGreaterThan(Date value) {
            super(GREATER_THAN, value);
        }

        /**
         * Create a new rule with the given date.
         * @param value The date to reference in the rule.
         * @param equals Whether a field value equal to the rule value is considered valid.
         */
        public DateGreaterThan(Date value, Boolean equals) {
            super(GREATER_THAN, value, equals);
        }

        @Override
        protected void putValue(Date value) {
            put(VALUE, ObjectUtils.toISO8601DateOnly(value));
        }
    }

    /**
     * Less-than rule for integers.
     */
    public static class IntLessThan extends ComparisonRule<Integer> {
        /**
         * Create a new rule with the given integer.
         * @param value The integer to reference in the rule
         */
        public IntLessThan(Integer value) {
            super(LESS_THAN, value);
        }

        /**
         * Create a new rule with the given integer.
         * @param value The integer to reference in the rule.
         * @param equals Whether a field value equal to the rule value is considered valid.
         */
        public IntLessThan(Integer value, Boolean equals) {
            super(LESS_THAN, value, equals);
        }
    }

    /**
     * Less-than rule for dates.
     */
    public static class DateLessThan extends ComparisonRule<Date> {
        /**
         * Create a new rule with the given date.
         * @param value The integer to reference in the rule
         */
        public DateLessThan(Date value) {
            super(LESS_THAN, value);
        }

        /**
         * Create a new rule with the given date.
         * @param value The date to reference in the rule.
         * @param equals Whether a field value equal to the rule value is considered valid.
         */
        public DateLessThan(Date value, Boolean equals) {
            super(LESS_THAN, value, equals);
        }

        @Override
        protected void putValue(Date value) {
            put(VALUE, ObjectUtils.toISO8601DateOnly(value));
        }
    }
}

