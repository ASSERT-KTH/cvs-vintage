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
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypePeer;
//import org.tigris.scarab.om.RModuleIssueType;
import org.tigris.scarab.util.ScarabConstants;

/**
 * This class deals with modifying Global Artifact Types.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: GlobalArtifactTypeCreate.java,v 1.11 2002/02/27 22:04:14 elicia Exp $
 */
public class GlobalArtifactTypeCreate extends RequireLoginFirstAction
{

    /**
     * creates or edits global artifact type
     */
    public void doSave( RunData data, TemplateContext context )
        throws Exception
    {
        IntakeTool intake = getIntakeTool(context);
        IssueType issueType = getScarabRequestTool(context).getIssueType();
        Group group = intake.get("IssueType", issueType.getQueryKey());
        String lastTemplate = getLastTemplate(data);

        if ( intake.isAllValid() ) 
        {
            if (issueType.getIssueTypeId() == null)
            {
                // Create new issue type
                // make sure name is unique
                Field field = group.get("Name");
                String name = field.toString();
                if ( IssueTypePeer.isUnique(name, null) ) 
                {
                    group.setProperties(issueType);
                    issueType.setParentId(new NumberKey("0"));
                    issueType.save();
                    
                    // Create template type.
                    IssueType template = new IssueType();
                    template.setName(issueType.getName() + " Template");
                    template.setParentId(issueType.getIssueTypeId());
                    template.save();
                    doCancel(data ,context);

                    // If they came from the manage issue types page
                    // Cancel back one more time to skip extra step
                    if (lastTemplate != null && 
                        lastTemplate.equals("admin,ArtifactTypeSelect.vm"))
                    {
                        getScarabRequestTool(context)
                           .getCurrentModule().addRModuleIssueType(issueType);
                        data.setMessage("The issue type has been added to "
                                         + "the module.");
                        doCancel(data ,context);
                    }
                }
                else 
                {
                    data.setMessage("Issue type by that name already exists");
                }
            }
            else
            {
                // Edit existing issue type
                group.setProperties(issueType);
                issueType.save();
            }

        }
        else
        {
            data.setMessage(ERROR_MESSAGE);
        }
    }
}
