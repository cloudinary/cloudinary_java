package cloudinary.lib;

import cloudinary.models.PhotoUpload;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class PhotoUploadValidator implements Validator {
    public boolean supports(Class clazz) {
        return PhotoUpload.class.equals(clazz);
    }

    public void validate(Object obj, Errors e) {
        ValidationUtils.rejectIfEmpty(e, "title", "title.empty");
        PhotoUpload pu = (PhotoUpload) obj;
        if (pu.getFile() == null || pu.getFile().isEmpty()) {
            if (!pu.validSignature()) {
                e.rejectValue("signature", "signature.mismatch");
            }
        }
    }

}
