package com.cloudinary;

import com.cloudinary.utils.ObjectUtils;
import org.cloudinary.json.JSONObject;

import java.util.Date;

/**
 * A class representing a single access control rule for a resource. Used as a parameter for {@link Api#update} and {@link Uploader#upload}
 */
public class AccessControlRule extends JSONObject {

    /**
     * Construct a new token access rule
     * @return The access rule instance
     */
    public static AccessControlRule token(){
        return new AccessControlRule(AccessType.token, null, null);
    }

    /**
     * Construct a new anonymous access rule
     * @param start The start date for the rule
     * @return The access rule instance
     */
    public static AccessControlRule anonymousFrom(Date start){
        return new AccessControlRule(AccessType.anonymous, start, null);
    }

    /**
     * Construct a new anonymous access rule
     * @param end The end date for the rule
     * @return The access rule instance
     */
    public static AccessControlRule anonymousUntil(Date end){
        return new AccessControlRule(AccessType.anonymous, null, end);
    }

    /**
     * Construct a new anonymous access rule
     * @param start The start date for the rule
     * @param end The end date for the rule
     * @return The access rule instance
     */
    public static AccessControlRule anonymous(Date start, Date end){
        return new AccessControlRule(AccessType.anonymous, start, end);
    }

    private AccessControlRule(AccessType accessType, Date start, Date end) {
        put("access_type", accessType.name());
        if (start != null) {
            put("start", ObjectUtils.toISO8601(start));
        }

        if (end != null) {
            put("end", ObjectUtils.toISO8601(end));
        }
    }

    /**
     * Access type for an access rule
     */
    public enum AccessType {
        anonymous, token
    }
}
