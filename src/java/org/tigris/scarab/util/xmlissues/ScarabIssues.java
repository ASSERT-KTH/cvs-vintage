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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.io.File;

import org.apache.fulcrum.localization.Localization;
import org.apache.commons.collections.SequencedHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tigris.scarab.util.ScarabConstants;

/**
 * This class manages the validation and importing of issues.
 *
 * This classes has a format dictated by the betwixt parser. This class is 
 * given to the betwixt parser and as betwixt parses elements of the xml file,
 * it looks for the public getters and setters in this class's signature and
 * thereby makes determinations on what to call in this class. What this means
 * in effect is that you can not read this file in isolation: Doing so would
 * make you do things like remove the data member 'issue' and its accessor
 * methods because it looks as though they are unused whereas in fact they are
 * signals to the betwixt parser: It reads them and interprets their presence
 * and instruction to create instances of issues from the xml being parsed.
 *
 * <p>Also, of note, the design of this class is that, it has two modes based
 * off the setting of the inValidationMode class.  When parsing w/ the
 * inValidationMode flag set, the db is not touched. The code just validates
 * the xml's data content checking the users exist in the db, that the 
 * attributes and modules referenced already exit.  A parse w/ the 
 * inValidationMode set to false will do actual insert of the xml issues.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ScarabIssues.java,v 1.32 2003/04/21 19:39:15 jackrepenning Exp $
 */
public class ScarabIssues implements java.io.Serializable
{
    private static final Log LOG = LogFactory.getLog(ScarabIssues.class);

    private Module module = null;

    /**
     * Betwixt parser adds here instance of issues found in parsed xml.
     */
    private List issues = null;

    private Issue issue = null;
    private String importType = null;
    private int importTypeCode = -1;
    
    private List allDependencies = new ArrayList();

    /** maps the issue id in the xml file to the issue id of issue
       that was created from the xml file. */
    private Map issueXMLMap = new HashMap();

    /** left side is the id in the XML file. right side is the created id in the db */
    private Map activitySetIdMap = new HashMap();

    /** left side is the id in the XML file. right side is the created id in the db */
    private List dependActivitySetId = new ArrayList();

    private static final int CREATE_SAME_DB = 1;
    private static final int CREATE_DIFFERENT_DB = 2;
    private static final int UPDATE_SAME_DB = 3;

    private static @OM@.Attribute nullAttribute = null;

    /** We default to be in validation mode. insert only happens after validation has happened. */
    private static boolean inValidationMode = true;
    private List importErrors = null;
    private List importUsers = null;

    public ScarabIssues() 
    {
        issues = new ArrayList();
        if (nullAttribute == null)
        {
            try
            {
                nullAttribute = @OM@.Attribute.getInstance(0);
            }
            catch (Exception e)
            {
                LOG.debug("Could not assign nullAttribute");
            }
        }
    }

    public static void setInValidationMode(boolean value)
    {
        inValidationMode=value;
    }

    public static boolean isInValidationMode()
    {
        return inValidationMode;
    }
    
