/*
 * \$Header\$
 * \$Revision\$
 * \$Date\$
 *
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.struts.chain.servlet;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.web.servlet.ServletWebContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.chain.Constants;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.Controller;
import org.apache.struts.tiles.FactoryNotFoundException;
import org.apache.struts.tiles.DefinitionsUtil;
import org.apache.struts.tiles.TilesUtil;

import org.apache.struts.upload.MultipartRequestWrapper;


/**
 * <p>Command class intended to perform responsibilities of the
 * TilesRequestProcessor in Struts 1.1.  Does not actually dispatch requests,
 * but simply prepares the chain context for a later forward as
 * appropriate.  Should be added to a chain before something which
 * would handle a conventional ForwardConfig.</p>
 *
 * <p>This class will never have any effect on the chain unless a
 * <code>TilesDefinitionFactory</code> can be found; however it does not
 * consider the absence of a definition factory to be a fatal error; the
 * command simply returns false and lets the chain continue.</p>
 *
 * <p>To initialize the <code>TilesDefinitionFactory</code>, use
 * <code>org.apache.struts.chain.legacy.TilesPlugin</code>.  This class
 * is a simple extension to <code>org.apache.struts.tiles.TilesPlugin</code>
 * which simply does not interfere with your choice of <code>RequestProcessor</code>
 * implementation.
 *  </p>
 *
 *
 */
public class TilesPreProcessor implements Command
{


    // ------------------------------------------------------ Instance Variables


    private static final Log log = LogFactory.getLog(TilesPreProcessor.class);

    private String forwardConfigKey = Constants.FORWARD_CONFIG_KEY;

    private String includeKey = Constants.INCLUDE_KEY;

    private String moduleConfigKey = Constants.MODULE_CONFIG_KEY;


    // -------------------------------------------------------------- Properties


    /**
     * <p>Return the context attribute key under which the
     * <code>ForwardConfig</code> for the currently selected application
     * action is stored.</p>
     */
    public String getForwardConfigKey() {

        return (this.forwardConfigKey);

    }


    /**
     * <p>Set the context attribute key under which the
     * <code>ForwardConfig</code> for the currently selected application
     * action is stored.</p>
     *
     * @param forwardConfigKey The new context attribute key
     */
    public void setForwardConfigKey(String forwardConfigKey) {

        this.forwardConfigKey = forwardConfigKey;

    }


    /**
     * <p>Return the context attribute key under which the
     * include uri for the currently selected application
     * action is stored.</p>
     */
    public String getIncludeKey() {

        return (this.includeKey);

    }


