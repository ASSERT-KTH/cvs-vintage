/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 */
 
 
package com.wintecinc.struts.validation;


/**
 * <p>Used in the Validation framework for creating a constant.  A constant can be 
 * used to define a global or locale level constant that can be used to replace 
 * some values in the <code>Field</code> class.<BR>
 * &nbsp;&nbsp;&nbsp;ex: &lt;constant name="zip" value="^\d{5}$" /&gt;<BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;mask="${zip}" </p>
 *
 * <ul><li>See /WEB-INF/validation.xml for validation rules.</li></ul>
 *
 * @author David Winterfeldt
*/
public class Constant implements java.io.Serializable {
    /**
     * The name of the constant.
    */
    private String name = null;

    /**
     * The name of the constant.
    */
    private String value = null;

    /**
     * Gets the name of the constant.
    */
    public String getName() {
       return name;	
    }

    /**
     * Sets the name of the constant.
    */
    public void setName(String name) {
       this.name = name;	
    }

    /**
     * Gets the value of the constant.
    */
    public String getValue() {
       return value;	
    }

    /**
     * Sets the value of the constant.
    */
    public void setValue(String value) {
       this.value = value;	
    }
       
    public String toString() {
       StringBuffer results = new StringBuffer();
    	
       results.append("Constant: name=");
       results.append(name);
       results.append("  value=");
       results.append(value);
       results.append("\n");
       
       return results.toString();
    }
    
}