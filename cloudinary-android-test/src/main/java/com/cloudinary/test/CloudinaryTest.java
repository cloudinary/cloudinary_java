package com.cloudinary.test;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.test.AndroidTestCase;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;

@SuppressWarnings({ "unchecked" })
public class CloudinaryTest extends AndroidTestCase {

	private Cloudinary cloudinary;

	public void setUp() {
		this.cloudinary = new Cloudinary("cloudinary://a:b@test123");
	}

	public void testCloudName() {
		// should use cloud_name from config
		String result = cloudinary.url().generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/test", result);
	}

	public void testCloudNameOptions() {
		// should allow overriding cloud_name in options
		String result = cloudinary.url().cloudName("test321").generate("test");
		assertEquals("http://res.cloudinary.com/test321/image/upload/test", result);
	}

	public void testSecureDistribution() {
		// should use default secure distribution if secure=TRUE
		String result = cloudinary.url().secure(true).generate("test");
		assertEquals("https://res.cloudinary.com/test123/image/upload/test", result);
	}

	public void testSecureDistributionOverwrite() {
		// should allow overwriting secure distribution if secure=TRUE
		String result = cloudinary.url().secure(true).secureDistribution("something.else.com").generate("test");
		assertEquals("https://something.else.com/test123/image/upload/test", result);
	}

	public void testSecureDistibution() {
		// should take secure distribution from config if secure=TRUE
		cloudinary.config.secureDistribution = "config.secure.distribution.com";
		String result = cloudinary.url().secure(true).generate("test");
		assertEquals("https://config.secure.distribution.com/test123/image/upload/test", result);
	}

	public void testSecureAkamai() {
		// should default to akamai if secure is given with private_cdn and no
		// secure_distribution
		cloudinary.config.secure = true;
		cloudinary.config.privateCdn = true ; 
		String result = cloudinary.url().generate("test");
		assertEquals("https://test123-res.cloudinary.com/image/upload/test", result);
	}

	public void testSecureNonAkamai() {
		// should not add cloud_name if private_cdn and secure non akamai
		// secure_distribution
		cloudinary.config.secure = true;
		cloudinary.config.privateCdn = true; 
		cloudinary.config.secureDistribution = "something.cloudfront.net";
		String result = cloudinary.url().generate("test");
		assertEquals("https://something.cloudfront.net/image/upload/test", result);
	}

	public void testHttpPrivateCdn() {
		// should not add cloud_name if private_cdn and not secure
		cloudinary.config.privateCdn = true ; 
		String result = cloudinary.url().generate("test");
		assertEquals("http://test123-res.cloudinary.com/image/upload/test", result);
	}

	public void testFormat() {
		// should use format from options
		String result = cloudinary.url().format("jpg").generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/test.jpg", result);
	}

	public void testCrop() {
		Transformation transformation = new Transformation().width(100).height(101);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/h_101,w_100/test", result);
		assertEquals("101", transformation.getHtmlHeight().toString());
		assertEquals("100", transformation.getHtmlWidth().toString());
		transformation = new Transformation().width(100).height(101).crop("crop");
		result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/c_crop,h_101,w_100/test", result);
	}

	public void testVariousOptions() {
		// should use x, y, radius, prefix, gravity and quality from options
		Transformation transformation = new Transformation().x(1).y(2).radius(3).gravity("center").quality(0.4).prefix("a");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/g_center,p_a,q_0.4,r_3,x_1,y_2/test", result);
	}

	public void testTransformationSimple() {
		// should support named transformation
		Transformation transformation = new Transformation().named("blip");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/t_blip/test", result);
	}

	public void testTransformationArray() {
		// should support array of named transformations
		Transformation transformation = new Transformation().named("blip", "blop");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/t_blip.blop/test", result);
	}

	public void testBaseTransformations() {
		// should support base transformation
		Transformation transformation = new Transformation().x(100).y(100).crop("fill").chain().crop("crop").width(100);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("100", transformation.getHtmlWidth().toString());
		assertEquals("http://res.cloudinary.com/test123/image/upload/c_fill,x_100,y_100/c_crop,w_100/test", result);
	}

