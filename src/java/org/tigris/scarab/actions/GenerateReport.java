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

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateAction;
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

/**
    This class is responsible for report generation forms
    @author <a href="mailto:jmcnally@collab.net">John D. McNally</a>
    @version $Id: GenerateReport.java,v 1.3 2001/10/01 03:43:23 jmcnally Exp $
*/
public class GenerateReport 
    extends TemplateAction
{
    public void doStep1( RunData data, TemplateContext context )
        throws Exception
    {
        ReportGenerator report = populateReportGenerator(data, context);
        if ( report == null) 
        {
            setTarget(data, "reports,Step1.vm");            
        }
        else 
        {
            setTarget(data, "reports,Step2.vm");
        }
    }

    public void doStep2agoto2b( RunData data, TemplateContext context )
        throws Exception
    {
        if ( populateReportGenerator(data, context) == null) 
        {
            setTarget(data, "reports,Step2.vm");            
        }
        else 
        {
            step2a(data, context);
            setTarget(data, "reports,Step2b.vm");
        }
    }

    public void doStep2agoto3( RunData data, TemplateContext context )
        throws Exception
    {
        if ( populateReportGenerator(data, context) == null) 
        {
            setTarget(data, "reports,Step2.vm");            
        }
        else 
        {
            step2a(data, context);
            setTarget(data, "reports,Step3_1a.vm");
        }
    }

    public void step2a( RunData data, TemplateContext context )
    {
    }

    public void doStep2baddgroup( RunData data, TemplateContext context )
        throws Exception
    {
        ReportGenerator report = populateReportGenerator(data, context);
        if ( report != null) 
        {
            // add new option group
            List groups = report.getOptionGroups();
            ReportGenerator.OptionGroup group = report.getNewOptionGroup();
            IntakeTool intake = (IntakeTool)context
                .get(ScarabConstants.INTAKE_TOOL);
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
        if ( report != null) 
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
        if ( report != null) 
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
        if ( populateReportGenerator(data, context) == null) 
        {
            setTarget(data, "reports,Step3_1a.vm"); 
        }
        else 
        {
            setTarget(data, "reports,Step3_1b.vm");
        }
    }

    public void doStep3_1b( RunData data, TemplateContext context )
        throws Exception
    {
        if ( populateReportGenerator(data, context) == null) 
        {
            setTarget(data, "reports,Step3_1b.vm"); 
        }
        else 
        {
            setTarget(data, "reports,Report_1.vm");
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
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);

        if ( intake.isAllValid() ) 
        {
            ScarabRequestTool scarabR = (ScarabRequestTool)context
                .get(ScarabConstants.SCARAB_REQUEST_TOOL); 
            report = scarabR.getReport();
            populateReportGenerator(report, data.getParameters());

        }
        return report;
    }

    public static void populateReportGenerator(ReportGenerator report, 
                                               ValueParser parameters)
       throws Exception
    {
        Intake intake = new Intake();
        intake.init(parameters);
        
        // System.out.println("Parameters: "+ 
        //    ((RunData)context.get("data")).getParameters() );
        intake.get("Report", report.getQueryKey(), false)
            .setProperties(report);

        // set up option groups
        int i = 0;
        List groups = new ArrayList();
        ReportGenerator.OptionGroup group = report.getNewOptionGroup();
        group.setQueryKey(String.valueOf(i++));
        Group intakeGroup = intake.get("OptionGroup", 
                                       group.getQueryKey(), false);
        while ( intakeGroup != null ) 
        {
            intakeGroup.setProperties(group);
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
    }
}
