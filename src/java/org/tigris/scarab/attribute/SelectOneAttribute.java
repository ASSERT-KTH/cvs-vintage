package org.tigris.scarab.attribute;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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

import org.tigris.scarab.baseom.*;
import org.tigris.scarab.baseom.peer.*;
import org.apache.turbine.util.db.*;
import org.apache.turbine.util.*;

import com.workingdogs.village.*;

import java.util.*;
/**
 *
 * @author <a href="mailto:fedor.karpelevitch@home.com">Fedor</a>
 * @version $Revision: 1.1 $ $Date: 2000/12/18 05:03:29 $
 */
public abstract class SelectOneAttribute extends OptionAttribute
{
    protected ScarabAttributeOption value=null;
    public void init() throws Exception
    {
        Criteria crit = new Criteria()
            .add(ScarabIssueAttributeValuePeer.ISSUE_ID, getIssue().getId())
            .add(ScarabIssueAttributeValuePeer.ATTRIBUTE_ID, getId());

        Vector results = ScarabIssueAttributeValuePeer.doSelect(crit);
        if (results.size() == 1) // if value is not found it will be null until 
        {
            value = getOptionById(((ScarabIssueAttributeValue)results.get(0)).getOptionId());
            loaded = true;
        }
    }
    
    /** Updates both InternalValue and Value of the Attribute object and saves them
     * to database
     * @param newValue String representation of new value.
     * @param data app data. May be needed to get user info for votes and/or for security checks.
     * @throws Exception Generic exception
     *
     */
    public void setValue(String newValue,RunData data) throws Exception
    {
        value = getOptionByNum(Integer.parseInt(newValue));
        Criteria crit = new Criteria()
            .add(ScarabIssueAttributeValuePeer.ISSUE_ID, getIssue().getId())
            .add(ScarabIssueAttributeValuePeer.ATTRIBUTE_ID, getId())
            .add(ScarabIssueAttributeValuePeer.OPTION_ID, value.getId())
            .add(ScarabIssueAttributeValuePeer.VALUE, value.getDisplayValue());
        
        if (loaded)
        {
            ScarabIssueAttributeValuePeer.doUpdate(crit);
        }
        else
        {
            ScarabIssueAttributeValuePeer.doInsert(crit);
            loaded = true;
        }
    }
    
    public String getValue()
    {
        return value.getDisplayValue();
    }
}