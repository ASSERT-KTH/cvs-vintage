package org.tigris.scarab.actions;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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
import java.util.Iterator;

import org.apache.commons.util.SequencedHashtable;

// Turbine Stuff 
import org.apache.turbine.Turbine;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;
import org.apache.turbine.modules.ContextAdapter;
import org.apache.torque.om.NumberKey; 

import org.apache.turbine.tool.IntakeTool;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

// Scarab Stuff
import org.tigris.scarab.actions.base.RequireLoginFirstAction;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTemplateInfo;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.attribute.OptionAttribute;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.tools.ScarabRequestTool;


/**
    This class is responsible for report managing enter issue templates.
    ScarabIssueAttributeValue
    @author <a href="mailto:elicia@collab.net">Elicia David</a>
    @version $Id: TemplateList.java,v 1.15 2001/11/03 00:32:28 elicia Exp $
*/
public class TemplateList extends RequireLoginFirstAction
{

    private static final String ERROR_MESSAGE = "More information was " +
                                "required to submit your request. Please " +
                                "see error messages."; 

    /**
        Saves template.
    */
    public void doSubmit( RunData data, TemplateContext context )
         throws Exception
    {        
        IntakeTool intake = getIntakeTool(context);        
        ScarabRequestTool scarabR = getScarabRequestTool(context);
        ScarabUser user = (ScarabUser)data.getUser();
        Issue issue = scarabR.getIssueTemplate();

        SequencedHashtable avMap = issue.getModuleAttributeValuesMap();
        AttributeValue aval = null;
        Group group = null;
        
        IssueTemplateInfo info = scarabR.getIssueTemplateInfo();
        Group infoGroup = intake.get("IssueTemplateInfo", info.getQueryKey() );
        Group issueGroup = intake.get("Issue", issue.getQueryKey() );
        issueGroup.setProperties(issue);

        Field name = infoGroup.get("Name");
        name.setRequired(true);

        if (intake.isAllValid() ) 
        {
            // Save transaction record
            Transaction transaction = new Transaction();
            transaction.create(TransactionTypePeer.CREATE_ISSUE__PK, user, null);

            Iterator iter = avMap.iterator();
            while (iter.hasNext()) 
            {
                aval = (AttributeValue)avMap.get(iter.next());
                group = intake.get("AttributeValue", aval.getQueryKey(),false);
                if ( group != null ) 
                {
                    group.setProperties(aval);
                    aval.startTransaction(transaction);
                }                
            }

            // get issue type id = the child type of the current issue type
            issue.setTypeId(scarabR.getCurrentIssueType().getTemplateId());
            issue.save();

            // Save template info
            infoGroup.setProperties(info);
            info.setIssueId(issue.getIssueId());
            info.saveAndSendEmail(user, scarabR.getCurrentModule(),
                new ContextAdapter(context));

            String template = data.getParameters()
                .getString(ScarabConstants.NEXT_TEMPLATE);
            setTarget(data, template);            
        } 
        else
        {
            data.setMessage(ERROR_MESSAGE);
        }
    }


    public void doDeletetemplates( RunData data, TemplateContext context )
        throws Exception
    {
        Object[] keys = data.getParameters().getKeys();
        String key;
        String templateId;
        ScarabUser user = (ScarabUser)data.getUser();

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("delete_"))
            {
               templateId = key.substring(7);
               Issue issue = (Issue) IssuePeer
                  .retrieveByPK(new NumberKey(templateId));
               try
               {
                   issue.delete(user);
               }
               catch (Exception e)
               {
                   data.setMessage(ScarabConstants.NO_PERMISSION_MESSAGE);
               }
            }
        } 
    } 

    public void doCreatenew( RunData data, TemplateContext context )
        throws Exception
    {
        setTarget(data, "SaveTemplate.vm");            
    }

    /**
     * This manages clicking the Cancel button
     */
    public void doCancel( RunData data, TemplateContext context ) throws Exception
    {
        setTarget(data, getCancelTemplate(data, "SaveTemplate.vm"));
    }

    /**
     * This manages clicking the Back button
     */
    public void doBack( RunData data, TemplateContext context ) throws Exception
    {
        setTarget(data, getBackTemplate(data, "TemplateList.vm"));
    }

    /**
     * calls doCancel()
     */
    public void doPerform( RunData data, TemplateContext context ) throws Exception
    {
        doCancel(data, context);
    }
}
