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

import java.sql.Connection;
import java.util.List;
import java.util.HashMap;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.tigris.scarab.util.ScarabConstants;

/** 
 * This class manages Activity objects.  
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ActivityManager.java,v 1.20 2004/05/01 19:04:23 dabbous Exp $
 */
public class ActivityManager
    extends BaseActivityManager
{
    /**
     * Creates a new <code>ActivityManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public ActivityManager()
        throws TorqueException
    {
        super();
        validFields = new HashMap();
        validFields.put(ActivityPeer.ISSUE_ID, null);
    }

    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        List listeners = (List)listenersMap.get(ActivityPeer.ISSUE_ID);
        notifyListeners(listeners, oldOm, om);
        return oldOm;
    }

    /**
     * Convenience method for getting an Activity instance
     * with a String primary key (which gets converted to a Integer).
     */
    public static Activity getInstance(String id)
        throws TorqueException
    {
        return getInstance(new Long(id));
    }
    
    public static Activity createNumericActivity(Issue issue, Attribute attribute,
                                                 ActivitySet activitySet, 
                                                 String description,
                                                 Attachment attachment,
                                                 Integer oldNumericValue,
                                                 Integer newNumericValue)
        throws TorqueException
    {
        return create(issue,attribute,activitySet,description,attachment,
                      oldNumericValue, newNumericValue,
                      null, null, null, null, null, null);
    }

    public static Activity createUserActivity(Issue issue, Attribute attribute,
                                                 ActivitySet activitySet, 
                                                 String description,
                                                 Attachment attachment,
                                                 Integer oldUserId,
                                                 Integer newUserId)
        throws TorqueException
    {
        String oldUsername = null;
        String newUsername = null;
        if (oldUserId != null)
        {
            oldUsername = ScarabUserManager.getInstance(oldUserId).getUserName();
        }
        if (newUserId != null)
        {
            newUsername = ScarabUserManager.getInstance(newUserId).getUserName();
        }
        return create(issue,attribute,activitySet,description,attachment,
                      null, null,
                      oldUserId, newUserId, 
                      null, null, oldUsername, newUsername);
    }
    
    public static Activity createAddDependencyActivity(Issue issue,
                                                 ActivitySet activitySet, 
                                                 Depend depend,
                                                 String description)
        throws TorqueException
    {
        return create(issue,null,activitySet,description,null,depend,
                      null, null,
                      null, null,
                      null, null,
                      null, depend.getDependType().getName(), null);
    }

    public static Activity createChangeDependencyActivity(Issue issue,
                                                 ActivitySet activitySet, 
                                                 Depend depend,
                                                 String description,
                                                 String oldTextValue,
                                                 String newTextValue)
        throws TorqueException
    {
        return create(issue,null,activitySet,description,null,depend,
                      null, null,
                      null, null,
                      null, null,
                      oldTextValue, newTextValue, null);
    }
    
    public static Activity createDeleteDependencyActivity(Issue issue,
                                                 ActivitySet activitySet, 
                                                 Depend depend,
                                                 String description)
        throws TorqueException
    {
        return create(issue,null,activitySet,description,null,depend,
                      null, null,
                      null, null,
                      null, null,
                      depend.getDependType().getName(), null, null);
    }
    
    public static Activity createOptionActivity(Issue issue, Attribute attribute,
                                                 ActivitySet activitySet, 
                                                 String description,
                                                 Attachment attachment,
                                                 Integer oldOptionId,
                                                 Integer newOptionId)
        throws TorqueException
    {
        return create(issue,attribute,activitySet,description,attachment,
                      null, null,
                      null, null,
                      oldOptionId, newOptionId,
                      null, null);
    }

    public static Activity createTextActivity(Issue issue,
                                                 ActivitySet activitySet, 
                                                 String description,
                                                 String newTextValue)
        throws TorqueException
    {
        return create(issue,null,activitySet,description,null,
                      null, null,
                      null, null,
                      null, null,
                      null, newTextValue);
    }

    public static Activity createTextActivity(Issue issue,
                                                 ActivitySet activitySet, 
                                                 String description,
                                                 Attachment attachment)
        throws TorqueException
    {
        return create(issue,null,activitySet,description,attachment,
                      null, null,
                      null, null,
                      null, null,
                      null, null);
    }

    public static Activity createTextActivity(Issue issue, Attribute attribute,
                                                 ActivitySet activitySet, 
                                                 String description,
                                                 String oldTextValue,
                                                 String newTextValue)
        throws TorqueException
    {
        return create(issue,attribute,activitySet,description,null,
                      null, null,
                      null, null,
                      null, null,
                      oldTextValue, newTextValue);
    }

    public static Activity createTextActivity(Issue issue,
                                                 ActivitySet activitySet, 
                                                 String description,
                                                 Attachment attachment, 
                                                 String oldTextValue,
                                                 String newTextValue)
        throws TorqueException
    {
        return create(issue,null,activitySet,description,attachment,
                      null, null,
                      null, null,
                      null, null,
                      oldTextValue, newTextValue);
    }

    public static Activity createTextActivity(Issue issue, Attribute attribute,
                                                 ActivitySet activitySet, 
                                                 String description,
                                                 Attachment attachment,
                                                 String oldTextValue,
                                                 String newTextValue)
        throws TorqueException
    {
        return create(issue,attribute,activitySet,description,attachment,
                      null, null,
                      null, null,
                      null, null,
                      oldTextValue, newTextValue);
    }

    /**
     * Populates a new Activity object for initial issue creation.
     */
    public static Activity createReportIssueActivity(Issue issue, 
                                                     ActivitySet activitySet,
                                                     String message)
        throws TorqueException
    {
        return create(issue, AttributeManager.getInstance(ScarabConstants.INTEGER_0), 
                      activitySet, message, null,
                      null, null, null, null, null, null, null, null);
    }

    /**
     * Populates a new Activity object.
     */
    public static Activity create(Issue issue, Attribute attribute, 
                       ActivitySet activitySet, String description, 
                       Attachment attachment, 
                       Integer oldNumericValue, Integer newNumericValue,
                       Integer oldUserId, Integer newUserId,
                       Integer oldOptionId, Integer newOptionId,
                       String oldTextValue, String newTextValue)
         throws TorqueException
    {
        return create(issue,attribute,activitySet,description,attachment,null,
                      oldNumericValue, newNumericValue,
                      oldUserId, newUserId,
                      oldOptionId, newOptionId,
                      oldTextValue, newTextValue, null);
    }
    
    /**
     * Populates a new Activity object.
     */
    public static Activity create(Issue issue, Attribute attribute, 
                       ActivitySet activitySet, String description, 
                       Attachment attachment, 
                       Integer oldNumericValue, Integer newNumericValue,
                       Integer oldUserId, Integer newUserId,
                       Integer oldOptionId, Integer newOptionId,
                       String oldTextValue, String newTextValue,
                       Connection dbCon)
         throws TorqueException
    {
        return create(issue,attribute,activitySet,description,attachment,null,
                      oldNumericValue, newNumericValue,
                      oldUserId, newUserId,
                      oldOptionId, newOptionId,
                      oldTextValue, newTextValue, dbCon);
    }

    /**
     * Populates a new Activity object.
     */
    public static Activity create(Issue issue, Attribute attribute, 
                       ActivitySet activitySet, String description, 
                       Attachment attachment, Depend depend,
                       Integer oldNumericValue, Integer newNumericValue,
                       Integer oldUserId, Integer newUserId,
                       Integer oldOptionId, Integer newOptionId,
                       String oldTextValue, String newTextValue, 
                       Connection dbCon)
         throws TorqueException
    {
        Activity activity = ActivityManager.getInstance();
        activity.setIssue(issue);
        if (attribute == null)
        {
            attribute = Attribute.getInstance(0);
        }
        activity.setAttribute(attribute);
        activity.setDescription(description);
        activity.setActivitySet(activitySet);
        activity.setOldNumericValue(oldNumericValue);
        activity.setNewNumericValue(newNumericValue);
        activity.setOldUserId(oldUserId);
        activity.setNewUserId(newUserId);
        activity.setOldOptionId(oldOptionId);
        activity.setNewOptionId(newOptionId);
        activity.setOldValue(oldTextValue);
        activity.setNewValue(newTextValue);
        activity.setDepend(depend);
        if (attachment != null)
        {
            activity.setAttachment(attachment);
        }
        if (dbCon == null) 
        {
            try
            {
                activity.save();
            }
            catch (Exception e)
            {
                if (e instanceof TorqueException) 
                {
                    throw (TorqueException)e; //EXCEPTION
                }
                else 
                {
                    throw new TorqueException(e); //EXCEPTION
                }
            }
        }
        else 
        {
            activity.save(dbCon);
        }
        // Make sure new activity is added to activity cache
        try
        {
            issue.addActivity(activity);
        }
        catch (Exception e)
        {
            throw new TorqueException(e); //EXCEPTION
        }
        return activity;
    }
}
