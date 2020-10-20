package com.cloudinary;

import com.cloudinary.transformation.Condition;
import com.cloudinary.transformation.TextLayer;
import com.cloudinary.utils.ObjectUtils;
import org.cloudinary.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.cloudinary.transformation.Expression.faceCount;
import static com.cloudinary.transformation.Expression.variable;
import static org.junit.Assert.*;

/**
 *
 */
@SuppressWarnings("unchecked")
public class TransformationTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void withLiteral() throws Exception {
        Transformation transformation = new Transformation().ifCondition("w_lt_200").crop("fill").height(120).width(80);
        assertEquals("should include the if parameter as the first component in the transformation string", "if_w_lt_200,c_fill,h_120,w_80", transformation.toString());

        transformation = new Transformation().crop("fill").height(120).ifCondition("w_lt_200").width(80);
        assertEquals("should include the if parameter as the first component in the transformation string", "if_w_lt_200,c_fill,h_120,w_80", transformation.toString());

        String chained = "[{if: \"w_lt_200\",crop: \"fill\",height: 120, width: 80}, {if: \"w_gt_400\",crop: \"fit\",width: 150,height: 150},{effect: \"sepia\"}]";
        List transformations = ObjectUtils.toList(new JSONArray(chained));

        transformation = new Transformation(transformations);
        assertEquals("should allow multiple conditions when chaining transformations", "if_w_lt_200,c_fill,h_120,w_80/if_w_gt_400,c_fit,h_150,w_150/e_sepia", transformation.toString());
    }

    @Test
    public void literalWithSpaces() throws Exception {
        Map map = ObjectUtils.asMap("if", "width < 200", "crop", "fill", "height", 120, "width", 80);
        List<Map> list = new ArrayList<Map>();
        list.add(map);
        Transformation transformation = new Transformation(list);

        assertEquals("should translate operators", "if_w_lt_200,c_fill,h_120,w_80", transformation.toString());
    }

    @Test
    public void endIf() throws Exception {
        String chained = "[{if: \"w_lt_200\"},\n" +
                "          {crop: \"fill\", height: 120, width: 80,effect: \"sharpen\"},\n" +
                "          {effect: \"brightness:50\"},\n" +
                "          {effect: \"shadow\",color: \"red\"}, {if: \"end\"}]";
        List transformations = ObjectUtils.toList(new JSONArray(chained));

        Transformation transformation = new Transformation(transformations);
        assertEquals("should include the if_end as the last parameter in its component", "if_w_lt_200/c_fill,e_sharpen,h_120,w_80/e_brightness:50/co_red,e_shadow/if_end", transformation.toString());

    }

    @Test
    public void ifElse() throws Exception {
        String chained = "[{if: \"w_lt_200\",crop: \"fill\",height: 120,width: 80},\n" +
                "          {if: \"else\",crop: \"fill\",height: 90, width: 100}]";
        List transformations = ObjectUtils.toList(new JSONArray(chained));

        Transformation transformation = new Transformation(transformations);

        assertEquals("should support if_else with transformation parameters", "if_w_lt_200,c_fill,h_120,w_80/if_else,c_fill,h_90,w_100", transformation.toString());

        chained = "[{if: \"w_lt_200\"},\n" +
                "          {crop: \"fill\",height: 120,width: 80},\n" +
                "          {if: \"else\"},\n" +
                "          {crop: \"fill\",height: 90,width: 100}]";
        transformations = ObjectUtils.toList(new JSONArray(chained));

        transformation = new Transformation(transformations);
        assertEquals("if_else should be without any transformation parameters", "if_w_lt_200/c_fill,h_120,w_80/if_else/c_fill,h_90,w_100", transformation.toString());
    }

    @Test
    public void testDuration() throws Exception {
        Transformation transformation = new Transformation().ifCondition().duration("gt", "30").then().width(100).crop("scale");
        assertEquals("passing an operator and a value adds a condition", "if_du_gt_30,c_scale,w_100", transformation.toString());
        transformation = new Transformation().ifCondition().initialDuration("gt", "30").then().width(100).crop("scale");
        assertEquals("passing an operator and a value adds a condition", "if_idu_gt_30,c_scale,w_100", transformation.toString());
        transformation=new Transformation().ifCondition("initialDuration > 20").crop("scale").width(200);
        assertEquals("if_idu_gt_20,c_scale,w_200", transformation.generate());
    }


    @Test
    public void chainedConditions() throws Exception {
        Transformation transformation = new Transformation().ifCondition().aspectRatio("gt", "3:4").then().width(100).crop("scale");
        assertEquals("passing an operator and a value adds a condition", "if_ar_gt_3:4,c_scale,w_100", transformation.toString());
        transformation = new Transformation().ifCondition().aspectRatio("gt", "3:4").and().width("gt", 100).then().width(50).crop("scale");
        assertEquals("should chaining condition with `and`", "if_ar_gt_3:4_and_w_gt_100,c_scale,w_50", transformation.toString());
        transformation = new Transformation().ifCondition().aspectRatio("gt", "3:4").and().width("gt", 100).or().width("gt", 200).then().width(50).crop("scale");
        assertEquals("should chain conditions with `or`", "if_ar_gt_3:4_and_w_gt_100_or_w_gt_200,c_scale,w_50", transformation.toString());
        transformation = new Transformation().ifCondition().aspectRatio(">", "3:4").and().width("<=", 100).or().width("gt", 200).then().width(50).crop("scale");
        assertEquals("should translate operators", "if_ar_gt_3:4_and_w_lte_100_or_w_gt_200,c_scale,w_50", transformation.toString());
        transformation = new Transformation().ifCondition().aspectRatio(">", "3:4").and().width("<=", 100).or().width(">", 200).then().width(50).crop("scale");
        assertEquals("should translate operators", "if_ar_gt_3:4_and_w_lte_100_or_w_gt_200,c_scale,w_50", transformation.toString());
        transformation = new Transformation().ifCondition().aspectRatio(">=", "3:4").and().pageCount(">=", 100).or().pageCount("!=", 0).then().width(50).crop("scale");
        assertEquals("should translate operators", "if_ar_gte_3:4_and_pc_gte_100_or_pc_ne_0,c_scale,w_50", transformation.toString());

    }

    @Test
    public void shouldSupportAndTranslateOperators() {

        String allOperators =
                        "if_"           +
                        "w_eq_0_and"    +
                        "_h_ne_0_or"    +
                        "_ar_lt_0_and"   +
                        "_pc_gt_0_and"   +
                        "_fc_lte_0_and"  +
                        "_w_gte_0"      +
                        ",e_grayscale";
        assertEquals("should support and translate operators:  '=', '!=', '<', '>', '<=', '>=', '&&', '||'",
                allOperators, new Transformation().ifCondition()
                        .width("=", 0).and()
                        .height("!=", 0).or()
                        .aspectRatio("<", 0).and()
                        .pageCount(">", 0).and()
                        .faceCount("<=", 0).and()
                        .width(">=", 0)
                        .then().effect("grayscale").toString());

        assertEquals(allOperators, new Transformation().ifCondition("w = 0 && height != 0 || aspectRatio < 0 and pageCount > 0 and faceCount <= 0 and width >= 0")
                .effect("grayscale")
                .toString());
    }

    @Test
    public void endIf2() throws Exception {
        Transformation transformation = new Transformation().ifCondition().width("gt", 100).and().width("lt", 200).then().width(50).crop("scale").endIf();
        assertEquals("should serialize to 'if_end'", "if_w_gt_100_and_w_lt_200/c_scale,w_50/if_end", transformation.toString());
        transformation = new Transformation().ifCondition().width("gt", 100).and().width("lt", 200).then().width(50).crop("scale").endIf();
        assertEquals("force the if clause to be chained", "if_w_gt_100_and_w_lt_200/c_scale,w_50/if_end", transformation.toString());
        transformation = new Transformation().ifCondition().width("gt", 100).and().width("lt", 200).then().width(50).crop("scale").ifElse().width(100).crop("crop").endIf();
        assertEquals("force the if_else clause to be chained", "if_w_gt_100_and_w_lt_200/c_scale,w_50/if_else/c_crop,w_100/if_end", transformation.toString());

    }

    @Test
    public void testArrayShouldDefineASetOfVariables() {
        // using methods
        Transformation t = new Transformation();
        t.ifCondition("face_count > 2")
                .variables(variable("$z", 5), variable("$foo", "$z * 2"))
                .crop("scale")
                .width("$foo * 200");
        assertEquals("if_fc_gt_2,$z_5,$foo_$z_mul_2,c_scale,w_$foo_mul_200", t.generate());
    }

    @Test
    public void testShouldSortDefinedVariable(){
        Transformation t = new Transformation().variable("$second", 1).variable("$first", 2);
        assertEquals("$first_2,$second_1", t.generate());
    }

    @Test
    public void testShouldPlaceDefinedVariablesBeforeOrdered(){
        Transformation t = new Transformation()
                .variables(variable("$z", 5), variable("$foo", "$z * 2"))
                .variable("$second", 1)
                .variable("$first", 2);
        assertEquals("$first_2,$second_1,$z_5,$foo_$z_mul_2", t.generate());
    }

    @Test
    public void testVariable(){
        // using strings
        Transformation t = new Transformation();
        t.variable("$foo", 10)
                .chain()
                .ifCondition(faceCount().gt(2))
                .crop("scale")
                .width(new Condition("$foo * 200 / faceCount"))
                .endIf();
        assertEquals("$foo_10/if_fc_gt_2/c_scale,w_$foo_mul_200_div_fc/if_end", t.generate());
    }

    @Test
    public void testShouldSupportTextValues() {
        Transformation t = new Transformation();
        t.effect("$efname", 100)
            .variable("$efname", "!blur!");
        assertEquals("$efname_!blur!,e_$efname:100", t.generate());
    }

    @Test
    public void testSupportStringInterpolation() {
        Transformation t = new Transformation()
                .crop("scale")
                .overlay(new TextLayer().text(
                        "$(start)Hello $(name)$(ext), $(no ) $( no)$(end)"
                ).fontFamily("Arial").fontSize(18));
        assertEquals("c_scale,l_text:Arial_18:$(start)Hello%20$(name)$(ext)%252C%20%24%28no%20%29%20%24%28%20no%29$(end)", t.generate());
    }

    @Test
    public void testShouldSupportPowOperator() {
        Transformation t = new Transformation()
                .variables(variable("$small", 150), variable("$big", "$small ^ 1.5"));

        assertEquals("$small_150,$big_$small_pow_1.5", t.generate());
    }

    @Test
    public void testShouldNotChangeVariableNamesWhenTheyNamedAfterKeyword() {
        Transformation t = new Transformation()
                .variable("$width", 10)
                .chain()
                .width("$width + 10 + width");

        assertEquals("$width_10/w_$width_add_10_add_w", t.generate());
    }

    @Test
    public void testRadiusTwoCornersAsValues() {
        Transformation t = new Transformation()
                .radius(10, 20);

        assertEquals("r_10:20", t.generate());
    }

    @Test
    public void testRadiusTwoCornersAsExpressions() {
        Transformation t = new Transformation()
                .radius("10", "$v");

        assertEquals("r_10:$v", t.generate());
    }

    @Test
    public void testRadiusThreeCorners() {
        Transformation t = new Transformation()
                .radius(10, "$v", "30");

        assertEquals("r_10:$v:30", t.generate());
    }

    @Test
    public void testRadiusFourCorners() {
        Transformation t = new Transformation()
                .radius(10, "$v", "30", 40);

        assertEquals("r_10:$v:30:40", t.generate());
    }

    @Test
    public void testRadiusArray1() {
        Transformation t = new Transformation()
                .radius(new Object[]{10});

        assertEquals("r_10", t.generate());
    }

    @Test
    public void testRadiusArray2() {
        Transformation t = new Transformation()
                .radius(new Object[]{10, "$v"});

        assertEquals("r_10:$v", t.generate());
    }

    @Test
    public void testUserVariableNamesContainingPredefinedNamesAreNotAffected() {
        Transformation t = new Transformation()
                .variable("$mywidth", "100")
                .variable("$aheight", 300)
                .chain()
                .width("3 + $mywidth * 3 + 4 / 2 * initialWidth * $mywidth")
                .height("3 * initialHeight + $aheight");

        assertEquals("$aheight_300,$mywidth_100/h_3_mul_ih_add_$aheight,w_3_add_$mywidth_mul_3_add_4_div_2_mul_iw_mul_$mywidth", t.generate());
    }

    @Test
    public void testContextMetadataToUserVariables() {
        Transformation t = new Transformation()
                .variable("$xpos", "ctx:!x_pos!_to_f")
                .variable("$ypos", "ctx:!y_pos!_to_f")
                .crop("crop")
                .x("$xpos * w")
                .y("$ypos * h");

        assertEquals("$xpos_ctx:!x_pos!_to_f,$ypos_ctx:!y_pos!_to_f,c_crop,x_$xpos_mul_w,y_$ypos_mul_h", t.generate());
    }
}