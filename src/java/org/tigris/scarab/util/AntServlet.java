package org.tigris.scarab.util;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class AntServlet
    extends HttpServlet
{
    private static String buildCommand = 
        new String ("ant -buildfile");

    private static File buildFile =
        new File ("../webapps/newapp/WEB-INF/build/build.xml");

    /**
     * Describe <code>init</code> method here.
     *
     * @param config a <code>ServletConfig</code> value
     * @exception ServletException if an error occurs
     */
    public final void init(ServletConfig config)
        throws ServletException
    {
        super.init(config);

        String command = config.getInitParameter("buildCommand");
        if (command != null)
        {
            buildCommand = command;
            System.out.println ("AntServlet Command: " + 
                buildCommand);
        }
        String file = config.getInitParameter("buildFile");
        if (file != null)
        {
            buildFile = new File(file);
            System.out.println ("AntServlet File: " + 
                buildFile.getAbsolutePath());
        }
    }

    /**
     * Describe <code>doGet</code> method here.
     *
     * @param req a <code>HttpServletRequest</code> value
     * @param res a <code>HttpServletResponse</code> value
     * @exception IOException if an error occurs
     * @exception ServletException if an error occurs
     */
    public final void doGet (HttpServletRequest req, HttpServletResponse res)
        throws IOException,
               ServletException
    {
        res.setContentType("text/html");

        /* Retrieve some parameters.
         * Refresh = the value of refresh tag in seconds.
         * target = the name of the target to execute with ant.
         */        
        String refresh = req.getParameter("refresh");
        String target =  req.getParameter("target");
        
        if (target == null)
        {
            target = new String("");
        }

        /*
         * Write output to the client..
         */
        PrintWriter out = res.getWriter();
        try
        {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Ant Servlet</title>");
            
            // write the refresh tag if necessary 
            if (refresh != null)
            {
                out.println("<meta http-equiv=Refresh content=" + refresh + "/>");
            }
    
            out.println("</head>");
            out.println("<body bgcolor=\"white\">");
            out.println("<hr size=\"1\" noshade=\"true\">");
            out.flush();

            // do the ant command..
            Runtime runtime = Runtime.getRuntime();
    
            int returnValue = 0;
            BufferedReader in = null;
            try
            {
                Process pro = runtime.exec(buildCommand + " " + buildFile + 
                    " " + target);
                in = new BufferedReader(
                                       new InputStreamReader(
                                           pro.getInputStream()));
                String inline = null;
                out.println("<pre>");
                while((inline = in.readLine()) != null)
                {
                    out.println(inline);
                    out.flush();
                }
                out.println("</pre>");
                out.flush();
                returnValue = pro.waitFor();
            }
            catch (Exception ignored)
            {
            }
            finally
            {
                if (in != null)
                {
                    in.close();
                }
            }
    
            out.println("<hr size=\"1\" noshade=\"true\">");
            out.println("</body>");
            out.println("</html>");
            out.flush();
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }
}
