package cloudinary.lib;

import cloudinary.models.PhotoUpload;
import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PhotoUploadValidator implements Validator {
    public boolean supports(Class clazz) {
        return PhotoUpload.class.equals(clazz);
    }

    public void validate(Object obj, Errors e) {
        ValidationUtils.rejectIfEmpty(e, "title", "title.empty");
        PhotoUpload pu = (PhotoUpload) obj;
        if (pu.getFile() == null || pu.getFile().isEmpty()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("version", pu.getVersion().toString());
            params.put("public_id", pu.getPublicIdForSigning());
            Singleton.getCloudinary().signRequest(params, new HashMap<String, Object>());
            if (!params.get("signature").toString().equals(pu.getSignature())) {
                e.rejectValue("signature", "signature.mismatch");
            }
        }
    }

}
