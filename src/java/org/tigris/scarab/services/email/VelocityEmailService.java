package org.tigris.scarab.services.email;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.Vector;
import java.util.Iterator;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalEventContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.fulcrum.ServiceException;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.template.TemplateContext;
import org.apache.fulcrum.template.BaseTemplateEngineService;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.fulcrum.velocity.ContextAdapter;

import org.tigris.scarab.tools.ScarabLocalizationTool;
import org.tigris.scarab.util.ScarabConstants;

/**
 * This is a Service that can process Velocity templates from within a
 * Turbine Screen.  Here's an example of how you might use it from a
 * screen:<br>
 *
 * <code><pre>
 * Context context = new VelocityContext();
 * context.put("message", "Hello from Turbine!");
 * String results = TurbineVelocity.handleRequest(context,"HelloWorld.vm");
 * </pre></code>
 *
 * Character sets map codes to glyphs, while encodings map between
 * chars/bytes and codes.
 * <i>bytes -> [encoding] -> charset -> [rendering] -> glyphs</i>
 *
 * <p>This copy of TurbineVelocityService has been slightly modified
 * from its original form to support toggling of Scarab's cross-site
 * scripting filter.</p>
 *
 * @author <a href="mailto:mbryson@mont.mindspring.com">Dave Bryson</a>
 * @author <a href="mailto:krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:jvanzyl@periapt.com">Jason van Zyl</a>
 * @author <a href="mailto:sean@informage.ent">Sean Legassick</a>
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @author <a href="mailto:jon@latchkey.com">Jon S. Stevens</a>
 * @author <a href="mailto:mpoeschl@marmot.at">Martin Poeschl</a>
 * @author <a href="mailto:james@jamestaylor.org">James Taylor</a>
 * @version $Id: VelocityEmailService.java,v 1.4 2003/05/03 22:37:24 jon Exp $
 */