	public void testBaseTransformationArray() {
		// should support array of base transformations
		Transformation transformation = new Transformation().x(100).y(100).width(200).crop("fill").chain().radius(10).chain().crop("crop")
				.width(100);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("100", transformation.getHtmlWidth().toString());
		assertEquals("http://res.cloudinary.com/test123/image/upload/c_fill,w_200,x_100,y_100/r_10/c_crop,w_100/test", result);
	}

	public void testNoEmptyTransformation() {
		// should not include empty transformations
		Transformation transformation = new Transformation().chain().x(100).y(100).crop("fill").chain();
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/c_fill,x_100,y_100/test", result);
	}

	public void testType() {
		// should use type from options
		String result = cloudinary.url().type("facebook").generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/facebook/test", result);
	}

	public void testResourceType() {
		// should use resource_type from options
		String result = cloudinary.url().resourcType("raw").generate("test");
		assertEquals("http://res.cloudinary.com/test123/raw/upload/test", result);
	}

	public void testIgnoreHttp() {
		// should ignore http links only if type is not given or is asset
		String result = cloudinary.url().generate("http://test");
		assertEquals("http://test", result);
		result = cloudinary.url().type("asset").generate("http://test");
		assertEquals("http://test", result);
		result = cloudinary.url().type("fetch").generate("http://test");
		assertEquals("http://res.cloudinary.com/test123/image/fetch/http://test", result);
	}

	public void testFetch() {
		// should escape fetch urls
		String result = cloudinary.url().type("fetch").generate("http://blah.com/hello?a=b");
		assertEquals("http://res.cloudinary.com/test123/image/fetch/http://blah.com/hello%3Fa%3Db", result);
	}

	public void testCname() {
		// should support extenal cname
		String result = cloudinary.url().cname("hello.com").generate("test");
		assertEquals("http://hello.com/test123/image/upload/test", result);
	}

	public void testCnameSubdomain() {
		// should support extenal cname with cdn_subdomain on
		String result = cloudinary.url().cname("hello.com").cdnSubdomain(true).generate("test");
		assertEquals("http://a2.hello.com/test123/image/upload/test", result);
	}

	public void testHttpEscape() {
		// should escape http urls
		String result = cloudinary.url().type("youtube").generate("http://www.youtube.com/watch?v=d9NF2edxy-M");
		assertEquals("http://res.cloudinary.com/test123/image/youtube/http://www.youtube.com/watch%3Fv%3Dd9NF2edxy-M", result);
	}

	public void testBackground() {
		// should support background
		Transformation transformation = new Transformation().background("red");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/b_red/test", result);
		transformation = new Transformation().background("#112233");
		result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/b_rgb:112233/test", result);
	}

	public void testDefaultImage() {
		// should support default_image
		Transformation transformation = new Transformation().defaultImage("default");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/d_default/test", result);
	}

	public void testAngle() {
		// should support angle
		Transformation transformation = new Transformation().angle(12);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/a_12/test", result);
		transformation = new Transformation().angle("exif", "12");
		result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/a_exif.12/test", result);
	}

