package org.tigris.scarab.util.xml;

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
import java.util.ArrayList;

import org.apache.commons.digester.Digester;

import org.apache.fulcrum.security.TurbineSecurity;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Attachment;

/**
 * Handler for the xpath "scarab/module/issue/attachment/modified-by"
 *
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @author <a href="mailto:richard.han@bitonic.com">Richard Han</a>
 */
public class AttachmentModifiedByRule extends BaseRule
{
    public AttachmentModifiedByRule(Digester digester, String state, 
                                    ArrayList userList)
    {
        super(digester, state, userList);
    }
    
    /**
     * This method is called when the body of a matching XML element
     * is encountered.  If the element has no body, this method is
     * not called at all.
     *
     * @param text The text of the body of this element
     */
    public void body(String text) throws Exception
    {
        log().debug("(" + getState() + ") attachment modified by body: " + text);
        super.doInsertionOrValidationAtBody(text);
    }
    
    protected void doInsertionAtBody(String text) throws Exception
    {
        if (text != null && !text.trim().equals(""))
        {
            Attachment attachment = (Attachment)digester.pop();
            ScarabUser user = (ScarabUser)TurbineSecurity.getUser(text);
            attachment.setModifiedBy(user.getUserId());
            digester.push(attachment);
        }
    }
    
    protected void doValidationAtBody(String text) throws Exception
    {
        if (text != null && !text.trim().equals(""))
        {
            validateUser(text);
        }
    }
}
