package org.tigris.scarab.screens;

/* ================================================================
 * Copyright (c) 2003 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by CollabNet <http://www.collab.net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of CollabNet.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLABNET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CollabNet.
 */ 

import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;
import java.util.Iterator;

// Turbine Stuff 
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.commons.lang.StringUtils;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.util.word.QueryResult;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;


/**
 * <p>Sends file contents directly to the output stream, setting the
 * <code>Content-Type</code> and writing back to the browser a
 * tab-delimited file (Excel digests this fine).  We used to use <a
 * href="http://jakarta.apache.org/poi/">POI</a> to compose an Excel
 * binary data file, but its outrageous memory consumption didn't
 * scale for large result sets. POI assembles the its output in
 * memory.  After study of the native OLE2 excel file format, it
 * appears very difficult to generate the file in another fashion.</p>
 *
 * <p>Regards output encoding, for now we're assuming the response
 * stream is appropriately set upon fetching. Also, we're assuming
 * that Excel will do the right thing on receipt of our TSV file with
 * Japanese or other multibyte characters (we're not setting an
 * encoding on the <code>Content-Type</code> we return).  Both of the
 * above to be verified.</p>
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:stack@collab.net">St.Ack</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 */
public class IssueListExport extends Default
{
    /**
     * What to show if cell is empty.
     */
    private static final String NO_CONTENT = "-------";

    /**
     * Builds up the context for display of variables on the page.
     */
    public void doBuildTemplate(RunData data, TemplateContext context)
        throws Exception 
    {
        super.doBuildTemplate(data, context);

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        MITList mitlist = user.getCurrentMITList();

        String format = data.getParameters().getString("format");
        if ("tsv".equalsIgnoreCase(format))
        {
            data.getResponse().setContentType("text/plain");
        }
        else
        {
            data.getResponse().setContentType("application/vnd.ms-excel");
        }
        // Since we're streaming the TSV content directly from our
        // data source, we don't know its length ahead of time.
        //data.getResponse().setContentLength(?);
        
        TSVPrinter printer = new TSVPrinter(data.getResponse().getWriter());

        List rmuas = scarabR.getRModuleUserAttributes();
        writeHeading(printer, mitlist, l10n, rmuas);
        writeRows(printer, mitlist, l10n, scarabR, rmuas);

        // Above we sent the response, so no target to render
        data.setTarget(null);
    }

    /**
     * Write out the tsv heading.
     *
     * @param printer Printer to write on.
     * @param mitlist Result list headings.
     * @param l10n Localization tool.
     * @param rmuas
     *
     * @exception Exception
     */
    private void writeHeading(TSVPrinter printer, MITList mitlist, 
                              ScarabLocalizationTool l10n, List rmuas)
        throws Exception
    {
        if (mitlist != null && !mitlist.isSingleModule())
        {
            printer.print(l10n.get("CapModule"));
        }

        if (mitlist != null && !mitlist.isSingleIssueType())
        {
            printer.print(l10n.get("IssueType"));
        }

        printer.print(l10n.get("IssueId"));

        // ISSUE ATTRIBUTE VALUES as column headings.
        if (containsElements(rmuas)) 
        {
            for(Iterator i = rmuas.iterator(); i.hasNext();) 
            {
                RModuleUserAttribute rmua = (RModuleUserAttribute)i.next();
                Attribute userAttribute = rmua.getAttribute();
                printer.print(userAttribute.getName());
            }            
        }
    }

    /**
     * Write out tsv rows.
     *
     * Assumes already written out header.
     *
     * @param printer Printer to write on.
     * @param mitlist Result list headings.
     * @param l10n Localization tool.
     * @param scarabR ScarabRequestTool to use.
     * @param rmuas
     *
     * @exception Exception
     */
    private void writeRows(TSVPrinter printer, MITList mitlist, 
            ScarabLocalizationTool l10n, ScarabRequestTool scarabR, List rmuas)
        throws Exception
    {
        List issueIdList = scarabR.getCurrentSearchResults();
        if (containsElements(issueIdList)) 
        {
            for (Iterator i = issueIdList.iterator();i.hasNext(); )
            {
                String issueId = ((QueryResult)i.next()).getUniqueId();
                Issue issue = scarabR.getIssue(issueId);
                printer.println();
                writeRow(printer, mitlist, l10n, rmuas, issue);
            }
        }
    }

