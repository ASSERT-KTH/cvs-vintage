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

import java.util.*;

import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ParameterParser;

import org.tigris.scarab.baseom.*;
import org.tigris.scarab.baseom.peer.*;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.om.peer.BasePeer;
import com.workingdogs.village.Record;

import org.tigris.scarab.baseom.*;

/** 
 * All attributes must extend this class. All attributes have a name.
 * @author <a href="mailto:fedor.karpelevitch@home.com">Fedor</a>
 * @version $Revision: 1.2 $ $Date: 2001/01/23 22:33:44 $
 */
public abstract class Attribute
{
    //    private int id = -1;
    //    private String name;
    
    private ScarabAttribute scarabAttribute;
    private ScarabIssue scarabIssue;
    protected ScarabIssueAttributeValue scarabIssueAttributeValue;

    private String controlName;
    protected boolean loaded = false;

    
    
    /*
     *  this is cache of Attribute resources key is Attribute_id
     *  and value is an array with three elements:
     *   0: class name
     *   1: attribute name
     *   2: attribute-specific resources such as vector of Options for OptionAttribute
     *
     */
    private static Hashtable resources = new Hashtable();
    
    /** Creates a new attribute. Do not do anything here.
     * All initialization should be performed in init().
     */
    protected Attribute()
    {
    }

    /** Creates, initializes and returns a new Attribute.
     * @return new Attribute instance
     * @param issue Isuue object which this attribute is associated with
     * @param intId This Attribute's Id
     */
    public static synchronized Attribute getInstance(
        ScarabIssueAttributeValue siav) throws Exception
    {
        int intId = siav.getAttributeId();
        Integer id = new Integer(intId);
        ScarabIssue issue = siav.getScarabIssue();
        boolean firstTime;
        
        ScarabAttribute scarabAttribute;
        Object[] res = new Object[2];

        if ((firstTime=!resources.containsKey(id)))
        {
            System.out.println("First time for Attribute: " + id);
            scarabAttribute = ScarabAttributePeer.retrieveByPK(intId);
            if ( scarabAttribute == null) // is this check needed?
            {
                throw new Exception("Attribute with ID " + id + 
                                    " can not be found"); //FIXME
            }
            res[0] = scarabAttribute;
            resources.put(id,res);
        }
        else
        {
            System.out.println("Should save hit for Attribute: " + id);
            res = (Object[])resources.get(id);
            scarabAttribute = (ScarabAttribute)res[0];
        }

        String className = scarabAttribute
            .getScarabAttributeType().getJavaClassName();
        Attribute attr = (Attribute)Class.forName(className).newInstance();
        attr.setScarabAttribute(scarabAttribute);
        attr.setScarabIssue(issue);
        attr.controlName = new StringBuffer("attr")
            .append(intId)
            .append("_")
            .append(issue.getPrimaryKey())
            .toString();
        if (firstTime)
        {
            res[1] = attr.loadResources();
        }
        attr.setResources(res[1]);
        attr.setScarabIssueAttributeValue(siav);
        attr.init();
        return attr;
    }

    /** Creates, initializes and returns a new Attribute.
     * @return new Attribute instance
     * @param issue Isuue object which this attribute is associated with
     * @param intId This Attribute's Id
     */
    public static synchronized Attribute getInstance(
        ScarabRModuleAttribute srma, ScarabIssue issue) throws Exception
    {
        return getInstance(srma.getAttributeId(), issue);
    }

    /** Creates, initializes and returns a new Attribute.
     * @return new Attribute instance
     * @param issue Isuue object which this attribute is associated with
     * @param intId This Attribute's Id
     */
    public static synchronized Attribute getInstance(int intId, 
        ScarabIssue issue) throws Exception
    {
        Integer id = new Integer(intId);
        boolean firstTime;
        
        ScarabAttribute scarabAttribute;
        Object[] res = new Object[2];

        if ((firstTime=!resources.containsKey(id)))
        {
            System.out.println("First time for Attribute: " + id);
            scarabAttribute = ScarabAttributePeer.retrieveByPK(intId);
            if ( scarabAttribute == null) // is this check needed?
            {
                throw new Exception("Attribute with ID " + id + 
                                    " can not be found"); //FIXME
            }
            res[0] = scarabAttribute;
            resources.put(id,res);
        }
        else
        {
            System.out.println("Should save hit for Attribute: " + id);
            res = (Object[])resources.get(id);
            scarabAttribute = (ScarabAttribute)res[0];
        }

        String className = scarabAttribute
            .getScarabAttributeType().getJavaClassName();
        Attribute attr = (Attribute)Class.forName(className).newInstance();
        attr.setScarabAttribute(scarabAttribute);
        attr.setScarabIssue(issue);
        attr.controlName = new StringBuffer("attr")
            .append(intId)
            .append("_")
            .append(issue.getPrimaryKey())
            .toString();
        if (firstTime)
        {
            res[1] = attr.loadResources();
        }
        attr.setResources(res[1]);
        attr.init();
        return attr;
    }
    
