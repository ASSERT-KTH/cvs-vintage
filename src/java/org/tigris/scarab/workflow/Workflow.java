package org.tigris.scarab.workflow;

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

import java.util.Map;
import java.util.List;


import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.OptionWorkflow;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.WorkflowRules;
import org.tigris.scarab.util.ScarabException;

/**
 * This is the primary interface for workflow within Scarab.
 * Implementations of this interface are loaded through the
 * WorkflowFactory interface. It gets which class to load
 * from the Scarab.properties file.
 *   
 * @author <a href="mailto:elicia@tigris.org">Elicia David</a>
 * @version $Id: Workflow.java,v 1.12 2003/03/15 21:56:59 jon Exp $
 */
public interface Workflow
{
    boolean canMakeTransition(ScarabUser user,
                                     AttributeOption fromOption, 
                                     AttributeOption toOption,
                                     Issue issue)
        throws ScarabException;


    String checkTransition(AttributeOption fromOption, 
                                  AttributeOption toOption,
                                  Issue issue, Map newAttVals,
                                  ScarabUser user)
        throws ScarabException;
        
    String checkInitialTransition(AttributeOption toOption,
                                         Issue issue, Map newAttVals,
                                         ScarabUser user)
        throws ScarabException;

    OptionWorkflow getWorkflowForRole(AttributeOption fromOption, 
                                             AttributeOption toOption,
                                             String roleName,
                                             Module module,
                                             IssueType issueType)
        throws ScarabException;


    List getWorkflowsForRoleList(AttributeOption fromOption, 
                                        AttributeOption toOption,
                                        List roleNames, Module module,
                                        IssueType issueType)
        throws ScarabException;

    List getWorkflowsForIssueType(IssueType issueType)
        throws ScarabException;

    void saveWorkflow(AttributeOption fromOption, 
                             AttributeOption toOption,
                             String roleName, Module module,
                             IssueType issueType, WorkflowRules workflowRule)
        throws ScarabException;

    OptionWorkflow inherit(AttributeOption fromOption, 
                                   AttributeOption toOption,
                                   String roleName, Module module,
                                   IssueType issueType)
        throws ScarabException;

    void resetWorkflow(AttributeOption fromOption, 
                              AttributeOption toOption,
                              String roleName, Module module,
                              IssueType issueType)
        throws ScarabException;

    void resetWorkflows(String roleName, Module module, IssueType issueType,
                               boolean initial)
        throws ScarabException;


    void deleteWorkflowsForOption(AttributeOption option,
                                         Module module, IssueType issueType)
        throws ScarabException;

    void deleteWorkflowsForAttribute(Attribute attr, Module module, 
                                            IssueType issueType)
        throws ScarabException;

    void addIssueTypeWorkflowToModule(Module module, 
                                            IssueType issueType)
        throws ScarabException;

    void resetAllWorkflowsForIssueType(Module module, 
                                              IssueType issueType)
        throws ScarabException;

}
