package org.tigris.scarab.services;

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

import org.apache.turbine.Turbine;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.InitializationException;
import org.apache.torque.Torque;
import org.tigris.scarab.om.*;
import org.tigris.scarab.util.Log;

/**
 * Turbine does not yet have a way to initialize components directly, and
 * use of fulcrum's DatabaseService causes fulcrum to try to treat all the
 * Managers as services.  So this service is used to initialize Torque.
 * It also creates an instance of each scarab om object to avoid deadlock.
 *
 * @author <a href="mailto:jmcnally@collab.net">John McNally</a>
 * @version $Id: TorqueService.java,v 1.7 2004/05/10 21:04:46 dabbous Exp $
 */
public class TorqueService
    extends BaseService
{
    /**
     * Initializes the service by setting up Torque.
     */
    public void init()
        throws InitializationException
    {
        try
        {
            Torque.init(Turbine.getConfiguration());
            Log.get("org.apache.fulcrum").info("  Loading om instances via managers...");
            loadOM();
            Log.get("org.apache.fulcrum").info("  Done loading om instances.");
        }
        catch (Exception e)
        {
            throw new InitializationException("Can't initialize Torque!", e); //EXCEPTION
        }

        // indicate that the service initialized correctly
        setInit(true);
    }

    /**
     * This method loads all the classes in the org.tigris.scarab.om
     * package.  The torque classes of Foo and FooPeer contain a circular
     * relationship so loading the class of Foo requires a FooPeer instance
     * and creating a FooPeer instance requires the Foo class to be loaded.
     * It is possible to deadlock if multiple threads attempt to load Foo 
     * simultaneously.  A full analysis of possible deadlock scenarios has not
     * been done, so to be as safe as possible we create one instance of each
     * om object via the Manager.getInstance() method; this makes sure the
     * Managers are initialized as well.  It may be possible to
     * reduce this to only calling Class.forName on each class.
     */ 
    protected void loadOM()
        throws Exception
    {
        ActivityManager.getInstance();
        ActivitySetManager.getInstance();
        ActivitySetTypeManager.getInstance();
        AttachmentManager.getInstance();
        AttachmentTypeManager.getInstance();
        AttributeClassManager.getInstance();
        AttributeGroupManager.getInstance();
        AttributeManager.getInstance();
        AttributeOptionManager.getInstance();
        AttributeTypeManager.getInstance();
        // AttributeValue class is abstract
        AttributeValueManager.getManager();
        Class.forName("org.tigris.scarab.om.AttributeValue");
        DependManager.getInstance();
        DependTypeManager.getInstance();
        FrequencyManager.getInstance();
        GlobalParameterManager.getInstance();
        // Issue class does not have public no-arg ctor
        IssueManager.getManager();
        Class.forName("org.tigris.scarab.om.Issue");
        IssueTemplateInfoManager.getInstance();
        IssueTypeManager.getInstance();
        IssueVoteManager.getInstance();
        MITListItemManager.getInstance();
        MITListManager.getInstance();
        ModificationManager.getInstance();
        ModuleManager.getInstance();
        OptionRelationshipManager.getInstance();
        PendingGroupUserRoleManager.getInstance();
        QueryManager.getInstance();
        RAttributeAttributeGroupManager.getInstance();
        ReportManager.getInstance();
        RIssueTypeAttributeManager.getInstance();
        RIssueTypeOptionManager.getInstance();
        RModuleAttributeManager.getInstance();
        RModuleIssueTypeManager.getInstance();
        RModuleOptionManager.getInstance();
        RModuleUserAttributeManager.getInstance();
        // ROptionOption class does not have public no-arg ctor
        ROptionOptionManager.getManager();
        Class.forName("org.tigris.scarab.om.ROptionOption");
        RQueryUserManager.getInstance();
        ScarabUserManager.getInstance();
        ScopeManager.getInstance();
        UserPreferenceManager.getInstance();
        UserVoteManager.getInstance();
    }

    /**
     * Shuts down the service.
     *
     * This method halts the IDBroker's daemon thread in all of
     * the DatabaseMap's.
     */
    public void shutdown()
    {
        Torque.shutdown();
    }
}
