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

import java.util.HashMap;
import java.util.List;

import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;

/** 
 * This class manages Attachment objects.  
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: AttachmentManager.java,v 1.9 2003/03/25 16:57:53 jmcnally Exp $
 */
public class AttachmentManager
    extends BaseAttachmentManager
{
    /**
     * Creates a new <code>AttachmentManager</code> instance.
     *
     * @exception TorqueException if an error occurs
     */
    public AttachmentManager()
        throws TorqueException
    {
        super();
        validFields = new HashMap();
        validFields.put(AttachmentPeer.ISSUE_ID, null);
    }

    protected Persistent putInstanceImpl(Persistent om)
        throws TorqueException
    {
        Persistent oldOm = super.putInstanceImpl(om);
        List listeners = (List)listenersMap.get(AttachmentPeer.ISSUE_ID);
        notifyListeners(listeners, oldOm, om);
        return oldOm;
    }

    public static Attachment getInstance(String id)
        throws TorqueException
    {
        return getInstance(new Long(id));
    }

    public static Attachment getComment(Attachment attachment, Issue issue, 
                                        ScarabUser user)
         throws Exception
    {
        return populate(attachment, issue, Attachment.COMMENT__PK, "comment", 
                 user, "text/plain");
    }

    public static Attachment getReason(Attachment attachment, Issue issue, 
                                        ScarabUser user)
         throws Exception
    {
        return populate(attachment, issue, Attachment.MODIFICATION__PK, "reason", 
                 user, "text/plain");
    }

    /**
     * Populate a new Attachment object.
     */
    private static Attachment populate(Attachment attachment,
                                      Issue issue, Integer typeId, 
                                      String name, ScarabUser user, 
                                      String mimetype)
         throws Exception
    {
        attachment.setIssue(issue);
        attachment.setTypeId(typeId);
        attachment.setName(name);
        attachment.setCreatedBy(user.getUserId());
        attachment.setMimeType(mimetype);
        attachment.save();
        return attachment;
    }
}
