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
import java.util.Date;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import java.sql.Connection;

import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * This class represents Activity records.
 *
 * @author <a href="mailto:jmcnally@collab.new">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: Activity.java,v 1.35 2002/07/30 22:48:15 jmcnally Exp $
 */
public class Activity 
    extends BaseActivity
    implements Persistent
{
    private AttributeOption oldAttributeOption;                 
    private AttributeOption newAttributeOption;                 

    protected static final String GET_ATTACHMENT = 
        "getAttachment";

    /**
     * This method properly handles the case where there may not be
     * any rows selected and returns null in that case.
     */
    public Attachment getAttachment()
        throws TorqueException
    {
        Attachment attachment = null;
        Object obj = ScarabCache.get(this, GET_ATTACHMENT); 
        if (obj == null)
        {
            try
            {
                Criteria crit = new Criteria();
                crit.add(ActivityPeer.ACTIVITY_ID, this.getActivityId());
                crit.addJoin(AttachmentPeer.ATTACHMENT_ID, ActivityPeer.ATTACHMENT_ID);
                List results = AttachmentPeer.doSelect(crit);
                if (!results.isEmpty())
                {
                    attachment = (Attachment) results.get(0);
                    ScarabCache.put(attachment, this, GET_ATTACHMENT);
                }
            }
            catch (Exception e)
            {
                log().error("Activity.getAttachment(): ", e);
            }
        }
        else
        {
            attachment = (Attachment)obj;
        }
        return attachment;
    }

    /**
     * Gets the AttributeOption object associated with the Old Value field
     * (i.e., the old value for the attribute before the change.)
     */
    public AttributeOption getOldAttributeOption() throws Exception
    {
        if (oldAttributeOption==null && (getOldValue() != null))
        {
            oldAttributeOption = AttributeOptionManager
                .getInstance(new NumberKey(getOldValue()));
        }
        return oldAttributeOption;
    }

    /**
     * Gets the AttributeOption object associated with the New Value field
     * (i.e., the new value for the attribute after the change.)
     */
    public AttributeOption getNewAttributeOption() throws Exception
    {
        if (newAttributeOption==null && (getNewValue() != null))
        {
            newAttributeOption = AttributeOptionManager
                .getInstance(new NumberKey(getNewValue()));
        }
        return newAttributeOption;
    }

    public void save(Connection dbCon)
        throws TorqueException
    {
        // make sure to mark last related activity as done
        if (isNew()) 
        {
            Criteria crit = new Criteria();
            crit.add(ActivityPeer.ISSUE_ID, getIssueId());
            crit.add(ActivityPeer.ATTRIBUTE_ID, getAttributeId());
            crit.add(ActivityPeer.END_DATE, null);
            List result = ActivityPeer.doSelect(crit);
            if (result.size() == 1) 
            {
                Activity a = (Activity)result.get(0);
                a.setEndDate(getActivitySet().getCreatedDate());
                a.save(dbCon);
            }
            else if (result.size() > 1) 
            {
                // something is wrong with database
                throw new TorqueException(
                    new ScarabException("Multiple activities on the same"
                                        +" attribute are active."));
            }
            else if (result.size() == 0) 
            {
                // this is okay if the issue is new or has had no previous
                // activity on this attribute.  Go ahead and check that
                // database is not corrupt.
                crit = new Criteria();
                crit.add(ActivityPeer.ISSUE_ID, getIssueId());
                crit.add(ActivityPeer.ATTRIBUTE_ID, getAttributeId());
                result = ActivityPeer.doSelect(crit);
                if (result.size() != 0) 
                {
                    throw new TorqueException(
                        new ScarabException("Previous activity has occured" 
                        + " on the same attribute but none are active."));
                }
            }
        }
        super.save(dbCon);
    }

    public String getDescription()
    {
        String desc = super.getDescription();
        if (desc != null && desc.length() > 255) 
        {
            char[] chDesc = new char[255];
            desc.getChars(0, 251, chDesc, 0);
            chDesc[252] = chDesc[253] = chDesc[254] = '.';
            desc = new String(chDesc);
        }
        return desc;
    }
}
