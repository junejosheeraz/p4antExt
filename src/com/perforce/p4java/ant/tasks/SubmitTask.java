/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.changelist.SubmitOptions;

/**
 * Commits a pending changelist and its files to the depot. </p>
 *
 * With no argument 'p4 submit' attempts to submit all files in the 'default'
 * changelist. Submit provides the user with a dialog similar to 'p4 change' so
 * the user can compose a changelist description. In this dialog the user is
 * presented with the list of files open in changelist 'default'. Files may be
 * deleted from this list but they cannot be added. (Use an open command (edit,
 * add, delete) to add additional files to a changelist.). </p>
 *
 * If a (single) file pattern is given, only those files in the 'default'
 * changelist that match the pattern will be submitted. </p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class SubmitTask extends ClientTask {

    /**
     * Submits the numbered pending changelist that has been previously created
     * or a failed submit. Initialize to default changelist.
     */
    protected String changelist = String.valueOf(IChangelist.DEFAULT);

    /** The description of the changelist. */
    protected String changelistDescription;

    /**
     * Name of property to set the new changelist number, if the Perforce server
     * renumbers the change.
     */
    protected String changelistProperty;

    /** Name of property to be set to true, if the submit requires a resolve. */
    protected String needsResolveProperty;

    /**
     * If true, it allows submitted files to remain open (on the client's
     * default changelist) after the submit has completed.
     */
    protected boolean reOpen = false;

    /**
     * List of job IDs, separated by whitespace, for jobs that will have their
     * status changed to fixed or "jobStatus".
     */
    protected String jobs;

    /**
     * Status of jobs will be set on a successful submit; if null the jobs will
     * be marked fixed.
     */
    protected String jobStatus;

    /**
     * Default constructor.
     */
    public SubmitTask() {
        super();
        commandOptions = new SubmitOptions(reOpen, null, jobStatus);
    }

    /**
     * Sets the changelist.
     *
     * @param changelist
     *            the new changelist
     */
    public void setChangelist(String changelist) {
        this.changelist = changelist;
    }

    /**
     * Sets the changelist description.
     *
     * @param changelistDescription
     *            the new changelist description
     */
    public void setChangelistDescription(String changelistDescription) {
        this.changelistDescription = changelistDescription;
    }

    /**
     * Sets the changelist property.
     *
     * @param changelistProperty
     *            the new changelist property
     */
    public void setChangelistProperty(String changelistProperty) {
        this.changelistProperty = changelistProperty;
    }

    /**
     * Sets the needs resolve property.
     *
     * @param needsResolveProperty
     *            the new needs resolve property
     */
    public void setNeedsResolveProperty(String needsResolveProperty) {
        this.needsResolveProperty = needsResolveProperty;
    }

    /**
     * Sets the re open.
     *
     * @param reOpen
     *            the new re open
     */
    public void setReOpen(boolean reOpen) {
        ((SubmitOptions) commandOptions).setReOpen(reOpen);
    }

    /**
     * Sets the jobs.
     *
     * @param jobs
     *            the new jobs
     */
    public void setJobs(String jobs) {
        ((SubmitOptions) commandOptions).setJobIds(getJobs(jobs));
    }

    /**
     * Sets the job status.
     *
     * @param jobStatus
     *            the new job status
     */
    public void setJobStatus(String jobStatus) {
        ((SubmitOptions) commandOptions).setJobStatus(jobStatus);
    }

    /**
     * Execute the Perforce submit command with changelist and options. Assign
     * the new changelist number to a specified property. Log the returned file
     * specs.
     * <p>
     * Submit this changelist and associate it with any jobs in the passed-in
     * options. Will fail with a suitable request exception if this is not a
     * pending changelist associated with the current client.
     * <p>
     * If the submit is successful, the status of the underlying changelist will
     * be updated to reflect the new status. Other fields will not be
     * automatically updated and need to be refreshed with the refresh() method
     * if you need to access them live.
     * <p>
     * Note that the list of filespecs returned from the submit will contain
     * only summary filespecs for successful files -- generally only the depot
     * path, action, and revisions fields will be valid; other fields may be
     * null or undefined depending on the server and client implementations.
     * That is, do not rely on the returned filespec list for anything other
     * than depot paths.
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            IChangelist change = getP4Server().getChangelist(
                    parseChangelist(changelist));
            // Refresh the changelist
            if (change.canRefresh()) {
                if (change.getId() != IChangelist.UNKNOWN) {
                    change.refresh();
                }
            }
            // Set changelist description
            if (!isEmpty(changelistDescription)) {
                change.setDescription(changelistDescription);
                // Update the changelist
                if (change.canUpdate()) {
                    if (change.getId() != IChangelist.DEFAULT
                            && change.getId() != IChangelist.UNKNOWN) {
                        change.update();
                    }
                }
            }
            retFileSpecs = change.submit(((SubmitOptions) commandOptions));
            // Change the value of the "p4.changelist" property in case the
            // Perforce server renumbered changelist after the submit.
            getProject().setProperty("p4.changelist", "" + change.getId());
            // Set the specified changelist property (if exists) with the
            // changelist number.
            if (!isEmpty(changelistProperty)) {
                getProject().setNewProperty(changelistProperty,
                        String.valueOf(change.getId()));
            }
            handleMessage(retFileSpecs);
            logFileSpecs(retFileSpecs);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }

    /**
     * Gets the tokenized (by whitespace) job attribute.
     *
     * @param jobs
     *            the jobs
     * @return the jobs
     */
    protected LinkedList<String> getJobs(String jobs) {
        if (jobs != null) {
            String[] tokens = jobs.trim().split("\\s+");
            if (tokens != null && tokens.length > 0) {
                return new LinkedList<String>(Arrays.asList(tokens));
            }
        }
        return null;
    }

    /**
     * Handle the "must resolve" message for file specs returned from the
     * Perforce submit command. It sets the "p4.needsresolve" to "1" and the
     * specified "needsResolveProperty" attribute to "true".
     *
     * @param fileSpecList
     *            the file spec list
     */
    protected void handleMessage(List<IFileSpec> fileSpecList) {
        // Search pattern "must resolve".
        final String MUST_RESOLVE_PATTERN = "must resolve"; //$NON-NLS-1$
        // Initialize the "needsresolve" property to "0" (false)
        getProject().setProperty("p4.needsresolve", "0");
        if (fileSpecList != null) {
            boolean found = false;
            for (IFileSpec fileSpec : fileSpecList) {
                if (fileSpec != null) {
                    switch (fileSpec.getOpStatus()) {
                    case INFO:
                        // If the "must resolve" fragment is in the info
                        // message, set the "needresolve" property to "1"
                        // (true).
                        if (fileSpec.getStatusMessage().contains(
                                MUST_RESOLVE_PATTERN)) {
                            getProject().setProperty("p4.needsresolve", "1");
                            if (needsResolveProperty != null) {
                                getProject().setNewProperty(
                                        needsResolveProperty, "true");
                            }
                            found = true;
                        }
                        break;
                    default:
                        break;
                    }
                }
                if (found) {
                    // Break out of for loop.
                    break;
                }
            }
        }
    }
}
