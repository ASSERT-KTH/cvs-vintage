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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.struts.digester.Digester;
import org.xml.sax.SAXException;


/**
 * <p>Maps an xml file to <code>ValidatorResources</code>.</p>
 *
 * @author David Winterfeldt
*/
public class ValidatorResourcesInitializer {

   /**
    * Initializes a <code>ValidatorResources</code> based on a
    * file path.
    *
    * @param	fileName	The file path for the xml resource.
   */
   public static ValidatorResources initialize(String fileName)
      throws IOException { 
      
      return initialize(fileName, 0);
   }

   /**
    * Initializes a <code>ValidatorResources</code> based on an 
    * <code>InputStream</code>.
    *
    * @param	in		<code>InputStream</code> for the xml resource.
   */
   public static ValidatorResources initialize(InputStream in)
      throws IOException { 
      	
      return initialize(in, 0);
   	
   }

   /**
    * Initializes a <code>ValidatorResources</code> based on a
    * file path and the debug level.
    *
    * @param	fileName	The file path for the xml resource.
    * @param	debug		The debug level
   */
   public static ValidatorResources initialize(String fileName, int debug)
      throws IOException { 
      
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName));      
      
      return initialize(new DefaultValidatorLog(), in, debug);
   }

   /**
    * Initializes a <code>ValidatorResources</code> based on an 
    * <code>InputStream</code> and the debug level.
    *
    * @param	in		<code>InputStream</code> for the xml resource.
    * @param	debug		The debug level
   */
   public static ValidatorResources initialize(InputStream in, int debug)
      throws IOException { 
      	
      return initialize(new DefaultValidatorLog(), in, debug);
   	
   }

   /**
    * Initializes a <code>ValidatorResources</code> based on an 
    * <code>ValidatorLog</code>, <code>InputStream</code>, and the debug level.
    *
    * @param	logger		Used for logging.
    * @param	in		<code>InputStream</code> for the xml resource.
    * @param	debug		The debug level
   */   
   public static ValidatorResources initialize(ValidatorLog logger, InputStream in, int debug)
      throws IOException { 
       	
      logger.setDebug(debug);
      
      ValidatorResources resources = new ValidatorResources(logger);
         
      Digester digester = new Digester();
      digester.push(resources);
      digester.setValidating(false);
      // Create Global Constant objects
      digester.addObjectCreate("form-validation/global/constant",
      			 "com.wintecinc.struts.validation.Constant", "className");
      digester.addSetProperties("form-validation/global/constant");
      digester.addSetNext("form-validation/global/constant", "addConstant",
      		    "com.wintecinc.struts.validation.Constant");
      // Create Global ValidatorAction objects
      digester.addObjectCreate("form-validation/global/validator",
      			 "com.wintecinc.struts.validation.ValidatorAction", "className");
      digester.addSetProperties("form-validation/global/validator");
      digester.addSetNext("form-validation/global/validator", "addValidatorAction",
      		    "com.wintecinc.struts.validation.ValidatorAction");
      
      // Add the body of a javascript element to the Validatoraction
      digester.addCallMethod("form-validation/global/validator",
                             "setJavascript", 1);
      digester.addCallParam("form-validation/global/validator/javascript", 0);      		    
      		    
      		    
      // Create FormSet objects
      digester.addObjectCreate("form-validation/formset", "com.wintecinc.struts.validation.FormSet",
      			 "className");
      digester.addSetProperties("form-validation/formset");
      digester.addSetNext("form-validation/formset", "put",
      		    "com.wintecinc.struts.validation.FormSet");
      // Create Constant objects
      digester.addObjectCreate("form-validation/formset/constant",
      			 "com.wintecinc.struts.validation.Constant", "className");
      digester.addSetProperties("form-validation/formset/constant");
      digester.addSetNext("form-validation/formset/constant", "addConstant",
      		    "com.wintecinc.struts.validation.Constant");
      // Create Form objects
      digester.addObjectCreate("form-validation/formset/form",
      			 "com.wintecinc.struts.validation.Form", "className");
      digester.addSetProperties("form-validation/formset/form");
      digester.addSetNext("form-validation/formset/form", "addForm",
      		    "com.wintecinc.struts.validation.Form");
      // Create Field objects
      digester.addObjectCreate("form-validation/formset/form/field",
      			 "com.wintecinc.struts.validation.Field", "className");
      digester.addSetProperties("form-validation/formset/form/field");
      digester.addSetNext("form-validation/formset/form/field", "addField",
      		    "com.wintecinc.struts.validation.Field");

      // Create Var objects
      digester.addCallMethod("form-validation/formset/form/field/var",
                             "addVarParam", 3);
      digester.addCallParam("form-validation/formset/form/field/var/var-name", 0);
      digester.addCallParam("form-validation/formset/form/field/var/var-value", 1);
      digester.addCallParam("form-validation/formset/form/field/var/var-jstype", 2);
        
      // Create Msg object
      digester.addObjectCreate("form-validation/formset/form/field/msg",
      			 "com.wintecinc.struts.validation.Msg", "className");
      digester.addSetProperties("form-validation/formset/form/field/msg");
      digester.addSetNext("form-validation/formset/form/field/msg", "addMsg",
      		    "com.wintecinc.struts.validation.Msg");
      		    
      // Create Arg objects
      // Arg0
      digester.addObjectCreate("form-validation/formset/form/field/arg0",
      			 "com.wintecinc.struts.validation.Arg", "className");
      digester.addSetProperties("form-validation/formset/form/field/arg0");
      digester.addSetNext("form-validation/formset/form/field/arg0", "addArg0",
      		    "com.wintecinc.struts.validation.Arg");
      
      // Arg1
      digester.addObjectCreate("form-validation/formset/form/field/arg1",
      			 "com.wintecinc.struts.validation.Arg", "className");
      digester.addSetProperties("form-validation/formset/form/field/arg1");
      digester.addSetNext("form-validation/formset/form/field/arg1", "addArg1",
      		    "com.wintecinc.struts.validation.Arg");
      
      // Arg2
      digester.addObjectCreate("form-validation/formset/form/field/arg2",
      			 "com.wintecinc.struts.validation.Arg", "className");
      digester.addSetProperties("form-validation/formset/form/field/arg2");
      digester.addSetNext("form-validation/formset/form/field/arg2", "addArg2",
      		    "com.wintecinc.struts.validation.Arg");
      
      // Arg3
      digester.addObjectCreate("form-validation/formset/form/field/arg3",
      			 "com.wintecinc.struts.validation.Arg", "className");
      digester.addSetProperties("form-validation/formset/form/field/arg3");
      digester.addSetNext("form-validation/formset/form/field/arg3", "addArg3",
      		    "com.wintecinc.struts.validation.Arg");
      
      try {
         // Parse the input stream to initialize our database
         digester.parse(in);
         in.close();
      } catch (SAXException e) {
          logger.log("ValidatorResourcesInitializer::initialize - SAXException: " + e.getMessage(), e);
      } finally {
      	 if (in != null)
            try {in.close(); } catch (Exception e) {}
      }
      
      resources.process();
      
      return resources;

   }
   
}