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
import java.io.Writer;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import org.apache.commons.fileupload.FileItem;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.tigris.scarab.workflow.WorkflowFactory;
import org.tigris.scarab.util.TurbineInitialization;
import org.tigris.scarab.util.xmlissues.ScarabIssues;


/**
 * This is a bean'ish object which allows one to set values for importing 
 * issues, and then run the actual import. 
 *
 * Amenable to the ant task wrapper {@link AntTaskWrapper AntTaskWrapper} or
 * you can pass an explicit file for explicit import if turbine is already 
 * up and running.
 * 
 * <p>The way the ant task wrapper works is simple: call all the appropriate
 * set methods to define the properties. Then you will need to call the init()
 * and execute methods to start running things. Note: If Turbine is already
 * initialized, there is no need to call the init() method.
 *
 * @author <a href="mailto:jon@collab.net">Jon S. Stevens</a>
 * @version $Id: ImportIssues.java,v 1.8 2003/03/22 18:35:51 jon Exp $
 */
public class ImportIssues
{
    private static final Log log = LogFactory.getLog(ImportIssues.class);


    /** 
     * Name of the TR.props file.
     */
    private String TR_PROPS = 
        "/WEB-INF/conf/TurbineResourcesTest.properties";

    /** 
     * Name of the xmlimport.properties file used for configuration of log4j.
     */
    private String CONFIG_PROPS = 
            "/WEB-INF/conf/xmlimport.properties";

    private File configDir = null;
    private boolean sendEmail = false;
    private File xmlFile = null;

    /**
     * Instance of scarabissues we ran the actual insert with.
     *
     * Make it available post import so importer can get at info about what has
     * just been imported.
     */
    private ScarabIssues si = null;


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
        TurbineInitialization.setTurbineResources(getTurbineResources());
        TurbineInitialization.setUp(getConfigDir().getAbsolutePath(), 
            getConfigFile());
    }

    public void execute() 
        throws Exception
    {
        runImport(getXmlFile());
    }

    /**
     * Run an import.
     *
     * Assumes you've already set the xml file we're to run the import with
     * by calling  {@link #setXmlFile}.
     *
     * @return List of errors if any.
     *
     * @exception Exception
     */
    public List runImport(File importFile)
        throws Exception
    {
        List importErrors = null;
        log.debug("Importing: " + importFile.getAbsolutePath());

        try
        {
            // Disable workflow
            WorkflowFactory.setForceUseDefault(true);

            BeanReader reader = createScarabIssuesBeanReader();
            importErrors = validate(importFile.getAbsolutePath(), 
                new BufferedInputStream(new FileInputStream(importFile)),
                reader);
            if (importErrors == null)
            {
                this.si = insert(importFile.getAbsolutePath(), 
                    new BufferedInputStream(new FileInputStream(importFile)),
                    reader);
            }
        }

        catch(Exception e)
        {
            log.debug("\nThe following error(s) were found: " 
                + "\n------------------------------------------------------\n" 
                + e.getMessage());
            throw e;
        }

        finally
        {
            // Renable workflow
            WorkflowFactory.setForceUseDefault(false);
        }

        return importErrors;
    }

    /**
     * Run an import.
     *
     * Assumes we're up and running inside of turbine.  Awkwardly duplicates 
     * {@link import(File) import} but duplication is so we can do the reget of
     * the input stream; FileInput "manages" backing up the Upload for us on the
     * second get of the input stream (It creates new ByteArrayInputStream 
     * w/ the src being a byte array of the file its kept in memory or in 
     * temporary storage on disk).  
     *
     * @param importFile FileItem reference to use importing.
     *
     * @return List of errors if any.
     *
     * @exception Exception
     */
    public List runImport(FileItem importFile)
        throws Exception
    {
        List importErrors = null;
        log.debug("Importing: " + importFile.getName());

        try
        {
            // Disable workflow
            WorkflowFactory.setForceUseDefault(true);
            BeanReader reader = createScarabIssuesBeanReader();
            importErrors = validate(importFile.getName(), 
                importFile.getInputStream(), reader);
            if (importErrors == null)
            {
                // Reget the input stream.
                this.si = insert(importFile.getName(), 
                    importFile.getInputStream(), reader);
            }
        }

        catch(Exception e)
        {
            log.debug("\nThe following error(s) were found: " 
                + "\n------------------------------------------------------\n" 
                + e.getMessage());
            throw e;
        }

        finally
        {
            // Renable workflow
            WorkflowFactory.setForceUseDefault(false);
        }

        return importErrors;
    }

    /**
     * Run validation phase.
     *
     * @param name Filename to output in log message.  May be null.
     * @param is Input stream to read.
     * @param reader ScarabIssues bean reader instance.
     *
     * @return Null if stream passes validation else list of errors.
     *
     * @exception Exception.
     */
    protected List validate(String name, InputStream is, BeanReader reader)
        throws Exception
    {
        ScarabIssues.setInValidationMode(true);
        ScarabIssues si = (ScarabIssues)reader.parse(is);
        si.doValidateDependencies();
        si.doValidateUsers();
        List importErrors = si.doGetImportErrors();
        if (importErrors != null) 
        {
            if (importErrors.size() == 0)
            {
                importErrors = null;
            }
            else
            {
                log.error("Found " + importErrors.size() + " errors importing "
                    + ((name != null)? name: "null") + ":");
                for (Iterator itr = importErrors.iterator(); itr.hasNext();)
                {
                    String message = (String)itr.next();
                    log.error(message);
                }
            }
        }

        return importErrors;
    }

    /**
     * Do actual issue insert.
     *
     * Assumes issues passed have already been validated.  If they haven't
     * been, could damage scarab.
     *
     * @param name Name to use in log messages (E.g. filename).  May be null.
     * @param is Input stream of xml to insert.
     * @param reader ScarabIssues bean reader instance.
     * 
     * @return The instance of scarabissues we inserted in case you need to 
     * display info about the issues inserted.
     */
    protected ScarabIssues insert(String name, InputStream is, 
            BeanReader reader)
        throws Exception
    {
        ScarabIssues.setInValidationMode(false);
        ScarabIssues si = (ScarabIssues)reader.parse(is);
        si.doHandleDependencies();
        log.debug("Successfully imported " + name + "!");
        return si;
    }

    /**
     * Get instance of the ScarabIssues used importing.
     *
     * You'd use this method to get at the instance of scarab issues used 
     * importing for case where you want to print out info on the import thats
     * just happened. Call after a successful import. Calling before will give
     * undefined results.
     *
     * @return Instance of ScarabIssues we ran the import with.
     */
    public ScarabIssues getScarabIssuesBeanReader()
    {
        return this.si;
    }

    /**
     * Return a bean reader for ScarabIssue.
     *
     * @return A bean reader.
     */
    protected BeanReader createScarabIssuesBeanReader()
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
     * Method to output the bean object as XML. 
     * 
     * Not used right now.
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