    /**
     * <p>Set the context attribute key under which the
     * include uri for the currently selected application
     * action is stored.</p>
     *
     * @param includeKey The new context attribute key
     */
    public void setIncludeKey(String includeKey) {

        this.includeKey = includeKey;

    }


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>If the current <code>ForwardConfig</code> is using "tiles",
     * perform necessary pre-processing to set up the <code>TilesContext</code>
     * and substitute a new <code>ForwardConfig</code> which is understandable
     * to a <code>RequestDispatcher</code>.</p>
     *
     * <p>Note that if the command finds a previously existing
     * <code>ComponentContext</code> in the request, then it
     * infers that it has been called from within another tile,
     * so instead of changing the <code>ForwardConfig</code> in the chain
     * <code>Context</code>, the command uses <code>RequestDispatcher</code>
     * to <em>include</em> the tile, and returns true, indicating that the processing
     * chain is complete.</p>
     *
     * @param context The <code>Context</code> for the current request
     *
     * @return <code>false</code> in most cases, but true if we determine
     * that we're processing in "include" mode.
     */
    public boolean execute(Context context) throws Exception {

        // Is there a Tiles Definition to be processed?
        ForwardConfig forwardConfig = (ForwardConfig)
                                      context.get(getForwardConfigKey());
        if (forwardConfig == null || forwardConfig.getPath() == null)
        {
            log.debug("No forwardConfig or no path, so pass to next command.");
            return (false);
        }

        ServletWebContext swcontext = (ServletWebContext) context;

        ComponentDefinition definition = null;
        try
        {
            definition = TilesUtil.getDefinition(forwardConfig.getPath(),
                    swcontext.getRequest(),
                    swcontext.getContext());
        }
        catch (FactoryNotFoundException ex)
        {
            // this is not a serious error, so log at low priority
            log.debug("Tiles DefinitionFactory not found, so pass to next command.");
            return false;
        }

        // Do we do a forward (original behavior) or an include ?
        boolean doInclude = false;
        ComponentContext tileContext = null;

        // Get current tile context if any.
        // If context exists, we will do an include
        tileContext = ComponentContext.getContext(swcontext.getRequest());
        doInclude = (tileContext != null);

        // Controller associated to a definition, if any
        Controller controller = null;

        // Computed uri to include
        String uri = null;

        if (definition != null)
        {
            // We have a "forward config" definition.
            // We use it to complete missing attribute in context.
            // We also get uri, controller.
            uri = definition.getPath();
            controller = definition.getOrCreateController();

            if (tileContext == null) {
                tileContext =
                        new ComponentContext(definition.getAttributes());
                ComponentContext.setContext(tileContext, swcontext.getRequest());

            } else {
                tileContext.addMissing(definition.getAttributes());
            }
        }

        // Process definition set in Action, if any.  This may override the
        // values for uri or controller found using the ForwardConfig, and
        // may augment the tileContext with additional attributes.
        // :FIXME: the class DefinitionsUtil is deprecated, but I can't find
        // the intended alternative to use.
        definition = DefinitionsUtil.getActionDefinition(swcontext.getRequest());
        if (definition != null) { // We have a definition.
                // We use it to complete missing attribute in context.
                // We also overload uri and controller if set in definition.
                if (definition.getPath() != null) {
                    log.debug("Override forward uri "
                              + uri
                              + " with action uri "
                              + definition.getPath());
                        uri = definition.getPath();
                }

                if (definition.getOrCreateController() != null) {
                    log.debug("Override forward controller with action controller");
                        controller = definition.getOrCreateController();
                }

                if (tileContext == null) {
                        tileContext =
                                new ComponentContext(definition.getAttributes());
                        ComponentContext.setContext(tileContext, swcontext.getRequest());
                } else {
                        tileContext.addMissing(definition.getAttributes());
                }
        }


        if (uri == null) {
            log.debug("no uri computed, so pass to next command");
            return false;
        }

        // Execute controller associated to definition, if any.
        if (controller != null) {
            log.trace("Execute controller: " + controller);
            controller.execute(
                    tileContext,
                    swcontext.getRequest(),
                    swcontext.getResponse(),
                    swcontext.getContext());
        }

        // If request comes from a previous Tile, do an include.
        // This allows to insert an action in a Tile.

        if (doInclude) {
            log.info("Tiles process complete; doInclude with " + uri);
            doInclude(swcontext, uri);
            return (true);
        } else {
            // create an "instant" forward config which can be used
            // by an AbstractPerformForward later as if our ForwardConfig
            // were the one actually returned by an executing Action
            log.info("Tiles process complete; forward to " + uri);
            // :FIXME: How do we need to coordinate the "context-relative" value
            // with other places it might be set.  For now, hardcode to true.
            context.put(getForwardConfigKey(), new ForwardConfig("tiles-chain", uri, false, true));
            return (false);
        }
    }


    // ------------------------------------------------------- Protected Methods

    /**
     * <p>Do an include of specified URI using a <code>RequestDispatcher</code>.</p>
     *
     * @param swcontext a chain servlet/web context
     * @param uri Context-relative URI to include
     */
    protected void doInclude(
        ServletWebContext swcontext,
        String uri)
        throws IOException, ServletException {

        HttpServletRequest request = swcontext.getRequest();

        // Unwrap the multipart request, if there is one.
        if (request instanceof MultipartRequestWrapper) {
            request = ((MultipartRequestWrapper) request).getRequest();
        }

        HttpServletResponse response = swcontext.getResponse();
        RequestDispatcher rd = swcontext.getContext().getRequestDispatcher(uri);
        if (rd == null) {
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error getting RequestDispatcher for " + uri);
            return;
        }
        rd.include(request, response);
    }


}