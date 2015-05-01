/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.IFileLineMatch;
import com.perforce.p4java.core.IFix;
import com.perforce.p4java.core.IJob;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.server.ServerFactory;

/**
 * Base class for Perforce server and client tasks. It defines some common
 * attributes and methods shared by most Perforce tasks. This class is further
 * extended by a base server and a base client classes. </p>
 *
 * Perforce tasks requires some basic properties. These properties are
 * respectively retrieved through individual attributes, project-wide properties
 * and environment variables. </p>
 *
 * @see ServerTask
 * @see ClientTask
 * @see AddTask
 * @see ChangeTask
 * @see ChangesTask
 * @see CounterTask
 * @see DeleteTask
 * @see Diff2Task
 * @see EditTask
 * @see FilesTask
 * @see FixTask
 * @see FstatTask
 * @see GrepTask
 * @see HaveTask
 * @see IntegrateTask
 * @see JobTask
 * @see JobsTask
 * @see LabelTask
 * @see LabelsyncTask
 * @see LockTask
 * @see MoveTask
 * @see ReopenTask
 * @see ResolveTask
 * @see RevertTask
 * @see ShelveTask
 * @see SubmitTask
 * @see SyncTask
 * @see TagTask
 * @see UnlockTask
 * @see UnshelveTask
 */
public abstract class PerforceTask extends Task {

    /**
     * This inner class is used for handling nested "field" elements.
     */
    public class Field {
        /**
         * Constructor to create a new instance of "field".
         */
        public Field() {
        }

        /** The "name" attribute inside the nested "field" element. */
        private String name;

        /** The "value" attribute inside the nested "field" element. */
        private String value;

        /**
         * Sets the name.
         *
         * @param name
         *            the new name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
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
     * Collection of fields (name-value pairs) contained in the "field" nested
     * elements.
     */
    protected List<Field> fields = Collections
            .synchronizedList(new LinkedList<Field>());

    /**
     * This method is called by an Ant factory method to instantiates a
     * collection of "field" nested elements. It saves the reference to the
     * collection and returns it to Ant Core.
     *
     * @return the field
     */
    public Field createField() {
        Field field = new Field();
        fields.add(field);
        return field;
    }

    /**
     * This inner class is used for handling nested "file" elements.
     */
    public class File {
        /**
         * Constructor to create a new instance of "file".
         */
        public File() {
        }

        /** The "path" attribute inside the nested "file" elements. */
        private String path;

        /**
         * Sets the path.
         *
         * @param path
         *            the new path
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * Gets the path.
         *
         * @return the path
         */
        public String getPath() {
            return path;
        }
    }

    /**
     * Collection of file and revision specifiers contained in the "file" nested
     * elements.
     */
    protected List<File> fileList = Collections
            .synchronizedList(new LinkedList<File>());

    /**
     * This method is called by an Ant factory method to instantiates a
     * collection of "file" nested elements. It saves the reference to the list
     * and returns it to Ant Core.
     *
     * @return the files
     */
    public File createFile() {
        File file = new File();
        fileList.add(file);
        return file;
    }

    /** Collection of files contained in Ant's FileSet objects. */
    private List<FileSet> fileSets = Collections
            .synchronizedList(new LinkedList<FileSet>());

    /**
     * This method is use for adding new "fileset" to the collection.
     *
     * @param fileSet
     *            the file set
     */
    public void addFileset(FileSet fileSet) {
        if (fileSet != null) {
            fileSets.add(fileSet);
        }
    }

    /** Perforce message bundle. */
    protected PerforceMessages p4Messages;

    /** Line separator for this system. */
    protected static final String LINE_SEPARATOR = System
            .getProperty("line.separator");

    /** Line padding. */
    protected static final String LINE_PADDING = "    ";

    /**
     * Regular expression pattern for splitting a string by whitespace and
     * sequences of characters that begin and end with a quote.
     */
    protected static final String FILE_TOKEN_REGEX_PATTERN = "[^\\s\"']+|\"([^\"]*)\"|'([^']*)'"; //$NON-NLS-1$

    /** Perforce server protocol. Initialize to default protocol. */
    protected String protocol = ServerFactory.DEFAULT_PROTOCOL_SPEC;

