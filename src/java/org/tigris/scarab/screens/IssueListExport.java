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

import java.util.List;
import java.util.Iterator;

import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.RModuleUserAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.util.word.QueryResult;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;


/**
 * Handles export of an issue list non-web formats.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:stack@collab.net">St.Ack</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @see org.tigris.scarab.screens.DataExport
 * @since Scarab 1.0
 */
public class IssueListExport extends DataExport
{
    /**
     * Writes the response.
     */
    public void doBuildTemplate(RunData data, TemplateContext context)
        throws Exception 
    {
        super.doBuildTemplate(data, context);

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        MITList mitlist = user.getCurrentMITList();
        TSVPrinter printer = new TSVPrinter(data.getResponse().getWriter());
        List rmuas = scarabR.getRModuleUserAttributes();
        writeHeading(printer, mitlist, l10n, rmuas);
        writeRows(printer, mitlist, l10n, scarabR, rmuas);
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
        if (mitlist != null)
        {
            if (!mitlist.isSingleModule())
            {
                printer.print(l10n.get("CapModule"));
            }

            if (!mitlist.isSingleIssueType())
            {
                printer.print(l10n.get("IssueType"));
            }
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
                // FIXME! don't do this QueryResult should already have
                // the info pcn#16558
                Issue issue = IssueManager.getIssueById(issueId);
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
        if (mitlist != null)
        {
            if (!mitlist.isSingleModule())
            {
                printer.print(issue.getModule().getRealName());
            }

            if (!mitlist.isSingleIssueType())
            {
                printer.print(issue.getRModuleIssueType().getDisplayName());
            }
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
}
