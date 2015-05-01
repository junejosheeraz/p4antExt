/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IJob;
import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;

/**
 * Create and edit job specifications. A job is a defect, enhancement, or other
 * unit of intended work. When a new job is saved a job name of the form
 * jobNNNNNN is created. If a jobName is given on either that named job will be
 * created or, if the job already exists, the job can be modified. </p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class JobTask extends ServerTask {

    /** If true, the job will be deleted. */
    protected boolean delete = false;

    /** The job name. 'new' generates a sequenced job number. */
    protected String name = null;

    /** Job fields. Name value pair */
    protected String fields = null;

    /** The property to be set with the job ID. **/
    protected String property = "p4.job";

    /** New or updated job returned from the Perforce command. */
    protected IJob retJob;

    /**
     * Default constructor.
     */
    public JobTask() {
        super();
    }

    /**
     * Sets the delete.
     *
     * @param delete
     *            the new delete
     */
    public void setDelete(boolean delete) {
        this.delete = delete;
    }

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
     * Sets the property.
     *
     * @param property
     *            the new property
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Gets the ret job.
     *
     * @return the ret job
     */
    public IJob getRetJob() {
        return retJob;
    }

    /**
     * Execute the Perforce job command with the following logical sequence:
     * <p>
     * 1. Delete the job, if "delete" is true.<br>
     * 2. Create a new job, if the job name is "new" or empty.<br>
     * 3. Update the job with update fields, if the job exists.
     * <p>
     * Delete a job from the Perforce server. Note that this method does not
     * change the status of the associated job locally, just on the Perforce
     * server.
     * <p>
     * Create a new Perforce job in the Perforce server corresponding to the
     * passed-in Perforce job fields (which in turn should correspond to at
     * least the mandatory fields defined in the reigning Perforce job spec).
     * <p>
     * Update a Perforce job on the Perforce server. Note that <i>only</i> the
     * associated raw fields map is used for field values; the main description
     * and ID fields are actually ignored.
     * <p>
     * <b>Note:</b> The fields from the "field" nested elements will be used as
     * the raw fields for job creation and update.
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            if (delete) {
                retStatusMessage = getP4Server().deleteJob(name);
                return;
            }
            // Get name-value pair fields.
            Map<String, String> fields = getFields();
            // If the job name is "new" or empty, assume create job command.
            if (isEmpty(name) || name.equalsIgnoreCase("new")) {
                retJob = createJob(fields);
                if (retJob != null) {
                    getProject().setProperty(property, retJob.getId());
                    logJob(retJob);
                }
                return;
            }
            // Update existing job, if it exists
            if (!isEmpty(name)) {
                retJob = getP4Server().getJob(name);
                if (retJob != null) {
                    retJob = updateJob(fields, retJob);
                    getProject().setProperty(property, retJob.getId());
                    logJob(retJob);
                }
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
     * Gets the job spec fields.
     *
     * @param jobSpec
     *            the job spec
     * @return the job spec fields
     */
    private Map<String, Integer> getJobSpecFields(IJobSpec jobSpec) {
        Map<String, Integer> jobSpecFields = new HashMap<String, Integer>();
        if (jobSpec != null) {
            if (jobSpec != null) {
                List<IJobSpecField> fields = jobSpec.getFields();
                for (IJobSpecField field : fields) {
                    jobSpecFields.put(field.getName(), field.getCode());
                }
            }
        }
        return jobSpecFields;
    }

    /**
     * Update job.
     *
     * @param fields
     *            the fields
     * @param job
     *            the job
     * @return the i job
     * @throws P4JavaException
     *             the p4 java exception
     */
    private IJob updateJob(Map<String, String> fields, IJob job)
            throws P4JavaException {
        if (job != null) {
            // Get existing job's raw fields.
            Map<String, Object> rawFields = job.getRawFields();
            // Create update fields from raw fields.
            Map<String, Object> updateFields = new HashMap<String, Object>(
                    rawFields);
            // Exclude fields not in the job spec. For example, the field
            // "specFormatted" causes an "Unknown field name 'specFormatted'"
            // update error.
            IJobSpec jobSpec = getP4Server().getJobSpec();
            if (jobSpec != null) {
                Map<String, Integer> jobSpecFields = getJobSpecFields(jobSpec);
                for (String key : rawFields.keySet()) {
                    if (!jobSpecFields.containsKey(key)) {
                        updateFields.remove(key);
                    }
                }
                // Update fields with name-value pair fields.
                updateFields.putAll(fields);
                job.setRawFields(updateFields);
                job.update();
            }
        }
        return job;
    }

    /**
     * Creates the job.
     *
     * @param fields
     *            the fields
     * @return the i job
     * @throws P4JavaException
     *             the p4 java exception
     */
    private IJob createJob(Map<String, String> fields) throws P4JavaException {
        IJob job = null;
        // Create raw fields from name-value pair fields.
        Map<String, Object> rawFields = new HashMap<String, Object>();
        // Certain field codes have special significance:
        // code 101, required: the job name
        // code 102, optional: the job status
        // code 103, optional: the user who created the job
        // code 104, optional: the date the job was created
        // code 105, optional: the description
        IJobSpec jobSpec = getP4Server().getJobSpec();
        if (jobSpec != null) {
            List<IJobSpecField> jobSpecFields = jobSpec.getFields();
            for (IJobSpecField jobSpecField : jobSpecFields) {
                switch (jobSpecField.getCode()) {
                case 101: // code 101, required: the job name
                    rawFields.put(jobSpecField.getName(), "new");
                    break;
                case 102: // code 102, optional: the job status
                    rawFields.put(jobSpecField.getName(), "open");
                    break;
                case 103: // code 103, optional: the user who created
                          // the job
                    rawFields.put(jobSpecField.getName(), user);
                    break;
                case 104: // code 104, optional: the date the job was
                          // created
                    rawFields.put(jobSpecField.getName(), "");
                    break;
                case 105: // code 105, optional: the description
                    rawFields.put(jobSpecField.getName(), "");
                    break;
                default:
                    break;
                }
            }
            // Update fields with name-value pair fields.
            rawFields.putAll(fields);
            // Create job with raw fields.
            job = getP4Server().createJob(rawFields);
        }
        return job;
    }
}
