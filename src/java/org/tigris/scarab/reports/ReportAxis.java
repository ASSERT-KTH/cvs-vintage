/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
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
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 */

package org.tigris.scarab.reports;

import java.util.List;
import java.util.ArrayList;
import org.apache.fulcrum.intake.Retrievable;

public class ReportAxis
    implements java.io.Serializable,
               Retrievable
{
    /**
     * Gets the heading at the given index.
     * if headingIndex is negative, a new ReportHeading is returned
     * that is 
     *
     * @param axisIndex an <code>int</code> value
     * @param headingIndex an <code>int</code> value
     * @return a <code>ReportHeading</code> value
     */
    public ReportHeading getHeading(int headingIndex)
    {
        ReportHeading heading = null;
        List headings = getReportHeadings();
        if (headingIndex >= 0)
        {
            if (headings == null || headings.size() <= headingIndex) 
            {
                throw new IllegalArgumentException(headingIndex + 
                    " is larger than the number of headings");
            }
            else 
            {
                heading = (ReportHeading)headings.get(headingIndex);
            }
        }
        else 
        {
            heading = new ReportHeading();
            addReportHeading(heading);
        }
        return heading;
    }

    List reportHeadings;

    /**
     * Get the ReportHeadings value.
     * @return the ReportHeadings value.
     */
    public List getReportHeadings()
    {
        return reportHeadings;
    }

    /**
     * Set the ReportHeadings value.
     * @param newReportHeadings The new ReportHeadings value.
     */
    public void setReportHeadings(List newReportHeadings)
    {
        this.reportHeadings = newReportHeadings;
    }

    /**
     * Add a ReportHeading value.
     * @param newReportHeading The new ReportHeading value.
     */
    public void addReportHeading(ReportHeading newReportHeading)
    {
        if (reportHeadings == null) 
        {
            reportHeadings = new ArrayList();
        }
        reportHeadings.add(newReportHeading);
    }

    private String queryKey;

    /**
     * Get the QueryKey value.
     * @return the QueryKey value.
     */ 
    public String getQueryKey()
    {
        if (queryKey == null) 
        {
            return "";
        }
        return queryKey;
    }
    
    /**
     * Set the QueryKey value.
     * @param newQueryKey The new QueryKey value.
     */
    public void setQueryKey(String newQueryKey)
    {
        this.queryKey = newQueryKey;
    }
}
