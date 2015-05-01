/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.perforce.p4java.PropertyDefs;
import com.perforce.p4java.exception.MessageSeverityCode;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.mapbased.rpc.sys.helper.RpcSystemFileCommandsHelper;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.option.UsageOptions;
import com.perforce.p4java.option.server.LoginOptions;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.ServerFactory;
import com.perforce.p4java.server.callback.ICommandCallback;

/**
 * Base class for Perforce server specific Ant tasks. It initializes an instance
 * of the Perforce server. </p>
 *
 * @see PerforceTask
 */
public abstract class ServerTask extends PerforceTask {

    /**
     * This inner class is used for handling nested "globaloption" elements.
     */
    public class GlobalOption {
        /**
         * Constructor to create a new instance of "globaloption".
         */
        public GlobalOption() {
        }

        /** The "key" attribute inside the nested "globaloption" element. */
        private String key;

        /** The "value" attribute inside the nested "globaloption" element. */
        private String value;

        /**
         * Sets the key.
         *
         * @param key
         *            the new key
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         * Gets the key.
         *
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Collection of globaloptions (name-value pairs) contained in the
     * "globaloption" nested elements.
     */
    protected List<GlobalOption> globaloptions = Collections
            .synchronizedList(new LinkedList<GlobalOption>());

    /**
     * This method is called by an Ant factory method to instantiates a
     * collection of "globaloption" nested elements. It saves the reference to
     * the collection and returns it to Ant Core.
     *
     * @return the globaloption
     */
    public GlobalOption createGlobalOption() {
        GlobalOption globaloption = new GlobalOption();
        globaloptions.add(globaloption);
        return globaloption;
    }

    /** Default Perforce server protocol-specific properties file name. */
    public static final String P4_SERVER_PROTOCOL_PROPERTIES_FILE = "com.perforce.p4java.ant.tasks.P4ServerProtocol";

    /** Default Perforce server usage properties file name. */
    public static final String P4_SERVER_USAGE_PROPERTIES_FILE = "com.perforce.p4java.ant.tasks.P4ServerUsage";

    /** Perforce server. */
    protected IOptionsServer p4Server;

    /** Perforce server protocol-specific properties file. */
    protected String protocolPropertiesFile = P4_SERVER_PROTOCOL_PROPERTIES_FILE;

    /** Perforce server usage properties file. */
    protected String usagePropertiesFile = P4_SERVER_USAGE_PROPERTIES_FILE;

    /**
     * Protocol-specific properties. Note that these are typically described in
     * usage or implementation notes supplied elsewhere, and are not typically
     * used by end-users.
     */
    protected Properties protocolProps = null;

    /**
     * Perforce server usage properties. Note that these properties are
     * potentially accessed for each command.
     */
    protected Properties usageProps = null;

    /**
     * If not null, will be used to identify the P4Java application's program
     * name to the Perforce server.
     */
    protected String programName = null;

    /**
     * If not null, will be used to identify the P4Java application's program
     * version to the Perforce server.
     */
    protected String programVersion = null;

    /**
     * If not null, this specifies the Perforce server's idea of each command's
     * working directory for the associated server object. Corresponds to the p4
     * -d usage option.
     * <p>
     *
     * This affects all commands on the associated server from this point on,
     * and the passed-in path should be both absolute and valid, otherwise
     * strange errors may appear from the server. If workingDirectory is null,
     * the Java VM's actual current working directory <b>at the time this object
     * is constructed</b> is used instead (which is almost always a safe option
     * unless you're using Perforce alt roots).
     * <p>
     *
     * Note: no checking is done at any time for correctness (or otherwise) of
     * the workingDirectory option.
     */
    protected String workingDirectory = null;

    /**
     * If not null, specifies the host name used by the server's commands. Set
     * to null by the default constructor. Corresponds to the p4 -H usage
     * option. HostName is not live -- that is, unlike many other UsageOption
     * fields, its value is only read once when the associated server is
     * created; subsequent changes will not be reflected in the associated
     * server.
     */
    protected String hostName = null;

    /**
     * If not null, use this field to tell the server which language to use in
     * text messages it sends back to the client. Corresponds to the p4 -L
     * option, with the same limitations. Set to null by the default
     * constructor.
     */
    protected String textLanguage = null;

    /**
     * What will be sent to the Perforce server with each command as the user
     * name if no user name has been explicitly set for servers associated with
     * this UsageOption.
     */
    protected String unsetUserName = null;

    /**
     * If set, this will be used as the name of the client when no client has
     * actually been explicitly set for the associated server(s).
     */
    protected String unsetClientName = null;

    /**
     * Global Perforce server usage options.
     */
    private UsageOptions usageOptions;

    /** If true, corresponds to -a flag on login. */
    protected boolean allHosts = false;

    /**
     * Options for Perforce server login command.
     */
    private LoginOptions loginOptions = new LoginOptions(allHosts);

    /**
     * Options for Perforce command.
     */
    protected Options commandOptions;

    /**
     * Default constructor.
     */
    public ServerTask() {
        super();
        usageOptions = new UsageOptions(null);
    }

