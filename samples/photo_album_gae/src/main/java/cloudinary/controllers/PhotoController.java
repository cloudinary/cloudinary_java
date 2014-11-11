package cloudinary.controllers;

import cloudinary.lib.PhotoUploadValidator;
import cloudinary.models.PhotoUpload;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.Singleton;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.FetchOptions;


import java.io.IOException;
import java.util.Map;
import java.util.List;

import org.esxx.js.protocol.GAEConnectionManager;

@Controller
@RequestMapping("/")
public class PhotoController {

	private final static GAEConnectionManager connectionManager = new GAEConnectionManager();
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String listPhotos(ModelMap model) {
    	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    	Key photoKey = KeyFactory.createKey("photos", "album");
    	List<Entity> photoEntities = datastore.prepare(new Query("photo", photoKey)).asList(FetchOptions.Builder.withDefaults());
    	List<PhotoUpload> photos = new java.util.ArrayList<PhotoUpload>();
		for(int i = 0, n = photoEntities.size(); i < n; i++) {	
    		photos.add(new PhotoUpload(photoEntities.get(i)));
    	}
        model.addAttribute("photos", photos);
        return "photos";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadPhoto(@ModelAttribute PhotoUpload photoUpload, BindingResult result, ModelMap model) throws IOException {
        PhotoUploadValidator validator = new PhotoUploadValidator();
        validator.validate(photoUpload, result);

        Map uploadResult = null;
        if (photoUpload.getFile() != null && !photoUpload.getFile().isEmpty()) {            
            Singleton.getCloudinary().config.properties.put("connectionManager", connectionManager);
            uploadResult = Singleton.getCloudinary().uploader().upload(photoUpload.getFile().getBytes(),
                    ObjectUtils.asMap("resource_type", "auto"));
            
            photoUpload.setPublicId((String) uploadResult.get("public_id"));
            photoUpload.setVersion((Long) uploadResult.get("version"));
            photoUpload.setSignature((String) uploadResult.get("signature"));
            photoUpload.setFormat((String) uploadResult.get("format"));
            photoUpload.setResourceType((String) uploadResult.get("resource_type"));
        }

        if (result.hasErrors()){
            model.addAttribute("photoUpload", photoUpload);
            return "upload_form";
        } else {
        	Key photoKey = KeyFactory.createKey("photos", "album");
        	Entity photo = new Entity("photo", photoKey);
        	photoUpload.toEntity(photo);
            model.addAttribute("upload", uploadResult);
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            datastore.put(photo);
            model.addAttribute("photo", photoUpload);
            return "upload";
        }
    }

    @RequestMapping(value = "/upload_form", method = RequestMethod.GET)
    public String uploadPhotoForm(ModelMap model) {
        model.addAttribute("photo", new PhotoUpload());
        return "upload_form";
    }

    @RequestMapping(value = "/direct_upload_form", method = RequestMethod.GET)
    public String directUploadPhotoForm(ModelMap model) {
        model.addAttribute("photo", new PhotoUpload());
        return "direct_upload_form";
    }
}