    /**
     * Write out a tsv row.
     *
     * @param printer Printer to write on.
     * @param mitlist Result list headings.
     * @param l10n Localization tool.
     * @param rmuas
     * @param issue Issue to write out in row.
     *
     * @exception Exception
     */
    private void writeRow(TSVPrinter printer, MITList mitlist, 
            ScarabLocalizationTool l10n, List rmuas, Issue issue)
        throws Exception
    {
        if (mitlist != null && !mitlist.isSingleModule())
        {
            printer.print(issue.getModule().getRealName());
        }

        if (mitlist != null && !mitlist.isSingleIssueType())
        {
            printer.print(issue.getRModuleIssueType().getDisplayName());
        }

        printer.print(issue.getUniqueId());

        // Add ISSUE ATTRIBUTE VALUES
        if (containsElements(rmuas))
        {
            for (Iterator rmuai = rmuas.iterator(); rmuai.hasNext(); )
            {
                RModuleUserAttribute rmua = (RModuleUserAttribute)rmuai.next();
                Attribute userAttribute = rmua.getAttribute();
                String value = null;
                if (userAttribute.isUserAttribute())
                {
                    List values = issue.getAttributeValues(userAttribute);
                    if (containsElements(values)) 
                    {
                        StringBuffer buf = new StringBuffer();
                        String comma = null;
                        for (Iterator i = values.iterator(); i.hasNext(); )
                        {
                            if (comma != null)
                            {
                                buf.append(comma);
                            }
                            else
                            {
                                comma = ", ";
                            }

                            buf.append(escapeCommas(((AttributeValue) i.next())
                                                    .getValue()));
                        }
                        value = buf.toString();
                    }
                    else 
                    {
                        value = NO_CONTENT;
                    }   
                }
                else 
                {
                    AttributeValue av = issue.getAttributeValue(userAttribute);
                    value = (av == null ? NO_CONTENT : av.getValue());
                }
                
                printer.print(value);
            }
        }       
    }

    private boolean containsElements(List l)
    {
        return l != null && !l.isEmpty();
    }

    /**
     * Escape any commas in passed string.
     *
     * @param s String to check.
     * @return Passed string with commas escaped.
     */
    private String escapeCommas(String s)
    {
        // Not sure how to escape commas. What to use instead? Quote for now.
        return quote(s);
    }

    /**
     * Quote passed string.
     *
     * @param s String to quote.
     * @return Passed string quoted.
     */
    private String quote(String s)
    {
        return '"' + StringUtils.replace(s, "\"", "\"\"") + '"';
    }

    /**
     * Inner-class to write out tab-delimited file.
     *
     * Uses a printwriter internally to do actual writing.  
     *
     * <p>If you dbl-quote content w/ tabs and newlines in it, excel does the 
     * right thing parsing.
     *
     * <p>This code helped me: <a 
     * href="http://ostermiller.org/utils/ExcelCSVPrinter.java.html">ExcelCSVPrinter.java.html</a>.
     *
     * <p>Inherit javadoc from parent.
     */
    private class TSVPrinter
    {
        /**
         * Start of a new line flag.
         */
        private boolean lineStart = true;

        /**
         * Printer write on.
         */
        private PrintWriter printer = null;

        /**
         * Constructor.
         *
         * @param writer Writer to output on.
         */
        private TSVPrinter(Writer writer)
        {
            if (writer == null)
            {
                throw new NullPointerException("Got a null writer.");
            }
            
            if (writer instanceof PrintWriter)
            {
                this.printer = (PrintWriter) writer;
            }
            else
            {
                this.printer = new PrintWriter(writer);
            }
        }

        /**
         * Prints one field at a time.
         */
        private void print(String s)
        {
            if (!lineStart)
            {
                // Print a tab seperator before we print our field content.
                printer.print('\t');
            }
            lineStart = false;

            if (StringUtils.isNotEmpty(s))
            {
                printer.print(escape(s));
            }
        }

        private void println()
        {
            printer.println();  
            printer.flush();
            lineStart = true;
        }

        /**
         * Escape string.
         *
         * If the passed string has any problematic characters, quote the
         * whole thing after escaping any quotes already present. Excel
         * does the right thing parsing if it gets quoted content.
         *
         * @param s String to escape.
         *
         * @return Escaped version of passed string.
         */
        private String escape(String s)
        {
            if (StringUtils.isNotEmpty(s))
            {
                for (int i = 0; i < s.length(); i++)
                {
                    char c = s.charAt(i);
                    if(c == '"' || c == '\t' || c == '\n' || c == '\r')
                    {
                        s = quote(s);
                        break;
                    }
                }
            }

            return s;
        }
    }
}
