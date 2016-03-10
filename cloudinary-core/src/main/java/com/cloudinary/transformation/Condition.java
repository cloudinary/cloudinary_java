package com.cloudinary.transformation;

import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Condition {
    public static final Map OPERATORS = ObjectUtils.asMap(
            "=", "eq",
            "!=", "ne",
            "<", "lt",
            ">", "gt",
            "<=", "lte",
            ">=", "gte",
            "&&", "and",
            "||", "or");

    protected List<String> predicateList = null;
    private Transformation parent = null;

    public Condition( ) {
        predicateList = new ArrayList<String>();

    }
    public Condition( String conditionStr) {
        this();
        if (conditionStr != null) {
            predicateList.add( literal(conditionStr));
        }
    }

    private String literal(String conditionStr) {
        String[] list = conditionStr.split("[ _]+");
        String[] translated = new String[list.length];
        for (int i = 0, j = 0; i < list.length; i++) {
            String s = list[i];
            if (OPERATORS.containsKey(s)) {
                translated[j++] = (String) OPERATORS.get(s);
            } else {
                translated[j++] = s;
            }
        }
        return StringUtils.join(translated, "_");
    }
    public Transformation getParent() { return parent;}
    public Condition setParent( Transformation parent) {
        this.parent = parent;
        return this;
    }

    public String serialize() { return StringUtils.join(predicateList, "_");}

    @Override
    public String toString() {
        return serialize();
    }

    protected Condition predicate( String name, String operator, String value) {
        if (OPERATORS.containsKey(operator)) {
            operator = (String) OPERATORS.get(operator);
        }
        predicateList.add(String.format("%s_%s_%s",name, operator, value));
        return this;
    }

    public Condition and() {
        predicateList.add("and");
        return this;
    }

    public Condition or() {
        predicateList.add("or");
        return this;
    }

    public Transformation then() {
        getParent().ifCondition( serialize());
        return getParent();
    }

    public Condition width(String operator, Object value) {
        predicateList.add("w_"+ operator + "_" + value);
        return this;
    }

    public Condition height(String operator, Object value) {
        predicateList.add("h_"+ operator + "_" + value);
        return this;
    }

    public Condition aspectRatio(String operator, Object value) {
        predicateList.add("ar_"+ operator + "_" + value);
        return this;
    }


    public Condition pages(String operator, Object value) {
        predicateList.add("pg_"+ operator + "_" + value);
        return this;
    }
}
