package com.cloudinary.transformation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExpressionTest {

    @Test
    public void normalize_null_null() {
        String result = Expression.normalize(null);
        assertNull(result);
    }

    @Test
    public void normalize_number_number() {
        String result = Expression.normalize(10);
        assertEquals("10", result);
    }

    @Test
    public void normalize_emptyString_emptyString() {
        String result = Expression.normalize("");
        assertEquals("", result);
    }

    @Test
    public void normalize_singleSpace_underscore() {
        String result = Expression.normalize(" ");
        assertEquals("_", result);
    }

    @Test
    public void normalize_blankString_underscore() {
        String result = Expression.normalize("   ");
        assertEquals("_", result);
    }

    @Test
    public void normalize_underscore_underscore() {
        String result = Expression.normalize("_");
        assertEquals("_", result);
    }

    @Test
    public void normalize_underscores_underscore() {
        String result = Expression.normalize("___");
        assertEquals("_", result);
    }

    @Test
    public void normalize_underscoresAndSpaces_underscore() {
        String result = Expression.normalize(" _ __  _");
        assertEquals("_", result);
    }

    @Test
    public void normalize_arbitraryText_isNotAffected() {
        String result = Expression.normalize("foobar");
        assertEquals("foobar", result);
    }

    @Test
    public void normalize_doubleAmpersand_replacedWithAndOperator() {
        String result = Expression.normalize("foo && bar");
        assertEquals("foo_and_bar", result);
    }

    @Test
    public void normalize_doubleAmpersandWithNoSpaceAtEnd_isNotAffected() {
        String result = Expression.normalize("foo&&bar");
        assertEquals("foo&&bar", result);
    }

    @Test
    public void normalize_width_recognizedAsVariableAndReplacedWithW() {
        String result = Expression.normalize("width");
        assertEquals("w", result);
    }

    @Test
    public void normalize_initialAspectRatio_recognizedAsVariableAndReplacedWithIar() {
        String result = Expression.normalize("initial_aspect_ratio");
        assertEquals("iar", result);
    }

    @Test
    public void normalize_dollarWidth_recognizedAsUserVariableAndNotAffected() {
        String result = Expression.normalize("$width");
        assertEquals("$width", result);
    }

    @Test
    public void normalize_dollarInitialAspectRatio_recognizedAsUserVariableAndAsVariableReplacedWithAr() {
        String result = Expression.normalize("$initial_aspect_ratio");
        assertEquals("$initial_ar", result);
    }

    @Test
    public void normalize_dollarMyWidth_recognizedAsUserVariableAndNotAffected() {
        String result = Expression.normalize("$mywidth");
        assertEquals("$mywidth", result);
    }

    @Test
    public void normalize_dollarWidthWidth_recognizedAsUserVariableAndNotAffected() {
        String result = Expression.normalize("$widthwidth");
        assertEquals("$widthwidth", result);
    }

    @Test
    public void normalize_dollarUnderscoreWidth_recognizedAsUserVariableAndNotAffected() {
        String result = Expression.normalize("$_width");
        assertEquals("$_width", result);
    }

    @Test
    public void normalize_dollarUnderscoreX2Width_recognizedAsUserVariableAndNotAffected() {
        String result = Expression.normalize("$__width");
        assertEquals("$_width", result);
    }

    @Test
    public void normalize_dollarX2Width_recognizedAsUserVariableAndNotAffected() {
        String result = Expression.normalize("$$width");
        assertEquals("$$width", result);
    }

    @Test
    public void normalize_doesntReplaceVariable_1() {
        String actual = Expression.normalize("$height_100");
        assertEquals("$height_100", actual);
    }

    @Test
    public void normalize_doesntReplaceVariable_2() {
        String actual = Expression.normalize("$heightt_100");
        assertEquals("$heightt_100", actual);
    }

    @Test
    public void normalize_doesntReplaceVariable_3() {
        String actual = Expression.normalize("$$height_100");
        assertEquals("$$height_100", actual);
    }

    @Test
    public void normalize_doesntReplaceVariable_4() {
        String actual = Expression.normalize("$heightmy_100");
        assertEquals("$heightmy_100", actual);
    }

    @Test
    public void normalize_doesntReplaceVariable_5() {
        String actual = Expression.normalize("$myheight_100");
        assertEquals("$myheight_100", actual);
    }

    @Test
    public void normalize_doesntReplaceVariable_6() {
        String actual = Expression.normalize("$heightheight_100");
        assertEquals("$heightheight_100", actual);
    }

    @Test
    public void normalize_doesntReplaceVariable_7() {
        String actual = Expression.normalize("$theheight_100");
        assertEquals("$theheight_100", actual);
    }

    @Test
    public void normalize_doesntReplaceVariable_8() {
        String actual = Expression.normalize("$__height_100");
        assertEquals("$_height_100", actual);
    }

    @Test
    public void normalize_duration() {
        String actual = Expression.normalize("duration");
        assertEquals("du", actual);
    }

    @Test
    public void normalize_previewDuration() {
        String actual = Expression.normalize("preview:duration_2");
        assertEquals("preview:duration_2", actual);
    }
}
