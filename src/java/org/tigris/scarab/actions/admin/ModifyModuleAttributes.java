package org.tigris.scarab.actions.admin;

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

import java.util.Vector;
import java.util.List;

// Turbine Stuff 
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.BooleanField;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
 * action methods on RModuleAttribute table
 *      
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ModifyModuleAttributes.java,v 1.10 2001/08/28 00:51:55 jon Exp $
 */
public class ModifyModuleAttributes extends RequireLoginFirstAction
{
    /**
     * Used on ModuleAttributeEditor.vm to change the properties
     * of existing RModuleAttributes or to add a new one
     */
    public synchronized void 
        doModifyattributes( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
           .get(ScarabConstants.INTAKE_TOOL);

        ModuleEntity module = ((ScarabUser)data.getUser()).getCurrentModule();
        List rmas = (List)((Vector)module
            .getRModuleAttributes(false)).clone();

        // should set DisplayValue and Order as required. !FIXME!


        if ( intake.isAllValid() ) 
        {

            RModuleAttribute rma = null;
            for (int i=rmas.size()-1; i>=0; i--) 
            {
                rma = (RModuleAttribute)rmas.get(i);                
                Group group = intake.get("RModuleAttribute", 
                                         rma.getQueryKey(), false);
                // group should never be null, might want to remove 
                if ( group != null ) 
                {                    
                    if ( !rma.getModuleId().equals(module.getModuleId()) ) 
                    {
                        NumberKey attId = rma.getAttributeId();
                        rma = rma.copy();
                        rma.setAttributeId(attId);
                        rma.setModuleId(module.getModuleId());
                        rmas.remove(i);
                        rmas.add(i, rma);
                    }
                    group.setProperties(rma);
                    rma.save();

                    // we need this because we are accepting duplicate
                    // numeric values and resorting, so we do not want
                    // to show the actual value entered by the user.
                    intake.remove(group);
                }                
            }
            //module.sortAttributes(rmas);
        }
    }

    /**
     * Used on ModuleOptionEditor.vm to change the name of an existing
     * AttributeOption or add a new one.
     */
    public synchronized void 
        doAddormodifymoduleoptions( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = (IntakeTool)context
           .get(ScarabConstants.INTAKE_TOOL);
        ScarabRequestTool scarab = (ScarabRequestTool)context
           .get(ScarabConstants.SCARAB_REQUEST_TOOL);

        if ( intake.isAllValid() ) 
        {
            RModuleAttribute attribute = 
                scarab.getRModuleAttribute();
            ModuleEntity module = scarab.getUser().getCurrentModule();
            RModuleOption option = null;
            Vector attributeOptions = (Vector)((Vector)module
                .getRModuleOptions(attribute.getAttribute(), false)).clone(); 
            // go in reverse because we may be removing from the list
            for (int i=attributeOptions.size()-1; i>=0; i--) 
            {
                option = (RModuleOption)attributeOptions.get(i);
                Group group = intake.get("RModuleOption", 
                                         option.getQueryKey());
                // in case the template is not showing all the options at once
                if ( group != null ) 
                {
                    group.setProperties(option);

                    // check for a deleted flag.  AttributeOptions are removed
                    // from the db when deleted.
                    BooleanField deletedField = 
                        (BooleanField)group.get("Active");
                    if (deletedField != null && !deletedField.booleanValue()) 
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
            //attribute.sortOptions(attributeOptions);

            /*
            // was a new option added?
            option = new RModuleOption();
            Group group = intake.get("RModuleOption", 
                                     option.getQueryKey());
            if ( group != null ) 
            {
                group.setProperties(option);
                if ( option.getDisplayValue() != null 
                     && option.getDisplayValue().length() != 0 ) 
                {
                    try
                    {
                        attribute.addRModuleOption(option);
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
                    option = (RModuleOption)attributeOptions.get(i);
                    group = intake.get("RModuleOption", 
                                             option.getQueryKey());
                    // in case the template is not showing all the options
                    if ( group != null ) 
                    {
                        intake.remove(group);
                    }
                }
            }
            */
        }
    }

    /**
     * Manages clicking of the AllDone button
     */
    public void doAlldone( RunData data, TemplateContext context ) throws Exception
    {
        String nextTemplate = data.getParameters().getString(
            ScarabConstants.NEXT_TEMPLATE );

        setTarget(data, nextTemplate);
    }

    /**
        This manages clicking the cancel button
    */
    public void doCancel( RunData data, TemplateContext context ) throws Exception
    {
        data.setMessage("Changes were not saved!");
    }
    
    /**
        does nothing.
    */
    public void doPerform( RunData data, TemplateContext context ) throws Exception
    {
        doCancel(data, context);
    }
}
