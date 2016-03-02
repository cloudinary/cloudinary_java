package com.cloudinary;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;

/**
 * Configuration object for a {@link Cloudinary} instance
 */
public class Configuration {
    public final static String CF_SHARED_CDN = "d3jpl91pxevbkh.cloudfront.net";
    public final static String OLD_AKAMAI_SHARED_CDN = "cloudinary-a.akamaihd.net";
    public final static String AKAMAI_SHARED_CDN = "res.cloudinary.com";
    public final static String SHARED_CDN = AKAMAI_SHARED_CDN;
    public final static String VERSION = "1.0.2";
    public final static String USER_AGENT = "cld-android-" + VERSION;

    public String cloudName;
    public String apiKey;
    public String apiSecret;
    public String secureDistribution;
    public String cname;
    public String uploadPrefix;
    public boolean secure;
    public boolean privateCdn;
    public boolean cdnSubdomain;
    public boolean shorten;
    public String callback;
    public String proxyHost;
    public int proxyPort;
    public Map<String, Object> properties = new HashMap<String, Object>();
    public Boolean secureCdnSubdomain;
    public boolean useRootPath;
    public int timeout;
    public boolean loadStrategies = true;

    public Configuration() {
    }

    private Configuration(String cloudName, String apiKey, String apiSecret, String secureDistribution, String cname, String uploadPrefix, boolean secure, boolean privateCdn, boolean cdnSubdomain, boolean shorten, String callback, String proxyHost, int proxyPort, Boolean secureCdnSubdomain, boolean useRootPath) {
        this(cloudName, apiKey, apiSecret, secureDistribution, cname, uploadPrefix, secure, privateCdn, cdnSubdomain, shorten, callback, proxyHost, proxyPort, secureCdnSubdomain, useRootPath, 0, true);
    }

    private Configuration(String cloudName, String apiKey, String apiSecret, String secureDistribution, String cname, String uploadPrefix, boolean secure, boolean privateCdn, boolean cdnSubdomain, boolean shorten, String callback, String proxyHost, int proxyPort, Boolean secureCdnSubdomain, boolean useRootPath, int timeout, boolean loadStrategies) {
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.secureDistribution = secureDistribution;
        this.cname = cname;
        this.uploadPrefix = uploadPrefix;
        this.secure = secure;
        this.privateCdn = privateCdn;
        this.cdnSubdomain = cdnSubdomain;
        this.shorten = shorten;
        this.callback = callback;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.secureCdnSubdomain = secureCdnSubdomain;
        this.useRootPath = useRootPath;
        this.timeout = 0;
        this.loadStrategies = loadStrategies;
    }

    @SuppressWarnings("rawtypes")
    public Configuration(Map config) {
        update(config);
    }

    @SuppressWarnings("rawtypes")
    public void update(Map config) {
        this.cloudName = (String) config.get("cloud_name");
        this.apiKey = (String) config.get("api_key");
        this.apiSecret = (String) config.get("api_secret");
        this.secureDistribution = (String) config.get("secure_distribution");
        this.cname = (String) config.get("cname");
        this.secure = ObjectUtils.asBoolean(config.get("secure"), false);
        this.privateCdn = ObjectUtils.asBoolean(config.get("private_cdn"), false);
        this.cdnSubdomain = ObjectUtils.asBoolean(config.get("cdn_subdomain"), false);
        this.shorten = ObjectUtils.asBoolean(config.get("shorten"), false);
        this.uploadPrefix = (String) config.get("upload_prefix");
        this.callback = (String) config.get("callback");
        this.proxyHost = (String) config.get("proxy_host");
        this.proxyPort = ObjectUtils.asInteger(config.get("proxy_port"), 0);
        this.secureCdnSubdomain = ObjectUtils.asBoolean(config.get("secure_cdn_subdomain"), null);
        this.useRootPath = ObjectUtils.asBoolean(config.get("use_root_path"), false);
        this.loadStrategies = ObjectUtils.asBoolean(config.get("load_strategies"), true);
        this.timeout = ObjectUtils.asInteger(config.get("timeout"), 0);
    }

