package org.tigris.scarab.screens.admin;

/*
 *  ================================================================
 *  Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *
 *  3. The end-user documentation included with the redistribution, if
 *  any, must include the following acknowlegement: "This product includes
 *  software developed by Collab.Net <http://www.Collab.Net/>."
 *  Alternately, this acknowlegement may appear in the software itself, if
 *  and wherever such third-party acknowlegements normally appear.
 *
 *  4. The hosted project names must not be used to endorse or promote
 *  products derived from this software without prior written
 *  permission. For written permission, please contact info@collab.net.
 *
 *  5. Products derived from this software may not use the "Tigris" or
 *  "Scarab" names nor may "Tigris" or "Scarab" appear in their names without
 *  prior written permission of Collab.Net.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 *  IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *  OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Collab.Net.
 */

// Core Java Stuff
import java.util.List;

// Turbine & Apache Commons Stuff
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.TemplateContext;
import org.apache.commons.fileupload.FileItem;

// Scarab Stuff
import org.tigris.scarab.screens.Default;
import org.tigris.scarab.util.xmlissues.ScarabIssues;
import org.tigris.scarab.services.security.ScarabSecurity;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.util.xmlissues.ImportIssues;
import org.tigris.scarab.tools.ScarabLocalizationTool;

/**
 * Loads XML into Scarab via import, returning XML-formatted results
 *
 * @author <a href="mailto:mmurphy@collab.net">Mark L. Murphy</a>
 * @version $Id: XMLImportIssuesResults.java,v 1.6 2003/03/28 00:00:52 jon Exp $
 */
public class XMLImportIssuesResults extends Default
{
    private static final int MIN_XML_SIZE = 1;
    private static final int RESULT_OK = 0;
    private static final int RESULT_ERROR_EXCEPTION = 100;
    private static final int RESULT_ERROR_XML_MISSING = 101;
    private static final int RESULT_ERROR_UNAUTHORIZED = 102;
    private static final int RESULT_ERROR_INVALID_ISSUE_DATA = 103;

    /**
     * Builds up the context for display of variables on the page.
     *
     * Runs import of POSTed issue.
     *
     * @param data Turbine run data
     * @param context Velocity template context
     */
    public void doBuildTemplate(RunData data, TemplateContext context)
        throws Exception
    {
        String resultString = "";
        int resultCode = RESULT_OK;
        List importErrors = null;
        ScarabIssues si = null;

        super.doBuildTemplate(data, context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        
        if (isImportAuthorized(data))
        {
            FileItem issuesToImport = data.getParameters()
                .getFileItem("issues");
            if (issuesToImport != null 
                && issuesToImport.getSize() >= MIN_XML_SIZE)
            {
                try
                {
                    ImportIssues importIssues = new ImportIssues();
                    importErrors = importIssues.runImport(issuesToImport);
                    si = importIssues.getScarabIssuesBeanReader();
                    if (importErrors != null)
                    {
                        resultCode = RESULT_ERROR_INVALID_ISSUE_DATA;
                        resultString = l10n.get("ProcessingErrors");
                    }
                }

                catch (Exception e)
                {
                    resultCode = RESULT_ERROR_EXCEPTION;
                    resultString = e.getMessage();
                }
            }
            else
            {
                resultCode = RESULT_ERROR_XML_MISSING;
                resultString = l10n.get("MissingXML");
            }
        }
        else
        {
            resultCode = RESULT_ERROR_UNAUTHORIZED;
            resultString = l10n.get("Unauthorized");
        }

        context.put("resultString", resultString);
        context.put("resultCode", new Integer(resultCode));
        context.put("importErrors", importErrors);
        context.put("issues", si);
        
        String format = data.getParameters().getString("format");
        if (format != null && format.equals("xml")) 
        {
            String result = org.apache.turbine.modules.Module.handleRequest
                (context, "macros/XMLImportIssuesResultsMacro.vm");
            data.getResponse().setContentType("text/plain");
            data.getResponse().setContentLength(result.length());
            data.getResponse().getOutputStream().print(result);
    
            // we already sent the response, there is no target to render
            data.setTarget(null);
        }
    }

    /**
     * Indicates if this request is authorized. 
     * 
     * Overridden so we always return XML for this request, with that XML
     * containing an error message if unauthorized. Otherwise, requesting this
     * page might sometimes return HTML (error page) and sometimes return XML
     * (authorized request), which will make parsing by automation clients
     * difficult.
     *
     * @param data Turbine run data.
     *
     * @return Boolean indicating if authorized.
     */
    protected boolean isAuthorized(RunData data) throws Exception
    {
        return (true);
    }

    /**
     * Indicates if this import request is authorized
     *
     * @param data Turbine run data.
     *
     * @return Boolean indicating whether or not authorized.
     */
    private boolean isImportAuthorized(RunData data) 
        throws Exception
    {
        String perm = ScarabSecurity.getScreenPermission
            ("admin.XMLImportIssuesResults.vm");
        TemplateContext context = getTemplateContext(data);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        Module currentModule = scarabR.getCurrentModule();
        ScarabUser user = (ScarabUser) data.getUser();
        boolean result = false;

        if (perm != null) // Is there is a permission for this?
        {
            if (user.hasLoggedIn() &&
                    user.hasPermission(perm, currentModule))
            {
                result = true; // Confirmed user has permission
            }
        }
        else
        {
            result = true; // No permission = unsecured
        }

        return (result);
    }
}