	public void testOverlay() {
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

	public void testUnderlay() {
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

	public void testFetchFormat() {
		// should support format for fetch urls
		String result = cloudinary.url().format("jpg").type("fetch").generate("http://cloudinary.com/images/old_logo.png");
		assertEquals("http://res.cloudinary.com/test123/image/fetch/f_jpg/http://cloudinary.com/images/old_logo.png", result);
	}

	public void testEffect() {
		// should support effect
		Transformation transformation = new Transformation().effect("sepia");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/e_sepia/test", result);
	}

	public void testEffectWithParam() {
		// should support effect with param
		Transformation transformation = new Transformation().effect("sepia", 10);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/e_sepia:10/test", result);
	}

	public void testDensity() {
		// should support density
		Transformation transformation = new Transformation().density(150);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/dn_150/test", result);
	}

	public void testPage() {
		// should support page
		Transformation transformation = new Transformation().page(5);
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/pg_5/test", result);
	}

	public void testBorder() {
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

	public void testFlags() {
		// should support flags
		Transformation transformation = new Transformation().flags("abc");
		String result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/fl_abc/test", result);
		transformation = new Transformation().flags("abc", "def");
		result = cloudinary.url().transformation(transformation).generate("test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/fl_abc.def/test", result);
	}

	public void testImageTag() {
		Transformation transformation = new Transformation().width(100).height(101).crop("crop");
		String result = cloudinary.url().transformation(transformation).imageTag("test", ObjectUtils.asMap("alt", "my image"));
		assertEquals(
				"<img src='http://res.cloudinary.com/test123/image/upload/c_crop,h_101,w_100/test' alt='my image' height='101' width='100'/>",
				result);
	}

	public void testFolders() {
		// should add version if public_id contains /
		String result = cloudinary.url().generate("folder/test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/v1/folder/test", result);
		result = cloudinary.url().version(123).generate("folder/test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/v123/folder/test", result);
	}

	public void testFoldersWithVersion() {
		// should not add version if public_id contains version already
		String result = cloudinary.url().generate("v1234/test");
		assertEquals("http://res.cloudinary.com/test123/image/upload/v1234/test", result);
	}

	public void testShorten() {
		// should allow to shorted image/upload urls
		String result = cloudinary.url().shorten(true).generate("test");
		assertEquals("http://res.cloudinary.com/test123/iu/test", result);
	}

	@SuppressWarnings("unchecked")
	public void testEscapePublicId() {
		// should escape public_ids
		Map<String, String> tests = ObjectUtils.asMap("a b", "a%20b", "a+b", "a%2Bb", "a%20b", "a%20b", "a-b", "a-b", "a??b", "a%3F%3Fb");
		for (Map.Entry<String, String> entry : tests.entrySet()) {
			String result = cloudinary.url().generate(entry.getKey());
			assertEquals("http://res.cloudinary.com/test123/image/upload/" + entry.getValue(), result);
		}
	}

	public void testRecommendedIdentifierFormat() {
		String imageIdentifier = "image:upload:dfhjghjkdisudgfds7iyf.jpg";
		String[] components = imageIdentifier.split(":");

		String url = cloudinary.url().resourceType(components[0]).type(components[1]).generate(components[2]);
		assertEquals("http://res.cloudinary.com/test123/image/upload/dfhjghjkdisudgfds7iyf.jpg", url);

		String rawIdentifier = "raw:upload:cguysfdsfuydsfyuds31.doc";
		components = rawIdentifier.split(":");

		url = cloudinary.url().resourceType(components[0]).type(components[1]).generate(components[2]);
		assertEquals("http://res.cloudinary.com/test123/raw/upload/cguysfdsfuydsfyuds31.doc", url);
	}

	public void testSignedUrl() {
		// should correctly sign a url
		String expected = "http://res.cloudinary.com/test123/image/upload/s--Ai4Znfl3--/c_crop,h_20,w_10/v1234/image.jpg";
		String actual = cloudinary.url().version(1234).transformation(new Transformation().crop("crop").width(10).height(20)).signed(true)
				.generate("image.jpg");
		assertEquals(expected, actual);

		expected = "http://res.cloudinary.com/test123/image/upload/s----SjmNDA--/v1234/image.jpg";
		actual = cloudinary.url().version(1234).signed(true).generate("image.jpg");
		assertEquals(expected, actual);

		expected = "http://res.cloudinary.com/test123/image/upload/s--Ai4Znfl3--/c_crop,h_20,w_10/image.jpg";
		actual = cloudinary.url().transformation(new Transformation().crop("crop").width(10).height(20)).signed(true).generate("image.jpg");
		assertEquals(expected, actual);
	}

	public void testDisallowUrlSuffixInSharedDistribution() {
		boolean thrown = false;
		try {
			cloudinary.url().suffix("hello").generate("test");
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), "URL Suffix only supported in private CDN");
			thrown = true;
		}
		assertTrue(thrown);
	}

	public void testDisallowUrlSuffixInNonUploadTypes() {
		boolean thrown = false;
		try {
			cloudinary.url().suffix("hello").privateCdn(true).type("facebook").generate("test");
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), "URL Suffix only supported for image/upload and raw/upload");
		}
	}
	
	public void testDisallowUrlSuffixWithSlash() {
		boolean thrown = false;
		try {
			cloudinary.url().suffix("hello/world").privateCdn(true).generate("test");
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), "url_suffix should not include . or /");
		}
	}

	public void testDisallowUrlSuffixWithDot() {
		boolean thrown = false;
		try {
			cloudinary.url().suffix("hello.world").privateCdn(true).generate("test");
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), "url_suffix should not include . or /");
			thrown = true;
		}
		assertTrue(thrown);
	}

	public void testSupportUrlSuffixForPrivateCdn() {
		String actual = cloudinary.url().suffix("hello").privateCdn(true).generate("test");
		assertEquals("http://test123-res.cloudinary.com/images/test/hello", actual);

		actual = cloudinary.url().suffix("hello").privateCdn(true).transformation(new Transformation().angle(0)).generate("test");
		assertEquals("http://test123-res.cloudinary.com/images/a_0/test/hello", actual);
	}

	public void testPutFormatAfterUrlSuffix() {
		String actual = cloudinary.url().suffix("hello").privateCdn(true).format("jpg").generate("test");
		assertEquals("http://test123-res.cloudinary.com/images/test/hello.jpg", actual);
	}

	public void testNotSignTheUrlSuffix() {

		Pattern pattern = Pattern.compile("s--[0-9A-Za-z_-]{8}--");
		String url = cloudinary.url().format("jpg").signed(true).generate("test");
		Matcher matcher = pattern.matcher(url);
		matcher.find();
		String expectedSignature = url.substring(matcher.start(), matcher.end());

		String actual = cloudinary.url().format("jpg").privateCdn(true).signed(true).suffix("hello").generate("test");
		assertEquals("http://test123-res.cloudinary.com/images/" + expectedSignature + "/test/hello.jpg", actual);

		url = cloudinary.url().format("jpg").signed(true).transformation(new Transformation().angle(0)).generate("test");
		matcher = pattern.matcher(url);
		matcher.find();
		expectedSignature = url.substring(matcher.start(), matcher.end());

		actual = cloudinary.url().format("jpg").privateCdn(true).signed(true).suffix("hello").transformation(new Transformation().angle(0)).generate("test");

		assertEquals("http://test123-res.cloudinary.com/images/" + expectedSignature + "/a_0/test/hello.jpg", actual);
	}

	public void testSupportUrlSuffixForRawUploads() {
		String actual = cloudinary.url().suffix("hello").privateCdn(true).resourceType("raw").generate("test");
		assertEquals("http://test123-res.cloudinary.com/files/test/hello", actual);
	}

	public void testDisllowUseRootPathInSharedDistribution() {
		boolean thrown = false;
		try {
			cloudinary.url().useRootPath(true).generate("test");
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), "Root path only supported in private CDN");
			thrown = true;
		}
		assertTrue(thrown);
	}

	public void testSupportUseRootPathForPrivateCdn() {
		String actual = cloudinary.url().privateCdn(true).useRootPath(true).generate("test");
		assertEquals("http://test123-res.cloudinary.com/test", actual);

		actual = cloudinary.url().privateCdn(true).transformation(new Transformation().angle(0)).useRootPath(true).generate("test");
		assertEquals("http://test123-res.cloudinary.com/a_0/test", actual);
	}

	public void testSupportUseRootPathTogetherWithUrlSuffixForPrivateCdn() {
		String actual = cloudinary.url().privateCdn(true).suffix("hello").useRootPath(true).generate("test");
		assertEquals("http://test123-res.cloudinary.com/test/hello", actual);
	}

	public void testDisllowUseRootPathIfNotImageUploadForFacebook() {
		boolean thrown = false;
		try {
			cloudinary.url().useRootPath(true).privateCdn(true).type("facebook").generate("test");
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), "Root path only supported for image/upload");
			thrown = true;
		}
		assertTrue(thrown);
	}

	public void testDisllowUseRootPathIfNotImageUploadForRaw() {
		boolean thrown = false;
		try {
			cloudinary.url().useRootPath(true).privateCdn(true).resourceType("raw").generate("test");
		} catch (IllegalArgumentException e) {
			assertEquals(e.getMessage(), "Root path only supported for image/upload");
			thrown = true;
		}
		assertTrue(thrown);
	}

}
