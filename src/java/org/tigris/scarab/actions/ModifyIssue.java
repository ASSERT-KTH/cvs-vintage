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

import java.util.*;
import java.math.BigDecimal;

// Velocity Stuff 
import org.apache.turbine.services.velocity.*; 
import org.apache.velocity.*; 
import org.apache.velocity.context.*; 
// Turbine Stuff 
import org.apache.turbine.util.*;
import org.apache.turbine.util.db.Criteria;
import org.apache.turbine.services.resources.*;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.services.intake.model.Field;
import org.apache.turbine.modules.*;
import org.apache.turbine.modules.actions.*;
import org.apache.turbine.om.*;

// Scarab Stuff
import org.tigris.scarab.om.*;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.util.*;
import org.tigris.scarab.util.word.IssueSearch;

/**
    This class is responsible for edit issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:elicia@collab.net">Elicia David</a>
    @version $Id: ModifyIssue.java,v 1.1 2001/06/27 23:43:41 elicia Exp $
*/
public class ModifyIssue extends VelocityAction
{
    public void doSubmitattributes( RunData data, Context context )
        throws Exception
    {
        //until we get the user and module set through normal application
        BaseScarabObject.tempWorkAround(data,context);

        String id = data.getParameters().getString("id");
        Issue issue = (Issue) IssuePeer.retrieveByPK(new NumberKey(id));
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);
       
        Group group = null; 
        if ( intake.isAllValid() ) 
        {
            HashMap avMap = issue.getAllAttributeValuesMap(); 
            Iterator i = avMap.keySet().iterator();
            while (i.hasNext()) 
            {
                AttributeValue aval = (AttributeValue)avMap.get(i.next());
                group = intake.get("AttributeValue", aval.getQueryKey(), false);

               /* Debugging code */
                System.out.println("aval=" + aval.getAttributeId());
                System.out.println("group=" + group);
                Field field = null;
                if ( group != null ) 
                {            
                    if ( aval instanceof OptionAttribute ) 
                    {
                        field = group.get("OptionId");
                    }
                    else 
                    {
                        field = group.get("Value");
                    }

                    System.out.println("field=" + field);

                    group.setProperties(aval);
                }
            }
            
            issue.save();

            String template = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE, 
                           "IssueView.vm");
            setTemplate(data, template);            
        }
    }

   public void doSaveComment (RunData data, Context context )
        throws Exception
    {                          
        String id = data.getParameters().getString("id");
        Issue issue = (Issue) IssuePeer.retrieveByPK(new NumberKey(id));
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);
        Attachment attachment = new Attachment();
        Group group = intake.get("Attachment", 
                           attachment.getQueryKey(), false);
        if ( group != null ) 
        {
            group.setProperties(attachment);
            if ( attachment.getData().length > 0 ) 
            {
                attachment.setIssue(issue);
                attachment.setTypeId(new NumberKey(1));
                        attachment.save();
            }
        }
     }


    /**
        This manages clicking the Cancel button
    */
    public void doCancel( RunData data, Context context ) throws Exception
    {
        setTemplate(data, "Start.vm");
    }
    /**
        calls doCancel()
    */
    public void doPerform( RunData data, Context context ) throws Exception
    {
        doCancel(data, context);
    }
}
