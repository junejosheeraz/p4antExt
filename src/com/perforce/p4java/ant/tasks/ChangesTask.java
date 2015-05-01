/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.List;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.GetChangelistsOptions;

/**
 * Display list of pending and submitted changelists. If files are specified, it
 * limits its report to changelists that affect those files. If the file
 * specification includes a revision range, it limits its report to submitted
 * changelists that affect those particular revisions.</p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class ChangesTask extends ClientTask {

    /**
     * If positive, restrict the list to the maxMostRecent most recent
     * changelists. Corresponds to -mmax.
     */
    protected int maxMostRecent = 0;

    /**
     * If non-null, restrict the results to changelists associated with the
     * given client. Corresponds to -cclient flag.
     */
    protected String clientName = null;

    /**
     * If non-null, restrict the results to changelists associated with the
     * given user name. Corresponds to -uuser flag.
     */
    protected String userName = null;

    /**
     * If true, also include any changelists integrated into the specified files
     * (if any). Corresponds to -i flag.
     */
    protected boolean includeIntegrated = false;

    /**
     * If not null, restrict output to pending, shelved or submitted
     * changelists. Corresponds to -sstatus flag.
     */
    protected Type type = null;

    /**
     * If true, produce a non-truncated long version of the description.
     * Corresponds to the -l flag.
     */
    protected boolean longDesc = false;

    /**
     * The property to be set with a list of changelist numbers, separated by a
     * whitespace.
     */
    protected String property = "p4.changelists";

    /**
     * Collection of changelist summaries returned from the Perforce command.
     */
    protected List<IChangelistSummary> retChangelistSummaries;

    /**
     * Default constructor.
     */
    public ChangesTask() {
        super();
        commandOptions = new GetChangelistsOptions(maxMostRecent, clientName,
                userName, includeIntegrated, type, longDesc);
    }

    /**
     * Sets the max most recent.
     *
     * @param maxMostRecent
     *            the new max most recent
     */
    public void setMaxMostRecent(int maxMostRecent) {
        ((GetChangelistsOptions) commandOptions)
                .setMaxMostRecent(maxMostRecent);
    }

    /**
     * Sets the client name.
     *
     * @param clientName
     *            the new client name
     */
    public void setClientName(String clientName) {
        ((GetChangelistsOptions) commandOptions).setClientName(clientName);
    }

    /**
     * Sets the user name.
     *
     * @param userName
     *            the new user name
     */
    public void setUserName(String userName) {
        ((GetChangelistsOptions) commandOptions).setUserName(userName);
    }

    /**
     * Sets the include integrated.
     *
     * @param includeIntegrated
     *            the new include integrated
     */
    public void setIncludeIntegrated(boolean includeIntegrated) {
        ((GetChangelistsOptions) commandOptions)
                .setIncludeIntegrated(includeIntegrated);
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(Type type) {
        ((GetChangelistsOptions) commandOptions).setType(type);
    }

    /**
     * Sets the long desc.
     *
     * @param longDesc
     *            the new long desc
     */
    public void setLongDesc(boolean longDesc) {
        ((GetChangelistsOptions) commandOptions).setLongDesc(longDesc);
    }

    /**
     * Sets the property.
     *
     * @param property
     *            the new property
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Gets the ret changelist summaries.
     *
     * @return the ret changelist summaries
     */
    public List<IChangelistSummary> getRetChangelistSummaries() {
        return retChangelistSummaries;
    }

    /**
     * Execute the Perforce changes command with file specs and options. Log the
     * returned changelist summaries and set the changelist numbers to the
     * specified property.
     * <p>
     * Get a list of Perforce changelist summary objects from the Perforce
     * server.
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retChangelistSummaries = getP4Server().getChangelists(fileSpecs,
                    ((GetChangelistsOptions) commandOptions));
            logChangelistSummaries(retChangelistSummaries);
            // Set the changelist numbers to a specified property.
            StringBuilder sb = new StringBuilder();
            for (IChangelistSummary changelistSummary : retChangelistSummaries) {
                sb.append(changelistSummary.getId() + " ");
            }
            getProject().setProperty(property, "" + sb.toString().trim());
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }
}
