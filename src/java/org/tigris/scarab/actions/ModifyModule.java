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
import java.util.List;

import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.parser.ParameterParser;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.torque.oid.IDBroker;
import org.apache.torque.util.BasePeer;
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.tool.IntakeTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.GlobalParameter;
import org.tigris.scarab.om.GlobalParameterManager;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.ScarabModule;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.localization.L10NKeySet;
import org.tigris.scarab.util.Log;

/**
 * This class is responsible for creating / updating Scarab Modules
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ModifyModule.java,v 1.41 2005/01/10 20:29:23 dabbous Exp $
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
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        
        if (intake.isAllValid())
        {
            Module me = null;
            try
            {
                me = scarabR.getModule();
            }
            catch (Exception e)
            {
                throw new Exception("Could not locate module"); //EXCEPTION
            }

            Group moduleGroup = intake.get
                ("Module",me.getQueryKey(), false);
            if (moduleGroup == null)
            {
                setTarget(data, template);
                scarabR.setAlertMessage(
                    L10NKeySet.CouldNotLocateModuleGroup);
                return;
            }
            else
            {
                ScarabUser user = (ScarabUser) data.getUser();

                // make sure that the user has Edit permission 
                // in the module.
                if (!user.hasPermission(ScarabSecurity.MODULE__EDIT, me))
                {
                    scarabR.setAlertMessage(NO_PERMISSION_MESSAGE);
                    intake.remove(moduleGroup);
                    setTarget(data, nextTemplate);
                    return;
                }

                Module origParent = me.getParent();
                String origCode = me.getCode();
                moduleGroup.setProperties(me);
                Module newParent = me.getParent();
                String newCode = me.getCode();

                if (newParent.getParent() == me && origParent!=me)
                {
                    scarabR.setAlertMessage(L10NKeySet.CircularParentChildRelationship);
                    intake.remove(moduleGroup);
                    setTarget(data, template);
                    return;
                }
                else if (!user.hasPermission(ScarabSecurity.MODULE__EDIT, origParent) && 
                    origParent.getModuleId() != newParent.getModuleId())
                {
                    scarabR.setAlertMessage(L10NKeySet.NoPermissionInParentModule);
                    setTarget(data, template);
                    return;
                }
                
                // Cascade update the code to the (denormalized) issue prefix
                if (! newCode.equals(origCode))
                {
                    if (me instanceof ScarabModule)
                    {
                        ScarabModule sm = (ScarabModule)me;
                        List issues = sm.getIssues();
                        for (int i = 0; i < issues.size(); i++)
                        {
                            Issue issue = (Issue)issues.get(i);
                            if (! issue.getIdPrefix().equals(me.getCode()))
                            {
                                issue.setIdPrefix(me.getCode());
                                issue.save();
                            }
                        }
                        //Update the ID table to reflect the module code r
                        // FIXME: Using SQL because IDBroker doesn't have a Peer yet.
                        String idTable = IDBroker.TABLE_NAME.substring(0, 
                                IDBroker.TABLE_NAME.indexOf('.'));
                        String sql = "update " + idTable 
                         + " SET TABLE_NAME='" + newCode + "' WHERE TABLE_NAME='" +
                         origCode + "'";
                        BasePeer.executeStatement(sql);                                                
                    }
                    else
                    {
                        throw new Exception ("Did not get a ScarabModule"); //EXCEPTION
                    }
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

                ParameterParser pp = data.getParameters();
                String name = GlobalParameter.ISSUE_ALLOW_EMPTY_REASON;
                boolean allowEmptyReason = pp.getBoolean(name,false);
                GlobalParameterManager.setBoolean(name, me,allowEmptyReason);
         
                intake.remove(moduleGroup);
                setTarget(data, nextTemplate);
                scarabR.setConfirmMessage(L10NKeySet.ModuleUpdated);
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
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        
        if (intake.isAllValid())
        {
            Group moduleGroup = intake.get
                ("Module",IntakeTool.DEFAULT_KEY, false);
            Module me = ModuleManager.getInstance();
            if (moduleGroup == null)
            {
                throw new Exception("Could not locate module"); //EXCEPTION
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
                        " assign this module to the requested parent module."); //EXCEPTION
                }
                me.setOwnerId(user.getUserId());
                me.save();

                data.setACL(TurbineSecurity.getACL(data.getUser()));
                data.save();

                scarabR.setConfirmMessage(L10NKeySet.NewModuleCreated);
            }
            catch (Exception e)
            {
                setTarget(data, template);
                Log.get().error(e);
                String msg = l10n.getMessage(e);
                scarabR.setAlertMessage(msg);
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
