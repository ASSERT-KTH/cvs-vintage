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
import java.util.ArrayList;

import org.apache.commons.digester.Digester;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.BuildException;
import org.apache.log4j.Category;

import org.tigris.scarab.util.TurbineInitialization;

/**
 * This class manages doing the XMLImport process. It is an Ant Task
 * and should be called from an Ant XML file or directly from Java code.
 * Please look at the build/import.xml file for examples on how to construct
 * an Ant xml file.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: XMLImport.java,v 1.7 2002/01/11 21:13:19 kminshull Exp $
 */
public class XMLImport extends MatchingTask
{
    /** name of the TR.props file */
    private String TR_PROPS = 
        "/WEB-INF/conf/TurbineResourcesTest.properties";

    /** name of the xmlimport.properties file used for configuration
        of log4j */
    private String CONFIG_PROPS = 
            "/WEB-INF/conf/xmlimport.properties";

    private File configDir = null;

    private boolean xmlValidation = true;
    private boolean dbValidation = false;
    private boolean dbInsertion = false;

    protected static final String STATE_XML_VALIDATION = "XMLVALIDATION";
    protected static final String STATE_DB_VALIDATION = "DBVALIDATION";
    protected static final String STATE_DB_INSERTION = "DBINSERTION";

    private File xmlFile = null;

    private Digester digester = null;

    private static Category cat = 
        Category.getInstance(org.tigris.scarab.util.xml.XMLImport.class);

    public XMLImport()
    {
    }
    
    Category log()
    {
        return this.cat;
    }

    public boolean getXmlValidation()
    {
        return this.xmlValidation;
    }

    public void setXmlValidation(boolean state)
    {
        this.xmlValidation = state;
    }

    public boolean getDbValidation()
    {
        return this.dbValidation;
    }

    public void setDbValidation(boolean state)
    {
        this.dbValidation = state;
    }

    public boolean getDbInsertion()
    {
        return this.dbInsertion;
    }

    public void setDbInsertion(boolean state)
    {
        this.dbInsertion = state;
    }

    public File getXmlFile()
    {
        return this.xmlFile;
    }

    public void setXmlFile(File xmlFile)
    {
        this.xmlFile = xmlFile;
    }

    public File getConfigDir()
    {
        return this.configDir;
    }

    public void setConfigDir(File configDir)
    {
        this.configDir = configDir;
    }

    public String getConfigFile()
    {
        return this.CONFIG_PROPS;
    }

    public void setConfigFile(String CONFIG_PROPS)
    {
        this.CONFIG_PROPS = CONFIG_PROPS;
    }

    public String getTurbineResources()
    {
        return this.TR_PROPS;
    }

    public void setTurbineResources(String TR_PROPS)
    {
        this.TR_PROPS = TR_PROPS;
    }

    public void execute() 
        throws BuildException
    {
        try
        {
            TurbineInitialization.setTurbineResources(getTurbineResources());
            TurbineInitialization.setUp(getConfigDir().getAbsolutePath(), getConfigFile());
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }
            
        try
        {
            if (getXmlValidation())
            {
                parse (getXmlFile(), STATE_XML_VALIDATION);
                log().info("XMLValidation is done without any errors found");
            }
            
            if (getDbValidation() || getDbInsertion())
            {
                parse(getXmlFile(), STATE_DB_VALIDATION);
                log().info("DBValidation is done without any errors found");
            }
            
            if (getDbInsertion())
            {
                parse(getXmlFile(), STATE_DB_INSERTION);
                log().info("DBInsertion is done without any errors found");
            }
        }
        catch(Exception e)
        {
            log().error("\nThe following error(s) found: " +
                      "\n------------------------------------------------------\n" +
                      e.getMessage());
            throw new BuildException(e);
        }
    }

    /**
     * Handles the parsing of the file according to state.
     */
    public void parse(File xmlFile, String state) throws Exception
    {
        DependencyTree dependencyTree = new DependencyTree();
        ArrayList userList = new ArrayList();
        
        if (state.equals(STATE_DB_VALIDATION) || state.equals(STATE_DB_INSERTION))
        {
            addRules(state, dependencyTree, userList);
        }

        getDigester().parse(xmlFile.getAbsolutePath());
        
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
            if (!validationError.toString().equals(""))
            {
                throw new Exception(validationError.toString());
            }
        }
        else if (state.equals(STATE_DB_INSERTION))
        {
            //insert all previously unresolved depencencies
            dependencyTree.resolveIssueDependencies();
        }
    }

    /**
     * Creates an instance of a Digester
     */
    protected Digester getDigester()
    {
        if (digester == null)
        {
            digester = new Digester();
            digester.push(this);
            digester.setValidating(true);
            digester.setErrorHandler(new ParserErrorHandler());
        }
        return digester;
    }

    /**
     * Adds rules to the instance of the digester
     */
    protected void addRules(String state, 
                            DependencyTree dependencyTree, ArrayList userList)
    {
        getDigester().addRule("scarab/user", new UserRule(getDigester(), state, userList));
        getDigester().addRule("scarab/user/firstname", new PropertyRule(getDigester(), state, "user-firstname"));
        getDigester().addRule("scarab/user/lastname", new PropertyRule(getDigester(), state, "user-lastname"));
        getDigester().addRule("scarab/user/email", new PropertyRule(getDigester(), state, "user-email"));
        getDigester().addRule("scarab/module", new ModuleRule(getDigester(), state, dependencyTree));
        getDigester().addRule("scarab/module/name", new ModuleNameRule(getDigester(), state));
        getDigester().addRule("scarab/module/code", new ModuleCodeRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue", new IssueRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/artifact-type", new ArtifactTypeRule(getDigester(), state, dependencyTree));
        getDigester().addRule("scarab/module/issue/dependency", new DependencyRule(getDigester(), state, dependencyTree));
        getDigester().addRule("scarab/module/issue/dependency/type", new DependencyTypeRule(getDigester(),state));
        getDigester().addRule("scarab/module/issue/dependency/child", new DependencyChildRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/dependency/parent", new DependencyParentRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/attachment", new AttachmentRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/attachment/name", new AttachmentNameRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/attachment/type", new AttachmentTypeRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/attachment/path", new AttachmentPathRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/attachment/data", new AttachmentDataRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/attachment/mimetype", new AttachmentMimetypeRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/attachment/created-date", new AttachmentCreatedDateRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/attachment/modified-date", new AttachmentModifiedDateRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/attachment/created-by", new AttachmentCreatedByRule(getDigester(), state, userList));
        getDigester().addRule("scarab/module/issue/attachment/modified-by", new AttachmentModifiedByRule(getDigester(), state, userList));
        getDigester().addRule("scarab/module/issue/transaction", new TransactionRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/transaction/type", new TransactionTypeRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/transaction/committed-by", new TransactionCommittedByRule(getDigester(), state, userList));
        getDigester().addRule("scarab/module/issue/transaction/activity", new ActivityRule(getDigester(), state, userList));
        getDigester().addRule("scarab/module/issue/transaction/activity/attribute/name", new ActivityAttributeNameRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/transaction/activity/attribute/value", new ActivityAttributeValueRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/transaction/activity/attribute/old-value", new ActivityAttributeOldValueRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/transaction/activity/attribute/type", new ActivityAttributeTypeRule(getDigester(), state));
        getDigester().addRule("scarab/module/issue/transaction/activity/description", new ActivityDescriptionRule(getDigester(), state));
    }
}
