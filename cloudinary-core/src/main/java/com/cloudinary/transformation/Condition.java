package com.cloudinary.transformation;

import com.cloudinary.Transformation;

/**
 * Represents a condition for {@link Transformation#ifCondition()}
 */
public class Condition extends BaseExpression<Condition> {

    public Condition() {
        super();

    }

    /**
     * Create a Condition Object. The conditionStr string will be translated to a serialized condition.
     * <br>
     * For example, <code>new Condition("fc &gt; 3")</code>
     * @param conditionStr condition in string format
     */
    public Condition(String conditionStr) {
        this();
        if (conditionStr != null) {
            expressions.add(normalize(conditionStr));
        }
    }

    @Override
    protected Condition newInstance() {
        return new Condition();
    }

    protected Condition predicate(String name, String operator, Object value) {
        if (OPERATORS.containsKey(operator)) {
            operator = (String) OPERATORS.get(operator);
        }
        expressions.add(String.format("%s_%s_%s", name, operator, value));
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
        return predicate("w", operator, value);
    }

    public Condition height(String operator, Object value) {
        return predicate("h", operator, value);
    }

    public Condition aspectRatio(String operator, Object value) {
        return predicate("ar", operator, value);
    }
    public Condition duration(String operator, Object value) {
        return predicate("du", operator, value);
    }
    public Condition initialDuration(String operator, Object value) {
        return predicate("idu", operator, value);
    }

    public Condition faceCount(String operator, Object value) {
        return predicate("fc", operator, value);
    }

    public Condition pageCount(String operator, Object value) {
        return predicate("pc", operator, value);
    }

}
