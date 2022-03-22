package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.BadRequest;
import com.cloudinary.metadata.*;

import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.*;

import static com.cloudinary.utils.ObjectUtils.asMap;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

public abstract class AbstractStructuredMetadataTest extends MockableTest {
    private static final String METADATA_UPLOADER_TAG = SDK_TEST_TAG + "_uploader";
    private static final String PUBLIC_ID = "before_class_public_id" + SUFFIX;
    private static final String PRIVATE_PUBLIC_ID = "before_class_private_public_id" + SUFFIX;
    protected Api api;
    public static final List<String> metadataFieldExternalIds = new ArrayList<String>();

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
            System.err.println("Please setup environment for Upload test to run");
        }

        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("public_id", PUBLIC_ID));
        cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("public_id", PRIVATE_PUBLIC_ID, "type", "private"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        Api api = new Cloudinary().api();

        for (String externalId : metadataFieldExternalIds) {
            try {
                api.deleteMetadataField(externalId);
            } catch (Exception ignored) {
            }
        }
    }

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary();
        assumeNotNull(cloudinary.config.apiSecret);
        this.api = cloudinary.api();
    }

    @Test
    public void testCreateMetadata() throws Exception {
        StringMetadataField stringField = newFieldInstance("testCreateMetadata_1");
        ApiResponse result = addFieldToAccount(stringField);
        assertNotNull(result);
        assertEquals(stringField.getLabel(), result.get("label"));

        SetMetadataField setField = createSetField("testCreateMetadata_2");
        result = cloudinary.api().addMetadataField(setField);
        assertNotNull(result);
        assertEquals(setField.getLabel(), result.get("label"));
    }

    @Test
    public void testDateFieldDefaultValueValidation() throws Exception {
        // now minus 3 days hours.
        Date max = new Date();
        Date min = new Date(max.getTime() - 72 * 60 * 60 * 1000);

        Date legalValue = new Date(min.getTime() + 36 * 60 * 60 * 1000);
        Date illegalValue = new Date(max.getTime() + 36 * 60 * 60 * 1000);

        DateMetadataField dateMetadataField = new DateMetadataField();
        dateMetadataField.setLabel("Start date" + new Date().getTime());

        List<MetadataValidation> rules = new ArrayList<MetadataValidation>();
        rules.add(new MetadataValidation.DateGreaterThan(min));
        rules.add(new MetadataValidation.DateLessThan(max));
        dateMetadataField.setValidation(new MetadataValidation.AndValidator(rules));

        String message = null;
        ApiResponse res = null;
        try {
            // should fail
            dateMetadataField.setDefaultValue(illegalValue);
            res = api.addMetadataField(dateMetadataField);
            // this line should not be reached if all is working well, but when it's not we still want to clean it up:
            metadataFieldExternalIds.add(res.get("external_id").toString());
        } catch (BadRequest e) {
            message = e.getMessage();
        }

        assertEquals(message, "default_value is invalid");

        // should work:
        dateMetadataField.setDefaultValue(legalValue);
        res = api.addMetadataField(dateMetadataField);
        metadataFieldExternalIds.add(res.get("external_id").toString());
    }

    @Test
    public void testListFields() throws Exception {
        StringMetadataField stringField = newFieldInstance("testListFields");
        addFieldToAccount(stringField);

        ApiResponse result = cloudinary.api().listMetadataFields();
        assertNotNull(result);
        assertNotNull(result.get("metadata_fields"));
        assertTrue(((List)result.get("metadata_fields")).size() > 0);
    }

    @Test
    public void testGetMetadata() throws Exception {
        ApiResponse fieldResult = addFieldToAccount(newFieldInstance("testGetMetadata"));
        ApiResponse result = api.metadataFieldByFieldId(fieldResult.get("external_id").toString());
        assertNotNull(result);
        assertEquals(fieldResult.get("label"), result.get("label"));
    }

    @Test
    public void testUpdateField() throws Exception {
        ApiResponse fieldResult = addFieldToAccount(newFieldInstance("testUpdateField"));
        assertNotEquals("new_def", fieldResult.get("default_value"));
        StringMetadataField field = new StringMetadataField();
        field.setDefaultValue("new_def");
        ApiResponse result = api.updateMetadataField(fieldResult.get("external_id").toString(), field);
        assertNotNull(result);
        assertEquals("new_def", result.get("default_value"));
    }

    @Test
    public void testDeleteField() throws Exception {
        ApiResponse fieldResult = addFieldToAccount(newFieldInstance("testDeleteField"));
        ApiResponse result = api.deleteMetadataField(fieldResult.get("external_id").toString());
        assertNotNull(result);
        assertEquals("ok", result.get("message"));
    }

    @Test
    public void testUpdateDatasource() throws Exception {
        SetMetadataField setField = createSetField("testUpdateDatasource");
        ApiResponse fieldResult = addFieldToAccount(setField);
        MetadataDataSource.Entry newEntry = new MetadataDataSource.Entry("id1", "new1");
        ApiResponse result = api.updateMetadataFieldDatasource(fieldResult.get("external_id").toString(), Collections.singletonList(newEntry));
        assertNotNull(result);
        assertEquals("new1", ((Map) ((List) result.get("values")).get(0)).get("value"));
    }

    @Test
    public void testDeleteDatasourceEntries() throws Exception {
        SetMetadataField setField = createSetField("testDeleteDatasourceEntries");
        ApiResponse fieldResult = addFieldToAccount(setField);
        ApiResponse result = api.deleteDatasourceEntries(fieldResult.get("external_id").toString(), Collections.singletonList("id1"));
        assertNotNull(result);
    }

    @Test
    public void testRestoreDatasourceEntries() throws Exception {
        SetMetadataField setField = createSetField("testRestoreDatasourceEntries");
        ApiResponse fieldResult = addFieldToAccount(setField);
        String fieldExternalId = fieldResult.get("external_id").toString();
        api.deleteDatasourceEntries(fieldExternalId, Collections.singletonList("id1"));
        ApiResponse result = api.restoreDatasourceEntries(fieldExternalId, Collections.singletonList("id1"));
        assertNotNull(result);
    }

    @Test
    public void testReorderMetadataFieldsByLabel() throws Exception {
        AddStringField("some_value");
        AddStringField("aaa");
        AddStringField("zzz");
        
        ApiResponse result = api.reorderMetadataFields("label", null, Collections.EMPTY_MAP);
        assertThat(getField(result, 0), Matchers.containsString("aaa"));

        result = api.reorderMetadataFields("label", "desc", Collections.EMPTY_MAP);
        assertThat(getField(result, 0), Matchers.containsString("zzz"));

        result = api.reorderMetadataFields("label", "asc", Collections.EMPTY_MAP);
        assertThat(getField(result, 0), Matchers.containsString("aaa"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReorderMetadataFieldsOrderByIsRequired() throws Exception {
        api.reorderMetadataFields(null, null, Collections.EMPTY_MAP);
    }

    private String getField(ApiResponse result, int index) {
        String actual = ((Map)((ArrayList)result.get("metadata_fields")).get(index)).get("label").toString();
        return actual;
    }

    private void AddStringField(String labelPrefix) throws Exception {
        StringMetadataField field = newFieldInstance(labelPrefix);
        ApiResponse fieldResult = addFieldToAccount(field);
        String fieldId = fieldResult.get("external_id").toString();
    }

    @Test
    public void testUploadWithMetadata() throws Exception {
        StringMetadataField field = newFieldInstance("testUploadWithMetadata");
        ApiResponse fieldResult = addFieldToAccount(field);
        String fieldId = fieldResult.get("external_id").toString();
        Map<String, Object> metadata = Collections.<String, Object>singletonMap(fieldId, "123456");
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("metadata", metadata, "tags", Arrays.asList(SDK_TEST_TAG, METADATA_UPLOADER_TAG)));
        assertNotNull(result.get("metadata"));
        assertEquals("123456", ((Map) result.get("metadata")).get(fieldId));
    }

    @Test
    public void testExplicitWithMetadata() throws Exception {
        Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", Arrays.asList(SDK_TEST_TAG, METADATA_UPLOADER_TAG)));
        String publicId = uploadResult.get("public_id").toString();
        StringMetadataField field = newFieldInstance("testExplicitWithMetadata");
        ApiResponse fieldResult = addFieldToAccount(field);
        String fieldId = fieldResult.get("external_id").toString();
        Map<String, Object> metadata = Collections.<String, Object>singletonMap(fieldId, "123456");
        Map result = cloudinary.uploader().explicit(publicId, asMap("type", "upload", "resource_type", "image", "metadata", metadata));
        assertNotNull(result.get("metadata"));
        assertEquals("123456", ((Map) result.get("metadata")).get(fieldId));

        // explicit with invalid data, should fail:
        metadata = Collections.<String, Object>singletonMap(fieldId, "12");
        String message = "";
        try {
            result = cloudinary.uploader().explicit(publicId, asMap("type", "upload", "resource_type", "image", "metadata", metadata));
        } catch (Exception e){
            message = e.getMessage();
        }

        assertTrue(message.contains("is not valid for field") );
    }

    @Test
    public void testUpdateWithMetadata() throws Exception {
        Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", Arrays.asList(SDK_TEST_TAG, METADATA_UPLOADER_TAG)));
        String publicId = uploadResult.get("public_id").toString();
        StringMetadataField field = newFieldInstance("testUpdateWithMetadata");
        ApiResponse fieldResult = addFieldToAccount(field);
        String fieldId = fieldResult.get("external_id").toString();
        Map<String, Object> metadata = Collections.<String, Object>singletonMap(fieldId, "123456");
        Map result = cloudinary.api().update(publicId, asMap("type", "upload", "resource_type", "image", "metadata", metadata));
        assertNotNull(result.get("metadata"));
        assertEquals("123456", ((Map) result.get("metadata")).get(fieldId));
    }

    @Test
    public void testUploaderUpdateMetadata() throws Exception {
        StringMetadataField field = newFieldInstance("testUploaderUpdateMetadata");
        ApiResponse fieldResult = addFieldToAccount(field);
        String fieldId = fieldResult.get("external_id").toString();
        Map result = cloudinary.uploader().updateMetadata(Collections.<String, Object>singletonMap(fieldId, "123456"), new String[]{PUBLIC_ID}, null);
        assertNotNull(result);
        assertEquals(PUBLIC_ID, ((List) result.get("public_ids")).get(0).toString());
        //test updateMetadata for private asset
        Map result2 = cloudinary.uploader().updateMetadata(Collections.<String, Object>singletonMap(fieldId, "123456"), new String[]{PRIVATE_PUBLIC_ID}, asMap("type","private"));
        assertNotNull(result);
        assertEquals(PRIVATE_PUBLIC_ID, ((List) result2.get("public_ids")).get(0).toString());
    }

    @Test
    public void testSetField() throws Exception {
        SetMetadataField field = createSetField("test123");
        ApiResponse fieldResult = addFieldToAccount(field);
        String fieldId = fieldResult.get("external_id").toString();
        Map result = cloudinary.uploader().updateMetadata(asMap(fieldId, new String[]{"id2", "id3"}), new String[]{PUBLIC_ID}, null);
        assertNotNull(result);
        assertEquals(PUBLIC_ID, ((List) result.get("public_ids")).get(0).toString());
        List<String> list = new ArrayList<String>(2);
        list.add("id1");
        list.add("id2");
        result = cloudinary.uploader().updateMetadata(asMap(fieldId, list), new String[]{PUBLIC_ID}, null);
        assertNotNull(result);
        assertEquals(PUBLIC_ID, ((List) result.get("public_ids")).get(0).toString());
    }
    // Metadata test helpers
    private SetMetadataField createSetField(String labelPrefix) {
        SetMetadataField setField = new SetMetadataField();
        String label = labelPrefix + "_" + SUFFIX;
        setField.setLabel(label);
        setField.setMandatory(false);
        setField.setValidation(new MetadataValidation.StringLength(3, 99));
        setField.setDefaultValue(Arrays.asList("id2", "id3"));
        setField.setValidation(null);
        List<MetadataDataSource.Entry> entries = new ArrayList<MetadataDataSource.Entry>();
        entries.add(new MetadataDataSource.Entry("id1", "first_value"));
        entries.add(new MetadataDataSource.Entry("id2", "second_value"));
        entries.add(new MetadataDataSource.Entry("id3", "third_value"));
        MetadataDataSource dataSource = new MetadataDataSource(entries);
        setField.setDataSource(dataSource);
        return setField;
    }

    private StringMetadataField newFieldInstance(String labelPrefix) throws Exception {
        String label = labelPrefix + "_" + SUFFIX;
        return MetadataTestHelper.newFieldInstance(label);
    }

    private ApiResponse addFieldToAccount(MetadataField field) throws Exception {
        ApiResponse apiResponse = MetadataTestHelper.addFieldToAccount(api, field);
        metadataFieldExternalIds.add(apiResponse.get("external_id").toString());
        return apiResponse;
    }
}
