package org.tigris.scarab.actions.admin;

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

// Velocity Stuff 
import org.apache.turbine.services.velocity.*; 
import org.apache.velocity.*; 
import org.apache.velocity.context.*; 
// Turbine Stuff 
import org.apache.turbine.util.*;
import org.apache.turbine.modules.*;
import org.apache.turbine.modules.actions.*;
import org.apache.turbine.om.StringKey;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.services.pull.ApplicationTool;
import org.apache.turbine.services.pull.TurbinePull;
// Scarab Stuff
import org.tigris.scarab.actions.base.*;
import org.tigris.scarab.om.*;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;

/**
    This class will store the form data for a project modification
        
    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: ModifyAttributes.java,v 1.1 2001/04/06 22:12:41 jon Exp $
*/
public class ModifyAttributes extends VelocityAction
{
    /**
        This manages clicking the Modify button
    */
    public void doSelectattribute( RunData data, Context context ) throws Exception
    {
        try
        {
            IntakeTool intake = (IntakeTool)context
                .get(ScarabConstants.INTAKE_TOOL);

            if ( intake.isAllValid() )
            {
                Group attribute = intake.get("Attribute", IntakeTool.DEFAULT_KEY);
                String attributeID = attribute.get("Id").toString();

                ApplicationTool srt = TurbinePull.getTool(context, 
                                        ScarabConstants.SCARAB_REQUEST_TOOL);
                if (srt != null)
                {
                    StringKey sk = new StringKey();
                    sk.setValue(attributeID);
                    Attribute attr = Attribute.getInstance(sk);
                    ((ScarabRequestTool)srt).setAttribute(attr);
                }
            }
        }
        catch (Exception e)
        {
            // display the error message
            data.setMessage(e.getMessage());
        }
    }
    /**
        This manages clicking the cancel button
    */
    public void doCancel( RunData data, Context context ) throws Exception
    {
        data.setMessage("Changes were not saved!");
    }
    /**
        does nothing.
    */
    public void doPerform( RunData data, Context context ) throws Exception
    {
        doCancel(data, context);
    }
}