    /** Loads from database data specific for this Attribute including Name.
     * These are data common to all Attribute instances with same id.
     * Data retrieved here will then be used in setResources.
     * @return Object containing Attribute resources which will be used in setResources.
     */
    protected abstract Object loadResources() throws Exception;
    
    /** this method is used by an Attribute instance
     * to obtain specific resources such as option list for SelectOneAttribute.
     * It may, for example put them into instance variables.
     * Attribute may use common resources as-is or create it's own
     * resources based on common, it should not, however, modify common resources
     * since they will be used by other Attribute instances.
     *
     * @param resources Resources common for Attributes with the specified id.
     */
    protected abstract void setResources(Object resources);
    

    
    /**
     * Get the value of scarabAttribute.
     * @return value of scarabAttribute.
     */
    public ScarabAttribute getScarabAttribute() 
    {
        return scarabAttribute;
    }
    
    /**
     * Set the value of scarabAttribute.
     * @param v  Value to assign to scarabAttribute.
     */
    private void setScarabAttribute(ScarabAttribute  v) 
    {
        this.scarabAttribute = v;
    }
    /**
     * Get the value of scarabIssueAttributeValue.
     * @return value of scarabIssueAttributeValue.
     */
    public ScarabIssueAttributeValue getScarabIssueAttributeValue() 
    {
        return scarabIssueAttributeValue;
    }
    
    /**
     * Set the value of scarabIssueAttributeValue.
     * @param v  Value to assign to scarabIssueAttributeValue.
     */
    public void setScarabIssueAttributeValue(ScarabIssueAttributeValue  v) 
    {
        this.scarabIssueAttributeValue = v;
    }
    

    
    /** Override this method if you need any initialization for this attr.
     * @throws Exception Generic Exception
     */
    public abstract void init() throws Exception;
    
    /*
    private void setId(int id)
    {
        this.id = id;
    }
    /** returns this Attribute's id
     * @return this Attribute's id
     
    public int getId()
    {
        return id;
    }
    */
    
    /** Gets the Name attribute of the Attribute object
     * @return The Name value
     */
    public String getName()
    {
        return scarabAttribute.getName();
    }

    /** displays the attribute.
     * @return Object to display the property. May be a String containing HTML
     * @param data app data. may be needed to render control
     * differently in different circumstances.
     * Not sure about this though. It may be a better
     * idea to handle this on the UI level.
     */
    public abstract Object show(RunData data);
    
    /** 
     * returns ScarabIssue object this attribute belongs to
     * @return ScarabIssue object
     */
    public ScarabIssue getScarabIssue()
    {
        return scarabIssue;
    }
    /**
     * Set the value of scarabIssueAttributeValue.
     * @param v  Value to assign to scarabIssueAttributeValue.
     */
    private void setScarabIssue(ScarabIssue  v) 
    {
        this.scarabIssue = v;
    }

    /** Updates both InternalValue and Value of the Attribute object and saves them
     * to database
     * @param newValue String representation of new value.
     * @param data app data. May be needed to get user info for votes and/or for security checks.
     * @throws Exception Generic exception
     * 
     */
    public abstract void setValue(String newValue, RunData data) throws Exception;
    
    /**
     *  Gets the Value attribute of the Attribute object
     *
     * @return    The Value value
     */
    public abstract String getValue();

    /** generates HTML form control name which represents this value
     * @return control name
     */
    public String getControlName()
    {
        // TODO: control name can be scrambled for security.
        return controlName;
    }

/*
    public String getQueryKey()
    {
        StringBuffer qs = new StringBuffer("Attribute[");
        if ( !scarabAttribute.isNew() ) 
        {
            qs.append(scarabAttribute.getId().toString());
        }
        return qs.append("]").toString();
    }
*/
    public String getQueryKey()
    {
        return scarabIssueAttributeValue.getQueryOID();
    }

    public boolean supportsVoting()
    {
        return false;
    }

    public static Vector getAttributes(ParameterParser pp) 
        throws Exception
    {
        Vector sAttValues = ScarabIssueAttributeValue
            .getScarabIssueAttributeValues(pp);
        Vector attValues = new Vector(sAttValues.size());
        for ( int i=0; i<sAttValues.size(); i++) 
        {
            attValues.add( getInstance( 
                (ScarabIssueAttributeValue) sAttValues.get(i) ));
        }
        return attValues;
    }
}
