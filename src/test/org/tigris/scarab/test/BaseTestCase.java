package org.tigris.scarab.test;

/* ================================================================
 * Copyright (c) 2000-2001 CollabNet.  All rights reserved.
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

import org.apache.turbine.TurbineConfig;
import org.apache.torque.om.NumberKey;

import junit.framework.TestCase;

import org.tigris.scarab.om.ScarabModulePeer;
import org.tigris.scarab.om.Issue;
import org.tigris.scarab.om.IssuePeer;
import org.tigris.scarab.om.Activity;
import org.tigris.scarab.om.Attribute;
import org.tigris.scarab.om.AttributePeer;
import org.tigris.scarab.om.ScarabUser;
import org.tigris.scarab.om.ScarabUserImplPeer;
import org.tigris.scarab.om.Attachment;
import org.tigris.scarab.om.Transaction;
import org.tigris.scarab.om.TransactionTypePeer;
import org.tigris.scarab.om.Query;
import org.tigris.scarab.services.module.ModuleEntity;

/**
 * Base test case that provides a few utility methods for
 * the rest of the tests.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @version $Id: BaseTestCase.java,v 1.8 2001/10/24 06:11:00 jon Exp $
 */
public class BaseTestCase extends TestCase
{
    /** name of the TR.props file */
    private static final String TR_PROPS = "/WEB-INF/conf/TurbineResourcesTest.properties";

    private static ModuleEntity module = null;
    private ScarabUser user0 = null;
    private ScarabUser user1 = null;
    private ScarabUser user2 = null;
    private Issue issue0 = null;
    private Attribute platformAttribute = null;

    private static boolean initialized = false;

    /**
     * Default constructor.
     */
    public BaseTestCase(String name)
    {
        super(name);
    }

    protected void setUp()
        throws Exception
    {
        if (!initialized)
        {
            String configDir = System.getProperty("config.dir");
            if (configDir != null)
            {
                initTurbine(configDir);
                initScarab();
                initialized = true;
            }
            else
            {
                System.out.println (
                    "config.dir System property was not defined");
                System.exit(-1);
            }
        }
    }

    /**
     * We are currently depending on TurbineConfig, but this may go away
     * in the future or be changed to something else.
     */
    private void initTurbine (String configDir)
        throws Exception
    {
        TurbineConfig tc = new TurbineConfig(configDir, TR_PROPS);
        tc.init();
    }

    /**
     * Grab Module #5 for testing. This is the same as what the web
     * application does and this is setup in ScarabPage.tempWorkAround()
     */
    private void initScarab()
        throws Exception
    {
        module = (ModuleEntity) ScarabModulePeer.retrieveByPK(new NumberKey(5));
    }

    /**
     * If something like an Issue needs a mapping to a ModuleEntity, then
     * this is Module #5 that you can use. For example, you should call:
     * issue.setModule(getModule()) in your Test before you use any
     * of the rest of the methods on the Issue object.
     */
    protected ModuleEntity getModule()
    {
        return this.module;
    }

    protected ScarabUser getUser0()
        throws Exception
    {
        if (user0 == null)
        {
            user0 = (ScarabUser) ScarabUserImplPeer.retrieveByPK(new NumberKey(0));
        }
        return user0;
    }

    protected ScarabUser getUser1()
        throws Exception
    {
        if (user1 == null)
        {
            user1 = (ScarabUser) ScarabUserImplPeer.retrieveByPK(new NumberKey(2));
        }
        return user1;
    }

    protected ScarabUser getUser2()
        throws Exception
    {
        if (user2 == null)
        {
            user2 = (ScarabUser) ScarabUserImplPeer.retrieveByPK(new NumberKey(3));
        }
        return user2;
    }

    protected Issue getIssue0()
        throws Exception
    {
        if (issue0 == null)
        {
            issue0 = IssuePeer.retrieveByPK(new NumberKey(1));
        }
        return issue0;
    }

    protected Attribute getPlatformAttribute()
        throws Exception
    {
        if (platformAttribute == null)
        {
            platformAttribute  = AttributePeer.retrieveByPK(new NumberKey(5));
        }
        return platformAttribute;
    }

    protected Transaction getEditTransaction()
        throws Exception
    {
        Attachment attach = new Attachment();
        attach.setTextFields(getUser1(), getIssue0(), Attachment.MODIFICATION__PK);
        attach.setName("commenttest");
        attach.save();
        Transaction trans = new Transaction();
        trans.create(TransactionTypePeer.EDIT_ISSUE__PK, getUser1(), attach);
        return trans;
    }

    /**
     * Concatenates the file name parts together appropriately.
     *
     * @return The full path to the file.
     */
    protected static String getFileName (String dir, String base, String ext)
    {
        StringBuffer buf = new StringBuffer();
        if (dir != null)
        {
            buf.append(dir).append('/');
        }
        buf.append(base).append('.').append(ext);
        return buf.toString();
    }

    /**
     * Assures that the results directory exists.  If the results directory
     * cannot be created, fails the test.
     */
    protected static void assureResultsDirectoryExists (String resultsDirectory)
    {
        File dir = new File(resultsDirectory);
        if (!dir.exists())
        {
            System.out.println("Template results directory does not exist");
            if (dir.mkdirs())
            {
                System.out.println("Created template results directory");
            }
            else
            {
                String errMsg = "Unable to create template results directory";
                System.out.println(errMsg);
                fail(errMsg);
            }
        }
    }

    /**
     * Just prints to System.out
     */
    public void log(String message)
    {
        System.out.println(message);
    }
}
