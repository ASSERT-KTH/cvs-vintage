package org.tigris.scarab.actions.base;

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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.ParameterParser;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.ScarabException;

/**
 * Base class for actions which modify issues. Has a method to check
 * for collisions between different changes.
 * 
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: BaseModifyIssue.java,v 1.6 2003/02/03 19:12:21 jmcnally Exp $
 */
public class BaseModifyIssue extends RequireLoginFirstAction
{
    protected Issue getIssueFromRequest(ParameterParser pp)
        throws ScarabException
    {
        String id = pp.getString("id");
        if (id == null || id.length() == 0)
        {
            throw new ScarabException("Could not locate issue.");
        }
        Issue issue = IssueManager.getIssueById(id);
        if (issue == null)
        {
            throw new ScarabException("Could not locate issue: " + id);
        }
        return issue;
    }

    protected boolean isCollision(RunData data, TemplateContext context)
        throws Exception
    {
        boolean isCollision = false;
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        long modTimestamp = data.getParameters().getLong("mod_ts");

        String id = data.getParameters().getString("id");
        if (id == null) 
        {
            List issues = scarabR.getIssues();
            List conflictIssues = new ArrayList(issues.size());
            Iterator iter = issues.iterator();
            while (iter.hasNext()) 
            {
                Issue issue = (Issue)iter.next();
                long lastModTime = issue.getModifiedDate().getTime();
                boolean isSingleCollision = modTimestamp < lastModTime;
                if (isSingleCollision) 
                {
                    isCollision = true;
                    scarabR.setAlertMessage(
                        l10n.get("MultiIssueChangeCollision"));
                    ActivitySet lastActivitySet = issue.getLastActivitySet();
                    List activities = lastActivitySet.getActivitys();
                    ArrayList objs = new ArrayList(2);
                    objs.add(issue);
                    objs.add(activities);
                    conflictIssues.add(objs);
                }
            }
            context.put("lastActivities", conflictIssues);
        }
        else 
        {
            Issue issue = IssueManager.getIssueById(id);
            List conflictIssues = new ArrayList(1);
            long lastModTime = issue.getModifiedDate().getTime();
            isCollision = modTimestamp < lastModTime;
            if (isCollision) 
            {
                scarabR.setAlertMessage(l10n.get("IssueChangeCollision"));
                ActivitySet lastActivitySet = issue.getLastActivitySet();
                List activities = lastActivitySet.getActivitys();
                ArrayList objs = new ArrayList(2);
                objs.add(issue);
                objs.add(activities);
                conflictIssues.add(objs);
                context.put("lastActivities", conflictIssues);
            }
        }        
        return isCollision;
    }
}
