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

import java.beans.BeanDescriptor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;
import org.apache.commons.betwixt.strategy.NameMapper;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fulcrum.localization.Localization;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.util.ScarabConstants;
import org.tigris.scarab.util.TurbineInitialization;
import org.tigris.scarab.workflow.WorkflowFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


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
 * initialized, there is no need to call the init() method.</p>
 *
 * <p>Instances of this class are not thread-safe.</p>
 *
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @version $Id: ImportIssues.java,v 1.34 2004/05/10 21:04:50 dabbous Exp $
 * @since Scarab beta 14
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
     * through the UI.
     */    
    private boolean allowFileAttachments;

    /**
     * Whether we're in validation mode, or some phase after that.
     */
    private boolean validationMode;

    /**
     * A list of any errors encountered during the import, likely
     * added during the validation phase.
     */
    private ImportErrors importErrors;

    public ImportIssues()
    {
        this(false);
    }

    public ImportIssues(boolean allowFileAttachments) 
    {
        this.allowFileAttachments = allowFileAttachments;
        this.importErrors = new ImportErrors();
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

    /**
     * Hook method called by <a
     * href="http://ant.apache.org/">Ant's</a> Task wrapper.
     */
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
    public Collection runImport(File importFile)
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
    public Collection runImport(File importFile, Module currentModule)
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
    public Collection runImport(FileItem importFile)
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
    public Collection runImport(FileItem importFile, Module currentModule)
        throws Exception
    {
        return runImport(importFile.getName(), importFile, currentModule);
    }

    /**
     * @param input A <code>File</code> or <code>FileItem</code>.
     */
    protected Collection runImport(String filePath, Object input,
                                   Module currentModule)
        throws Exception
    {
        String msg = "Importing issues from XML '" + filePath + '\'';
        LOG.debug(msg);
        try
        {
            // Disable workflow and set file attachment flag
            WorkflowFactory.setForceUseDefault(true);
            BeanReader reader = createScarabIssuesBeanReader();
            validate(filePath, inputStreamFor(input), reader, currentModule);

            if (importErrors == null || importErrors.isEmpty())
            {
                // Reget the input stream.
                this.si = insert(filePath, inputStreamFor(input), reader);
            }
        }
        catch (Exception e)
        {
            LOG.error(msg, e);
            throw e; //EXCEPTION
        }
        finally
        {
            // Re-enable workflow.
            WorkflowFactory.setForceUseDefault(false);
        }

        return importErrors;
    }

    /**
     * Coerces a new <code>InputStream</code> from <code>input</code>.
     * Necessary because the stream is read twice by
     * <code>runImport()</code>, so the source of the stream must be
     * passed into that method.
     *
     * @throws IllegalArgumentException If <code>input</code> is
     * unrecognized.
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
            throw new IllegalArgumentException(); //EXCEPTION
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
     * @param currentModule If not <code>null</code>, check whether
     * import is going against this module.
     *
     * @return Any errors encountered during XML or content
     * validation.
     *
     * @exception Exception
     */
    protected void validate(String name, InputStream is,
                            BeanReader reader, Module currentModule)
        throws Exception
    {
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
            // ASSUMPTION: Parse errors prevent entry into this block.
            validateContent(si, currentModule);

            // Log any errors encountered during import.
            if (importErrors != null)
            {
                int nbrErrors = importErrors.size();
                LOG.error("Found " + nbrErrors + " error" +
                          (nbrErrors == 1 ? "" : "s") + " importing '" +
                          name + "':");
                for (Iterator itr = importErrors.iterator(); itr.hasNext(); )
                {
                    LOG.error(itr.next());
                }
            }
        }
    }

    /**
     * Helper method for validate() which invokes validation routines
     * supplied by <code>ScarabIssues</code> plus (conditionally)
     * additional module validation.
     */
    private void validateContent(ScarabIssues si, Module currentModule)
        throws Exception
    {
        if (currentModule != null)
        {
            // Make sure the XML module corresponds to the current
            // module.  This is later than we'd like to perform this
            // check, since we've already parsed the XML.  On the
            // upside, si.getModule() should not return null.
            XmlModule xmlModule = si.getModule();

            // HELP: Check domain also?

            String xmlModuleName = xmlModule.getName();
            String curModuleName = currentModule.getRealName();
            if (!curModuleName.equals(xmlModuleName))
            {
                Object[] args = { xmlModuleName, curModuleName };
                String error = Localization.format
                    (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                     "XMLAndCurrentModuleMismatch", args);
                importErrors.add(error);
            }

            String xmlCode = xmlModule.getCode();
            if (xmlCode == null ||
                !currentModule.getCode().equals(xmlCode))
            {
                Object[] args = { xmlCode, currentModule.getCode() };
                String error = Localization.format
                    (ScarabConstants.DEFAULT_BUNDLE_NAME, getLocale(),
                     "XMLAndCurrentCodeMismatch", args);
                importErrors.add(error);
            }
        }

        si.doValidateDependencies();
        si.doValidateUsers();
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
        this.validationMode = state;

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
        NameMapper nm = reader.getXMLIntrospector().getNameMapper();
        reader.addRule(nm.mapTypeToElementName
                       (new BeanDescriptor(ScarabIssues.class).getName()),
                       new ScarabIssuesSetupRule());
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
     * A rule to perform setup of the a ScarabIssues instance.
     */
    class ScarabIssuesSetupRule extends Rule
    {
        public void begin(String namespace, String name, Attributes attributes)
        {
            ScarabIssues si = (ScarabIssues) getDigester().peek();
            si.allowFileAttachments(allowFileAttachments);
            si.inValidationMode(validationMode);
            si.importErrors = importErrors;
        }
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
        throw e; //EXCEPTION
    }

    /** Receive notification of a non-recoverable error. */
    public void fatalError(SAXParseException e)
        throws SAXParseException
    {
        LOG.error("Parse Fatal Error at line " + e.getLineNumber() +
                  " column " + e.getColumnNumber() + ": " + e.getMessage(), e);
        throw e; //EXCEPTION
    }

    /** Receive notification of a warning. */
    public void warning(SAXParseException e)
    {
        // Warnings are non-fatal.  At some point we should report
        // these back to the end user.
        LOG.debug("Parse Warning at line " + e.getLineNumber() +
                  " column " + e.getColumnNumber() + ": " + e.getMessage());
    }
}
