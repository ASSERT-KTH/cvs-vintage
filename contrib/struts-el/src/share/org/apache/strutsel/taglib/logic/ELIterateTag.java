/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/share/org/apache/strutsel/taglib/logic/ELIterateTag.java,v 1.5 2003/02/26 06:12:25 dmkarr Exp $
 * $Revision: 1.5 $
 * $Date: 2003/02/26 06:12:25 $
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

package org.apache.strutsel.taglib.logic;

import org.apache.struts.taglib.logic.IterateTag;
import javax.servlet.jsp.JspException;
import org.apache.strutsel.taglib.utils.EvalHelper;
import org.apache.taglibs.standard.tag.common.core.NullAttributeException;

/**
 * Custom tag that iterates the elements of a collection, which can be
 * either an attribute or the property of an attribute.  The collection
 * can be any of the following:  an array of objects, an Enumeration,
 * an Iterator, a Collection (which includes Lists, Sets and Vectors),
 * or a Map (which includes Hashtables) whose elements will be iterated over.
 *<p>
 * This class is a subclass of the class
 * <code>org.apache.struts.taglib.logic.IterateTag</code> which provides most of
 * the described functionality.  This subclass allows all attribute values to
 * be specified as expressions utilizing the JavaServer Pages Standard Library
 * expression language.
 *
 * @author David M. Karr
 * @version $Revision: 1.5 $
 */
public class ELIterateTag extends IterateTag {

    /**
     * Instance variable mapped to "collection" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String collectionExpr;
    /**
     * Instance variable mapped to "id" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String idExpr;
    /**
     * Instance variable mapped to "indexId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String indexIdExpr;
    /**
     * Instance variable mapped to "length" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String lengthExpr;
    /**
     * Instance variable mapped to "name" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String nameExpr;
    /**
     * Instance variable mapped to "offset" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String offsetExpr;
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
     * Instance variable mapped to "type" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String typeExpr;

    /**
     * Getter method for "collection" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getCollectionExpr() { return (collectionExpr); }
    /**
     * Getter method for "id" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getIdExpr() { return (idExpr); }
    /**
     * Getter method for "indexId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getIndexIdExpr() { return (indexIdExpr); }
    /**
     * Getter method for "length" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getLengthExpr() { return (lengthExpr); }
    /**
     * Getter method for "name" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getNameExpr() { return (nameExpr); }
    /**
     * Getter method for "offset" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getOffsetExpr() { return (offsetExpr); }
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
     * Getter method for "type" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getTypeExpr() { return (typeExpr); }

    /**
     * Setter method for "collection" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setCollectionExpr(String collectionExpr) { this.collectionExpr = collectionExpr; }
    /**
     * Setter method for "id" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setIdExpr(String idExpr) { this.idExpr = idExpr; }
    /**
     * Setter method for "indexId" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setIndexIdExpr(String indexIdExpr) { this.indexIdExpr = indexIdExpr; }
    /**
     * Setter method for "length" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setLengthExpr(String lengthExpr) { this.lengthExpr = lengthExpr; }
    /**
     * Setter method for "name" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setNameExpr(String nameExpr) { this.nameExpr = nameExpr; }
    /**
     * Setter method for "offset" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setOffsetExpr(String offsetExpr) { this.offsetExpr = offsetExpr; }
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
     * Setter method for "type" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setTypeExpr(String typeExpr) { this.typeExpr = typeExpr; }

    /**
     * Releases state of custom tag so this instance can be reused.
     */
    public void release()
    {
        super.release();
        setCollectionExpr(null);
        setIdExpr(null);
        setIndexIdExpr(null);
        setLengthExpr(null);
        setNameExpr(null);
        setOffsetExpr(null);
        setPropertyExpr(null);
        setScopeExpr(null);
        setTypeExpr(null);
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
     * Evaluates and returns a single attribute value, given the attribute
     * name, attribute value, and attribute type.  It uses
     * <code>ExpressionUtil.evalNotNull</code> to do the actual evaluation, and
     * it passes to this the name of the current tag, the <code>this</code>
     * pointer, and the current pageContext.
     *
     * @param attrName attribute name being evaluated
     * @param attrValue String value of attribute to be evaluated using EL
     * @param attrType Required resulting type of attribute value
     * @return Resulting attribute value
     */
    private Object   evalAttr(String   attrName,
                              String   attrValue,
                              Class    attrType)
        throws JspException, NullAttributeException
    {
        return (EvalHelper.eval("iterate", attrName, attrValue,
                                attrType, this, pageContext));
    }
    
    /**
     * Processes all attribute values which use the JSTL expression evaluation
     * engine to determine their values.  If any evaluation fails with a
     * <code>NullAttributeException</code> it will just use <code>null</code>
     * as the value.
     *
     * @exception JspException if a JSP exception has occurred
     */
    private void evaluateExpressions() throws JspException {

        try {
            setCollection(evalAttr("collection", getCollectionExpr(), 
                                   Object.class));
        } catch (NullAttributeException ex) {
        }

        try {
            setId((String) evalAttr("id", getIdExpr(), String.class));
        } catch (NullAttributeException ex) {
        }

        try {
            setIndexId((String) evalAttr("indexId", getIndexIdExpr(),
                                         String.class));
        } catch (NullAttributeException ex) {
        }

        try {
            setLength((String) evalAttr("length", getLengthExpr(), String.class));
        } catch (NullAttributeException ex) {
        }

        try {
            setName((String) evalAttr("name", getNameExpr(), String.class));
        } catch (NullAttributeException ex) {
        }

        try {
            setOffset((String) evalAttr("offset", getOffsetExpr(), String.class));
        } catch (NullAttributeException ex) {
        }

        try {
            setProperty((String) evalAttr("property", getPropertyExpr(),
                                          String.class));
        } catch (NullAttributeException ex) {
        }

        try {
            setScope((String) evalAttr("scope", getScopeExpr(), String.class));
        } catch (NullAttributeException ex) {
        }

        try {
            setType((String) evalAttr("type", getTypeExpr(), String.class));
        } catch (NullAttributeException ex) {
        }
    }
}
