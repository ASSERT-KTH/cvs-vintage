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

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.turbine.ParameterParser;

// Scarab Stuff
import org.tigris.scarab.om.GlobalParameter;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabLocalizationTool;

/**
 * This class is responsible for creating / updating Scarab Modules
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ModifyModule.java,v 1.32 2003/04/17 22:52:13 jon Exp $
 */
public class ModifyModule extends RequireLoginFirstAction
{
    private static final String[] EMAIL_PARAMS = 
        {GlobalParameter.EMAIL_ENABLED, 
         GlobalParameter.EMAIL_INCLUDE_ISSUE_DETAILS};

    /**
     * Process Update button which updates a Module
     */
    public void doUpdate(RunData data, TemplateContext context) 
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);

        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            Module me = null;
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
                setTarget(data, template);
                getScarabRequestTool(context).setAlertMessage(
                    l10n.get("CouldNotLocateModuleGroup"));
                return;
            }
            else
            {
                ScarabUser user = (ScarabUser) data.getUser();

                // make sure that the user has Edit permission 
                // in the module.
                if (!user.hasPermission(ScarabSecurity.MODULE__EDIT, me))
                {
                    getScarabRequestTool(context).setAlertMessage(
                        l10n.get(NO_PERMISSION_MESSAGE));
                    intake.remove(moduleGroup);
                    setTarget(data, nextTemplate);
                    return;
                }

                Module origParent = me.getParent();
                moduleGroup.setProperties(me);
                Module newParent = me.getParent();

                if (newParent.getParent() == me)
                {
                    getScarabRequestTool(context).setAlertMessage(
                        l10n.get("CircularParentChildRelationship"));
                    intake.remove(moduleGroup);
                    setTarget(data, template);
                    return;
                }
                else if (!user.hasPermission(ScarabSecurity.MODULE__EDIT, origParent) && 
                    origParent.getModuleId() != newParent.getModuleId())
                {
                    getScarabRequestTool(context).setAlertMessage(
                        l10n.get("NoPermissionInParentModule"));
                    setTarget(data, template);
                    return;
                }
                me.save();

                // Set email overrides
                if (GlobalParameterManager.getBoolean(
                        GlobalParameter.EMAIL_ALLOW_MODULE_OVERRIDE)) 
                {
                    ParameterParser pp = data.getParameters();
                    String name;
                    for (int i=0; i<EMAIL_PARAMS.length; i++) 
                    {
                        name = EMAIL_PARAMS[i];
                        GlobalParameterManager
                            .setBoolean(name, pp.getBoolean(name));
                    }
                }

                intake.remove(moduleGroup);
                setTarget(data, nextTemplate);
                getScarabRequestTool(context)
                    .setConfirmMessage(l10n.get("ModuleUpdated"));
            }
        }
    }

    /**
     * Process Create button which creates a new Module
     */
    public void doCreate(RunData data, TemplateContext context) 
        throws Exception
    {
        String template = getCurrentTemplate(data, null);
        String nextTemplate = getNextTemplate(data, template);

        ScarabLocalizationTool l10n = getLocalizationTool(context);
        IntakeTool intake = getIntakeTool(context);
        if (intake.isAllValid())
        {
            Group moduleGroup = intake.get
                ("Module",IntakeTool.DEFAULT_KEY, false);
            Module me = ModuleManager.getInstance();
            if (moduleGroup == null)
            {
                throw new Exception("Could not locate module");
            }
            try
            {
                moduleGroup.setProperties(me);
                ScarabUser user = (ScarabUser)data.getUser();
                
                // make sure that the user has Edit permission 
                // in the parent module.
                // FIXME: move this logic into the ScarabModule.save() method
                if (!user.hasPermission(ScarabSecurity.MODULE__EDIT, 
                    me.getParent()))
                {
                    throw new Exception ("You do not have permission to" + 
                        " assign this module to the requested parent module.");
                }
                me.setOwnerId(user.getUserId());
                me.save();

                data.setACL(TurbineSecurity.getACL(data.getUser()));
                data.save();

                getScarabRequestTool(context).setConfirmMessage(
                    l10n.get("NewModuleCreated"));
            }
            catch (Exception e)
            {
                setTarget(data, template);
                Log.get().error(e);
                getScarabRequestTool(context).setAlertMessage(e.getMessage());
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
