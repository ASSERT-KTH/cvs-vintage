/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/util/Attic/StrutsValidatorUtil.java,v 1.6 2002/10/14 16:32:59 rleland Exp $
 * $Revision: 1.6 $
 * $Date: 2002/10/14 16:32:59 $
 *
 * ====================================================================
 *
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


package org.apache.struts.util;

import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.util.MessageResources;
import org.apache.commons.validator.Arg;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.Validator;
import org.apache.commons.validator.ValidatorAction;
import org.apache.commons.validator.ValidatorResources;
import org.apache.struts.validator.ValidatorPlugIn;
import org.apache.struts.config.ApplicationConfig;
import org.apache.struts.Globals;


/**
 * <p>This class helps provides some useful methods for retrieving objects
 * from different scopes of the application.</p>
 *
 * @author David Winterfeldt
 * @author Eddie Bush
 * @version $Revision: 1.6 $ $Date: 2002/10/14 16:32:59 $
 * @since Struts 1.1
*/
public class StrutsValidatorUtil  {

   /**
    * Resources key the <code>ServletContext</code> is stored under.
   */
   public static String SERVLET_CONTEXT_KEY = "javax.servlet.ServletContext";

   /**
    * Resources key the <code>HttpServletRequest</code> is stored under.
   */
   public static String HTTP_SERVLET_REQUEST_KEY = "javax.servlet.http.HttpServletRequest";

   /**
    * Resources key the <code>ActionErrors</code> is stored under.
   */
   public static String ACTION_ERRORS_KEY = "org.apache.struts.action.ActionErrors";

   private static Locale defaultLocale = Locale.getDefault();

   /**
    * Retrieve <code>ValidatorResources</code> for the module.
    *
    * @deprecated In Struts 1.1b3 This method can only return the resources for the default
    *  module.  Use getValidatorResources(HttpServletRequest, ServletContext)
    *  to get the resources for the current application module.
   */
   public static ValidatorResources getValidatorResources(ServletContext application) {
      return (ValidatorResources)application.getAttribute(ValidatorPlugIn.VALIDATOR_KEY);
   }

  /**
   * Retrieve <code>ValidatorResources</code> for the current module.
   */
   public static ValidatorResources getValidatorResources(ServletContext application,HttpServletRequest request) {
      return (ValidatorResources) application.getAttribute(ValidatorPlugIn.VALIDATOR_KEY +
                           ((ApplicationConfig)request.getAttribute(Globals.APPLICATION_KEY)).getPrefix());
   }

   /**
    * Retrieve <code>MessageResources</code> for the application module.
    *
    * @deprecated This method can only return the resources for the default
    *  module.  Use getMessageResources(HttpServletRequest) to get the
    *  resources for the current application module.
   */
   public static MessageResources getMessageResources(ServletContext application) {
      return (MessageResources)application.getAttribute(Action.MESSAGES_KEY);
   }

   /**
    * Retrieve <code>MessageResources</code> for the application module.
   */
   public static MessageResources getMessageResources(HttpServletRequest request) {
      return (MessageResources)request.getAttribute(Action.MESSAGES_KEY);
   }

   /**
    * Get the <code>Locale</code> of the current user.
   */
   public static Locale getLocale(HttpServletRequest request) {
      Locale locale = null;
      try {
          locale = (Locale) request.getSession().getAttribute(Action.LOCALE_KEY);
      } catch (IllegalStateException e) {   // Invalidated session
          locale = null;
      }
      if (locale == null) {
         locale = defaultLocale;
      }

      return locale;
   }

   /**
    * Gets the <code>Locale</code> sensitive value based on the key passed in.
   */
   public static String getMessage(MessageResources messages, Locale locale, String key) {
      String message = null;

      if (messages != null) {
         message = messages.getMessage(locale, key);
      }
      if (message == null) {
         message = "";
      }

      return message;
   }

   /**
    * Gets the <code>Locale</code> sensitive value based on the key passed in.
   */
   public static String getMessage(HttpServletRequest request, String key) {
      MessageResources messages = getMessageResources(request);

      return getMessage(messages, getLocale(request), key);
   }

   /**
    * Gets the locale sensitive message based on the <code>ValidatorAction</code> message and the
    * <code>Field</code>'s arg objects.
   */
   public static String getMessage(MessageResources messages, Locale locale,
                                   ValidatorAction va, Field field) {

      String arg[] = getArgs(va.getName(), messages, locale, field);
      String msg = (field.getMsg(va.getName()) != null ? field.getMsg(va.getName()) : va.getMsg());

      return messages.getMessage(locale, msg, arg[0], arg[1], arg[2], arg[3]);
   }

   /**
    * Gets the <code>ActionError</code> based on the <code>ValidatorAction</code> message and the
    * <code>Field</code>'s arg objects.
   */
   public static ActionError getActionError(HttpServletRequest request,
                                            ValidatorAction va, Field field) {

      String arg[] = getArgs(va.getName(), getMessageResources(request), getLocale(request), field);
      String msg = (field.getMsg(va.getName()) != null ? field.getMsg(va.getName()) : va.getMsg());

      return new ActionError(msg, arg[0], arg[1], arg[2], arg[3]);
   }

   /**
    * Gets the message arguments based on the current <code>ValidatorAction</code>
    * and <code>Field</code>.
   */
   public static String[] getArgs(String actionName, MessageResources messages,
                                  Locale locale, Field field) {

      Arg arg0 = field.getArg0(actionName);
      Arg arg1 = field.getArg1(actionName);
      Arg arg2 = field.getArg2(actionName);
      Arg arg3 = field.getArg3(actionName);

      String sArg0 = null;
      String sArg1 = null;
      String sArg2 = null;
      String sArg3 = null;

      if (arg0 != null) {
         if (arg0.getResource()) {
            sArg0 = getMessage(messages, locale, arg0.getKey());
         } else {
            sArg0 = arg0.getKey();
         }
      }

      if (arg1 != null) {
         if (arg1.getResource()) {
            sArg1 = getMessage(messages, locale, arg1.getKey());
         } else {
            sArg1 = arg1.getKey();
         }
      }

      if (arg2 != null) {
         if (arg2.getResource()) {
            sArg2 = getMessage(messages, locale, arg2.getKey());
         } else {
            sArg2 = arg2.getKey();
         }
      }

      if (arg3 != null) {
         if (arg3.getResource()) {
            sArg3 = getMessage(messages, locale, arg3.getKey());
         } else {
            sArg3 = arg3.getKey();
         }
      }

      return new String[] { sArg0, sArg1, sArg2, sArg3 };

   }

   /**
    * Initialize the <code>Validator</code> to perform validation.
    *
    * @param    key     The key that the validation rules are under
    *               (the form elements name attribute).
    * @param    request     The current request object.
    * @param    errors      The object any errors will be stored in.
   */
   public static Validator initValidator(String key, Object bean,
                                         ServletContext application, HttpServletRequest request,
                                         ActionErrors errors, int page) {

      ValidatorResources resources = StrutsValidatorUtil.getValidatorResources(application,request);
      Locale locale = StrutsValidatorUtil.getLocale(request);

      Validator validator = new Validator(resources, key);
      validator.setUseContextClassLoader(true);

      validator.setPage(page);

      validator.addResource(SERVLET_CONTEXT_KEY, application);
      validator.addResource(HTTP_SERVLET_REQUEST_KEY, request);
      validator.addResource(Validator.LOCALE_KEY, locale);
      validator.addResource(ACTION_ERRORS_KEY, errors);
      validator.addResource(Validator.BEAN_KEY, bean);

      return validator;
   }

}