    /** Perforce server host and port. */
    protected String port;

    /** Perforce client workspace. */
    protected String client;

    /** Perforce user. */
    protected String user;

    /** Perforce user's password. */
    protected String passwd;

    /**
     * Character set used for translation of unicode files. P4CHARSET only
     * affects files of type unicode and utf16; non-unicode files are never
     * translated. For servers operating in the default (non-Unicode mode),
     * P4CHARSET must be left unset on client machines. If P4CHARSET is set, but
     * the server is not operating in internationalized mode, the server returns
     * the following error message: "Unicode clients require a unicode enabled
     * server". For servers operating in Unicode mode, P4CHARSET must be set on
     * client machines. If P4CHARSET is unset, but the server is operating in
     * Unicode mode, client programs return the following error message:
     * "Unicode server permits only unicode enabled clients". Note that the
     * names below are not actually the standard name for the charset in some
     * cases: e.g. UTF-8 should be "utf-8" not "utf8", but we follow the
     * Perforce server's rules here. </p>
     *
     * "none", "utf8", "iso8859-1", "shiftjis", "eucjp", "winansi", "cp949",
     * "macosroman", "iso8859-15", "iso8859-5", "koi8-r", "cp1251", "utf16le",
     * "utf16be", "utf16"
     */
    protected String charset;

    /**
     * Perforce file and revision specifiers, separated by whitespace. If the
     * path contains whitespace, it must be double-quoted.
     */
    protected String files;

    /**
     * If true, it will fail on error, otherwise it will keep on going (default
     * is to fail on error).
     */
    protected boolean failOnError = true;

    /**
     * Collection of file and revision specifiers used as input for the Perforce
     * command.
     */
    protected List<IFileSpec> fileSpecs;

    /**
     * Collection of file and revision specifiers returned from the Perforce
     * command.
     */
    protected List<IFileSpec> retFileSpecs;

    /**
     * Result status message returned from the Perforce command.
     */
    protected String retStatusMessage;

    /**
     * Default constructor.
     */
    public PerforceTask() {
        super();
    }

    /**
     * Sets the protocol.
     *
     * @param protocol
     *            the new protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Sets the port.
     *
     * @param port
     *            the new port
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Sets the client.
     *
     * @param client
     *            the new client
     */
    public void setClient(String client) {
        this.client = client;
    }

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Sets the passwd.
     *
     * @param passwd
     *            the new passwd
     */
    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    /**
     * Sets the charset.
     *
     * @param charset
     *            the new charset
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Sets the files.
     *
     * @param files
     *            the new files
     */
    public void setFiles(String files) {
        this.files = files;
    }

    /**
     * Sets the fail on error.
     *
     * @param failOnError
     *            the new fail on error
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Gets the file specs.
     *
     * @return the file specs
     */
    public List<IFileSpec> getFileSpecs() {
        return fileSpecs;
    }

    /**
     * Gets the ret file specs.
     *
     * @return the ret file specs
     */
    public List<IFileSpec> getRetFileSpecs() {
        return retFileSpecs;
    }

    /**
     * Gets the ret status message.
     *
     * @return the ret status message
     */
    public String getRetStatusMessage() {
        return retStatusMessage;
    }

    /**
     * Inits the.
     *
     * @see org.apache.tools.ant.Task#init()
     */
    public void init() {
        initEnv();
    }

    /**
     * Initialize Perforce variables from project properties or system
     * environment, respectively.
     */
    private void initEnv() {
        if (isEmpty(port)) {
            port = getProject().getProperty("p4.port");
            if (isEmpty(port)) {
                port = System.getenv("P4PORT");
            }
        }
        if (isEmpty(user)) {
            user = getProject().getProperty("p4.user");
            if (isEmpty(user)) {
                user = System.getenv("P4USER");
            }
        }
        if (isEmpty(passwd)) {
            passwd = getProject().getProperty("p4.passwd");
            if (isEmpty(passwd)) {
                passwd = System.getenv("P4PASSWD");
            }
        }
        if (isEmpty(client)) {
            client = getProject().getProperty("p4.client");
            if (isEmpty(client)) {
                client = System.getenv("P4CLIENT");
            }
        }
        if (isEmpty(charset)) {
            charset = getProject().getProperty("p4.charset");
            if (isEmpty(charset)) {
                charset = System.getenv("P4CHARSET");
            }
        }
    }

