/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/tiles/UseAttributeTag.java,v 1.10 2003/09/07 03:18:45 dmkarr Exp $
 * $Revision: 1.10 $
 * $Date: 2003/09/07 03:18:45 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
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


package org.apache.struts.taglib.tiles;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.struts.taglib.tiles.util.TagUtils;
import org.apache.struts.tiles.ComponentContext;


/**
 * Custom tag exposing a component attribute to page.
 *
 */
public class UseAttributeTag extends TagSupport {


    // ----------------------------------------------------- Instance Variables


    /**
     * Class name of object.
     */
    private String  classname = null;


    /**
     * The scope name.
     */
    private String scopeName = null;

    /**
     * The scope value.
     */
    private int scope = PageContext.PAGE_SCOPE;



    /**
     * The attribute name to be exposed.
     */
    private String attributeName = null;

    /**
     * Are errors ignored. This is the property for attribute 'ignore'.
     * Default value is <code>false</code>, which throws an exception.
     * Only "attribute not found" - errors are ignored.
     */
  protected boolean isErrorIgnored = false;


    // ------------------------------------------------------------- Properties


    /**
     * Release all allocated resources.
     */
    public void release() {

        super.release();
        attributeName = null;
        classname = null;
        scope = PageContext.PAGE_SCOPE;
        scopeName = null;
        isErrorIgnored = false;
          // Parent doesn't clear id, so we do it
          // bug reported by Heath Chiavettone on 18 Mar 2002
        id = null;
    }

    /**
     * Get class name.
     */
    public String getClassname() {

  return (this.classname);

    }


    /**
     * Set the class name.
     *
     * @param name The new class name.
     */
    public void setClassname(String name) {

  this.classname = name;

    }

    /**
     * Set name.
     */
  public void setName(String value){
    this.attributeName = value;
  }

    /**
     * Get name.
     */
  public String getName()
  {
  return attributeName;
  }

    /**
     * Set the scope.
     *
     * @param scope The new scope.
     */
    public void setScope(String scope) {
  this.scopeName = scope;
    }

    /**
     * Get scope.
     */
  public String getScope()
  {
  return scopeName;
  }

    /**
     * Set ignore.
     */
  public void setIgnore(boolean ignore)
    {
    this.isErrorIgnored = ignore;
    }

    /**
     * Get ignore.
     */
  public boolean getIgnore()
  {
  return isErrorIgnored;
  }

    // --------------------------------------------------------- Public Methods


    /**
     * Expose the requested attribute from component context.
     *
     * @exception JspException if a JSP exception has occurred
     */
  public int doStartTag() throws JspException
    {
      // Do a local copy of id
    String localId=this.id;
    if( localId==null )
      localId=attributeName;

    ComponentContext compContext = (ComponentContext)pageContext.getAttribute( ComponentConstants.COMPONENT_CONTEXT, PageContext.REQUEST_SCOPE);
    if( compContext == null )
      throw new JspException ( "Error - tag useAttribute : no tiles context found." );

    Object value = compContext.getAttribute(attributeName);
        // Check if value exists and if we must send a runtime exception
    if( value == null )
      if(!isErrorIgnored)
        throw new JspException ( "Error - tag useAttribute : attribute '"+ attributeName + "' not found in context. Check tag syntax" );
       else
        return SKIP_BODY;

    if( scopeName != null )
      {
      scope = TagUtils.getScope( scopeName, PageContext.PAGE_SCOPE );
      if(scope!=ComponentConstants.COMPONENT_SCOPE)
        pageContext.setAttribute(localId, value, scope);
      }
     else
      pageContext.setAttribute(localId, value);

      // Continue processing this page
    return SKIP_BODY;
    }




    /**
     * Clean up after processing this enumeration.
     *
     * @exception JspException if a JSP exception has occurred
     */
  public int doEndTag() throws JspException
    {
    return (EVAL_PAGE);
    }

}