    public Configuration(Configuration other) {
        this.cloudName = other.cloudName;
        this.apiKey = other.apiKey;
        this.apiSecret = other.apiSecret;
        this.secureDistribution = other.secureDistribution;
        this.cname = other.cname;
        this.uploadPrefix = other.uploadPrefix;
        this.secure = other.secure;
        this.privateCdn = other.privateCdn;
        this.cdnSubdomain = other.cdnSubdomain;
        this.shorten = other.shorten;
        this.callback = other.callback;
        this.proxyHost = other.proxyHost;
        this.proxyPort = other.proxyPort;
        this.secureCdnSubdomain = other.secureCdnSubdomain;
        this.useRootPath = other.useRootPath;
        this.timeout = other.timeout;
    }

    /**
     * Create a new Configuration from an existing one
     *
     * @param other
     * @return a new configuration with the arguments supplied by another configuration object
     */
    public static Configuration from(Configuration other) {
        return new Builder().from(other).build();
    }

    /**
     * Create a Configuration from a cloudinary url
     * <p>
     * Example url: cloudinary://123456789012345:abcdeghijklmnopqrstuvwxyz12@n07t21i7
     *
     * @param cloudinaryUrl configuration url
     * @return a new configuration with the arguments supplied by the url
     */
    public static Configuration from(String cloudinaryUrl) {
        return from(parseConfigUrl(cloudinaryUrl));
    }

    private static Configuration parseConfigUrl(String cloudinaryUrl) {
        Builder builder = new Builder();

        URI cloudinaryUri = URI.create(cloudinaryUrl);
        builder.setCloudName(cloudinaryUri.getHost());
        if (cloudinaryUri.getUserInfo() != null) {
            String[] creds = cloudinaryUri.getUserInfo().split(":");
            builder.setApiKey(creds[0]);
            builder.setApiSecret(creds[1]);
        }
        builder.setPrivateCdn(!StringUtils.isEmpty(cloudinaryUri.getPath()));
        builder.setSecureDistribution(cloudinaryUri.getPath());
        if (cloudinaryUri.getQuery() != null) {
            for (String param : cloudinaryUri.getQuery().split("&")) {
                String[] keyValue = param.split("=");
                String val = null;
                try {
//                    params.put(keyValue[0], URLDecoder.decode(keyValue[1], "ASCII"));
                    val = URLDecoder.decode(keyValue[1], "ASCII");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException("Error decoding cloudinaryUrl", e);
                }

                String key = keyValue[0];
                if (key.equals("cname")) {
                    builder.setCname(val);
                } else if (key.equals("upload_prefix")) {
                    builder.setUploadPrefix(val);
                } else if (key.equals("secure")) {
                    builder.setSecure(ObjectUtils.asBoolean(val, false));
                } else if (key.equals("cdn_subdomain")) {
                    builder.setCdnSubdomain(ObjectUtils.asBoolean(val, false));
                } else if (key.equals("shorten")) {
                    builder.setShorten(ObjectUtils.asBoolean(val, false));
                } else if (key.equals("load_strategies")) {
                    builder.setLoadStrategies(ObjectUtils.asBoolean(val, true));
                } else {
//                	Log.w("Cloudinary", "ignoring invalid parameter " + val);
                }
            }
        }
        return builder.build();
    }

    /**
     * Build a new {@link Configuration}
     */
    public static class Builder {
        private String cloudName;
        private String apiKey;
        private String apiSecret;
        private String secureDistribution;
        private String cname;
        private String uploadPrefix;
        private boolean secure;
        private boolean privateCdn;
        private boolean cdnSubdomain;
        private boolean shorten;
        private String callback;
        private String proxyHost;
        private int proxyPort;
        private Boolean secureCdnSubdomain;
        private boolean useRootPath;
        private boolean loadStrategies = true;
        private int timeout;

