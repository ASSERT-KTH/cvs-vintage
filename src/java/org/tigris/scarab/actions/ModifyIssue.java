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

import java.util.Iterator;
import java.util.Date;
import java.util.Vector;
import java.util.HashMap;

// Turbine Stuff 
import org.apache.turbine.TemplateAction;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.RunData;

import org.apache.torque.om.NumberKey; 
import org.apache.torque.om.ObjectKey; 
import org.apache.torque.om.NumberKey;
import org.apache.turbine.tool.IntakeTool;
import org.apache.torque.util.Criteria;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.services.intake.model.Field;
import org.apache.commons.util.SequencedHashtable;
import org.apache.turbine.ParameterParser;

// Scarab Stuff
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.AttachmentPeer;
import org.tigris.scarab.om.RModuleAttributePeer;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributeValue;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.AttributeOptionPeer;
import org.tigris.scarab.om.Depend;
import org.tigris.scarab.om.DependPeer;
import org.tigris.scarab.om.DependType;
import org.tigris.scarab.om.DependTypePeer;
import org.tigris.scarab.om.ScarabUser;

import org.tigris.scarab.attribute.OptionAttribute;

import org.tigris.scarab.util.ScarabConstants;

/**
    This class is responsible for edit issue forms.
    ScarabIssueAttributeValue
    @author <a href="mailto:elicia@collab.net">Elicia David</a>
    @version $Id: ModifyIssue.java,v 1.22 2001/08/17 22:03:11 jmcnally Exp $
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
        ScarabUser user = (ScarabUser)data.getUser();

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
        Attribute[] requiredAttributes = issue.getScarabModule()
            .getRequiredAttributes();
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
                if ( aval instanceof OptionAttribute ) 
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
            // Save explanatory comment
            commentGroup.setProperties(attachment);
            attachment.setTextFields(user, issue, 
                                     Attachment.MODIFICATION__PK);
            attachment.save();

            // Set the attribute values entered 
            HashMap avMap = issue.getAllAttributeValuesMap();
            Iterator iter2 = avMap.keySet().iterator();

            // Save transaction record
            Transaction transaction = new Transaction();
            transaction.create(user);

            while (iter2.hasNext())
            {
                aval = (AttributeValue)avMap.get(iter2.next());
                group = intake.get("AttributeValue", aval.getQueryKey(), false);

                if ( group != null ) 
                {            
                    String newValue = "";
                    String oldValue = "";
                    if ( aval instanceof OptionAttribute ) 
                    {
                        newValue = group.get("OptionId").toString();
                        oldValue = aval.getOptionIdAsString();
                    
                        if ( !newValue.equals("") )
                        {
                            AttributeOption newAttributeOption = 
                              AttributeOptionPeer
                              .retrieveByPK(new NumberKey(newValue));
                            newValue = newAttributeOption.getName();
                        }
                        if ( !oldValue.equals("") )
                        {
                            AttributeOption oldAttributeOption = 
                              AttributeOptionPeer
                              .retrieveByPK(aval.getOptionId());
                            oldValue = oldAttributeOption.getName();
                        }
                        
                    }
                    else 
                    {
                        newValue = group.get("Value").toString();
                        oldValue = aval.getValue();
                    }

                    if (!newValue.equals("") && !oldValue.equals(newValue))
                    {
                        group.setProperties(aval);
                        aval.startTransaction(transaction, attachment);
                        aval.save();
                    }
                } 
            }
            intake.removeAll();
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
        NumberKey typeId = null;
        Group group = null;
        ScarabUser user = (ScarabUser)data.getUser();

        if (type.equals("url"))
        {
            group = intake.get("Attachment", "urlKey", false);
            typeId = Attachment.URL__PK;
        } 
        else if (type.equals("comment")) 
        {
            group = intake.get("Attachment", "commentKey", false);
            typeId = Attachment.COMMENT__PK;
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
                attachment.setTextFields(user, issue, typeId);
                attachment.save();

                if (type.equals("url"))
                {
                    // Save transaction record
                    Transaction transaction = new Transaction();
                    transaction.create(user);

                    // Save activity record
                    Activity activity = new Activity();

                    // Generate description of modification
                    StringBuffer descBuf = new StringBuffer("added URL '");
                    descBuf.append(dataField.toString()).append("'");
                    String desc = descBuf.toString();
                    activity.create(issue, null, desc, transaction,
                                    attachment, null, null, "", "");
                }
                intake.remove(group);
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
        ScarabUser user = (ScarabUser)data.getUser();
        String id = data.getParameters().getString("id");
        Issue issue = (Issue) IssuePeer.retrieveByPK(new NumberKey(id));

        for (int i =0; i<keys.length; i++)
        {
            key = keys[i].toString();
            if (key.startsWith("url_delete_"))
            {
               attachmentId = key.substring(11);
               Attachment attachment = (Attachment) AttachmentPeer
                                     .retrieveByPK(new NumberKey(attachmentId));
               attachment.delete();

               // Save transaction record
               Transaction transaction = new Transaction();
               transaction.create(user);

               // Save activity record
               Activity activity = new Activity();

               // Generate description of modification
               StringBuffer descBuf = new StringBuffer("deleted URL '");
               descBuf.append(attachment.getDataAsString()).append("'");
               String desc = descBuf.toString();
               activity.create(issue, null, desc, transaction,
                               attachment, null, null, "", "");
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
        Issue issue = (Issue) IssuePeer.retrieveByPK(new NumberKey(id));
        ParameterParser params = data.getParameters();
        Object[] keys = params.getKeys();
        ScarabUser user = (ScarabUser)data.getUser();
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
                depend = parent.getDependency(issue);
                String oldValue = depend.getDependType().getName();
                String newValue = null;

                // User selected to remove the dependency
                if (dependTypeId.equals("none"))
                {
                    depend.delete();
                    newValue = "none";
                }
                else
                {
                    depend.setTypeId(dependTypeId);
                    DependType dependType = (DependType) DependTypePeer
                              .retrieveByPK(new NumberKey(dependTypeId));
                    newValue = dependType.getName();
                }
                depend.save();

               // Save transaction record
               Transaction transaction = new Transaction();
               transaction.create(user);

               // Save activity record
               Activity activity = new Activity();
               StringBuffer descBuf = new StringBuffer("changed dependency" + 
                                                       "type on Issue  ");
               descBuf.append(issue.getUniqueId());
               //descBuf.append(" from ").append(oldValue);
               //descBuf.append(" to ").append(newValue);
               String desc = descBuf.toString();
               activity.create(issue, null, desc,
                               transaction, null, null, null, "", "");
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
        Issue issue = (Issue) IssuePeer.retrieveByPK(new NumberKey(id));
        ScarabUser user = (ScarabUser)data.getUser();
        IntakeTool intake = (IntakeTool)context
            .get(ScarabConstants.INTAKE_TOOL);
        Group group = intake.get("Depend", "dependKey", false);
        boolean isValid = true;
        Issue parentIssue = null;

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
                parentIssue = (Issue) IssuePeer.retrieveByPK(
                                    new NumberKey(observedId.toString()));
                if (parentIssue.getDependency(issue) != null)
                {
                    observedId.setMessage("This issue already has a dependency on " +
                                          "the issue id you entered.");
                    isValid = false;
                }
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
            depend.setObserverId(issue.getIssueId());
            depend.setObservedId(new NumberKey(observedId.toString()));
            depend.setTypeId(new NumberKey(dependTypeId.toString()));

            // TODO: would like to set these properties using 
            // group.setProperties, but getting errors. (John?)
            //group.setProperties(depend);
            depend.save();
            intake.remove(group);

            // Save transaction record
            Transaction transaction = new Transaction();
            transaction.create(user);

            // Save activity record
            Activity activity = new Activity();
            StringBuffer descBuf = new StringBuffer("added dependency" + 
                                                    " on Issue  ");
            descBuf.append(issue.getUniqueId());
            String desc = descBuf.toString();
            activity.create(issue, null, desc,
                            transaction, null, null, null, "", "");
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
        Redirects to AssignIssue page.
    */
    public void doEditassignees(RunData data, TemplateContext context)
         throws Exception
    {        
        String id = data.getParameters().getString("id");
        data.getParameters().add("intake-grp", "issue"); 
        data.getParameters().add("issue", "_0"); 
        data.getParameters().add("issue_0id", id);
        data.getParameters().add("issue_id", id);
        setTarget(data, "AssignIssue.vm");            
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
