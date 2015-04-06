package cloudinary.controllers;

import cloudinary.lib.PhotoUploadValidator;
import cloudinary.models.Photo;
import cloudinary.models.PhotoUpload;
import cloudinary.repositories.PhotoRepository;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.Singleton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/")
public class PhotoController {
    @Autowired
    private PhotoRepository photoRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String listPhotos(ModelMap model) {
        model.addAttribute("photos", photoRepository.findAll());
        return "photos";
    }

    @SuppressWarnings("rawtypes")
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadPhoto(@ModelAttribute PhotoUpload photoUpload, BindingResult result, ModelMap model) throws IOException {
        PhotoUploadValidator validator = new PhotoUploadValidator();
        validator.validate(photoUpload, result);

        Map uploadResult = null;
        if (photoUpload.getFile() != null && !photoUpload.getFile().isEmpty()) {
            uploadResult = Singleton.getCloudinary().uploader().upload(photoUpload.getFile().getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            photoUpload.setPublicId((String) uploadResult.get("public_id"));
            Object version = uploadResult.get("version");
            if (version instanceof Integer) {
                photoUpload.setVersion(new Long((Integer) version));    
            } else {
                photoUpload.setVersion((Long) version);
            }
            
            photoUpload.setSignature((String) uploadResult.get("signature"));
            photoUpload.setFormat((String) uploadResult.get("format"));
            photoUpload.setResourceType((String) uploadResult.get("resource_type"));
        }

        if (result.hasErrors()){
            model.addAttribute("photoUpload", photoUpload);
            return "upload_form";
        } else {
            Photo photo = new Photo();
            photo.setTitle(photoUpload.getTitle());
            photo.setUpload(photoUpload);
            model.addAttribute("upload", uploadResult);
            photoRepository.save(photo);
            model.addAttribute("photo", photo);
            return "upload";
        }
    }

    @RequestMapping(value = "/upload_form", method = RequestMethod.GET)
    public String uploadPhotoForm(ModelMap model) {
        model.addAttribute("photoUpload", new PhotoUpload());
        return "upload_form";
    }

    @RequestMapping(value = "/direct_upload_form", method = RequestMethod.GET)
    public String directUploadPhotoForm(ModelMap model) {
        model.addAttribute("photoUpload", new PhotoUpload());
        model.addAttribute("unsigned", false);
        return "direct_upload_form";
    }
    
    @SuppressWarnings("unchecked")
	@RequestMapping(value = "/direct_unsigned_upload_form", method = RequestMethod.GET)
    public String directUnsignedUploadPhotoForm(ModelMap model) throws Exception {
        model.addAttribute("photoUpload", new PhotoUpload());
        model.addAttribute("unsigned", true);
        Cloudinary cld = Singleton.getCloudinary();
        String preset = "sample_" + cld.apiSignRequest(ObjectUtils.asMap("api_key", cld.config.apiKey), cld.config.apiSecret).substring(0, 10);
        model.addAttribute("preset", preset);
        try {
        	Singleton.getCloudinary().api().createUploadPreset(ObjectUtils.asMap(
        			"name", preset, 
        			"unsigned", true,
        			"folder", "preset_folder"));
        } catch (Exception e) {
        }
        return "direct_upload_form";
    }
}
