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
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.Intake;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;
import org.apache.fulcrum.util.parser.ValueParser;

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
    @version $Id: GenerateReport.java,v 1.7 2001/10/16 00:47:31 jmcnally Exp $
*/
public class GenerateReport 
    extends RequireLoginFirstAction
{
    public void doStep1( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
        Intake intake = getIntakeTool(context);
        if ( !intake.isAllValid() ) 
        {
            data.setMessage("Invalid data");
            setTarget(data, "reports,Step1.vm");            
        }
        else if (report.getType() == 1)
        {
            // this should go to Step3_2a but that screen currently only
            // presents one valid option in the select so skip it.
            setTarget(data, "reports,Step3_2b.vm");
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
        Report report = populateReport(data, context);
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
                if ( data.getParameters().getBoolean("overwrite") ||
                     !ReportPeer.exists(report.getName()) ) 
                {
                    report.save();
                    data.setMessage("The report has been saved.");
                    setTarget(data, "reports,Report_1.vm");                    
                }
                else 
                {
                    data.setMessage("A report already exists under this name."
                                    + "if you wish to overwrite the old report"
                                    + ", click Save.");
                    setTarget(data, "reports,SaveReport.vm");
                }
            }
        }
        else 
        {
            data.setMessage("An error prevented saving your report.  Please" +
                            "notify the developers of Scarab.");
        }
    }




    /**
        Edits the stored story.
    */
    public void doEditstoredreport( RunData data, TemplateContext context )
         throws Exception
    {        /*
        IntakeTool intake = getIntakeTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Report report = populateReport();
        Group queryGroup = intake.get("Query", 
                                      query.getQueryKey() );
        String newValue = getQueryString(data);
        queryGroup.setProperties(query);
        query.setValue(newValue);
        query.saveAndSendEmail((ScarabUser)data.getUser(), 
                        scarabR.getCurrentModule(), 
                        new ContextAdapter(context));
             */
    }

    /**
        Runs the stored story.
    */
    public void doRunstoredreport( RunData data, TemplateContext context )
         throws Exception
    {        
        populateReport(data, context);
        setTarget(data, "reports,Report_1.vm");
        
        /*
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Query query = scarabR.getQuery();
        data.getParameters().add("queryString", query.getValue());
        context.put("queryString", query.getValue());
        setTarget(data, "IssueList.vm");
        */
    }


    public void doPrint( RunData data, TemplateContext context )
        throws Exception
    {
        Report report = populateReport(data, context);
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

    private Report populateReport( RunData data, TemplateContext context)
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
        return report;
    }
}
