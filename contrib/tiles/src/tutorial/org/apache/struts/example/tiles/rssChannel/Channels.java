package org.apache.struts.example.tiles.rssChannel;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.portlet.PortletDataHashMap;

import org.apache.commons.digester.rss.RSSDigester;
import org.apache.commons.digester.rss.Channel;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;

import org.apache.struts.tiles.ComponentContext;

/**
 * Read and parse RSS files found at on a given
 * list in user request or session, save the Channel
 * beans in request scope,and forward to "continue".
 * @expects path={uri} on command line or as parameter property to ActionMapping.
 * @expects an input page or error forwarding if exception digesting RSS
 * @author Ted Husted
 * @version $Revision: 1.2 $ $Date: 2001/11/02 16:50:05 $
 */
public final class Channels extends Action {


      /** Debug flag */
    public static final boolean debug = true;
    /**
     * Tile attribute key for saving Channel bean
     */
    public static final String CHANNELS_KEY = "CHANNELS";
    /**
     * Tile attribute key for getting Channel urls list
     */
    public static final String CHANNEL_URLS_KEY = "urls";
    /**
     * Tile attribute key for getting Channel url attribute
     */
    public static final String CHANNEL_URL_KEY = "url";

    // --------------------------------------------------------- Instances Variables
    // --------------------------------------------------------- Public Methods

    /**
     * Main process of class. Reads, parses
     */
    public ActionForward perform(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException, ServletException {

    if(debug)
      System.out.println( "Enter Rss Channel Action" );

          // Try to retrieve tile context
    ComponentContext context = ComponentContext.getContext( request );
    if( context == null )
      {
      throw new ServletException( "This action must be called by a Tile, not directly" );
      }

        ActionErrors errors = new ActionErrors();
        org.apache.commons.digester.rss.Channel channel = null;

        // -- Retrieve parameters --
        // Urls can come from a list, or from a single attribute.

        List channels = (List)context.getAttribute( CHANNEL_URLS_KEY );
        if( channels == null )
          {
          Object url = context.getAttribute( CHANNEL_URL_KEY );
          channels = new ArrayList(1);
          channels.add(url);
          }
            //channels.add("http://www.newsforge.com/newsforge.rss");
            //channels.add("http://xmlhack.com/rss.php");
            //channels.add("http://lwn.net/headlines/rss");
        // channels.trimToSize();

        if(debug)
          System.out.println( "urls count" + channels.size() ) ;
        // -- Loop through channels --
        ArrayList channelBeans = new ArrayList(channels.size());
        try {
            for (int i=0; i<channels.size(); i++) {
                RSSDigester digester = new RSSDigester();
                String url = (String)channels.get(i);
                  // Add application path if needed
                if( url.startsWith("/") )
                  {
                  url = toFullUrl( request, url );
                  }
                if(debug)  System.out.println( "Channel url=" + url) ;
                Channel obj = (Channel)digester.parse(url);
                if(debug)  System.out.println( "Channel:" + obj) ;
                //System.out.println( "Channel.items:" + obj.getI) ;
                channelBeans.add(obj);
            }
        }
        catch (Throwable t) {
            errors.add(ActionErrors.GLOBAL_ERROR,
                new ActionError("rss.access.error"));
            servlet.log(t.toString());
        }

        // -- Handle Errors ---
        if (!errors.empty()) {
            saveErrors(request, errors);
            if (mapping.getInput()!=null)
                return (new ActionForward(mapping.getInput()));
            // If no input page, use error forwarding
         if(debug)
           System.out.println( "Exit Rss Channel Action : error" );
            return (mapping.findForward("error"));
        }

        // -- Save Bean, and Continue  ---

         if(debug)
           System.out.println( "Exit Rss Channel Action" );
        //request.setAttribute(CHANNELS_KEY,channelBeans);
          // Use Tile context to pass channels
        context.putAttribute( CHANNELS_KEY,channelBeans);
        return (mapping.findForward("continue"));
    } // ---- End perform ----

  private String toFullUrl( HttpServletRequest request, String url )
    {
    StringBuffer buff = new StringBuffer();

    buff.append( request.getScheme() ) .append( "://" ) . append(request.getServerName());
    if( request.getServerPort() != 80 )
      buff.append( ":" ).append( request.getServerPort() );
    buff.append( request.getContextPath()).append( url);
    return buff.toString();
    }

} // ---- End Fetch ----


/*
 * $Header: /tmp/cvs-vintage/struts/contrib/tiles/src/tutorial/org/apache/struts/example/tiles/rssChannel/Attic/Channels.java,v 1.2 2001/11/02 16:50:05 cedric Exp $
 * $Revision: 1.2 $
 * $Date: 2001/11/02 16:50:05 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
 *
 */

