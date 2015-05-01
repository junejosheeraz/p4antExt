/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.Date;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.mapbased.server.Server;

/**
 * Create or edit a changelist description. With no argument, 'p4 change'
 * creates a new changelist. If a changelist number is given, 'p4 change' edits
 * an existing, pending changelist. In both cases the changelist specification
 * is placed into a form and the user's editor is invoked. The
 * "deletependingchangelist" (-d flag) option discards a pending changelist, but
 * only if it has no opened files and no pending fixes associated with it.</p>
 *
 * @see PerforceTask
 * @see ClientTask
 */
public class ChangeTask extends ClientTask {

    /** The description of the changelist. */
    protected String description;

    /** If true, the pending changelist will be deleted. */
    protected boolean deletePendingChangelist;

    /** The changelist to be deleted. */
    protected String changelist = String.valueOf(IChangelist.UNKNOWN);

    /** The property to be set with the changelist number. */
    protected String property = "p4.changelist";

    /** New or updated changelist returned by the Perforce command. */
    protected IChangelist retChangelist;

    /**
     * Default constructor.
     */
    public ChangeTask() {
        super();
    }

    /**
     * Sets the description.
     *
     * @param description
     *            the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the delete pending changelist.
     *
     * @param deletePendingChangelist
     *            the new delete pending changelist
     */
    public void setDeletePendingChangelist(boolean deletePendingChangelist) {
        this.deletePendingChangelist = deletePendingChangelist;
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
     * Sets the property.
     *
     * @param property
     *            the new property
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Gets the ret changelist.
     *
     * @return the ret changelist
     */
    public IChangelist getRetChangelist() {
        return retChangelist;
    }

    /**
     * Execute the Perforce change command with the following logical sequence:
     * <p>
     * 1. Delete the changelist, if "deletePendingChangelist" is true.<br>
     * 2. Otherwise, create a new changelist.<br>
     * 3. Assign the new changelist number to a specified property.
     * <p>
     * Create a new changelist for this Perforce client in the associated
     * Perforce server. The newly-created changelist has no files associated
     * with it (regardless of whether the passed-in changelist spec has files
     * associated with it); if you wish to add files to the new changelist, you
     * need to do a reopen on them explictly after the new changelist is
     * returned.
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            if (deletePendingChangelist) {
                retStatusMessage = getP4Server().deletePendingChangelist(
                        parseChangelist(changelist));
                return;
            }
            if (parseChangelist(changelist) == IChangelist.UNKNOWN) {
                Changelist newChangelistImpl = new Changelist(
                        IChangelist.UNKNOWN, getP4Client().getName(), user,
                        ChangelistStatus.NEW, new Date(), description, false,
                        (Server) getP4Server());
                retChangelist = getP4Client().createChangelist(
                        newChangelistImpl);
                logChangelistSummary(retChangelist);
                // Assign the new changelist number to a specified property.
                getProject().setProperty(property, "" + retChangelist.getId());
                return;
            }
            retChangelist = getP4Server().getChangelist(parseChangelist(changelist));
            logChangelistSummary(retChangelist);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }
}
