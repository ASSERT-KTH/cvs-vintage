package org.tigris.scarab.util.xml;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.digester.Digester;
import org.apache.turbine.TurbineConfig;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author <a href="mailto:kevin.minshull@bitonic.com">Kevin Minshull</a>
 * @author <a href="mailto:richard.han@bitonic.com">Richard Han</a>
 */
public class DBImport
{
    public static final String STATE_XML_VALIDATION = "XMLVALIDATION";
    public static final String STATE_DB_VALIDATION = "DBVALIDATION";
    public static final String STATE_DB_INSERTION = "DBINSERTION";
    
    private static final String USAGE = "Usage: DBImport [" + STATE_XML_VALIDATION + " | " + STATE_DB_VALIDATION + " | " + STATE_DB_INSERTION + "] <import-xml-file>";
    private static final String TR_PROPS = "/WEB-INF/conf/TurbineResources.properties";
    
    private static boolean initialized = false;
    private static Category cat = Category.getInstance(org.tigris.scarab.util.xml.DBImport.class);
    
    public DBImport()
    {
    }
    
    /**
     * Handles the parsing of the file according to state.
     */
    public void parse(String pathname, String state) throws Exception
    {
        DependencyTree dependencyTree = new DependencyTree();
        
        Digester digester = new Digester();
        digester.push(this);
        digester.setValidating(true);
        digester.setErrorHandler(new ParserErrorHandler());
        
        ArrayList userList = new ArrayList();
        
        if (state.equals(STATE_XML_VALIDATION))
        {
        }
        else if (state.equals(STATE_DB_VALIDATION) || state.equals(STATE_DB_INSERTION))
        {
            digester.addRule("scarab/user", new UserRule(digester, state, userList));
            digester.addRule("scarab/user/firstname", new PropertyRule(digester, state, "user-firstname"));
            digester.addRule("scarab/user/lastname", new PropertyRule(digester, state, "user-lastname"));
            digester.addRule("scarab/user/email", new PropertyRule(digester, state, "user-email"));
            digester.addRule("scarab/module", new ModuleRule(digester, state, dependencyTree));
            digester.addRule("scarab/module/name", new ModuleNameRule(digester, state));
            digester.addRule("scarab/module/code", new ModuleCodeRule(digester, state));
            digester.addRule("scarab/module/issue", new IssueRule(digester, state));
            digester.addRule("scarab/module/issue/artifact-type", new ArtifactTypeRule(digester, state, dependencyTree));
            digester.addRule("scarab/module/issue/committed-by", new CommittedByRule(digester, state, userList));
            digester.addRule("scarab/module/issue/issue-attribute", new IssueAttributeRule(digester, state));
            digester.addRule("scarab/module/issue/issue-attribute/name", new IssueAttributeNameRule(digester, state));
            digester.addRule("scarab/module/issue/issue-attribute/value", new IssueAttributeValueRule(digester, state));
            digester.addRule("scarab/module/issue/issue-attribute/type", new IssueAttributeTypeRule(digester, state));
            digester.addRule("scarab/module/issue/dependency", new DependencyRule(digester, state, dependencyTree));
            digester.addRule("scarab/module/issue/dependency/type", new DependencyTypeRule(digester,state));
            digester.addRule("scarab/module/issue/dependency/child", new DependencyChildRule(digester, state));
            digester.addRule("scarab/module/issue/dependency/parent", new DependencyParentRule(digester, state));
            digester.addRule("scarab/module/issue/attachment", new AttachmentRule(digester, state));
            digester.addRule("scarab/module/issue/attachment/name", new AttachmentNameRule(digester, state));
            digester.addRule("scarab/module/issue/attachment/type", new AttachmentTypeRule(digester, state));
            digester.addRule("scarab/module/issue/attachment/path", new AttachmentPathRule(digester, state));
            digester.addRule("scarab/module/issue/attachment/data", new AttachmentDataRule(digester, state));
            digester.addRule("scarab/module/issue/attachment/mimetype", new AttachmentMimetypeRule(digester, state));
            digester.addRule("scarab/module/issue/attachment/created-date", new AttachmentCreatedDateRule(digester, state));
            digester.addRule("scarab/module/issue/attachment/modified-date", new AttachmentModifiedDateRule(digester, state));
            digester.addRule("scarab/module/issue/attachment/created-by", new AttachmentCreatedByRule(digester, state, userList));
            digester.addRule("scarab/module/issue/attachment/modified-by", new AttachmentModifiedByRule(digester, state, userList));
            digester.addRule("scarab/module/issue/transaction", new TransactionRule(digester, state));
            digester.addRule("scarab/module/issue/transaction/type", new TransactionTypeRule(digester, state));
            digester.addRule("scarab/module/issue/transaction/committed-by", new TransactionCommittedByRule(digester, state, userList));
            digester.addRule("scarab/module/issue/transaction/activity", new ActivityRule(digester, state));
            digester.addRule("scarab/module/issue/transaction/activity/activity-attribute/name", new ActivityAttributeNameRule(digester, state));
            digester.addRule("scarab/module/issue/transaction/activity/activity-attribute/value", new ActivityAttributeValueRule(digester, state));
            digester.addRule("scarab/module/issue/transaction/activity/activity-attribute/old-value", new ActivityAttributeOldValueRule(digester, state));
            digester.addRule("scarab/module/issue/transaction/activity/activity-attribute/type", new ActivityAttributeTypeRule(digester, state));
            digester.addRule("scarab/module/issue/transaction/activity/description", new ActivityAttributeDescriptionRule(digester, state));
        }
        digester.parse(pathname);
        
        if(state.equals(STATE_DB_VALIDATION))
        {
            StringBuffer validationError = new StringBuffer();
            if (!dependencyTree.isAllModuleDependencyValid())
            {
                validationError.append(dependencyTree.getInvalidModuleDependencyInfo());
            }
            
            if (!dependencyTree.isAllIssueDependencyValid())
            {
                validationError.append(dependencyTree.getInvalidIssueDependencyInfo());
            }
            if (!validationError.toString().equals("")) {
                throw new Exception(validationError.toString());
            }
        }
        else if (state.equals(STATE_DB_INSERTION))
        {
            //insert all previously unresolved depencencies
            dependencyTree.resolveIssueDependencies();
        }
    }

