package com.cloudinary.transformation;

import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines an expression used in transformation parameter values
 *
 * @param <T> Children must define themselves as T
 */
public abstract class BaseExpression<T extends BaseExpression> {
    public static final Map<String, String> OPERATORS = ObjectUtils.asMap(
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
            "-", "sub",
            "^", "pow"
    );
    public static final Map<String, String> PREDEFINED_VARS = ObjectUtils.asMap(
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
            "pageY", "py",
            "duration","du",
            "initial_duration","idu",
            "initialDuration","idu"

    );
    private static final Pattern PATTERN = getPattern();

    protected List<String> expressions = null;
    protected Transformation parent = null;

    protected BaseExpression() {
        expressions = new ArrayList<String>();
    }

    /**
     * Normalize an expression string, replace "nice names" with their coded values and spaces with "_".
     *
     * @param expression an expression
     * @return a parsed expression
     */
    public static String normalize(Object expression) {
        if (expression == null) {
            return null;
        }

        // If it's a number it's not an expression
        if (expression instanceof Number){
            return String.valueOf(expression);
        }

        String replacement;
        String conditionStr = StringUtils.mergeToSingleUnderscore(String.valueOf(expression));
        Matcher matcher = PATTERN.matcher(conditionStr);
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
     * @return a regex pattern for operators and predefined vars as /((operators)(?=[ _])|variables)/
     */
    private static Pattern getPattern() {
        String pattern;
        final ArrayList<String> operators = new ArrayList<String>(OPERATORS.keySet());
        Collections.sort(operators, Collections.<String>reverseOrder());
        StringBuilder sb = new StringBuilder("((");
        for (String op : operators) {
            sb.append(Pattern.quote(op)).append("|");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")(?=[ _])|").append(StringUtils.join(PREDEFINED_VARS.keySet(), "|")).append(")");
        pattern = sb.toString();
        return Pattern.compile(pattern);
    }

    public Transformation getParent() {
        return parent;
    }

    public T setParent(Transformation parent) {
        this.parent = parent;
        return (T) this;
    }

    public String serialize() {
        return normalize(StringUtils.join(expressions, "_"));
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

    /**
     * Utility shortcut method which invokes on this Expression instance {@link #pow()} method, takes its result and
     * invokes {@link #value(Object)} method on it. Effectively, invocation of this shortcut results in
     * "to the power of value" sub-expression added to the end of current expression instance.
     *
     * @param value argument for {@link #value(Object)} call
     * @return result of {@link #value(Object)} call
     */
    public T pow(Object value) {
        return (T) pow().value(value);
    }

    /**
     * Adds "to the power of" sub-expression to the end of the list of already present sub-expressions in this
     * expression instance.
     *
     * @return this expression instance
     */
    public T pow() {
        expressions.add("pow");
        return (T) this;
    }

    public T value(Object value) {
        expressions.add(String.valueOf(value));
        return (T) this;
    }
}
