/*
 * @(#)Tag.java	1.35 99/10/11
 * 
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * CopyrightVersion 1.0
 */
 
package javax.servlet.jsp.tagext;

import javax.servlet.jsp.*;


/**
 * The Tag interface defines the basic protocol between a Tag handler and JSP page
 * implementation class.  It defines the life cycle and the methods to be invoked at
 * start and end tag.
 * <p>
 * There are several methods that get invoked to set the state of a Tag handler.
 * The Tag handler is required to keep this state so the page compiler can
 * choose not to reinvoke some of the state setting.
 * <p>
 * The page compiler guarantees that setPageContext and setParent
 * will all be invoked on the Tag handler, in that order, before doStartTag() or
 * doEndTag() are invoked on it.
 * The page compiler also guarantees that release will be invoked on the Tag
 * handler before the end of the page.
 * <p>
 * Here is a typical invocation sequence:
 *
 * <pre>
 * <code>
 * 
 * ATag t = new ATag();
 *
 * -- need to set required information 
 * t.setPageContext(...);
 * t.setParent(...);
 * t.setAttribute1(value1);
 * t.setAttribute2(value2);
 *
 * -- all ready to go
 * t.doStartTag();
 * t.doEndTag();
 * 
 * ... other tags and template text
 *
 * -- say one attribute is changed, but parent and pageContext have not changed
 * t.setAttribute2(value3);
 * t.doStartTag()
 * t.doEndTag()
 *
 * ... other tags and template text
 *
 * -- assume that this new action happens to use the same attribute values
 * -- it is legal to reuse the same handler instance,  with no changes...
 * t.doStartTag();
 * t.doEndTag();
 *
 * -- OK, all done
 * t.release()
 * </code>
 * </pre>
 *
 * <p>
 * The Tag interface also includes methods to set a parent chain, which is used
 * to find enclosing tag handlers.
 *
 */

public interface Tag {

    /**
     * Skip body evaluation.
     * Valid return value for doStartTag and doAfterBody.
     */
 
    public final static int SKIP_BODY = 0;
 
    /**
     * Evaluate body into existing out stream.
     * Valid return value for doStartTag.
     * This is an illegal return value for doStartTag when the class implements
     * BodyTag, since BodyTag implies the creation of a new BodyContent.
     */
 
    public final static int EVAL_BODY_INCLUDE = 1;

    /**
     * Skip the rest of the page.
     * Valid return value for doEndTag.
     */

    public final static int SKIP_PAGE = 5;

    /**
     * Continue evaluating the page.
     * Valid return value for doEndTag().
     */

    public final static int EVAL_PAGE = 6;

    // Setters for Tag handler data

    /**
     * Set the current page context.
     * Called by the page implementation prior to doStartTag().
     * <p>
     * This value is *not* reset by doEndTag() and must be explicitly reset
     * by a page implementation
     */

    void setPageContext(PageContext pc);

    /**
     * Set the current nesting Tag of this Tag.
     * Called by the page implementation prior to doStartTag().
     * <p>
     * This value is *not* reset by doEndTag() and must be explicitly reset
     * by a page implementation.  Code can assume that setPageContext
     * has been called with the proper values before this point.
     */

    void setParent(Tag t);

    /**
     * @return the current parent
     * @seealso TagSupport.findAncestorWithClass().
     */

    Tag getParent();


    // Actions for basic start/end processing.

    /**
     * Process the start tag for this instance.
     *
     * @returns EVAL_BODY_INCLUDE if the tag wants to process body, SKIP_BODY if it
     * does not want to process it.
     *
     * When a Tag returns EVAL_BODY_INCLUDE the body (if any) is evaluated
     * and written into the current "out" JspWriter then doEndTag() is invoked.
     *
     * @see BodyTag
     */
 
    int doStartTag() throws JspException;
 

    /**
     * Process the end tag. This method will be called on all Tag objects.
     */

    int doEndTag() throws JspException;

    /**
     * Called on a Tag handler to release state.
     * The page compiler guarantees this method will be called on all tag handlers,
     * but there may be multiple invocations on doStartTag and doEndTag in between.
     */

    void release();

}
