package org.tigris.scarab.util.xmlissues;

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
import java.util.Iterator;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.tigris.scarab.util.TurbineInitialization;

/**
 * This is a bean'ish object which allows one to set the right
 * values for importing issues. There is an Ant wrapper called
 * ImportIssuesTask.java which allows you to call this file from
 * an Ant xml file. The way this works is simple: call all the
 * appropriate set methods to define the properties. Then you will
 * need to call the init() and execute methods to start running 
 * things. Note: If Turbine is already initialized, there is no need to
 * call the init() method.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ImportIssues.java,v 1.1 2003/01/28 11:41:31 jon Exp $
 */
public class ImportIssues
{
    private final static Log log = LogFactory.getLog(ImportIssues.class);

    /** name of the TR.props file */
    private String TR_PROPS = 
        "/WEB-INF/conf/TurbineResourcesTest.properties";
    /** name of the xmlimport.properties file used for configuration
        of log4j */
    private String CONFIG_PROPS = 
            "/WEB-INF/conf/xmlimport.properties";
    private File configDir = null;
    private boolean sendEmail = false;
    private File xmlFile = null;

    public boolean getSendEmail()
    {
        return this.sendEmail;
    }

    public void setSendEmail(boolean state)
    {
        this.sendEmail = state;
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

    public void init()
        throws Exception
    {
        try
        {
            TurbineInitialization.setTurbineResources(getTurbineResources());
            TurbineInitialization.setUp(getConfigDir().getAbsolutePath(), getConfigFile());
        }
        catch (Exception e)
        {
            throw new Exception(e);
        }
    }
    
    public void execute() 
        throws Exception
    {
        try
        {
            BeanReader reader = createBeanReader();
            log.debug("Importing: " + getXmlFile().getAbsolutePath());
            
            ScarabIssues.setInValidationMode(true);
            ScarabIssues si = (ScarabIssues) reader.parse(
                getXmlFile().getAbsolutePath());
            si.doValidateDependencies();
            si.doValidateUsers();
            List importErrors = si.getImportErrors();
            if (importErrors != null && importErrors.size() > 0)
            {
                log.error("Found " + importErrors.size() + " errors:");
                for (Iterator itr = importErrors.iterator(); itr.hasNext();)
                {
                    String message = (String)itr.next();
                    log.error(message);
                }
                return;
            }
            log.debug("Zero validation errors!");
            ScarabIssues.setInValidationMode(false);
            si = (ScarabIssues) reader.parse(
                getXmlFile().getAbsolutePath());
            si.doHandleDependencies();

            // now lets output it to a buffer
//            StringWriter buffer = new StringWriter();
//            write(si, buffer);
//            log.debug(buffer.toString());
        }
        catch(Exception e)
        {
            log.error("\nThe following error(s) were found: " +
                      "\n------------------------------------------------------\n" +
                      e.getMessage());
            throw new Exception(e);
        }
    }

    protected BeanReader createBeanReader()
        throws Exception
    {
        BeanReader reader = new BeanReader();
        reader.setXMLIntrospector(createXMLIntrospector());
        reader.registerBeanClass(ScarabIssues.class);
        return reader;
    }

    protected XMLIntrospector createXMLIntrospector()
    {
        XMLIntrospector introspector = new XMLIntrospector();

        // set elements for attributes to true
        introspector.setAttributesForPrimitives(false);

        // wrap collections in an XML element
        //introspector.setWrapCollectionsInElement(true);

        // turn bean elements into lower case
        introspector.setElementNameMapper(new HyphenatedNameMapper());

        return introspector;
    }

    /**
     * Method to output the bean object as XML. Not really used
     * right now.
     */
    protected void write(Object bean, Writer out)
        throws Exception
    {
        BeanWriter writer = new BeanWriter(out);
        writer.setXMLIntrospector(createXMLIntrospector());
        writer.enablePrettyPrint();
        writer.setWriteIDs(false);
        writer.write(bean);
    }
}