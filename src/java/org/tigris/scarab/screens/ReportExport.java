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

import java.util.Date;
import java.util.List;
import java.util.Iterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.StringUtils;

import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.reports.ReportBridge;
import org.tigris.scarab.reports.ReportDate;
import org.tigris.scarab.reports.ReportGroup;
import org.tigris.scarab.reports.ReportOptionAttribute;
import org.tigris.scarab.reports.ReportHeading;
import org.tigris.scarab.reports.ReportTableModel;
import org.tigris.scarab.reports.ReportUserAttribute;
import org.tigris.scarab.tools.Format;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.Log;

/**
 * Handles export of a report to non-web formats.
 *
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @version $Id: ReportExport.java,v 1.2 2003/05/03 22:37:24 jon Exp $
 * @see org.tigris.scarab.screens.DataExport
 * @since Scarab 1.0
 */
public class ReportExport extends DataExport
{
    /**
     * What to show if a header cell is empty.
     */
    private static final String NO_HEADER = "=======";

    /**
     * Writes the response.
     *
     * Modelled after the <code>#reportTable()</code> Velocimacro.
     */
    public void doBuildTemplate(RunData data, TemplateContext context)
        throws Exception 
    {
        super.doBuildTemplate(data, context);

        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        TSVPrinter printer = new TSVPrinter(data.getResponse().getWriter());

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        ReportTableModel model = report.getModel(user);

        List rowHeadings = model.getRowHeadings();
        List columnHeadings = model.getColumnHeadings();
        int multiplier = 1;
        for (int level = 0; level < columnHeadings.size(); level++)
        {
            ReportHeading columnHeadingLevel =
                (ReportHeading) columnHeadings.get(level);
            int maxBlank = rowHeadings.size() - 1;
            if (maxBlank > 0)
            {
                for (int i = 1; i <= maxBlank; i++)
                {
                    // Empty heading cell.
                    printer.print(NO_HEADER);
                }
            }

            boolean singleAttribute = columnHeadingLevel.singleAttribute();
            // It would be preferable to concatenate an arrow
            // pointing to the right to the return value of
            // displayAttribute(), but I wasn't able to come up with a
            // good "down arrow" for the X-axis headings.
            printer.print(singleAttribute
                          ? model.displayAttribute(columnHeadingLevel.get(0))
                          : NO_HEADER);
            int colspan = model.getColspan(level);
            for (int i = 1; i <= multiplier; i++)
            {
                for (int nextIndex = 1; nextIndex <= columnHeadingLevel.size();
                     nextIndex++)
                {
                    Object heading = columnHeadingLevel.get(nextIndex - 1);
                    printer.print(reportLabel(model, heading, singleAttribute,
                                              l10n));
                }
                if (colspan > 1)
                {
                    for (int j = 2; j <= colspan; j++)
                    {
                        printer.print(NO_HEADER);
                    }
                }
            }

            // End of first heading row.
            printer.println();
            multiplier *= columnHeadingLevel.size();
        }

        for (Iterator i = rowHeadings.iterator(); i.hasNext(); )
        {
            i.next();
            printer.print(NO_HEADER);
        }
        int nbrColumns = model.getColumnCount();
        for (int i = 0; i < nbrColumns; i++)
        {
            printer.print(NO_HEADER);
        }
        printer.println();

        int nbrRows = model.getRowCount();

        if (rowHeadings.size() > 1
            || ((ReportHeading) rowHeadings.get(0)).singleAttribute())
        {
            for (int level = 0; level < rowHeadings.size(); level++)
            {
                ReportHeading curHeading =
                    (ReportHeading) rowHeadings.get(level);
                boolean singleAttribute = curHeading.singleAttribute();
                // "Down arrow" is prepend to displayAttribute() in
                // web view.
                printer.print(singleAttribute
                              ? model.displayAttribute(curHeading.get(0))
                              : NO_HEADER);
                    
            }
            for (int i = 0; i < nbrRows; i++)
            {
                printer.print(NO_HEADER);
            }
            printer.println();
        }

        DateFormat dateFormat = new SimpleDateFormat(Format.DATE_TIME_FMT);
        for (int rowIndex = 0; rowIndex < nbrRows; rowIndex++)
        {
            for (int level = 0; level < rowHeadings.size(); level++)
            {
                ReportHeading curHeading =
                    (ReportHeading) rowHeadings.get(level);
                boolean singleAttribute = curHeading.singleAttribute();
                int rowspan = model.getRowspan(level);
                if ((rowIndex % rowspan) == 0)
                {
                    int index = (rowIndex / rowspan) % curHeading.size();
                    printer.print(reportLabel(model, curHeading.get(index),
                                              singleAttribute, l10n));
                }
                else
                {
                    printer.print(NO_HEADER);
                }
            }

            for (int columnIndex = 0; columnIndex < nbrColumns; columnIndex++)
            {
                Object cell = model.getValueAt(rowIndex, columnIndex);
                String cellData = (model.isDate(cell)
                                   ? dateFormat.format((Date) cell)
                                   : cell.toString());
                if (StringUtils.isEmpty(cellData))
                {
                    cellData = NO_CONTENT;
                }
                printer.print(cellData);
            }

            printer.println();
        }
    }

    /**
     * Makes a report label.  Modelled after the
     * <code>#reportLabel()</code> Velocimacro.
     */
    private String reportLabel(ReportTableModel model, Object cellLabel,
                               boolean singleAttribute,
                               ScarabLocalizationTool l10n)
    {
        if (model.isReportDate(cellLabel))
        {
            return Format.getDate(Format.DATE_TIME_FMT,
                                  ((ReportDate) cellLabel).dateValue());
        }
        else if (model.isOption(cellLabel))
        {
            ReportOptionAttribute roa = (ReportOptionAttribute) cellLabel;
            return (singleAttribute
                    ? model.displayOption(roa)
                    : model.displayAttribute(roa) + ": "
                      + model.displayOption(roa));
        }          
        else if (model.isOptionGroup(cellLabel))
        {
            return ((ReportGroup) cellLabel).getName();
        }
        else if (model.isAttributeAndUser(cellLabel))
        {
            ReportUserAttribute rua = (ReportUserAttribute) cellLabel;
            return (singleAttribute
                    ? model.displayUser(rua)
                    : model.displayAttribute(rua) + ": "
                      + model.displayUser(rua));
        }
        else if (model.isUser(cellLabel))
        {
            return l10n.get("Author") + ": "
                + ((ScarabUser) cellLabel).getUserName();
        }
        else
        {
            Log.get().debug("Unhandled cell label type: "
                            + cellLabel.getClass().getName());
            return cellLabel.toString();
        }
    }
}
