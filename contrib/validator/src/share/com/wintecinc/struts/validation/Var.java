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
 * <p>Used in the Validation framework for creating a variable.  A variable 
 * can be used to pass in values on a field level for a validation method.</p>
 *
 * <ul><li>See /WEB-INF/validation.xml for validation rules.</li></ul>
 *
 * @author David Winterfeldt
*/
public class Var implements Cloneable, java.io.Serializable {

    /**
     * Int Constant for JavaScript type.  This is used when generating the JavaScript objects 
     * for use in client side validation.
    */
    public final static String JSTYPE_INT = "int";

    /**
     * String Constant for JavaScript type.  This is used when generating the JavaScript objects 
     * for use in client side validation.
    */
    public final static String JSTYPE_STRING = "string";

    /**
     * Regular Expression Constant for JavaScript type.  This is used when generating the JavaScript objects 
     * for use in client side validation.
    */
    public final static String JSTYPE_REGEXP = "regexp";
    
    /**
     * The name of the variable.
    */
    private String name = null;

    /**
     * The name of the value.
    */
    private String value = null;

    /**
     * The JavaScript type of the variable.
    */
    private String jsType = null;
    
    public Var() {}
    
    public Var(String name, String value, String jsType) {
       this.name = name;
       this.value = value;
       this.jsType = jsType;	
    }
    
    /**
     * Gets the name of the variable.
    */
    public String getName() {
       return name;	
    }

    /**
     * Sets the name of the variable.
    */
    public void setName(String name) {
       this.name = name;	
    }

    /**
     * Gets the value of the variable.
    */
    public String getValue() {
       return value;	
    }

    /**
     * Sets the value of the variable.
    */
    public void setValue(String value) {
       this.value = value;	
    }

    /**
     * Gets the JavaScript type of the variable.
    */
    public String getJsType() {
       return jsType;	
    }

    /**
     * Sets the JavaScript type of the variable.
    */
    public void setJsType(String jsType) {
       this.jsType = jsType;	
    }

    public Object clone() {
       try {
           Var var = (Var)super.clone();
           
           if (name != null)
              var.name = new String(name);
           
           if (value != null)
              var.value = new String(value);
           
           if (jsType != null)
              var.jsType = new String(jsType);

           return var;
       } catch (CloneNotSupportedException e) {
          throw new InternalError(e.toString());
       }
    }  
           
    public String toString() {
    	String sReturn = "Var: name=" + name + 
    			 "  value=" + value + 
    			 "  jsType=" + jsType + "\n";
    	return sReturn;
    }
    
}