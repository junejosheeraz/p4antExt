/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.List;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IJob;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.GetJobsOptions;

/**
 * Reports the list of all jobs currently known to the system. If a file
 * (pattern) is given, only fixes for submitted changelists affecting that file
 * (or set of files) are listed. The file pattern may include wildcards and/or a
 * revision number range. See 'p4 help revisions' for help specifying
 * revisions.</p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class JobsTask extends ClientTask {

    /**
     * If greater than zero, limit the output to the first maxJobs jobs.
     * Corresponds to the -m flag.
     */
    protected int maxJobs = 0;

    /**
     * If true, return full descriptions, otherwise show only a subset
     * (typically the first 128 characters, but this is not guaranteed).
     * Corresponds to the -l flag.
     */
    protected boolean longDescriptions = false;

    /**
     * If true, reverse the normal sort order. Corresponds to the -r flag.
     */
    protected boolean reverseOrder = false;

    /**
     * If true, include any fixes made by changelists integrated into the
     * specified files. Corresponds to the -i flag.
     */
    protected boolean includeIntegrated = false;

    /**
     * If not null, this should be a string in format detailed by
     * "p4 help jobview" used to restrict jobs to those satisfying the job view
     * expression. Corresponds to the -e flag.
     */
    protected String jobView = null;

    /** Collection of jobs returned from the Perforce command. */
    List<IJob> retJobs;

    /**
     * Default constructor.
     */
    public JobsTask() {
        super();
        commandOptions = new GetJobsOptions(maxJobs, longDescriptions,
                reverseOrder, includeIntegrated, jobView);
    }

    /**
     * Sets the max jobs.
     *
     * @param maxJobs
     *            the new max jobs
     */
    public void setMaxJobs(int maxJobs) {
        ((GetJobsOptions) commandOptions).setMaxJobs(maxJobs);
    }

    /**
     * Sets the long descriptions.
     *
     * @param longDescriptions
     *            the new long descriptions
     */
    public void setLongDescriptions(boolean longDescriptions) {
        ((GetJobsOptions) commandOptions).setLongDescriptions(longDescriptions);
    }

    /**
     * Sets the reverse order.
     *
     * @param reverseOrder
     *            the new reverse order
     */
    public void setReverseOrder(boolean reverseOrder) {
        ((GetJobsOptions) commandOptions).setReverseOrder(reverseOrder);
    }

    /**
     * Sets the include integrated.
     *
     * @param includeIntegrated
     *            the new include integrated
     */
    public void setIncludeIntegrated(boolean includeIntegrated) {
        ((GetJobsOptions) commandOptions)
                .setIncludeIntegrated(includeIntegrated);
    }

    /**
     * Sets the job view.
     *
     * @param jobView
     *            the new job view
     */
    public void setJobView(String jobView) {
        ((GetJobsOptions) commandOptions).setJobView(jobView);
    }

    /**
     * Gets the ret jobs.
     *
     * @return the ret jobs
     */
    public List<IJob> getRetJobs() {
        return retJobs;
    }

    /**
     * Execute the Perforce jobs command with file specs and options. Log the
     * returned jobs.
     * <p>
     * Return a list of Perforce jobs. Note that (as discussed in the IJob
     * comments) Perforce jobs can have a wide variety of fields, formats,
     * semantics, etc., and this method can return a list that may have to be
     * unpacked at the map level by the consumer to make any sense of it.
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retJobs = getP4Server().getJobs(fileSpecs,
                    ((GetJobsOptions) commandOptions));
            logJobs(retJobs);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }
}
