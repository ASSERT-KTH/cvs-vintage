/*
 * @(#)BodyTag.java	1.11 99/10/07
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
 * The BodyTag interface extends Tag by defining additional methods that let
 * a Tag handler access its body.
 * <p>
 * The interface provides two new methods: one is to be invoked with the BodyContent
 * for the evaluation of the body, the other is to be reevaluated after every body
 * evaluation.
 * <p>
 * Without repeating the portions described in Tag.java, a typical invocation sequence is:
 *
 * <pre>
 * <code>
 *
 * -- we are picking up after all the setters have been done
 * t.doStartTag();
 * out = pageContext.pushBody();
 * -- prepare for body
 * t.setBodyContent(out);
 * -- preamble
 * t.doBodyInit();
 * -- BODY evaluation into out
 * t.doAfterBody();
 * -- while doAfterBody returns EVAL_BODY_TAG we iterate
 * -- BODY evaluation into out
 * t.doAfterBody()
 * -- done
 * t.doEndTag()
 *
 * </code>
 * </pre>
 */

public interface BodyTag extends Tag {

    /**
     * Request the creation of new BodyContent on which to evaluate the
     * body of this tag.
     * Returned from doStartTag and doAfterBody.
     * This is an illegal return value for doStartTag when the class does not
     * implement BodyTag, since BodyTag is needed to manipulate the new Writer.
     */
 
    public final static int EVAL_BODY_TAG = 2;

    /**
     * Setter method for the bodyContent property.
     * <p>
     * This method will not be invoked if there is no body evaluation.
     *
     * @param b the BodyContent
     * @seealso #doInitBody
     * @seealso #doAfterBody
     */

    void setBodyContent(BodyContent b);

    /**
     * Prepare for evaluation of the body.
     * <p>
     * The method will be invoked once per action invocation by the page implementation
     * after a new BodyContent has been obtained and set on the tag handler
     * via the setBodyContent() method and before the evaluation
     * of the tag's body into that BodyContent.
     * <p>
     * This method will not be invoked if there is no body evaluation.
     *
     * @seealso #doAfterBody
     */

    void doInitBody() throws JspError;

    /**
     * Actions after some body has been evaluated.
     * <p>
     * Not invoked in empty tags or in tags returning SKIP_BODY in doStartTag()
     * This method is invoked after every body evaluation.
     * The pair "BODY -- doAfterBody()" is invoked initially if doStartTag()
     * returned EVAL_BODY_TAG, and it is repeated as long
     * as the doAfterBody() evaluation returns EVAL_BODY_TAG
     * <p>
     * The method re-invocations may be lead to different actions because
     * there might have been some changes to shared state, or because
     * of external computation.
     *
     * @returns whether additional evaluations of the body are desired
     * @seealso #doInitBody
     */

    int doAfterBody() throws JspError;
}
