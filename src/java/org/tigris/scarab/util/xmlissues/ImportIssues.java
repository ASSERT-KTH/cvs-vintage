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
import java.util.ArrayList;
import java.util.Locale;

import java.io.File;
import java.io.Writer;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.commons.fileupload.FileItem;

import org.apache.fulcrum.localization.Localization;
import org.apache.commons.digester.Digester;
import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.tigris.scarab.workflow.WorkflowFactory;
import org.tigris.scarab.util.TurbineInitialization;
import org.tigris.scarab.util.xmlissues.ScarabIssues;
import org.tigris.scarab.util.ScarabConstants;

import org.tigris.scarab.om.Module;


/**
 * This is a bean'ish object which allows one to set values for importing 
 * issues, and then run the actual import. 
 *
 * Amenable to the ant task wrapper or you can pass an explicit file for 
 * explicit import if turbine is already up and running.
 * 
 * <p>The way the ant task wrapper works is simple: call all the appropriate
 * set methods to define the properties. Then you will need to call the init()
 * and execute methods to start running things. Note: If Turbine is already
 * initialized, there is no need to call the init() method.
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @version $Id: ImportIssues.java,v 1.23 2003/07/29 17:29:25 dlr Exp $
 */
public class ImportIssues
    implements ErrorHandler
{
    private static final Log LOG = LogFactory.getLog(ImportIssues.class);

    /**
     * The virtual URL to the document type definition (DTD) used with
     * this version of Scarab.  Though this file doesn't actually
     * exist, it's what can be used as a friendly way to refer to
     * Scarab's DTD in an XML file's <code>DOCTYPE</code> declaration.
     */
    public static final String SYSTEM_DTD_URI =
        "http://scarab.tigris.org/dtd/scarab-0.16.29.dtd";

    /**
     * The absolute URL to the document type definition (DTD) used
     * with this version of Scarab.
     */
    private static final String INTERNAL_DTD_URI =
        "http://scarab.tigris.org/unbranded-source/browse/scarab/src/dtd/scarab.dtd?rev=1.49&content-type=text/plain";

    /**
     * The resource location of the DTD in the classpath.
     */
    private static final String DTD_RESOURCE = "/org/tigris/scarab/scarab.dtd";

    /** 
     * Name of the properties file.
     */
    private String trProps = "/WEB-INF/conf/TurbineResourcesTest.properties";

    /** 
     * Name of the xmlimport.properties file used for configuration of log4j.
     */
    private String configProps = "/WEB-INF/conf/xmlimport.properties";

    private File configDir = null;
    private boolean sendEmail = false;
    private File xmlFile = null;

    /**
     * Current file attachment handling code contains a security hole
     * which can allow a user to see any file on the host that is
     * readable by Scarab.  It is not easy to exploit this hole (you
     * have to know about file paths on a host you likely don't have
     * access to), and there are cases where we want to use the
     * functionality and can be sure the hole is not being exploited.
     * So adding a flag to disallow file attachments when importing
     * through the UI.  private boolean allowFileAttachments;
     */    
    private boolean allowFileAttachments;

    public ImportIssues()
    {
        this(false);
    }

    public ImportIssues(boolean allowFileAttachments) 
    {
        this.allowFileAttachments = allowFileAttachments;
    }

    /**
     * Instance of scarabissues we ran the actual insert with.
     *
     * Make it available post import so importer can get at info about
     * what has just been imported.
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
        return this.configProps;
    }

    public void setConfigFile(String configProps)
    {
        this.configProps = configProps;
    }

    public String getTurbineResources()
    {
        return this.trProps;
    }

    public void setTurbineResources(String trProps)
    {
        this.trProps = trProps;
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
     * Assumes we're up and running inside of turbine.
     *
     * @param importFile File to import.
     *
     * @return List of errors if any.
     *
     * @exception Exception
     */
    public List runImport(File importFile)
        throws Exception
    {
        return runImport(importFile, (Module) null);
    }

    /**
     * Run an import.
     *
     * Assumes we're up and running inside of turbine.
     *
     * @param importFile File to import.
     * @param currentModule If non-null, run check that import is going 
     * against this module.
     *
     * @return List of errors if any.
     *
     * @exception Exception
     */
    public List runImport(File importFile, Module currentModule)
        throws Exception
    {
        return runImport(importFile.getAbsolutePath(), importFile,
                         currentModule);
    }

    /**
     * Run an import.
     *
     * Assumes we're up and running inside of turbine.  Awkwardly duplicates 
     * {@link #runImport(File) import} but duplication is so we can do the reget of
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
        return runImport(importFile, (Module) null);
    }

    /**
     * Run an import.
     *
     * Assumes we're up and running inside of turbine.  Awkwardly duplicates 
     * {@link #runImport(File) import} but duplication is so we can do the reget of
     * the input stream; FileInput "manages" backing up the Upload for us on the
     * second get of the input stream (It creates new ByteArrayInputStream 
     * w/ the src being a byte array of the file its kept in memory or in 
     * temporary storage on disk).  
     *
     * @param importFile FileItem reference to use importing.
     * @param currentModule If non-null, run check that import is going 
     * against this module.
     *
     * @return List of errors if any.
     *
     * @exception Exception
     */
    public List runImport(FileItem importFile, Module currentModule)
        throws Exception
    {
        return runImport(importFile.getName(), importFile, currentModule);
    }

    /**
     * @param input A <code>File</code> or <code>FileItem</code>.
     */
    protected List runImport(String filePath, Object input,
                             Module currentModule)
        throws Exception
    {
        List importErrors = null;
        String msg = "Importing issues from XML '" + filePath + '\'';
        LOG.debug(msg);
        try
        {
            // Disable workflow and set file attachment flag
            WorkflowFactory.setForceUseDefault(true);
            ScarabIssues.allowFileAttachments(allowFileAttachments);
            BeanReader reader = createScarabIssuesBeanReader();
            importErrors = validate(filePath, inputStreamFor(input),
                                    reader, currentModule);

            if (importErrors == null)
            {
                // Reget the input stream.
                this.si = insert(filePath, inputStreamFor(input), reader);
            }
        }
        catch (Exception e)
        {
            LOG.error(msg, e);
            throw e;
        }
        finally
        {
            // Renable workflow and disable file attachments
            WorkflowFactory.setForceUseDefault(false);
            ScarabIssues.allowFileAttachments(false);
        }

        return importErrors;
    }

    /**
     * Coerces a new <code>InputStream</code> from <code>input</code>.
     * Necessary because the stream is read twice by
     * <code>runImport()</code>, so the source of the stream must be
     * passed into that method.
     */
    private InputStream inputStreamFor(Object input)
        throws IOException
    {
        if (input instanceof FileItem)
        {
            return ((FileItem) input).getInputStream();
        }
        else if (input instanceof File)
        {
            return new BufferedInputStream(new FileInputStream((File) input));
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Run validation phase.  Starts by performing XML-well
     * formed-ness and DTD validation (if present), then checks the
     * content.
     *
     * @param name Filename to output in log message.  May be null.
     * @param is Input stream to read.
     * @param reader ScarabIssues bean reader instance.
     * @param currentModule If non-null, run check that import is going 
     * against this module.
     *
     * @return <code>null</code> if the XML stream passes validation,
     * or otherwise the list of errors.
     *
     * @exception Exception.
     */
    protected List validate(String name, InputStream is, BeanReader reader, 
            Module currentModule)
        throws Exception
    {
        List importErrors = null;

        // While parsing the XML, we perform well formed-ness and DTD
        // validation (if present, see Xerces dynamic feature).
        setValidationMode(reader, true);
        ScarabIssues si = null;
        try
        {
            si = (ScarabIssues) reader.parse(is);
        }
        catch (SAXParseException e)
        {
            importErrors = new ArrayList(1);
            // TODO: L10N this error message from Xerces (somehow),
            // and provide a prefix that describes that a XML parse
            // error was encountered.
            importErrors.add("XML parse error at line " + e.getLineNumber() +
                             " column " + e.getColumnNumber() + ": " +
                             e.getMessage());
        }

        // If the XML is okay, validate the actual content.
        if (si != null)
        {
            si.doValidateDependencies();
            si.doValidateUsers();
            importErrors = si.doGetImportErrors();
            if (currentModule != null)
            {
                // If currentModule is not null, make sure the XML
                // module is that of the passed currentModule.  We do
                // the check here late because we know the xml is good
                // if we get this far -- that the si.getModule() will
                // not return null.
                String xmlCode = si.getModule().getCode();
                if (xmlCode == null ||
                    !currentModule.getCode().equals(xmlCode))
                {
                    if (importErrors == null)
                    {
                        importErrors = new ArrayList(1);
                    }

                    // FIXME: This is a bogus error message to report
                    // for an unrecognized code.
                    Object[] args = { si.getModule().getName(),
                                      currentModule.getName() };
                    String error = Localization.format
                        (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                         "XMLModuleNotCurrent", args);
                    importErrors.add(error);
                }
            }

            // Error handling.
            if (importErrors != null) 
            {
                if (importErrors.isEmpty())
                {
                    importErrors = null;
                }
                else
                {
                    LOG.error("Found " + importErrors.size() +
                              " errors importing '" + name + "':");
                    for (Iterator itr = importErrors.iterator();
                         itr.hasNext(); )
                    {
                        LOG.error(itr.next());
                    }
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
        setValidationMode(reader, false);
        ScarabIssues si = (ScarabIssues)reader.parse(is);
        si.doHandleDependencies();
        LOG.debug("Successfully imported " + name + '!');
        return si;
    }

    /**
     * Sets the validation mode for both this instance and the
     * specified <code>Digester</code>.
     *
     * @param reader The XML parser to set the validation mode for.
     * @param state The validation mode.
     * @see <a href="http://xml.apache.org/xerces-j/faq-general.html#valid">Xerces validation FAQ</a>
     * @see <a href="http://xml.apache.org/xerces-j/features.html">Xerces SAX2 feature list</a>
     */
    private void setValidationMode(Digester reader, boolean state)
        throws ParserConfigurationException, SAXException
    {
        ScarabIssues.setInValidationMode(state);

        // Setup the XML parser SAX2 features.

        // Turn on DTD validation (these are functionally equivalent
        // with Xerces 1.4.4 and likely most other SAX2 impls).
        reader.setValidating(state);
        reader.setFeature("http://xml.org/sax/features/validation", state);

        // Validate the document only if a grammar is specified
        // (http://xml.org/sax/features/validation must be state).
        reader.setFeature("http://apache.org/xml/features/validation/dynamic",
                          state);
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
        BeanReader reader = new BeanReader()
            {
                public InputSource resolveEntity(String publicId,
                                                 String systemId)
                    throws SAXException
                {
                    InputSource input = null;
                    if (publicId == null && systemId != null)
                    {
                        // Resolve SYSTEM DOCTYPE.
                        if (SYSTEM_DTD_URI.equalsIgnoreCase(systemId) ||
                            INTERNAL_DTD_URI.equalsIgnoreCase(systemId))
                        {
                            // First look for the DTD in the classpath.
                            input = resolveDTDResource();

                            if (input == null)
                            {
                                // Kick resolution back to Digester.
                                input = super.resolveEntity(publicId,
                                                            systemId);
                            }
                        }
                    }
                    return input;
                }

                /**
                 * Looks for the DTD in the classpath as resouce
                 * {@link #DTD_RESOURCE}.
                 *
                 * @return The DTD, or <code>null</code> if not found.
                 */
                private InputSource resolveDTDResource()
                {
                    InputStream stream =
                        getClass().getResourceAsStream(DTD_RESOURCE);
                    if (stream != null)
                    {
                        LOG.debug("Located DTD in classpath using " +
                                  "resource path '" + DTD_RESOURCE + '\'');
                        return new InputSource(stream);
                    }
                    else
                    {
                        LOG.debug("DTD resource '" + DTD_RESOURCE + "' not " +
                                  "found in classpath");
                        return null;
                    }
                }
            };

        // Connecting Digster's logger to ours logs too verbosely.
        //reader.setLogger(LOG);
        reader.register(SYSTEM_DTD_URI, INTERNAL_DTD_URI);
        // Be forgiving about the encodings we accept.
        reader.setFeature("http://apache.org/xml/features/allow-java-encodings",
                          true);
        reader.setXMLIntrospector(createXMLIntrospector());
        reader.registerBeanClass(ScarabIssues.class);
        reader.setErrorHandler(this);
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

    private Locale getLocale()
    {
        return ScarabConstants.DEFAULT_LOCALE;
    }


    // ---- org.xml.sax.ErrorHandler implementation ------------------------

    /** Receive notification of a recoverable error. */
    public void error(SAXParseException e)
        throws SAXParseException
    {
        LOG.error("Parse Error at line " + e.getLineNumber() +
                  " column " + e.getColumnNumber() + ": " + e.getMessage(), e);
        throw e;
    }

    /** Receive notification of a non-recoverable error. */
    public void fatalError(SAXParseException e)
        throws SAXParseException
    {
        LOG.error("Parse Fatal Error at line " + e.getLineNumber() +
                  " column " + e.getColumnNumber() + ": " + e.getMessage(), e);
        throw e;
    }

    /** Receive notification of a warning. */
    public void warning(SAXParseException e)
        throws SAXParseException
    {
        // Warnings are non-fatal.  At some point we should report
        // these back to the end user.
    }
}