    /**
     * Gets the p4 server.
     *
     * @return the p4 server
     */
    public IOptionsServer getP4Server() {
        return p4Server;
    }

    /**
     * Sets the p4 server.
     *
     * @param p4Server
     *            the new p4 server
     */
    public void setP4Server(IOptionsServer p4Server) {
        this.p4Server = p4Server;
    }

    /**
     * Sets the protocol properties file.
     *
     * @param protocolPropertiesFile
     *            the new protocol properties file
     */
    public void setProtocolPropertiesFile(String protocolPropertiesFile) {
        this.protocolPropertiesFile = protocolPropertiesFile;
    }

    /**
     * Sets the usage properties file.
     *
     * @param usagePropertiesFile
     *            the new usage properties file
     */
    public void setUsagePropertiesFile(String usagePropertiesFile) {
        this.usagePropertiesFile = usagePropertiesFile;
    }

    /**
     * Sets the protocol props.
     *
     * @param protocolProps
     *            the new protocol props
     */
    public void setProtocolProps(Properties protocolProps) {
        this.protocolProps = protocolProps;
    }

    /**
     * Sets the usage props.
     *
     * @param usageProps
     *            the new usage props
     */
    public void setUsageProps(Properties usageProps) {
        usageOptions.setProps(usageProps);
    }

    /**
     * Sets the program name.
     *
     * @param programName
     *            the new program name
     */
    public void setProgramName(String programName) {
        usageOptions.setProgramName(programName);
    }

    /**
     * Sets the program version.
     *
     * @param programVersion
     *            the new program version
     */
    public void setProgramVersion(String programVersion) {
        usageOptions.setProgramVersion(programVersion);
    }

    /**
     * Sets the working directory.
     *
     * @param workingDirectory
     *            the new working directory
     */
    public void setWorkingDirectory(String workingDirectory) {
        usageOptions.setWorkingDirectory(workingDirectory);
    }

    /**
     * Sets the host name.
     *
     * @param hostName
     *            the new host name
     */
    public void setHostName(String hostName) {
        usageOptions.setHostName(hostName);
    }

    /**
     * Sets the text language.
     *
     * @param textLanguage
     *            the new text language
     */
    public void setTextLanguage(String textLanguage) {
        usageOptions.setTextLanguage(textLanguage);
    }

    /**
     * Sets the unset user name.
     *
     * @param unsetUserName
     *            the new unset user name
     */
    public void setUnsetUserName(String unsetUserName) {
        usageOptions.setUnsetUserName(unsetUserName);
    }

    /**
     * Sets the unset client name.
     *
     * @param unsetClientName
     *            the new unset client name
     */
    public void setUnsetClientName(String unsetClientName) {
        usageOptions.setUnsetClientName(unsetClientName);
    }

    /**
     * Sets the all hosts.
     *
     * @param allHosts
     *            the new all hosts
     */
    public void setAllHosts(boolean allHosts) {
        loginOptions.setAllHosts(allHosts);
    }

