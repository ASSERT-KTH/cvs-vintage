package org.tigris.scarab.om;

import org.apache.torque.util.Criteria;
import org.apache.torque.TorqueException;
import org.tigris.scarab.util.Log;

import java.util.List;

/**
 * Exposes additional methods to manage torque-generated Transition objects.
 */
public class TransitionPeer extends org.tigris.scarab.om.BaseTransitionPeer
{
    public static List getAllTransitions(Attribute attribute)
    {

        Integer attribId = attribute.getAttributeId();
        List transitions = null;
        try
        {
            Criteria crit = new Criteria();
            crit.add(ATTRIBUTE_ID, attribId);
            transitions = doSelect(crit);
        }
        catch (TorqueException te)
        {
            te.printStackTrace();
        }
        return transitions;
    }
    /**
     * Obtains every allowed transition from fromOption to toOption, including
     * those implicited by a null value in fromOption and/or toOption.
     * 
     * @param fromOption
     * @param toOption
     * @return
     */
    public static List getTransitions(AttributeOption fromOption,
            AttributeOption toOption)
    {

        Integer attribId = toOption.getAttributeId();
        Integer fromOptionId = fromOption.getOptionId();
        Integer toOptionId = toOption.getOptionId();
        List transitions = null;
        try
        { // "Open" transitions (null->null) must always be added if existing
            Criteria crit = new Criteria();
            crit.add(ATTRIBUTE_ID, attribId);
            crit.add(FROM_OPTION_ID, (Object) "FROM_OPTION_ID IS NULL",
                    Criteria.CUSTOM);
            crit.add(TO_OPTION_ID, (Object) "TO_OPTION_ID IS NULL",
                    Criteria.CUSTOM);
            transitions = doSelect(crit);
        }
        catch (TorqueException te)
        {
            Log.get(TransitionPeer.class.getName()).error(
                    "getTransitions(): " + te);
        }
        try
        { // Asked transition
            Criteria crit = new Criteria();
            crit.add(ATTRIBUTE_ID, attribId);
            crit.add(FROM_OPTION_ID, fromOptionId);
            crit.add(TO_OPTION_ID, toOptionId);
            transitions.addAll(doSelect(crit));
        }
        catch (TorqueException te)
        {
            Log.get(TransitionPeer.class.getName()).error(
                    "getTransitions(): " + te);
        }
        try
        { // Open-beginning
            Criteria crit = new Criteria();
            crit.add(ATTRIBUTE_ID, attribId);
            crit.add(TO_OPTION_ID, toOptionId);
            crit.add(FROM_OPTION_ID, (Object) "FROM_OPTION_ID IS NULL",
                    Criteria.CUSTOM);
            transitions.addAll(doSelect(crit));
        }
        catch (TorqueException te)
        {
            Log.get(TransitionPeer.class.getName()).error(
                    "getTransitions(): Looking for nulls fromOption: " + te);
        }
        try
        { // Open-ending
            Criteria crit = new Criteria();
            crit.add(ATTRIBUTE_ID, attribId);
            crit.add(FROM_OPTION_ID, fromOptionId);
            crit.add(TO_OPTION_ID, (Object) "TO_OPTION_ID IS NULL",
                    Criteria.CUSTOM);
            transitions.addAll(doSelect(crit));
        }
        catch (TorqueException te)
        {
            Log.get(TransitionPeer.class.getName()).error(
                    "getTransitions(): Looking for nulls toOption: " + te);
        }
        return transitions;
    }
    /**
     * If there is any defined transitions for the given attribute, it will
     * return true. It's needed because the CheapWorkflow system will not try to
     * restrict the transitions if there is none defined.
     * 
     * @param attribute
     * @return
     */
    public static boolean hasDefinedTransitions(Attribute attribute)
    {
        boolean result = false;
        try
        {
            Criteria crit = new Criteria();
            crit.add(ATTRIBUTE_ID, attribute.getAttributeId());
            List attributeTransitions = doSelect(crit);
            result = (attributeTransitions.size() > 0);
        }
        catch (TorqueException te)
        {
            Log.get(TransitionPeer.class.getName()).error(
                    "hasDefinedTransitions(): " + te);
        }
        return result;
    }

}

