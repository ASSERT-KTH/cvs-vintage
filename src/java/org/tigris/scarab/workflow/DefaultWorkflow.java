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

import java.util.HashMap;

import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;

import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.OptionWorkflow;
import org.tigris.scarab.om.OptionWorkflowPeer;

/**
    This class adds a ModuleManager.CURRENT_PROJECT to every link. This class is added
    into the context to replace the $link that Turbine adds.
    
    @author <a href="mailto:elicia@tigris.org">Elicia David</a>
    @version $Id: DefaultWorkflow.java,v 1.1 2002/08/08 01:38:47 elicia Exp $
*/
public class DefaultWorkflow implements Workflow
{
    public boolean canMakeTransition(ScarabUser user,
                                     AttributeOption fromOption, 
                                     AttributeOption toOption,
                                     Issue issue)
        throws Exception

    {
        return true;
    }

    public String checkTransition(AttributeOption fromOption, 
                                  AttributeOption toOption,
                                  Issue issue, HashMap newAttVals,
                                  ScarabUser user)
        throws Exception
    {
        return null;
    }

    public OptionWorkflow getOptionWorkflow(AttributeOption fromOption, 
                                    AttributeOption toOption,
                                    String roleName, Module module,
                                    IssueType issueType)
        throws Exception
    {
        return null;
    }
        
    public void deleteWorkflowsForOption(AttributeOption option,
                                                Module module, IssueType issueType)
        throws Exception
    {
       // nothing
    }
}

