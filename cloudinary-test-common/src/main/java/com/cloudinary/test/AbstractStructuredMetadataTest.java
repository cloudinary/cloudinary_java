package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.metadata.*;
import org.junit.*;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.*;

import static com.cloudinary.utils.ObjectUtils.asMap;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

public abstract class AbstractStructuredMetadataTest extends MockableTest {
    private static final String METADATA_UPLOADER_TAG = SDK_TEST_TAG + "_uploader";

    protected Api api;
    public static final List<String> metadataFieldExternalIds = new ArrayList<String>();

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
            System.err.println("Please setup environment for Upload test to run");
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {


        // TODO remove from this line
        Api api = new Cloudinary().api();
        ApiResponse fields = api.listMetadataFields();
        List list = (List) api.listMetadataFields().get("fields");
        for (Object o : list) {
            try {
                api.deleteMetadataField(((Map) o).get("external_id").toString());
            } catch (Exception ignored) {
            }
        }
        // TODO until this one!!

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
        StringMetadataField stringField = createStringField("testCreateMetadata_1");
        ApiResponse result = getFieldResult(stringField);
        assertNotNull(result);
        assertEquals(stringField.getLabel(), result.get("label"));

        SetMetadataField setField = createSetField("testCreateMetadata_2");
        result = cloudinary.api().addMetadataField(setField);
        assertNotNull(result);
        assertEquals(setField.getLabel(), result.get("label"));
    }

    @Test
    public void testListFields() throws Exception {
        StringMetadataField stringField = createStringField("testListFields");
        getFieldResult(stringField);

        ApiResponse result = cloudinary.api().listMetadataFields();
        assertNotNull(result);
        // TODO server returns array in json root, waiting for fix

    }

    @Test
    public void testGetMetadata() throws Exception {
        ApiResponse fieldResult = getFieldResult(createStringField("testGetMetadata"));
        ApiResponse result = api.metadataFieldByFieldId(fieldResult.get("external_id").toString());
        assertNotNull(result);
        assertEquals(fieldResult.get("label"), result.get("label"));
    }

    @Test
    public void testUpdateField() throws Exception {
        ApiResponse fieldResult = getFieldResult(createStringField("testUpdateField"));
        assertNotEquals("new_def", fieldResult.get("default_value"));
        StringMetadataField field = new StringMetadataField();
        field.setDefaultValue("new_def");
        ApiResponse result = api.updateMetadataField(fieldResult.get("external_id").toString(), field);
        assertNotNull(result);
        assertEquals("new_def", result.get("default_value"));
    }

    @Test
    public void testDeleteField() throws Exception {
        ApiResponse fieldResult = getFieldResult(createStringField("testDeleteField"));
        ApiResponse result = api.deleteMetadataField(fieldResult.get("external_id").toString());
        assertNotNull(result);
        assertEquals("ok", result.get("message"));
    }

    @Test
    public void testUpdateDatasource() throws Exception {
        SetMetadataField setField = createSetField("testUpdateDatasource");
        ApiResponse fieldResult = getFieldResult(setField);
        MetadataDataSource.Entry newEntry = new MetadataDataSource.Entry("id1", "new1");
        ApiResponse result = api.updateMetadataFieldDatasource(fieldResult.get("external_id").toString(), Collections.singletonList(newEntry));
        assertNotNull(result);
        assertEquals("new1", ((Map) ((List) result.get("values")).get(0)).get("value"));
    }

    @Test
    public void testDeleteDatasourceEntries() throws Exception {
        // TODO server responds with html page 404
        SetMetadataField setField = createSetField("testDeleteDatasourceEntries");
        ApiResponse fieldResult = getFieldResult(setField);
        MetadataDataSource.Entry newEntry = new MetadataDataSource.Entry("id1", "new1");

        api.updateMetadataFieldDatasource(fieldResult.get("external_id").toString(), Collections.singletonList(newEntry));
        ApiResponse result = api.deleteDatasourceEntries(fieldResult.get("external_id").toString(), Collections.singletonList("id1"));
        assertNotNull(result);
        assertEquals("new1", ((Map) ((List) result.get("values")).get(0)).get("value"));
    }

    @Test
    public void testUploadWithMetadata() throws Exception {
        StringMetadataField field = createStringField("testUploadWithMetadata");
        ApiResponse fieldResult = getFieldResult(field);
        String fieldId = fieldResult.get("external_id").toString();
        Map<String, Object> metadata = Collections.<String, Object>singletonMap(fieldId, "123456");
        Map result = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("metadata", metadata, "tags", Arrays.asList(SDK_TEST_TAG, METADATA_UPLOADER_TAG)));
        assertNotNull(result.get("metadata"));
        assertEquals("123456", ((Map) result.get("metadata")).get(fieldId));
    }

    private ApiResponse getFieldResult(AbstractMetadataField field) throws Exception {
        ApiResponse apiResponse = api.addMetadataField(field);
        metadataFieldExternalIds.add(apiResponse.get("external_id").toString());
        return apiResponse;
    }

    @Test
    public void testExplicitWithMetadata() throws Exception {
        Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", Arrays.asList(SDK_TEST_TAG, METADATA_UPLOADER_TAG)));
        String publicId = uploadResult.get("public_id").toString();
        StringMetadataField field = createStringField("testExplicitWithMetadata");
        ApiResponse fieldResult = getFieldResult(field);
        String fieldId = fieldResult.get("external_id").toString();
        Map<String, Object> metadata = Collections.<String, Object>singletonMap(fieldId, "123456");
        Map result = cloudinary.uploader().explicit(publicId, asMap("type", "upload", "resource_type", "image", "metadata", metadata));
        assertNotNull(result.get("metadata"));
        assertEquals("123456", ((Map) result.get("metadata")).get(fieldId));
    }

    @Test
    public void testUpdateWithMetadata() throws Exception {
        Map uploadResult = cloudinary.uploader().upload(SRC_TEST_IMAGE, asMap("tags", Arrays.asList(SDK_TEST_TAG, METADATA_UPLOADER_TAG)));
        String publicId = uploadResult.get("public_id").toString();
        StringMetadataField field = createStringField("testUpdateWithMetadata");
        ApiResponse fieldResult = getFieldResult(field);
        String fieldId = fieldResult.get("external_id").toString();
        Map<String, Object> metadata = Collections.<String, Object>singletonMap(fieldId, "123456");
        Map result = cloudinary.api().update(publicId, asMap("type", "upload", "resource_type", "image", "metadata", metadata));
        assertNotNull(result.get("metadata"));
        assertEquals("123456", ((Map) result.get("metadata")).get(fieldId));
    }

    @Test
    public void testUploaderUpdateMetadata() throws Exception {
        StringMetadataField field = createStringField("testUploaderUpdateMetadata");
        ApiResponse fieldResult = getFieldResult(field);
        String fieldId = fieldResult.get("external_id").toString();
        Map result = cloudinary.uploader().updateMetadata(Collections.<String, Object>singletonMap(fieldId, "123456"), new String[]{"sample"}, null);
        assertNotNull(result);
        assertEquals("sample", ((List)result.get("public_ids")).get(0).toString());
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

    private StringMetadataField createStringField(String labelPrefix) throws Exception {
        StringMetadataField field = new StringMetadataField();
        String label = labelPrefix + "_" + SUFFIX;
        field.setLabel(label);
        field.setMandatory(true);
        field.setValidation(new MetadataValidation.StringLength(3, 9));
        field.setDefaultValue("val_test");
        return field;
    }
}
