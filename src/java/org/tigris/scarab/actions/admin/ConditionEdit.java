package org.tigris.scarab.actions.admin;

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

import org.apache.fulcrum.intake.model.Group;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.tool.IntakeTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.ConditionManager;
import org.tigris.scarab.om.ConditionPeer;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleAttributeManager;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.Transition;
import org.tigris.scarab.om.TransitionManager;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;

public class ConditionEdit extends RequireLoginFirstAction
{
    private int transitionId;
    private int moduleId;
    private int attributeId;
    private int issueTypeId;

    public void doDelete(RunData data, TemplateContext context) throws TorqueException, Exception
    {
        IntakeTool intake = getIntakeTool(context);
        Group attrGroup = intake.get("ConditionEdit", IntakeTool.DEFAULT_KEY);
        data.getParameters().remove(attrGroup.get("ConditionsArray").getKey());
        updateObject(data, context, null);
    }

    private void delete(RunData data) throws TorqueException, Exception
    {
        int nObjectType = data.getParameters().getInt("obj_type");
        Criteria crit = new Criteria();
        switch (nObjectType)
        {
            case ScarabConstants.TRANSITION_OBJECT:
            	crit.add(ConditionPeer.TRANSITION_ID, data.getParameters().getInt("transition_id"));
                break;
            case ScarabConstants.GLOBAL_ATTRIBUTE_OBJECT:
                crit.add(ConditionPeer.ATTRIBUTE_ID, data.getParameters().getInt("attId"));
            	crit.add(ConditionPeer.MODULE_ID, 0);
            	crit.add(ConditionPeer.ISSUE_TYPE_ID, 0);
                break;
            case ScarabConstants.MODULE_ATTRIBUTE_OBJECT:
        		crit.add(ConditionPeer.ATTRIBUTE_ID, data.getParameters().getInt("attId"));
            	crit.add(ConditionPeer.MODULE_ID, data.getParameters().getInt("module_id"));
            	crit.add(ConditionPeer.ISSUE_TYPE_ID, data.getParameters().getInt("issueTypeId"));
        		break;
        }
        ConditionPeer.doDelete(crit);
    	ConditionManager.clear();
    	TransitionManager.clear();
    }
    
    private void updateObject(RunData data, TemplateContext context, Integer aConditions[]) throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        switch (data.getParameters().getInt("obj_type"))
        {
            case ScarabConstants.TRANSITION_OBJECT:
                Transition transition = scarabR.getTransition(data.getParameters().getInteger("transition_id"));
            	transition.setConditionsArray(aConditions);
            	transition.save();
                break;
            case ScarabConstants.GLOBAL_ATTRIBUTE_OBJECT:
                Attribute attribute = scarabR.getAttribute(data.getParameters().getInteger("attId"));
            	attribute.setConditionsArray(aConditions);
            	attribute.save();
                break;
            case ScarabConstants.MODULE_ATTRIBUTE_OBJECT:
                Module module = scarabR.getCurrentModule();
            	RModuleAttribute rma = RModuleAttributePeer.retrieveByPK(data.getParameters().getInteger("moduleId"), data.getParameters().getInteger("attId"), data.getParameters().getInteger("issueTypeId"));
                rma.setConditionsArray(aConditions);
                RModuleAttributeManager.clear();
                ConditionManager.clear();
                rma.save(); /** TODO: Esto sobra! **/
        		break;
        }
    	AttributeManager.clear();        
    }
    
    public void doSave(RunData data, TemplateContext context) throws Exception
    {
        this.delete(data);
        IntakeTool intake = getIntakeTool(context);
        Group attrGroup = intake.get("ConditionEdit", IntakeTool.DEFAULT_KEY);
        Integer aConditions[] = ((Integer[])attrGroup.get("ConditionsArray").getValue());
        updateObject(data, context, aConditions);
            
    }
    
    public void doCancel(RunData data, TemplateContext context) throws Exception
    {
        String lastTemplate = getCancelTemplate(data);
        if (lastTemplate != null)
        {
            setTarget(data, lastTemplate);
        }
        else
        {
            super.doCancel(data, context);
        }
    }
}

