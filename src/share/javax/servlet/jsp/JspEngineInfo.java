/*
 * @(#)JspEngineInfo.java	1.1 99/05/28
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
 
package javax.servlet.jsp;

/**
 * <p>
 * The JspEngineInfo is an abstract class that provides information on the
 * current JSP engine
 * </p>
 */

public abstract class JspEngineInfo {
    
    /**
     * <p>
     * Specification version numbers use a "Dewey Decimal"
     * syntax that consists of positive decimal integers
     * separated by periods ".", for example, "2.0" or "1.2.3.4.5.6.7".
     * This allows an extensible number to be used to
     * represent major, minor, micro, etc versions.
     * The version number must begin with a number.
     * </p>
     *
     * @return the specification version, nullis returned if it is not known
     */

    public abstract String getSpecificationVersion();
}
