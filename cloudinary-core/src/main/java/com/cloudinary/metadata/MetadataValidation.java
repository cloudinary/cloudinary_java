package com.cloudinary.metadata;

import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;

import java.util.Date;
import java.util.List;

public abstract class MetadataValidation extends JSONObject {

    public static class AndValidator extends MetadataValidation {
        public AndValidator(List<MetadataValidation> rules) {
            put("type", "and");
            put("rules", new JSONArray(rules));
        }
    }

    public static class StringLength extends MetadataValidation {
        public StringLength(Integer min, Integer max) {
            put("type", "strlen");
            put("min", min);
            put("max", max);
        }
    }

    abstract static class ComparisonRule<T> extends MetadataValidation {
        ComparisonRule(String type, T value) {
            this(type, value, null);
        }

        ComparisonRule(String type, T value, Boolean equals) {
            put("type", type);
            put("value", value);
            if (equals != null) {
                put("equals", equals);
            }
        }
    }

    public static class IntGreaterThan extends ComparisonRule<Integer> {
        public IntGreaterThan(Integer value) {
            super("greater_than", value);
        }

        public IntGreaterThan(Integer value, Boolean equals) {
            super("greater_than", value, equals);
        }
    }

    public static class DateGreaterThan extends ComparisonRule<Date> {
        public DateGreaterThan(Date value) {
            super("greater_than", value);
        }

        public DateGreaterThan(Date value, Boolean equals) {
            super("greater_than", value, equals);
        }
    }

    public static class IntLessThan extends ComparisonRule<Integer> {
        public IntLessThan(Integer value) {
            super("less_than", value);
        }

        public IntLessThan(Integer value, Boolean equals) {
            super("less_than", value, equals);
        }
    }

    public static class DateLessThan extends ComparisonRule<Date> {
        public DateLessThan(Date value) {
            super("less_than", value);
        }

        public DateLessThan(Date value, Boolean equals) {
            super("less_than", value, equals);
        }
    }
}

