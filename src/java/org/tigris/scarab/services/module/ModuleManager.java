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
import java.util.Vector;

import org.tigris.scarab.om.ScarabModulePeer;

import org.apache.torque.om.ObjectKey;
import org.apache.fulcrum.security.entity.User;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.security.TurbineSecurity;
import org.apache.commons.util.StringUtils;
import org.apache.turbine.RunData;
import org.apache.torque.util.Criteria;

/**
 * This class has static methods for working with a Module object
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ModuleManager.java,v 1.12 2001/08/30 19:26:35 jmcnally Exp $
 */
public abstract class ModuleManager
{
    /**
     * Retrieves an implementation of ModuleService, base on the settings in
     * TurbineResources.
     *
     * @return an implementation of ModuleService.
     */
    public static ModuleService getService()
    {
        return (ModuleService)TurbineServices.getInstance().
            getService(ModuleService.SERVICE_NAME);    
    }

    public static ModuleEntity getInstance()
        throws Exception
    {
        return getService().getInstance();
    }

    public static ModuleEntity getInstance(ObjectKey id)
        throws Exception
    {
        return getService().getInstance(id);
    }

    public static Class getModuleClass()
    {
        return getService().getModuleClass();
    }

    /**
        gets a single project
        @return null on error
    */
    public static ModuleEntity getProject(ObjectKey project_id) 
        throws Exception
    {
        ModuleEntity project = null;
        try
        {
            Criteria criteria = new Criteria();
            criteria.add(ScarabModulePeer.MODULE_ID, project_id);
            // get the Project object
            Vector projectVec = ScarabModulePeer.doSelect(criteria);
            if (projectVec.size() == 1)
                project = (ModuleEntity)projectVec.elementAt(0);
        }
        catch (Exception e)
        {
        }
        return project;    
    }

    /**
        give me a list of components that match the parent project id
    */
    public static Vector getComponents(ObjectKey parent_project_id)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add (ScarabModulePeer.PARENT_ID, parent_project_id);
        return ScarabModulePeer.doSelect(crit);
    }
    
    /**
        create a new ScarabModule based on form input.
        
        it will optionally try to validate the data. if there is an error, it will
        throw an exception.
    public static ModuleEntity getModule(RunData data, boolean validate)
        throws Exception
    {
        String project_id = data.getParameters().getString("project_id", null);
        String name = data.getParameters().getString("project_name",null);
        String desc = data.getParameters().getString("project_description",null);

        ModuleEntity sm = (ModuleEntity) getInstance();
        sm.setPrimaryKey(project_id);
//        sm.setName( StringUtils.makeString( name ));
//        sm.setDescription( StringUtils.makeString( desc ));
//        sm.setUrl( StringUtils.makeString(data.getParameters().getString("project_url") ));
        if (validate)
        {
            if (project_id == null)
                throw new Exception ( "Missing project_id!" );
            if (! StringUtils.isValid(name))
                throw new Exception ( "Missing project name!" );
            if (! StringUtils.isValid(desc))
                throw new Exception ( "Missing project description!" );

            User project_owner = TurbineSecurity.getUser(
                data.getParameters().getString("project_owner", ""));
            User project_qacontact = TurbineSecurity.getUser(
                data.getParameters().getString("project_qacontact", ""));
            
            if (project_owner == null)
                throw new Exception ("Could not find a registered user for the project owner!");
            if (project_qacontact == null )
                throw new Exception ("Could not find a registered user for the project qa contact!");

//            sm.setOwnerId((NumberKey)((ScarabUser)project_owner).getPrimaryKey() );
//            sm.setQaContactId((NumberKey)((ScarabUser)
//                                project_qacontact).getPrimaryKey() );
        }
        return sm;
    }
    */
    
    /**
        check for a duplicate project name
    */
    public static void checkForDuplicateProject(ModuleEntity module)
        throws Exception
    {
        Criteria crit = new Criteria();
        crit.add (ScarabModulePeer.MODULE_NAME, module.getName());
        crit.setSingleRecord(true);
        Vector result = ScarabModulePeer.doSelect(crit);
        if (result.size() > 0)
            throw new Exception ("Project: " + module.getName() + 
            " already exists. Please choose another name!" );        
    }
}