    public static void main (String[] args)
    {
        // FIXME: should be a nicer way to accomplish this.
        // this simply determines the directory where this class resides.
        // working in the directory, we can relatively place ourselves to
        // where we need to be to configure turbine for startup.
        String path = new File(DBImport.class.getResource("DBImport.class").getFile()).getParent();
        String configDir;
        if (path.indexOf("target") == -1)
        {
            configDir = path + "/../../../../../../../target/webapps/scarab";
        } else {
            configDir = path + "/../../../../../../../";
        }
        
        //args = new String[] {STATE_DB_INSERTION, path + "/../../../../../../dtd/scarab-sample.xml"};
        
        DBImport importer = new DBImport();
        try
        {
            importer.setUp(configDir);
        }
        catch(Exception e)
        {
            System.out.println("Error found for configuring Turbine");
        }
        if (args.length < 2 || args.length > 2)
        {
            cat.error(USAGE);
            return;
        }
        if (!args[0].equals(STATE_DB_INSERTION) && !args[0].equals(STATE_DB_VALIDATION) && !args[0].equals(STATE_XML_VALIDATION))
        {
            cat.error(USAGE + "\nInvalid argument: " + args[0]);
            return;
        }
        if (!new File(args[1]).exists())
        {
            cat.error(USAGE + "\nFile does not exist.");
            return;
        }
        String state = null;
        try
        {
            importer.parse(args[1], STATE_XML_VALIDATION);
            cat.info(STATE_XML_VALIDATION + " is done without any errors found");
            
            if (args[0].equals(STATE_DB_VALIDATION) || args[0].equals(STATE_DB_INSERTION))
            {
                importer.parse(args[1], STATE_DB_VALIDATION);
                cat.info(STATE_DB_VALIDATION + " is done without any errors found");
            }
            
            if (args[0].equals(STATE_DB_INSERTION))
            {
                importer.parse(args[1], STATE_DB_INSERTION);
                cat.info(STATE_DB_INSERTION + " is done without any errors found");
            }
        }
        catch(Exception e)
        {
            cat.error("\nThe following error(s) found for " + args[0] + 
                          "\n-------------------------------------------------------------------------\n" +
                          e.getMessage());
        }
    }
    
    private void initTurbine (String configDir) throws Exception
    {
        TurbineConfig tc = new TurbineConfig(configDir, TR_PROPS);
        tc.init();
    }
    
    protected void setUp(String configDir) throws Exception
    {
        Properties sysprops = System.getProperties();
        sysprops.setProperty("dataImportRoot", configDir);
        
        if (!initialized)
        {
            if (configDir != null)
            {
                initTurbine(configDir);
                initialized = true;
                
                Properties props = new Properties();
                InputStream is = new File(configDir + "/WEB-INF/conf/dataimport.properties").toURL().openStream();
                props = new Properties();
                try
                {
                    props.load(is);
                    PropertyConfigurator.configure(props);
                }
                catch (Exception e)
                {
                    System.err.println("Can't read the properties file (" + configDir + "/WEB-INF/conf/dataimport.properties). ");
                }
            }
            else
            {
                System.err.println("config.dir System property was not defined");
            }
        }
    }
}
