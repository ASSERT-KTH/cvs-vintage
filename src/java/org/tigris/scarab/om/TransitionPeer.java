package org.tigris.scarab.om;

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

    public static void doDelete(Transition tran) throws TorqueException
    {
        Criteria crit = new Criteria();
        crit.add(ConditionPeer.TRANSITION_ID, tran.getTransitionId());
        ConditionPeer.doDelete(crit);
        BaseTransitionPeer.doDelete(tran);
    }
}