    /**
     * Runs the task.
     *
     * @throws BuildException
     *             the build exception
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        // Load Perforce message bundle.
        p4Messages = new PerforceMessages();
        // Make sure the required attributes are set.
        if (isEmpty(port)) {
            throw new BuildException(
                    p4Messages.getMessage("p4.port.attribute.exception")); //$NON-NLS-1$
        }
        if (isEmpty(user)) {
            throw new BuildException(
                    p4Messages.getMessage("p4.user.attribute.exception")); //$NON-NLS-1$
        }
        if (isEmpty(client)) {
            throw new BuildException(
                    p4Messages.getMessage("p4.client.attribute.exception")); //$NON-NLS-1$
        }
        try {
            // Initialize Perforce server and/or client instances.
            initP4();
            // Execute specific Perforce command.
            execP4Command();
        } catch (Exception e) {
            String failMsg = p4Messages.getMessage("project.task.failed",
                    new Object[] { e.getLocalizedMessage() }); //$NON-NLS-1$
            // If true, stop the task and throw an exception.
            if (failOnError) {
                if (e instanceof BuildException) {
                    throw new BuildException(e);
                } else {
                    throw new BuildException(failMsg, e);
                }
            } else {
                log(failMsg, Project.MSG_ERR);
            }
        } finally {
            try {
                // Clean up Perforce server and/or client instances.
                cleanupP4();
            } catch (Exception e) {
                log(e.getLocalizedMessage(), Project.MSG_WARN);
            }
        }
        // Log the completion of the task.
        log(p4Messages
                .getMessage(
                        "project.task.completed", new Object[] { getProject().getName() }), Project.MSG_INFO); //$NON-NLS-1$
    }

    /**
     * Initialize Perforce server and client instances. This should be
     * implemented in the Perforce server and client subclasses.
     *
     * @throws BuildException
     *             the build exception
     */
    protected abstract void initP4() throws BuildException;

    /**
     * Cleanup Perforce server and client instances; logout, disconnect, etc.
     * This should be implemented by the Perforce server and client subclasses.
     *
     * @throws BuildException
     *             the build exception
     */
    protected abstract void cleanupP4() throws BuildException;

    /**
     * Execute the Perforce command. This should be implemented by the task
     * subclasses handling specific Perforce commands. Please refer to
     * individual Perforce commands for definitions and details.
     *
     * @throws BuildException
     *             the build exception
     */
    protected abstract void execP4Command() throws BuildException;
    
    /**
     * Log the attributes of a list of file specs.
     *
     * @param fileSpecs
     *            the file specs
     */
    protected void logFileSpecs(List<IFileSpec> fileSpecs) {
        if (fileSpecs != null) {
            for (IFileSpec fileSpec : fileSpecs) {
                logFileSpec(fileSpec);
            }
        }
    }

