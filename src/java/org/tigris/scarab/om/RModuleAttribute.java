package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000-2004 CollabNet.  All rights reserved.
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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;

// Turbine classes
import org.apache.torque.TorqueException;
import org.apache.torque.om.Persistent;
import org.apache.torque.util.Criteria;
import org.apache.fulcrum.localization.Localization;

import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.IssueTypePeer;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.workflow.WorkflowFactory;

/** 
 * This class represents a RModuleAttribute relationship.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: RModuleAttribute.java,v 1.50 2004/12/27 22:43:35 jorgeuriarte Exp $
 */
public class RModuleAttribute 
    extends BaseRModuleAttribute
    implements Persistent, Conditioned
{
    private static final String R_MODULE_ATTTRIBUTE = 
        "RModuleAttribute";
    private static final String GET_RMAS = 
        "getRMAs";

    public void save(Connection con) throws TorqueException
    {
        if (isModified())
        {
            if (isNew())
            {
                super.save(con);
            }
            else
            { 
                RIssueTypeAttribute ria = null;
                try
                {
                    ria = getIssueType().getRIssueTypeAttribute(getAttribute());
                    if ((ria != null && ria.getLocked()))
                    {
                    throw new TorqueException(getAttribute().getName() + " is locked"); //EXCEPTION
                    }
                    else
                    {
                        super.save(con);
                    }
                }
                catch (Exception e)
                {
                    throw new TorqueException("An error has occurred.", e); //EXCEPTION
                }
            }
        }
    }

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>getModule()</code> instead.
     *
     * @return a <code>ScarabModule</code> value
     */
    public ScarabModule getScarabModule()
    {
        throw new UnsupportedOperationException(
            "Should use getModule"); //EXCEPTION
    }

    /**
     * Throws UnsupportedOperationException.  Use
     * <code>setModule(Module)</code> instead.
     *
     */
    public void setScarabModule(ScarabModule module)
    {
        throw new UnsupportedOperationException(
            "Should use setModule(Module). Note module cannot be new."); //EXCEPTION
    }

    /**
     * Use this instead of setScarabModule.  Note: module cannot be new.
     */
    public void setModule(Module me)
        throws TorqueException
    {
        Integer id = me.getModuleId();
        if (id == null) 
        {
            throw new TorqueException("Modules must be saved prior to " +
                                      "being associated with other objects."); //EXCEPTION
        }
        setModuleId(id);
    }

    /**
     * Module getter.  Use this method instead of getScarabModule().
     *
     * @return a <code>Module</code> value
     */
    public Module getModule()
        throws TorqueException
    {
        Module module = null;
        Integer id = getModuleId();
        if ( id != null ) 
        {
            module = ModuleManager.getInstance(id);
        }
        
        return module;
    }

    /**
     * Get the Display Value for the attribute.  In the event that this
     * is a new RModuleAttribute that has not been assigned a Display 
     * Value, this method will return the Attribute Name.
     */
    public String getDisplayValue()
    {
        String dispVal = super.getDisplayValue();
        if (dispVal == null) 
        {
            try
            {
                dispVal = getAttribute().getName();
            }
            catch (Exception e)
            {
                getLog().error(e);
                dispVal = "!Error-Check Logs!";
            }
        }
        return dispVal;
    }

    public void delete()
         throws Exception
    {                
         delete(false);
    }

    protected void delete(boolean overrideLock)
         throws Exception
    {                
        Module module = getModule();

            IssueType issueType = IssueTypeManager
               .getInstance(getIssueTypeId(), false);
            if (issueType.getLocked() && !overrideLock)
            { 
                throw new ScarabException(L10NKeySet.CannotDeleteAttributeFromLockedIssueType);
            }            
            else
            {
                Criteria c = new Criteria()
                    .add(RModuleAttributePeer.MODULE_ID, getModuleId())
                    .add(RModuleAttributePeer.ISSUE_TYPE_ID, getIssueTypeId())
                    .add(RModuleAttributePeer.ATTRIBUTE_ID, getAttributeId());
                RModuleAttributePeer.doDelete(c);
                Attribute attr = getAttribute();
                String attributeType = null;
                attributeType = (attr.isUserAttribute() ? Module.USER : Module.NON_USER);
                module.getRModuleAttributes(getIssueType(), false, attributeType)
                                            .remove(this);
                WorkflowFactory.getInstance().deleteWorkflowsForAttribute(
                                              attr, module, getIssueType());

                // delete module-user-attribute mappings
                Criteria crit = new Criteria()
                    .add(RModuleUserAttributePeer.ATTRIBUTE_ID, 
                         attr.getAttributeId())
                    .add(RModuleUserAttributePeer.MODULE_ID, 
                         getModuleId())
                    .add(RModuleUserAttributePeer.ISSUE_TYPE_ID, 
                         getIssueTypeId());
                RModuleUserAttributePeer.doDelete(crit);

                // delete module-option mappings
                if (attr.isOptionAttribute())
                {
                    List optionList = module.getRModuleOptions(attr, 
                                      IssueTypePeer.retrieveByPK(getIssueTypeId()), 
                                      false);
                    if (optionList != null && !optionList.isEmpty())
                    {
                        ArrayList optionIdList =
                            new ArrayList(optionList.size());
                        for (int i = 0; i < optionList.size(); i++)
                        { 
                            optionIdList.add(((RModuleOption)
                                              optionList.get(i))
                                             .getOptionId());
                        }
                        Criteria c2 = new Criteria()
                            .add(RModuleOptionPeer.MODULE_ID, getModuleId())
                            .add(RModuleOptionPeer.ISSUE_TYPE_ID,
                                 getIssueTypeId())
                            .addIn(RModuleOptionPeer.OPTION_ID, optionIdList);
                        RModuleOptionPeer.doDelete(c2);
                    }
                }
            }

            RModuleAttributeManager.removeInstanceFromCache(this);
    }


    private static List getRMAs(Integer moduleId, Integer issueTypeId)
        throws Exception
    {
        List result = null;
        Object obj = ScarabCache.get(R_MODULE_ATTTRIBUTE, GET_RMAS, 
                                     moduleId, issueTypeId); 
        if (obj == null) 
        {        
            Criteria crit = new Criteria()
                .add(RModuleAttributePeer.MODULE_ID, moduleId)
                .add(RModuleAttributePeer.ISSUE_TYPE_ID, issueTypeId);
            crit.addAscendingOrderByColumn(
                RModuleAttributePeer.PREFERRED_ORDER);
            result = RModuleAttributePeer.doSelect(crit);
            ScarabCache.put(result, R_MODULE_ATTTRIBUTE, GET_RMAS, 
                            moduleId, issueTypeId);
        }
        else 
        {
            result = (List)obj;
        }
        return result;
    }

    /**
     * if this RMA is the chosen attribute for email subjects then return
     * true.  if not explicitly chosen, check the other RMA's for this module
     * and if none is chosen as the email attribute, choose the highest
     * ordered text attribute.
     *
     * @return a <code>boolean</code> value
     */
    public boolean getIsDefaultText()
        throws Exception
    {
        boolean isDefault = getDefaultTextFlag();
        if (!isDefault && getAttribute().isTextAttribute()) 
        {
            // get related RMAs
            List rmas = getRMAs(getModuleId(), getIssueTypeId());
            
            // check if another is chosen
            boolean anotherIsDefault = false;
            for (int i=0; i<rmas.size(); i++) 
            {
                RModuleAttribute rma = (RModuleAttribute)rmas.get(i);
                if (rma.getDefaultTextFlag()) 
                {
                    anotherIsDefault = true;
                    break;
                }
            }
            
            if (!anotherIsDefault) 
            {
                // locate the default text attribute
                for (int i=0; i<rmas.size(); i++) 
                {
                    RModuleAttribute rma = (RModuleAttribute)rmas.get(i);
                    if (rma.getAttribute().isTextAttribute()) 
                    {
                        if (rma.getAttributeId().equals(getAttributeId())) 
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

    /**
     * This method sets the defaultTextFlag property and also makes sure 
     * that no other related RMA is defined as the default.  It should be
     * used instead of setDefaultTextFlag in application code.
     *
     * @param b a <code>boolean</code> value
     */
    public void setIsDefaultText(boolean b)
        throws Exception
    {
        if (b && !getDefaultTextFlag()) 
        {
            // get related RMAs
            List rmas = getRMAs(getModuleId(), getIssueTypeId());
            
            // make sure no other rma is selected
            for (int i=0; i<rmas.size(); i++) 
            {
                RModuleAttribute rma = (RModuleAttribute)rmas.get(i);
                if (rma.getDefaultTextFlag()) 
                {
                    rma.setDefaultTextFlag(false);
                    rma.save();
                    break;
                }
            }
        }
        setDefaultTextFlag(b);
    }
    
    public List getConditions() throws TorqueException
    {
        if (collConditions == null)
        {
            Criteria crit = new Criteria();
            crit.add(ConditionPeer.ATTRIBUTE_ID, this.getAttributeId());
            crit.add(ConditionPeer.MODULE_ID, this.getModuleId());
            crit.add(ConditionPeer.ISSUE_TYPE_ID, this.getIssueTypeId());
            crit.add(ConditionPeer.TRANSITION_ID, new Integer(0));
            collConditions = getConditions(crit);
        }
        return collConditions;
    }
    
    /**
     * Returns the array of attributeOptionIds that will force the requiment of this
     * attribute if set. Used by templates to load the combo.
     * @return
     */
    public Integer[] getConditionsArray()
    {
        List conditions = new ArrayList();
        Integer[] aIDs = null;
        try
        {
            conditions = this.getConditions();
            aIDs = new Integer[conditions.size()];
            int i=0;
            for (Iterator iter = conditions.iterator(); iter.hasNext(); i++)
            {
                aIDs[i] = ((Condition)iter.next()).getOptionId();
            }
        }
        catch (TorqueException e)
        {
            this.getLog().error("getConditionsArray: " + e);
        }
        return aIDs;
    }
    /**
     * Load the attribute options' IDs from the template combo.
     * @param aOptionId
     * @throws Exception
     */
    public void setConditionsArray(Integer aOptionId[]) throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(ConditionPeer.ATTRIBUTE_ID, this.getAttributeId());
        crit.add(ConditionPeer.MODULE_ID, this.getModuleId());
        crit.add(ConditionPeer.ISSUE_TYPE_ID, this.getIssueTypeId());
        crit.add(ConditionPeer.TRANSITION_ID, new Integer(0));
        ConditionPeer.doDelete(crit);
        this.getConditions().clear();
        ConditionManager.clear();
        if (aOptionId != null)
        {
	        for (int i=0; i<aOptionId.length; i++)
	        {
	            if (aOptionId[i].intValue() != 0)
	            {
		            Condition cond = new Condition();
		            cond.setAttribute(this.getAttribute());
		            cond.setOptionId(aOptionId[i]);
		            cond.setTransitionId(new Integer(0));
		            cond.setIssueTypeId(this.getIssueTypeId());
		            cond.setModuleId(this.getModuleId());
		            this.addCondition(cond);
		            cond.save();
	            }
	        }
        }
    }
    /**
     * Return true if the given attributeOptionId will make the current
     * attribute required.
     * @param optionID
     * @return
     * @throws TorqueException
     */
    public boolean isRequiredIf(Integer optionID) throws TorqueException
    {
        Condition condition = new Condition();
        condition.setAttribute(this.getAttribute());
        condition.setModuleId(this.getModuleId());
        condition.setIssueTypeId(this.getIssueTypeId());
        condition.setTransitionId(new Integer(0));
        condition.setOptionId(optionID);
        return this.getConditions().contains(condition);
    }
    
    /**
     * Returns true if this object is conditioned (has related conditions)
     * @return
     */
    public boolean isConditioned()
    {
        boolean bRdo = false;
        try {
        	bRdo = this.getConditions().size()>0;
        } catch (TorqueException te)
        {
            // Nothing to do
        }
        return bRdo;
    }
}
