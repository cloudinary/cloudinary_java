package com.cloudinary.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;

public class CloudinaryTest {

	private Cloudinary cloudinary;

	@Before
	public void setUp() {
		this.cloudinary = new Cloudinary("cloudinary://a:b@test123");
	}

	@Test
	public void test_cloud_name() {
		// should use cloud_name from config
		String result = cloudinary.url().generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/test", result);
	}

	@Test
	public void test_cloud_name_options() {
		// should allow overriding cloud_name in options
		String result = cloudinary.url().cloud_name("test321").generate("test");
		assertEquals("http://res.cloudinary.com/test321/image/upload/test", result);
	}

	@Test
	public void test_secure_distribution() {
		// should use default secure distribution if secure=TRUE
		String result = cloudinary.url().secure(true).generate("test");
		assertEquals("https://d3jpl91pxevbkh.cloudfront.net/test123/image/upload/test", result);
	}

	@Test
	public void test_secure_distribution_overwrite() {
		// should allow overwriting secure distribution if secure=TRUE
		String result = cloudinary.url().secure(true).secure_distribution("something.else.com").generate("test");
		assertEquals("https://something.else.com/test123/image/upload/test", result);
	}

	@Test
	public void test_secure_distibution() {
		// should take secure distribution from config if secure=TRUE
		cloudinary.setConfig("secure_distribution", "config.secure.distribution.com");
		String result = cloudinary.url().secure(true).generate("test");
		assertEquals("https://config.secure.distribution.com/test123/image/upload/test", result);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_missing_secure_distribution() {
		// should raise exception if secure is given with private_cdn and no
		// secure_distribution
		cloudinary.setConfig("private_cdn", true);
		cloudinary.url().secure(true).generate("test");
	}

	@Test
	public void test_format() {
		// should use format from options
		String result = cloudinary.url().format("jpg").generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/test.jpg", result);
	}

	@Test
	public void test_crop() {
		Transformation transformation = new Transformation().width(100).height(101);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/h_101,w_100/test", result);
		assertEquals("101", transformation.getHtmlHeight().toString());
		assertEquals("100", transformation.getHtmlWidth().toString());
		transformation = new Transformation().width(100).height(101).crop("crop");
		result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/c_crop,h_101,w_100/test", result);
	}

	@Test
	public void test_various_options() {
		// should use x, y, radius, prefix, gravity and quality from options
		Transformation transformation = new Transformation().x(1).y(2).radius(3).gravity("center").quality(0.4).prefix("a");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/g_center,p_a,q_0.4,r_3,x_1,y_2/test", result);
	}

	@Test
	public void test_transformation_simple() {
		// should support named transformation
		Transformation transformation = new Transformation().named("blip");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/t_blip/test", result);
	}

	@Test
	public void test_transformation_array() {
		// should support array of named transformations
		Transformation transformation = new Transformation().named("blip", "blop");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/t_blip.blop/test", result);
	}

	@Test
	public void test_base_transformations() {
		// should support base transformation
		Transformation transformation = new Transformation().x(100).y(100).crop("fill").chain().crop("crop").width(100);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("100", transformation.getHtmlWidth().toString());
		assertEquals("http://res.cloudinary.com/test123/image/upload/c_fill,x_100,y_100/c_crop,w_100/test", result);
	}

	@Test
	public void test_base_transformation_array() {
		// should support array of base transformations
		Transformation transformation = new Transformation().x(100).y(100).width(200).crop("fill").chain().radius(10).chain().crop("crop")
				.width(100);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("100", transformation.getHtmlWidth().toString());
		assertEquals("http://res.cloudinary.com/test123/image/upload/c_fill,w_200,x_100,y_100/r_10/c_crop,w_100/test", result);
	}

	@Test
	public void test_no_empty_transformation() {
		// should not include empty transformations
		Transformation transformation = new Transformation().chain().x(100).y(100).crop("fill").chain();
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/c_fill,x_100,y_100/test", result);
	}

	@Test
	public void test_type() {
		// should use type from options
		String result = cloudinary.url().type("facebook").generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/facebook/test", result);
	}

	@Test
	public void test_resource_type() {
		// should use resource_type from options
		String result = cloudinary.url().resource_type("raw").generate("test");
		assertEquals("http://res.cloudinary.com/test123/raw/upload/test", result);
	}

	@Test
	public void test_ignore_http() {
		// should ignore http links only if type is not given or is asset
		String result = cloudinary.url().generate("http://test");
		assertEquals("http://test", result);
		result = cloudinary.url().type("asset").generate("http://test");
		assertEquals("http://test", result);
		result = cloudinary.url().type("fetch").generate("http://test");
		assertEquals("http://res.cloudinary.com/test123/image/fetch/http://test", result);
	}

	@Test
	public void test_fetch() {
		// should escape fetch urls
		String result = cloudinary.url().type("fetch").generate("http://blah.com/hello?a=b");
		assertEquals("http://res.cloudinary.com/test123/image/fetch/http://blah.com/hello%3Fa%3Db", result);
	}

	@Test
	public void test_cname() {
		// should support extenal cname
		String result = cloudinary.url().cname("hello.com").generate("test");
		assertEquals("http://hello.com/test123/image/upload/test", result);
	}

	@Test
	public void test_cname_subdomain() {
		// should support extenal cname with cdn_subdomain on
		String result = cloudinary.url().cname("hello.com").cdn_subdomain(true).generate("test");
		assertEquals("http://a2.hello.com/test123/image/upload/test", result);
	}

	@Test
	public void test_http_escape() {
		// should escape http urls
		String result = cloudinary.url().type("youtube").generate("http://www.youtube.com/watch?v=d9NF2edxy-M");
		assertEquals("http://res.cloudinary.com/test123/image/youtube/http://www.youtube.com/watch%3Fv%3Dd9NF2edxy-M", result);
	}

	@Test
	public void test_background() {
		// should support background
		Transformation transformation = new Transformation().background("red");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/b_red/test", result);
		transformation = new Transformation().background("#112233");
		result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/b_rgb:112233/test", result);
	}

	@Test
	public void test_default_image() {
		// should support default_image
		Transformation transformation = new Transformation().default_image("default");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/d_default/test", result);
	}

	@Test
	public void test_angle() {
		// should support angle
		Transformation transformation = new Transformation().angle(12);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/a_12/test", result);
		transformation = new Transformation().angle("exif", "12");
		result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/a_exif.12/test", result);
	}

	@Test
	public void test_overlay() {
		// should support overlay
		Transformation transformation = new Transformation().overlay("text:hello");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/l_text:hello/test", result);
		// should not pass width/height to html if overlay
		transformation = new Transformation().overlay("text:hello").width(100).height(100);
		result = cloudinary.url().transformation(transformation).generate("test");
		assertNull(transformation.getHtmlHeight());
		assertNull(transformation.getHtmlWidth());
		assertEquals("http://res.cloudinary.com/test123/image/upload/h_100,l_text:hello,w_100/test", result);
	}

	@Test
	public void test_underlay() {
		Transformation transformation = new Transformation().underlay("text:hello");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/u_text:hello/test", result);
		// should not pass width/height to html if underlay
		transformation = new Transformation().underlay("text:hello").width(100).height(100);
		result = cloudinary.url().transformation(transformation).generate("test");
		assertNull(transformation.getHtmlHeight());
		assertNull(transformation.getHtmlWidth());
		assertEquals("http://res.cloudinary.com/test123/image/upload/h_100,u_text:hello,w_100/test", result);
	}

	@Test
	public void test_fetch_format() {
		// should support format for fetch urls
		String result = cloudinary.url().format("jpg").type("fetch").generate("http://cloudinary.com/images/logo.png");
		assertEquals("http://res.cloudinary.com/test123/image/fetch/f_jpg/http://cloudinary.com/images/logo.png", result);
	}

	@Test
	public void test_effect() {
		// should support effect
		Transformation transformation = new Transformation().effect("sepia");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/e_sepia/test", result);
	}

	@Test
	public void test_effect_with_param() {
		// should support effect with param
		Transformation transformation = new Transformation().effect("sepia", 10);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/e_sepia:10/test", result);
	}

	@Test
	public void test_density() {
		// should support density
		Transformation transformation = new Transformation().density(150);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/dn_150/test", result);
	}

	@Test
	public void test_page() {
		// should support page
		Transformation transformation = new Transformation().page(5);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/pg_5/test", result);
	}

	@Test
	public void test_border() {
		// should support border
		Transformation transformation = new Transformation().border(5, "black");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/bo_5px_solid_black/test", result);
		transformation = new Transformation().border(5, "#ffaabbdd");
		result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/bo_5px_solid_rgb:ffaabbdd/test", result);
		transformation = new Transformation().border("1px_solid_blue");
		result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/bo_1px_solid_blue/test", result);
	}

}
