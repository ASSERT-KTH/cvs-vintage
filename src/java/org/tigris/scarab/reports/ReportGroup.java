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
import org.apache.commons.lang.ObjectUtils;

public class ReportGroup
    implements java.io.Serializable,
               Retrievable
{
    String name;

    /**
     * Get the Name value.
     * @return the Name value.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the Name value.
     * @param newName The new Name value.
     */
    public void setName(String newName)
    {
        this.name = newName;
    }

    List reportOptionAttributes;

    /**
     * Get the ReportOptionAttributes value.
     * @return the ReportOptionAttributes value.
     */
    public List getReportOptionAttributes()
    {
        return reportOptionAttributes;
    }

    /**
     * Set the ReportOptionAttributes value.
     * @param newReportOptionAttributes The new ReportOptionAttributes value.
     */
    public void setReportOptionAttributes(List newReportOptionAttributes)
    {
        this.reportOptionAttributes = newReportOptionAttributes;
    }

    /**
     * Add a ReportOptionAttribute value.
     * @param newOptionAttribute The new ReportOptionAttribute value.
     */
    public void addReportOptionAttribute(ReportOptionAttribute newReportOptionAttribute)
    {
        if (reportOptionAttributes == null) 
        {
            reportOptionAttributes = new ArrayList();
        }
        reportOptionAttributes.add(newReportOptionAttribute);
    }
    
    List reportUserAttributes;

    /**
     * Get the ReportUserAttributes value.
     * @return the ReportUserAttributes value.
     */
    public List getReportUserAttributes()
    {
        return reportUserAttributes;
    }

    /**
     * Set the ReportUserAttributes value.
     * @param newReportUserAttributes The new ReportUserAttributes value.
     */
    public void setReportUserAttributes(List newReportUserAttributes)
    {
        this.reportUserAttributes = newReportUserAttributes;
    }

    /**
     * Add a ReportUserAttribute value.
     * @param newReportUserAttribute The new ReportUserAttribute value.
     */
    public void addReportUserAttribute(ReportUserAttribute newReportUserAttribute)
    {
        if (reportUserAttributes == null) 
        {
            reportUserAttributes = new ArrayList();
        }
        reportUserAttributes.add(newReportUserAttribute);
    }

    /**
     * sets the option and user lists to null.  does not affect name.
     */
    public void reset()
    {
        reportOptionAttributes = null;
        reportUserAttributes = null;
    }

    /**
     * A group is considered equal if it has the same name. As the name
     * should be unique, there is no need to compare the option/user lists.
     *
     * @param obj an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean equals(Object obj)
    {
        boolean result = obj == this;
        if (!result && obj instanceof ReportGroup) 
        {
            result = ObjectUtils.equals(name,
                ((ReportGroup)obj).getName());
        }
        return result;
    }

    public int hashCode()
    {
        int result = 0;
        if (name != null) 
        {
            result = name.hashCode();
        }
        return result;
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
