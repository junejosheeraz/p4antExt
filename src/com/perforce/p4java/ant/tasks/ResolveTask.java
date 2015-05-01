/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.client.ResolveFilesAutoOptions;

/**
 * Merge open files with other revisions or files. Automatically resolve the
 * results of a previous Perforce file integration. Note also that having
 * safeMerge, acceptTheirs, acceptYours, and forceResolve all set to false in
 * the associated ResolveFilesAutoOptions object results in "-am" behavior. </p>
 * 
 * @see PerforceTask
 * @see ClientTask
 */
public class ResolveTask extends ClientTask {

    /** If true, only do "safe" resolves, as documented for the p4 "-as" option. */
    protected boolean safeMerge = false;

    /**
     * If true, automatically accept "their" changes, as documented for the p4
     * "-at" option.
     */
    protected boolean acceptTheirs = false;

    /**
     * If true, automatically accept "your" changes, as documented for the p4
     * "-ay" option.
     */
    protected boolean acceptYours = false;

    /**
     * If true, don't do the actual resolve, just return the actions that would
     * have been performed for the resolve.
     */
    protected boolean showActionsOnly = false;

    /**
     * Forces auto-mode resolve to accept the merged file even if there are
     * conflicts. Corresponds to the -af option.
     */
    protected boolean forceResolve = false;

    /**
     * Default constructor.
     */
    public ResolveTask() {
        super();
        commandOptions = new ResolveFilesAutoOptions(showActionsOnly,
                safeMerge, acceptTheirs, acceptYours, forceResolve);
    }

    /**
     * Sets the safe merge.
     * 
     * @param safeMerge
     *            the new safe merge
     */
    public void setSafeMerge(boolean safeMerge) {
        ((ResolveFilesAutoOptions) commandOptions).setSafeMerge(safeMerge);
    }

    /**
     * Sets the accept theirs.
     * 
     * @param acceptTheirs
     *            the new accept theirs
     */
    public void setAcceptTheirs(boolean acceptTheirs) {
        ((ResolveFilesAutoOptions) commandOptions)
                .setAcceptTheirs(acceptTheirs);
    }

    /**
     * Sets the accept yours.
     * 
     * @param acceptYours
     *            the new accept yours
     */
    public void setAcceptYours(boolean acceptYours) {
        ((ResolveFilesAutoOptions) commandOptions).setAcceptYours(acceptYours);
    }

    /**
     * Sets the show actions only.
     * 
     * @param showActionsOnly
     *            the new show actions only
     */
    public void setShowActionsOnly(boolean showActionsOnly) {
        ((ResolveFilesAutoOptions) commandOptions)
                .setShowActionsOnly(showActionsOnly);
    }

    /**
     * Sets the force resolve.
     * 
     * @param forceResolve
     *            the new force resolve
     */
    public void setForceResolve(boolean forceResolve) {
        ((ResolveFilesAutoOptions) commandOptions)
                .setForceResolve(forceResolve);
    }

    /**
     * Execute the Perforce resolve command with file specs and options. Log the
     * returned file specs.
     * <p>
     * Automatically resolve the results of a previousPerforce file integration.
     * <p>
     * Note that this is currently a very limited version of the full Perforce
     * resolve feature, corresponding only to (some of) the various auto-resolve
     * features, meaning this method will never invoke (or need to invoke) end
     * user interaction. More extensive versions of the resolve command will be
     * surfaced as needed.
     * <p>
     * This method notionally returns an IFileSpec, as it's closely related to
     * the integ method and shares many of its return values, but there are
     * several limitations in the use of the returned IFileSpecs. In general,
     * what is returned from this method is a mixture of resolution info
     * messages (i.e. messages from the server that spell out what would or did
     * happen during the resolve), and "true" filespecs. In the latter case, the
     * filespec has a very limited set of valid fields: only client path, from
     * file, and the from revisions are guaranteed to be valid. In the former
     * case, since the info messages do NOT correspond one-to-one with the input
     * file specs that caused the messages, consumers need to explicitly search
     * each returned info message string for the relevant file path or name.
     * This is an unfortunate artefact of the Perforce server's implementation
     * of this command.
     * <p>
     * Note: results and behaviour are undefined if clashing or inconsistent
     * options are used with this method. In general, the behaviour of (e.g.)
     * setting both acceptYours and acceptTheirs true will be whatever the
     * Perforce server makes of it (usually an error), but that's not
     * guaranteed....
     * <p>
     * Note also that having safeMerge, acceptTheirs, acceptYours, and
     * forceResolve all set to false in the associated ResolveFilesAutoOptions
     * object results in "-am" behavior.
     * 
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            retFileSpecs = getP4Client().resolveFilesAuto(fileSpecs,
                    ((ResolveFilesAutoOptions) commandOptions));
            logFileSpecs(retFileSpecs);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }
}
