/*
 * @(#)VariableInfo.java	1.5 99/10/05
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

/**
 * Information on the scripting variables that are created/modified by
 * a tag (at run-time); this information is provided by TagExtraInfo
 * classes and it is used by the translation phase of JSP.
 *
 */

public class VariableInfo {
    /**
     * Different types of scope for an scripting variable introduced
     * by this action
     *
     * <pre>
     * NESTED ==> variable is visible only within the start/end tags
     * AT_BEGIN ==> variable is visible after start tag
     * AT_END ==> variable is visible after end tag
     * </pre>
     */
    public static final int NESTED = 0;
    public static final int AT_BEGIN = 1;
    public static final int AT_END = 2;


    /**
     * Constructor
     * These objects can be created (at translation time) by the TagExtraInfo
     * instances.
     *
     * @param id The name of the scripting variable
     * @param className The name of the scripting variable
     * @param declare If true, it is a new variable (in some languages this will
     * require a declaration)
     * @param scope Indication on the lexical scope of the variable
     */

    public VariableInfo(String varName,
			String className,
			boolean declare,
			int scope) {
	this.varName = varName;
	this.className = className;
	this.declare = declare;
	this.scope = scope;
    }

    // Accessor methods
    public String getVarName() { return varName; }
    public String getClassName() { return className; }
    public boolean getDeclare() { return declare; }
    public int getScope() { return scope; }



    // == private data
    private String varName;
    private String className;
    private boolean declare;
    private int scope;
}

