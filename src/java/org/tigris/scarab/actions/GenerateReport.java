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
import org.tigris.scarab.util.ReportGenerator;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;

/**
    This class is responsible for report generation forms
    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id: GenerateReport.java,v 1.5 2001/10/04 01:21:07 jmcnally Exp $
*/
public class GenerateReport 
    extends RequireLoginFirstAction
{
    public void doStep1( RunData data, TemplateContext context )
        throws Exception
    {
        ReportGenerator report = populateReportGenerator(data, context);
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
        ReportGenerator report = populateReportGenerator(data, context);
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
        ReportGenerator report = populateReportGenerator(data, context);
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
        ReportGenerator report = populateReportGenerator(data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            // add new option group
            List groups = report.getOptionGroups();
            ReportGenerator.OptionGroup group = report.getNewOptionGroup();
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
        ReportGenerator report = populateReportGenerator(data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            // remove any selected option groups
            List groups = report.getOptionGroups();
            for ( int i=groups.size()-1; i>=0; i-- ) 
            {
                if (((ReportGenerator.OptionGroup)groups.get(i)).isSelected())
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
        ReportGenerator report = populateReportGenerator(data, context);
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
        ReportGenerator report = populateReportGenerator(data, context);
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
        ReportGenerator report = populateReportGenerator(data, context);
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
        ReportGenerator report = populateReportGenerator(data, context);
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
        ReportGenerator report = populateReportGenerator(data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            // add new option group
            List dates = report.getReportDates();
            ReportGenerator.ReportDate newDate = report.getNewReportDate();
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
                    System.out.println("Entered delete date");
        ReportGenerator report = populateReportGenerator(data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            // remove any selected option groups
            List dates = report.getReportDates();
            for ( int i=dates.size()-1; i>=0; i-- ) 
            {
                if (((ReportGenerator.ReportDate)dates.get(i)).isSelected())
                {
                    dates.remove(i);
                    System.out.println("Removing date, size now = " + dates.size());
                }
            }
        }
        setTarget(data, "reports,Step3_2b.vm");
    }


    public void doStep3_2b( RunData data, TemplateContext context )
        throws Exception
    {
        ReportGenerator report = populateReportGenerator(data, context);
        Intake intake = getIntakeTool(context);
        if ( intake.isAllValid() ) 
        {
            setTarget(data, "reports,Report_2.vm");
        }
        else 
        {
            setTarget(data, "reports,Step3_2b.vm"); 
        }
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

    private ReportGenerator populateReportGenerator( RunData data, 
                                                     TemplateContext context)
       throws Exception
    {
        ReportGenerator report = null;
        IntakeTool intake = getIntakeTool(context);

        if ( !intake.isAllValid() ) 
        {
            data.setMessage("Please check data");
        }
        ScarabRequestTool scarabR = getScarabRequestTool(context); 
        report = scarabR.getReport();
        populateReportGenerator(report, data.getParameters());
        return report;
    }

    public static void populateReportGenerator(ReportGenerator report, 
                                               ValueParser parameters)
       throws Exception
    {
        Intake intake = new Intake();
        intake.init(parameters);

        Group intakeReport = intake.get("Report", report.getQueryKey(), false);
        intakeReport.setValidProperties(report);

        // set up option groups
        int i = 0;
        List groups = new ArrayList();
        ReportGenerator.OptionGroup group = report.getNewOptionGroup();
        group.setQueryKey(String.valueOf(i++));
        Group intakeGroup = intake.get("OptionGroup", 
                                       group.getQueryKey(), false);
        while ( intakeGroup != null ) 
        {
            intakeGroup.setValidProperties(group);
            groups.add(group);

            group = report.getNewOptionGroup();
            group.setQueryKey(String.valueOf(i++));
            intakeGroup = intake.get("OptionGroup", 
                                     group.getQueryKey(), false);
        }
        report.setOptionGroups(groups);

        List options = report.getSelectedOptionsForGrouping();
        for ( i=0; i<options.size(); i++ ) 
        {
            RModuleOption rmo = (RModuleOption)options.get(i);
            String key = "ofg" + rmo.getQueryKey();
            int groupIndex = parameters.getInt(key);
            if ( groupIndex >= 0 && groupIndex < groups.size() ) 
            {
                ((ReportGenerator.OptionGroup)groups.get(groupIndex))
                    .addOption(rmo);
            }
        }

        // set up dates
        i = 0;
        List dates = new ArrayList();
        ReportGenerator.ReportDate date = report.getNewReportDate();
        date.setQueryKey(String.valueOf(i++));
        Group intakeDate = intake.get("ReportDate", 
                                       date.getQueryKey(), false);
        while ( intakeDate != null ) 
        {
            if ( intakeDate.get("Date").isSet()) 
            {
                intakeDate.setValidProperties(date);
                dates.add(date);                
            }
            
            date = report.getNewReportDate();
            date.setQueryKey(String.valueOf(i++));
            intakeDate = intake.get("ReportDate", 
                                     date.getQueryKey(), false);
        }
        if ( dates.size() > 0 ) 
        {
            // the intakeReport.setProperties call above may have added a date
            // so we do not want to lose it. 
            List reportDates = report.getReportDates();
            if ( reportDates != null ) 
            {
                for ( int j=0; j<reportDates.size(); j++ ) 
                {
                    ReportGenerator.ReportDate reportDate = 
                        (ReportGenerator.ReportDate)reportDates.get(j);
                    date.setQueryKey(String.valueOf(i++));
                    dates.add(reportDate);
                }
            }
            report.setReportDates(dates);            
        }
    }
}
