package org.tigris.scarab.workflow;

/* ================================================================
 * Copyright (c) 2000-2003 CollabNet.  All rights reserved.
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

import org.tigris.scarab.om.*;
import org.tigris.scarab.util.Log;

import org.apache.torque.TorqueException;
import org.apache.fulcrum.security.entity.Role;
import org.apache.fulcrum.security.util.AccessControlList;
import org.apache.fulcrum.security.util.GroupSet;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.entity.Group;

import java.util.Iterator;
import java.util.List;

/**
 * Simple implementation of Workflow, relies on the Transition tables, where every record defines a
 * transition available for a given Role, from an option to another.
 * <ul>
 * <li>If there are no transitions defined, it will always return true</li>
 * <li>If the "from" option of a transition is null, it will mean "Any option", and the "To" will
 * be available from any option.</li>
 * <li>If the "to" option of a transition is null, any option will be available from the "From" option.</li>
 * <li>If both the 'to' and 'from' options are null, the role will be able to change freely from one value
 * to another.</li>
 * If any transition happens to have associated Conditions, will only be available when this condition
 * evals to true. 
 * </ul>
 */
public class CheapWorkflow extends DefaultWorkflow{
 
    /**
     * Returns true if the transition from the option fromOption to toOption is
     * allowed for the current user.
     *  
     */
    public boolean canMakeTransition(ScarabUser user,
            AttributeOption fromOption, AttributeOption toOption, Issue issue)
    {
        boolean result = false;
        List allTransitions = null;
        Module module = null;
        try
        {
            if (fromOption.equals(toOption))
            {
                result = true;
            }
            else
            {
                if (TransitionPeer.hasDefinedTransitions(toOption
                        .getAttribute()))
                {
                    allTransitions = TransitionPeer.getTransitions(fromOption,
                            toOption);
                    allTransitions = filterConditionalTransitions(allTransitions, issue);
                    module = issue.getModule();
                    Iterator iter = allTransitions.iterator();
                    while (!result && iter.hasNext())
                    {
                        Object obj = iter.next();
                        Transition tran = (Transition) obj;
                        Role requiredRole = tran.getRole();
                        if (requiredRole != null)
                        { // A role is required for this transition to be
                          // allowed
                            try
                            {
                                List modules = user.getModules();
                                AccessControlList acl = TurbineSecurity
                                        .getACL(user);
                                GroupSet allGroups = TurbineSecurity
                                        .getAllGroups();
                                Group group = allGroups.getGroup(module
                                        .getName());
                                result = acl.hasRole(requiredRole, group);
                            }
                            catch (Exception e)
                            {
                                Log.get(this.getClass().getName())
                                        .error(
                                                "canMakeTransition: getModules(): "
                                                        + e);
                            }
                        }
                        else
                        {
                            result = true;
                        }
                    }
                }
                else
                {
                    result = true;
                }
            }
        }
        catch (TorqueException te)
        {
            Log.get(this.getClass().getName())
                    .error("canMakeTransition: " + te);
        }
        return result;
    }
    
    /**
     * Filter the allowed transitions so only those not-conditioned and those whose condition
     * is true will remain.
     *  
     * @param transitions
     * @param issue
     * @return
     * @throws TorqueException
     */
    public List filterConditionalTransitions(List transitions, Issue issue) throws TorqueException
    {
        try
        {
	        if (transitions != null)
	        {
		        for (int i=transitions.size()-1; i>=0; i--)
		        {
		            Transition tran = (Transition)transitions.get(i);
		            List conditions = tran.getConditions();
		            if (null != conditions && conditions.size() > 0)
		            {
		                boolean bRemove = true;
		                for (Iterator itReq = conditions.iterator(); bRemove && itReq.hasNext(); )
		                {
		                    Condition cond = (Condition)itReq.next();
		                    Attribute requiredAttribute = cond.getAttributeOption().getAttribute();
		                    Integer optionId = cond.getOptionId();
		                    Integer issueOptionId = issue.getAttributeValue(requiredAttribute).getOptionId(); 
	                        if (issueOptionId != null && issueOptionId.equals(optionId))
	                        {
	                            bRemove = false;
	                        }
		                }
		                if (bRemove)
		                {
		                    transitions.remove(i);
		                }
		            }
		        }
	        }
	    }
        catch (Exception e)
    	{
    	    Log.get().error("filterConditionalTransitions: " + e);
    	}

        return transitions;
    }
}