public class VelocityEmailService
    extends BaseTemplateEngineService
    implements EmailService
{
    /**
     * The generic resource loader path property in velocity.
     */
    private static final String RESOURCE_LOADER_PATH =
        ".resource.loader.path";

    /**
     * Default character set to use if not specified by caller.
     */
    private static final String DEFAULT_CHAR_SET = "ISO-8859-1";

    /**
     * The prefix used for URIs which are of type <code>jar</code>.
     */
    private static final String JAR_PREFIX = "jar:";

    /**
     * The prefix used for URIs which are of type <code>absolute</code>.
     */
    private static final String ABSOLUTE_PREFIX = "file://";

    /**
     * The VelocityEngine used by the service to merge templates
     */
    private VelocityEngine velocityEngine = new VelocityEngine();

    /**
     * Performs early initialization of this Turbine service.
     */
    public void init()
        throws InitializationException
    {
        try
        {
            initVelocity();
            // Register with the template service.
            registerConfiguration("ve");
            setInit(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new InitializationException(
                "Failed to initialize VelocityEmailService", e);
        }
    }

    /**
     * @see org.apache.fulcrum.velocity.VelocityService
     */
    public String handleRequest(TemplateContext context, String template)
        throws ServiceException
    {
        return handleRequest(new ContextAdapter(context), template);
    }

    /**
     * @see org.apache.fulcrum.velocity.VelocityService
     */
    public String handleRequest(Context context, String filename)
        throws ServiceException
    {
        return handleRequest(context, filename, (String)null, null);
    }

    /**
     * @see org.apache.fulcrum.velocity.VelocityService
     */
    public String handleRequest(Context context, String filename,
                                String charset, String encoding)
        throws ServiceException
    {
        String results = null;
        if (charset == null)
	    {
            StringWriter writer = null;
            try
            {
                writer = new StringWriter();
                handleRequest(context, filename, writer, encoding);
                results = writer.toString();
            }
            catch (Exception e)
            {
                renderingError(filename, e);
            }
            finally
            {
                try
                {
                    if (writer != null)
                    {
                        writer.close();
                    }
                }
                catch (IOException ignored)
                {
                }
            }
        }
        else 
        {
        ByteArrayOutputStream bytes = null;
        try
        {
            bytes = new ByteArrayOutputStream();
            charset = decodeRequest(context, filename, bytes, charset,
                                    encoding);
            results = bytes.toString(charset);
        }
        catch (Exception e)
        {
            renderingError(filename, e);
        }
        finally
        {
            try
            {
                if (bytes != null)
                {
                    bytes.close();
                }
            }
            catch (IOException ignored)
            {
            }
        }
        }
        
        return results;
    }

    /**
     * @see org.apache.fulcrum.template.TemplateEngineService
     */
    public void handleRequest(TemplateContext context, String template,
                              OutputStream outputStream)
        throws ServiceException
    {
        handleRequest(new ContextAdapter(context), template, outputStream);
    }

    /**
     * @see org.apache.fulcrum.velocity.VelocityService
     */
    public void handleRequest(Context context, String filename,
                              OutputStream output)
        throws ServiceException
    {
        handleRequest(context, filename, output, null, null);
    }

    /**
     * @see org.apache.fulcrum.velocity.VelocityService
     */
    public void handleRequest(Context context, String filename,
                              OutputStream output, String charset,
                              String encoding)
        throws ServiceException
    {
        decodeRequest(context, filename, output, charset, encoding);
    }

    /**
     * @see BaseTemplateEngineService#handleRequest(TemplateContext, String, Writer)
     */
    public void handleRequest(TemplateContext context,
                                       String template, Writer writer)
        throws ServiceException
    {
        handleRequest(new ContextAdapter(context), template, writer);
    }

    /**
     * @see VelocityEmailService#handleRequest(Context, String, Writer)
     */
    public void handleRequest(Context context, String filename,
                              Writer writer)
        throws ServiceException
    {
        handleRequest(context, filename, writer, null);
    }

    /**
     * @see VelocityEmailService#handleRequest(Context, String, Writer, String)
     */
    public void handleRequest(Context context, String filename,
                              Writer writer, String encoding)
        throws ServiceException
    {
        ScarabLocalizationTool l10n = null;
        try
        {
            // If the context is not already an instance of
            // InternalEventContext, wrap it in a VeclocityContext so that
            // event cartridges will work. Unfortunately there is no interface
            // that extends both Context and InternalEventContext, so this
            // is not as clear as it could be.

            Context eventContext;

            if ( context instanceof InternalEventContext )
            {
                eventContext = context;
            }
            else
            {
                eventContext = new VelocityContext( context );
            }

            if (encoding == null)
            {
                encoding = DEFAULT_CHAR_SET;
            }

            // Assumption: Emails are always plain text.
            l10n = (ScarabLocalizationTool)
                context.get(ScarabConstants.LOCALIZATION_TOOL);
            if (l10n != null)
            {
                l10n.setFilterEnabled(false);
            }
            // Request scoped encoding first supported by Velocity 1.1.
            velocityEngine.mergeTemplate(filename, encoding,
                                         eventContext, writer);
        }
        catch (Exception e)
        {
            renderingError(filename, e);
        }
        finally
        {
            if (l10n != null)
            {
                l10n.setFilterEnabled(true);
            }
        }
    }

    /**
     * Processes the request and fill in the template with the values
     * you set in the the supplied Context. Applies the specified
     * character and template encodings.
     *
     * @param context A context to use when evaluating the specified
     * template.
     * @param filename The file name of the template.
     * @param output The stream to which we will write the processed
     * template as a String.
     * @return The character set applied to the resulting text.
     *
     * @throws ServiceException Any exception trown while processing
     * will be wrapped into a ServiceException and rethrown.
     */
    private String decodeRequest(Context context, String filename,
                                 OutputStream output, String charset,
                                 String encoding)
        throws ServiceException
    {
        if (charset == null)
        {
            charset = DEFAULT_CHAR_SET;
        }

        OutputStreamWriter writer = null;
        try
        {
            try
            {
                writer = new OutputStreamWriter(output, charset);
            }
            catch (Exception e)
            {
                renderingError(filename, e);
            }
            handleRequest(context, filename, writer, encoding);
        }
        finally
        {
            try
            {
                if (writer != null)
                {
                    writer.flush();
                }
            }
            catch (Exception ignored)
            {
            }
        }
        return charset;
    }

    /**
     * Macro to handle rendering errors.
     *
     * @param filename The file name of the unrenderable template.
     * @param e        The error.
     *
     * @exception ServiceException Thrown every time.  Adds additional
     *                             information to <code>e</code>.
     */
    private final void renderingError(String filename, Throwable e)
        throws ServiceException
    {
        String err = "Error rendering Velocity template: " + filename;
        getCategory().error(err + ": " + e.getMessage());
        // if the Exception is a MethodInvocationException, the underlying
        // Exception is likely to be more informative, so rewrap that one.
        if (e instanceof MethodInvocationException)
        {
            e = ((MethodInvocationException)e).getWrappedThrowable();
        }

        throw new ServiceException(err, e);
    }

    /**
     * Setup the velocity runtime by using a subset of the
     * Turbine configuration which relates to velocity.
     *
     * @exception InitializationException For any errors during initialization.
     */
    private void initVelocity()
        throws InitializationException
    {
        // Now we have to perform a couple of path translations
        // for our log file and template paths.
        String path = getRealPath(
            getConfiguration().getString(VelocityEngine.RUNTIME_LOG, null));

        if (path != null && path.length() > 0)
        {
            getConfiguration().setProperty(VelocityEngine.RUNTIME_LOG, path);
        }
        else
        {
            String msg = EmailService.SERVICE_NAME + " runtime log file " +
                "is misconfigured: '" + path + "' is not a valid log file";

            throw new Error(msg);
        }

        // Get all the template paths where the velocity
        // runtime should search for templates and
        // collect them into a separate vector
        // to avoid concurrent modification exceptions.
        String key;
        Vector keys = new Vector();
        for (Iterator i = getConfiguration().getKeys(); i.hasNext();)
        {
            key = (String) i.next();
            if (key.endsWith(RESOURCE_LOADER_PATH))
            {
                keys.add(key);
            }
        }

        // Loop through all template paths, clear the corresponding
        // velocity properties and translate them all to the webapp space.
        int ind;
        Vector paths;
        String entry;
        for (Iterator i = keys.iterator(); i.hasNext();)
        {
            key = (String) i.next();
            paths = getConfiguration().getVector(key,null);
            if (paths != null)
            {
                velocityEngine.clearProperty(key);
                getConfiguration().clearProperty(key);

                for (Iterator j = paths.iterator(); j.hasNext();)
                {
                    path = (String) j.next();
                    if (path.startsWith(JAR_PREFIX + "file"))
                    {
                        // A local jar resource URL path is a bit more
                        // complicated, but we can translate it as well.
                        ind = path.indexOf("!/");
                        if (ind >= 0)
                        {
                            entry = path.substring(ind);
                            path = path.substring(9,ind);
                        }
                        else
                        {
                            entry = "!/";
                            path = path.substring(9);
                        }
                        path = JAR_PREFIX + "file:" + getRealPath(path) +
                            entry;
                    }
                    else if (path.startsWith(ABSOLUTE_PREFIX))
                    {
                        path = path.substring (ABSOLUTE_PREFIX.length(),
                                               path.length());
                    }
                    else if (!path.startsWith(JAR_PREFIX))
                    {
                        // But we don't translate remote jar URLs.
                        path = getRealPath(path);
                    }
                    // Put the translated paths back to the configuration.
                    getConfiguration().addProperty(key,path);
                }
            }
        }

        try
        {
            velocityEngine.setExtendedProperties(ConfigurationConverter
                    .getExtendedProperties(getConfiguration()));

            velocityEngine.init();
        }
        catch(Exception e)
        {
            // This will be caught and rethrown by the init() method.
            // Oh well, that will protect us from RuntimeException folk showing
            // up somewhere above this try/catch
            throw new InitializationException(
                "Failed to set up VelocityEmailService", e);
        }
    }

    /**
     * Find out if a given template exists. Velocity
     * will do its own searching to determine whether
     * a template exists or not.
     *
     * @param String template to search for
     * @return boolean
     */
    public boolean templateExists(String template)
    {
        return velocityEngine.templateExists(template);
    }
}
