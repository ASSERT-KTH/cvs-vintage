package org.tigris.scarab.om;

/* ================================================================
 * Copyright (c) 2000 Collab.Net.  All rights reserved.
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

// JDK
import java.util.Date;

// Turbine
import org.apache.turbine.RunData;
import org.apache.turbine.TemplateContext;
import org.apache.turbine.services.security.TurbineSecurity;
import org.apache.turbine.services.db.om.BaseObject;
import org.apache.turbine.services.db.om.NumberKey;

import org.tigris.scarab.tools.ScarabRequestTool;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.services.module.ModuleEntity;
import org.tigris.scarab.services.module.ModuleManager;

/**
    This BaseScarabObject contains methods and variables that are common
    across all of the Scarab db objects.
    
    @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
    @version $Id: BaseScarabObject.java,v 1.8 2001/07/11 07:33:47 jon Exp $    
*/
public class BaseScarabObject extends BaseObject
{
    /** created_by_id */
    private int created_by_id = -1;
    /** created_date */
    private Date created_date = null;
    /** modified_by_id */
    private int modified_by_id = -1;
    /** modified_date */
    private Date modified_date = null;
    
    /**
        get the CreatedBy
    */
    public int getCreatedBy()
    {
        return created_by_id;
    }
    /**
        set the CreatedBy
    */
    public void setCreatedBy(int name)
    {
        created_by_id = name;
    }
    /**
        get the CreatedDate
    */
    public Date getCreatedDate()
    {
        return created_date;
    }
    /**
        set the CreatedDate
    */
    public void setCreatedDate(Date name)
    {
        created_date = name;
    }
    /**
        get the ModifiedBy
    */
    public int getModifiedBy()
    {
        return created_by_id;
    }
    /**
        set the ModifiedBy
    */
    public void setModifiedBy(int name)
    {
        created_by_id = name;
    }
    /**
        get the ModifiedDate
    */
    public Date getModifiedDate()
    {
        return modified_date;
    }
    /**
        set the ModifiedDate
    */
    public void setModifiedDate(Date name)
    {
        modified_date = name;
    }

    /**
     * This method gives us a user with a current module.
     * It should be removed as soon as we have some way to set 
     * this within the application
     */
    public static void tempWorkAround( RunData data, 
                                TemplateContext context ) 
        throws Exception
    {

        ScarabRequestTool scarab = (ScarabRequestTool)
            context.get(ScarabConstants.SCARAB_REQUEST_TOOL);
          
        if ( data.getUser() == null ) 
        {
            ScarabUser user = (ScarabUser) TurbineSecurity.getAnonymousUser();
            // bad bad bad...
            ((ScarabUserImpl)user).setPrimaryKey(new NumberKey("2"));
            user.setUserName("workarounduser");
            scarab.setUser(user);
            data.setUser(user);
        }
          
        if ( scarab.getUser().getCurrentModule() == null ) 
        {
            ModuleEntity module = 
                ModuleManager.getInstance(
                    new NumberKey("5"));
            scarab.getUser().setCurrentModule(module);
        }
    }
}
