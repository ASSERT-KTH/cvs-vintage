/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language 
 */

package org.apache.jasper.runtime;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.HttpJspPage;
import javax.servlet.jsp.PageContext;

import org.apache.jasper.Constants;

/**
 * This is the subclass of all JSP-generated servlets.
 *
 * @author Anil K. Vijendran
 */
public abstract class HttpJspBase 
    extends HttpServlet 
    implements HttpJspPage 
{
    private ClassLoader cl;

    protected PageContext pageContext;

    protected HttpJspBase() {
    }

    public final void init(ServletConfig config) 
	throws ServletException 
    {
        super.init(config);
	jspInit();
    }
    
    public String getServletInfo() {
	return Constants.getString ("jsp.engine.info");
    }

    public final void destroy() {
	jspDestroy();
    }

    public final void setClassLoader(ClassLoader cl) {
        this.cl = cl;
    }
    
    protected ClassLoader getClassLoader() {
	if (cl == null) 
            return this.getClass().getClassLoader();
	return cl;
    }

    /**
     * Entry point into service.
     */
    public final void service(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException 
    {
	_jspService(request, response);
    }
    
    public void jspInit() {
    }
    
    public void jspDestroy() {
    }
    
    public abstract void _jspService(HttpServletRequest request, 
				     HttpServletResponse response) 
	throws ServletException, IOException;
}
