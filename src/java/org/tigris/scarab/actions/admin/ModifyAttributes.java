package org.tigris.scarab.actions.admin;

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

import java.util.Vector;

// Velocity Stuff 
import org.apache.turbine.services.velocity.*; 
import org.apache.velocity.*; 
import org.apache.velocity.context.*; 
// Turbine Stuff 
import org.apache.turbine.util.*;
import org.apache.turbine.modules.*;
import org.apache.turbine.modules.actions.*;
import org.apache.turbine.services.db.om.StringKey;
import org.apache.turbine.services.db.om.ObjectKey;
import org.apache.turbine.services.db.om.NumberKey;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.services.intake.model.Field;
import org.apache.turbine.services.intake.model.BooleanField;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.services.pull.TurbinePull;
// Scarab Stuff
import org.tigris.scarab.actions.base.*;
import org.tigris.scarab.om.*;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
    This class will store the form data for a project modification
        
    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: ModifyAttributes.java,v 1.10 2001/06/29 02:45:33 jon Exp $
*/
public class ModifyAttributes extends VelocityAction
{
    /**
     * On the admin,attribute-show.vm page, when you click the button,
     * this will get the right Attribute from the database and put it into
     * the $scarabR tool.
     */
    public void doSelectattribute( RunData data, Context context ) 
        throws Exception
    {
        String template = data.getParameters()
            .getString(ScarabConstants.TEMPLATE, null);
        String nextTemplate = data.getParameters().getString(
            ScarabConstants.NEXT_TEMPLATE, template );

        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);

        Field id = intake.get("Attribute", IntakeTool.DEFAULT_KEY).get("Id");
        id.setRequired(true);
        if ( id.isValid() ) 
        {
            setTemplate(data, nextTemplate);                
        }
    }

    /**
     * If someone wants to edit the attributes, handle the clicking
     * of the button.
     */
    public void doModifyattributeoptions( RunData data, Context context )
        throws Exception
    {
        String template = data.getParameters()
            .getString(ScarabConstants.TEMPLATE, null);
        String nextTemplate = data.getParameters().getString(
            ScarabConstants.NEXT_TEMPLATE, template );

        setTemplate(data, nextTemplate);
    }

    /**
     * Used on AttributeEdit.vm to change the attribute type for 
     * an Attribute.
     */
    public void doModifyattributetype( RunData data, Context context )
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
           .get(ScarabConstants.INTAKE_TOOL);

        if ( intake.isAllValid() )
        {
            Group attribute = intake.get("Attribute", IntakeTool.DEFAULT_KEY);
            String attributeID = attribute.get("Id").toString();
            Group attributeType = intake.get("AttributeType", IntakeTool.DEFAULT_KEY);
            String attributeTypeID = attributeType.get("AttributeTypeId").toString();

            Attribute attr = Attribute.getInstance((ObjectKey)new NumberKey(attributeID));
            attr.setTypeId(new NumberKey(attributeTypeID));
            attr.save();
        }
    }

    /**
     * Used on AttributeEdit.vm to change the attribute name for
     * an Attribute.
     */
    public void doModifyattributename( RunData data, Context context )
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
           .get(ScarabConstants.INTAKE_TOOL);

        if ( intake.isAllValid() )
        {
            Group attribute = intake.get("Attribute", IntakeTool.DEFAULT_KEY);
            String attributeID = attribute.get("Id").toString();
            String attributeName = attribute.get("Name").toString();

            Attribute attr = Attribute.getInstance((ObjectKey)new NumberKey(attributeID));
            attr.setName(attributeName);
            attr.save();
        }
    }

    /**
     * Used on AttributeEditOptions.vm to change the name of an existing
     * AttributeOption or add a new one if the name doesn't already exist.
     */
    public synchronized void 
        doAddormodifyattributeoptions( RunData data, Context context )
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
           .get(ScarabConstants.INTAKE_TOOL);

        if ( intake.isAllValid() ) 
        {
            Attribute attribute = ((ScarabRequestTool)context
                .get(ScarabConstants.SCARAB_REQUEST_TOOL)).getAttribute();

            AttributeOption option = null;
            Vector attributeOptions = (Vector)attribute
                .getAttributeOptions().clone(); 
            // go in reverse because we may be removing from the list
            for (int i=attributeOptions.size()-1; i>=0; i--) 
            {
                option = (AttributeOption)attributeOptions.get(i);
                Group group = intake.get("AttributeOption", 
                                         option.getQueryKey());
                // in case the template is not showing all the options at once
                if ( group != null ) 
                {
                    group.setProperties(option);

                    // check for a deleted flag.  AttributeOptions are removed
                    // from the db when deleted.
                    BooleanField deletedField = 
                        (BooleanField)group.get("Deleted");
                    if ( deletedField != null && deletedField.booleanValue() ) 
                    {
                        // remove from the Attribute's list
                        attributeOptions.remove(i);
                    }
                    option.save();

                    // we need this because we are accepting duplicate
                    // numeric values and resorting, so we do not want
                    // to show the actual value entered by the user.
                    intake.remove(group);
                }                
            }
            attribute.sortOptions(attributeOptions);

            // was a new option added?
            option = new AttributeOption();
            Group group = intake.get("AttributeOption", 
                                     option.getQueryKey());
            if ( group != null ) 
            {
                group.setProperties(option);
                if ( option.getName() != null 
                     && option.getName().length() != 0 ) 
                {
                    try
                    {
                        attribute.addAttributeOption(option);
                    }
                    catch (ScarabException se)
                    {
                        group.get("Name")
                            .setMessage("Please select a unique name.");
                    }
                }

                // we need this because we are accepting duplicate
                // numeric values and resorting, so we do not want
                // to show the actual value entered by the user.
                intake.remove(group);

                for (int i=attributeOptions.size()-1; i>=0; i--) 
                {
                    option = (AttributeOption)attributeOptions.get(i);
                    group = intake.get("AttributeOption", 
                                             option.getQueryKey());
                    // in case the template is not showing all the options
                    if ( group != null ) 
                    {
                        intake.remove(group);
                    }
                }
            }                           
        }
    }

    /**
     * Manages clicking of the AllDone button
     */
    public void doAlldone( RunData data, Context context ) throws Exception
    {
        String nextTemplate = data.getParameters().getString(
            ScarabConstants.NEXT_TEMPLATE );

        setTemplate(data, nextTemplate);
    }
    
    /**
        This manages clicking the cancel button
    */
    public void doCancel( RunData data, Context context ) throws Exception
    {
        data.setMessage("Changes were not saved!");
    }
    
    /**
        does nothing.
    */
    public void doPerform( RunData data, Context context ) throws Exception
    {
        doCancel(data, context);
    }
}
