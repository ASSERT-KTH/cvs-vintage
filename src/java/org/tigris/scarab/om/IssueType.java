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
import java.util.Iterator;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;
import org.apache.torque.TorqueException;
import org.apache.torque.manager.MethodResultCache;

import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.util.ScarabException;

/** 
 * This class represents an IssueType.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: IssueType.java,v 1.26 2002/09/12 00:59:09 elicia Exp $
 */
public  class IssueType 
    extends org.tigris.scarab.om.BaseIssueType
    implements Persistent
{
    private static final String ISSUE_TYPE = 
        "IssueType";
    private static final String GET_TEMPLATE_ISSUE_TYPE = 
        "getTemplateIssueType";
    private static final String GET_INSTANCE = 
        "getInstance";
    protected static final String GET_R_ISSUETYPE_ATTRIBUTES = 
        "getRIssueTypeAttributes";
    protected static final String GET_R_ISSUETYPE_OPTIONS = 
        "getRIssueTypeOptions";
    protected static final String GET_ALL_R_ISSUETYPE_OPTIONS = 
        "getAllRIssueTypeOptions";

    public static final NumberKey ISSUE__PK = new NumberKey("1");
    public static final NumberKey USER_TEMPLATE__PK = new NumberKey("2");
    public static final NumberKey MODULE_TEMPLATE__PK = new NumberKey("3");
    static final String USER = "user";
    static final String NON_USER = "non-user";

    /**
     * Gets the IssueType template for this IssueType. The template
     * is a special type of IssueType.
     */
    public IssueType getTemplateIssueType()
        throws Exception
    {
        IssueType result = null;
        Object obj = ScarabCache.get(this, GET_TEMPLATE_ISSUE_TYPE); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria();
            crit.add(IssueTypePeer.PARENT_ID, getIssueTypeId());
            List results = (List)IssueTypePeer.doSelect(crit);
            if (results.isEmpty() || results.size()>1 )
            {
                throw new ScarabException("There has been an error.");
            }
            else
            {
                result = (IssueType)results.get(0);
            }
            ScarabCache.put(result, this, GET_TEMPLATE_ISSUE_TYPE);
        }
        else 
        {
            result = (IssueType)obj;
        }
        return result;
    }

    /**
     * Gets the id of the template that corresponds to the issue type.
     */
    public NumberKey getTemplateId()
        throws Exception
    {
        return getTemplateIssueType().getIssueTypeId();
    }        

    /**
     * Get the IssueType using a issue type name
     */
    public static IssueType getInstance(String issueTypeName)
        throws Exception
    {
        IssueType result = null;
        Object obj = ScarabCache.get(ISSUE_TYPE, GET_INSTANCE, issueTypeName); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria();
            crit.add(IssueTypePeer.NAME, issueTypeName);
            List issueTypes = (List)IssueTypePeer.doSelect(crit);
            if(issueTypes == null || issueTypes.size() == 0 )
            {
                throw new ScarabException("Invalid issue type: " +
                                          issueTypeName);
            }
            result = (IssueType)issueTypes.get(0);
            ScarabCache.put(result, ISSUE_TYPE, GET_INSTANCE, issueTypeName);
        }
        else 
        {
            result = (IssueType)obj;
        }
        return result;
    }

    /**
     * Copy the IssueType and its corresponding template type 
     */
    public IssueType copyIssueType()
        throws Exception
    {
        IssueType newIssueType = new IssueType();
        newIssueType.setName(getName() + " (copy)");
        newIssueType.setDescription(getDescription());
        newIssueType.setParentId(new NumberKey(0));
        newIssueType.save();
        IssueType template = (IssueType)IssueTypePeer
              .retrieveByPK(getTemplateId());
        IssueType newTemplate = new IssueType();
        newTemplate.setName(template.getName());
        newTemplate.setParentId(newIssueType.getIssueTypeId());
        newTemplate.save();
        return newIssueType;
    }

    /**
     * Delete mappings with all modules
     */
    public void deleteModuleMappings(ScarabUser user)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(RModuleIssueTypePeer.ISSUE_TYPE_ID, 
                 getIssueTypeId());
        List rmits = RModuleIssueTypePeer.doSelect(crit);
        for (int i=0; i<rmits.size(); i++)
        {
            RModuleIssueType rmit = (RModuleIssueType)rmits.get(i);
            rmit.delete(user);
        }
        ScarabCache.clear();
    }


    /**
     * Create default groups upon issue type creation.
     */
    public void createDefaultGroups()
        throws Exception
    {
        AttributeGroup ag = createNewGroup();
        ag.setOrder(1);
        ag.setDedupe(true);
        ag.setDescription(null);
        ag.save();
        AttributeGroup ag2 = createNewGroup();
        ag2.setOrder(3);
        ag2.setDedupe(false);
        ag2.setDescription(null);
        ag2.save();
    }

    /**
     * List of attribute groups associated with this issue type.
     */
    public List getAttributeGroups(boolean activeOnly)
        throws Exception
    {
        List groups = null;
        Boolean activeBool = activeOnly ? Boolean.TRUE : Boolean.FALSE;
        Criteria crit = new Criteria()
            .add(AttributeGroupPeer.ISSUE_TYPE_ID, getIssueTypeId())
            .add(AttributeGroupPeer.MODULE_ID, null)
            .addAscendingOrderByColumn(AttributeGroupPeer.PREFERRED_ORDER);
        if (activeOnly)
        {
            crit.add(AttributeGroupPeer.ACTIVE, true);
        }
        groups = AttributeGroupPeer.doSelect(crit);
        return groups;
    }

    /**
     * Creates new attribute group.
     */
    public AttributeGroup createNewGroup ()
        throws Exception
    {
        List groups = getAttributeGroups(false);
        AttributeGroup ag = new AttributeGroup();

        // Make default group name 'attribute group x' where x is size + 1
        ag.setName("Attribute group " + Integer.toString(groups.size()+1));
        ag.setActive(true);
        ag.setIssueTypeId(getIssueTypeId());
        if (groups.size() == 0)
        {
            ag.setDedupe(true);
            ag.setOrder(groups.size() +1);
        }
        else if (groups.size() == 1)
        {
            ag.setDedupe(false);
            ag.setOrder(groups.size() +2);
        }
        else
        {
            ag.setDedupe(false);
            ag.setOrder(groups.size() +1);
        }
        ag.save();
        groups.add(ag);
        return ag;
    }

    /**
     * Gets the sequence where the dedupe screen fits between groups.
     */
    public int getDedupeSequence()
        throws Exception
    {
        int sequence = 1;
        List groups = getAttributeGroups(false);
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
     * Gets associated attributes.
     */
    public List getRIssueTypeAttributes()
        throws TorqueException
    {
        List rias = null;
        try
        {
            rias = getRIssueTypeAttributes(false);
        }
        catch (Exception e)
        {
        }
        return rias;
    }

    /**
     * Gets associated attributes.
     */
    public List getRIssueTypeAttributes(boolean activeOnly)
        throws Exception
    {
        return getRIssueTypeAttributes(activeOnly, "all");
    }


    /**
     * Gets associated attributes.
     */
    public List getRIssueTypeAttributes(boolean activeOnly,
                                        String attributeType)
        throws Exception
    {
        List rias = null;
        Boolean activeBool = (activeOnly ? Boolean.TRUE : Boolean.FALSE);
        Object obj = getMethodResult().get(this, GET_R_ISSUETYPE_ATTRIBUTES, 
                                           activeBool, attributeType); 
        if ( obj == null ) 
        {        
            Criteria crit = new Criteria();
            crit.add(RIssueTypeAttributePeer.ISSUE_TYPE_ID, 
                     getIssueTypeId());
            crit.addAscendingOrderByColumn(
                RIssueTypeAttributePeer.PREFERRED_ORDER);
            
            if ( activeOnly )
            {
                crit.add(RIssueTypeAttributePeer.ACTIVE, true);
            }
            
            if (USER.equals(attributeType))
            {
                crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                         AttributeTypePeer.USER_TYPE_KEY);
                crit.addJoin(AttributePeer.ATTRIBUTE_ID,
                         RIssueTypeAttributePeer.ATTRIBUTE_ID); 
            }
            else if (NON_USER.equals(attributeType))
            {
                crit.addJoin(AttributePeer.ATTRIBUTE_ID,
                         RIssueTypeAttributePeer.ATTRIBUTE_ID); 
                crit.add(AttributePeer.ATTRIBUTE_TYPE_ID, 
                         AttributeTypePeer.USER_TYPE_KEY,
                         Criteria.NOT_EQUAL);
            }
            
            rias = RIssueTypeAttributePeer.doSelect(crit); 
            getMethodResult().put(rias, this, GET_R_ISSUETYPE_ATTRIBUTES, 
                                  activeBool, attributeType);
        }
        else 
        {
            rias = (List)obj;
        }
        return rias;
    }

    /**
     * Adds issuetype-attribute mapping to issue type.
     */
    public RIssueTypeAttribute addRIssueTypeAttribute(Attribute attribute)
        throws Exception
    {
        String attributeType = null;
        attributeType = (attribute.isUserAttribute() ? USER : NON_USER);

        RIssueTypeAttribute ria = new RIssueTypeAttribute();
        ria.setIssueTypeId(getIssueTypeId());
        ria.setAttributeId(attribute.getAttributeId());
        ria.setOrder(getLastAttribute(attributeType) + 1);
        ria.save();
        getRIssueTypeAttributes(false, attributeType).add(ria);
        return ria;
    }

    public RIssueTypeAttribute getRIssueTypeAttribute(Attribute attribute)
        throws Exception
    {
        RIssueTypeAttribute ria = null;
        List rias = null;
        if (attribute.isUserAttribute())
        {
            rias = getRIssueTypeAttributes(false, USER);
        }
        else
        {
            rias = getRIssueTypeAttributes(false, NON_USER);
        }
        Iterator i = rias.iterator();
        while ( i.hasNext() )
        {
            RIssueTypeAttribute tempRia = (RIssueTypeAttribute)i.next();
            if ( tempRia.getAttribute().equals(attribute) )
            {
                ria = tempRia;
                break;
            }
        }
        return ria;
    }

    /**
     * gets a list of all of the User Attributes in an issue type.
     */
    public List getUserAttributes()
        throws Exception
    {
        return getUserAttributes(true);
    }

    /**
     * gets a list of all of the User Attributes in an issue type.
     */
    public List getUserAttributes(boolean activeOnly)
        throws Exception
    {
        List rIssueTypeAttributes = getRIssueTypeAttributes(activeOnly, USER);
        List userAttributes = new ArrayList();

        for ( int i=0; i<rIssueTypeAttributes.size(); i++ )
        {
            Attribute att = ((RIssueTypeAttribute)rIssueTypeAttributes.get(i)).getAttribute();
            userAttributes.add(att);
        }
        return userAttributes;
    }

    /**
     * FIXME: can this be done more efficently?
     * gets highest sequence number for issueType-attribute map
     * so that a new RIssueTypeAttribute can be added at the end.
     */
    public int getLastAttribute(String attributeType)
        throws Exception
    {
        List itAttributes = getRIssueTypeAttributes(false, attributeType);
        int last = 0;

        for ( int i=0; i<itAttributes.size(); i++ )
        {
               int order = ((RIssueTypeAttribute) itAttributes.get(i))
                         .getOrder();
               if (order > last)
               {
                   last = order;
               }
        }
        return last;
    }


    /**
     * FIXME: can this be done more efficently?
     * gets highest sequence number for module-attribute map
     * so that a new RIssueTypeOption can be added at the end.
     */
    public int getLastAttributeOption(Attribute attribute)
        throws Exception
    {
        List issueTypeOptions = getRIssueTypeOptions(attribute);
        int last = 0;

        for ( int i=0; i<issueTypeOptions.size(); i++ )
        {
               int order = ((RIssueTypeOption) issueTypeOptions.get(i))
                         .getOrder();
               if (order > last)
               {
                   last = order;
               }
        }
        return last;
    }

    /**
     * Adds issuetype-attribute-option mapping to module.
     */
    public RIssueTypeOption addRIssueTypeOption(AttributeOption option)
        throws Exception
    {
        RIssueTypeOption rio = new RIssueTypeOption();
        rio.setIssueTypeId(getIssueTypeId());
        rio.setOptionId(option.getOptionId());
        rio.setOrder(getLastAttributeOption(option.getAttribute()) + 1);
        return rio;
    }

    /**
     * Gets associated attribute options.
     */
    public List getRIssueTypeOptions(Attribute attribute)
        throws Exception
    {
        return getRIssueTypeOptions(attribute, true);
    }

    /**
     * Gets associated attribute options.
     */
    public List getRIssueTypeOptions(Attribute attribute, boolean activeOnly)
        throws Exception
    {
        List allRIssueTypeOptions = null;
        allRIssueTypeOptions = getAllRIssueTypeOptions(attribute);

        if (allRIssueTypeOptions != null)
        {
            if ( activeOnly )
            {
                List activeRIssueTypeOptions =
                    new ArrayList(allRIssueTypeOptions.size());
                for ( int i=0; i<allRIssueTypeOptions.size(); i++ )
                {
                    RIssueTypeOption rio =
                        (RIssueTypeOption)allRIssueTypeOptions.get(i);
                    if ( rio.getActive() )
                    {
                        activeRIssueTypeOptions.add(rio);
                    }
                }
                allRIssueTypeOptions =  activeRIssueTypeOptions;
            }
        }
        return allRIssueTypeOptions;
    }
    

    private List getAllRIssueTypeOptions(Attribute attribute)
        throws Exception
    {
        List rIssueTypeOpts = null;
        Object obj = ScarabCache.get(this, GET_ALL_R_ISSUETYPE_OPTIONS, 
                                     attribute); 
        if ( obj == null ) 
        {        
            List options = attribute.getAttributeOptions(true);
            NumberKey[] optIds = null;
            if (options == null)
            {
                optIds = new NumberKey[0];
            }
            else
            {
                optIds = new NumberKey[options.size()];
            }
            for ( int i=optIds.length-1; i>=0; i-- )
            {
                optIds[i] = ((AttributeOption)options.get(i)).getOptionId();
            }
            
            if (optIds.length > 0)
            { 
                Criteria crit = new Criteria();
                crit.add(RIssueTypeOptionPeer.ISSUE_TYPE_ID, getIssueTypeId());
                crit.addIn(RIssueTypeOptionPeer.OPTION_ID, optIds);
                crit.addJoin(RIssueTypeOptionPeer.OPTION_ID, AttributeOptionPeer.OPTION_ID);
                crit.addAscendingOrderByColumn(RIssueTypeOptionPeer.PREFERRED_ORDER);
                crit.addAscendingOrderByColumn(AttributeOptionPeer.OPTION_NAME);
                rIssueTypeOpts = RIssueTypeOptionPeer.doSelect(crit);
            }
            ScarabCache.put(rIssueTypeOpts, this, GET_ALL_R_ISSUETYPE_OPTIONS, 
                            attribute);
        }
        else 
        {
            rIssueTypeOpts = (List)obj;
        }
        return rIssueTypeOpts;
    }

    public RIssueTypeOption getRIssueTypeOption(AttributeOption option)
        throws Exception
    {
        RIssueTypeOption rio = null;
        List rios = getRIssueTypeOptions(option.getAttribute(), false);
        Iterator i = rios.iterator();
        while ( i.hasNext() )
        {
            rio = (RIssueTypeOption)i.next();
            if ( rio.getAttributeOption().equals(option) )
            {
                break;
            }
        }

        return rio;
    }

    /**
     * Gets a list of all of the global Attributes that are not 
     * Associated with this issue type
     */
    public List getAvailableAttributes(String attributeType)
        throws Exception
    {
        List allAttributes = AttributePeer.getAttributes(attributeType);
        List availAttributes = new ArrayList();
        List rIssueTypeAttributes = getRIssueTypeAttributes(false,
                                                            attributeType);
            List attrs = new ArrayList();
            for ( int i=0; i<rIssueTypeAttributes.size(); i++ )
            {
                attrs.add(
                   ((RIssueTypeAttribute) rIssueTypeAttributes.get(i)).getAttribute());
            }
            for ( int i=0; i<allAttributes.size(); i++ )
            {
                Attribute att = (Attribute)allAttributes.get(i);
                if (!attrs.contains(att))
                {
                    availAttributes.add(att);
                }
            }
        return availAttributes;
    }

    private MethodResultCache getMethodResult()
    {
        return ModuleManager.getMethodResult();
    }
}
