package org.tigris.scarab.om;

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
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.om.Persistent;

import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.util.ScarabException;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 *
 * @author <a href="mailto:elicia@collab.net">Elicia David</a>
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: IssueType.java,v 1.13 2002/02/13 20:06:06 elicia Exp $
 */
public  class IssueType 
    extends org.tigris.scarab.om.BaseIssueType
    implements Persistent
{

    public static final NumberKey ISSUE__PK = new NumberKey("1");
    public static final NumberKey USER_TEMPLATE__PK = new NumberKey("2");
    public static final NumberKey GLOBAL_TEMPLATE__PK = new NumberKey("3");

    /**
     * Gets the id of the template that corresponds to the issue type.
     */
    public String test(ModuleEntity module)
        throws Exception
    {
       return module.getName();
    }

    /**
     * Gets the id of the template that corresponds to the issue type.
     */
    public String test(ModuleEntity module, boolean b)
        throws Exception
    {
        if (b)
        {
            return "true";
        }
        else
        {
            return "false";
         }
    }

    /**
     * Gets the id of the template that corresponds to the issue type.
     */
    public NumberKey getTemplateId()
        throws Exception
    {
        NumberKey templateId = null;
        Criteria crit = new Criteria();
        crit.add(IssueTypePeer.PARENT_ID, getIssueTypeId());
        List results = (List)IssueTypePeer.doSelect(crit);
        if (results.isEmpty() || results.size()>1 )
        {
            throw new ScarabException("There has been an error.");
        }
        else
        {
            templateId = ((IssueType)results.get(0)).getIssueTypeId();
        }
        return templateId;
    }        
    
    /**
     * Get the IssueType using a issue type name
     */
    public static IssueType getInstance(String issueTypeName)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add(IssueTypePeer.NAME, issueTypeName);
        List issueTypes = (List)IssueTypePeer.doSelect(crit);
        if(issueTypes == null || issueTypes.size() == 0 )
        {
            throw new ScarabException("Invalid issue artifact type: " +
                                      issueTypeName);
        }
        return (IssueType)issueTypes.get(0);
    }
}
