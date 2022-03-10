package com.cloudinary.utils;

import com.cloudinary.Cloudinary;

import java.util.Arrays;
import java.util.List;

public class Analytics {
    private String sdkTokenQueryKey = "_a"; //sdkTokenQueryKey
    private String sdkQueryDelimiter = "=";
    public String algoVersion = "A";
    public String SDKCode = ""; // Java = G, Android = F
    public String SDKSemver = ""; // Calculate the SDK version .
    public String techVersion = ""; // Calculate the Java version.

    public Analytics() {
        this("G", Cloudinary.VERSION,System.getProperty("java.version"));
    }
    public Analytics(String sdkCode, String sdkVersion, String techVersion) {
        this.SDKCode = sdkCode;
        this.SDKSemver = sdkVersion;
        this.techVersion = techVersion;
    }

    public Analytics setSDKCode(String SDKCode) {
        this.SDKCode = SDKCode;
        return this;
    }

    public Analytics setSDKSemver(String SDKSemver) {
        this.SDKSemver = SDKSemver;
        return this;
    }

    public Analytics setTechVersion(String techVersion) {
        this.techVersion = techVersion;
        return this;
    }

    /**
     * Function turn analytics variables into viable query parameter.
     * @return query param with analytics values.
     */
    public String toQueryParam() {
        try {
            return sdkTokenQueryKey + sdkQueryDelimiter + getAlgorithmVersion() + getSDKType() + getSDKVersion() + getTechVersion() + getSDKFeatureCode();
        } catch (Exception e) {
            return sdkTokenQueryKey + sdkQueryDelimiter + "E";
        }
    }

    private String getTechVersion() throws Exception {
        String[] techVersionString = techVersion.split("_");
        String[] versions = techVersionString[0].split("\\.");
        if (versions.length > 2) {
            versions = Arrays.copyOf(versions, versions.length - 1);
        }
        return getPaddedString(StringUtils.join(versions, "."));
    }

    private String getSDKType() {
        return SDKCode;
    }

    private String getAlgorithmVersion() {
        return "A";
    }

    private String getSDKFeatureCode() {
        return "0";
    }

    private String getSDKVersion() throws Exception {
        return getPaddedString(SDKSemver);
    }

    private String getPaddedString(String string) throws Exception {
        String paddedReversedSemver = "";
        int parts = string.split("\\.").length;
        int paddedStringLength = parts * 6;
        try {
            paddedReversedSemver = reverseVersion(string);
        } catch (Exception e) {
            throw new Exception("Error");
        }
        int num = Integer.parseInt(StringUtils.join(paddedReversedSemver.split("\\."),""));

        String paddedBinary = StringUtils.padStart(Integer.toBinaryString(num), paddedStringLength, '0');

        if (paddedBinary.length() % 6 != 0) {
            throw new Exception("Error");
        }

        String result = "";
        List<String> resultList = StringUtils.getAllSubStringWithSize(paddedBinary,6);
        int i = 0;
        while (i < resultList.size()) {
            result = result + Base64Map.values.get(resultList.get(i));
            i++;
        }
        return result;
    }

    private String reverseVersion(String SDKSemver) throws Exception {
        if (SDKSemver.split("\\.").length < 2) {
            throw new Exception("invalid semVer, must have at least two segments");
        }
        String[] versionArray = SDKSemver.split("\\.");
        for (int i = 0 ; i < versionArray.length; i ++) {
            versionArray[i] = StringUtils.padStart(versionArray[i], 2, '0');
        }
        return StringUtils.join(StringUtils.reverseStringArray(versionArray), ".");
    }
}
