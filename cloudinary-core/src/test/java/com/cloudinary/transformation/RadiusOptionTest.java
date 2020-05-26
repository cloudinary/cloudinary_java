package com.cloudinary.transformation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class RadiusOptionTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void toExpression_immediateNull_returnsAsIs() {
        Object actual = RadiusOption.toExpression(null);
        assertNull(actual);
    }

    @Test
    public void toExpression_singleNullInArray_returnsZeroAsExpression() {
        Object actual = RadiusOption.toExpression(new Object[]{null});
        assertEquals("0", actual);
    }

    @Test
    public void toExpression_singleCornerAsValueInArray_returnsExpression() {
        Object actual = RadiusOption.toExpression(new Object[]{10});
        assertEquals("10", actual);
    }

    @Test
    public void toExpression_singleCornerAsExpressionInArray_returnsExpression() {
        Object actual = RadiusOption.toExpression(new Object[]{"10"});
        assertEquals("10", actual);
    }

    @Test
    public void toExpression_singleCornerAsExpressionInArray2_returnsExpression() {
        Object actual = RadiusOption.toExpression(new Object[]{"$v"});
        assertEquals("$v", actual);
    }

    @Test
    public void toExpression_threeCornerValuesInArray_joinsValuesViaSemicolon() {
        Object actual = RadiusOption.toExpression(new Object[]{10, 20, 30});
        assertEquals("10:20:30", actual);
    }

    @Test
    public void toExpression_threeCornersMixedInArray_joinsMixViaSemicolon() {
        Object actual = RadiusOption.toExpression(new Object[]{10, 20, "$v"});
        assertEquals("10:20:$v", actual);
    }

    @Test
    public void toExpression_fourCornersMixedInArray_joinsMixViaSemicolon() {
        Object actual = RadiusOption.toExpression(new Object[]{10, 20, "$v", 40});
        assertEquals("10:20:$v:40", actual);
    }

    @Test
    public void toExpression_nullCornerValuesMixedInArray_replacesNullsWithZero() {
        Object actual = RadiusOption.toExpression(new Object[]{null, 20, "$v", null});
        assertEquals("0:20:$v:0", actual);
    }

    @Test
    public void toExpression_emptyArray_throwsIllegalArgumentException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Radius array should contain between 1 and 4 values");

        RadiusOption.toExpression(new Object[]{});
    }

    @Test
    public void toExpression_fiveCornerValuesInArray_throwsIllegalArgumentException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Radius array should contain between 1 and 4 values");

        RadiusOption.toExpression(new Object[]{10, 20, 30, 40, 50});
    }

    @Test
    public void toExpression_singleEmptyStringInArray_replacesEmptyStringWithZero() {
        Object actual = RadiusOption.toExpression(new Object[]{""});
        assertEquals("0", actual);
    }

    @Test
    public void toExpression_emptyStringsMixedInArray_replacesEmptyStringsWithZero() {
        Object actual = RadiusOption.toExpression(new Object[]{10, "", "$v", ""});
        assertEquals("10:0:$v:0", actual);
    }

    @Test
    public void toExpression_doesntMutateArrayElements() {
        Object[] radiusOption = {10, "20", "", null};
        RadiusOption.toExpression(radiusOption);

        assertArrayEquals(radiusOption, new Object[]{10, "20", "", null});
    }
}