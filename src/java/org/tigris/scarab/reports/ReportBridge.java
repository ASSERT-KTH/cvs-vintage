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

// JDK classes
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.StringReader;

import org.apache.log4j.Logger;

// Turbine classes
import org.apache.torque.TorqueException;

import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.AttributeOptionManager;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Scope;
import org.tigris.scarab.om.MITList;
import org.tigris.scarab.om.MITListItem;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.services.security.ScarabSecurity;

import org.apache.commons.betwixt.io.BeanReader;

/** 
 * This class is a bridge between the xml related classes for defining a
 * report and the business objects that are used within the screens
 * to configure the report. 
 */
public  class ReportBridge
    implements java.io.Serializable,
               org.apache.fulcrum.intake.Retrievable // do we want this?
{

    private ScarabUser generatedBy;
    private Date generatedDate;

    private org.tigris.scarab.om.Report torqueReport;
    private ReportDefinition reportDefn;
    private ReportHeading newHeading;

    public ReportBridge()
    {
        torqueReport = new org.tigris.scarab.om.Report();
        reportDefn = new ReportDefinition();
    }

    public ReportBridge(org.tigris.scarab.om.Report report)
        throws Exception
    {
        torqueReport = report;
        populate(report.getQueryString());
    }

    public ReportDefinition getReportDefinition()
    {
        return reportDefn;
    }

    public ReportHeading getNewHeading()
    {
        if (newHeading == null) 
        {
            newHeading = new ReportHeading();
        }
        return newHeading;
    }

    // I'm not sure I want this but for now we will implement the Retrievable
    // interface
    public String getQueryKey()
    {
        return torqueReport.getQueryKey();
    }
    public void setQueryKey(String key)
        throws TorqueException
    {
        torqueReport.setQueryKey(key);
    }

    public String getName()
    {
        return torqueReport.getName();
    }
    public void setName(String name)        
    {
        torqueReport.setName(name);
        reportDefn.setName(name);
    }
    public String getDescription()
    {
        return torqueReport.getDescription();
    }
    public void setDescription(String name)       
    {
        torqueReport.setDescription(name);
        reportDefn.setDescription(name);
    }

    public String getFormat()
    {
        return reportDefn.getFormat();
    }

    public void setFormat(String format)
    {
        reportDefn.setFormat(format);
    }

    public boolean getDeleted()
    {
        return torqueReport.getDeleted();
    }
    public void setDeleted(boolean b)
    {
        torqueReport.setDeleted(b);
    }
    public Integer getScopeId()
    {
        return torqueReport.getScopeId();
    }
    public void setScopeId(Integer id)
        throws TorqueException
    {
        torqueReport.setScopeId(id);
    }

    public Integer getUserId()
    {
        return torqueReport.getUserId();
    }
    public void setUserId(Integer id)
        throws TorqueException
    {
        torqueReport.setUserId(id);
    }

    public Integer getReportId()
    {
        return torqueReport.getReportId();
    }
    public void setReportId(Integer id)
    {
        torqueReport.setReportId(id);
    }

    public Scope getScope()
        throws TorqueException
    {
        return torqueReport.getScope();
    }

    /**
     * Get the value of module.
     * @return value of module.
     */
    public Module getModule() 
        throws TorqueException
    {
        Module module = null;
        if (torqueReport.getModuleId() != null) 
        {
            module = ModuleManager.getInstance(torqueReport.getModuleId());
        }
        
        return module;
    }
    
    /**
     * Set the value of module.
     * @param v  Value to assign to module.
     */
    public void setModule(Module  v) 
        throws TorqueException
    {
        reportDefn.setModuleIssueTypes(null);
        if (v == null) 
        {
            torqueReport.setModuleId((Integer)null);            
        }
        else 
        {
            torqueReport.setModuleId(v.getModuleId());
            if (torqueReport.getIssueTypeId() != null) 
            {
                ModuleIssueType mit = new ModuleIssueType();
                mit.setModuleId(v.getModuleId());
                mit.setIssueTypeId(torqueReport.getIssueTypeId());
                reportDefn.addModuleIssueType(mit);
            }
        }
    }

    /**
     * Set the value of module.
     * @param v  Value to assign to module.
     */
    public void setIssueType(IssueType  v) 
        throws TorqueException
    {
        reportDefn.setModuleIssueTypes(null);
        if (v == null) 
        {
            // issue type id cannot be null
            torqueReport.setIssueTypeId(ScarabConstants.INTEGER_0);
        }
        else 
        {
            torqueReport.setIssueTypeId(v.getIssueTypeId());
            if (torqueReport.getModuleId() != null) 
            {
                ModuleIssueType mit = new ModuleIssueType();
                mit.setModuleId(torqueReport.getModuleId());
                mit.setIssueTypeId(v.getIssueTypeId());
                reportDefn.addModuleIssueType(mit);
            }
        }
    }

    /**
     * Checks permission in all modules involved in report
     */
    private boolean hasPermission(String permission, ScarabUser user)
    {
        boolean result = false;
        try
        {
            MITList mitlist = getMITList();
            result = mitlist.isSingleModule() ? 
                user.hasPermission(permission, mitlist.getModule()) :
                user.hasPermission(permission, mitlist.getModules());
        }
        catch (Exception e)
        {
            result = false;
            Log.get().error(e);
        }
        return result;
    }

    public boolean isEditable(ScarabUser user)
    {
        return torqueReport.isNew()
            ||
            (Scope.PERSONAL__PK.equals(torqueReport.getScopeId()) 
             && user.getUserId().equals(torqueReport.getUserId()))
            ||
            (Scope.MODULE__PK.equals(torqueReport.getScopeId()) &&
             hasPermission(ScarabSecurity.MODULE__EDIT, user));
    }

    public boolean isDeletable(ScarabUser user)
    {
        return (Scope.PERSONAL__PK.equals(torqueReport.getScopeId()) 
                && user.getUserId().equals(torqueReport.getUserId()))
            ||
            (Scope.MODULE__PK.equals(torqueReport.getScopeId()) &&
             hasPermission(ScarabSecurity.ITEM__DELETE, user));
    }

    public boolean isSavable(ScarabUser user)
    {
        return isEditable(user) && 
            hasPermission( ScarabSecurity.USER__EDIT_PREFERENCES, user);
    }

    /**
     * Get the value of generatedBy.
     * @return value of generatedBy.
     */
    public ScarabUser getGeneratedBy() 
        throws Exception
    {
        if (generatedBy == null) 
        {
            if (torqueReport.getUserId() != null) 
            {
                generatedBy = 
                    ScarabUserManager.getInstance(torqueReport.getUserId());
            }
        }
        
        return generatedBy;
    }
    
    /**
     * Set the value of generatedBy.
     * @param v  Value to assign to generatedBy.
     */
    public void setGeneratedBy(ScarabUser  v)
        throws Exception
    {
        this.generatedBy = v;
        torqueReport.setUserId(v.getUserId());
    }
    
    
    /**
     * This is the date that was used in the queries for reports on a 
     * single date.  It is not necessarily the same as the date on which the
     * queries were run.
     * @return value of generatedDate.
     */
    public Date getGeneratedDate() 
    {
        if (generatedDate == null) 
        {
            // if no date was set just set this 
            // date to the current time
            generatedDate = getDefaultDate();
            if (generatedDate == null)
            {
                generatedDate = new Date();
            }
        }
        
        return generatedDate;
    }

    /**
     * Date used for a single date report.
     */
    public Date getDefaultDate() 
    {
        ReportDate rdate = reportDefn.getDefaultDate();
        return (rdate != null ? new Date(rdate.getTime()) : null);
    }

    /**
     * Date used for a single date report.
     */
    public void setDefaultDate(Date date)
    {
        if (date == null)
        {
            reportDefn.setDefaultDate(null);
        }
        else
        {
            ReportDate rdate = reportDefn.getDefaultDate();
            if (rdate == null) 
            {
                rdate = new ReportDate();
                reportDefn.setDefaultDate(rdate);
            }
            rdate.setTime(date.getTime());
            Log.get().debug("Default date set to " + date);
        }
    }


    public MITList getMITList()
        throws TorqueException
    {
        MITList mitList = null;
        List mits = reportDefn.getModuleIssueTypes();
        if (mits != null) 
        {
            Log.get().debug("mits were not null");
            mitList = new MITList();
            for (Iterator i = mits.iterator(); i.hasNext();) 
            {
                ModuleIssueType mit = (ModuleIssueType)i.next();
                MITListItem item = new MITListItem();
                item.setModuleId(mit.getModuleId());
                item.setIssueTypeId(mit.getIssueTypeId());
                mitList.addMITListItem(item);
            }
        }
        
        return mitList;
    }

    public void setMITList(MITList mitList)
        throws Exception
    {
        if (mitList == null) 
        {
            reportDefn.setModuleIssueTypes(null);
            setModule(null);
            setIssueType(null);
        }
        else 
        {
            boolean isOk = true;
            // need to check that the changes are compatible with the currently
            // selected criteria
            for (Iterator roai = reportDefn
                .retrieveAllReportOptionAttributes().iterator(); 
                 roai.hasNext() && isOk;) 
            {
                isOk = mitList.isCommon( AttributeOptionManager.getInstance( 
                    ((ReportOptionAttribute)roai.next())
                    .getOptionId()));
            }
            for (Iterator ruai = reportDefn
                .retrieveAllReportUserAttributes().iterator(); 
                 ruai.hasNext() && isOk;) 
            {
                isOk = mitList.isCommon( AttributeManager.getInstance( 
                    ((ReportUserAttribute)ruai.next())
                    .getAttributeId()));
            }
            
            if (!isOk) 
            {
                throw new IncompatibleMITListException(L10NKeySet.ExceptionIncompatibleMITListChanges); //EXCEPTION
            }
            
            reportDefn.setModuleIssueTypes(null);
            setModule(null);
            setIssueType(null);

            for (Iterator i = mitList.getExpandedMITListItems().iterator(); i.hasNext();) 
            {
                MITListItem item = (MITListItem)i.next();
                ModuleIssueType mit = new ModuleIssueType();
                mit.setModuleId(item.getModuleId());
                mit.setIssueTypeId(item.getIssueTypeId());
                reportDefn.addModuleIssueType(mit);
            }
            if (mitList.isSingleModule()) 
            {
                torqueReport.setModule(mitList.getModule());
            }
            if (mitList.isSingleIssueType()) 
            {
                torqueReport.setIssueType(mitList.getIssueType());
            }
        }
    }

    public boolean isReadyForCalculation()
    {
        List mits = reportDefn.getModuleIssueTypes();
        boolean result = mits != null && !mits.isEmpty();
        List axes = reportDefn.getReportAxisList();
        result &= axes != null && axes.size() > 1;
        if (result) 
        {
            for (Iterator i = axes.iterator(); i.hasNext() && result;) 
            {
                ReportAxis axis = (ReportAxis)i.next();
                List headings = axis.getReportHeadings();
                result &= headings != null && !headings.isEmpty();
                if (result) 
                {
                    for (Iterator j = headings.iterator(); j.hasNext() && result;) 
                    {
                        ReportHeading heading = (ReportHeading)j.next();
                        result &= heading.size() > 0;
                    }
                }
            }
        }
        
        return result;
    }

    public boolean removeStaleDefinitions()
        throws Exception
    {
        boolean reportModified = false;
        MITList mitList = getMITList();
        List axes = reportDefn.getReportAxisList();
        if (axes != null) 
        {
            for (Iterator i = axes.iterator(); i.hasNext();) 
            {
                ReportAxis axis = (ReportAxis)i.next();
                List headings = axis.getReportHeadings();
                if (headings != null) 
                {
                    for (Iterator j = headings.iterator(); j.hasNext();) 
                    {
                        ReportHeading heading = (ReportHeading)j.next();
                        reportModified |= removeStaleOptions(
                            heading.getReportOptionAttributes(), mitList);
                        reportModified |= removeStaleUserAttributes(heading
                            .getReportUserAttributes(), mitList);
                        List groups = heading.getReportGroups();
                        if (groups != null && !groups.isEmpty()) 
                        {
                            for (Iterator n = groups.iterator(); n.hasNext();) 
                            {
                                ReportGroup group = (ReportGroup)n.next();
                                reportModified |= removeStaleOptions(
                                    group.getReportOptionAttributes(), mitList );
                                reportModified |= removeStaleUserAttributes( 
                                    group.getReportUserAttributes(), mitList );
                            }
                        }
                    }
                }
            }
        }
        return reportModified;
    }

    private boolean removeStaleOptions(List options, MITList mitList)
        throws Exception
    {
        boolean anyRemoved = false;
        if (options != null && !options.isEmpty()) 
        {
            for (Iterator n = options.iterator(); n.hasNext();)
            {
                ReportOptionAttribute rao = 
                    (ReportOptionAttribute)n.next();
                AttributeOption ao = AttributeOptionManager
                    .getInstance(rao.getOptionId());
                if (!mitList.isCommon(ao, false)) 
                {
                    n.remove();
                    anyRemoved = true;
                }
                else if (!mitList.isCommon(ao.getAttribute(), false)) 
                {
                    n.remove();
                    anyRemoved = true;
                }
            }
        }
        return anyRemoved;
    }    

    private boolean removeStaleUserAttributes(List attributes, MITList mitList)
        throws Exception
    {
        boolean anyRemoved = false;
        if (attributes != null && !attributes.isEmpty()) 
        {
            for (Iterator n = attributes.iterator(); n.hasNext();)
            {
                ReportUserAttribute rua = 
                    (ReportUserAttribute)n.next();
                Attribute attr = AttributeManager
                    .getInstance(rua.getAttributeId());
                if (!mitList.isCommon(attr, false)) 
                {
                    n.remove();
                    anyRemoved = true;
                }
            }
        }
        return anyRemoved;
    }    

    public ReportTableModel getModel(ScarabUser searcher)
        throws Exception
    {
        return new ReportTableModel(this, getGeneratedDate(), searcher);
    }
        
    public void save() 
        throws Exception
    {
        torqueReport.setQueryString(getQueryString());
        torqueReport.save();
    }
    
    /**
     * State of persistence of the report.
     *
     * @return true if the report has NOT been saved.
     */
    public boolean isNew()
    {
        return torqueReport.isNew();
    }

    public void populate(String v)
        throws Exception
    {
        if (v == null)
        {
            reportDefn = new ReportDefinition();
        }
        else
        {
            BeanReader reader = new BeanReader();
            reader.registerBeanClass(ReportDefinition.class);
            reportDefn = (ReportDefinition) 
                reader.parse(new StringReader(v));

            Logger log = Log.get();
            if (log.isDebugEnabled())
            {
                log.debug("Created a new report using:\n " + v + 
                          "; and it resulted in:\n " + 
                          reportDefn.toXmlString());
            }
        }
    }

    String getQueryString()
        throws Exception
    {
        return reportDefn.toXmlString();
    }


    public void populateSearch(IssueSearch search, ReportHeading heading)
        throws Exception
    {
        List reportOptions = heading.getReportOptionAttributes();
        if (reportOptions == null || reportOptions.isEmpty()) 
        {
            List groups = heading.getReportGroups();
            if (groups != null && !groups.isEmpty()) 
            {
                reportOptions = new ArrayList();
                for (Iterator i = groups.iterator(); i.hasNext();) 
                {
                    ReportGroup group = (ReportGroup)i.next();
                    List tmpOptions = group.getReportOptionAttributes();
                    if (tmpOptions != null && !tmpOptions.isEmpty()) 
                    {
                        for (Iterator j = tmpOptions.iterator(); j.hasNext();) 
                        {
                            reportOptions.add(j.next());
                        }
                    }
                }
            }
        }

        if (reportOptions != null && !reportOptions.isEmpty()) 
        {
            Map commonAttributeMap = new HashMap(reportOptions.size());
            for (Iterator i = reportOptions.iterator(); i.hasNext();) 
            {
                ReportOptionAttribute roa = (ReportOptionAttribute)i.next();
                Integer optionId = roa.getOptionId();
                Integer attId = AttributeOptionManager.getInstance(optionId)
                    .getAttributeId();
                AttributeValue av = AttributeValue
                    .getNewInstance(attId, search);
                av.setOptionId(optionId);
                if (commonAttributeMap.containsKey(attId)) 
                {
                    AttributeValue prevAV = 
                        (AttributeValue)commonAttributeMap.get(attId);
                    prevAV.setChainedValue(av);
                }
                else 
                {
                    search.addAttributeValue(av);
                    commonAttributeMap.put(attId, av);
                }
            }
        }
    }
}
