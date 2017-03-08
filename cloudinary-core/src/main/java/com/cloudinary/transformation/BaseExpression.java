package com.cloudinary.transformation;

import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines an expression used in transformation parameter values
 * @param <T> Children must define themselves as T
 */
public abstract class BaseExpression<T extends BaseExpression> {
    public static final Map<String,String> OPERATORS = ObjectUtils.asMap(
            "=", "eq",
            "!=", "ne",
            "<", "lt",
            ">", "gt",
            "<=", "lte",
            ">=", "gte",
            "&&", "and",
            "||", "or",
            "*", "mul",
            "/", "div",
            "+", "add",
            "-", "sub"
    );
    public static final Map<String,String> PREDEFINED_VARS = ObjectUtils.asMap(
            "width", "w",
            "height", "h",
            "initialWidth", "iw",
            "initialHeight", "ih",
            "aspect_ratio", "ar",
            "initial_aspect_ratio", "iar",
            "aspectRatio", "ar",
            "initialAspectRatio", "iar",
            "page_count", "pc",
            "pageCount", "pc",
            "face_count", "fc",
            "faceCount", "fc",
            "current_page", "cp",
            "currentPage", "cp",
            "tags", "tags",
            "pageX", "px",
            "pageY", "py"

    );
    private static final String PATTERN = getpattern();

    protected List<String> expressions = null;
    protected Transformation parent = null;

    protected BaseExpression() {
        expressions = new ArrayList<String>();
    }

    /**
     * Normalize an expression string, replace "nice names" with their coded values and spaces with "_".
     * @param expresion an expression
     * @return a parsed expression
     */
    public static String normalize(Object expresion) {

        String replacement;
        if (expresion == null) {
            return null;
        }
        String conditionStr = String.valueOf(expresion);
        conditionStr = conditionStr.replaceAll("[ _]+", "_");
        Pattern replaceRE = Pattern.compile(PATTERN);
        Matcher matcher = replaceRE.matcher(conditionStr);
        StringBuffer result = new StringBuffer(conditionStr.length());
        while (matcher.find()) {
            if (OPERATORS.containsKey(matcher.group())) {
                replacement = (String) OPERATORS.get(matcher.group());
            } else if (PREDEFINED_VARS.containsKey(matcher.group())) {
                replacement = (String) PREDEFINED_VARS.get(matcher.group());
            } else {
                replacement = matcher.group();
            }
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * @return a regex pattern for operators and predefined vars
     */
    private static String getpattern() {
        String pattern;
        final ArrayList<String> operators = new ArrayList<String>(OPERATORS.keySet());
        Collections.sort(operators, Collections.<String>reverseOrder());
        StringBuffer sb = new StringBuffer();
        for(String op: operators) {
            sb.append("|").append(Pattern.quote(op));
        }
        pattern = "(" + StringUtils.join(PREDEFINED_VARS.keySet(), "|") + sb.toString() + ")";
        return pattern;
    }

    public Transformation getParent() {
        return parent;
    }

    public T setParent(Transformation parent) {
        this.parent = parent;
        return (T) this;
    }

    public String serialize() {
        return StringUtils.join(expressions, "_");
    }

    @Override
    public String toString() {
        return serialize();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public T clone() {
        T newCondition = newInstance();
        newCondition.expressions.addAll(expressions);
        newCondition.parent = parent;
        return newCondition;
    }

    public T multiple(Object value) {
        expressions.add("mul");
        expressions.add(value.toString());
        return (T) this;
    }

    abstract protected T newInstance();

    public T gt(Object value) {
        return (T) this.gt().value(value);
    }

    public T gt() {
        expressions.add("gt");
        return (T) this;
    }

    public T and(Object value) {
        return (T) and().value(value);
    }

    public T and() {
        expressions.add("and");
        return (T) this;
    }

    public T or(Object value) {
        return (T) or().value(value);
    }

    public T or() {
        expressions.add("or");
        return (T) this;
    }

    public T eq(Object value) {
        return (T) eq().value(value);
    }

    public T eq() {
        expressions.add("eq");
        return (T) this;
    }

    public T ne(Object value) {
        return (T) ne().value(value);
    }

    public T ne() {
        expressions.add("ne");
        return (T) this;
    }

    public T lt(Object value) {
        return (T) lt().value(value);
    }

    public T lt() {
        expressions.add("lt");
        return (T) this;
    }

    public T lte(Object value) {
        return (T) lte().value(value);
    }

    public T lte() {
        expressions.add("lte");
        return (T) this;
    }

    public T gte(Object value) {
        return (T) gte().value(value);
    }

    public T gte() {
        expressions.add("gte");
        return (T) this;
    }

    public T div(Object value) {
        return (T) div().value(value);
    }

    public T div() {
        expressions.add("div");
        return (T) this;
    }

    public T add(Object value) {
        return (T) add().value(value);
    }

    public T add() {
        expressions.add("add");
        return (T) this;
    }

    public T sub(Object value) {
        return (T) sub().value(value);
    }

    public T sub() {
        expressions.add("sub");
        return (T) this;
    }

    public T value(Object value) {
        expressions.add(String.valueOf(value));
        return (T) this;
    }
}
