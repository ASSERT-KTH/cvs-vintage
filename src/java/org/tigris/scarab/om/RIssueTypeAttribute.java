
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
import java.util.ArrayList;

import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class RIssueTypeAttribute 
    extends org.tigris.scarab.om.BaseRIssueTypeAttribute
    implements Persistent
{
    /**
     * This method sets the defaultTextFlag property and also makes sure 
     * that no other related RIA is defined as the default.  It should be
     * used instead of setDefaultTextFlag in application code.
     *
     * @param b a <code>boolean</code> value
     */
    public void setIsDefaultText(boolean b)
        throws Exception
    {
        if (b && !getDefaultTextFlag()) 
        {
            // get related RIAs
            List rias = getIssueType().getRIssueTypeAttributes(false);
            
            // make sure no other rma is selected
            for (int i=0; i<rias.size(); i++) 
            {
                RIssueTypeAttribute ria = (RIssueTypeAttribute)rias.get(i);
                if (ria.getDefaultTextFlag()) 
                {
                    ria.setDefaultTextFlag(false);
                    ria.save();
                    break;
                }
            }
        }
        setDefaultTextFlag(b);
    }

    /**
     * Avoids a problem with intake and not having a getter.  It always 
     * returns false, but the method is not used in code.  Assuming some
     * logic should be used here, see RModuleAttribute for a similar 
     * method that is functional.
     *
     * @return false
     */
    public boolean getIsDefaultText()
        throws Exception
    {
        boolean isDefault = getDefaultTextFlag();
        if (!isDefault && getAttribute().isTextAttribute()) 
        {
            // get related RIAs
            List rias = getIssueType().getRIssueTypeAttributes();
            
            // check if another is chosen
            boolean anotherIsDefault = false;
            for (int i=0; i<rias.size(); i++) 
            {
                RIssueTypeAttribute ria = (RIssueTypeAttribute)rias.get(i);
                if (ria.getDefaultTextFlag()) 
                {
                    anotherIsDefault = true;
                    break;
                }
            }
            
            if (!anotherIsDefault) 
            {
                // locate the default text attribute
                for (int i=0; i<rias.size(); i++) 
                {
                    RIssueTypeAttribute ria = (RIssueTypeAttribute)rias.get(i);
                    if (ria.getAttribute().isTextAttribute()) 
                    {
                        if (ria.getAttributeId().equals(getAttributeId())) 
                        {
                            isDefault = true;
                        }
                        else 
                        {
                            anotherIsDefault = true;
                        }
                        
                        break;
                    }
                }
            }            
        }
        return isDefault;
    }

    public void delete(ScarabUser user)
         throws Exception
    {                
        Criteria c = new Criteria()
            .add(RIssueTypeAttributePeer.ISSUE_TYPE_ID, getIssueTypeId())
            .add(RIssueTypeAttributePeer.ATTRIBUTE_ID, getAttributeId());
        RIssueTypeAttributePeer.doDelete(c);
        Attribute attr = getAttribute();
        String attributeType = null;
        attributeType = (attr.isUserAttribute() ? IssueType.USER : IssueType.NON_USER);
        getIssueType().getRIssueTypeAttributes(false, attributeType).remove(this);

        // delete issuetype-option mappings
        if (attr.isOptionAttribute())
        {
            List optionList = getIssueType().getRIssueTypeOptions(attr, false);
            ArrayList optionIdList = new ArrayList(optionList.size());
            for (int i =0; i<optionList.size(); i++)
            { 
                optionIdList.add(((RIssueTypeOption)optionList.get(i)).getOptionId());
            }
            Criteria c2 = new Criteria()
                .add(RIssueTypeOptionPeer.ISSUE_TYPE_ID, getIssueTypeId())
                    .addIn(RIssueTypeOptionPeer.OPTION_ID, optionIdList);
            RIssueTypeOptionPeer.doDelete(c2);
        }
    }

    /**
     * Copies this object's properties.
     */
    public RIssueTypeAttribute copyRia()
         throws Exception
    {                
        RIssueTypeAttribute ria = new RIssueTypeAttribute(); 
        ria.setIssueTypeId(getIssueTypeId());
        ria.setAttributeId(getAttributeId());
        ria.setActive(getActive());
        ria.setRequired(getRequired());
        ria.setOrder(getOrder());
        ria.setQuickSearch(getQuickSearch());
        ria.setDefaultTextFlag(getDefaultTextFlag());
        return ria;
    }
}
