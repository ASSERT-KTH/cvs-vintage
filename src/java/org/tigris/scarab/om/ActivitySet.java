package org.tigris.scarab.om;

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
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria; 

import org.apache.fulcrum.template.TemplateContext;
import org.apache.fulcrum.template.DefaultTemplateContext;
import org.apache.fulcrum.localization.Localization;

import org.apache.turbine.Turbine;
import org.apache.torque.om.Persistent;

import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.services.cache.ScarabCache;

/** 
 * This object represents a ActivitySet. It is used as a container
 * for one or more Activity objects.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ActivitySet.java,v 1.7 2003/02/04 11:26:00 jon Exp $
 */
public class ActivitySet 
    extends BaseActivitySet
    implements Persistent
{
    private static final String GET_ACTIVITY_LIST = 
        "getActivityList";
    
    /**
     * Sets the activity list for this activitySet.
     */
    public void setActivityList(List activityList)
        throws Exception
    {
        Iterator itr = activityList.iterator();
        while (itr.hasNext())
        {
            Activity activity = (Activity) itr.next();
            activity.setActivitySet(this);
            activity.save();
        }
        ScarabCache.put(activityList, this, GET_ACTIVITY_LIST);
    }

    /**
     * Returns a list of Activity objects associated with this ActivitySet.
     */
    public List getActivityList() throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(this, GET_ACTIVITY_LIST); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria()
                .add(ActivityPeer.TRANSACTION_ID, getActivitySetId());
            result = ActivityPeer.doSelect(crit);
            ScarabCache.put(result, this, GET_ACTIVITY_LIST);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    public ScarabUser getCreator()
        throws TorqueException
    {
        return getScarabUser();
    }

    public boolean sendEmail(TemplateContext context, Issue issue)
         throws Exception
    {
        return sendEmail(context, issue, null, null, null, null);
    }

    public boolean sendEmail(TemplateContext context, Issue issue, 
                           String subject, String template)
         throws Exception
    {
        return sendEmail(context, issue, null, null, subject, template);
    }

    /** 
     *   Sends email to the users associated with the issue.
     *   That is associated with this activitySet.
     *   If no subject and template specified, assume modify issue action.
     *   throws Exception
     */
    public boolean sendEmail(TemplateContext context, Issue issue, 
                           List toUsers, List ccUsers,
                           String subject, String template)
         throws Exception
    {
        if (context == null) 
        {
            context = new DefaultTemplateContext();
        }
        
        // add data to context
        context.put("issue", issue);
        context.put("attachment", getAttachment());

        List activityList = getActivityList();
        context.put("activityList", activityList);
        Iterator itr = activityList.iterator();
        Set set = new HashSet(activityList.size());
        while (itr.hasNext())
        {
            Activity activity = (Activity) itr.next();
            set.add(activity.getDescription());
        }
        context.put("uniqueActivityDescriptions", set);

        if (subject == null)
        {
            subject = Localization.format(ScarabConstants.DEFAULT_BUNDLE_NAME,
                Locale.getDefault(),
                "DefaultModifyIssueEmailSubject", 
                issue.getModule().getRealName().toUpperCase(), 
                issue.getUniqueId());
        }
        
        if (template == null)
        {
            template = Turbine.getConfiguration().
                getString("scarab.email.modifyissue.template",
                "email/ModifyIssue.vm");
        }
        
        if (toUsers == null)
        {
            // Then add users who are assigned to "email-to" attributes
            toUsers = issue.getUsersToEmail(AttributePeer.EMAIL_TO);
        }
        
        if (ccUsers == null)
        {
            // add users to cc field of email
            ccUsers = issue.getUsersToEmail(AttributePeer.CC_TO);
        }
        
        String[] replyToUser = issue.getModule().getSystemEmail();

        return Email.sendEmail(context, issue.getModule(), getCreator(), 
            replyToUser, toUsers, ccUsers, subject, template);
    }
}
