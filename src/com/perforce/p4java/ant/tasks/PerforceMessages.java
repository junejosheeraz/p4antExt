/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Helper class for formatting Perforce messages. It provides locale (language &
 * country) specific messages. The default locale is set during startup of the
 * JVM based on the host environment. </p>
 * 
 * Additionally, this class provides a convenient way to format messages with
 * parameters.
 */
public class PerforceMessages {

    /** Name of the Perforce message bundle properties file. */
    public static final String P4_MESSAGE_BUNDLE = "com.perforce.p4java.ant.tasks.P4MessageBundle";

    /** The locale. */
    private Locale locale;

    /** The messages. */
    private ResourceBundle messages;

    /**
     * Instantiates a new perforce messages.
     */
    public PerforceMessages() {
        this.messages = ResourceBundle.getBundle(P4_MESSAGE_BUNDLE);
    }

    /**
     * Instantiates a new perforce messages.
     * 
     * @param locale
     *            the locale
     */
    public PerforceMessages(Locale locale) {
        this.locale = locale;
        this.messages = ResourceBundle.getBundle(P4_MESSAGE_BUNDLE, locale);
    }

    /**
     * Gets the locale.
     * 
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     * 
     * @param locale
     *            the new locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Gets the messages.
     * 
     * @return the messages
     */
    public ResourceBundle getMessages() {
        return messages;
    }

    /**
     * Sets the messages.
     * 
     * @param messages
     *            the new messages
     */
    public void setMessages(ResourceBundle messages) {
        this.messages = messages;
    }

    /**
     * Gets the message.
     * 
     * @param key
     *            the key
     * @return the message
     */
    public String getMessage(String key) {
        return messages.getString(key);
    }

    /**
     * Gets the message.
     * 
     * @param key
     *            the key
     * @param params
     *            the params
     * @return the message
     */
    public String getMessage(String key, Object[] params) {
        return format(messages.getString(key), params);
    }

    /**
     * Format a message with parameters.
     * 
     * @param message
     *            the message
     * @param params
     *            the params
     * @return the string
     * @see MessageFormat
     */
    public String format(String message, Object[] params) {
        return MessageFormat.format(message, params);
    }
}
