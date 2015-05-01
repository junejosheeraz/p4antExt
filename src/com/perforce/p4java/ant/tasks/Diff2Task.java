/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.List;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.option.server.GetFileDiffsOptions;

/**
 * Run diff (on the server) of two files in the depot. Both files may optionally
 * include a revision specification; the default is to compare the head
 * revision. Wildcards may be used, but they must match between file1 and file2.
 * Note if using clients or labels as file arguments they must be preceded with
 * a file path e.g. //...@mylabel //...@yourlabel.</p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class Diff2Task extends ClientTask {

    /**
     * The first depot file and revision specifier for diff2.
     */
    protected String file1;

    /**
     * The second depot file and revision specifier for diff2.
     */
    protected String file2;

    /**
     * If not null, it causes diff2 to use the branch view to specify the pairs
     * of files to compare. If file arguments are also present, they can further
     * limit the files and specify the revisions for comparison. Note that if
     * only one file is given, it restricts the right-hand side of the branch
     * view.
     */
    protected String branch;

    /**
     * If true, suppresses the display of the header lines of files whose
     * content and types are identical and suppresses the actual diff for all
     * files. Corresponds to the -q flag.
     */
    protected boolean quiet = false;

    /**
     * If true, diff even files with non-text (binary) types. Corresponds to the
     * -t flag.
     */
    protected boolean includeNonTextDiffs = false;

    /**
     * If true, use the GNU diff -u format and displays only files that differ.
     * See the "-u" option in the main diff2 documentation for an explanation.
     * Corresponds to the -u flag.
     */
    boolean gnuDiffs = false;

    /** If true, use RCS diff; corresponds to -dn. */
    protected boolean rcsDiffs = false;

    /**
     * If positive, specifies the number of context diff lines; if zero, lets
     * server pick context number; if negative, no options are generated.
     * Corresponds to -dc[n], with -dc generated for diffContext == 0, -dcn for
     * diffContext > 0, where "n" is of course the value of diffContext.
     */
    protected int diffContext = -1;

    /** If true, perform summary diff; corresponds to -ds. */
    protected boolean summaryDiff = false;

    /**
     * If true, do a unified diff; corresponds to -du[n] with -du generated for
     * unifiedDiff == 0, -dun for unifiedDiff > 0, where "n" is of course the
     * value of unifiedDiff.
     */
    protected int unifiedDiff = -1;
    /** If true, ignore whitespace changes; corresponds to -db. */
    protected boolean ignoreWhitespaceChanges = false;

    /** If true, ignore whitespace; corresponds to -dw. */
    protected boolean ignoreWhitespace = false;

    /** If true, ignore line endings; corresponds to -dl. */
    protected boolean ignoreLineEndings = false;

    /**
     * Left depot file and revision specifier used as input for this Perforce
     * command.
     */
    protected IFileSpec file1FileSpec;

    /**
     * Right depot file and revision specifier used as input for this Perforce
     * command.
     */
    protected IFileSpec file2FileSpec;

    /**
     * Collection of file diffs returned from the Perforce command.
     */
    protected List<IFileDiff> retFileDiffs;

    /**
     * Default constructor.
     */
    public Diff2Task() {
        super();
        commandOptions = new GetFileDiffsOptions(quiet, includeNonTextDiffs,
                gnuDiffs, rcsDiffs, diffContext, summaryDiff, unifiedDiff,
                ignoreWhitespaceChanges, ignoreWhitespace, ignoreLineEndings);
    }

    /**
     * Sets the file1.
     *
     * @param file1
     *            the new file1
     */
    public void setFile1(String file1) {
        this.file1 = file1;
    }

    /**
     * Sets the file2.
     *
     * @param file2
     *            the new file2
     */
    public void setFile2(String file2) {
        this.file2 = file2;
    }

    /**
     * Sets the branch.
     *
     * @param branch
     *            the new branch
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Sets the quiet.
     *
     * @param quiet
     *            the new quiet
     */
    public void setQuiet(boolean quiet) {
        ((GetFileDiffsOptions) commandOptions).setQuiet(quiet);
    }

    /**
     * Sets the include non text diffs.
     *
     * @param includeNonTextDiffs
     *            the new include non text diffs
     */
    public void setIncludeNonTextDiffs(boolean includeNonTextDiffs) {
        ((GetFileDiffsOptions) commandOptions)
                .setIncludeNonTextDiffs(includeNonTextDiffs);
    }

    /**
     * Sets the gnu diffs.
     *
     * @param gnuDiffs
     *            the new gnu diffs
     */
    public void setGnuDiffs(boolean gnuDiffs) {
        ((GetFileDiffsOptions) commandOptions).setGnuDiffs(gnuDiffs);
    }

    /**
     * Sets the rcs diffs.
     *
     * @param rcsDiffs
     *            the new rcs diffs
     */
    public void setRcsDiffs(boolean rcsDiffs) {
        ((GetFileDiffsOptions) commandOptions).setRcsDiffs(rcsDiffs);
    }

    /**
     * Sets the diff context.
     *
     * @param diffContext
     *            the new diff context
     */
    public void setDiffContext(int diffContext) {
        ((GetFileDiffsOptions) commandOptions).setDiffContext(diffContext);
    }

    /**
     * Sets the summary diff.
     *
     * @param summaryDiff
     *            the new summary diff
     */
    public void setSummaryDiff(boolean summaryDiff) {
        ((GetFileDiffsOptions) commandOptions).setSummaryDiff(summaryDiff);
    }

    /**
     * Sets the unified diff.
     *
     * @param unifiedDiff
     *            the new unified diff
     */
    public void setUnifiedDiff(int unifiedDiff) {
        ((GetFileDiffsOptions) commandOptions).setUnifiedDiff(unifiedDiff);
    }

    /**
     * Sets the ignore whitespace changes.
     *
     * @param ignoreWhitespaceChanges
     *            the new ignore whitespace changes
     */
    public void setIgnoreWhitespaceChanges(boolean ignoreWhitespaceChanges) {
        ((GetFileDiffsOptions) commandOptions)
                .setIgnoreWhitespaceChanges(ignoreWhitespaceChanges);
    }

    /**
     * Sets the ignore whitespace.
     *
     * @param ignoreWhitespace
     *            the new ignore whitespace
     */
    public void setIgnoreWhitespace(boolean ignoreWhitespace) {
        ((GetFileDiffsOptions) commandOptions)
                .setIgnoreWhitespace(ignoreWhitespace);
    }

    /**
     * Sets the ignore line endings.
     *
     * @param ignoreLineEndings
     *            the new ignore line endings
     */
    public void setIgnoreLineEndings(boolean ignoreLineEndings) {
        ((GetFileDiffsOptions) commandOptions)
                .setIgnoreLineEndings(ignoreLineEndings);
    }

    /**
     * Gets the file1 file spec.
     *
     * @return the file1 file spec
     */
    public IFileSpec getFile1FileSpec() {
        return file1FileSpec;
    }

    /**
     * Gets the file2 file spec.
     *
     * @return the file2 file spec
     */
    public IFileSpec getFile2FileSpec() {
        return file2FileSpec;
    }

    /**
     * Gets the ret file diffs.
     *
     * @return the ret file diffs
     */
    public List<IFileDiff> getRetFileDiffs() {
        return retFileDiffs;
    }

    /**
     * Execute the Perforce diff2 command with file spec 1, file spec 2, branch
     * and options. Log the returned file diffs. This command is perform on the
     * server side.
     * <p>
     * Run diff on the Perforce server of two files in the depot.
     * <p>
     * This method corresponds closely to the standard diff2 command, and that
     * command's documentation should be consulted for the overall and detailed
     * semantics
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        try {
            file1FileSpec = new FileSpec(file1);
            file2FileSpec = new FileSpec(file2);
            retFileDiffs = getP4Server().getFileDiffs(file1FileSpec,
                    file2FileSpec, branch,
                    ((GetFileDiffsOptions) commandOptions));
            logFileDiffs(retFileDiffs);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }
}
