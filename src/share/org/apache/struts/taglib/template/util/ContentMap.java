/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/template/util/Attic/ContentMap.java,v 1.5 2002/10/25 23:54:53 dgraham Exp $
 * $Revision: 1.5 $
 * $Date: 2002/10/25 23:54:53 $
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
package org.apache.struts.taglib.template.util;

import java.util.HashMap;

/**
 * A simple facade for a hash map. This class restricts operations
 * that can be performed on a hash map of contents. 
 *
 * @author David Geary
 * @version $Revision: 1.5 $ $Date: 2002/10/25 23:54:53 $
 * @deprecated Use Tiles instead.
 */
public class ContentMap implements java.io.Serializable {


// ------------------------------------------------------------ Construtors

   /**
     * Explicitly declare a do-nothing, no-arg constructor. 
     * @deprecated Use Tiles instead.
     */
   public ContentMap() { }

// ----------------------------------------------------- Instance Variables


   /**
     * The map.
     */
   private HashMap map = new HashMap();

// --------------------------------------------------------- Public Methods


   /**
     * Put named content into map.
     *
     * @param name The content's name
     * @param content The content
     * @deprecated Use Tiles instead.
     */
   public void put(String name, Content content) {
   
      map.put(name, content);

   }


   /**
     * Returns the content associated with name.
     *
     * @param name Name of content to retrieve
     * @deprecated Use Tiles instead.
     */
   public Content get(String name) {

      return (Content)map.get(name);

   }
}
