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

import java.util.Iterator;
import java.util.Date;
import java.util.HashMap;
import java.math.BigDecimal;

// Turbine Stuff 
import org.apache.turbine.TemplateAction;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.turbine.services.db.om.NumberKey;
import org.apache.turbine.services.intake.IntakeTool;
import org.apache.turbine.services.db.util.Criteria;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.services.intake.model.Field;
import org.apache.turbine.services.db.om.ObjectKey;
import org.apache.turbine.om.security.User;
import org.apache.turbine.util.SequencedHashtable;
import org.apache.turbine.util.ParameterParser;

// Scarab Stuff
import org.tigris.scarab.om.*;
import org.tigris.scarab.attribute.*;
import org.tigris.scarab.util.*;
import org.tigris.scarab.util.word.IssueSearch;

/**
    This class is responsible for edit issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:elicia@collab.net">Elicia David</a>
    @version $Id: ModifyIssue.java,v 1.10 2001/07/13 23:18:13 jon Exp $
*/
public class ModifyIssue extends TemplateAction
{
    private static final String ERROR_MESSAGE = "More information was " +
                                "required to submit your request. Please " +
                                "scroll down to see error messages."; 

    public void doSubmitattributes( RunData data, TemplateContext context )
        throws Exception
    {
        String id = data.getParameters().getString("id");
        Issue issue = (Issue) IssuePeer.retrieveByPK(new NumberKey(id));
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);
       
        // Comment field is required to modify attributes
        Attachment attachment = new Attachment();
        Group commentGroup = intake.get("Attachment", "attCommentKey", false);
        Field commentField = null;
        commentField = commentGroup.get("DataAsString");
        commentField.setRequired(true);
        if (commentGroup == null || !commentField.isValid() )
        {
            commentField.setMessage("An explanatory comment is required " + 
                                    "to modify attributes.");
        }

        // Set any other required flags
        Criteria crit = new Criteria()
            .add(RModuleAttributePeer.ACTIVE, true)        
            .add(RModuleAttributePeer.REQUIRED, true);        
        Attribute[] requiredAttributes = issue.getModule().getAttributes(crit);
        AttributeValue aval = null;
        Group group = null;

        SequencedHashtable modMap = issue.getModuleAttributeValuesMap(); 
        Iterator iter = modMap.iterator();
        while ( iter.hasNext() ) 
        {
            aval = (AttributeValue)modMap.get(iter.next());
            group = intake.get("AttributeValue", aval.getQueryKey(), false);

            if ( group != null ) 
            {            
                Field field = null;
                if ( aval.getAttribute().getAttributeType()
                     .getAttributeClass().getName().equals("user") )
                {
                    field = group.get("UserId");
                }
                else if ( aval instanceof OptionAttribute ) 
                {
                    field = group.get("OptionId");
                }
                else
                {
                    field = group.get("Value");
                }
            
                for ( int j=requiredAttributes.length-1; j>=0; j-- ) 
                {
                    if ( aval.getAttribute().getPrimaryKey().equals(
                         requiredAttributes[j].getPrimaryKey() )) 
                    {
                        field.setRequired(true);
                        break;
                    }                    
                }
            } 
        } 

