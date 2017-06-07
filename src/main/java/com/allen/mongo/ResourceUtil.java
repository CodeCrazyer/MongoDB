package com.allen.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created by PC on 2017/6/7.
 */
public class ResourceUtil {
    private static final Logger logger = LoggerFactory.getLogger(ResourceUtil.class);
    public static String get(String fileName, String key) {
        try {
            PropertyResourceBundle bundle = (PropertyResourceBundle) ResourceBundle.getBundle(fileName);
            return bundle.getString(key);
        } catch (Exception e) {
            logger.error(e.toString());
            return null;
        }
    }
}
