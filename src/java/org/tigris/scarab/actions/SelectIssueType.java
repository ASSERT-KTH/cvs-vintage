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

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.torque.om.NumberKey;

// Scarab Stuff
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;

/**
 * This class will allow you to set the selected Issue Type for a user.
 * It will also contains the logic for setting the target screen
 * to display depending on whether or not the issue type supports
 * dedupe or not. this is business logic that belongs in the action,
 * not in the templates.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: SelectIssueType.java,v 1.15 2003/02/04 11:26:00 jon Exp $
 */
public class SelectIssueType extends RequireLoginFirstAction
{
    /**
     * Main action execution.
     */
    public void doSelect(RunData data, TemplateContext context) throws Exception
    {
        // set the next issue type
        String newIssueType = 
            data.getParameters().getString(ScarabConstants.NEW_ISSUE_TYPE);
        if (newIssueType == null)
        {
            setTarget(data, ((ScarabUser)data.getUser()).getHomePage());
            return;
        }
        data.getParameters().setString(ScarabConstants.CURRENT_ISSUE_TYPE, 
            newIssueType);
            data.getParameters().remove(ScarabConstants.REPORTING_ISSUE);
        
        IssueType issueType = IssueTypeManager
            .getInstance(new NumberKey(newIssueType), false);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        scarabR.setCurrentIssueType(issueType);

        String nextTemplate = 
            data.getParameters().getString(ScarabConstants.NEXT_TEMPLATE);
     
        if (nextTemplate == null) 
        {
            scarabR.setReportingIssue(null);
            data.getParameters().remove(ScarabConstants.REPORTING_ISSUE);
            setTarget(data, scarabR.getNextEntryTemplate());
        }
        // if the user has just changed issue types and their homepage
        // tab is set Enter New... take them to the issue entry instead.
        else if ("home,EnterNew.vm".equals(nextTemplate))
        {
            setTarget(data, scarabR.getNextEntryTemplate());
        }
        else 
        {
            setTarget(data, nextTemplate);
        }
        
    }

    /**
        calls doSelect().
    */
    public void doPerform(RunData data, TemplateContext context) throws Exception
    {
        doSelect(data, context);
    }
}
