/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/share/org/apache/strutsel/taglib/html/ELHtmlTag.java,v 1.6 2003/03/09 05:47:23 dmkarr Exp $
 * $Revision: 1.6 $
 * $Date: 2003/03/09 05:47:23 $
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
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

package org.apache.strutsel.taglib.html;

import org.apache.struts.taglib.html.HtmlTag;
import javax.servlet.jsp.JspException;
import org.apache.strutsel.taglib.utils.EvalHelper;

/**
 * Renders an HTML <html> element with appropriate language attributes if
 * there is a current Locale available in the user's session.
 *<p>
 * This class is a subclass of the class
 * <code>org.apache.struts.taglib.html.HtmlTag</code> which provides most of
 * the described functionality.  This subclass allows all attribute values to
 * be specified as expressions utilizing the JavaServer Pages Standard Library
 * expression language.
 *
 * @author David M. Karr
 * @version $Revision: 1.6 $
 */
public class ELHtmlTag extends HtmlTag {

    /**
     * Instance variable mapped to "locale" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String localeExpr;
    /**
     * Instance variable mapped to "xhtml" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String xhtmlExpr;

    /**
     * Getter method for "locale" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getLocaleExpr() { return (localeExpr); }
    /**
     * Getter method for "xhtml" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getXhtmlExpr() { return (xhtmlExpr); }

    /**
     * Setter method for "locale" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setLocaleExpr(String localeExpr) { this.localeExpr = localeExpr; }
    /**
     * Setter method for "xhtml" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setXhtmlExpr(String xhtmlExpr) { this.xhtmlExpr = xhtmlExpr; }

    /**
     * Resets attribute values for tag reuse.
     */
    public void release()
    {
        super.release();
        setLocaleExpr(null);
        setXhtmlExpr(null);
    }
    
    /**
     * Process the start tag.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {
        evaluateExpressions();
        return (super.doStartTag());
    }

    /**
     * Processes all attribute values which use the JSTL expression evaluation
     * engine to determine their values.
     *
     * @exception JspException if a JSP exception has occurred
     */
    private void evaluateExpressions() throws JspException {
        Boolean bool    = null;

        if ((bool = EvalHelper.evalBoolean("locale", getLocaleExpr(),
                                           this, pageContext)) != null)
            setLocale(bool.booleanValue());

        if ((bool = EvalHelper.evalBoolean("xhtml", getXhtmlExpr(),
                                           this, pageContext)) != null)
            setXhtml(bool.booleanValue());
    }
}
