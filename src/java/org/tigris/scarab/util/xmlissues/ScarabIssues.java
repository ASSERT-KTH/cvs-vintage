package org.tigris.scarab.util.xmlissues;

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

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScarabIssues implements java.io.Serializable
{
    private final static Log log = LogFactory.getLog(ScarabIssues.class);

    private Module module = null;
    private List issues = null;
    private Issue issue = null;

    public ScarabIssues() 
    {
        issues = new ArrayList();
    }

    public Module getModule()
    {
        return this.module;
    }

    public void setModule(Module module)
    {
        log.debug("Module.setModule(): " + module.getName());
        this.module = module;
    }

    public List getIssues()
    {
        return issues;
    }

    public Issue getIssue()
    {
        return this.issue;
    }

    public void addIssue(Issue issue)
        throws Exception
    {
        log.debug("Module.addIssue(): " + issue.getId());
        this.issue = issue;
        doIssueEvent(getModule(), getIssue());
    }
    
    private void doIssueEvent(Module module, Issue issue)
        throws Exception
    {
        // Get me an issue
        @OM@.Issue issueOM = @OM@.Issue.getIssueById(module.getCode() + issue.getId());
        // failed to find the issue
        if (issueOM == null)
        {
            // get the instance of the module
            @OM@.Module moduleOM = @OM@.ModuleManager.getInstance(module.getName(), module.getCode());
            // get the instance of the issue type
            @OM@.IssueType issueTypeOM = @OM@.IssueType.getInstance(issue.getArtifactType());
            issueTypeOM.setName(issue.getArtifactType());
            // get me a new issue since we couldn't find one before
            issueOM = @OM@.Issue.getNewInstance(moduleOM, issueTypeOM);
            // create the issue in the database
            issueOM.save();
            log.debug("Created new issue: " + issueOM.getUniqueId());
        }
        else
        {
            log.debug("Found issue in db: " + issueOM.getUniqueId());
        }

        // Loop over the XML transactions
        List transactions = issue.getTransactions();
        log.debug("Number of transactions in issue: " + transactions.size());
        for (Iterator itr = transactions.iterator(); itr.hasNext();)
        {
            Transaction transaction = (Transaction) itr.next();

            // deal with the attachment for the transaction
            Attachment attachment = transaction.getAttachment();
            @OM@.Attachment attachmentOM = null;
            if (attachment != null)
            {
                attachmentOM = @OM@.AttachmentManager.getInstance(attachment.getId());
                if (attachmentOM == null)
                {
                    attachmentOM = createAttachment(issueOM, attachment);
                }
            }
            // attempt to get the transaction OM
            @OM@.Transaction transactionOM = @OM@.TransactionManager.getInstance(transaction.getId());
            if (transactionOM == null)
            {
                transactionOM = @OM@.TransactionManager.getInstance();
                log.debug("Created new transaction");
            }
            else
            {
                log.debug("Found transaction in db: " + transactionOM.getTransactionId());
            }
            // get the type/createdby values (we know these are valid)
            @OM@.TransactionType ttOM = @OM@.TransactionTypeManager.getInstance(transaction.getType());
            @OM@.ScarabUser createdByOM = @OM@.ScarabUserManager.getInstance(transaction.getCreatedBy(), 
                 module.getDomain());
            transactionOM.setTransactionType(ttOM);
            transactionOM.setCreatedBy(createdByOM.getUserId());
            transactionOM.setAttachment(attachmentOM);
            transactionOM.save();

            // deal with the activities in the transaction
            List activities = transaction.getActivities();
            log.debug("Number of activities in transactionid '" + transactionOM.getTransactionId() + 
                "': " + activities.size());
            for (Iterator itrb = activities.iterator(); itrb.hasNext();)
            {
                Activity activity = (Activity) itrb.next();
                
                // get the Attribute associated with the Activity
                @OM@.Attribute attributeOM = @OM@.Attribute.getInstance(activity.getAttribute());
                @OM@.Activity activityOM = @OM@.ActivityManager.getInstance(activity.getId());
                if (activityOM == null)
                {
                    log.debug("Created new activity");
                    
                    @OM@.AttributeValue attributeValueOM = 
                        @OM@.AttributeValue.getNewInstance(attributeOM, issueOM);
                    if (attributeOM.isOptionAttribute())
                    {
                        @OM@.AttributeOption newAttributeOptionOM = null;
                        @OM@.AttributeOption oldAttributeOptionOM = null;
                        
                        if (activity.getNewValue() != null)
                        {
                            newAttributeOptionOM = @OM@.AttributeOption
                                .getInstance(attributeOM, activity.getNewOption());
                        }
/*
                        if (activity.getOldValue())
                        {
                            oldAttributeOptionOM = @OM@.AttributeOption
                                .getInstance(attributeOM, activity.getOldOption());
                        }
*/
                        attributeValueOM.setOptionId(newAttributeOptionOM.getOptionId());
                    }
                    else if (attributeOM.isUserAttribute())
                    {
                        @OM@.ScarabUser newUserOM = @OM@.ScarabUserManager.getInstance(activity.getNewUser(), 
                            module.getDomain());
                        attributeValueOM.setUserId(newUserOM.getUserId());
                    }
                    else if (attributeOM.isTextAttribute())
                    {
                        attributeValueOM.setValue(activity.getNewValue());
                    }

                    attributeValueOM.startTransaction(transactionOM);
                    attributeValueOM.save();

                    if (activity.getAttachment() != null)
                    {
                        @OM@.Activity saveActivityOM = attributeValueOM.getActivity();
                        @OM@.Attachment newAttachmentOM = createAttachment(issueOM, activity.getAttachment());
                        saveActivityOM.setAttachment(newAttachmentOM);
                        saveActivityOM.save();
                    }
                }
                else
                {
                    log.debug("Found activity in database, do we want to be able to update it? I don't think so.");
                }
            }
        }

        // deal with dependencies
        List dependencies = issue.getDependencies();
        log.debug("Number of dependencies found: " + dependencies.size());
        for (Iterator depitr = dependencies.iterator(); depitr.hasNext();)
        {
            Dependency dependency = (Dependency) depitr.next();
//            @OM@.Depend dependencyOM = @OM@.DependManager.getInstance(dependency.getChild(), dependency.getParent(), dependency.getType());
        }
    }

    private @OM@.Attachment createAttachment(@OM@.Issue issueOM, 
                                             Attachment attachment)
        throws Exception
    {
        @OM@.Attachment attachmentOM = @OM@.AttachmentManager.getInstance();
        attachmentOM.setIssue(issueOM);
        attachmentOM.setName(attachment.getName());
        attachmentOM.setAttachmentType(@OM@.AttachmentType.getInstance(attachment.getType()));
        attachmentOM.setFileName(attachment.getPath());
        attachmentOM.setName(attachment.getName());
        SimpleDateFormat sdf = new SimpleDateFormat(attachment.getCreatedDate().getFormat());
        attachmentOM.setCreatedDate(sdf.parse(attachment.getCreatedDate().getTimestamp()));
        sdf = new SimpleDateFormat(attachment.getModifiedDate().getFormat());
        attachmentOM.setCreatedDate(sdf.parse(attachment.getModifiedDate().getTimestamp()));
        attachmentOM.save();
        return attachmentOM;
    }
}
