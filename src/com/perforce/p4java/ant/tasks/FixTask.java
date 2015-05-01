/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IFix;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.FixJobsOptions;

/**
 * Mark jobs as being fixed by a changelist number. It marks each named job as
 * being fixed by the changelist number given with -c. The changelist may be
 * either pending or, submitted and the jobs may still be opened or already
 * closed (fixed by another changelist). </p>
 * 
 * If the changelist has already been submitted and the job is still open then
 * 'p4 fix' marks the job closed. If the changelist has not been submitted and
 * the job is still open, the job will be marked closed when the changelist is
 * submitted. If the job is already closed, it is left alone.</p>
 * 
 * @see PerforceTask
 * @see ServerTask
 */
public class FixTask extends ClientTask {

    /**
     * Changelist number to be used for marking each named job as being fixed
     * (given with -c). Initialize to default changelist.
     */
    protected String changelist = String.valueOf(IChangelist.DEFAULT);

    /**
     * List of job IDs, separated by whitespace, for jobs that will have their
     * status marked as "fixed" or specified job status.
     */
    protected String jobs;

    /**
     * If not null, use this as the new status rather than "fixed". Corresponds
     * to the -s flag.
     */
    protected String status = null;

    /**
     * If true, delete the specified fixes. Corresponds to the -d flag.
     */
    protected boolean delete = false;

    /**
     * Collection of file fixes returned from the Perforce command.
     */
    protected List<IFix> retFixList;

    /**
     * Default constructor.
     */
    public FixTask() {
        super();
        commandOptions = new FixJobsOptions(status, delete);
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
     * Sets the jobs.
     * 
     * @param jobs
     *            the new jobs
     */
    public void setJobs(String jobs) {
        this.jobs = jobs;
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            the new status
     */
    public void setStatus(String status) {
        ((FixJobsOptions) commandOptions).setStatus(status);
    }

    /**
     * Sets the delete.
     * 
     * @param delete
     *            the new delete
     */
    public void setDelete(boolean delete) {
        ((FixJobsOptions) commandOptions).setDelete(delete);
    }

    /**
     * Gets the ret fix list.
     * 
     * @return the ret fix list
     */
    public List<IFix> getRetFixList() {
        return retFixList;
    }

    /**
     * Execute the Perforce fix command with jobs, changelist and options. Log
     * the returned fixes.
     * <p>
     * Mark each named job as being fixed by the changelist number given with
     * changelist.
     * 
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        if (isEmpty(jobs)) {
            throw new BuildException("No jobs specified."); //$NON-NLS-1$
        }
        if (parseChangelist(changelist) == IChangelist.DEFAULT) {
            throw new BuildException(
                    "Cannot fix jobs with the default changelist."); //$NON-NLS-1$
        }
        if (parseChangelist(changelist) < 0) {
            throw new BuildException("Must enter a valid changelist."); //$NON-NLS-1$
        }
        try {
            retFixList = getP4Server().fixJobs(getJobs(),
                    parseChangelist(changelist),
                    ((FixJobsOptions) commandOptions));
            logFixes(retFixList);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }

    /**
     * Split up the jobs (by whitespace) into tokens.
     * 
     * @return the jobs
     */
    protected LinkedList<String> getJobs() {
        if (jobs != null) {
            String[] tokens = jobs.trim().split("\\s+");
            if (tokens != null && tokens.length > 0) {
                return new LinkedList<String>(Arrays.asList(tokens));
            }
        }
        return null;
    }
}
