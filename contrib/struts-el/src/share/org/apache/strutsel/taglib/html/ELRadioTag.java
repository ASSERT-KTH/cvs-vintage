/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/share/org/apache/strutsel/taglib/html/ELRadioTag.java,v 1.2 2002/09/28 04:43:04 dmkarr Exp $
 * $Revision: 1.2 $
 * $Date: 2002/09/28 04:43:04 $
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

import org.apache.struts.taglib.html.RadioTag;
import javax.servlet.jsp.JspException;
import org.apache.taglibs.standard.tag.el.core.ExpressionUtil;
import org.apache.taglibs.standard.tag.common.core.NullAttributeException;

public class ELRadioTag extends RadioTag {

    public int doStartTag() throws JspException {
        evaluateExpressions();
        return(super.doStartTag());
    }
    
    private Object   evalAttr(String   attrName,
                              String   attrValue,
                              Class    attrType)
        throws JspException, NullAttributeException
    {
        return (ExpressionUtil.evalNotNull("radio", attrName, attrValue,
                                           attrType, this, pageContext));
    }
    
    private void evaluateExpressions() throws JspException {
        try {
            setAccesskey((String) evalAttr("accessKey", getAccesskey(), String.class));
        } catch (NullAttributeException ex) {
            setAccesskey(null);
        }

        try {
            setAlt((String) evalAttr("alt", getAlt(), String.class));
        } catch (NullAttributeException ex) {
            setAlt(null);
        }

        try {
            setAltKey((String) evalAttr("altKey", getAltKey(), String.class));
        } catch (NullAttributeException ex) {
            setAltKey(null);
        }

        try {
            setDisabled(((Boolean) evalAttr("disabled", getDisabled() + "", Boolean.class)).
                        booleanValue());
        } catch (NullAttributeException ex) {
            setDisabled(false);
        }

        try {
            setIdName((String) evalAttr("idName", getIdName(), String.class));
        } catch (NullAttributeException ex) {
            setIdName(null);
        }

        try {
            setIndexed(((Boolean) evalAttr("indexed", getIndexed() + "", Boolean.class)).
                       booleanValue());
        } catch (NullAttributeException ex) {
            setIndexed(false);
        }

        try {
            setName((String) evalAttr("name", getName(), String.class));
        } catch (NullAttributeException ex) {
            setName(null);
        }

        try {
            setOnblur((String) evalAttr("onblur", getOnblur(), String.class));
        } catch (NullAttributeException ex) {
            setOnblur(null);
        }

        try {
            setOnchange((String) evalAttr("onchange", getOnchange(), String.class));
        } catch (NullAttributeException ex) {
            setOnchange(null);
        }

        try {
            setOnclick((String) evalAttr("onclick", getOnclick(), String.class));
        } catch (NullAttributeException ex) {
            setOnclick(null);
        }

        try {
            setOndblclick((String) evalAttr("ondblclick", getOndblclick(), String.class));
        } catch (NullAttributeException ex) {
            setOndblclick(null);
        }

        try {
            setOnfocus((String) evalAttr("onfocus", getOnfocus(), String.class));
        } catch (NullAttributeException ex) {
            setOnfocus(null);
        }

        try {
            setOnkeydown((String) evalAttr("onkeydown", getOnkeydown(), String.class));
        } catch (NullAttributeException ex) {
            setOnkeydown(null);
        }

        try {
            setOnkeypress((String) evalAttr("onkeypress", getOnkeypress(), String.class));
        } catch (NullAttributeException ex) {
            setOnkeypress(null);
        }

        try {
            setOnkeyup((String) evalAttr("onkeyup", getOnkeyup(), String.class));
        } catch (NullAttributeException ex) {
            setOnkeyup(null);
        }

        try {
            setOnmousedown((String) evalAttr("onmousedown", getOnmousedown(), String.class));
        } catch (NullAttributeException ex) {
            setOnmousedown(null);
        }

//         try {
//             setOnmousemove((String) evalAttr("onmousemove", getOnmousemove(), 
//                                              String.class));
//         } catch (NullAttributeException ex) {
//             setOnmousemove(null);
//         }

//         try {
//             setOnmouseout((String) evalAttr("onmouseout", getOnmouseout(), String.class));
//         } catch (NullAttributeException ex) {
//             setOnmouseout(null);
//         }

//         try {
//             setOnmouseover((String) evalAttr("onmouseover", getOnmouseover(), 
//                                              String.class));
//         } catch (NullAttributeException ex) {
//             setOnmouseover(null);
//         }

//         try {
//             setOnmouseup((String) evalAttr("onmouseup", getOnmouseup(), String.class));
//         } catch (NullAttributeException ex) {
//             setOnmouseup(null);
//         }

        try {
            setProperty((String) evalAttr("property", getProperty(), String.class));
        } catch (NullAttributeException ex) {
            setProperty(null);
        }

        try {
            setStyle((String) evalAttr("style", getStyle(), String.class));
        } catch (NullAttributeException ex) {
            setStyle(null);
        }

        try {
            setStyleClass((String) evalAttr("styleClass", getStyleClass(), String.class));
        } catch (NullAttributeException ex) {
            setStyleClass(null);
        }

        try {
            setStyleId((String) evalAttr("styleId", getStyleId(), String.class));
        } catch (NullAttributeException ex) {
            setStyleId(null);
        }

        try {
            setTabindex((String) evalAttr("tabindex", getTabindex(), String.class));
        } catch (NullAttributeException ex) {
            setTabindex(null);
        }

        try {
            setTitle((String) evalAttr("title", getTitle(), String.class));
        } catch (NullAttributeException ex) {
            setTitle(null);
        }

        try {
            setTitleKey((String) evalAttr("titleKey", getTitleKey(), String.class));
        } catch (NullAttributeException ex) {
            setTitleKey(null);
        }

        try {
            setValue((String) evalAttr("value", getValue(), String.class));
        } catch (NullAttributeException ex) {
            setValue(null);
        }
    }
}
