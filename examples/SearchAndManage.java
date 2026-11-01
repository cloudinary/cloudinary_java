/*
 * Search for assets, then resolve an immutable asset ID back to a public ID.
 *
 * Prerequisites
 *   - CLOUDINARY_URL set in the environment.
 *   - cloudinary-http5 on the classpath.
 *   - At least one image in the product environment. Run UploadImage first if it is empty.
 *
 * Run
 *   javac -cp "<classpath>" -d out examples/SearchAndManage.java
 *   java  -cp "<classpath>:out" SearchAndManage
 *
 * How this differs in a real project
 *   A real application would paginate with next_cursor rather than capping at MAX_RESULTS,
 *   and would cache rather than re-querying, since the Search API is rate-limited more
 *   tightly than other Admin API calls.
 *
 * Docs: docs/search-and-manage-assets.md
 */

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.util.List;
import java.util.Map;

public class SearchAndManage {

    private static final String EXPRESSION = "resource_type:image";
    private static final int MAX_RESULTS = 5;

    public static void main(String[] args) {
        try {
            Cloudinary cloudinary = new Cloudinary();
            if (cloudinary.config.cloudName == null) {
                System.err.println("No Cloudinary configuration found. Set CLOUDINARY_URL.");
                System.exit(1);
            }

            Map page = cloudinary.search()
                    .expression(EXPRESSION)
                    .sortBy("created_at", "desc")
                    .maxResults(MAX_RESULTS)
                    .execute();

            System.out.println("total matching: " + page.get("total_count"));

            List<Map> resources = (List<Map>) page.get("resources");
            if (resources == null || resources.isEmpty()) {
                System.out.println("No assets found. Run UploadImage first.");
                return;
            }

            for (Map asset : resources) {
                System.out.println("  " + asset.get("public_id")
                        + "  asset_id=" + asset.get("asset_id")
                        + "  " + asset.get("format")
                        + "  " + asset.get("bytes") + " bytes");
            }

            // The immutable handle survives renames; the public ID does not. Only a few
            // methods accept an asset ID, so resolving it to a public ID is the usual
            // first step.
            String assetId = (String) resources.get(0).get("asset_id");
            Map resolved = cloudinary.api().resourceByAssetID(assetId, ObjectUtils.emptyMap());
            System.out.println("resolved " + assetId + " -> " + resolved.get("public_id"));

            // An empty expression matches everything. A bare "*" is rejected as invalid.
            Map all = cloudinary.search().expression("").maxResults(1).execute();
            System.out.println("assets in environment: " + all.get("total_count"));

            Map usage = cloudinary.api().usage(ObjectUtils.emptyMap());
            System.out.println("environment media limits: " + usage.get("media_limits"));

        } catch (IllegalArgumentException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            // Admin API and Search methods are declared `throws Exception`, so a broad
            // catch is forced here. ApiException subclasses are what actually arrive.
            System.err.println("API call failed (" + e.getClass().getSimpleName() + "): "
                    + e.getMessage());
            System.exit(1);
        }
    }
}
