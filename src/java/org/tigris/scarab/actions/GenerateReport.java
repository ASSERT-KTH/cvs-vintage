package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.turbine.RunData;

import org.apache.commons.util.SequencedHashtable;

import org.apache.torque.util.Criteria;
import org.apache.torque.om.NumberKey;
import org.apache.fulcrum.intake.Retrievable;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.Intake;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.util.parser.ValueParser;
import org.apache.fulcrum.util.parser.BaseValueParser;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.attribute.UserAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.util.word.IssueSearch;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.om.Report;
import org.tigris.scarab.om.ReportPeer;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;

/**
    This class is responsible for report generation forms
    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id: GenerateReport.java,v 1.10 2001/10/28 19:57:27 jmcnally Exp $
*/
public class GenerateReport 
    extends RequireLoginFirstAction
{
    public void doStep1( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step1.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( !intake.isAllValid() ) 
        {
            data.setMessage("Invalid data");
            setTarget(data, "reports,Step1.vm");            
        }
        else if (report.getType() == 1)
        {
            setTarget(data, "reports,Step3_2a.vm");
        }
        else if (report.getType() == 0)
        {
            setTarget(data, "reports,Step2.vm");
        }
        else 
        {
            data.setMessage("Invalid data");
            setTarget(data, "reports,Step1.vm");            
        }
    }

    public void doStep2agoto2b( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step2.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Step2b.vm");
        }
        else 
        {
            setTarget(data, "reports,Step2.vm");            
        }
    }

    public void doStep2agoto3( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step2.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Step3_1a.vm");
        }
        else 
        {
            setTarget(data, "reports,Step2.vm");            
        }
    }

    public void doStep2baddgroup( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step2b.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            // add new option group
            List groups = report.getOptionGroups();
            Report.OptionGroup group = report.getNewOptionGroup();
            Group intakeGroup = intake.get("OptionGroup", 
                                           group.getQueryKey(), false);
            if ( intakeGroup != null ) 
            {
                intakeGroup.setProperties(group);
                if ( group.getDisplayValue() != null 
                     && group.getDisplayValue().length() > 0 ) 
                {
                    group.setQueryKey(String.valueOf(groups.size()));
                    groups.add(group);
                }
            }
        }
        setTarget(data, "reports,Step2b.vm");
    }

    public void doStep2bdeletegroup( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step2b.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            // remove any selected option groups
            List groups = report.getOptionGroups();
            for ( int i=groups.size()-1; i>=0; i-- ) 
            {
                if (((Report.OptionGroup)groups.get(i)).isSelected())
                {
                    groups.remove(i);
                }
            }
        }
        setTarget(data, "reports,Step2b.vm");
    }

    public void doStep2b( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step2b.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Step3_1a.vm");
        }
        else 
        {
            setTarget(data, "reports,Step2b.vm");
        }
    }

    public void doStep3_1a( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step3_1a.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Step3_1b.vm");
        }
        else 
        {
            setTarget(data, "reports,Step3_1a.vm"); 
        }
    }

    public void doStep3_1b( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step3_1b.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Report_1.vm");
        }
        else 
        {
            setTarget(data, "reports,Step3_1b.vm"); 
        }
    }

    public void doStep3_2a( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step3_2a.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Step3_2b.vm");
        }
        else 
        {
            setTarget(data, "reports,Step3_2a.vm"); 
        }
    }


    public void doStep3_2badddate( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step3_2b.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            // add new option group
            List dates = report.getReportDates();
            Report.ReportDate newDate = report.getNewReportDate();
            Group intakeDate = intake.get("ReportDate", 
                                           newDate.getQueryKey(), false);
            if ( intakeDate != null ) 
            {                
                intakeDate.setProperties(newDate);
                newDate.setQueryKey(String.valueOf(dates.size()));
                dates.add(newDate);
            }
        }
        setTarget(data, "reports,Step3_2b.vm");
    }

    public void doStep3_2bdeletedate( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step3_2b.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            // remove any selected option groups
            List dates = report.getReportDates();
            for ( int i=dates.size()-1; i>=0; i-- ) 
            {
                if (((Report.ReportDate)dates.get(i)).isSelected())
                {
                    dates.remove(i);
                }
            }
        }
        setTarget(data, "reports,Step3_2b.vm");
    }


    public void doStep3_2b( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Step3_2b.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Report_1.vm");
        }
        else 
        {
            setTarget(data, "reports,Step3_2b.vm"); 
        }
    }


    public void doSavereport( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,SaveReport.vm", data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            // make sure report has a name
            if ( report.getName() == null || report.getName().length() == 0 ) 
            {
                data.setMessage("Saved reports must have a name.");
                setTarget(data, "reports,SaveReport.vm");
            }
            else 
            {
                // make sure name is unique
                Report savedReport = ReportPeer
                    .retrieveByName(report.getName());
                if (savedReport == null 
                    || savedReport.getReportId().equals(report.getReportId()))
                {
                    report.save();
                    data.setMessage("The report has been saved.");
                    setTarget(data, "reports,Report_1.vm");                    
                }
                else 
                {
                    data.setMessage("A report already exists under this name."
                                    + "Please choose a unique name.");
                    setTarget(data, "reports,SaveReport.vm");
                }
            }
        }
        else 
        {
            data.setMessage("An error prevented saving your report.");
        }
    }


    /**
        Edits the stored story.
    */
    public void doEditstoredreport( RunData data, TemplateContext context )
         throws Exception
    {
        Intake intake = getIntakeTool(context);
        intake.removeAll();
        populateReport("", data, context);
        setTarget(data, "reports,Step1.vm");
    }

    /**
        Runs the stored story.
    */
    public void doRunstoredreport( RunData data, TemplateContext context )
         throws Exception
    {        
        populateReport("", data, context);
        setTarget(data, "reports,Report_1.vm");
    }

    public void doDeletereport( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,SaveReport.vm", data, context);
        report.setDeleted(true);
        report.save();
        ScarabRequestTool scarabR = getScarabRequestTool(context); 
        scarabR.setReport(null);
        Intake intake = getIntakeTool(context);
        intake.removeAll();
        setTarget(data, "reports,Step1.vm");
    }


    public void doPrint( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport("reports,Report_1.vm", data, context);
        setTarget(data, "reports,Report_1.vm");
        data.setMessage("Use your browser to print the report.");
    }

    /**
     *  This manages clicking the Cancel button
     */
    public void doCancel( RunData data, TemplateContext context ) 
        throws Exception
    {
        String template = Turbine.getConfiguration()
            .getString("template.homepage", "Start.vm");
        setTarget(data, template);
    }

    /**
     * calls doCancel()
     */
    public void doPerform( RunData data, TemplateContext context ) 
        throws Exception
    {
        doCancel(data, context);
    }

    private Report populateReport( String template, 
                                   RunData data, TemplateContext context)
       throws Exception
    {
        Report report = null;
        IntakeTool intake = getIntakeTool(context);

        if ( !intake.isAllValid() ) 
        {
            data.setMessage("Please check data");
        }

        ScarabRequestTool scarabR = getScarabRequestTool(context); 
        report = scarabR.getReport();

        // add a value parser to the context that can be used to build
        // links for forwarding report data
        ValueParser vp = getReportParameters(report, template, data, context);
        intake.addGroupsToParameters(vp);
        context.put("reportParameters", vp);
        return report;
    }

    private ValueParser getReportParameters(Report report, String template, 
                                            RunData data, 
                                            TemplateContext context)
        throws Exception
    {
        ValueParser params = new BaseValueParser();
        IntakeTool intake = getIntakeTool(context);
        Group ir = intake.get("Report").mapTo(report);
        String id = data.getParameters().getString("report_id"); 
        if (id != null && id.length() > 0)
        {
            params.add("report_id", id);
        }
        if (!template.equals("reports,Step1.vm"))
        {
            if (report.getName() != null)
            {
                Field field = ir.get("Name"); 
                params.add(field.getKey(), field.toString()); 
            }
            if (report.getDescription() != null)
            {
                Field field = ir.get("Description"); 
                params.add(field.getKey(), field.toString()); 
            }
            if (report.getType() != 0)
            {
                Field field = ir.get("Type"); 
                params.add(field.getKey(), field.toString()); 
            }
        }

        if (!template.equals("reports,Step2.vm"))
        {
            String[] aogs = report.getAttributesAndOptionsForGrouping();
            if (aogs != null)
            {
                Field field = ir.get("AttributesAndOptionsForGrouping"); 
                for ( int i=0; i<aogs.length; i++ ) 
                {
                    params.add(field.getKey(), aogs[i]);
                }
                
            }
        }

        if (!template.equals("reports,Step2b.vm"))
        {
            List ogs = report.getOptionGroups();
            if (ogs != null)
            {
                Iterator og = ogs.iterator();
                while (og.hasNext()) 
                {   
                    Report.OptionGroup group = (Report.OptionGroup)og.next();
                    Group intakeOptionGroup = 
                        intake.get("OptionGroup").mapTo(group);
                    Field field = intakeOptionGroup.get("DisplayValue");
                    params.add(field.getKey(), field.toString());

                    List options = group.getOptions();
                    if (options != null) 
                    {
                        Iterator opti = options.iterator();
                        while (opti.hasNext()) 
                        {
                            Retrievable option = (Retrievable)opti.next();
                            params.add("ofg" + option.getQueryKey(), 
                                       group.getQueryKey());
                        }
                    }
                }
            }
        }

        if (!template.equals("reports,Step3_1a.vm")  
            && !template.equals("reports,Step3_2a.vm"))
        {
            if (report.getAxis1Category() >= 0)
            {
                Field field = ir.get("Axis1Category"); 
                params.add(field.getKey(), field.toString()); 
            }
            if (report.getAxis2Category() >= 0)
            {
                Field field = ir.get("Axis2Category"); 
                params.add(field.getKey(), field.toString()); 
            }
        }

        if (!template.equals("reports,Step3_1b.vm")  
            && !template.equals("reports,Step3_2b.vm"))
        {
            String[] keys = report.getAxis1Keys();
            if (keys != null)
            {
                Field field = ir.get("Axis1Keys"); 
                for ( int i=0; i<keys.length; i++ ) 
                {
                    params.add(field.getKey(), keys[i]);
                }
            }

            keys = report.getAxis2Keys();
            if (keys != null)
            {
                Field field = ir.get("Axis2Keys"); 
                for ( int i=0; i<keys.length; i++ ) 
                {
                    params.add(field.getKey(), keys[i]);
                }
            }
        }


        if (!template.equals("reports,Step3_1a.vm")  
            && !template.equals("reports,Step3_2b.vm"))
        {
            List dates = report.getReportDates();
            if (dates != null)
            {
                Iterator datesi = dates.iterator();
                while (datesi.hasNext()) 
                {   
                    Report.ReportDate date = (Report.ReportDate)datesi.next();
                    Group intakeOptionGroup = 
                        intake.get("ReportDate").mapTo(date);
                    Field field = intakeOptionGroup.get("Date");
                    params.add(field.getKey(), date.getDate().getTime());
                }
            }
        }
        return params;
    }
}
