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
import org.apache.turbine.util.RunData;

import com.workingdogs.village.*;

import java.util.*;

/**
 *  This is a superclass for free-form attributes such as string, date
 *  etc...
 *
 * @author <a href="mailto:fedor.karpelevitch@home.com">Fedor</a>
 * @version $Revision: 1.2 $ $Date: 2001/01/23 22:33:44 $
 */
public abstract class FreeFormAttribute extends Attribute
{
    private boolean loaded;
    protected String value;
    
    public void init() throws Exception
    {
        
        ScarabIssueAttributeValue sIAValue = null; 
        if ( getScarabIssue().isNew() ) 
        {
            sIAValue = new ScarabIssueAttributeValue();
            sIAValue.setScarabAttribute(getScarabAttribute());
            sIAValue.setScarabIssue(getScarabIssue());
            sIAValue.setDeleted(false);                
        }
        else 
        {
            sIAValue = ScarabIssueAttributeValuePeer
                .retrieveByPK( getScarabAttribute().getAttributeId(), 
                               getScarabIssue().getIssueId() );
            loaded = true;
        }        
        scarabIssueAttributeValue = sIAValue;        
    }

    public void setResources(Object resources) 
    {
        // nothing to do. no resources whatsoever.
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
        value = newValue;
        
        Criteria crit = new Criteria();
        crit.add(ScarabIssueAttributeValuePeer.ISSUE_ID, 
                 getScarabIssue().getPrimaryKey())
            .add(ScarabIssueAttributeValuePeer.ATTRIBUTE_ID, 
                 getScarabAttribute().getPrimaryKey())
            .add(ScarabIssueAttributeValuePeer.VALUE, value);

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
    /** Loads from database data specific for this Attribute including Name.
     * These are data common to all Attribute instances with same id.
     * Data retrieved here will then be used in setResources.
     * @return Object containing Attribute resources which will be used in setResources.
     */
    public Object loadResources()
    {
        return "dummy"; //need something here
    }
    
    public String getValue()
    {
        return (value==null)?"":value;
    }
}
