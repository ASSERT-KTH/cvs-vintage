//Source file: H:\\TEMP\\generated\\org\\apache\\struts\\example\\tiles\\skin\\DefinitionCatalog.java

package org.apache.struts.example.tiles.skin;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsUtil;
import org.apache.struts.tiles.FactoryNotFoundException;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.NoSuchDefinitionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

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
