package org.tigris.scarab.reports;

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
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import java.io.StringWriter;
import org.apache.commons.betwixt.io.BeanWriter;
import org.tigris.scarab.util.Log;
import org.apache.torque.om.NumberKey;

import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleAttributeManager;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.RModuleOptionManager;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.util.ScarabConstants;

/**
 * This class is the container for the information used to generate a
 * report.  It is the outermost tag of the XML representation defined
 * by <a
 * href="http://scarab.tigris.org/source/browse/scarab/src/dtd/report.dtd?rev=1&content-type=text/x-cvsweb-markup">report.dtd</a>
 * (please see this file for an example).
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ReportDefinition.java,v 1.12 2003/09/04 00:51:16 jmcnally Exp $
 * @see <a href="http://scarab.tigris.org/source/browse/scarab/src/dtd/report.dtd?rev=1&content-type=text/x-cvsweb-markup">report.dtd</a>
 */
public class ReportDefinition
    implements java.io.Serializable
               //Retrievable
{
    /** 
     * A report can be expensive, so limit the criteria (which translates
     * to headings) to a number that mysql can handle safely
     */
    private static final int MAX_CRITERIA = ScarabConstants.REPORT_MAX_CRITERIA;

    private String name;

    private String description;

    private String format;

    private List moduleIssueTypes;

    private List reportAxisList;

    private ReportDate defaultDate;

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

    /**
     * Get the Description value.
     * @return the Description value.
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Set the Description value.
     * @param newDescription The new Description value.
     */
    public void setDescription(String newDescription)
    {
        this.description = newDescription;
    }

    /**
     * Get the format value.
     * @return The format value.
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * Set the format value.
     * @param format The new format value.
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * Get the ModuleIssueTypes value.
     * @return the ModuleIssueTypes value.
     */
    public List getModuleIssueTypes()
    {
        return moduleIssueTypes;
    }

    /**
     * Set the ModuleIssueTypes value.
     * @param newModuleIssueTypes The new ModuleIssueTypes value.
     */
    public void setModuleIssueTypes(List newModuleIssueTypes)
    {
        this.moduleIssueTypes = newModuleIssueTypes;
    }

    /**
     * Add a ModuleIssueTypes value.
     * @param newModuleIssueType The new ModuleIssueTypes value.
     */
    public void addModuleIssueType(ModuleIssueType newModuleIssueType)
    {
        Log.get().debug("Added a mit, " + newModuleIssueType + 
                           ", to reportDefn");
        if (moduleIssueTypes == null) 
        {
            moduleIssueTypes = new ArrayList();
        }
        if (!moduleIssueTypes.contains(newModuleIssueType))
        {
            moduleIssueTypes.add(newModuleIssueType);
        }
    }

    /**
     * Get the ReportAxisList value.
     * @return the ReportAxisList value.
     */
    public List getReportAxisList()
    {
        return reportAxisList;
    }

    /**
     * Set the ReportAxisList value.
     * @param newReportAxisList The new ReportAxisList value.
     */
    public void setReportAxisList(List newReportAxisList)
    {
        this.reportAxisList = newReportAxisList;
    }

    /**
     * Add a ReportAxis value.
     * @param newReportAxis The new ReportAxis value.
     */
    public void addReportAxis(ReportAxis newReportAxis)
    {
        if (reportAxisList == null) 
        {
            reportAxisList = new ArrayList();
        }
        reportAxisList.add(newReportAxis);
    }

    /**
     * Get the ReportDate value used if no axis is time.
     * @return the ReportDate value.
     */
    public ReportDate getDefaultDate()
    {
        return defaultDate;
    }

    /**
     * Set the ReportDate value used if no axis is time.
     * @param newDefaultDate The new ReportDate value.
     */
    public void setDefaultDate(ReportDate newDefaultDate)
    {
        this.defaultDate = newDefaultDate;
    }

    

    // private String queryKey;

    /* *
     * Get the QueryKey value.
     * @return the QueryKey value.
     * / 
    public String getQueryKey()
    {
        if (queryKey == null) 
        {
            return "";
        }
        return queryKey;
    }
    
    /* *
     * Set the QueryKey value.
     * @param newQueryKey The new QueryKey value.
     * /
    public void setQueryKey(String newQueryKey)
    {
        this.queryKey = newQueryKey;
    }
    */

    /**
     * Gets the specified axis, if it was null prior to this method request 
     * a new ReportAxis is returned for the given index
     *
     * @param axisIndex an <code>int</code> value
     * @return a <code>ReportHeading</code> value
     */
    public ReportAxis getAxis(int axisIndex)
    {
        List axisList = getReportAxisList();
        while (axisList == null || axisList.size() < axisIndex + 1) 
        {
            addReportAxis(new ReportAxis());
            axisList = getReportAxisList();
        }

        return (ReportAxis)axisList.get(axisIndex);
    }

    public List retrieveAllReportOptionAttributes()
    {
        List result = null;
        List axes = getReportAxisList();
        if (axes != null && !axes.isEmpty()) 
        {
            for (Iterator axi = axes.iterator(); axi.hasNext();) 
            {
                List headings = ((ReportAxis)axi.next()).getReportHeadings();
                if (headings != null && !headings.isEmpty()) 
                {
                    for (Iterator hi = headings.iterator(); hi.hasNext();) 
                    {
                        List options = ((ReportHeading)hi.next())
                            .consolidateReportOptionAttributes();
                        if (options != null && !options.isEmpty()) 
                        {
                            for (Iterator i = options.iterator(); i.hasNext();)
                            {
                                if (result == null) 
                                {
                                    result = new ArrayList();
                                }
                                result.add(i.next());
                            }
                        }
                    }
                }
            }
        }
        return result == null ? Collections.EMPTY_LIST : result;
    }

    public List retrieveAllReportUserAttributes()
    {
        List result = null;
        List axes = getReportAxisList();
        if (axes != null && !axes.isEmpty()) 
        {
            for (Iterator axi = axes.iterator(); axi.hasNext();) 
            {
                List headings = ((ReportAxis)axi.next()).getReportHeadings();
                if (headings != null && !headings.isEmpty()) 
                {
                    for (Iterator hi = headings.iterator(); hi.hasNext();) 
                    {
                        List users = ((ReportHeading)hi.next())
                            .consolidateReportUserAttributes();
                        if (users != null && !users.isEmpty()) 
                        {
                            for (Iterator i = users.iterator(); i.hasNext();)
                            {
                                if (result == null) 
                                {
                                    result = new ArrayList();
                                }
                                result.add(i.next());
                            }
                        }
                    }
                }
            }
        }
        return result == null ? Collections.EMPTY_LIST : result;
    }

    public String toXmlString()
    {
        String s;
        try 
        {
            StringWriter sw = new StringWriter(1024);
            BeanWriter bw = new BeanWriter(sw);
                /*
                {
                    {
                        writeIDs = false;
                    }
                };
                */
            bw.enablePrettyPrint();
            bw.writeXmlDeclaration("<?xml version='1.0' encoding='UTF-8' ?>");
            bw.write(this);
            bw.flush();
            s = sw.toString();
            bw.close();
        }
        catch (Exception e)
        {
            s = "ERROR! on " + super.toString();
            Log.get().error("", e);
        }

        return s;
    }

    public String displayAttribute(Object obj)
    //    throws TorqueException
    {
        Integer attId = null;
        if (obj instanceof ReportOptionAttribute) 
        {
            try 
            {
                attId = new Integer(AttributeOptionManager.getInstance(
                    new NumberKey(((ReportOptionAttribute)obj).getOptionId()
                    .toString())).getAttributeId().toString());
            }
            catch (Exception e)
            {
                Log.get().error("Error on Attribute Id=" + attId, e);
                return "Error on Attribute Id=" + attId;
            }
        }
        else if (obj instanceof ReportUserAttribute)
        {
            attId = ((ReportUserAttribute)obj).getAttributeId();
        }
        else 
        {
            return "";
        }
        
    
        String result = null;
        List mits = getModuleIssueTypes();
        if (mits != null && mits.size() == 1) 
        {
            ModuleIssueType mit = (ModuleIssueType)mits.get(0);
            try 
            {
                RModuleAttribute rma = RModuleAttributeManager.getInstance(
                    mit.getModuleId(), attId, mit.getIssueTypeId());
                result = rma.getDisplayValue();
            }
            catch (Exception e)
            {
                result = "Error on Attribute Id=" + attId;
                Log.get().error(result, e);
            }
        }
        else 
        {
            try 
            {
                result = AttributeManager.getInstance(
                    new NumberKey(attId.toString())).getName();
            }
            catch (Exception e)
            {
                result = "Error on Attribute Id=" + attId;
                Log.get().error(result, e);
            }
        }
        return result;
    }

    public String displayOption(ReportOptionAttribute roa)
    //    throws TorqueException
    {
        Integer optionId = roa.getOptionId();    
        String result = null;
        List mits = getModuleIssueTypes();
        if (mits != null && mits.size() == 1) 
        {
            ModuleIssueType mit = (ModuleIssueType)mits.get(0);
            try 
            {
                RModuleOption rmo = RModuleOptionManager.getInstance(
                    mit.getModuleId(), mit.getIssueTypeId(), optionId);
                result = rmo.getDisplayValue();
            }
            catch (Exception e)
            {
                result = "Error on Option Id=" + optionId;
                Log.get().error(result, e);
            }
        }
        else 
        {
            try 
            {
                result = AttributeOptionManager.getInstance(
                    new NumberKey(optionId.toString())).getName();
            }
            catch (Exception e)
            {
                result = "Error on Option Id=" + optionId;
                Log.get().error(result, e);
            }
        }
        return result;
    }

    public String displayUser(ReportUserAttribute rua)
    //        throws TorqueException
    {
        String result = null;
        try 
        {
            result = ScarabUserManager.getInstance(
                new NumberKey(rua.getUserId().toString())).getName();
        }
        catch (Exception e)
        {
            result = "Error on Option Id=" + rua.getUserId();
            Log.get().error(result, e);
        }
        return result;
    }

    public String displayHeading(ReportHeading heading)
    {
        String summary = null;
        List options = heading.getReportOptionAttributes();
        List groups = heading.getReportGroups();
        List users = heading.getReportUserAttributes();
        if (options != null && !options.isEmpty()) 
        {
            String attribute = null;
            StringBuffer sb = new StringBuffer(20*options.size());
            for (Iterator i = options.iterator(); i.hasNext();) 
            {
                ReportOptionAttribute roa = (ReportOptionAttribute)i.next();
                String newAttribute = displayAttribute(roa);
                if (!newAttribute.equals(attribute))
                {
                    if (attribute != null) 
                    {
                        sb.append("; ");
                    }
                    attribute = newAttribute;
                    sb.append(attribute).append(": ");
                }
                sb.append(displayOption(roa)).append(", ");
            }
            sb.setLength(sb.length() - 2);
            summary = sb.toString();
        }
        else if (groups != null && !groups.isEmpty())
        {
            StringBuffer sb = 
                new StringBuffer(10*groups.size());
            for (Iterator i = groups.iterator(); i.hasNext();) 
            {
                sb.append(((ReportGroup)i.next()).getName())
                    .append('/');
            }
            sb.setLength(sb.length() - 1);
                summary = sb.toString();
        }
        else if (users != null && !users.isEmpty()) 
        {
            String attribute = null;
            StringBuffer sb = new StringBuffer(20*users.size());
            for (Iterator i = users.iterator(); i.hasNext();) 
            {
                ReportUserAttribute rua = (ReportUserAttribute)i.next();
                String newAttribute = displayAttribute(rua);
                if (!newAttribute.equals(attribute))
                {
                    if (attribute != null) 
                    {
                        sb.append("; ");
                    }
                    attribute = newAttribute;
                    sb.append(attribute).append(": ");
                }
                sb.append(displayUser(rua)).append(", ");
                }
            sb.setLength(sb.length() - 2);
            summary = sb.toString();
        }
        // FIXME: Date ranges are not implemented yet.
        else if (heading.getReportDates() != null 
                 && !heading.getReportDates().isEmpty())
            //|| heading.getReportDateRanges() != null) 
        {
            summary = "Dates";
        }
        return summary;
    }

    public int maximumHeadings()
    {
        return MAX_CRITERIA;
    }

    public boolean allowMoreHeadings(ReportAxis axis)
    {
        return availableNumberOfHeadings(axis) > 0;
    }

    public boolean reportQueryIsExpensive()
    {
        return totalNumberOfNonDateHeadings() > MAX_CRITERIA;
    }

    public int totalAvailableNumberOfHeadings()
    {
        return maximumHeadings() - totalNumberOfNonDateHeadings();
    }

    public int availableNumberOfHeadings(ReportAxis axis)
    {
        // the following assumes two axes
        int result = maximumHeadings() - totalNumberOfNonDateHeadings() - 1;
        List axes = getReportAxisList();
        if (axes != null) 
        {
            ReportAxis tmpAxis;
            for (Iterator i = axes.iterator(); i.hasNext();) 
            {
                tmpAxis = (ReportAxis)i.next();
                if (tmpAxis != null && !tmpAxis.equals(axis)) 
                {
                    List headings = tmpAxis.getReportHeadings();
                    if (headings != null && headings.size() > 0 &&  
                        (((ReportHeading)headings.get(0)).size() > 0)) 
                    {
                        result++;
                    }
                    break;
                }
            }
        }
        return result;
    }

    private int totalNumberOfNonDateHeadings()
    {
        int count = 0;
        List axes = getReportAxisList();
        if (axes != null) 
        {
            ReportAxis axis;
            for (Iterator i = axes.iterator(); i.hasNext();) 
            {
                axis = (ReportAxis)i.next();
                if (axis != null) 
                {
                    count += numberOfNonDateHeadings(axis);
                }
            }
        }
        return count;
    }

    private int numberOfNonDateHeadings(ReportAxis axis)
    { 
        int count = 0;
        List headings = axis.getReportHeadings();
        if (headings != null) 
        {
            int size = headings.size();
            if (size > 0)
            {
                if ( size != 1 || 
                     !(((ReportHeading)headings.get(0))
                       .get(0) instanceof ReportDate)) 
                {
                    count += size;                                
                }
            }                        
        }
        return count;
    }
}