    /**
     * Log the attributes of a file spec.
     *
     * @param fileSpec
     *            the file spec
     */
    protected void logFileSpec(IFileSpec fileSpec) {
        if (fileSpec != null) {
            int messagePriority = Project.MSG_INFO;
            StringBuilder message = new StringBuilder();
            message.append("fileSpec {" + LINE_SEPARATOR);
            if (fileSpec.toString() != null) {
                message.append(LINE_PADDING).append("fileSpec").append("=")
                        .append(fileSpec.toString()).append(LINE_SEPARATOR);
            }
            if (fileSpec.getOpStatus() != null) {
                message.append(LINE_PADDING).append("opStatus").append("=")
                        .append(fileSpec.getOpStatus()).append(LINE_SEPARATOR);
            }
            if (fileSpec.getStatusMessage() != null) {
                message.append(LINE_PADDING).append("statusMessage")
                        .append("=").append(fileSpec.getStatusMessage())
                        .append(LINE_SEPARATOR);
            }
            if (fileSpec.getDepotPath() != null) {
                message.append(LINE_PADDING).append("depotPath").append("=")
                        .append(fileSpec.getDepotPath()).append(LINE_SEPARATOR);
            }
            if (fileSpec.getEndRevision() >= 0) {
                message.append(LINE_PADDING).append("endRevision").append("=")
                        .append(fileSpec.getEndRevision())
                        .append(LINE_SEPARATOR);
            }
            if (fileSpec.getChangelistId() >= 0) {
                message.append(LINE_PADDING).append("changelistId").append("=")
                        .append(fileSpec.getChangelistId())
                        .append(LINE_SEPARATOR);
            }
            if (fileSpec.getAction() != null) {
                message.append(LINE_PADDING).append("action").append("=")
                        .append(fileSpec.getAction()).append(LINE_SEPARATOR);
            }
            if (fileSpec.getFileType() != null) {
                message.append(LINE_PADDING).append("fileType").append("=")
                        .append(fileSpec.getFileType()).append(LINE_SEPARATOR);
            }
            if (fileSpec.getDate() != null) {
                message.append(LINE_PADDING).append("date").append("=")
                        .append(fileSpec.getDate()).append(LINE_SEPARATOR);
            }
            message.append("}" + LINE_SEPARATOR);
            switch (fileSpec.getOpStatus()) {
            case UNKNOWN:
                messagePriority = Project.MSG_WARN;
                break;
            case VALID:
                messagePriority = Project.MSG_INFO;
                break;
            case INFO:
                messagePriority = Project.MSG_INFO;
                break;
            case CLIENT_ERROR:
                messagePriority = Project.MSG_ERR;
                break;
            case ERROR:
                messagePriority = Project.MSG_ERR;
                break;
            default:
                messagePriority = Project.MSG_INFO;
                break;
            }
            log(message.toString(), messagePriority);
        }
    }

    /**
     * Log the attributes of a list of extended file specs.
     *
     * @param extendedFileSpecs
     *            the extended file specs
     */
    protected void logExtendedFileSpecs(
            List<IExtendedFileSpec> extendedFileSpecs) {
        if (extendedFileSpecs != null) {
            for (IExtendedFileSpec extendedFileSpec : extendedFileSpecs) {
                logExtendedFileSpec(extendedFileSpec);
            }
        }
    }

    /**
     * Log the attributes of an extended file spec.
     *
     * @param extendedFileSpec
     *            the extended file spec
     */
    protected void logExtendedFileSpec(IExtendedFileSpec extendedFileSpec) {
        logFileSpec(extendedFileSpec);
    }

    /**
     * Log the attributes of a list of file diffs.
     *
     * @param fileDiffs
     *            the file diffs
     */
    protected void logFileDiffs(List<IFileDiff> fileDiffs) {
        if (fileDiffs != null) {
            for (IFileDiff fileDiff : fileDiffs) {
                logFileDiff(fileDiff);
            }
        }
    }

