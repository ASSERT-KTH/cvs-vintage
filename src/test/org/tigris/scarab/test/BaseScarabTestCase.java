package org.tigris.scarab.test;

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

import java.io.File;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.fulcrum.TurbineServices;
import org.apache.fulcrum.localization.Localization;
import org.apache.fulcrum.localization.LocalizationService;
import org.apache.fulcrum.testcontainer.BaseUnitTest;
import org.apache.torque.om.NumberKey;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssueManager;
import org.tigris.scarab.om.IssueType;
import org.tigris.scarab.om.IssueTypeManager;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.ModuleManager;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImpl;
import org.tigris.scarab.om.ScarabUserManager;
import org.tigris.scarab.services.cache.ScarabCache;
import org.tigris.scarab.services.cache.ScarabCacheService;
import org.tigris.scarab.test.mocks.MockFulcrumServiceManager;
import org.tigris.scarab.util.word.SearchFactory;
import org.tigris.scarab.util.word.SearchIndex;

/**
 * Base class for Scarab that handles configuring up components.
 * 
 * @author <a href="mailto:epugh@opensourceconnections.com">Eric Pugh</a>
 * @version $Id: BaseScarabTestCase.java,v 1.1 2004/11/23 08:35:00 dep4b Exp $
 */
public abstract class BaseScarabTestCase extends BaseUnitTest {
    

    private ScarabUser user1 = null;
    private ScarabUser user2 = null;
    private ScarabUser user5 = null;

    private Issue issue0 = null;

    public BaseScarabTestCase() {
        super("");
    }
    
    public void setUp() throws Exception{        
        super.setUp();
        configureLogging();
        configureComponents();
        configureFulcrumComponents();
    }

    private void configureFulcrumComponents() {
        TurbineServices.setManager(new MockFulcrumServiceManager());
        
    }

    private void configureLogging() {
        File f = new File("\\logs\\");
        f.mkdir();
        
    }

    /**
     * @throws ComponentException
     */
    private void configureComponents() throws ComponentException {
        lookup(org.apache.torque.avalon.Torque.class.getName());
        ScarabCache.setScarabCacheService((ScarabCacheService)lookup(ScarabCacheService.class.getName()));
        SearchFactory.setSearchIndex((SearchIndex)lookup(SearchIndex.class.getName()));
        Localization.setLocalizationService((LocalizationService)lookup(LocalizationService.class.getName()));
    }

    protected ScarabUser getUser1() throws Exception {
        if (user1 == null) {
           // user1 = ScarabUserManager.getInstance(new NumberKey(1), false);
            user1 = new ScarabUserImpl();
        }
        return user1;
    }
    protected ScarabUser getUser2() throws Exception {
        if (user2 == null) {
            user2 = ScarabUserManager.getInstance(new NumberKey(2), false);

        }
        return user2;
    }    
    
    protected ScarabUser getUser5()
    throws Exception
    {
        if (user5 == null)
        {
            user5 = ScarabUserManager.getInstance(new NumberKey(5), false);
        }
        return user5;
    }    

    protected Issue getIssue0() throws Exception {
        if (issue0 == null) {
            issue0 = IssueManager.getInstance(new NumberKey(1), false);
        }
        return issue0;
    }
    protected Module getModule() throws Exception{
        return ModuleManager.getInstance(new NumberKey(5), false);
    }    
    
    
    protected IssueType getDefaultIssueType() throws Exception
    {
        return IssueTypeManager.getInstance(new NumberKey(1), false);
    }    

}