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
import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;

import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.Log;

/**
 * This class deals with modifying Global Attributes.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: GlobalAttributes.java,v 1.32 2003/04/25 18:16:34 jackrepenning Exp $
 */
public class GlobalAttributes extends RequireLoginFirstAction
{

    /**
     * Manages clicking of the create new button
     */
    public void doCreatenew(RunData data, TemplateContext context)
        throws Exception
    {
        String nextTemplate = data.getParameters().getString(
            ScarabConstants.OTHER_TEMPLATE, "admin, GlobalAttributeEdit.vm");
        setTarget(data, nextTemplate);

        ScarabRequestTool scarabR = getScarabRequestTool(context);
        scarabR.setAttribute(AttributeManager.getInstance());
    }

    public void doSave(RunData data, TemplateContext context)
        throws Exception
    {
        Log.get().debug("doSave");
        IntakeTool intake = getIntakeTool(context);
        List userAttrs = AttributePeer.getAttributes("user");
        for (int i=0; i<userAttrs.size(); i++)
        {
            Attribute attr = (Attribute)userAttrs.get(i);
            Group attrGroup = intake.get("Attribute", attr.getQueryKey());
            if (attrGroup != null)
            {
                attrGroup.setProperties(attr);
                attr.save();
            }
        }
        ScarabCache.clear();
        getScarabRequestTool(context).setConfirmMessage(getLocalizationTool(context).get(DEFAULT_MSG));
    }

    public synchronized void doCopy(RunData data, TemplateContext context)
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key = null;
        String id = null;
        Attribute attribute = null;
        boolean didCopy = false;
        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("action_"))
            {
               id = key.substring(7);
               attribute = AttributeManager.getInstance(new Integer(id));
               Attribute newAttribute = attribute
                  .copyAttribute((ScarabUser)data.getUser());
               newAttribute.save();
               didCopy = true;
            }
        }
        ScarabLocalizationTool l10n = getLocalizationTool(context);
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        if (didCopy)
        {
            scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
        }
        else
        {
            scarabR.setInfoMessage(l10n.get(NO_CHANGES_MADE));
        }
    }
}
