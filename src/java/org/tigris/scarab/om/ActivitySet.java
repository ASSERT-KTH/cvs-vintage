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

import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria; 

import org.apache.turbine.Turbine;
import org.apache.torque.om.Persistent;

import org.tigris.scarab.util.Email;
import org.tigris.scarab.util.EmailContext;
import org.tigris.scarab.services.cache.ScarabCache;

/** 
 * This object represents a ActivitySet. It is used as a container
 * for one or more Activity objects.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: ActivitySet.java,v 1.17 2004/09/15 14:12:21 legout Exp $
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
        for (Iterator itr = activityList.iterator();itr.hasNext();)
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
/* FIXME: caching is disabled here because new Activities can be
          added to this activityset and the addition does not trigger 
          a reset of this cache (JSS).
        Object obj = ScarabCache.get(this, GET_ACTIVITY_LIST); 
        if (obj == null) 
        {
*/
            Criteria crit = new Criteria()
                .add(ActivityPeer.TRANSACTION_ID, getActivitySetId());
            result = ActivityPeer.doSelect(crit);
            ScarabCache.put(result, this, GET_ACTIVITY_LIST);
/*
        }
        else 
        {
            result = (List)obj;
        }
*/
        return result;
    }

    /**
     * Returns a list of Activity objects associated with this ActivitySet
     * And this issue.
     */
    public List getActivityListForIssue(Issue issue) throws Exception
    {
            Criteria crit = new Criteria()
                .add(ActivityPeer.TRANSACTION_ID, getActivitySetId());
            crit.add(ActivityPeer.ISSUE_ID, issue.getIssueId());
            return ActivityPeer.doSelect(crit);
    }

    public ScarabUser getCreator()
        throws TorqueException
    {
        return getScarabUser();
    }

    public boolean sendEmail(Issue issue)
         throws Exception
    {
        return sendEmail(null, issue, null, null, null);
    }

    public boolean sendEmail(EmailContext context, Issue issue)
         throws Exception
    {
        return sendEmail(context, issue, null, null, null);
    }

    public boolean sendEmail(Issue issue, String template)
         throws Exception
    {
        return sendEmail(null, issue, null, null, template);
    }

    public boolean sendEmail(EmailContext context, Issue issue, 
                             String template)
         throws Exception
    {
        return sendEmail(context, issue, null, null, template);
    }

    /** 
     *   Sends email to the users associated with the issue.
     *   That is associated with this activitySet.
     *   If no subject and template specified, assume modify issue action.
     *   throws Exception
     *
     * @param context Any contextual information for the message.
     * @param issue The issue 
     */
    public boolean sendEmail(EmailContext context, Issue issue, 
                             Collection toUsers, Collection ccUsers,
                             String template)
         throws Exception
    {
        if (context == null) 
        {
            context = new EmailContext();
        }
        
        // add data to context
        context.setIssue(issue);
        context.put("attachment", getAttachment());

        List activityList = getActivityList();
        context.put("activityList", activityList);
        Set set = new HashSet(activityList.size());
        for (Iterator itr = activityList.iterator();itr.hasNext();)
        {
            Activity activity = (Activity) itr.next();
            String desc = activity.getDescription();
            set.add(desc);
        }
        context.put("uniqueActivityDescriptions", set);

        if (template == null)
        {
            template = Turbine.getConfiguration().
                getString("scarab.email.modifyissue.template",
                "ModifyIssue.vm");
        }
        
        if (toUsers == null)
        {
            // Then add users who are assigned to "email-to" attributes
            toUsers = issue.getAllUsersToEmail(AttributePeer.EMAIL_TO);
        }
        
        if (ccUsers == null)
        {
            // add users to cc field of email
            ccUsers = issue.getAllUsersToEmail(AttributePeer.CC_TO);
        }

        String[] replyToUser = issue.getModule().getSystemEmail();

        if(Turbine.getConfiguration().getString("scarab.email.replyto.sender").equals("true"))
          {
            return Email.sendEmail(context, issue.getModule(), getCreator(), 
                                   getCreator(), toUsers, ccUsers, template);
          } 
        else 
          {
            return Email.sendEmail(context, issue.getModule(), getCreator(), 
                                   replyToUser, toUsers, ccUsers, template);
          }
    }
}
