package com.cloudinary.transformation;

import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a condition for {@link Transformation#ifCondition()}
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

    public static final Map PARAMETERS = ObjectUtils.asMap(
            "width", "w",
            "height", "h",
            "aspect_ratio", "ar",
            "aspectRatio", "ar",
            "page_count", "pc",
            "pageCount", "pc",
            "face_count", "fc",
            "faceCount", "fc"
    );

    protected List<String> predicateList = null;
    private Transformation parent = null;

    public Condition() {
        predicateList = new ArrayList<String>();

    }

    /**
     * Create a Condition Object. The conditionStr string will be translated to a serialized condition.
     *
     * @param conditionStr condition in string format
     */
    public Condition(String conditionStr) {
        this();
        if (conditionStr != null) {
            predicateList.add(literal(conditionStr));
        }
    }

    private String literal(String conditionStr) {

        String replacement;
        conditionStr = conditionStr.replaceAll("[ _]+", "_");
        Pattern replaceRE = Pattern.compile("(" + StringUtils.join(PARAMETERS.keySet(), "|") + "|[=<>&|!]+)");
        Matcher matcher = replaceRE.matcher(conditionStr);
        StringBuffer result = new StringBuffer(conditionStr.length());
        while (matcher.find()) {
            if (OPERATORS.containsKey(matcher.group())) {
                replacement = (String) OPERATORS.get(matcher.group());
            } else if (PARAMETERS.containsKey(matcher.group())) {
                replacement = (String) PARAMETERS.get(matcher.group());
            } else {
                replacement = matcher.group();
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public Transformation getParent() { return parent;}

    public Condition setParent(Transformation parent) {
        this.parent = parent;
        return this;
    }

    public String serialize() { return StringUtils.join(predicateList, "_");}

    @Override
    public String toString() {
        return serialize();
    }

    protected Condition predicate(String name, String operator, String value) {
        if (OPERATORS.containsKey(operator)) {
            operator = (String) OPERATORS.get(operator);
        }
        predicateList.add(String.format("%s_%s_%s", name, operator, value));
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

    /**
     * Terminates the definition of the condition and continue with Transformation definition.
     * @return the Transformation object this Condition is attached to.
     */
    public Transformation then() {
        getParent().ifCondition(serialize());
        return getParent();
    }

    public Condition width(String operator, Object value) {
        predicateList.add("w_" + operator + "_" + value);
        return this;
    }

    public Condition height(String operator, Object value) {
        predicateList.add("h_" + operator + "_" + value);
        return this;
    }

    public Condition aspectRatio(String operator, Object value) {
        predicateList.add("ar_" + operator + "_" + value);
        return this;
    }

    /**
     * @deprecated Use {@link #faceCount(String, Object)} instead
     */
    public Condition faces(String operator, Object value) {
        return faceCount(operator, value);
    }

    public Condition faceCount(String operator, Object value) {
        predicateList.add("fc_" + operator + "_" + value);
        return this;
    }

    /**
     * @deprecated Use {@link #pageCount(String, Object)} instead
     */
    public Condition pages(String operator, Object value) {
        return pageCount(operator, value);
    }

    public Condition pageCount(String operator, Object value) {
        predicateList.add("pc_" + operator + "_" + value);
        return this;
    }
}
