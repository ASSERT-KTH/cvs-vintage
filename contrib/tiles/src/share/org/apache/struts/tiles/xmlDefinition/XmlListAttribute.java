/*
 * $Header: /tmp/cvs-vintage/struts/contrib/tiles/src/share/org/apache/struts/tiles/xmlDefinition/Attic/XmlListAttribute.java,v 1.4 2002/06/13 16:53:56 cedric Exp $
 * $Revision: 1.4 $
 * $Date: 2002/06/13 16:53:56 $
 * $Author: cedric $
 *
 */


package org.apache.struts.tiles.xmlDefinition;

import java.util.List;
import java.util.ArrayList;


/**
 * An attribute as a List.
 * This attribute associates a name with a list. The list can be found by the
 * property name.
 * Element in list are retrieved using List methods.
 * This class is used to read configuration files.
 */
public class XmlListAttribute extends XmlAttribute
{
    /** List.
     * We declare a list, to avoid cast.
     * Parent "value" property point to the same list.
     */
  private List list;

    /**
     * Constructor.
     */
  public XmlListAttribute()
    {
    list = new ArrayList();
    setValue(list);
    }

    /**
     * Constructor.
     */
  public XmlListAttribute( String name, List value)
    {
    super( name, value );
    list = value;
    }

    /**
     * Add an element in list.
     * We use a Property, to avoid rewriting a new class.
     */
  public void add( XmlAttribute element )
    {
    list.add( element.getValue() );
    }

    /**
     * Add an element in list.
     */
  public void add( Object value )
    {
    //list.add( value );
      // To correct a bug in digester, we need to check the object type
      // Digester doesn't call correct method according to object type ;-(
    if(value instanceof XmlAttribute)
      {
      add((XmlAttribute)value);
      return;
      }
     else
      list.add( value );
    }

    /**
     * Add an element in list.
     */
  public void addObject( Object value )
    {
    list.add( value );
    }



}
