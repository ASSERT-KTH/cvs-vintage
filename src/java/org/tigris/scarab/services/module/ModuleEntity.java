package org.tigris.scarab.services.module;

/* ================================================================
 * Copyright (c) 2001 Collab.Net.  All rights reserved.
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
import java.util.Vector;

import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.ScarabUser;

import org.apache.turbine.services.db.om.ObjectKey;
import org.apache.turbine.services.db.om.NumberKey;
import org.apache.turbine.services.security.entity.Role;
import org.apache.turbine.services.security.entity.User;
import org.apache.turbine.RunData;
import org.apache.turbine.services.db.util.Criteria;

/**
 * This class describes a Module
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ModuleEntity.java,v 1.10 2001/07/19 21:30:06 jon Exp $
 */
public interface ModuleEntity
{
    public String getName();
    public void setName(String name);

    public String getDescription();
    public void setDescription(String description);

    public String getUrl();
    public void setUrl(String url);

    public void setPrimaryKey(String key) throws Exception;
    public void setPrimaryKey(ObjectKey key) throws Exception;
    public NumberKey getModuleId();
    
/** THESE WILL BE DEPRECATED */
    public NumberKey getQaContactId();
/** THESE WILL BE DEPRECATED */
    public void setQaContactId(String v ) throws Exception;
/** THESE WILL BE DEPRECATED */
    public void setQaContactId(NumberKey v ) throws Exception;

/** THESE WILL BE DEPRECATED */
    public NumberKey getOwnerId();
/** THESE WILL BE DEPRECATED */
    public void setOwnerId(String v ) throws Exception;
/** THESE WILL BE DEPRECATED */
    public void setOwnerId(NumberKey v ) throws Exception;

    public void save() throws Exception;

    public void setModuleRelatedByParentId(ModuleEntity module) 
        throws Exception;

    public ModuleEntity getModuleRelatedByParentIdCast() throws Exception;

    public List getRModuleAttributes(boolean activeOnly)
        throws Exception;

    public Vector getRModuleAttributes(Criteria criteria)
        throws Exception;

    public List 
        getRModuleOptions(Attribute attribute, boolean activeOnly)
        throws Exception;

    public String getQueryKey();

    public boolean getDeleted();
    public void setDeleted(boolean b);

    public NumberKey getParentId();
    public void setParentId(String v ) throws Exception;
    public void setParentId(NumberKey v ) throws Exception;
    
    public Issue getNewIssue(ScarabUser user)
        throws Exception;

    public List getUsers(String partialUserName, String permission)
        throws Exception;

    public Attribute[] getActiveAttributes()
        throws Exception;
/*    
    
    public String getAbbreviation();
    public void setAbbreviation();
    
    public void setParentModuleId(NumberKey key);

    public Issue getNewIssue(User user)
        throws Exception;

    public Attribute[] getAttributes(Criteria criteria)
        throws Exception;

    public Attribute[] getDedupeAttributes()
        throws Exception;

    public Attribute[] getQuickSearchAttributes()
        throws Exception;

    public Attribute[] getAllAttributes()
        throws Exception;

    public List getRModuleOptions(Attribute attribute)
        throws Exception;

    public List getRModuleOptions(Attribute attribute, boolean activeOnly)
        throws Exception;

    public List getLeafRModuleOptions(Attribute attribute)
        throws Exception;

    public List getLeafRModuleOptions(Attribute attribute, boolean activeOnly)
        throws Exception;

    public List getOptionTree(Attribute attribute)
        throws Exception;

    public List getOptionTree(Attribute attribute, boolean activeOnly)
        throws Exception;

    public List getUsers(String partialUserName, 
                         Role[] includeRoles, Role[] excludeRoles) 
        throws Exception;

    public void save() throws Exception;

    public ModuleEntity doPopulate(RunData data)
        throws Exception;

    public ModuleEntity doPopulate(RunData data, boolean validate)
        throws Exception;
*/
}
