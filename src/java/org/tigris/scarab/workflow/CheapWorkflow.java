package org.tigris.scarab.workflow;

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
}
