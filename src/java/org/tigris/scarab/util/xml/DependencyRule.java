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

import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.DependType;
import org.tigris.scarab.om.Depend;

/**
 * Handler for the xpath "scarab/module/issue/dependency"
 *
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @author <a href="mailto:richard.han@bitonic.com">Richard Han</a>
 */
public class DependencyRule extends BaseRule
{
    public DependencyRule(ImportBean ib)
    {
        super(ib);
    }
    
    /**
     * This method is called when the end of a matching XML element
     * is encountered.
     */
    public void end() throws Exception
    {
        super.doInsertionOrValidationAtEnd();
        log().debug("(" + getImportBean().getState() + 
            ") dependency end");
    }
    
    protected void doValidationAtEnd()
    {
        String nodeType = (String)getDigester().pop();
        String parentOrChildIssueXmlId = (String)getDigester().pop();
        DependType dependType = (DependType)getDigester().pop();
        String issueXmlId = (String)getDigester().pop();

        DependencyTree dt = getImportBean().getDependencyTree();
        DependencyNode dn = new DependencyNode(nodeType, issueXmlId, 
                                               parentOrChildIssueXmlId, 
                                               dependType, false);

        if(!dt.isIssueDependencyValid(dn))
        {
            //if we can't resolve the dependency yet, it may still 
            // resolve it at the end when we have seen all ids
            dt.addIssueDependency(issueXmlId, dn);
        }
        else
        {
            dn = new DependencyNode(nodeType, issueXmlId, 
                                    parentOrChildIssueXmlId, 
                                    dependType, true);
            dt.addIssueDependency(issueXmlId, dn);
        }

        getDigester().push(issueXmlId);
    }
    
    protected void doInsertionAtEnd()
        throws Exception
    {
        String nodeType = (String)getDigester().pop();
        String parentOrChildIssueXmlId = (String)getDigester().pop();
        DependType dependType = (DependType)getDigester().pop();
        Issue issue = (Issue)getDigester().pop();
        
        DependencyTree dt = getImportBean().getDependencyTree();

        String issueXmlId = dt.getIssueXmlId(issue.getIssueId());
        Depend depend = Depend.getInstance();
        depend.setDependType(dependType);

        if (nodeType.equals(DependencyNode.NODE_TYPE_PARENT)) 
        {
            if (dt.isIssueResolvedYet(parentOrChildIssueXmlId)) 
            {
                log().debug("parent dependency of " + parentOrChildIssueXmlId + 
                                " has been resolved");
                depend.setObserverId(issue.getIssueId());
                depend.setObservedId(dt.getIssueId(parentOrChildIssueXmlId));
                depend.save();
            }
            else
            {
                log().debug("can't resolve the parent dependency of " + 
                                parentOrChildIssueXmlId);
                //resolve at the end 
                DependencyNode dn = new DependencyNode(nodeType, issueXmlId, 
                                                       parentOrChildIssueXmlId, 
                                                       dependType, false);
                
                dt.addIssueDependency(issueXmlId, dn);
            }
            
        } 
        getDigester().push(issue);
    }
}
