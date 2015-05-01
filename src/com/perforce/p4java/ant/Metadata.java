/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Prints information about the P4Ant-Built-Version, P4Ant-Built-Date and
 * P4Ant-Built-By in the manifest file.
 */
public class Metadata {

    /** The Constant BUILT_VERSION_ATTRIBUTE. */
    public static final String BUILT_VERSION_ATTRIBUTE = "P4Ant-Built-Version";

    /** The Constant BUILT_DATE_ATTRIBUTE. */
    public static final String BUILT_DATE_ATTRIBUTE = "P4Ant-Built-Date";

    /** The Constant BUILT_BY_ATTRIBUTE. */
    public static final String BUILT_BY_ATTRIBUTE = "P4Ant-Built-By";

    /** Built-Version. */
    private static String builtVersion = "Unknown";

    /** Built-Date. */
    private static String builtDate = "Unknown";

    /** Built-By. */
    private static String builtBy = "Unknown";

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        Class<Metadata> clazz = Metadata.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        if (classPath.startsWith("jar")) {
            String manifestPath = classPath.substring(0,
                    classPath.lastIndexOf("!") + 1)
                    + "/META-INF/MANIFEST.MF";
            try {
                Manifest manifest = new Manifest(
                        new URL(manifestPath).openStream());
                Attributes attr = manifest.getMainAttributes();
                for (Entry<Object, Object> attribute : attr.entrySet()) {
                    if (attribute.getKey().toString()
                            .equalsIgnoreCase(BUILT_VERSION_ATTRIBUTE)) {
                        builtVersion = attribute.getValue().toString();
                    } else if (attribute.getKey().toString()
                            .equalsIgnoreCase(BUILT_DATE_ATTRIBUTE)) {
                        builtDate = attribute.getValue().toString();
                    } else if (attribute.getKey().toString()
                            .equalsIgnoreCase(BUILT_BY_ATTRIBUTE)) {
                        builtBy = attribute.getValue().toString();
                    }
                }
            } catch (MalformedURLException ignore) {
            } catch (IOException ignore) {
            }
        }
        System.out.println(BUILT_VERSION_ATTRIBUTE + ": " + builtVersion);
        System.out.println(BUILT_DATE_ATTRIBUTE + ": " + builtDate);
        System.out.println(BUILT_BY_ATTRIBUTE + ": " + builtBy);
    }
}