        if ( intake.isAllValid() ) 
        {
System.out.println("all valid");

            // Save explanatory comment
            commentGroup.setProperties(attachment);
            attachment.setIssue(issue);
            attachment.setTypeId(new NumberKey("2"));
            attachment.setMimeType("text/plain");
            attachment.save();
                    
            // Save transaction record
            Transaction transaction = new Transaction();
            transaction.setCreatedDate(new Date());
            // TODO: set CreatedUser field
            //transaction.setCreatedBy(Integer.parseInt(userId.toString()));
            transaction.save();

            // Set the attribute values entered 
            HashMap avMap = issue.getAllAttributeValuesMap();
            Iterator iter2 = avMap.keySet().iterator();
            while (iter2.hasNext())
            {
                aval = (AttributeValue)avMap.get(iter2.next());
                group = intake.get("AttributeValue", aval.getQueryKey(), false);

                if ( group != null ) 
                {            
                    String newValue = null;
                    String oldValue = null;
                    if ( aval.getAttribute().getAttributeType()
                         .getAttributeClass().getName().equals("user") )
                    {
                        newValue = group.get("UserId").toString();
                        oldValue = aval.getUserId().toString();
                    }
                    else if ( aval instanceof OptionAttribute ) 
                    {
                        newValue = group.get("OptionId").toString();
                        oldValue = aval.getOptionIdAsString();
                    }
                    else 
                    {
                        newValue = group.get("Value").toString();
                        oldValue = aval.getValue();
                    }

                    if (!newValue.equals("") && !oldValue.equals(newValue))
                    {
                        group.setProperties(aval);

                        // Save activity record
                        Activity activity = new Activity();
                        activity.setIssueId(id);  
                        activity.setAttributeId(aval.getAttribute()
                                                .getAttributeId());  
                        activity.setTransaction(transaction);
                        activity.setAttachment(attachment);
                        activity.setOldValue(oldValue);
                        activity.setNewValue(newValue);
                        activity.save();
                    }
                } 
            }
            issue.save();
          
        } 
        else
        {
            data.setMessage(ERROR_MESSAGE);
        }

