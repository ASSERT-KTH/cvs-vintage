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


// JDK classes
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import com.workingdogs.village.Record;

// Turbine classes
import org.apache.fulcrum.intake.Retrievable;
import org.apache.torque.om.Persistent;
import org.apache.torque.om.NumberKey;
import org.apache.torque.util.Criteria;
import org.apache.torque.TorqueException;

import org.apache.fulcrum.util.parser.StringValueParser;
import org.apache.fulcrum.util.parser.ValueParser;
import org.apache.fulcrum.intake.Intake;
import org.apache.fulcrum.intake.model.Group;
import org.apache.fulcrum.intake.model.Field;

import org.tigris.scarab.util.ScarabException;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.RModuleAttribute;
import org.tigris.scarab.om.RModuleOption;
import org.tigris.scarab.om.AttributeOption;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ActivityPeer;
import org.tigris.scarab.om.ActivitySetPeer;
import org.tigris.scarab.om.ActivitySetTypePeer;
import org.tigris.scarab.util.OptionModel;
import org.tigris.scarab.util.TableModel;
import org.tigris.scarab.services.security.ScarabSecurity;

/** 
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class Report 
    extends org.tigris.scarab.om.BaseReport
    implements Persistent
{
    public void save(Connection dbCon) throws TorqueException
    {
       try
       {
           if (isNew())
           {
               super.save(dbCon);
               setModified(true);
               super.save(dbCon);
            }
            else
            {
               super.save(dbCon);
            }
        }
        catch (Exception e)
        {
            log().error(e);
         }
    }

    public NumberKey getScopeId()
    {
        NumberKey id = super.getScopeId();
        if (id == null) 
        {
            id = Scope.PERSONAL__PK;
        }
        return id;
    }

    
    /**
     * Get the value of module.
     * @return value of module.
     */
    public Module getModule() 
        throws TorqueException
    {
        return ModuleManager.getInstance(getModuleId());
    }
    
    /**
     * Set the value of module.
     * @param v  Value to assign to module.
     */
    public void setModule(Module  v) 
        throws TorqueException
    {
        setModuleId(v.getModuleId());
    }

    /*    
    public void setWrapperReport(org.tigris.scarab.reports.Report wrapper)
    {
        this.wrapperReport = wrapper;
    }
    */
}
