/*
 * $Header: /tmp/cvs-vintage/struts/contrib/service-manager/services/factory/src/org/apache/struts/service/factory/Attic/SimpleFactory.java,v 1.1 2001/07/25 20:42:22 oalexeev Exp $
 * $Revision: 1.1 $
 * $Date: 2001/07/25 20:42:22 $
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

package org.apache.struts.service.factory;

import java.lang.Class;
import java.lang.Throwable;
import java.lang.reflect.Constructor;

/** Simple factory. Take parameters list and call
 *  appropriate constructor for such parameters and target type.
 *
 * @author Oleg V Alexeev
 * @version $Revision: 1.1 $ $Date: 2001/07/25 20:42:22 $
 */
public class SimpleFactory extends Factory {

        //------------------------------------------------ public methods

        /** Core factory method. This method will be called at work
         *  time to create appropriate bean of <code>type</code> using
         *  <code>parameters</code>
         *
         *  @param type - class name of target bean, to be created
         *  @param parameters - array of parameters to use it at creation process
         *  @exception Throwable if any error occured
         *
         *  @see org.apache.struts.factory.Parameter
         */
        public Object create( String type, Parameter[] parameters ) 
                throws Throwable {
                
                Object result = null;
                Class clazz = null;
                Class[] types = null;
                Object[] values = null;

                clazz = Class.forName( type );  

                if( parameters == null ) {
                      result = clazz.newInstance();
                } else {
                      if( parameters!=null && parameters.length > 0 ) {
                              types = new Class[ parameters.length ];
                              values = new Object[ parameters.length ];
                              ParameterMapping mapping = null;
                              for( int i = 0; i < parameters.length; i++ ) {
                                        mapping = parameters[ i ].getMapping();
                                        types[ i ] = Class.forName( mapping.getType() );
                                        values[ i ] = parameters[ i ].getValue();
                              }
                      }
                      Constructor constructor = clazz.getConstructor( types );
                      result = constructor.newInstance( values );
                }

                return result;
        }

}