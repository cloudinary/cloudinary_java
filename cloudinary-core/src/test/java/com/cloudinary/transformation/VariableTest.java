package com.cloudinary.transformation;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertEquals;

public class VariableTest {

    private Cloudinary cloudinary;

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary("cloudinary://a:b@test123?load_strategies=false");
    }

    @Test
    public void testSingleCharacterVariable() {
        String url = cloudinary.url().transformation(new Transformation().variable("$a","100").chain().height("$a")).generate("sample.jpg");
        assertEquals("http://res.cloudinary.com/test123/image/upload/$a_100/h_$a/sample.jpg", url);
    }

    @Test
    public void testDollarOnlyAsVariable() {
        String url = cloudinary.url().transformation(new Transformation().variable("$","100").chain().height("$")).generate("sample.jpg");
        assertEquals("http://res.cloudinary.com/test123/image/upload/h_$/sample.jpg", url);
    }

    @Test
    public void testMultipleCharactersVariable() {
        String url = cloudinary.url().transformation(new Transformation().variable("$a45","100").chain().height("$a45")).generate("sample.jpg");
        assertEquals("http://res.cloudinary.com/test123/image/upload/$a45_100/h_$a45/sample.jpg", url);
    }
}
