package org.tigris.scarab.actions.admin;

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

import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.services.cache.ScarabCache;

/**
 * This class deals with modifying Global Artifact Types.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: GlobalArtifactTypes.java,v 1.30 2003/01/22 23:37:33 elicia Exp $
 */
public class GlobalArtifactTypes extends RequireLoginFirstAction
{

    /**
     * Used on GlobalAttributeEdit.vm to modify Attribute Name/Description/Type
     * Use doAddormodifyattributeoptions to modify the options.
     */
    public void doSave( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        List issueTypes = IssueTypePeer.getAllIssueTypes(false);

        if ( intake.isAllValid() )
        {
            for (int i=0; i<issueTypes.size(); i++)
            {
                IssueType issueType = (IssueType)issueTypes.get(i);
                Group group = intake.get("IssueType", issueType.getQueryKey());
                // make sure name is unique
                Field field = group.get("Name");
                String name = field.toString();
                if ( IssueTypePeer.isUnique(name, issueType.getPrimaryKey()) ) 
                {
                    group.setProperties(issueType);
                    issueType.save();
                    ScarabCache.clear();
                }
                else 
                {
                    getScarabRequestTool(context).setAlertMessage(
                        l10n.get("ChangesResultDuplicateNames"));
                    field.setMessage("Duplicate");
                }
            }
         }
     }
                
    public void doCopy( RunData data, TemplateContext context )
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String id;
        IssueType issueType;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
               id = key.substring(7);
               issueType = IssueTypePeer
                      .retrieveByPK(new NumberKey(id));
               issueType.copyIssueType();
             }
         }
     }

    public void doDelete( RunData data, TemplateContext context )
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        String key;
        String id;
        IssueType issueType;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
               id = key.substring(7);
               issueType = IssueTypePeer
                       .retrieveByPK(new NumberKey(id));
               if (issueType.hasIssues())
               {
                   Group group = getIntakeTool(context).get("IssueType", issueType.getQueryKey());
                   Field field = group.get("Name");
                   getScarabRequestTool(context).setAlertMessage(l10n.get("CannotDeleteIssueTypesWithIssues"));
                   field.setMessage("IssueTypeHasIssues");
               }
               else 
               {
                   issueType.setDeleted(true);
                   issueType.save();
                   getScarabRequestTool(context).setConfirmMessage(l10n.get("GlobalIssueTypesDeleted"));
               }
             }
         }
     }

    public void doUndelete( RunData data, TemplateContext context )
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String id;
        IssueType issueType;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
               id = key.substring(7);
               issueType = IssueTypePeer
                      .retrieveByPK(new NumberKey(id));
               issueType.setDeleted(false);
               issueType.save();
               getScarabRequestTool(context).setConfirmMessage(getLocalizationTool(context).get("GlobalIssueTypesUnDeleted"));
             }
         }
     }
}