    /**
     * Log the attributes of a file diff.
     *
     * @param fileDiff
     *            the file diff
     */
    protected void logFileDiff(IFileDiff fileDiff) {
        if (fileDiff != null) {
            int messagePriority = Project.MSG_INFO;
            StringBuilder message = new StringBuilder();
            message.append("fileDiff {" + LINE_SEPARATOR);
            message.append(LINE_PADDING).append("status").append("=")
                    .append(fileDiff.getStatus()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("fileType1").append("=")
                    .append(fileDiff.getFileType1()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("depotFile1").append("=")
                    .append(fileDiff.getDepotFile1()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("revision1").append("=")
                    .append(fileDiff.getRevision1()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("fileType2").append("=")
                    .append(fileDiff.getFileType2()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("depotFile2").append("=")
                    .append(fileDiff.getDepotFile2()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("revision2").append("=")
                    .append(fileDiff.getRevision2()).append(LINE_SEPARATOR);
            message.append("}" + LINE_SEPARATOR);
            switch (fileDiff.getStatus()) {
            case LEFT_ONLY:
                messagePriority = Project.MSG_INFO;
                break;
            case RIGHT_ONLY:
                messagePriority = Project.MSG_INFO;
                break;
            case CONTENT:
                messagePriority = Project.MSG_INFO;
                break;
            case IDENTICAL:
                messagePriority = Project.MSG_INFO;
                break;
            default:
                messagePriority = Project.MSG_INFO;
                break;
            }
            log(message.toString(), messagePriority);
        }
    }

    /**
     * Log the attributes of a list of changelist summaries.
     *
     * @param changelistSummaries
     *            the changelist summaries
     */
    protected void logChangelistSummaries(
            List<IChangelistSummary> changelistSummaries) {
        if (changelistSummaries != null) {
            for (IChangelistSummary changelistSummary : changelistSummaries) {
                logChangelistSummary(changelistSummary);
            }
        }
    }

    /**
     * Log the attributes of a changelist summary.
     *
     * @param changelistSummary
     *            the changelist summary
     */
    protected void logChangelistSummary(IChangelistSummary changelistSummary) {
        if (changelistSummary != null) {
            int messagePriority = Project.MSG_INFO;
            StringBuilder message = new StringBuilder();
            message.append("changelistSummary {" + LINE_SEPARATOR);
            message.append(LINE_PADDING).append("status").append("=")
                    .append(changelistSummary.getStatus())
                    .append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("id").append("=")
                    .append(changelistSummary.getId()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("description").append("=")
                    .append(changelistSummary.getDescription())
                    .append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("username").append("=")
                    .append(changelistSummary.getUsername())
                    .append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("date").append("=")
                    .append(changelistSummary.getDate()).append(LINE_SEPARATOR);
            message.append("}" + LINE_SEPARATOR);
            log(message.toString(), messagePriority);
        }
    }

    /**
     * Log the attributes of a list of fixes.
     *
     * @param fixes
     *            the fixes
     */
    protected void logFixes(List<IFix> fixes) {
        if (fixes != null) {
            for (IFix fix : fixes) {
                logFix(fix);
            }
        }
    }

    /**
     * Log the attributes of a fix.
     *
     * @param fix
     *            the fix
     */
    protected void logFix(IFix fix) {
        if (fix != null) {
            int messagePriority = Project.MSG_INFO;
            StringBuilder message = new StringBuilder();
            message.append("fix {" + LINE_SEPARATOR);
            message.append(LINE_PADDING).append("status").append("=")
                    .append(fix.getStatus()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("action").append("=")
                    .append(fix.getAction()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("date").append("=")
                    .append(fix.getDate()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("jobId").append("=")
                    .append(fix.getJobId()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("changelistId").append("=")
                    .append(fix.getChangelistId()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("clientName").append("=")
                    .append(fix.getClientName()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("userName").append("=")
                    .append(fix.getUserName()).append(LINE_SEPARATOR);
            message.append("}" + LINE_SEPARATOR);
            log(message.toString(), messagePriority);
        }
    }

    /**
     * Log the attributes of a list of file line matches.
     *
     * @param fileLineMatches
     *            the file line matches
     */
    protected void logFileLineMatches(List<IFileLineMatch> fileLineMatches) {
        if (fileLineMatches != null) {
            for (IFileLineMatch fileLineMatch : fileLineMatches) {
                logFileLineMatch(fileLineMatch);
            }
        }
    }

    /**
     * Log the attributes of a file line match.
     *
     * @param fileLineMatch
     *            the file line match
     */
    protected void logFileLineMatch(IFileLineMatch fileLineMatch) {
        if (fileLineMatch != null) {
            int messagePriority = Project.MSG_INFO;
            StringBuilder message = new StringBuilder();
            message.append("fileLineMatch {" + LINE_SEPARATOR);
            message.append(LINE_PADDING).append("type").append("=")
                    .append(fileLineMatch.getType()).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("depotFile").append("=")
                    .append(fileLineMatch.getDepotFile())
                    .append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("revision").append("=")
                    .append(fileLineMatch.getRevision()).append(LINE_SEPARATOR);
            // Suppress line number if -n flag is not set
            if (fileLineMatch.getLineNumber() > 0) {
                message.append(LINE_PADDING).append("lineNumber").append("=")
                        .append(fileLineMatch.getLineNumber())
                        .append(LINE_SEPARATOR);
            }
            message.append(LINE_PADDING).append("line").append("=")
                    .append(fileLineMatch.getLine()).append(LINE_SEPARATOR);
            message.append("}" + LINE_SEPARATOR);
            log(message.toString(), messagePriority);
        }
    }

    /**
     * Log the attributes of a list of jobs.
     *
     * @param jobs
     *            the jobs
     */
    protected void logJobs(List<IJob> jobs) {
        if (jobs != null) {
            for (IJob job : jobs) {
                logJob(job);
            }
        }
    }

    /**
     * Log the attributes of a job.
     *
     * @param job
     *            the job
     */
    protected void logJob(IJob job) {
        if (job != null) {
            int messagePriority = Project.MSG_INFO;
            StringBuilder message = new StringBuilder();
            message.append("job {" + LINE_SEPARATOR);
            Map<String, Object> rawFields = job.getRawFields();
            if (rawFields != null) {
                for (Map.Entry<String, Object> entry : rawFields.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    message.append(LINE_PADDING).append(key).append("=")
                            .append((value != null) ? value : "")
                            .append(LINE_SEPARATOR);
                }
            }
            message.append("}" + LINE_SEPARATOR);
            log(message.toString(), messagePriority);
        }
    }

    /**
     * Combine all of the fields specified by the "field" nested elements.
     *
     * @return the map
     */
    protected Map<String, String> getFields() {
        Map<String, String> map = new HashMap<String, String>();

        // Add field name-value pairs specified by the "field" nested elements
        // to the map.
        for (Field field : fields) {
            map.put(field.getName(), field.getValue());
        }
        return map;
    }

    /**
     * Combine all of the files specified by the "file" attribute, the "file"
     * nested element and Ant's FileSet.
     *
     * @return the files
     */
    protected String[] getFiles() {
        List<String> list = new LinkedList<String>();

        // Add files specified by the "file" attribute to collection.
        // Note: It is tokenized by whitespace and content inside quotes.
        if (files != null) {
            // Split the string by whitespace and sequences of characters that
            // begin and end with a quote.
            Pattern pattern = Pattern.compile(FILE_TOKEN_REGEX_PATTERN);
            Matcher regexMatcher = pattern.matcher(files);
            while (regexMatcher.find()) {
                if (regexMatcher.groupCount() > 0) {
                    if (regexMatcher.group(1) != null) {
                        // Add double-quoted string without the quotes.
                        list.add(regexMatcher.group(1));
                    } else if (regexMatcher.group(2) != null) {
                        // Add single-quoted string without the quotes.
                        list.add(regexMatcher.group(2));
                    } else {
                        // Add unquoted word
                        list.add(regexMatcher.group());
                    }
                }
            }
        }
        // Add file paths specified by the "file" nested elements to the
        // collection.
        for (File file : fileList) {
            list.add(file.getPath());
        }
        // Add files specified by the Ant's FileSet to collection.
        for (FileSet fs : fileSets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            if (srcFiles != null) {
                for (int j = 0; j < srcFiles.length; j++) {
                    java.io.File f = new java.io.File(ds.getBasedir(),
                            srcFiles[j]);
                    list.add(f.getAbsolutePath());
                }
            }
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * Parse the changelist string to a changelist number. Convert the "default"
     * changelist string to the default changelist number. If it is negative
     * return unknown changelist. Otherwise, return the converted changelist
     * number.
     *
     * @param changelist
     *            the changelist
     * @return the int
     */
    protected int parseChangelist(String changelist) {
        if (!isEmpty(changelist)) {
            if (changelist.trim().equalsIgnoreCase("default")) {
                return IChangelist.DEFAULT;
            }
            try {
                int changelistId = Integer.parseInt(changelist);
                if (changelistId < 0) {
                    return IChangelist.UNKNOWN;
                }
                return changelistId;
            } catch (NumberFormatException e) {
                // Suppress error
            }
        }
        return IChangelist.UNKNOWN;
    }

    /**
     * Checks if is empty.
     *
     * @param value
     *            the value
     * @return true, if is empty
     */
    protected boolean isEmpty(String value) {
        if (value == null || value.trim().length() == 0) {
            return true;
        }
        return false;
    }
}
