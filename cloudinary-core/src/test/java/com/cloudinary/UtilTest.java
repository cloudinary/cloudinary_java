package com.cloudinary;

import com.cloudinary.utils.ObjectUtils;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by amir on 17/11/2016.
 */
public class UtilTest {
    @Test
    public void encodeContext() throws Exception {
        Map context = ObjectUtils.asMap("caption", "different = caption", "alt2", "alt|alternative");
        String result = Util.encodeContext(context);
        assertTrue("caption=different \\= caption|alt2=alt\\|alternative".equals(result) ||
                "alt2=alt\\|alternative|caption=different \\= caption".equals(result));
    }

}