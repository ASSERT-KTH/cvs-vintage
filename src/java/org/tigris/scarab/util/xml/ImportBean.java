package org.tigris.scarab.util.xml;

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

import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.DependType;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ActivitySet;
import org.tigris.scarab.om.ActivitySetType;
import org.apache.commons.digester.Digester;

/**
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ImportBean.java,v 1.7 2002/07/30 22:48:15 jmcnally Exp $
 */
public class ImportBean
{
    private String state = null;
    private Digester digester = null;
    private DependencyTree dependencyTree = null;
    private DependencyNode dependencyNode = null;
    private List roleList = null;
    private List userList = null;
    private Module module = null;
    private ScarabUser user = null;
    private ActivityInfo activityInfo = null;
    private Issue issue = null;
    private String issueId = null;
    private ActivitySet activitySet = null;
    private ActivitySetType activitySetType = null;
    private Attachment attachment = null;
    private String attachmentDateFormat = null;


    public String getState()
    {
        return this.state;
    }
    
    public void setState(String value)
    {
        this.state = value;
    }
    
    public Digester getDigester()
    {
        return this.digester;
    }
    
    public void setDigester(Digester value)
    {
        this.digester = value;
    }

    public DependencyTree getDependencyTree()
    {
        return this.dependencyTree;
    }
    
    public void setDependencyTree(DependencyTree value)
    {
        this.dependencyTree = value;
    }

    public DependencyNode getDependencyNode()
    {
        return this.dependencyNode;
    }
    
    public void setDependencyNode(DependencyNode value)
    {
        this.dependencyNode = value;
    }

    public ActivityInfo getActivityInfo()
    {
        return this.activityInfo;
    }
    
    public void setActivityInfo(ActivityInfo value)
    {
        this.activityInfo = value;
    }
    
    public Attachment getAttachment()
    {
        return this.attachment;
    }
    
    public void setAttachment(Attachment value)
    {
        this.attachment = value;
    }
    
    public String getAttachmentDateFormat()
    {
        return this.attachmentDateFormat;
    }
    
    public void setAttachmentDateFormat(String value)
    {
        this.attachmentDateFormat = value;
    }

    public List getRoleList()
    {
        if (this.roleList == null)
        {
            this.roleList = new ArrayList();
        }
        return this.roleList;
    }
    
    public void setRoleList(List value)
    {
        this.roleList = value;
    }

    public List getUserList()
    {
        if (this.userList == null)
        {
            this.userList = new ArrayList();
        }
        return this.userList;
    }
    
    public void setUserList(List value)
    {
        this.userList = value;
    }

    public ActivitySet getActivitySet()
    {
        return this.activitySet;
    }
    
    public void setActivitySet(ActivitySet value)
    {
        this.activitySet = value;
    }

    public ActivitySetType getActivitySetType()
    {
        return this.activitySetType;
    }
    
    public void setActivitySetType(ActivitySetType value)
    {
        this.activitySetType = value;
    }

    public Module getModule()
    {
        return this.module;
    }
    
    public void setModule(Module value)
    {
        this.module = value;
    }

    public Issue getIssue()
    {
        return this.issue;
    }
    
    public void setIssue(Issue value)
    {
        this.issue = value;
    }

    public String getIssueId()
    {
        return this.issueId;
    }
    
    public void setIssueId(String value)
    {
        this.issueId = value;
    }

    public ScarabUser getScarabUser()
    {
        return this.user;
    }
    
    public void setScarabUser(ScarabUser value)
    {
        this.user = value;
    }

/*
    public String getIdentifier()
    {
        return this.identifier;
    }
    
    public void setIdentifier(String value)
    {
        this.identifier = value;
    }
*/
}