    /**
     * Initialize an instance of the Perforce server from the factory using the
     * specified protocol, server port, protocol specific properties and usage
     * options. Register callback on the server. Connect to server; set the user
     * (if present) to server and login to the server with the user's password
     * (if present).
     */
    protected void initP4Server() {
        try {
            // Initialize Perforce server options.
            initP4ServerOptions();
            // Set default system file helper
            ServerFactory
                    .setRpcFileSystemHelper(new RpcSystemFileCommandsHelper());
            // Get an instance of the P4J server.
            p4Server = ServerFactory.getOptionsServer(protocol + "://" + port,
                    protocolProps, usageOptions);
            // Register server callback.
            p4Server.registerCallback(new ICommandCallback() {
                public void receivedServerMessage(int key, int genericCode,
                        int severityCode, String message) {
                    // Log warning messages from server, since it's not included
                    // in the other callback methods.
                    if (severityCode == MessageSeverityCode.E_WARN) {
                        int messagePriority = Project.MSG_WARN;
                        StringBuilder sb = new StringBuilder();
                        sb.append("receivedServerMessage {" + LINE_SEPARATOR);
                        sb.append(LINE_PADDING).append("genericCode")
                                .append("=").append(genericCode)
                                .append(LINE_SEPARATOR);
                        sb.append(LINE_PADDING).append("severityCode")
                                .append("=").append(severityCode)
                                .append(LINE_SEPARATOR);
                        sb.append(LINE_PADDING).append("message").append("=")
                                .append(message).append(LINE_SEPARATOR);
                        sb.append("}" + LINE_SEPARATOR);
                        log(sb.toString(), messagePriority);
                    }
                }

                public void receivedServerInfoLine(int key, String infoLine) {
                    int messagePriority = Project.MSG_INFO;
                    StringBuilder sb = new StringBuilder();
                    sb.append("receivedServerInfoLine {" + LINE_SEPARATOR);
                    sb.append(LINE_PADDING).append("infoLine").append("=")
                            .append(infoLine).append(LINE_SEPARATOR);
                    sb.append("}" + LINE_SEPARATOR);
                    log(sb.toString(), messagePriority);
                }

                public void receivedServerErrorLine(int key, String errorLine) {
                    int messagePriority = Project.MSG_ERR;
                    StringBuilder sb = new StringBuilder();
                    sb.append("receivedServerErrorLine {" + LINE_SEPARATOR);
                    sb.append(LINE_PADDING).append("errorLine").append("=")
                            .append(errorLine).append(LINE_SEPARATOR);
                    sb.append("}" + LINE_SEPARATOR);
                    log(sb.toString(), messagePriority);
                }

                public void issuingServerCommand(int key, String command) {
                    int messagePriority = Project.MSG_ERR;
                    StringBuilder sb = new StringBuilder();
                    sb.append("issuingServerCommand {" + LINE_SEPARATOR);
                    sb.append(LINE_PADDING).append("key").append("=")
                            .append(key).append(LINE_SEPARATOR);
                    sb.append(LINE_PADDING).append("command").append("=")
                            .append(command).append(LINE_SEPARATOR);
                    sb.append("}" + LINE_SEPARATOR);
                    log(sb.toString(), messagePriority);
                }

                public void completedServerCommand(int key, long millisecsTaken) {
                    int messagePriority = Project.MSG_ERR;
                    StringBuilder sb = new StringBuilder();
                    sb.append("completedServerCommand {" + LINE_SEPARATOR);
                    sb.append(LINE_PADDING).append("key").append("=")
                            .append(key).append(LINE_SEPARATOR);
                    sb.append(LINE_PADDING).append("millisecsTaken")
                            .append("=").append(millisecsTaken)
                            .append(LINE_SEPARATOR);
                    sb.append("}" + LINE_SEPARATOR);
                    log(sb.toString(), messagePriority);
                }
            });
            // Connect to the server.
            p4Server.connect();
            // Set the Perforce charset.
            if (!isEmpty(charset)) {
                if (p4Server.isConnected()) {
                    if (p4Server.supportsUnicode()) {
                        p4Server.setCharsetName(charset);
                    }
                }
            }
            // Set server user.
            if (!isEmpty(user)) {
                p4Server.setUserName(user);
                // Login to the server with a password.
                // Password can be null if it is not needed (i.e. SSO logins).
                p4Server.login(passwd, loginOptions);
            }
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }

    /**
     * Initialize the Perforce server options, such as protocol-specific
     * properties and usage options.
     */
    protected void initP4ServerOptions() {
        // Load server protocol-specific properties for server creation.
        protocolProps = PerforceProperties.load(protocolPropertiesFile);
        setProtocolProps(protocolProps);
        // Load server usage properties for usage options.
        usageProps = PerforceProperties.load(usagePropertiesFile);
        // Merge global options from attributes.
        Properties globalProps = getGlobalOptions();
        if (globalProps != null) {
            if (usageProps == null) {
                usageProps = new Properties();
            }
            usageProps.putAll(globalProps);
            if (protocolProps == null) {
                protocolProps = new Properties();
            }
            protocolProps.putAll(globalProps);
            // Override local attributes.
            String globalClient = globalProps
                    .getProperty(PropertyDefs.CLIENT_NAME_KEY_SHORTFORM,
                            globalProps.getProperty(
                                    PropertyDefs.CLIENT_NAME_KEY, null));
            String globalUser = globalProps.getProperty(
                    PropertyDefs.USER_NAME_KEY_SHORTFORM,
                    globalProps.getProperty(PropertyDefs.USER_NAME_KEY, null));
            String globalPasswd = globalProps.getProperty(
                    PropertyDefs.PASSWORD_KEY_SHORTFORM,
                    globalProps.getProperty(PropertyDefs.PASSWORD_KEY, null));
            if (!isEmpty(globalClient)) {
                client = globalClient;
            }
            if (!isEmpty(globalUser)) {
                user = globalUser;
            }
            if (!isEmpty(globalPasswd)) {
                passwd = globalPasswd;
            }
        }
        setUsageProps(usageProps);
    }

    /**
     * Cleanup the Perforce server instance. Disconnect from the Perforce
     * server. Also, set the server to null.
     *
     * Note: It does not logout, because that will delete the user's ticket.
     */
    protected void cleanupP4Server() {
        try {
            p4Server.disconnect();
            p4Server = null;
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }

    /**
     * Initialize the Perforce server instance.
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#initP4()
     */
    protected void initP4() throws BuildException {
        // Initialize the P4J server.
        initP4Server();
    }

    /**
     * Cleanup the Perforce server instance.
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#cleanupP4()
     */
    protected void cleanupP4() throws BuildException {
        // Cleanup the Perforce server.
        cleanupP4Server();
    }

    /**
     * Combine all of the globaloptions specified by the "globaloption" nested
     * elements.
     *
     * @return the properties
     */
    protected Properties getGlobalOptions() {
        Properties properties = new Properties();

        // Add field name-value pairs specified by the "field" nested elements
        // to the map.
        for (GlobalOption globaloption : globaloptions) {
            properties.put(globaloption.getKey(), globaloption.getValue());
        }
        return properties;
    }
}
