/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/logic/IterateTei.java,v 1.7 2004/01/10 21:03:32 dgraham Exp $
 * $Revision: 1.7 $
 * $Date: 2004/01/10 21:03:32 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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
package org.apache.struts.taglib.logic;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 * Implementation of <code>TagExtraInfo</code> for the <b>iterate</b>
 * tag, identifying the scripting object(s) to be made visible.
 *
 * @version $Revision: 1.7 $ $Date: 2004/01/10 21:03:32 $
 */
public class IterateTei extends TagExtraInfo {

  /**
   * Return information about the scripting variables to be created.
   */
  public VariableInfo[] getVariableInfo(TagData data) {

    // prime this array with the maximum potential variables.
    // will be arraycopy'd out to the final array based on results.
    VariableInfo[] variables = new VariableInfo[2];

    // counter for matched results.
    int counter = 0;

    /* id : object of the current iteration */
    String id = data.getAttributeString("id");
    String type = data.getAttributeString("type");
    if (id != null) {
      if (type == null) {
        type = "java.lang.Object";
      }
      variables[counter++] = new VariableInfo(data.getAttributeString("id"),
                                              type, true,
                                              VariableInfo.NESTED);
    }

    /* indexId : number value of the current iteration */
    String indexId = data.getAttributeString("indexId");
    if (indexId != null) {
      variables[counter++] = new VariableInfo(indexId, "java.lang.Integer",
                                              true, VariableInfo.NESTED);
    }

    /* create returning array, and copy results */
    VariableInfo[] result;
    if (counter > 0) {
      result = new VariableInfo[counter];
      System.arraycopy(variables, 0, result, 0, counter);
    } else {
      result = new VariableInfo[0];
    }
    return result;
  }
}