        String template = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, template);            
    }

    
    /**
    *  Adds an attachment of type "url".
    */
   public void doSubmiturl (RunData data, TemplateContext context ) 
        throws Exception
   {
        submitAttachment (data, context, "url");
   } 

    /**
    *  Adds an attachment of type "comment".
    */
   public void doSubmitcomment (RunData data, TemplateContext context ) 
        throws Exception
   {
        submitAttachment (data, context, "comment");
   } 

    /**
    *  Adds an attachment.
    */
   private void submitAttachment (RunData data, TemplateContext context, 
                                  String type)
        throws Exception
    {                          
        String id = data.getParameters().getString("id");
        Issue issue = (Issue) IssuePeer.retrieveByPK(new NumberKey(id));
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);
        Attachment attachment = new Attachment();
        String typeID= null;
        Group group = null;

        if (type.equals("url"))
        {
            group = intake.get("Attachment", "urlKey", false);
            typeID = "3";
        } 
        else if (type.equals("comment")) 
        {
            group = intake.get("Attachment", "commentKey", false);
            typeID = "2";
        }

        if ( group != null ) 
        {
            Field nameField = group.get("Name"); 
            Field dataField = group.get("DataAsString"); 
            nameField.setRequired(true);
            dataField.setRequired(true);
            if (!nameField.isValid() )
            {
                nameField.setMessage("This field requires a value.");
            }
            if (!dataField.isValid() )
            {
                dataField.setMessage("This field requires a value.");
            }

            if (intake.isAllValid() )
            {
                group.setProperties(attachment);
                attachment.setIssue(issue);
                attachment.setTypeId(new NumberKey(typeID));
                attachment.setMimeType("text/plain");
                attachment.save();
                String template = data.getParameters()
                                 .getString(ScarabConstants.NEXT_TEMPLATE);
                setTarget(data, template);            
            } 
            else
            {
                data.setMessage(ERROR_MESSAGE);
            }
        }
        String template = data.getParameters()
                          .getString(ScarabConstants.NEXT_TEMPLATE, "ViewIssue");
        setTarget(data, template);            
   } 

    /**
    *  Deletes an url.
    */
   public void doDeleteurl (RunData data, TemplateContext context )
        throws Exception
    {                          
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        String key;
        String attachmentId;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("url_delete_"))
            {
               attachmentId = key.substring(11);
               Attachment attachment = (Attachment) AttachmentPeer
                                     .retrieveByPK(new NumberKey(attachmentId));
               attachment.delete();
            } 
        }
        String template = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, template);            
    }

    /**
    *  Modifies the dependency type between the current issue
    *  And its child issue.
    */
    public void doUpdatechild (RunData data, TemplateContext context )
        throws Exception
    {                          
        String id = data.getParameters().getString("id");
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        Issue currentIssue = (Issue) IssuePeer.retrieveByPK(
                             new NumberKey(id));
        String key;
        String childId;
        Depend depend;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("child_depend_type_id"))
            {
               String dependTypeId = params.getString(key);
               
               childId = key.substring(21);
               Issue child = (Issue) IssuePeer.retrieveByPK(
                              new NumberKey(childId));
               depend = currentIssue.getDependency(child);

               // User selected to remove the dependency
               if (dependTypeId.equals("none"))
               {
                   depend.delete();
               } 
               else
               {
                   DependType dependType = (DependType) DependTypePeer
                              .retrieveByPK(new NumberKey(dependTypeId));
                   depend.setDependType(dependType);
               }
               depend.save();
               break;
            }
         }
    }

    /**
    *  Modifies the dependency type between the current issue
    *  And its parent issue.
    */
    public void doUpdateparent (RunData data, TemplateContext context )
        throws Exception
    {                          
        String id = data.getParameters().getString("id");
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        Issue currentIssue = (Issue) IssuePeer.retrieveByPK(
                             new NumberKey(id));
        String key;
        String parentId;
        Depend depend;

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("parent_depend_type_id"))
            {
                String dependTypeId = params.getString(key);
                parentId = key.substring(22);
                Issue parent = (Issue) IssuePeer
                      .retrieveByPK(new NumberKey(parentId));
                depend = parent.getDependency(currentIssue);

                // User selected to remove the dependency
                if (dependTypeId.equals("none"))
                {
                    depend.delete();
                }
                else
                {
                    depend.setTypeId(dependTypeId);
                }
                depend.save();
            }
        }
    }

    /**
    *  Adds a dependency between this issue and another issue.
    *  This issue will be the child issue. 
    */
    public void doAdddependency (RunData data, TemplateContext context )
        throws Exception
    {                          
        String id = data.getParameters().getString("id");
        Issue currentIssue = (Issue) IssuePeer.retrieveByPK(
                             new NumberKey(id));
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);
        Group group = intake.get("Depend", "dependKey", false);
        boolean isValid = true;

        // The depend type is required.
        Field dependTypeId = group.get("TypeId");
        if (dependTypeId.toString().equals("0"))
        {
            dependTypeId.setMessage("Please select a dependency type.");
        }

        // The parent issue id is required, and must be a valid issue.
        Field observedId = group.get("ObservedId");
        observedId.setRequired(true);
        if (!observedId.isValid())
        {
            observedId.setMessage("Please enter a valid issue id.");
        }
        else
        {
            try
            {
                Issue parentIssue = (Issue) IssuePeer.retrieveByPK(
                                    new NumberKey(observedId.toString()));
            }
            catch (Exception e)
            {
                observedId.setMessage("The id you entered does " +
                                      "not correspond to a valid issue.");
                isValid = false;
            }
        }

        if (intake.isAllValid() && isValid)
        {
            Depend depend = new Depend();
            depend.setObserverId(currentIssue.getIssueId());
            depend.setObservedId(new NumberKey(observedId.toString()));
            depend.setTypeId(new NumberKey(dependTypeId.toString()));

            // TODO: would like to set these properties using 
            // group.setProperties, but getting errors. (John?)
            //group.setProperties(depend);
            depend.save();
        }
        else
        {
            data.setMessage(ERROR_MESSAGE);
        }

        String nextTemplate = data.getParameters()
            .getString(ScarabConstants.NEXT_TEMPLATE);
        setTarget(data, nextTemplate);            
    }


    /**
        This manages clicking the Cancel button
    */
    public void doCancel( RunData data, TemplateContext context ) throws Exception
    {
        setTarget(data, "Start.vm");
    }
    /**
        calls doCancel()
    */
    public void doPerform( RunData data, TemplateContext context ) throws Exception
    {
        doCancel(data, context);
    }
}
