package org.tigris.scarab.actions;

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

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.fulcrum.security.util.AccessControlList;
import org.apache.torque.om.NumberKey;

// Scarab Stuff
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeGroup;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.module.ModuleManager;

/**
 * This class is responsible for creating / updating Scarab Modules
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ModifyModule.java,v 1.16 2002/02/05 02:48:44 jon Exp $
 */
public class ModifyModule extends RequireLoginFirstAction
{
    /**
     * Process Update button which updates a Module
     */
    public void doUpdate( RunData data, TemplateContext context ) 
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);
        ModuleEntity me = null;

        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            try
            {
                me = getScarabRequestTool(context).getModule();
            }
            catch (Exception e)
            {
                throw new Exception("Could not locate module");
            }

            Group moduleGroup = intake.get
                ("Module",me.getQueryKey(), false);
            if (moduleGroup == null)
            {
                setTarget(data, data.getParameters().getString(
                    ScarabConstants.TEMPLATE, "admin,ManageModules.vm"));
                data.setMessage("Could not locate module.");
                return;
            }
            else
            {
                moduleGroup.setProperties(me);
                me.save();
                intake.remove(moduleGroup);
                setTarget(data, nextTemplate);
            }
        }
    }

    /**
     * Process Create button which creates a new Module
     */
    public void doCreate( RunData data, TemplateContext context ) 
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);

        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            Group moduleGroup = intake.get
                ("Module",IntakeTool.DEFAULT_KEY, false);
            ModuleEntity me = ModuleManager.getInstance();
            if (moduleGroup == null)
            {
                throw new Exception("Could not locate module");
            }
            try
            {
                moduleGroup.setProperties(me);
                me.setOwnerId(((ScarabUser)data.getUser()).getUserId());
                me.save();

                data.setACL(TurbineSecurity.getACL(data.getUser()));
                data.save();

                data.setMessage("New Module Created!");
            }
            catch (Exception e)
            {
                setTarget(data, template);
                data.setMessage(e.getMessage());
                return;
            }
            intake.remove(moduleGroup);
        }
        else
        {
            setTarget(data, template);
            return;
        }
        setTarget(data, nextTemplate);
    }
}
