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

import java.util.Date;

import org.tigris.scarab.util.ScarabException;

import org.apache.torque.TorqueException;
import org.apache.torque.om.NumberKey;

/** 
 * This class manages ActivitySet objects.
 *
 * @author <a href="mailto:jmcnally@collab.net">JohnMcNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ActivitySetManager.java,v 1.5 2003/03/25 16:57:52 jmcnally Exp $
 */
public class ActivitySetManager
    extends BaseActivitySetManager
{
    /**
     * Creates a new <code>ActivitySetManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public ActivitySetManager()
        throws TorqueException
    {
        super();
    }

    /**
     * Gets a new ActivitySet object by the ActivitySetId String
     */
    public static ActivitySet getInstance(String key)
        throws TorqueException
    {
        return getInstance(new NumberKey(key));
    }

    /**
     * Populates a new activitySet object.
     */
    public static ActivitySet getInstance(ActivitySetType tt, ScarabUser user)
        throws Exception
    {
        return getInstance(tt.getTypeId(), user, null);
    }

    /**
     * Populates a new activitySet object.
     */
    public static ActivitySet getInstance(ActivitySetType tt, 
                                          ScarabUser user, Attachment attachment)
        throws Exception
    {
        return getInstance(tt.getTypeId(), user, attachment);
    }

    /**
     * Populates a new activitySet object.
     */
    public static ActivitySet getInstance(Integer typeId, ScarabUser user)
        throws Exception
    {
        return getInstance(typeId, user, null);
    }

    /**
     * Populates a new activitySet object.
     */
    public static ActivitySet getInstance(Integer typeId, 
                                          ScarabUser user, Attachment attachment)
        throws Exception
    {
        if (attachment != null && attachment.getAttachmentId() == null) 
        {
            String mesg = 
                "Attachment must be saved before starting activitySet";
            throw new ScarabException(mesg);
        }
        ActivitySet activitySet = new ActivitySet();
        activitySet.setTypeId(typeId);
        activitySet.setCreatedBy(user.getUserId());
        activitySet.setCreatedDate(new Date());
        if (attachment != null && 
            attachment.getData() != null &&
            attachment.getData().length() > 0)
        {
            activitySet.setAttachment(attachment);
        }
        return activitySet;
    }
}
