/**
 * Copyright (c) 2010 Perforce Software. All rights reserved.
 */
package com.perforce.p4java.ant.tasks;

import java.util.List;

import org.apache.tools.ant.BuildException;

import com.perforce.p4java.core.IFileLineMatch;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.MatchingLinesOptions;

/**
 * Grep searches files for lines matching a given regular expression, the
 * expression (or pattern) can contain wild cards. The parser used internally is
 * based on V8 regexp and might not be compatible with later parsers, however
 * the majority of functionality is available. </p>
 *
 * If the file argument has a revision, then all files as of that revision are
 * searched. If the file argument has a revision range, then only files selected
 * by that revision range are listed, and the highest revision in the range is
 * used for each file. Normally, the head revision is searched. See 'p4 help
 * revisions' for help specifying revisions.</p>
 *
 * @see PerforceTask
 * @see ServerTask
 */
public class GrepTask extends ClientTask {

    /**
     * The patterns used by p4 grep are regular expressions comparable to those
     * used in UNIX. Corresponds to the p4 grep -e pattern option.
     */
    protected String pattern = null;

    /**
     * Search all revisions within the specified range, rather than only the
     * highest revision in the range. Corresponds to the p4 grep -a option.
     */
    protected boolean allRevisions = false;

    /**
     * Perform case-insensitive pattern matching. (By default, matching is
     * case-sensitive.). Corresponds to the p4 grep -i option.
     */
    protected boolean caseInsensitive = false;

    /**
     * Display a matching line number after the file revision number.
     * Corresponds to the p4 grep -n option.
     */
    protected boolean includeLineNumbers = false;

    /**
     * Display files with non-matching lines. Corresponds to the p4 grep -v
     * option.
     */
    protected boolean nonMatchingLines = false;

    /**
     * Treat binary files as text. (By default, only files of type text are
     * selected for pattern matching.). Corresponds to the p4 grep -t option.
     */
    protected boolean searchBinaries = false;

    /**
     * Display num lines of output context. Corresponds to the p4 grep -C num
     * option; if zero, option is off.
     */
    protected int outputContext = 0;

    /**
     * Display num lines of trailing context after matching lines. Corresponds
     * to the p4 grep -A num option; if zero, option is off.
     */
    protected int trailingContext = 0;

    /**
     * Display num lines of trailing context before matching lines. Corresponds
     * to the p4 grep -B num option; if zero, option is off.
     */
    protected int leadingContext = 0;

    /**
     * If true, interpret the pattern as a fixed string. If false, interpret the
     * pattern as a regular expression. Corresponds to the p4 grep -F and -G
     * options: if true, corresponds to -F; if false, to -G.
     */
    protected boolean fixedPattern = false;

    /**
     * Collection of file line matches returned from the Perforce command.
     */
    protected List<IFileLineMatch> fileLineMatches;

    /**
     * Default constructor.
     */
    public GrepTask() {
        super();
        commandOptions = new MatchingLinesOptions(allRevisions,
                caseInsensitive, includeLineNumbers, nonMatchingLines,
                searchBinaries, outputContext, trailingContext, leadingContext,
                fixedPattern);
    }

    /**
     * Sets the pattern.
     *
     * @param pattern
     *            the new pattern
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Sets the all revisions.
     *
     * @param allRevisions
     *            the new all revisions
     */
    public void setAllRevisions(boolean allRevisions) {
        ((MatchingLinesOptions) commandOptions).setAllRevisions(allRevisions);
    }

    /**
     * Sets the case insensitive.
     *
     * @param caseInsensitive
     *            the new case insensitive
     */
    public void setCaseInsensitive(boolean caseInsensitive) {
        ((MatchingLinesOptions) commandOptions)
                .setCaseInsensitive(caseInsensitive);
    }

    /**
     * Sets the include line numbers.
     *
     * @param includeLineNumbers
     *            the new include line numbers
     */
    public void setIncludeLineNumbers(boolean includeLineNumbers) {
        ((MatchingLinesOptions) commandOptions)
                .setIncludeLineNumbers(includeLineNumbers);
    }

    /**
     * Sets the non matching lines.
     *
     * @param nonMatchingLines
     *            the new non matching lines
     */
    public void setNonMatchingLines(boolean nonMatchingLines) {
        ((MatchingLinesOptions) commandOptions)
                .setNonMatchingLines(nonMatchingLines);
    }

    /**
     * Sets the search binaries.
     *
     * @param searchBinaries
     *            the new search binaries
     */
    public void setSearchBinaries(boolean searchBinaries) {
        ((MatchingLinesOptions) commandOptions)
                .setSearchBinaries(searchBinaries);
    }

    /**
     * Sets the output context.
     *
     * @param outputContext
     *            the new output context
     */
    public void setOutputContext(int outputContext) {
        ((MatchingLinesOptions) commandOptions).setOutputContext(outputContext);
    }

    /**
     * Sets the trailing context.
     *
     * @param trailingContext
     *            the new trailing context
     */
    public void setTrailingContext(int trailingContext) {
        ((MatchingLinesOptions) commandOptions)
                .setTrailingContext(trailingContext);
    }

    /**
     * Sets the leading context.
     *
     * @param leadingContext
     *            the new leading context
     */
    public void setLeadingContext(int leadingContext) {
        ((MatchingLinesOptions) commandOptions)
                .setLeadingContext(leadingContext);
    }

    /**
     * Sets the fixed pattern.
     *
     * @param fixedPattern
     *            the new fixed pattern
     */
    public void setFixedPattern(boolean fixedPattern) {
        ((MatchingLinesOptions) commandOptions).setFixedPattern(fixedPattern);
    }

    /**
     * Gets the file line matches.
     *
     * @return the file line matches
     */
    public List<IFileLineMatch> getFileLineMatches() {
        return fileLineMatches;
    }

    /**
     * Execute the Perforce grep command with file specs, pattern and options.
     * Log the returned line matches.
     * <p>
     * Get list of matching lines in the specified file specs. This method
     * implements the p4 grep command; for full semantics, see the separate p4
     * documentation and / or the GrepOptions Javadoc.
     *
     * @throws BuildException
     *             the build exception
     * @see PerforceTask#execP4Command()
     */
    protected void execP4Command() throws BuildException {
        if (isEmpty(pattern)) {
            throw new BuildException("No pattern specified."); //$NON-NLS-1$
        }
        try {
            fileSpecs = FileSpecBuilder.makeFileSpecList(getFiles());
            fileLineMatches = getP4Server().getMatchingLines(fileSpecs,
                    pattern, ((MatchingLinesOptions) commandOptions));
            logFileLineMatches(fileLineMatches);
        } catch (P4JavaException e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (P4JavaError e) {
            throw new BuildException(e.getLocalizedMessage(), e, getLocation());
        } catch (Throwable t) {
            throw new BuildException(t.getLocalizedMessage(), t, getLocation());
        }
    }
}
