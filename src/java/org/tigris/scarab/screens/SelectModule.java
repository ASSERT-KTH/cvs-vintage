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
import org.apache.turbine.tool.TemplateLink;

// Scarab Stuff
import org.tigris.scarab.util.ScarabLink;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.Module;
import org.apache.torque.om.NumberKey;    

/**
 * This class adds a special link tool that should only be used
 * in SelectModule.vm
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: SelectModule.java,v 1.4 2002/06/19 03:44:25 jmcnally Exp $
 */
public class SelectModule extends Default
{
    /**
     * builds up the context for display of variables on the page.
     */
    public void doBuildTemplate( RunData data, TemplateContext context )
        throws Exception 
    {
        super.doBuildTemplate(data, context);
        context.put("modulelink", new ModuleSwitchingLink(data));
    }

    public static class ModuleSwitchingLink extends ScarabLink
    {
        private ModuleSwitchingLink(RunData data)
        {
            super();
            init((Object)data);
        }
        
        /**
         * override super method and make it public
         */
        public TemplateLink setPage(String moduleId)
        {
            String template = null;
            Module module = null;
            try
            {
                module = ModuleManager.getInstance(new NumberKey(moduleId));
                if (module.getIssueTypes(true).size() == 0)
                { 
                    template = "SelectArtifactType.vm";
                }
                else
                {
                    template = "home,EnterNew.vm";
                }
            }catch(Exception e){e.printStackTrace();}
            return super.setPage(template, moduleId);
        }
    }
}