        /**
         * Set the HTTP connection timeout.
         *
         * @param timeout time in milliseconds, or 0 to use the default platform value
         * @return builder for chaining
         */
        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Creates a {@link Configuration} with the arguments supplied to this builder
         */
        public Configuration build() {
            return new Configuration(cloudName, apiKey, apiSecret, secureDistribution, cname, uploadPrefix, secure, privateCdn, cdnSubdomain, shorten, callback, proxyHost, proxyPort, secureCdnSubdomain, useRootPath, timeout, loadStrategies);
        }

        /**
         * The unique name of your cloud at Cloudinary
         * You can find your cloud name in the Account Details section in the dashboard of Cloudinary Management Console.
         */
        public Builder setCloudName(String cloudName) {
            this.cloudName = cloudName;
            return this;
        }

        /**
         * API Key
         * You can find API Key in the Account Details section in the dashboard of Cloudinary Management Console.
         */
        public Builder setApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * API Secret
         * You can find API Secret in the Account Details section in the dashboard of Cloudinary Management Console.
         */
        public Builder setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
            return this;
        }

        /**
         * The domain name of the CDN distribution to use for building HTTPS URLs.
         * Relevant only for Advanced plan's users that have a private CDN distribution.
         */
        public Builder setSecureDistribution(String secureDistribution) {
            this.secureDistribution = secureDistribution;
            return this;
        }

        /**
         * Custom domain name to use for building HTTP URLs.
         * Relevant only for Advanced plan's users that have a private CDN distribution and a custom CNAME.
         */
        public Builder setCname(String cname) {
            this.cname = cname;
            return this;
        }

        /**
         * Force HTTPS URLs of images even if embedded in non-secure HTTP pages.
         */
        public Builder setSecure(boolean secure) {
            this.secure = secure;
            return this;
        }

        /**
         * Should be set to true for Advanced plan's users that have a private CDN distribution.
         */
        public Builder setPrivateCdn(boolean privateCdn) {
            this.privateCdn = privateCdn;
            return this;
        }

        public Builder setSecureCdnSubdomain(Boolean secureCdnSubdomain) {
            this.secureCdnSubdomain = secureCdnSubdomain;
            return this;
        }


        /**
         * Whether to automatically build URLs with multiple CDN sub-domains.
         */
        public Builder setCdnSubdomain(boolean cdnSubdomain) {
            this.cdnSubdomain = cdnSubdomain;
            return this;
        }

        public Builder setShorten(boolean shorten) {
            this.shorten = shorten;
            return this;
        }

        public Builder setCallback(String callback) {
            this.callback = callback;
            return this;
        }

        public Builder setUploadPrefix(String uploadPrefix) {
            this.uploadPrefix = uploadPrefix;
            return this;
        }

        public Builder setUseRootPath(boolean useRootPath) {
            this.useRootPath = useRootPath;
            return this;
        }

        public Builder setLoadStrategies(boolean loadStrategies) {
            this.loadStrategies = loadStrategies;
            return this;
        }

        /**
         * Initialize builder from existing {@link Configuration}
         *
         * @param other a different configuration object
         * @return an initialized builder configured with <code>other</code>
         */
        public Builder from(Configuration other) {
            this.cloudName = other.cloudName;
            this.apiKey = other.apiKey;
            this.apiSecret = other.apiSecret;
            this.secureDistribution = other.secureDistribution;
            this.cname = other.cname;
            this.uploadPrefix = other.uploadPrefix;
            this.secure = other.secure;
            this.privateCdn = other.privateCdn;
            this.cdnSubdomain = other.cdnSubdomain;
            this.shorten = other.shorten;
            this.callback = other.callback;
            this.proxyHost = other.proxyHost;
            this.proxyPort = other.proxyPort;
            this.secureCdnSubdomain = other.secureCdnSubdomain;
            this.useRootPath = other.useRootPath;
            this.loadStrategies = other.loadStrategies;
            this.timeout = other.timeout;
            return this;
        }
    }
}