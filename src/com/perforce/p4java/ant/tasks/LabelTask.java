/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.Date;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.ILabel;
import com.perforce.p4java.core.ILabelMapping;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Label;
import com.perforce.p4java.impl.generic.core.Label.LabelMapping;

/**
 * Create a new label specification or edit an existing label specification. A
 * name is required. </p>
 *
 * A label is a shorthand for referring to a collection of revisions. See 'p4
 * help revisions' for information on using labels. A label is either automatic
 * or static. </p>
 *
 * An automatic label refers to the revisions given in the View: and Revision:
 * fields. </p>
 *
 * A static label refers to the revisions associated with the label by the 'p4
 * tag' or 'p4 labelsync' commands. A static label cannot have a Revision:
 * field. </p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class LabelTask extends ClientTask {

    /** Name of the label. */
    protected String name;

    /** Owner of the label. */
    protected String owner;

    /** Description of the label. */
    protected String description;

    /** Revision specification for the label. */
    protected String revision;

    /** Lock the label. */
    protected boolean locked = false;

    /** Delete the label. */
    protected boolean delete = false;

    /** Force the update or deletion of a label. */
    protected boolean force = false;

    /** New or updated label returned from the Perforce command. */
    protected ILabel retLabel;

    /**
     * Default constructor.
     */
    public LabelTask() {
        super();
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
     * Sets the owner.
     *
     * @param owner
     *            the new owner
     */
    public void setOwner(String owner) {
        this.owner = owner;
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
     * Sets the locked.
     *
     * @param locked
     *            the new locked
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Sets the revision.
     *
     * @param revision
     *            the new revision
     */
    public void setRevision(String revision) {
        this.revision = revision;
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
     * Sets the force.
     *
     * @param force
     *            the new force
     */
    public void setForce(boolean force) {
        this.force = force;
    }

    /**
     * Gets the ret label.
     *
     * @return the ret label
     */
    public ILabel getRetLabel() {
        return retLabel;
    }

    /**
     * Execute the Perforce label command with the following logical sequence:
     * <p>
     * 1. Delete the label, if "delete" is true.<br>
     * 2. Otherwise, create a new label, if the required label fields are set.<br>
     * 3. Otherwise, update the label with new info, if it exists.
     * <p>
     * Also, log the info of the newly created or updated label.
     * <p>
     * Delete a named Perforce label from the Perforce server.
     * <p>
     * Update an existing Perforce label in the Perforce server.
     * <p>
     * Create a new Perforce label in the Perforce server.
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        if (isEmpty(name)) {
            throw new BuildException("No label name specified."); //$NON-NLS-1$
        }
        try {
            if (delete) {
                retStatusMessage = getP4Server().deleteLabel(name, force);
                return;
            }
            // Build the view map for the label.
            // Only the left hand side of a mapping is used for labels.
            ViewMap<ILabelMapping> viewMap = new ViewMap<ILabelMapping>();
            String[] viewPaths = getFiles();
            for (int i = 0; i < viewPaths.length; i++) {
                ILabelMapping entry = new LabelMapping(i + 1, viewPaths[i]);
                viewMap.addEntry((LabelMapping) entry);
            }
            // Update existing label.
            retLabel = getP4Server().getLabel(name);
            if (retLabel != null) {
                if (!isEmpty(owner)) {
                    retLabel.setOwnerName(owner);
                }
                retLabel.setLastUpdate(new Date());
                if (!isEmpty(description)) {
                    retLabel.setDescription(description);
                }
                if (!isEmpty(revision)) {
                    retLabel.setRevisionSpec(revision);
                }
                retLabel.setLocked(locked);
                if (viewMap != null && viewMap.getSize() > 0) {
                    retLabel.setViewMapping(viewMap);
                }
                retStatusMessage = getP4Server().updateLabel(retLabel);
                return;
            }
            // Create new label.
            Date lastAccess = null;
            Date lastUpdate = null;
            retLabel = new Label(name, user, lastAccess, lastUpdate,
                    description, revision, locked, viewMap);
            retStatusMessage = getP4Server().createLabel(retLabel);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }
}
