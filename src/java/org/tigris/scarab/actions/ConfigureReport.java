package org.tigris.scarab.actions;

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

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.fulcrum.util.parser.ValueParser;

import org.apache.torque.om.NumberKey;
import org.apache.fulcrum.intake.Intake;
import org.apache.fulcrum.intake.model.Group;
import org.apache.commons.collections.SequencedHashMap;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.reports.ReportBridge;
import org.tigris.scarab.om.Report;
import org.tigris.scarab.om.ReportPeer;
import org.tigris.scarab.om.ReportManager;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.reports.ReportDefinition;
import org.tigris.scarab.reports.ReportAxis;
import org.tigris.scarab.reports.ReportHeading;
import org.tigris.scarab.reports.ReportOptionAttribute;
import org.tigris.scarab.reports.ReportUserAttribute;
import org.tigris.scarab.reports.ReportGroup;
import org.tigris.scarab.reports.ReportDate;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.export.ExportFormat;

/**
 * This class is responsible for report generation forms
 * @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
 * @version $Id: ConfigureReport.java,v 1.17 2003/05/06 17:11:25 elicia Exp $
 */
public class ConfigureReport 
    extends RequireLoginFirstAction
{
    static final String NO_PERMISSION_MESSAGE = 
        "NoPermissionToEditReport";

    private static final String ADD_USER = "add_user";
    private static final String SELECTED_USER = "select_user";

    public void doSaveinfo(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        Intake intake = getIntakeTool(context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if (intake.isAllValid()) 
        {
            Group intakeReport = 
                intake.get("Report", report.getQueryKey(), false);
            if (intakeReport == null) 
            {   
                intakeReport = intake.get("Report", "", false);
            }  
            
            if (intakeReport != null) 
            {   
                intakeReport.setValidProperties(report);

                if (report.isReadyForCalculation()) 
                {
                    scarabR.setConfirmMessage(l10n.get(
                        report.isNew() ? 
                        "ReportUpdated" : "ReportUpdatedNotSaved")); 
                    setTarget(data, "reports,Info.vm");     
                }
                else 
                {
                    scarabR.setConfirmMessage(l10n.get(
                        report.isNew() ? 
                        "ReportUpdatedPleaseAddRowAndColumnCriteria" :
                        "ReportUpdatedNotSavedPleaseAddRowAndColumnCriteria"));
                    setTarget(data, "reports,AxisConfiguration.vm");
                }
            }
            else 
            {
                // FIXME! i don't know that the intakeReport should ever be 
                // null, but since the conditional was here, don't fail silently
                scarabR.setAlertMessage(
                    l10n.get("ThisShouldNotHappenPleaseContactAdmin"));
                setTarget(data, "reports,Info.vm");
            }            
        }
        else 
        {
            getScarabRequestTool(context).setAlertMessage(
                l10n.get("InvalidData"));
            setTarget(data, "reports,Info.vm");            
        }
    }

    public void doSelectheading(RunData data, TemplateContext context)
        throws Exception
    {
        // the form will carry over the selected heading. just make sure
        // to remove old intake data
        Intake intake = getIntakeTool(context);
        intake.removeAll();
        // give the user a message if they are already on the selected
        // heading, in the event they are confused.
        ValueParser params = data.getParameters();
        int level = params.getInt("heading", -1);
        int prevLevel = params.getInt("prevheading", -2);
        if (level == prevLevel) 
        {
            getScarabRequestTool(context).setInfoMessage(
                getLocalizationTool(context)
                .format("AlreadyEditingSelectedHeading", new Integer(level+1)));
        }        
    }

    public void doSettype(RunData data, TemplateContext context)
        throws Exception
    {
        ValueParser params = data.getParameters();
        int axis = params.getInt("axis", 0); // 0=row; 1=column
        int level = params.getInt("heading", -1);
        int type = params.getInt("headingtype", 0);

        // remove any old data
        if (level != -1) 
        {
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            ReportBridge report = scarabR.getReport();
            ReportHeading heading = report.getReportDefinition()
                .getAxis(axis).getHeading(level);
            if (type != heading.calculateType()) 
            {
                ScarabLocalizationTool l10n = getLocalizationTool(context);
                if (heading.size() > 0) 
                {
                    heading.reset();
                    scarabR.setConfirmMessage(l10n.get("HeadingTypeChangedOldDataDiscarded"));
                }
                else
                {
                    scarabR.setConfirmMessage(l10n.get("HeadingTypeChanged"));
                }
            }
        }
    }

    public void doAddoptions(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        Intake intake = getIntakeTool(context);
        if (!report.isEditable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if (intake.isAllValid()) 
        {
            ScarabLocalizationTool l10n = getLocalizationTool(context);

            ValueParser params = data.getParameters();
            int axis = params.getInt("axis", 0); // 0=row; 1=column
            int level = params.getInt("heading", -1);
            int type = params.getInt("headingtype", 0);

            // we are using an IssueSearch object to gather the data to
            // create the ReportOptionAttribute objects.

            IssueSearch search = scarabR.getNewSearch();
            // Set intake properties
            //Group searchGroup = intake.get("SearchIssue", search.getQueryKey());
            //searchGroup.setProperties(search);

            // Set attribute values to search on
            SequencedHashMap avMap = search.getCommonAttributeValuesMap();
            for (Iterator i = avMap.iterator(); i.hasNext();) 
            {
                AttributeValue aval = (AttributeValue)avMap.get(i.next());
                Group group = intake.get("AttributeValue", aval.getQueryKey());
                if (group != null) 
                {
                    group.setProperties(aval);
                }                
            }
            
            // remove unset AttributeValues
            List setAttValues = removeUnsetValues(search.getAttributeValues());
            
            ReportHeading heading = report.getReportDefinition()
                .getAxis(axis).getHeading(level);
            if (type != heading.calculateType()) 
            {
                scarabR.setAlertMessage(l10n.get("ChangeOfTypeMessage")); 
            }

            // we are going to delete the old heading data and reconstruct it
            // so if there is any group info, we need to get it first
            List groups = heading.getReportGroups();
            Map optionGroupMap = null;
            if (groups != null && !groups.isEmpty()) 
            {
                optionGroupMap = new HashMap();
                for (Iterator i = groups.iterator(); i.hasNext();) 
                {
                    ReportGroup group = (ReportGroup)i.next();
                    List options = group.getReportOptionAttributes();
                    if (options != null && !options.isEmpty()) 
                    {
                        for (Iterator j = options.iterator(); j.hasNext();) 
                        {
                            optionGroupMap.put(j.next(), group);
                        }
                    }
                }
            }
            heading.reset();
            
            //convert to ReportOptionAttributes
            for (Iterator i = setAttValues.iterator(); i.hasNext();) 
            {
                AttributeValue av = (AttributeValue)i.next();
                //pull any chained values out to create a flat list
                List chainedValues = av.getValueList();
                for (Iterator j = chainedValues.iterator(); j.hasNext();) 
                {
                    ReportOptionAttribute roa = new ReportOptionAttribute();
                    Integer id = new Integer(((AttributeValue)j.next())
                                             .getOptionId().toString());
                    roa.setOptionId(id);

                    if (optionGroupMap == null) 
                    {
                        heading.addReportOptionAttribute(roa);
                    }
                    else 
                    {
                        ReportGroup group = 
                            (ReportGroup)optionGroupMap.get(roa);
                        if (group == null)
                        {
                            // add it to the first group
                            ((ReportGroup)groups.get(0))
                                .addReportOptionAttribute(roa);
                        }
                        else
                        {
                            group.addReportOptionAttribute(roa);
                        }
                    }
                }
            }
            if (level == -1) 
            {
                params.setString("heading", "0");
            }

            scarabR.setConfirmMessage(
                l10n.get(getHeadingConfirmMessageKey(report)));

/*
            //testing
            java.io.FileWriter fw = new java.io.FileWriter("/tmp/Report.xml");
            BeanWriter bw = new BeanWriter(fw);
            bw.writeXmlDeclaration("<?xml version='1.0' encoding='UTF-8' ?>");
            bw.write(report.getReportDefinition());
            bw.flush();
            bw.close();
*/
        }
    }


    private static String getHeadingConfirmMessageKey(ReportBridge report)
    {
        String key = null;
        if (report.isReadyForCalculation()) 
        {
            key = report.isNew() ? 
                "ReportUpdatedDoMoreOrCalculate" :
                "ReportUpdatedNotSavedDoMoreOrCalculate";
        }
        else 
        {
            key = report.isNew() ? 
                "ReportUpdatedDoMore" : "ReportUpdatedNotSavedDoMore";
        }
        return key;
    }

    /**
     * remove unset AttributeValues. this method is c/p from IssueSearch
     *
     * @param attValues a <code>List</code> value
     */
    private List removeUnsetValues(List attValues)
    {
        int size = attValues.size();
        List setAVs = new ArrayList(size);
        for (int i=0; i<size; i++) 
        {
            AttributeValue attVal = (AttributeValue) attValues.get(i);
            if (attVal.getOptionId() != null || attVal.getValue() != null
                 || attVal.getUserId() != null) 
            {
                setAVs.add(attVal);
            }
        }
        return setAVs;
    }
        
    /**
     * Adds users to the current header.
     */
    public void doAddusers(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }

        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        int axis = params.getInt("axis", 0); // 0=row; 1=column
        int level = params.getInt("heading", -1);
        int type = params.getInt("headingtype", 0);
        
        // not too elegant for editing, but if groups have been defined
        // we remove them for now until the rest of the reports is 
        // more code complete !FIXME!
        ReportHeading heading = report.getReportDefinition()
            .getAxis(axis).getHeading(level);
        if (type != heading.calculateType() 
            || heading.getReportGroups() != null) 
        {
            if (heading.getReportGroups() != null) 
            {
               scarabR.setAlertMessage(l10n.get("CouldNotMakeRequestedChange"));                
            }
            heading.reset();
        }

        String[] userIds = params.getStrings(ADD_USER);
        if (userIds != null && userIds.length > 0) 
        {
            for (int i =0; i<userIds.length; i++)
            {
                String userId = userIds[i];
                ReportUserAttribute rua = new ReportUserAttribute();
                rua.setUserId(new Integer(userId));
                rua.setAttributeId(new Integer(
                    params.getString("user_attr_" + userId)));
                heading.addReportUserAttribute(rua);
            } 
            if (level == -1) 
            {
                params.setString("heading", "0");
            }

            scarabR.setConfirmMessage(
                l10n.get(getHeadingConfirmMessageKey(report)));
        }
        else 
        {
            scarabR.setAlertMessage(l10n.get("NoUsersSelected"));
        }
    }
        
    /**
     * Removes users from temporary working list.
     */
    public void doRemoveusers(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }

        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        int axis = params.getInt("axis", 0); // 0=row; 1=column
        int level = params.getInt("heading", -1);
        int type = params.getInt("headingtype", 0);
        
        // not too elegant for editing, but if groups have been defined
        // we remove them for now until the rest of the reports is 
        // more code complete !FIXME!
        ReportHeading heading = report.getReportDefinition()
            .getAxis(axis).getHeading(level);
        if (type != heading.calculateType() 
            || heading.getReportGroups() != null) 
        {
            heading.reset();
            scarabR.setAlertMessage(l10n.get("CouldNotMakeRequestedChange"));                
        }
        else 
        {
            String[] userIds =  params.getStrings(SELECTED_USER);
            if (userIds != null && userIds.length > 0) 
            {
                for (int i =0; i<userIds.length; i++)
                {
                    String userId = userIds[i];
                    ReportUserAttribute rua = new ReportUserAttribute();
                    rua.setUserId(new Integer(userId));
                    rua.setAttributeId(new Integer(
                        params.getString("old_attr_" + userId)));
                    
                    List ruas =  heading.getReportUserAttributes();
                    if (ruas != null) 
                    {
                        ruas.remove(rua);
                    }
                }
                scarabR.setConfirmMessage(l10n.get("SelectedUsersWereRemoved"));
            }
            else 
            {
                scarabR.setAlertMessage(l10n.get("NoUsersSelected"));
            }
        }
    }

    /**
     * Changes the user attribute a user is associated with.
     */
    public void doUpdateusers(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }

        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        int axis = params.getInt("axis", 0); // 0=row; 1=column
        int level = params.getInt("heading", -1);
        int type = params.getInt("headingtype", 0);
        
        // not too elegant for editing, but if groups have been defined
        // we remove them for now until the rest of the reports is 
        // more code complete !FIXME!
        ReportHeading heading = report.getReportDefinition()
            .getAxis(axis).getHeading(level);
        if (type != heading.calculateType() 
            || heading.getReportGroups() != null) 
        {
            heading.reset();
            scarabR.setAlertMessage(l10n.get("CouldNotMakeRequestedChange"));
        }
        else 
        {
            String[] userIds =  params.getStrings(SELECTED_USER);
            if (userIds != null && userIds.length > 0) 
            {
                for (int i =0; i<userIds.length; i++)
                {
                    String userId = userIds[i];
                    ReportUserAttribute rua = new ReportUserAttribute();
                    rua.setUserId(new Integer(userId));
                    rua.setAttributeId(new Integer(
                        params.getString("old_attr_" + userId)));
                    
                    List ruas =  heading.getReportUserAttributes();
                    if (ruas != null) 
                    {
                        /* this will make it hard to find dupes
                        for (Iterator i=ruas.iterator(); i.hasNext();) 
                        {
                            Object obj = i.next();
                            if (obj.equals(rua)) 
                            {
                                ((ReportUserAttribute)obj).setAttributeId();
                            }
                        }
                        */
                        ruas.remove(rua);
                    }
                    
                    rua = new ReportUserAttribute();
                    rua.setUserId(new Integer(userId));
                    rua.setAttributeId(new Integer(
                        params.getString("asso_user_{" + userId + "}")));
                    heading.addReportUserAttribute(rua);
                }
                scarabR.setConfirmMessage(l10n.get("SelectedUsersWereModified"));
            }
            else 
            {
                scarabR.setAlertMessage(l10n.get("NoUsersSelected"));
            }
        }
    }

    /**
     * Changes the user attribute a user is associated with.
     */
    public void doRemoveheading(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }

        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        int axis = params.getInt("axis", 0); // 0=row; 1=column
        int level = params.getInt("heading", -1);
        //int type = params.getInt("headingtype", 0);
        
        if (level >= 0) 
        {
            List headings = report.getReportDefinition()
                .getAxis(axis).getReportHeadings();
            headings.remove(level);
            scarabR.setConfirmMessage(l10n.get("HeadingRemoved")); 
        }
        else 
        {
            scarabR.setAlertMessage(l10n.get("NoHeadingSelected"));     
        }
    }

    /**
     * Redirects to screen to group the options/users in the selected
     * heading.
     */
    public void doGotoeditgroups(RunData data, TemplateContext context) 
        throws Exception
    {
        ValueParser params = data.getParameters();
        int level = params.getInt("heading", -1);

        if (level >= 0) 
        {
            setTarget(data, "reports,EditGroups.vm");
        }
        else 
        {
            getScarabRequestTool(context).setAlertMessage(
                getLocalizationTool(context).get("NoHeadingSelected"));
        }
    }

    /**
     * 
     */
    public void doAddheading(RunData data, TemplateContext context) 
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }

        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        int axisIndex = params.getInt("axis", 0); // 0=row; 1=column
        ReportAxis axis = report.getReportDefinition().getAxis(axisIndex);
        axis.addReportHeading(new ReportHeading());
        params.setString("heading", String.valueOf(axis.getReportHeadings().size()-1));
        // remove old intake data
        Intake intake = getIntakeTool(context);
        intake.removeAll();
        scarabR.setConfirmMessage(getLocalizationTool(context)
            .get("HeadingAddedNowAddContent"));

    }        

    public void doAddgroup(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }

        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        int axis = params.getInt("axis", 0); // 0=row; 1=column
        int level = params.getInt("heading", -1);
                    
        String name = params.getString("groupname_new");
        if (name == null || name.length() == 0) 
        {
            scarabR.setAlertMessage(l10n.get("InvalidGroupName"));
        }
        else
        {
            ReportHeading heading = report.getReportDefinition()
                .getAxis(axis).getHeading(level);
            ReportGroup group = new ReportGroup();
            group.setName(name);
            // make sure we are not adding a new group with a non-unique name
            List groups = heading.getReportGroups();
            if (groups != null && groups.contains(group)) 
            {
                scarabR.setAlertMessage(l10n.get("DuplicateGroupName"));
            }
            else
            {
                heading.addReportGroup(group);
                int index = heading.getReportGroups().size() - 1;
                params.remove("groupname_new");
                //params.setString("groupname_" + index, group.getName());
                scarabR.setConfirmMessage(l10n.get("GroupAdded"));
            }

            /* intake way
            UIReportGroup group = new UIReportGroup();
            Group intakeGroup = intake.get("UIReportGroup", 
                                           group.getQueryKey(), false);
            if (intakeGroup != null) 
            {
                intakeGroup.setProperties(group);
                if (group.getDisplayValue() != null 
                     && group.getDisplayValue().length() > 0) 
                {
                    ReportHeading heading = report.getReportDefinition()
                        .getAxis(axis).getHeading(level);
                    ReportGroup rgroup = new ReportGroup();
                    rgroup.setName(group.getDisplayValue());
                    heading.addReportGroup(rgroup);
                }
            }
            */
        }
    }

    public void doDeletegroup(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }
        
        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        String[] groupIndices = params.getStrings("selectgroup");
        if (groupIndices == null || groupIndices.length == 0) 
        {
            scarabR.setAlertMessage(l10n.get("NoGroupSelected"));
        }
        else
        {
            int axis = params.getInt("axis", 0); // 0=row; 1=column
            int level = params.getInt("heading", -1);
            List reportGroups = report.getReportDefinition()
                .getAxis(axis).getHeading(level).getReportGroups();

            for (int j = groupIndices.length-1; j>=0; j--) 
            {
                reportGroups.remove(Integer.parseInt(groupIndices[j]));
            }
            scarabR.setConfirmMessage(l10n.get("SelectedGroupDeleted"));
                        
            /* intake way
            for (int i=groups.size()-1; i>=0; i--) 
            {
                ReportGroup rgroup = (ReportGroup)groups.get(i);
                UIReportGroup uirg = new UIReportGroup(rgroup.getName());
                Group intakeGroup = intake.get("UIReportGroup", 
                                               uirg.getQueryKey(), false);
                if (intakeGroup != null) 
                {
                    intakeGroup.setProperties(uirg);
                    if (uirg.isSelected())
                    {
                        groups.remove(i);
                    }
                }
            }
            */
        }
    }

    public void doEditgroupname(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }
        
        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        Object[] keys =  params.getKeys();
        int axis = params.getInt("axis", 0); // 0=row; 1=column
        int level = params.getInt("heading", -1);
        List reportGroups = report.getReportDefinition()
            .getAxis(axis).getHeading(level).getReportGroups();

        for (int i =0; i < keys.length; i++)
        {
            String key = keys[i].toString();
            if (key.startsWith("groupname_") && key.indexOf("new") == -1)
            {
                int index = Integer.parseInt(key.substring(key.indexOf("_")+1,
                                             key.length()));
                ReportGroup group = (ReportGroup)reportGroups.get(index);
                String name = params.getString(key, "").trim();
                if (name.length() == 0)
                {
                    scarabR.setAlertMessage(l10n.get("InvalidGroupName"));
                }
                else
                {
                    group.setName(name);
                }
            }
        }
        scarabR.setConfirmMessage(l10n.get("GroupsChanged"));
    }


    public void doSavegroups(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }

        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        int axis = params.getInt("axis", 0); // 0=row; 1=column
        int level = params.getInt("heading", -1);
        ReportHeading heading = report.getReportDefinition()
            .getAxis(axis).getHeading(level);
        List reportGroups = heading.getReportGroups();
        // the form contains data to restore the groups and it is 
        // easier to start from scratch but grab a copy of the option/users
        // before resetting them to null.
        List groupedAttributes = heading.retrieveGroupedAttributes();
        for (Iterator i = reportGroups.iterator(); i.hasNext();) 
        {
            ReportGroup group = (ReportGroup)i.next();
            group.reset();
        }

        boolean success = true;
        if (heading.calculateType() == 0) 
        {
            for (Iterator j = groupedAttributes.iterator(); j.hasNext();) 
            {
                ReportOptionAttribute reportOption = 
                    (ReportOptionAttribute)j.next();
                String name = params.getString("option_" + 
                                               reportOption.getOptionId());
                if (name == null || name.trim().length() == 0) 
                {
                    scarabR.setAlertMessage(l10n.get("InvalidGroupName"));
                    success = false;
                    break;
                }
                else 
                {
                    for (Iterator i = reportGroups.iterator(); i.hasNext();) 
                    {
                        ReportGroup group = (ReportGroup)i.next();
                        if (name.equals(group.getName())) 
                        {
                            group.addReportOptionAttribute(reportOption);
                        }
                    }
                }                
            }
        }
        else 
        {
            for (Iterator j = groupedAttributes.iterator(); j.hasNext();) 
            {
                ReportUserAttribute reportUser = 
                    (ReportUserAttribute)j.next();
                String name = params.getString(new StringBuffer(20)
                    .append("att_").append(reportUser.getAttributeId())
                    .append("user_").append(reportUser.getUserId()).toString());
                if (name == null || name.trim().length() == 0) 
                {
                    scarabR.setAlertMessage(l10n.get("InvalidGroupName"));
                    success = false;
                    break;
                }
                else 
                {
                    for (Iterator i = reportGroups.iterator(); i.hasNext();) 
                    {
                        ReportGroup group = (ReportGroup)i.next();
                        if (name.equals(group.getName())) 
                        {
                            group.addReportUserAttribute(reportUser);
                        }
                    }
                }                
            }
        }

        if (success) 
        {
            setTarget(data, "reports,AxisConfiguration.vm");
        }
    }


    public void doAdddate(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }

        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        int axis = params.getInt("axis", 0); // 0=row; 1=column
        int level = params.getInt("heading", -1);

        ReportHeading heading = report.getReportDefinition()
            .getAxis(axis).getHeading(level);
        // if level was -1, we have created a new level.  So mark the new
        // level as the current one.
        params.setString("heading", "0");
        
        List dates = heading.getReportDates();
        int index = 1;
        if (dates == null)
        {
            // make sure the heading does not contain old option or user data
            heading.reset();
        }
        else 
        {
            index = dates.size() + 1;
        }
        Calendar cal = scarabR.getCalendar();
        cal.set(Calendar.YEAR, params.getInt("y_" + index));
        cal.set(Calendar.MONTH, params.getInt("m_" + index) - 1);
        cal.set(Calendar.DAY_OF_MONTH, params.getInt("d_" + index));
        cal.set(Calendar.HOUR_OF_DAY, params.getInt("h_" + index));
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        ReportDate rdate = new ReportDate();
        rdate.setTime(cal.getTime().getTime());
        heading.addReportDate(rdate);
        scarabR.setConfirmMessage(l10n.get("DateAdded"));
    }

    public void doDeletedate(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }
        
        ScarabLocalizationTool l10n = getLocalizationTool(context);

        ValueParser params = data.getParameters();
        String[] dateIndices = params.getStrings("selectdate");
        if (dateIndices == null || dateIndices.length == 0) 
        {
            scarabR.setAlertMessage(l10n.get("NoDateSelected"));
        }
        else
        {
            int axis = params.getInt("axis", 0); // 0=row; 1=column
            int level = params.getInt("heading", -1);
            List reportDates = report.getReportDefinition()
                .getAxis(axis).getHeading(level).getReportDates();

            for (int j = dateIndices.length-1; j>=0; j--) 
            {
                reportDates.remove(Integer.parseInt(dateIndices[j])-1);
            }
            scarabR.setConfirmMessage(l10n.get("SelectedDateDeleted"));
        }
    }


    public void doRedirecttocrossmodulelist(RunData data, TemplateContext context)
         throws Exception
    {
        // x-module/issuetype works off of user's list, so make the report
        // list the current user's list.
        ((ScarabUser)data.getUser()).setCurrentMITList(
            getScarabRequestTool(context).getReport().getMITList());
        setTarget(data, "reports,XModuleList.vm");
    }

    public void doConfinedataset(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }

        ValueParser params = data.getParameters();
        if ("fixed".equals(params.getString("def_date"))) 
        {
            Calendar cal = scarabR.getCalendar();
            cal.set(Calendar.YEAR, params.getInt("def_yr"));
            cal.set(Calendar.MONTH, params.getInt("def_month") - 1);
            cal.set(Calendar.DAY_OF_MONTH, params.getInt("def_day"));
            cal.set(Calendar.HOUR_OF_DAY, params.getInt("def_hr"));
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            report.setDefaultDate(cal.getTime());
        }
        else 
        {
            report.setDefaultDate(null);
        }
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        scarabR.setConfirmMessage(l10n.get("ChangesSaved"));
        setTarget(data, "reports,ConfineDataset.vm");
    }


    public void doSwaprowcol(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ReportBridge report = scarabR.getReport();
        if (!report.isEditable(user)) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");
            return;
        }

        ValueParser params = data.getParameters();
        int axis = params.getInt("axis", 0); // 0=row; 1=column
        int level = params.getInt("heading", -1);

        ReportDefinition reportDefn = report.getReportDefinition();
        List axes = reportDefn.getReportAxisList();
        if (axes != null && !axes.isEmpty()) 
        {
            if (axes.size() == 1) 
            {
                reportDefn.addReportAxis(new ReportAxis());
            }
            ReportAxis row = (ReportAxis)axes.remove(0);
            // add back as column
            reportDefn.addReportAxis(row);            
        }
        
        // FIXME: do we need a confirmation message? -jon
    }

    public void doGeneratereport(RunData data, TemplateContext context)
         throws Exception
    {
        String format = ExportFormat.determine(data);
        if (ExportFormat.EXCEL_FORMAT.equalsIgnoreCase(format)
            || ExportFormat.TSV_FORMAT.equalsIgnoreCase(format))
        {
            // The ReportExport screen has no corresponding template.
            setTarget(data, "ReportExport.vm");
        }
        else
        {
            setTarget(data, "reports,Report_1.vm");
        }
    }
    
    public void doCreatenew(RunData data, TemplateContext context)
        throws Exception
    {
        String key = data.getParameters()
            .getString(ScarabConstants.CURRENT_REPORT);
        data.getParameters().remove(ScarabConstants.CURRENT_REPORT);
        if (key != null && key.length() > 0) 
        {
            ((ScarabUser)data.getUser()).setCurrentReport(key, null);
        }
        setTarget(data, "reports,Info.vm");            
    }
    

    public void doSavereport(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ReportBridge report = getScarabRequestTool(context).getReport();
        Intake intake = getIntakeTool(context);
        if (!report.isSavable((ScarabUser)data.getUser())) 
        {
            setNoPermissionMessage(context);
            setTarget(data, "reports,ReportList.vm");                        
        }
        else if (intake.isAllValid()) 
        {
            // make sure report has a name
            if (report.getName() == null || report.getName().trim().length() == 0) 
            {
                Group intakeReport = 
                    intake.get("Report", report.getQueryKey(), false);
                if (intakeReport == null) 
                {   
                    intakeReport = intake.get("Report", "", false);
                }  
            
                if (intakeReport != null) 
                {   
                    intakeReport.setValidProperties(report);
                }
            }

            if (report.getName() == null || report.getName().trim().length() == 0) 
            {
                getScarabRequestTool(context)
                    .setAlertMessage(l10n.get("SavedReportsMustHaveName"));
                setTarget(data, "reports,Info.vm");
            }
            else 
            {
                //don't save extra whitespace as part of name.
                String name = report.getName().trim();
                report.setName(name);
                // make sure name is unique, mysql text queries are 
                // case-insensitive, otherwise we may need to do this
                // differently to avoid similar but not exact matches.
                org.tigris.scarab.om.Report savedReport = ReportPeer
                    .retrieveByName(name);
                if (savedReport == null 
                    || savedReport.getQueryKey().equals(report.getQueryKey()))
                {
                    report.save();
                    getScarabRequestTool(context)
                        .setConfirmMessage(l10n.get("ReportSaved"));
                }
                else 
                {
                    getScarabRequestTool(context).setAlertMessage(
                        l10n.get("ReportNameNotUnique"));
                    setTarget(data, "reports,Info.vm");
                }
            }
        }
        else 
        {
            getScarabRequestTool(context).setAlertMessage(
                l10n.get("ErrorPreventedSavingReport"));
        }
    }

    public void doDeletestoredreport(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabUser user = (ScarabUser)data.getUser();
        String[] reportIds = data.getParameters().getStrings("report_id");
        if (reportIds == null || reportIds.length == 0) 
        {
            getScarabRequestTool(context).setAlertMessage(
                getLocalizationTool(context).get("MustSelectReport"));
        }
        else 
        {
            for (int i=0;i<reportIds.length; i++)
            {
                String reportId = reportIds[i];
                if (reportId != null && reportId.length() > 0)
                {
                    Report torqueReport = ReportManager
                        .getInstance(new NumberKey(reportId), false);
                    if (new ReportBridge(torqueReport).isDeletable(user)) 
                    {
                        torqueReport.setDeleted(true);
                        torqueReport.save();
                    }                   
                    else 
                    {
                        setNoPermissionMessage(context);
                    }
                }
            }
        }        
    }

    private void setNoPermissionMessage(TemplateContext context)
    {
        getScarabRequestTool(context).setAlertMessage(
            getLocalizationTool(context).get(NO_PERMISSION_MESSAGE));
    }
}
