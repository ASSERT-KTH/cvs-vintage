/*
 * $Header: /tmp/cvs-vintage/struts/src/tiles-documentation/org/apache/struts/webapp/tiles/skin/DefinitionCatalog.java,v 1.3 2004/01/13 12:48:58 husted Exp $
 * $Revision: 1.3 $
 * $Date: 2004/01/13 12:48:58 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowledgement:
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
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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

package org.apache.struts.webapp.tiles.skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.DefinitionsUtil;
import org.apache.struts.tiles.FactoryNotFoundException;
import org.apache.struts.tiles.NoSuchDefinitionException;

/**
 * A catalog of available definitions.
 */
public class DefinitionCatalog
{
      /** debug flag */
    public static boolean debug = true;
    /** Attribute carrying definition readable name */
   public static final String LABEL_NAME_ATTRIBUTE = "skin.label";
    /** Attribute carrying the list of definition names */
   public static final String DEFINITION_LIST_ATTRIBUTE = "skin.list";

   /**
    * Map of skins, by their keys
    */
   private Map definitions = new HashMap();
   /**
    * Map of skins, by their keys
    */
   private ComponentDefinition defaultDefinition;

   /**
    * List of names
    */
   private List names = new ArrayList();

   /**
    * List of keys
    */
   private List keys = new ArrayList();

   /**
    * Constructor.
    * Initialize catalog from definitions factory.
    * @param HttpRequest request
    * @param ServletContext context
    * @throws FactoryNotFoundException, DefinitionsFactoryException
    */
   public DefinitionCatalog( String catalogName, HttpServletRequest request, ServletContext context)
     throws FactoryNotFoundException, DefinitionsFactoryException
   {
     // Get definition containing list of definitions
   ComponentDefinition catalogDef = DefinitionsUtil.getDefinition( catalogName, request, context);
   if(debug)
     System.out.println( "Got definition " + catalogDef );
     // Get list of definition names
   List list = (List)catalogDef.getAttribute( DEFINITION_LIST_ATTRIBUTE );
   Iterator i = list.iterator();
   while(i.hasNext() )
     {
     String name = (String)i.next();
     System.out.println( "add " + name );
     ComponentDefinition def = DefinitionsUtil.getDefinition(name, request, context);
     if(def==null)
       throw new NoSuchDefinitionException("Can't find definition '" + name + "'" );
     add( name, def );
     } // end loop
   if(debug)
     System.out.println( "Catalog initialized" );
   }

   /**
    * Get definition identified by key.
    * @param key
    * @return Definition associated to key
    */
   public ComponentDefinition get(Object key)
   {
   if(key==null)
     return getDefault();
   return (ComponentDefinition)definitions.get(key);
   }

   /**
    * Get definition identified by key.
    * @param key
    * @return Definition associated to key
    */
   public ComponentDefinition getDefault()
   {
   return defaultDefinition;
   }

   /**
    * Return List of names of definitions presents in catalog.
    * Names are user readable names. Returned list has the same order as list
    * returned by getKeys.
    * @return List
    */
   public List getNames()
   {
    return names;
   }

   /**
    * Get list of keys of definitions present in catalog.
    * A key is used to retrieve a skin from catalog.
    * @return List
    */
   public List getKeys()
   {
    return keys;
   }

   /**
    * Check if requested key is valid in catalog.
    * Return null otherwise
    * @return valid key or null
    */
   public String getKey( String key )
   {
   if( definitions.get(key) != null)
    return key;

   return null;
   }

   /**
    * Add a skin definition
    * @param definition
    */
   public void add(String key, ComponentDefinition definition)
   {
     // Intitialize default definition with first definition encountered
   if( defaultDefinition == null )
     {
     defaultDefinition = definition;
     }
     // store definition
   definitions.put( key , definition);
   Object name = definition.getAttribute(LABEL_NAME_ATTRIBUTE);
   if( name == null )
     name = key;
   names.add( name );
   keys.add(key);
   }
}
