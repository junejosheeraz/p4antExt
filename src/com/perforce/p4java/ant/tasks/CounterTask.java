/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;

/**
 * Display, set, or delete a counter. The first form displays the value of the
 * named counter. The second form sets the counter to the given value. The third
 * form deletes the counter. This usually has the same effect as setting the
 * counter to 0. </p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class CounterTask extends ServerTask {

    /** If true, the counter will be deleted. */
    protected boolean delete = false;

    /** If true, this is a Perforce internal counter. */
    protected boolean perforceCounter = false;

    /** The name of the counter. */
    protected String name;

    /** The new value for the counter. */
    protected String value;

    /** The property to be set with the value of the counter. **/
    protected String property = "p4.counter";

    /**
     * Default constructor.
     */
    public CounterTask() {
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
     * Sets the perforce counter.
     *
     * @param perforceCounter
     *            the new perforce counter
     */
    public void setPerforceCounter(boolean perforceCounter) {
        this.perforceCounter = perforceCounter;
    }

    /**
     * Sets the counter name.
     *
     * @param name
     *            the new counter name
     */
    public void setName(String name) {
        this.name = name;
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
     * Sets the property.
     *
     * @param property
     *            the new property
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Execute the Perforce counter command with the following logical sequence:
     * <p>
     * 1. Delete the counter, if "delete" is true.<br>
     * 2. Otherwise, retrieve the counter, if "value" is empty.<br>
     * 3. Otherwise, set the counter with new value. 4. Set the counter value to
     * a specified property.
     * <p>
     * Get the value of a named Perforce counter from the Perforce server. Note
     * that this method will return a zero string (i.e. "0") if the named
     * counter doesn't exist (rather than throw an exception); use getCounters
     * to see if a counter actually exists before you use it.
     *
     * @see PerforceTask#execP4Command()
     */
    @Override
    protected void execP4Command() throws BuildException {
        if (isEmpty(name)) {
            throw new BuildException("No counter name specified."); //$NON-NLS-1$
        }
        try {
            if (delete) {
                getP4Server().deleteCounter(name, perforceCounter);
                return;
            }
            // If the "value" attribute is empty, assume get counter command.
            if (isEmpty(value)) {
                // Assign the counter value to the "value" attribute.
                value = getP4Server().getCounter(name);
            } else {
                getP4Server().setCounter(name, value, perforceCounter);
            }
            if (!isEmpty(property)) {
                // Set the counter value to a specified property.
                getProject().setProperty(property, value);
            }
            // Log the counter name and value
            int messagePriority = Project.MSG_INFO;
            StringBuilder message = new StringBuilder();
            message.append("counter {" + LINE_SEPARATOR);
            message.append(LINE_PADDING).append("name").append("=")
                    .append(name).append(LINE_SEPARATOR);
            message.append(LINE_PADDING).append("value").append("=")
                    .append(value).append(LINE_SEPARATOR);
            message.append("}" + LINE_SEPARATOR);
            log(message.toString(), messagePriority);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }
}
