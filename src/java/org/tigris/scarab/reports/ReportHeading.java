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
import java.util.Iterator;
import java.util.Collections;
import org.apache.torque.om.NumberKey;
import org.apache.fulcrum.intake.Retrievable;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.om.AttributeOptionManager;

public class ReportHeading
    implements java.io.Serializable,
               Retrievable
{
    public int calculateType()
    {
        int type = 0;
        if (getReportGroups() != null) 
        {
            ReportGroup firstGroup = (ReportGroup)getReportGroups().get(0);
            if (firstGroup.getReportUserAttributes() != null)
            {
                type = 1;
            }
        }
        else if (getReportUserAttributes() != null) 
        {
            type = 1;
        }
        else if (getReportDates() != null ||  getReportDateRanges() != null)
        {
            type = 2;
        }
        return type;
    }

    public List retrieveGroupedAttributes()
    {
        List result = null;
        List groups = getReportGroups();
        if (groups == null || groups.isEmpty()) 
        {
            result = Collections.EMPTY_LIST;
        }
        else if (calculateType() == 0)
        {
            result = new ArrayList();
            for (Iterator i = groups.iterator(); i.hasNext();) 
            {
                List options = ((ReportGroup)i.next()).
                    getReportOptionAttributes();
                if (options != null) 
                {
                    result.addAll(options);                    
                }
            }
        }
        else 
        {
            result = new ArrayList();
            for (Iterator i = groups.iterator(); i.hasNext();) 
            {
                List users = ((ReportGroup)i.next()).
                    getReportUserAttributes();
                if (users != null) 
                {
                    result.addAll(users);                    
                }
            }            
        }
        
        return result;
    }

    public void reset()
    {
        reportGroups = null;
        reportOptionAttributes = null;
        reportUserAttributes = null;
        reportDates = null;
        reportDateRanges = null;
    }

    public Object get(int i)
    {
        Object obj = null;
        if (getReportGroups() != null) 
        {
            obj = getReportGroups().get(i);
        }
        else if (getReportOptionAttributes() != null) 
        {
            obj = getReportOptionAttributes().get(i);
        }
        else if (getReportUserAttributes() != null) 
        {
            obj = getReportUserAttributes().get(i);
        }
        // need to sort out dates !FIXME!
        else if (getReportDates() != null ||  getReportDateRanges() != null)
        {
            obj = getReportDates().get(i);
        }
        return obj;
    }

    public int size()
    {
        int size = 0;
        if (getReportGroups() != null) 
        {
            size = getReportGroups().size();
        }
        else if (getReportOptionAttributes() != null) 
        {
            size = getReportOptionAttributes().size();
        }
        else if (getReportUserAttributes() != null) 
        {
            size = getReportUserAttributes().size();
        }
        // need to sort out dates !FIXME!
        else if (getReportDates() != null) 
        {
            size = getReportDates().size();
        }
        return size;
    }

    public boolean singleAttribute()
    {
        boolean result = false;
        Integer firstId = null;
        Iterator options = consolidateReportOptionAttributes().iterator();
        if (options.hasNext()) 
        {
            result = true;
            for (; options.hasNext() && result;) 
            {
                try
                {
                    Integer id = new Integer(
                        AttributeOptionManager.getInstance(
                        new NumberKey(((ReportOptionAttribute)options.next())
                                      .getOptionId().toString()))
                                             .getAttributeId().toString());
                    if (firstId == null) 
                    {
                        firstId = id;
                    }
                    else 
                    {
                        result = firstId.equals(id);
                    }
                }
                catch (Exception e)
                {
                    Log.get().warn("Error on attribute id", e);
                    result = false;
                }
            }
        }

        if (firstId == null) 
        {
            Iterator users = consolidateReportUserAttributes().iterator();
            if (users.hasNext()) 
            {
                result = true;
                for (; users.hasNext() && result;) 
                {
                    Integer id = ((ReportUserAttribute)users.next())
                        .getAttributeId();
                    if (firstId == null) 
                    {
                        firstId = id;
                    }
                    else 
                    {
                        result = firstId.equals(id);
                    }
                }
            }
        }
        return result;
    }


    /**
     * returns all the the ReportOptionAttributes involved in the heading
     * including those in groups
     *
     * @return a <code>List</code> value
     */
    public List consolidateReportOptionAttributes()
    {
        List result = null;
        List groups = getReportGroups();
        if (groups != null && !groups.isEmpty()) 
        {
            for (Iterator i = groups.iterator(); i.hasNext();) 
            {
                ReportGroup group = (ReportGroup)i.next();
                List options = group.getReportOptionAttributes();
                if (options != null && !options.isEmpty()) 
                {
                    for (Iterator j = options.iterator(); j.hasNext();)
                    {
                        if (result == null) 
                        {
                            result = new ArrayList();
                        }
                        result.add(j.next());
                    }
                }
            }
        }
        
        List options = getReportOptionAttributes();
        if (options != null && !options.isEmpty()) 
        {
            for (Iterator j = options.iterator(); j.hasNext();)
            {
                if (result == null) 
                {
                    result = new ArrayList();
                }
                result.add(j.next());
            }
        }
        if (result == null) 
        {
            result = Collections.EMPTY_LIST;
        }

        return result;
    }

    /**
     * returns all the the ReportUserAttributes involved in the heading
     * including those in groups
     *
     * @return a <code>List</code> value
     */
    public List consolidateReportUserAttributes()
    {
        List result = null;
        List groups = getReportGroups();
        if (groups != null && !groups.isEmpty()) 
        {
            for (Iterator i = groups.iterator(); i.hasNext();) 
            {
                ReportGroup group = (ReportGroup)i.next();
                List users = group.getReportUserAttributes();
                if (users != null && !users.isEmpty()) 
                {
                    for (Iterator j = users.iterator(); j.hasNext();)
                    {
                        if (result == null) 
                        {
                            result = new ArrayList();
                        }
                        result.add(j.next());
                    }
                }
            }
        }
        
        List users = getReportUserAttributes();
        if (users != null && !users.isEmpty()) 
        {
            for (Iterator j = users.iterator(); j.hasNext();)
            {
                if (result == null) 
                {
                    result = new ArrayList();
                }
                result.add(j.next());
            }
        }
        if (result == null) 
        {
            result = Collections.EMPTY_LIST;
        }

        return result;
    }

    
    List reportGroups;

    /**
     * Get the ReportGroups value.
     * @return the ReportGroups value.
     */
    public List getReportGroups()
    {
        return reportGroups;
    }

    /**
     * Set the ReportGroups value.
     * @param newReportGroups The new ReportGroups value.
     */
    public void setReportGroups(List newReportGroups)
    {
        this.reportGroups = newReportGroups;
    }

    /**
     * Add a ReportGroup value.
     * @param newReportGroup The new ReportGroup value.
     */
    public void addReportGroup(ReportGroup newReportGroup)
    {
        if (reportGroups == null) 
        {
            reportGroups = new ArrayList();
            // this is the first group, so move any options/users that were
            // part of the heading into the group
            if (newReportGroup.getReportOptionAttributes() == null) 
            {
                newReportGroup
                    .setReportOptionAttributes(getReportOptionAttributes());
                setReportOptionAttributes(null);
            }
            else if (getReportOptionAttributes() != null) 
            {
                newReportGroup.getReportOptionAttributes()
                    .addAll(getReportOptionAttributes());
                setReportOptionAttributes(null);
            }

            if (newReportGroup.getReportUserAttributes() == null) 
            {
                newReportGroup
                    .setReportUserAttributes(getReportUserAttributes());
                setReportUserAttributes(null);
            }
            else if (getReportUserAttributes() != null) 
            {
                newReportGroup.getReportUserAttributes()
                    .addAll(getReportUserAttributes());
                setReportUserAttributes(null);
            }            
        }
        if (!reportGroups.contains(newReportGroup))
        {
            reportGroups.add(newReportGroup);            
        }
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
     * @param newReportOptionAttribute The new ReportOptionAttribute value.
     */
    public void addReportOptionAttribute(ReportOptionAttribute newReportOptionAttribute)
    {
        if (reportOptionAttributes == null) 
        {
            reportOptionAttributes = new ArrayList();
        }
        if (!reportOptionAttributes.contains(newReportOptionAttribute))
        {
            reportOptionAttributes.add(newReportOptionAttribute);            
        }
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
        Log.get().debug("Potentially adding " + newReportUserAttribute);
        
        if (reportUserAttributes == null) 
        {
            reportUserAttributes = new ArrayList();
        }
        if (!reportUserAttributes.contains(newReportUserAttribute))
        {
            Log.get().debug("added " + newReportUserAttribute);
            reportUserAttributes.add(newReportUserAttribute);            
        }
    }


    List reportDateRanges;

    /**
     * Get the ReportDateRanges value.
     * @return the ReportDateRanges value.
     */
    public List getReportDateRanges()
    {
        return reportDateRanges;
    }

    /**
     * Set the ReportDateRanges value.
     * @param newReportDateRanges The new ReportDateRanges value.
     */
    public void setReportDateRanges(List newReportDateRanges)
    {
        this.reportDateRanges = newReportDateRanges;
    }

    /**
     * Add a ReportDateRange value.
     * @param newReportDateRange The new ReportDateRange value.
     */
    public void addReportDateRange(ReportDateRange newReportDateRange)
    {
        if (reportDateRanges == null) 
        {
            reportDateRanges = new ArrayList();
        }
        reportDateRanges.add(newReportDateRange);
    }

    List reportDates;

    /**
     * Get the ReportDates value.
     * @return the ReportDates value.
     */
    public List getReportDates()
    {
        return reportDates;
    }

    /**
     * Set the ReportDates value.
     * @param newReportDates The new ReportDates value.
     */
    public void setReportDates(List newReportDates)
    {
        this.reportDates = newReportDates;
    }

    /**
     * Add a ReportDate value.
     * @param newReportDate The new ReportDate value.
     */
    public void addReportDate(ReportDate newReportDate)
    {
        if (reportDates == null) 
        {
            reportDates = new ArrayList();
        }
        reportDates.add(newReportDate);
    }

    private String queryKey;

    /**
     * Get the QueryKey value.
     * @return the QueryKey value.
     */ 
    public String getQueryKey()
    {
        return queryKey == null ? "" : queryKey;
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
