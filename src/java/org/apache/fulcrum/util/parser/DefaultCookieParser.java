package org.apache.fulcrum.util.parser;

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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.fulcrum.pool.Recyclable;
import org.apache.log4j.Category;

/**
 * CookieParser is used to get and set values of Cookies on the Client
 * Browser.  You can use CookieParser to convert Cookie values to
 * various types or to set Bean values with setParameters(). See the
 * Servlet Spec for more information on Cookies.
 * <p>
 * Use set() or unset() to Create or Destroy Cookies.
 * <p>
 * NOTE: The name= portion of a name=value pair may be converted
 * to lowercase or uppercase when the object is initialized and when
 * new data is added.  This behaviour is determined by the url.case.folding
 * property in TurbineResources.properties.  Adding a name/value pair may
 * overwrite existing name=value pairs if the names match:
 *
 * <pre>
 * CookieParser cp = data.getCookies();
 * cp.add("ERROR",1);
 * cp.add("eRrOr",2);
 * int result = cp.getInt("ERROR");
 * </pre>
 *
 * In the above example, result is 2.
 *
 * @author <a href="mailto:ilkka.priha@simsoft.fi">Ilkka Priha</a>
 * @author <a href="mailto:leon@opticode.co.za">Leon Messerschmidt</a>
 * @version $Id: DefaultCookieParser.java,v 1.1 2004/10/24 22:12:30 dep4b Exp $
 */
public class DefaultCookieParser
    extends BaseValueParser
    implements CookieParser,
               Recyclable
{
    /**
     * The run data to parse.
     */
    private HttpServletRequest request;
    private HttpServletResponse response;

    /**
     * Log4j category
     */
    Category category = Category.getInstance(getClass().getName());

    /**
     * Constructs a new CookieParser.
     */
    public DefaultCookieParser()
    {
        super();
    }

    /**
     * Disposes the parser.
     */
    public void dispose()
    {
        this.request = null;
        super.dispose();
    }

    /**
     * Gets the parsed RunData.
     *
     * @return the parsed RunData object or null.
     */
    public HttpServletRequest getRequest()
    {
        return this.request;
    }

    /**
     * Sets the RunData to be parsed.
     * All previous cookies will be cleared.
     *
     * @param data the RunData object.
     */
    public void setData (HttpServletRequest request,
                         HttpServletResponse response)
    {
        clear();

        String enc = request.getCharacterEncoding();
        setCharacterEncoding(enc != null ? enc : "US-ASCII");

        Cookie[] cookies = request.getCookies();

        category.debug ("Number of Cookies "+cookies.length);

        for (int i=0; i<cookies.length; i++)
        {
            String name = convert (cookies[i].getName());
            String value = cookies[i].getValue();
            category.debug ("Adding "+name+"="+value);
            add (name,value);
        }

        this.request = request;
        this.response = response;
    }

    /**
     * Set a cookie that will be stored on the client for
     * the duration of the session.
     */
    public void set (String name, String value)
    {
        set (name,value,AGE_SESSION);
    }

    /**
     * Set a persisten cookie on the client that will expire
     * after a maximum age (given in seconds).
     */
    public void set (String name, String value, int seconds_age)
    {
        if (response == null)
        {
            throw new IllegalStateException("Servlet response not available");
        }

        Cookie cookie = new Cookie (name,value);
        cookie.setMaxAge (seconds_age);
        cookie.setPath (request.getServletPath());
        response.addCookie (cookie);
    }

    /**
     * Remove a previously set cookie from the client machine.
     */
    public void unset (String name)
    {
        set (name," ",AGE_DELETE);
    }

}
