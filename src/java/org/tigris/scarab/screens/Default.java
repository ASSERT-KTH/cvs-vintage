package org.tigris.scarab.screens;

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
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.TemplateSecureScreen;
import org.apache.turbine.Turbine;

// Scarab Stuff
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.Log;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabUser;

/**
 * This class is responsible for building the Context up
 * for the Default Screen as well as validating Security information
 * for all of the Screens. Please note that the Actions also may depend
 * on the checkAuthorized() method in order to prevent the need for
 * duplication of code.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: Default.java,v 1.72 2003/04/01 17:09:58 jmcnally Exp $
 */
public class Default extends TemplateSecureScreen
{
    /**
     * Override the subclass and call doBuildTemplate. This is a hack. 
     * For some reason the doBuildTemplate is not being called in a 
     * few select cases, so lets just hack things to always get called
     * properly.
     */
    public String doBuild(RunData data)
        throws Exception
    {
        super.doBuild(data);
        return "";
    }

    /**
     * builds up the context for display of variables on the page.
     */
    protected void doBuildTemplate(RunData data, TemplateContext context)
        throws Exception 
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        // This may not be the best location for this, we might need to create
        // a valve.  
        // check that the module exists, it may not have been created yet.
        try
        {
            scarabR.getCurrentModule();
        }
        catch (Exception ignore)
        {
        }

        // add the title text to the context.
        ScarabLocalizationTool l10n = (ScarabLocalizationTool)
            context.get("l10n");
        String title = getTitle(scarabR, l10n, data, context);
        if (title == null)
        {
            title = "Scarab";
        }
        context.put("title", title);
    }

    protected String getTitle(ScarabRequestTool scarabR, 
                              ScarabLocalizationTool l10n,
                              RunData data, TemplateContext context)
        throws Exception
    {
        return l10n.getTitle();
    }

    /**
     * sets the template to Login.vm if the user hasn't logged in yet
     * or if the user does not have the base permissions.
     */
    protected boolean isAuthorized(RunData data) throws Exception
    {
        return checkAuthorized(data);
    }

    /**
     * Public static access to the isAuthorized() method so that
     * an Action can use this same method to do authorization.
     */
    public static boolean checkAuthorized(RunData data)
        throws Exception
    {
        String template = data.getTarget();
        {
            template = template.replace(',','.');
            String perm = ScarabSecurity.getScreenPermission(template);
            TemplateContext context = getTemplateContext(data);
            ScarabRequestTool scarabR = getScarabRequestTool(context);
            ScarabLocalizationTool l10n = getLocalizationTool(context);
            Module currentModule = scarabR.getCurrentModule();
            ScarabUser user = (ScarabUser)data.getUser();
            if (perm != null)
            {
                if (! user.hasLoggedIn() 
                    || !user.hasPermission(perm, currentModule))
                {
                    scarabR.setInfoMessage(
                        l10n.get("LoginToAccountWithPermissions"));
                    // it is very common to come from email to view a 
                    // particular issue.  Until a more general formula for
                    // deciding which requests might be ok to continue after
                    // a login, we will at least allow this one.
                    if ("ViewIssue.vm".equals(data.getTarget())) 
                    {
                        data.getParameters().setString("viewIssueId", 
                                    data.getParameters().getString("id"));
                    }

                    setTargetLogin(data);
                    scarabR.setCurrentModule(null);
                    return false;
                }
                else if (currentModule == null)
                {
                    Log.get().debug("Current module is null");
                    scarabR.setInfoMessage(l10n.get("SelectModuleToWorkIn"));
                    setTargetSelectModule(data);
                    return false;
                }
            }
            // does the user at least have a role in the module?
            // we don't check user.hasLoggedIn() here because guest
            // users could have a role in a module.
            else if (currentModule != null && 
                     !user.hasAnyRoleIn(currentModule))
            {
                if (Log.get().isDebugEnabled()) 
                {
                    Log.get().debug("User (" + user.getUserId() + 
                        ") did not have any roles in current module" + 
                        currentModule.getName());
                }                
                scarabR.setCurrentModule(null);
                data.getParameters().remove(ScarabConstants.CURRENT_MODULE);
                scarabR.setAlertMessage(l10n.get("NoPermissionInModule"));
                setTargetSelectModule(data);
                return false;
            }
/* FIXME
   Breaks the ability to request roles because the permission is null and
   the module is null, but we are logged in. John, we should assign default
   permissions to each screen so that we can make it so that someone can be
   logged in, but not select a module yet and be shown the select module
   screen. (JSS)
   
            else if (currentModule == null && 
                     user != null && 
                     user.hasLoggedIn())
            {
                setTargetSelectModule(data);
                return true;
            }
*/
        }
        return true;
    }

    public static void setTargetSelectModule(RunData data)
    {
        getTemplateContext(data)
            .put(ScarabConstants.NEXT_TEMPLATE,
                          data.getParameters()
                          .getString(ScarabConstants.NEXT_TEMPLATE));

        setTarget(data, Turbine.getConfiguration()
                .getString("scarab.CurrentModuleTemplate", "SelectModule.vm"));        
    }

    public static void setTargetLogin(RunData data)
    {
        getTemplateContext(data).put(ScarabConstants.NEXT_TEMPLATE, 
            data.getParameters().getString("template"));
        setTarget(data, "Login.vm");        
    }

    /**
     * Helper method to retrieve the ScarabRequestTool from the Context
     */
    public static ScarabRequestTool getScarabRequestTool(TemplateContext context)
    {
        return (ScarabRequestTool)context
            .get(ScarabConstants.SCARAB_REQUEST_TOOL);
    }

    /**
     * Helper method to retrieve the ScarabLocalizationTool from the Context
     */
    public static ScarabLocalizationTool getLocalizationTool(TemplateContext context)
    {
        return (ScarabLocalizationTool)context
            .get(ScarabConstants.LOCALIZATION_TOOL);
    }
}