    /**
     * Return list of import errors if any.
     * 
     * Has funny name.  If we named it 'getImportErrors', because its public,
     * betwixt errors trying to find the corresponding 'setImportErrors'.
     *
     * @return List of import errors if any.
     */
    public List doGetImportErrors()
    {
        return importErrors;
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

    /**
     * @return Map of original id -> new scarab id.
     */
    public Map getIDs()
    {
        return this.issueXMLMap;
    }

    public Module getModule()
    {
        return this.module;
    }

    public void setModule(Module module)
    {
        LOG.debug("Module.setModule(): " + module.getName());
        this.module = module;
    }

    void doValidateUsers()
        throws Exception
    {
        if (importUsers != null && importUsers.size() > 0)
        {
            for (Iterator itr = importUsers.iterator(); itr.hasNext();)
            {
                String userStr = (String)itr.next();
                try
                {
                    @OM@.ScarabUser user = @OM@.ScarabUserManager.getInstance(userStr, 
                         getModule().getDomain());
                    if (user == null)
                    {
                        throw new Exception();
                    }
                }
                catch (Exception e)
                {
                    String error = Localization.format(
                        ScarabConstants.DEFAULT_BUNDLE_NAME,
                        getLocale(),
                        "CouldNotLocateUsername", userStr);
                    importErrors.add(error);
                }
            }
        }
    }

    void doValidateDependencies()
        throws Exception
    {
        if (allDependencies != null && allDependencies.size() > 0)
        {
            for (Iterator itr = allDependencies.iterator(); itr.hasNext();)
            {
                Activity activity = (Activity)itr.next();
                Dependency dependency = activity.getDependency();
                String child = (String)issueXMLMap.get(dependency.getChild());
                String parent = (String)issueXMLMap.get(dependency.getParent());
                if (parent == null || child == null)
                {
                    LOG.debug("Could not find issues: parent: " + parent + " child: " + child);
                    continue;
                }
                try
                {
                    @OM@.Issue parentIssueOM = @OM@.IssueManager.getIssueById(parent);
                    if (parentIssueOM == null)
                    {
                        throw new Exception();
                    }
                }
                catch (Exception e)
                {
                    String error = Localization.format(
                        ScarabConstants.DEFAULT_BUNDLE_NAME,
                        getLocale(),
                        "CouldNotLocateParentDepend", parent);
                    importErrors.add(error);
                }
                try
                {
                    @OM@.Issue childIssueOM = @OM@.IssueManager.getIssueById(child);
                    if (childIssueOM == null)
                    {
                        throw new Exception();
                    }
                }
                catch (Exception e)
                {
                    String error = Localization.format(
                        ScarabConstants.DEFAULT_BUNDLE_NAME,
                        getLocale(),
                        "CouldNotLocateChildDepend", child);
                    importErrors.add(error);
                }
            }
        }
        allDependencies.clear();
    }

    void doHandleDependencies()
        throws Exception
    {
        LOG.debug("Number of dependencies found: " + allDependencies.size());
        for (Iterator itr = allDependencies.iterator(); itr.hasNext();)
        {
            Object[] data = (Object[])itr.next();
            @OM@.ActivitySet activitySetOM = (@OM@.ActivitySet) data[0];
            Activity activity = (Activity) data[1];
            @OM@.Attachment activityAttachmentOM = (@OM@.Attachment) data[2];

            Dependency dependency = activity.getDependency();
            String child = (String)issueXMLMap.get(dependency.getChild());
            String parent = (String)issueXMLMap.get(dependency.getParent());
            if (parent == null || child == null)
            {
                LOG.debug("Could not find issues: parent: " + parent + " child: " + child);
                continue;
            }

            if (getImportTypeCode() == UPDATE_SAME_DB)
            {
                System.out.println("Not implemented yet.");
            }
            else
            {
                try
                {
                    String type = dependency.getType();
                    @OM@.Depend newDependOM = @OM@.DependManager.getInstance();
                    @OM@.Issue parentIssueOM = @OM@.IssueManager.getIssueById(parent);
                    @OM@.Issue childIssueOM = @OM@.IssueManager.getIssueById(child);
                    newDependOM.setDefaultModule(parentIssueOM.getModule());
                    newDependOM.setObservedId(parentIssueOM.getIssueId());
                    newDependOM.setObserverId(childIssueOM.getIssueId());
                    newDependOM.setDependType(type);
                    LOG.debug("Dep: " + dependency.getId() + " Type: " + type + " Parent: " + parent + " Child: " + child);
                    LOG.debug("XML Activity id: " + activity.getId());
                    if (activity.isAddDependency())
                    {
                        parentIssueOM
                          .doAddDependency(activitySetOM, newDependOM, childIssueOM, null);
                        LOG.debug("Added Dep Type: " + type + " Parent: " + parent + " Child: " + child);
                        LOG.debug("----------------------------------------------------");
                    }
                    else if (activity.isDeleteDependency())
                    {
                        parentIssueOM
                          .doDeleteDependency(activitySetOM, newDependOM, null);
                        LOG.debug("Deleted Dep Type: " + type + " Parent: " + parent + " Child: " + child);
                        LOG.debug("----------------------------------------------------");
                    }
                    else if (activity.isUpdateDependency())
                    {
                        @OM@.Depend oldDependOM = parentIssueOM.getDependency(childIssueOM);
                        if (oldDependOM == null)
                        {
                            throw new Exception ("Whoops! Could not find the original dependency!");
                        }
                        // we definitely know we are doing an update here.
                        newDependOM.setDeleted(false);
                        parentIssueOM
                          .doChangeDependencyType(activitySetOM, oldDependOM, newDependOM, null);
                        LOG.debug("Updated Dep Type: " + type + " Parent: " + parent + " Child: " + child);
                        LOG.debug("Old Type: " + oldDependOM.getDependType().getName() + " New type: " + newDependOM.getDependType().getName());
                        LOG.debug("----------------------------------------------------");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
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
        LOG.debug("Module.addIssue(): " + issue.getId());
        this.issue = issue;
        try
        {
            if (inValidationMode)
            {
                importUsers = new ArrayList();
                importErrors = new ArrayList();
                doIssueValidateEvent(getModule(), getIssue());
            }
            else
            {
                doIssueEvent(getModule(), getIssue());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

    private void doIssueValidateEvent(Module module, Issue issue)
        throws Exception
    {
        // check for existing module
        @OM@.Module moduleOM = null;
        try
        {
            moduleOM = @OM@.ModuleManager.getInstance(module.getDomain(), module.getName(), module.getCode());
            if (moduleOM == null)
            {
                throw new Exception();
            }
// commented out for now cause this isn't really necessary/required data            
//            importUsers.add(module.getOwner());
        }
        catch (Exception e)
        {
            Object[] args = {module.getName(), module.getCode(), module.getDomain()};
            String error = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "CouldNotFindModule", args);
            importErrors.add(error);
        }

        // get the instance of the issue type
        @OM@.IssueType issueTypeOM = null;
        try
        {
            issueTypeOM = @OM@.IssueType.getInstance(issue.getArtifactType());
            if (issueTypeOM == null)
            {
                throw new Exception();
            }
        }
        catch (Exception e)
        {
            String error = Localization.format(
                ScarabConstants.DEFAULT_BUNDLE_NAME,
                getLocale(),
                "CouldNotFindIssueType", issue.getArtifactType());
            importErrors.add(error);
        }

        List moduleAttributeList = null;
        if (moduleOM != null)
        {
            moduleAttributeList = moduleOM.getAttributes(issueTypeOM);
        }

        List activitySets = issue.getActivitySets();
        for (Iterator itr = activitySets.iterator(); itr.hasNext();)
        {
            ActivitySet activitySet = (ActivitySet) itr.next();
            if (activitySet.getCreatedBy() != null)
            {
                importUsers.add(activitySet.getCreatedBy());
            }
            if (activitySet.getAttachment() != null)
            {
                String attachCreatedBy = activitySet.getAttachment().getCreatedBy();
                if (attachCreatedBy != null)
                {
                    importUsers.add(attachCreatedBy);
                }
            }
            
            // validate the activity set types
            try
            {
                @OM@.ActivitySetType ttOM = @OM@.ActivitySetTypeManager.getInstance(activitySet.getType());
                if (ttOM == null)
                {
                    throw new Exception();
                }
            }
            catch (Exception e)
            {
                String error = Localization.format(
                    ScarabConstants.DEFAULT_BUNDLE_NAME,
                    getLocale(),
                    "CouldNotFindActivitySetType", activitySet.getType());
                importErrors.add(error);
            }
    
            List activities = activitySet.getActivities();
            for (Iterator itrb = activities.iterator(); itrb.hasNext();)
            {
                Activity activity = (Activity) itrb.next();
                if (activity.getOldUser() != null)
                {
                    importUsers.add(activity.getOldUser());
                }
                if (activity.getNewUser() != null)
                {
                    importUsers.add(activity.getNewUser());
                }
                Attachment activityAttachment = activity.getAttachment();
                if (activityAttachment != null)
                {
                    if (activityAttachment.getReconcilePath() &&
                       ! new File(activityAttachment.getFilename()).exists())
                    {
                        String error = Localization.format(
                            ScarabConstants.DEFAULT_BUNDLE_NAME,
                            getLocale(),
                            "CouldNotFindFileAttachment", activityAttachment.getFilename());
                        importErrors.add(error);
                    }

                    String attachCreatedBy = activityAttachment.getCreatedBy();
                    if (attachCreatedBy != null)
                    {
                        importUsers.add(attachCreatedBy);
                    }
                }

                // Get the Attribute associated with the Activity
                @OM@.Attribute attributeOM = null;
                String activityAttribute = activity.getAttribute();
                try
                {
                    attributeOM = @OM@.Attribute.getInstance(activityAttribute);
                    if (attributeOM == null)
                    {
                        throw new Exception();
                    }
                }
                catch (Exception e)
                {
                    String error = Localization.format(
                        ScarabConstants.DEFAULT_BUNDLE_NAME,
                        getLocale(),
                        "CouldNotFindGlobalAttribute", activityAttribute);
                    importErrors.add(error);
                }

                if (attributeOM != null)
                {
                    if (attributeOM.equals(nullAttribute))
                    {
                        // add any dependency activities to a list for later processing
                        if (isDependencyActivity(activity))
                        {
                            if (!isDuplicateDependency(activitySet))
                            {
                                allDependencies.add(activity);
                                LOG.debug("-------------Stored Dependency # " + allDependencies.size() + "-------------");
                            }
                            continue;
                        }
                    }
                    else
                    {
                        // the null attribute will never be in this list, so don't check for it.
                        if (moduleAttributeList != null && !moduleAttributeList.contains(attributeOM))
                        {
                            String error = Localization.format(
                                ScarabConstants.DEFAULT_BUNDLE_NAME,
                                getLocale(),
                                "CouldNotFindRModuleAttribute", activityAttribute);
                            importErrors.add(error);
                        }
                    }
                }
                else if (activity.getNewOption() != null)
                {
                    // check for global options
                    @OM@.AttributeOption attributeOptionOM = null;
                    try
                    {
                        attributeOptionOM = @OM@.AttributeOption
                            .getInstance(attributeOM, activity.getNewOption());
                        if (attributeOptionOM == null)
                        {
                            throw new Exception();
                        }
                    }
                    catch (Exception e)
                    {
                        Object[] args = {activity.getNewOption(), ((attributeOM != null)?
                                attributeOM.getName(): "null")};
                        String error = Localization.format(
                            ScarabConstants.DEFAULT_BUNDLE_NAME,
                            getLocale(),
                            "CouldNotFindAttributeOption", args);
                        importErrors.add(error);
                    }
                    // check for module options
                    try
                    {
                        @OM@.RModuleOption rmo = @OM@.RModuleOptionManager
                            .getInstance(moduleOM, issueTypeOM, attributeOptionOM);
                        if (rmo == null)
                        {
                            throw new Exception();
                        }
                    }
                    catch (Exception e)
                    {
                        Object[] args = {activity.getNewOption(), ((attributeOM != null)?
                                attributeOM.getName(): "null")};
                        String error = Localization.format(
                            ScarabConstants.DEFAULT_BUNDLE_NAME,
                            getLocale(),
                            "CouldNotFindModuleAttributeOption", args);
                        importErrors.add(error);
                    }
                }
                else if (activity.getOldOption() != null)
                {
                    @OM@.AttributeOption attributeOptionOM = null;
                    try
                    {
                        attributeOptionOM = @OM@.AttributeOption
                            .getInstance(attributeOM, activity.getOldOption());
                        if (attributeOptionOM == null)
                        {
                            throw new Exception();
                        }
                    }
                    catch (Exception e)
                    {
                        String error = Localization.format(
                            ScarabConstants.DEFAULT_BUNDLE_NAME,
                            getLocale(),
                            "CouldNotFindAttributeOption", activity.getOldOption());
                        importErrors.add(error);
                    }
                    // check for module options
                    try
                    {
                        @OM@.RModuleOption rmo = @OM@.RModuleOptionManager
                            .getInstance(moduleOM, issueTypeOM, attributeOptionOM);
                        if (rmo == null)
                        {
                            throw new Exception();
                        }
                    }
                    catch (Exception e)
                    {
                        Object[] args = {activity.getOldOption(), attributeOM.getName()};
                        String error = Localization.format(
                            ScarabConstants.DEFAULT_BUNDLE_NAME,
                            getLocale(),
                            "CouldNotFindModuleAttributeOption", args);
                        importErrors.add(error);
                    }
                }
            }
        }
    }

    private @OM@.Issue createNewIssue(Module module, Issue issue)
        throws Exception
    {
        // get the instance of the module
        @OM@.Module moduleOM = @OM@.ModuleManager.getInstance(module.getDomain(), module.getName(), module.getCode());
        // get the instance of the issue type
        @OM@.IssueType issueTypeOM = @OM@.IssueType.getInstance(issue.getArtifactType());
        issueTypeOM.setName(issue.getArtifactType());
        // get me a new issue since we couldn't find one before
        @OM@.Issue issueOM = @OM@.Issue.getNewInstance(moduleOM, issueTypeOM);
        // create the issue in the database
        issueOM.save();

        // Add the mapping between the issue id and the id that was created.
        // This mapping is used dependency checking and printing out in 
        // results list of original id and new id. The original issue id can be
        // null. In this case, have the original id show as 'null (index)'
        // where index is count into the issueXMLMap. We add the index to keep
        // the key unique. This substitute original id also shouldn't interfere
        // w/ issueXMLMap's use dependency checking.
        String issueID = "Null (" + Integer.toString(issueXMLMap.size()) + ")";
        if(issue.getId() != null)
        {
            issueID = module.getCode() + issue.getId();
        }
        issueXMLMap.put(issueID, issueOM.getUniqueId());

        LOG.debug("Created new Issue: " + issueOM.getUniqueId());
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
            issueOM = @OM@.IssueManager.getIssueById(module.getCode() + issue.getId());
            if (issueOM == null)
            {
                issueOM = createNewIssue(module, issue);
            }
            else
            {
                LOG.debug("Found Issue in db: " + issueOM.getUniqueId());
            }
        }

/////////////////////////////////////////////////////////////////////////////////  

        // Loop over the XML activitySets
        List activitySets = issue.getActivitySets();
        LOG.debug("-----------------------------------");
        LOG.debug("Number of ActivitySets in Issue: " + activitySets.size());
        for (Iterator itr = activitySets.iterator(); itr.hasNext();)
        {
            ActivitySet activitySet = (ActivitySet) itr.next();
            LOG.debug("Processing ActivitySet: " + activitySet.getId());

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
                        LOG.debug("Found existing ActivitySet Attachment");
                    }
                    catch (Exception e)
                    {
                        activitySetAttachmentOM = createAttachment(issueOM, module, activitySetAttachment);
                    }
                }
                else
                {
                    activitySetAttachmentOM = createAttachment(issueOM, module, activitySetAttachment);
                    LOG.debug("Created ActivitySet Attachment object");
                }
            }
            else
            {
                LOG.debug("OK- No Attachment in this ActivitySet");
            }

/////////////////////////////////////////////////////////////////////////////////  
            // Attempt to get the activitySet OM
            boolean alreadyCreated = false;
            @OM@.ActivitySet activitySetOM = null;
            if (getImportTypeCode() == UPDATE_SAME_DB)
            {
                try
                {
                    activitySetOM = @OM@.ActivitySetManager.getInstance(activitySet.getId());
                    LOG.debug("Found ActivitySet: " + activitySet.getId() + 
                              " in db: " + activitySetOM.getActivitySetId());
                }
                catch (Exception e)
                {
                    activitySetOM = @OM@.ActivitySetManager.getInstance();
                }
            }
            else
            {
                try
                {
                    // first try to get the ActivitySet from the internal map
                    if (activitySetIdMap.containsKey(activitySet.getId()))
                    {
                        activitySetOM = (@OM@.ActivitySet) activitySetIdMap.get(activitySet.getId());
                        alreadyCreated = true;
                        LOG.debug("Found ActivitySet: " + activitySet.getId() + 
                                  " in map: " + activitySetOM.getActivitySetId());
                    }
                    else // if it doesn't exist, then try to get it from the DB
                    {
                        activitySetOM = @OM@.ActivitySetManager.getInstance(activitySet.getId());
                        alreadyCreated = true;
                        LOG.debug("Found ActivitySet: " + activitySet.getId() + 
                                  " in db: " + activitySetOM.getActivitySetId());
                    }
                }
                catch (Exception e)
                {
                    // if all else fails, then get a new object
                    activitySetOM = @OM@.ActivitySetManager.getInstance();
                    LOG.debug("Created new ActivitySet");
                }
            }

            @OM@.ScarabUser activitySetCreatedByOM = @OM@.ScarabUserManager.getInstance(activitySet.getCreatedBy(), 
                 module.getDomain());
            if (!alreadyCreated)
            {
                // Populate the ActivitySet
                // Get the ActivitySet type/createdby values (we know these are valid)
                @OM@.ActivitySetType ttOM = @OM@.ActivitySetTypeManager.getInstance(activitySet.getType());
                activitySetOM.setActivitySetType(ttOM);
                activitySetOM.setCreatedBy(activitySetCreatedByOM.getUserId());
                activitySetOM.setCreatedDate(activitySet.getCreatedDate().getDate());
                if (activitySetAttachmentOM != null)
                {
                    activitySetAttachmentOM.save();
                    activitySetOM.setAttachment(activitySetAttachmentOM);
                }
                activitySetOM.save();
                activitySetIdMap.put(activitySet.getId(), activitySetOM);
            }

/////////////////////////////////////////////////////////////////////////////////  
// Deal with changing user attributes. this code needs to be in this *strange*
// location because we look at the entire activityset in order to determine
// that this is a change user activity set. of course in the future, it would
// be really nice to create an activityset/activiy type that more accurately 
// reflects what type of change this is. so that it is easier to case for. for
// now, we just look at some fingerprints to determine this information. -JSS

            if (activitySet.isChangeUserAttribute())
            {
                List activities = activitySet.getActivities();
                Activity activityA = (Activity)activities.get(0);
                Activity activityB = (Activity)activities.get(1);
                
                @OM@.ScarabUser assigneeOM = @OM@.ScarabUserManager
                    .getInstance(activityA.getOldUser(), module.getDomain());
                @OM@.ScarabUser assignerOM = @OM@.ScarabUserManager
                    .getInstance(activityB.getNewUser(), module.getDomain());

                @OM@.Attribute oldAttributeOM = @OM@.Attribute.getInstance(activityA.getAttribute());

                @OM@.AttributeValue oldAttValOM = issueOM.getUserAttributeValue(assigneeOM, oldAttributeOM);
                if (oldAttValOM == null)
                {
                    LOG.error("User '" + assigneeOM.getName() + "' was not previously '" + oldAttributeOM.getName() + "' to the issue!");
                }

                // Get the Attribute associated with the new Activity
                @OM@.Attribute newAttributeOM = @OM@.Attribute.getInstance(activityB.getAttribute());

                issueOM.changeUserAttributeValue(activitySetOM,
                            assigneeOM, 
                            assignerOM, 
                            oldAttValOM,
                            newAttributeOM, null);
                LOG.debug("-------------Updated User AttributeValue------------");
                continue;
            }

/////////////////////////////////////////////////////////////////////////////////  

            // Deal with the activities in the activitySet
            List activities = activitySet.getActivities();
            LOG.debug("Number of Activities in ActivitySet: " + activities.size());

            SequencedHashMap avMap = issueOM.getModuleAttributeValuesMap();
            LOG.debug("Total Module Attribute Values: " + avMap.size());
            for (Iterator itrb = activities.iterator(); itrb.hasNext();)
            {
                Activity activity = (Activity) itrb.next();

                // Get the Attribute associated with the Activity
                @OM@.Attribute attributeOM = @OM@.Attribute.getInstance(activity.getAttribute());

                @OM@.Activity activityOM = null;

                // deal with the activity attachment (if there is one)
                Attachment activityAttachment = activity.getAttachment();
                @OM@.Attachment activityAttachmentOM = null;
                if (activityAttachment != null)
                {
                    // look for an existing attachment in the activity
                    // the case is when we have a URL and we create it
                    // and then delete it, the attachment id is still the
                    // same so there is no reason to re-create the attachment
                    // again.
                    try
                    {
                        activityAttachmentOM = @OM@.AttachmentManager
                            .getInstance(activityAttachment.getId());
                        LOG.debug("Found existing Activity Attachment");
                    }
                    catch (Exception e)
                    {
                        activityAttachmentOM = createAttachment(issueOM, module, activityAttachment);
                        activityAttachmentOM.save();
                        
                        // Special case. After the Attachment object has been saved,
                        // if the ReconcilePath == true, then assume that the fileName is 
                        // an absolute path to a file and copy it to the right directory
                        // structure under Scarab's path.
                        if (activityAttachment.getReconcilePath())
                        {
                            activityAttachmentOM
                                .copyFileFromTo(activityAttachment.getFilename(), 
                                                activityAttachmentOM.getFullPath());
                        }
                        LOG.debug("Created Activity Attachment object");
                    }
                }
                else
                {
                    LOG.debug("OK- No Attachment in this Activity");
                }

                // deal with null attributes (need to do this before we create the 
                // activity right below because this will create its own activity).
                if (attributeOM.equals(nullAttribute))
                {
                    // add any dependency activities to a list for later processing
                    if (isDependencyActivity(activity))
                    {
                        if (!isDuplicateDependency(activitySet))
                        {
                            Object[] obj = {activitySetOM, activity, activityAttachmentOM};
                            allDependencies.add(obj);
                            dependActivitySetId.add(activitySet.getId());
                            LOG.debug("-------------Stored Dependency # " + allDependencies.size() + "-------------");
                            continue;
                        }
                    }
                    else
                    {
                        // create the activity record.
                        activityOM = @OM@.ActivityManager
                            .createTextActivity(issueOM, nullAttribute, activitySetOM, 
                                    activity.getDescription(), activityAttachmentOM, 
                                    activity.getOldValue(), activity.getNewValue());
        
                        LOG.debug("-------------Saved Null Attribute-------------");
                        continue;
                    }
                }

                // create the activityOM
                activityOM = createActivity(activity, activitySet, module, 
                                            issueOM, attributeOM, activitySetOM);

                // check to see if this is a new activity or an update activity
                for (Iterator moduleAttributeValueItr = avMap.iterator();moduleAttributeValueItr.hasNext();)
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

                    LOG.debug("Checking Attribute match: " + avalAttributeOM.getName() + 
                              " against: " + attributeOM.getName());
                    if (avalAttributeOM.equals(attributeOM))
                    {
                        LOG.debug("Attributes match!");

                        if (avalAttributeOM.isOptionAttribute())
                        {
                            LOG.debug("We have an Option Attribute: " + avalAttributeOM.getName());
                            @OM@.AttributeOption newAttributeOptionOM = @OM@.AttributeOption
                                .getInstance(attributeOM, activity.getNewOption());
                            if (activity.isNewActivity())
                            {
                                if(newAttributeOptionOM != null)
                                {
                                    avalOM.setOptionId(newAttributeOptionOM.getOptionId());
                                }
                                else
                                {
                                    LOG.debug("NewAttributeOptionOM is null.");
                                }
                            }
                            else
                            {
                                HashMap map = new HashMap();
                                map.put(avalOM.getAttributeId(), avalOM2);
                                issueOM.setAttributeValues(activitySetOM, map, null, activitySetCreatedByOM);
                                LOG.debug("-------------Saved Option Attribute Change-------------");
                                break;
                            }
                        }
                        else if (avalAttributeOM.isUserAttribute())
                        {
                            LOG.debug("We have a User Attribute: " 
                                + avalAttributeOM.getName());
                            if (activity.isNewActivity())
                            {
                                // Don't need to pass in the attachment because
                                // it is already in the activitySetOM.
                                // If we can't get an assignee new-user, then 
                                // use the activity set creator as assignee.
                                @OM@.ScarabUser assigneeOM 
                                    = @OM@.ScarabUserManager
                                    .getInstance(activity.getNewUser(), 
                                    module.getDomain());
                                assigneeOM = (assigneeOM != null)
                                    ? assigneeOM: activitySetCreatedByOM;
                                issueOM.assignUser(activitySetOM, 
                                    activity.getDescription(), 
                                    assigneeOM, null, avalAttributeOM, null);
                                LOG.debug("-------------Saved User Assign-------------");
                                break;
                            }
                            else
                            {
                                // remove a user activity
                                if (activity.isRemoveUserActivity())
                                {
                                    @OM@.ScarabUser oldUserOM = @OM@.ScarabUserManager
                                        .getInstance(activity.getOldUser(), module.getDomain());
                                    // need to reset the aval because the current one is
                                    // marked as new for some reason which causes an insert
                                    // and that isn't the right behavior here (we want an update)
                                    avalOM = issueOM.getAttributeValue(avalAttributeOM);
                                    // don't need to pass in the attachment because it is already
                                    // in the activitySetOM
                                    issueOM.deleteUser(activitySetOM, oldUserOM, activitySetCreatedByOM, 
                                                       avalOM, null);
                                    LOG.debug("-------------Saved User Remove-------------");
                                    break;
                                }
                            }
                        }
                        else if (avalAttributeOM.isTextAttribute())
                        {
                            LOG.debug("We have a Text Attribute: " + avalAttributeOM.getName());
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
                        LOG.debug("-------------Saved Attribute Value-------------");
                        break;
                    }
                }
                issueOM.save();
                LOG.debug("-------------Saved Issue-------------");
            }
        }
    }

    /**
     * Checks to see if there is a Dependency value for the Activity
     */
    private boolean isDependencyActivity(Activity activity)
    {
        return (activity.getDependency() != null);
    }

    private boolean isDuplicateDependency(ActivitySet activitySet)
    {
        return (dependActivitySetId.contains(activitySet.getId()));
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
        if (activity.getEndDate() != null)
        {
            activityOM.setEndDate(activity.getEndDate().getDate());
        }

        // Set the attachment for the activity
        @OM@.Attachment newAttachmentOM = null;
        if (activity.getAttachment() != null)
        {
            newAttachmentOM = createAttachment(issueOM, module, activity.getAttachment());
            newAttachmentOM.save();
            activityOM.setAttachment(newAttachmentOM);
        }

        LOG.debug("Created New Activity");
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
        attachmentOM.setFileName(attachment.getFilename());
        attachmentOM.setData(attachment.getData());
        attachmentOM.setCreatedDate(attachment.getCreatedDate().getDate());
        ModifiedDate modifiedDate = attachment.getModifiedDate();
        if (modifiedDate != null)
        {
            attachmentOM.setModifiedDate(modifiedDate.getDate());
        }
        @OM@.ScarabUser creUser = @OM@.ScarabUserManager
            .getInstance(attachment.getCreatedBy(), issueOM.getModule().getDomain());
        if (creUser != null)
        {
            attachmentOM.setCreatedBy(creUser.getUserId());
        }

        @OM@.ScarabUser modUserOM = null;
        String modifiedBy = attachment.getModifiedBy();
        if (modifiedBy != null)
        {
            modUserOM = @OM@.ScarabUserManager
                .getInstance(attachment.getModifiedBy(), 
                    issueOM.getModule().getDomain());
            if (modUserOM != null)
            {
                attachmentOM.setModifiedBy(modUserOM.getUserId());
            }
        }

        attachmentOM.setDeleted(attachment.getDeleted());
        return attachmentOM;
    }

    private Locale getLocale()
    {
        return ScarabConstants.DEFAULT_LOCALE;
    }
}
