package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collections;

// Turbine Stuff 
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.torque.om.NumberKey;
import org.apache.turbine.ParameterParser;
import org.apache.fulcrum.security.util.TurbineSecurityException;

// Scarab Stuff
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.actions.base.RequireLoginFirstAction;

/**
 * This class is responsible for the user configuration of the issue list.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @version $Id: ConfigureIssueList.java,v 1.33 2003/02/04 11:25:59 jon Exp $
 */
public class ConfigureIssueList extends RequireLoginFirstAction
{
    public void doSave(RunData data, TemplateContext context)
        throws Exception
    {
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabLocalizationTool l10n = getLocalizationTool(context);

        // Add user's new selection of attributes
        ParameterParser params = data.getParameters();
        String[] ids = params.getStrings("attid");
        String[] orders = params.getStrings("attorder");
        List attributes = new ArrayList(ids.length);
        final Map orderMap = new HashMap();            
        for (int i =0; i<ids.length; i++)
        {
            if (!orders[i].equals("hidden")) 
            {
                Attribute attribute = AttributeManager
                    .getInstance(new NumberKey(ids[i]));
                attributes.add(attribute);
                Integer order = new Integer(orders[i]);
                orderMap.put(attribute, order);
            }
        }

        if (attributes.isEmpty())
        {
            scarabR.setAlertMessage(l10n.get("MustSelectAtLeastOneAttribute"));
        }
        else
        {
            Comparator c = new Comparator()
                {
                    public int compare(Object o1, Object o2)
                    {
                        int order1 = ((Integer)orderMap.get(o1)).intValue();
                        int order2 = ((Integer)orderMap.get(o2)).intValue();
                        int result = order1 - order2;
                        if (result == 0) 
                        {
                            Attribute a1 = (Attribute)o1;
                            Attribute a2 = (Attribute)o2;
                            result = a1.getName().compareTo(a2.getName());
                        }
                        return result;
                    }
                };
            Collections.sort(attributes, c);
            try
            {
                ((ScarabUser)data.getUser()).updateIssueListAttributes(attributes);
                scarabR.setConfirmMessage(l10n.get(DEFAULT_MSG));
            }
            catch (TurbineSecurityException tse)
            {
                scarabR.setAlertMessage(l10n.get(NO_PERMISSION_MESSAGE));
            }
        }
    }

    /**
     * Resets back to default values for module.
     */
    public void doUsedefaults(RunData data, TemplateContext context) 
        throws Exception
    {
        data.getParameters().add("usedefaults", "true"); 
    }
}
