/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Helper class for loading a properties file.
 */
public class PerforceProperties {

    /** Properties file suffix. */
    private static final String PROPERTIES_FILE_SUFFIX = ".properties";

    /**
     * Private constructor.
     */
    private PerforceProperties() {
    }

    /**
     * Load a properties file from the path.
     * 
     * @param propertiesFileName
     *            the properties file name
     * @return Properties
     */
    public static Properties loadAsFile(String propertiesFileName) {
        Properties properties = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(propertiesFileName);
            properties = new Properties();
            properties.load(fis);
        } catch (FileNotFoundException ignore) {
            properties = null;
        } catch (IOException ignore) {
            properties = null;
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (Throwable ignore) {
                }
        }
        return properties;
    }

    /**
     * Load a properties file as resource bundle from the classpath.
     * 
     * @param propertiesFileName
     *            the properties file name
     * @return Properties
     */
    public static Properties loadAsResourceBundle(String propertiesFileName) {
        Properties properties = null;
        if (propertiesFileName.startsWith("/")) {
            propertiesFileName = propertiesFileName.substring(1);
        }
        if (propertiesFileName.endsWith(PROPERTIES_FILE_SUFFIX)) {
            propertiesFileName = propertiesFileName.substring(
                    0,
                    propertiesFileName.length()
                            - PROPERTIES_FILE_SUFFIX.length());
        }
        propertiesFileName = propertiesFileName.replace('/', '.');
        // Load as resource bundle.
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            ResourceBundle rb = ResourceBundle.getBundle(propertiesFileName,
                    Locale.getDefault(), loader);
            if (rb != null) {
                properties = new Properties();
                for (Enumeration<String> keys = rb.getKeys(); keys
                        .hasMoreElements();) {
                    final String key = (String) keys.nextElement();
                    final String value = rb.getString(key);
                    properties.put(key, value);
                }
            }
        } catch (MissingResourceException e) {
            properties = null;
        }
        return properties;
    }

    /**
     * Load a properties file as resource stream from the classpath.
     * 
     * @param propertiesFileName
     *            the properties file name
     * @return Properties
     */
    public static Properties loadAsResource(String propertiesFileName) {
        Properties properties = null;
        if (propertiesFileName.startsWith("/")) {
            propertiesFileName = propertiesFileName.substring(1);
        }
        if (propertiesFileName.endsWith(PROPERTIES_FILE_SUFFIX)) {
            propertiesFileName = propertiesFileName.substring(
                    0,
                    propertiesFileName.length()
                            - PROPERTIES_FILE_SUFFIX.length());
        }
        propertiesFileName = propertiesFileName.replace('.', '/');
        if (!propertiesFileName.endsWith(PROPERTIES_FILE_SUFFIX)) {
            propertiesFileName = propertiesFileName
                    .concat(PROPERTIES_FILE_SUFFIX);
        }
        // Load as resource stream.
        InputStream is = null;
        try {
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            is = loader.getResourceAsStream(propertiesFileName);
            if (is != null) {
                properties = new Properties();
                properties.load(is);
            }
        } catch (IOException e) {
            properties = null;
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (Throwable ignore) {
                }
        }
        return properties;
    }

    /**
     * Load a properties file.
     * 
     * @param propertiesFileName
     *            the properties file name
     * @return Properties
     */
    public static Properties load(String propertiesFileName) {
        Properties properties = loadAsFile(propertiesFileName);
        if (properties == null) {
            properties = loadAsResource(propertiesFileName);
        }
        if (properties == null) {
            properties = loadAsResourceBundle(propertiesFileName);
        }
        return properties;
    }
}
