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
import java.util.HashMap;
import java.util.Map;

import java.text.SimpleDateFormat;

import org.apache.commons.collections.SequencedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScarabIssues implements java.io.Serializable
{
    private final static Log log = LogFactory.getLog(ScarabIssues.class);

    private Module module = null;
    private List issues = null;
    private Issue issue = null;
    private String importType = null;
    private int importTypeCode = -1;
    
    private List allDependencies = new ArrayList();
    /* maps the issue id in the xml file to the issue id of issue
       that was created from the xml file. */
    private Map issueCreatedMap = new HashMap();
    private Map issueExistingMap = new HashMap();

    private static final int CREATE_SAME_DB = 1;
    private static final int CREATE_DIFFERENT_DB = 2;
    private static final int UPDATE_SAME_DB = 3;

    public ScarabIssues() 
    {
        issues = new ArrayList();
    }

    public Module getModule()
    {
        return this.module;
    }

    public void setImportType(String value)
    {
        this.importType = value;
        if (importType.equals("create-same-db"))
        {
            importTypeCode = CREATE_SAME_DB;
        }
        else if (importType.equals("create-different-db"))
        {
            importTypeCode = CREATE_DIFFERENT_DB;
        }
        else if (importType.equals("update-same-db"))
        {
            importTypeCode = UPDATE_SAME_DB;
        }
    }

    public String getImportType()
    {
        return this.importType;
    }

    public int getImportTypeCode()
    {
        return this.importTypeCode;
    }

    public void setModule(Module module)
    {
        log.debug("Module.setModule(): " + module.getName());
        this.module = module;
    }

    public void doHandleDependencies()
        throws Exception
    {
        log.debug("Dealing with dependencies...");
        for (Iterator itr = allDependencies.iterator(); itr.hasNext();)
        {
            List dependencies = (List)itr.next();
            for (Iterator itr2 = dependencies.iterator(); itr2.hasNext();)
            {
                Dependency dependency = (Dependency)itr2.next();
                String child = (String)issueCreatedMap.get(dependency.getChild());
                String parent = (String)issueCreatedMap.get(dependency.getParent());
                String type = dependency.getType();
                
                if (getImportTypeCode() == UPDATE_SAME_DB)
                {
                }
                else
                {
                    try
                    {
                        @OM@.Depend dependOM = @OM@.DependManager.getInstance();
                        @OM@.Issue parentIssueOM = @OM@.Issue.getIssueById(parent);
                        @OM@.Issue childIssueOM = @OM@.Issue.getIssueById(child);
                        dependOM.setDefaultModule(parentIssueOM.getModule());
                        dependOM.setObservedId(parentIssueOM.getIssueId());
                        dependOM.setObserverId(childIssueOM.getIssueId());
                        dependOM.setDependType(type);
                        @OM@.ScarabUser creUser = @OM@.ScarabUserManager
                            .getInstance("jon@latchkey.com", parentIssueOM.getModule().getDomain());
                        parentIssueOM.doAddDependency(null, dependOM, childIssueOM, creUser);
                        log.debug("Added Dep Type: " + type + " Parent: " + parent + " Child: " + child);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        throw new Exception();
                    }
                }
            }
        }
/*
            Dependency dependency = (Dependency) depitr.next();
            if (dependency.getChild() != null)
            {
                @OM@.Issue dependIssueOM = @OM@.Issue.getIssueById(dependency.getChild());
            }
            else
            {
                @OM@.Issue dependIssueOM = @OM@.Issue.getIssueById(dependency.getParent());
            }

            @OM@.Depend dependencyOM = @OM@.DependManager
                .getInstance(dependency.getChild(), dependency.getParent(), 
                             dependency.getType());
*/
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
        try
        {
            doIssueEvent(getModule(), getIssue());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    private @OM@.Issue createNewIssue(Module module, Issue issue)
        throws Exception
    {
        // get the instance of the module
        @OM@.Module moduleOM = @OM@.ModuleManager.getInstance(module.getName(), module.getCode());
        // get the instance of the issue type
        @OM@.IssueType issueTypeOM = @OM@.IssueType.getInstance(issue.getArtifactType());
        issueTypeOM.setName(issue.getArtifactType());
        // get me a new issue since we couldn't find one before
        @OM@.Issue issueOM = @OM@.Issue.getNewInstance(moduleOM, issueTypeOM);
        // create the issue in the database
        issueOM.save();
        // add the mapping between the issue id and the id that was created
        issueCreatedMap.put(issueOM.getUniqueId(), module.getCode() + issue.getId());
        issueExistingMap.put(module.getCode() + issue.getId(), issueOM.getUniqueId());
        log.debug("Created new issue: " + issueOM.getUniqueId());
        return issueOM;
    }    

    private void doIssueEvent(Module module, Issue issue)
        throws Exception
    {
/////////////////////////////////////////////////////////////////////////////////  
        // Get me an issue
        @OM@.Issue issueOM = null;
        if (getImportTypeCode() == CREATE_SAME_DB || getImportTypeCode() == CREATE_DIFFERENT_DB)
        {
            issueOM = createNewIssue(module, issue);
        }
        else
        {
            issueOM = @OM@.Issue.getIssueById(module.getCode() + issue.getId());
            if (issueOM == null)
            {
                issueOM = createNewIssue(module, issue);
            }
            else
            {
                log.debug("Found issue in db: " + issueOM.getUniqueId());
            }
        }

        issueCreatedMap.put(module.getCode() + issue.getId(), issueOM.getUniqueId());

/////////////////////////////////////////////////////////////////////////////////  

        // Loop over the XML activitySets
        List activitySets = issue.getActivitySets();
        log.debug("Number of activitySets in issue: " + activitySets.size());
        for (Iterator itr = activitySets.iterator(); itr.hasNext();)
        {
            ActivitySet activitySet = (ActivitySet) itr.next();

/////////////////////////////////////////////////////////////////////////////////  
            // Deal with the attachment for the activitySet
            Attachment activitySetAttachment = activitySet.getAttachment();
            @OM@.Attachment activitySetAttachmentOM = null;
            if (activitySetAttachment != null)
            {
                if (getImportTypeCode() == UPDATE_SAME_DB)
                {
                    try
                    {
                        activitySetAttachmentOM = @OM@.AttachmentManager
                            .getInstance(activitySetAttachment.getId());
                        log.debug("Found existing ActivitySet attachment");
                    }
                    catch (Exception e)
                    {
                        activitySetAttachmentOM = createAttachment(issueOM, module, activitySetAttachment);
                    }
                }
                else
                {
                    activitySetAttachmentOM = createAttachment(issueOM, module, activitySetAttachment);
                    log.debug("Created ActivitySet attachment object");
                }
            }
            else
            {
                log.debug("OK- No attachment in this ActivitySet");
            }

/////////////////////////////////////////////////////////////////////////////////  
            // Attempt to get the activitySet OM
            @OM@.ActivitySet activitySetOM = null;
            if (getImportTypeCode() == UPDATE_SAME_DB)
            {
                try
                {
                    activitySetOM = @OM@.ActivitySetManager.getInstance(activitySet.getId());
                    log.debug("Found activitySet in db: " + activitySetOM.getActivitySetId());
                }
                catch (Exception e)
                {
                    activitySetOM = @OM@.ActivitySetManager.getInstance();
                }
            }
            else
            {
                activitySetOM = @OM@.ActivitySetManager.getInstance();
                log.debug("Created new activitySet");
            }

/////////////////////////////////////////////////////////////////////////////////  

            // Get the ActivitySet type/createdby values (we know these are valid)
            @OM@.ActivitySetType ttOM = @OM@.ActivitySetTypeManager.getInstance(activitySet.getType());
            activitySetOM.setActivitySetType(ttOM);
            @OM@.ScarabUser activitySetCreatedByOM = @OM@.ScarabUserManager.getInstance(activitySet.getCreatedBy(), 
                 module.getDomain());
            activitySetOM.setCreatedBy(activitySetCreatedByOM.getUserId());
            activitySetOM.setCreatedDate(activitySet.getCreatedDate().getDate());
            if (activitySetAttachmentOM != null)
            {
                activitySetAttachmentOM.save();
                activitySetOM.setAttachment(activitySetAttachmentOM);
            }
            activitySetOM.save();

/////////////////////////////////////////////////////////////////////////////////  

            // Deal with the activities in the activitySet
            List activities = activitySet.getActivities();
            log.debug("Number of activities in activitySetid '" + activitySetOM.getActivitySetId() + 
                "': " + activities.size());

            for (Iterator itrb = activities.iterator(); itrb.hasNext();)
            {
                Activity activity = (Activity) itrb.next();

                // FIXME: this doesn't work for other languages!!!!!
                if (isDependencyActivity(activity.getDescription()))
                {
                    // we will take care of this later when we process
                    // the dependency tags and that will create the
                    // activity records for us.
                    continue;
                }

                // Get the Attribute associated with the Activity
                @OM@.Attribute attributeOM = @OM@.Attribute.getInstance(activity.getAttribute());

                // create the activityOM
                @OM@.Activity activityOM = createActivity(activity, activitySet, module, 
                                         issueOM, attributeOM, activitySetOM);

                // check to see if this is a new activity or an update activity
                SequencedHashMap avMap = issueOM.getModuleAttributeValuesMap();
                Iterator moduleAttributeValueItr = avMap.iterator();
                while (moduleAttributeValueItr.hasNext()) 
                {
                    @OM@.AttributeValue avalOM = 
                        (@OM@.AttributeValue)avMap.get(moduleAttributeValueItr.next());
                    @OM@.Attribute avalAttributeOM = avalOM.getAttribute();

                    @OM@.AttributeValue avalOM2 = null;
                    if (!activity.isNewActivity())
                    {
                        avalOM2 = @OM@.AttributeValue
                            .getNewInstance(avalAttributeOM.getAttributeId(), 
                                                          avalOM.getIssue());
                        avalOM2.setProperties(avalOM);
                    }
                    
                    if (avalAttributeOM.equals(attributeOM))
                    {
                        log.debug("Attribute match!");

                        if (avalAttributeOM.isOptionAttribute())
                        {
                            log.debug("we have an option attribute");
                            @OM@.AttributeOption newAttributeOptionOM = @OM@.AttributeOption
                                .getInstance(attributeOM, activity.getNewOption());
                            if (activity.isNewActivity())
                            {
                                avalOM.setOptionId(newAttributeOptionOM.getOptionId());
                            }
                            else
                            {
                                if (!newAttributeOptionOM.getOptionId()
                                    .equals(avalOM.getOptionId()))
                                {
                                    avalOM2.setOptionId(newAttributeOptionOM.getOptionId());
                                }
                            }
                        }
                        else if (avalAttributeOM.isUserAttribute())
                        {
                            log.debug("we have a user attribute");
                            @OM@.ScarabUser newUserOM = @OM@.ScarabUserManager
                                .getInstance(activity.getNewUser(), module.getDomain());
                            if (activity.isNewActivity())
                            {
                                avalOM.setUserId(newUserOM.getUserId());
                            }
                            else
                            {
                                if (!newUserOM.getUserId()
                                    .equals(avalOM.getUserId()))
                                {
                                    avalOM2.setUserId(newUserOM.getUserId());
                                }
                            }
                        }
                        else if (avalAttributeOM.isTextAttribute())
                        {
                            log.debug("we have a text attribute");
                            if (activity.isNewActivity())
                            {
                                avalOM.setValue(activity.getNewValue());
                            }
                            else
                            {
                                if (!activity.getNewValue()
                                    .equals(avalOM.getValue()))
                                {
                                    avalOM2.setValue(activity.getNewValue());
                                }
                            }
                        }
                        avalOM.startActivitySet(activitySetOM);
                        avalOM.setAttribute(attributeOM);
                        avalOM.setActivityDescription(activity.getDescription());
                        if (!activity.isNewActivity())
                        {
                            avalOM.setProperties(avalOM2);
                        }
                        avalOM.save();
                    }
                }
                issueOM.save();

/*
                // Get the Attribute associated with the Activity
                @OM@.Attribute attributeOM = @OM@.Attribute.getInstance(activity.getAttribute());
                @OM@.Activity activityOM = null;
                if (getImportTypeCode() == UPDATE_SAME_DB)
                {
                    try
                    {
                        activityOM = @OM@.ActivityManager.getInstance(activity.getId());
                    }
                    catch (Exception e)
                    {
                        activityOM = @OM@.ActivityManager.getInstance();
                    }
                }
                else
                {
                    activityOM = @OM@.ActivityManager.getInstance();
                }

                activityOM.setIssue(issueOM);
                activityOM.setAttribute(attributeOM);
                activityOM.setActivitySet(activitySetOM);
                activityOM.setEndDate(activitySet.getCreatedDate().getDate());
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

                @OM@.Attachment newAttachmentOM = null;
                if (activity.getAttachment() != null)
                {
                    newAttachmentOM = createAttachment(issueOM, module, activity.getAttachment());
                    newAttachmentOM.save();
                    activityOM.setAttachment(newAttachmentOM);
                }
                attributeValueOM.setActivityDescription(activity.getDescription());
                attributeValueOM.startActivitySet(activitySetOM);
                attributeValueOM.save();
*/
            }
        }

/////////////////////////////////////////////////////////////////////////////////  
        // deal with dependencies
        List dependencies = issue.getDependencies();
        log.debug("Number of dependencies found: " + dependencies.size());

        List finalDependencies = new ArrayList();
        for (Iterator depitr = dependencies.iterator(); depitr.hasNext();)
        {
            Dependency dependency = (Dependency) depitr.next();
            String mapId = issueOM.getUniqueId();
            if (dependency.getChild() != null)
            {
                dependency.setParent(mapId);
            }
            else
            {
                dependency.setChild(mapId);
            }
            
            if (isDuplicateDependency(dependency))
            {
                log.debug("Found duplicate dependency.");
                continue;
            }
            finalDependencies.add(dependency);
            log.debug("Dep add: " + dependency);
        }
        allDependencies.add(finalDependencies);
    }

    /**
     * this is a hack to determine if the activty is a dependency activity
     * FIXME: consider adding an ACTIVITY_TYPE table?
     */
    private boolean isDependencyActivity(String description)
    {
        if (description.indexOf("parent dependency on issue") > 0 ||
            description.indexOf("child dependency on issue") > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean isDuplicateDependency(Dependency dependency)
    {
        for (Iterator itr = allDependencies.iterator(); itr.hasNext();)
        {
            List dependencies = (List)itr.next();
            for (Iterator itr2 = dependencies.iterator(); itr2.hasNext();)
            {
                Dependency dep = (Dependency)itr2.next();
                String child = (String)issueCreatedMap.get(dep.getChild());
                String parent = (String)issueCreatedMap.get(dep.getParent());
                String type = dep.getType();
                if (child.equals(dependency.getChild()) &&
                    parent.equals(dependency.getParent()) &&
                    type.equals(dependency.getType()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private @OM@.Activity createActivity(Activity activity, ActivitySet activitySet, Module module,
                                         @OM@.Issue issueOM, 
                                         @OM@.Attribute attributeOM,
                                         @OM@.ActivitySet activitySetOM)
        throws Exception
    {
        @OM@.Activity activityOM = null;
        if (getImportTypeCode() == UPDATE_SAME_DB)
        {
            try
            {
                activityOM = @OM@.ActivityManager.getInstance(activity.getId());
            }
            catch (Exception e)
            {
                activityOM = @OM@.ActivityManager.getInstance();
            }
        }
        else
        {
            activityOM = @OM@.ActivityManager.getInstance();
        }

        activityOM.setIssue(issueOM);
        activityOM.setAttribute(attributeOM);
        activityOM.setActivitySet(activitySetOM);
        activityOM.setEndDate(activitySet.getCreatedDate().getDate());

        // Set the attachment for the activity
        @OM@.Attachment newAttachmentOM = null;
        if (activity.getAttachment() != null)
        {
            newAttachmentOM = createAttachment(issueOM, module, activity.getAttachment());
            newAttachmentOM.save();
            activityOM.setAttachment(newAttachmentOM);
        }

        log.debug("Created new activity");
        return activityOM;
    }

    private @OM@.Attachment createAttachment(@OM@.Issue issueOM, Module module,
                                             Attachment attachment)
        throws Exception
    {
        @OM@.Attachment attachmentOM = @OM@.AttachmentManager.getInstance();
        attachmentOM.setIssue(issueOM);
        attachmentOM.setName(attachment.getName());
        attachmentOM.setAttachmentType(@OM@.AttachmentType.getInstance(attachment.getType()));
        attachmentOM.setMimeType(attachment.getMimetype());
        attachmentOM.setFileName(attachment.getPath());
        attachmentOM.setData(attachment.getData());
        attachmentOM.setCreatedDate(attachment.getCreatedDate().getDate());
        attachmentOM.setModifiedDate(attachment.getModifiedDate().getDate());
        @OM@.ScarabUser creUser = @OM@.ScarabUserManager
            .getInstance(attachment.getCreatedBy(), issueOM.getModule().getDomain());
        if (creUser != null)
        {
            attachmentOM.setCreatedBy(creUser.getUserId());
        }
        @OM@.ScarabUser modUser = @OM@.ScarabUserManager
            .getInstance(attachment.getModifiedBy(), issueOM.getModule().getDomain());
        if (modUser != null)
        {
            attachmentOM.setModifiedBy(modUser.getUserId());
        }
        return attachmentOM;
    }
}
