/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/share/org/apache/strutsel/taglib/html/ELLinkTag.java,v 1.9 2004/01/18 07:11:27 dmkarr Exp $
 * $Revision: 1.9 $
 * $Date: 2004/01/18 07:11:27 $
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

import org.apache.struts.taglib.html.LinkTag;
import javax.servlet.jsp.JspException;
import org.apache.strutsel.taglib.utils.EvalHelper;

/**
 * Generate a URL-encoded hyperlink to the specified URI.
 *<p>
 * This class is a subclass of the class
 * <code>org.apache.struts.taglib.html.LinkTag</code> which provides most of
 * the described functionality.  This subclass allows all attribute values to
 * be specified as expressions utilizing the JavaServer Pages Standard Library
 * expression language.
 *
 * @author David M. Karr
 * @version $Revision: 1.9 $
 */
public class ELLinkTag extends LinkTag {

    /**
     * Instance variable mapped to "accessKey" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String accessKeyExpr;
    /**
     * Instance variable mapped to "action" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String actionExpr;
    /**
     * Instance variable mapped to "anchor" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String anchorExpr;
    /**
     * Instance variable mapped to "forward" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String forwardExpr;
    /**
     * Instance variable mapped to "href" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String hrefExpr;
    /**
     * Instance variable mapped to "indexed" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String indexedExpr;
    /**
     * Instance variable mapped to "indexId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String indexIdExpr;
    /**
     * Instance variable mapped to "linkName" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String linkNameExpr;
    /**
     * Instance variable mapped to "name" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String nameExpr;
    /**
     * Instance variable mapped to "onblur" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onblurExpr;
    /**
     * Instance variable mapped to "onclick" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onclickExpr;
    /**
     * Instance variable mapped to "ondblclick" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String ondblclickExpr;
    /**
     * Instance variable mapped to "onfocus" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onfocusExpr;
    /**
     * Instance variable mapped to "onkeydown" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onkeydownExpr;
    /**
     * Instance variable mapped to "onkeypress" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onkeypressExpr;
    /**
     * Instance variable mapped to "onkeyup" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onkeyupExpr;
    /**
     * Instance variable mapped to "onmousedown" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onmousedownExpr;
    /**
     * Instance variable mapped to "onmousemove" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onmousemoveExpr;
    /**
     * Instance variable mapped to "onmouseout" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onmouseoutExpr;
    /**
     * Instance variable mapped to "onmouseover" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onmouseoverExpr;
    /**
     * Instance variable mapped to "onmouseup" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String onmouseupExpr;
    /**
     * Instance variable mapped to "page" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String pageExpr;
    /**
     * Instance variable mapped to "paramId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String paramIdExpr;
    /**
     * Instance variable mapped to "paramName" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String paramNameExpr;
    /**
     * Instance variable mapped to "paramProperty" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String paramPropertyExpr;
    /**
     * Instance variable mapped to "paramScope" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String paramScopeExpr;
    /**
     * Instance variable mapped to "property" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String propertyExpr;
    /**
     * Instance variable mapped to "scope" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String scopeExpr;
    /**
     * Instance variable mapped to "style" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String styleExpr;
    /**
     * Instance variable mapped to "styleClass" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String styleClassExpr;
    /**
     * Instance variable mapped to "styleId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String styleIdExpr;
    /**
     * Instance variable mapped to "tabindex" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String tabindexExpr;
    /**
     * Instance variable mapped to "target" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String targetExpr;
    /**
     * Instance variable mapped to "title" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String titleExpr;
    /**
     * Instance variable mapped to "titleKey" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String titleKeyExpr;
    /**
     * Instance variable mapped to "transaction" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String transactionExpr;
    /**
     * Instance variable mapped to "useLocalEncoding" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String useLocalEncodingExpr;

    /**
     * Getter method for "accessKey" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getAccesskeyExpr() { return (accessKeyExpr); }
    /**
     * Getter method for "action" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getActionExpr() { return (actionExpr); }
    /**
     * Getter method for "anchor" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getAnchorExpr() { return (anchorExpr); }
    /**
     * Getter method for "forward" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getForwardExpr() { return (forwardExpr); }
    /**
     * Getter method for "href" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getHrefExpr() { return (hrefExpr); }
    /**
     * Getter method for "indexed" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getIndexedExpr() { return (indexedExpr); }
    /**
     * Getter method for "indexId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getIndexIdExpr() { return (indexIdExpr); }
    /**
     * Getter method for "linkName" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getLinkNameExpr() { return (linkNameExpr); }
    /**
     * Getter method for "name" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getNameExpr() { return (nameExpr); }
    /**
     * Getter method for "onblur" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnblurExpr() { return (onblurExpr); }
    /**
     * Getter method for "onclick" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnclickExpr() { return (onclickExpr); }
    /**
     * Getter method for "ondblclick" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOndblclickExpr() { return (ondblclickExpr); }
    /**
     * Getter method for "onfocus" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnfocusExpr() { return (onfocusExpr); }
    /**
     * Getter method for "onkeydown" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnkeydownExpr() { return (onkeydownExpr); }
    /**
     * Getter method for "onkeypress" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnkeypressExpr() { return (onkeypressExpr); }
    /**
     * Getter method for "onkeyup" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnkeyupExpr() { return (onkeyupExpr); }
    /**
     * Getter method for "onmousedown" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnmousedownExpr() { return (onmousedownExpr); }
    /**
     * Getter method for "onmousemove" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnmousemoveExpr() { return (onmousemoveExpr); }
    /**
     * Getter method for "onmouseout" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnmouseoutExpr() { return (onmouseoutExpr); }
    /**
     * Getter method for "onmouseover" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnmouseoverExpr() { return (onmouseoverExpr); }
    /**
     * Getter method for "onmouseup" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOnmouseupExpr() { return (onmouseupExpr); }
    /**
     * Getter method for "page" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getPageExpr() { return (pageExpr); }
    /**
     * Getter method for "paramId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getParamIdExpr() { return (paramIdExpr); }
    /**
     * Getter method for "paramName" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getParamNameExpr() { return (paramNameExpr); }
    /**
     * Getter method for "paramProperty" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getParamPropertyExpr() { return (paramPropertyExpr); }
    /**
     * Getter method for "paramScope" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getParamScopeExpr() { return (paramScopeExpr); }
    /**
     * Getter method for "property" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getPropertyExpr() { return (propertyExpr); }
    /**
     * Getter method for "scope" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getScopeExpr() { return (scopeExpr); }
    /**
     * Getter method for "style" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getStyleExpr() { return (styleExpr); }
    /**
     * Getter method for "styleClass" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getStyleClassExpr() { return (styleClassExpr); }
    /**
     * Getter method for "styleId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getStyleIdExpr() { return (styleIdExpr); }
    /**
     * Getter method for "tabindex" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getTabindexExpr() { return (tabindexExpr); }
    /**
     * Getter method for "target" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getTargetExpr() { return (targetExpr); }
    /**
     * Getter method for "title" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getTitleExpr() { return (titleExpr); }
    /**
     * Getter method for "titleKey" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getTitleKeyExpr() { return (titleKeyExpr); }
    /**
     * Getter method for "transaction" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getTransactionExpr() { return (transactionExpr); }
    /**
     * Getter method for "useLocalEncoding" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getUseLocalEncodingExpr() { return (useLocalEncodingExpr); }

    /**
     * Setter method for "accessKey" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setAccesskeyExpr(String accessKeyExpr) { this.accessKeyExpr = accessKeyExpr; }
    /**
     * Setter method for "action" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setActionExpr(String actionExpr) { this.actionExpr = actionExpr; }
    /**
     * Setter method for "anchor" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setAnchorExpr(String anchorExpr) { this.anchorExpr = anchorExpr; }
    /**
     * Setter method for "forward" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setForwardExpr(String forwardExpr) { this.forwardExpr = forwardExpr; }
    /**
     * Setter method for "href" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setHrefExpr(String hrefExpr) { this.hrefExpr = hrefExpr; }
    /**
     * Setter method for "indexed" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setIndexedExpr(String indexedExpr) { this.indexedExpr = indexedExpr; }
    /**
     * Setter method for "indexId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setIndexIdExpr(String indexIdExpr) { this.indexIdExpr = indexIdExpr; }
    /**
     * Setter method for "linkName" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setLinkNameExpr(String linkNameExpr) { this.linkNameExpr = linkNameExpr; }
    /**
     * Setter method for "name" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setNameExpr(String nameExpr) { this.nameExpr = nameExpr; }
    /**
     * Setter method for "onblur" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnblurExpr(String onblurExpr) { this.onblurExpr = onblurExpr; }
    /**
     * Setter method for "onclick" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnclickExpr(String onclickExpr) { this.onclickExpr = onclickExpr; }
    /**
     * Setter method for "ondblclick" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOndblclickExpr(String ondblclickExpr) { this.ondblclickExpr = ondblclickExpr; }
    /**
     * Setter method for "onfocus" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnfocusExpr(String onfocusExpr) { this.onfocusExpr = onfocusExpr; }
    /**
     * Setter method for "onkeydown" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnkeydownExpr(String onkeydownExpr) { this.onkeydownExpr = onkeydownExpr; }
    /**
     * Setter method for "onkeypress" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnkeypressExpr(String onkeypressExpr) { this.onkeypressExpr = onkeypressExpr; }
    /**
     * Setter method for "onkeyup" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnkeyupExpr(String onkeyupExpr) { this.onkeyupExpr = onkeyupExpr; }
    /**
     * Setter method for "onmousedown" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnmousedownExpr(String onmousedownExpr) { this.onmousedownExpr = onmousedownExpr; }
    /**
     * Setter method for "onmousemove" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnmousemoveExpr(String onmousemoveExpr) { this.onmousemoveExpr = onmousemoveExpr; }
    /**
     * Setter method for "onmouseout" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnmouseoutExpr(String onmouseoutExpr) { this.onmouseoutExpr = onmouseoutExpr; }
    /**
     * Setter method for "onmouseover" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnmouseoverExpr(String onmouseoverExpr) { this.onmouseoverExpr = onmouseoverExpr; }
    /**
     * Setter method for "onmouseup" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOnmouseupExpr(String onmouseupExpr) { this.onmouseupExpr = onmouseupExpr; }
    /**
     * Setter method for "page" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setPageExpr(String pageExpr) { this.pageExpr = pageExpr; }
    /**
     * Setter method for "paramId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setParamIdExpr(String paramIdExpr) { this.paramIdExpr = paramIdExpr; }
    /**
     * Setter method for "paramName" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setParamNameExpr(String paramNameExpr) { this.paramNameExpr = paramNameExpr; }
    /**
     * Setter method for "paramProperty" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setParamPropertyExpr(String paramPropertyExpr) { this.paramPropertyExpr = paramPropertyExpr; }
    /**
     * Setter method for "paramScope" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setParamScopeExpr(String paramScopeExpr) { this.paramScopeExpr = paramScopeExpr; }
    /**
     * Setter method for "property" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setPropertyExpr(String propertyExpr) { this.propertyExpr = propertyExpr; }
    /**
     * Setter method for "scope" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setScopeExpr(String scopeExpr) { this.scopeExpr = scopeExpr; }
    /**
     * Setter method for "style" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setStyleExpr(String styleExpr) { this.styleExpr = styleExpr; }
    /**
     * Setter method for "styleClass" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setStyleClassExpr(String styleClassExpr) { this.styleClassExpr = styleClassExpr; }
    /**
     * Setter method for "styleId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setStyleIdExpr(String styleIdExpr) { this.styleIdExpr = styleIdExpr; }
    /**
     * Setter method for "tabindex" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setTabindexExpr(String tabindexExpr) { this.tabindexExpr = tabindexExpr; }
    /**
     * Setter method for "target" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setTargetExpr(String targetExpr) { this.targetExpr = targetExpr; }
    /**
     * Setter method for "title" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setTitleExpr(String titleExpr) { this.titleExpr = titleExpr; }
    /**
     * Setter method for "titleKey" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setTitleKeyExpr(String titleKeyExpr) { this.titleKeyExpr = titleKeyExpr; }
    /**
     * Setter method for "transaction" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setTransactionExpr(String transactionExpr) { this.transactionExpr = transactionExpr; }
    /**
     * Setter method for "useLocalEncoding" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setUseLocalEncodingExpr(String useLocalEncodingExpr) { this.useLocalEncodingExpr = useLocalEncodingExpr; }

    /**
     * Resets attribute values for tag reuse.
     */
    public void release()
    {
        super.release();
        setAccesskeyExpr(null);
        setActionExpr(null);
        setAnchorExpr(null);
        setForwardExpr(null);
        setHrefExpr(null);
        setIndexedExpr(null);
        setIndexIdExpr(null);
        setLinkNameExpr(null);
        setNameExpr(null);
        setOnblurExpr(null);
        setOnclickExpr(null);
        setOndblclickExpr(null);
        setOnfocusExpr(null);
        setOnkeydownExpr(null);
        setOnkeypressExpr(null);
        setOnkeyupExpr(null);
        setOnmousedownExpr(null);
        setOnmousemoveExpr(null);
        setOnmouseoutExpr(null);
        setOnmouseoverExpr(null);
        setOnmouseupExpr(null);
        setPageExpr(null);
        setParamIdExpr(null);
        setParamNameExpr(null);
        setParamPropertyExpr(null);
        setParamScopeExpr(null);
        setPropertyExpr(null);
        setScopeExpr(null);
        setStyleExpr(null);
        setStyleClassExpr(null);
        setStyleIdExpr(null);
        setTabindexExpr(null);
        setTargetExpr(null);
        setTitleExpr(null);
        setTitleKeyExpr(null);
        setTransactionExpr(null);
        setUseLocalEncodingExpr(null);
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
        String  string  = null;
        Boolean bool    = null;

        if ((string = EvalHelper.evalString("accessKey", getAccesskeyExpr(),
                                            this, pageContext)) != null)
            setAccesskey(string);

        if ((string = EvalHelper.evalString("action", getActionExpr(),
                                            this, pageContext)) != null)
            setAction(string);

        if ((string = EvalHelper.evalString("anchor", getAnchorExpr(),
                                            this, pageContext)) != null)
            setAnchor(string);

        if ((string = EvalHelper.evalString("forward", getForwardExpr(),
                                            this, pageContext)) != null)
            setForward(string);

        if ((string = EvalHelper.evalString("href", getHrefExpr(),
                                            this, pageContext)) != null)
            setHref(string);

        if ((bool = EvalHelper.evalBoolean("indexed", getIndexedExpr(),
                                           this, pageContext)) != null)
            setIndexed(bool.booleanValue());

        if ((string = EvalHelper.evalString("indexId", getIndexIdExpr(),
                                            this, pageContext)) != null)
            setIndexId(string);

        if ((string = EvalHelper.evalString("linkName", getLinkNameExpr(),
                                            this, pageContext)) != null)
            setLinkName(string);

        if ((string = EvalHelper.evalString("name", getNameExpr(),
                                            this, pageContext)) != null)
            setName(string);

        if ((string = EvalHelper.evalString("onblur", getOnblurExpr(),
                                            this, pageContext)) != null)
            setOnblur(string);

        if ((string = EvalHelper.evalString("onclick", getOnclickExpr(),
                                            this, pageContext)) != null)
            setOnclick(string);

        if ((string = EvalHelper.evalString("ondblclick", getOndblclickExpr(),
                                            this, pageContext)) != null)
            setOndblclick(string);

        if ((string = EvalHelper.evalString("onfocus", getOnfocusExpr(),
                                            this, pageContext)) != null)
            setOnfocus(string);

        if ((string = EvalHelper.evalString("onkeydown", getOnkeydownExpr(),
                                            this, pageContext)) != null)
            setOnkeydown(string);

        if ((string = EvalHelper.evalString("onkeypress", getOnkeypressExpr(),
                                            this, pageContext)) != null)
            setOnkeypress(string);

        if ((string = EvalHelper.evalString("onkeyup", getOnkeyupExpr(),
                                            this, pageContext)) != null)
            setOnkeyup(string);

        if ((string = EvalHelper.evalString("onmousedown", getOnmousedownExpr(),
                                            this, pageContext)) != null)
            setOnmousedown(string);

        if ((string = EvalHelper.evalString("onmousemove", getOnmousemoveExpr(),
                                            this, pageContext)) != null)
            setOnmousemove(string);

        if ((string = EvalHelper.evalString("onmouseout", getOnmouseoutExpr(),
                                            this, pageContext)) != null)
            setOnmouseout(string);

        if ((string = EvalHelper.evalString("onmouseover", getOnmouseoverExpr(),
                                            this, pageContext)) != null)
            setOnmouseover(string);

        if ((string = EvalHelper.evalString("onmouseup", getOnmouseupExpr(),
                                            this, pageContext)) != null)
            setOnmouseup(string);

        if ((string = EvalHelper.evalString("page", getPageExpr(),
                                            this, pageContext)) != null)
            setPage(string);

        if ((string = EvalHelper.evalString("paramId", getParamIdExpr(),
                                            this, pageContext)) != null)
            setParamId(string);

        if ((string = EvalHelper.evalString("paramName", getParamNameExpr(),
                                            this, pageContext)) != null)
            setParamName(string);

        if ((string = EvalHelper.evalString("paramProperty", getParamPropertyExpr(),
                                            this, pageContext)) != null)
            setParamProperty(string);

        if ((string = EvalHelper.evalString("paramScope", getParamScopeExpr(),
                                            this, pageContext)) != null)
            setParamScope(string);

        if ((string = EvalHelper.evalString("property", getPropertyExpr(),
                                            this, pageContext)) != null)
            setProperty(string);

        if ((string = EvalHelper.evalString("scope", getScopeExpr(),
                                            this, pageContext)) != null)
            setScope(string);

        if ((string = EvalHelper.evalString("style", getStyleExpr(),
                                            this, pageContext)) != null)
            setStyle(string);

        if ((string = EvalHelper.evalString("styleClass", getStyleClassExpr(),
                                            this, pageContext)) != null)
            setStyleClass(string);

        if ((string = EvalHelper.evalString("styleId", getStyleIdExpr(),
                                            this, pageContext)) != null)
            setStyleId(string);

        if ((string = EvalHelper.evalString("tabindex", getTabindexExpr(),
                                            this, pageContext)) != null)
            setTabindex(string);

        if ((string = EvalHelper.evalString("target", getTargetExpr(),
                                            this, pageContext)) != null)
            setTarget(string);

        if ((string = EvalHelper.evalString("title", getTitleExpr(),
                                            this, pageContext)) != null)
            setTitle(string);

        if ((string = EvalHelper.evalString("titleKey", getTitleKeyExpr(),
                                            this, pageContext)) != null)
            setTitleKey(string);

        if ((bool = EvalHelper.evalBoolean("transaction", getTransactionExpr(),
                                           this, pageContext)) != null)
            setTransaction(bool.booleanValue());

        if ((bool = EvalHelper.evalBoolean("useLocalEncoding", getUseLocalEncodingExpr(),
                                           this, pageContext)) != null)
            setUseLocalEncoding(bool.booleanValue());
    }
}
