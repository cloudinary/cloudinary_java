package com.cloudinary.transformation;

/**
 * Represents a transformation parameter expression.
 */
public class Expression extends BaseExpression<Expression> {

    private boolean predefined = false;

    public Expression(){
        super();
    }

    public Expression(String name){
        super();
        expressions.add(name);
    }

    public static Expression variable(String name, Object value){
        Expression var = new Expression(name);
        var.expressions.add(value.toString());
        return var;
    }

    public static Expression faceCount() {
        return new Expression("fc");
    }

    @Override
    protected Expression newInstance() {
        return new Expression();
    }
    /*
    * @returns a new expression with the predefined variable "width"
     */
    public static Expression width() {
        return new Expression("width");
    }
    /*
    * @returns a new expression with the predefined variable "height"
     */
    public static Expression height() {
        return new Expression("height");
    }
    /*
    * @returns a new expression with the predefined variable "initialWidth"
     */
    public static Expression initialWidth() {
        return new Expression("initialWidth");
    }
    /*
    * @returns a new expression with the predefined variable "initialHeight"
     */
    public static Expression initialHeight() {
        return new Expression("initialHeight");
    }
    /*
    * @returns a new expression with the predefined variable "aspectRatio"
     */
    public static Expression aspectRatio() {
        return new Expression("aspectRatio");
    }
    /*
    * @returns a new expression with the predefined variable "initialAspectRatio"
     */
    public static Expression initialAspectRatio() {
        return new Expression("initialAspectRatio");
    }
    /*
    * @returns a new expression with the predefined variable "pageCount"
     */
    public static Expression pageCount() {
        return new Expression("pageCount");
    }
    /*
    * @returns a new expression with the predefined variable "currentPage"
     */
    public static Expression currentPage() {
        return new Expression("currentPage");
    }
    /*
    * @returns a new expression with the predefined variable "tags"
     */
    public static Expression tags() {
        return new Expression("tags");
    }
    /*
    * @returns a new expression with the predefined variable "pageX"
     */
    public static Expression pageX() {
        return new Expression("pageX");
    }
    /*
    * @returns a new expression with the predefined variable "pageY"
     */
    public static Expression pageY() {
        return new Expression("pageY");
    }
}
