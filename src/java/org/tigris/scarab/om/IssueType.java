package org.tigris.scarab.om;

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

import java.util.List;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;

import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.util.ScarabException;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: IssueType.java,v 1.8 2001/11/17 01:13:31 elicia Exp $
 */
public  class IssueType 
    extends org.tigris.scarab.om.BaseIssueType
    implements Persistent
{

    public static final NumberKey ISSUE__PK = new NumberKey("1");
    public static final NumberKey USER_TEMPLATE__PK = new NumberKey("2");
    public static final NumberKey GLOBAL_TEMPLATE__PK = new NumberKey("3");

    /**
     * List of attribute groups associated with this module and issue type.
     */
    public List getAttributeGroups(ModuleEntity module)
        throws Exception
    {
        Criteria crit = new Criteria()
            .add(AttributeGroupPeer.MODULE_ID, module.getModuleId())
            .add(AttributeGroupPeer.ISSUE_TYPE_ID, getIssueTypeId())
            .addAscendingOrderByColumn(AttributeGroupPeer.PREFERRED_ORDER);
        return AttributeGroupPeer.doSelect(crit);
    }

    /**
     * Creates new attribute group.
     */
    public AttributeGroup createNewGroup (ModuleEntity module)
        throws Exception
    {
        List groups = getAttributeGroups(module);
        AttributeGroup ag = new AttributeGroup();

        // Make default group name 'attribute group x' where x is size + 1
        ag.setName("attribute group " + Integer.toString(groups.size()+1));
        ag.setOrder(groups.size() +2);
        ag.setModuleId(module.getModuleId());
        ag.setIssueTypeId(getIssueTypeId());
        ag.save();
        return ag;
    }

    /**
     * Gets the sequence where the dedupe screen fits between groups.
     */
    public int getDedupeSequence(ModuleEntity module)
        throws Exception
    {
        int sequence = 1;
        List groups = getAttributeGroups(module);
        for (int i=1; i<=groups.size(); i++)
        {
            int order;
            int previousOrder;
            try
            {
                order = ((AttributeGroup)groups.get(i)).getOrder();
                previousOrder = ((AttributeGroup)groups.get(i-1)).getOrder();
            }
            catch (Exception e)
            {
                return sequence;
            }
            if (order != previousOrder + 1)
            {
                sequence = order-1;
                break;
            }
        }
        return sequence;
    }    

    /**
     * Gets the id of the template that corresponds to the issue type.
     */
    public NumberKey getTemplateId()
        throws Exception
    {
        NumberKey templateId = null;
        Criteria crit = new Criteria();
        crit.add(IssueTypePeer.PARENT_ID, getIssueTypeId());
        List results = (List)IssueTypePeer.doSelect(crit);
        if (results.isEmpty() || results.size()>1 )
        {
            throw new ScarabException("There has been an error.");
        }
        else
        {
            templateId = ((IssueType)results.get(0)).getIssueTypeId();
        }
        return templateId;
    }        
}
