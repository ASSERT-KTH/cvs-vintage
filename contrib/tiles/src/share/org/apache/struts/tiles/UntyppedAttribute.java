/*
 * $Header: /tmp/cvs-vintage/struts/contrib/tiles/src/share/org/apache/struts/tiles/Attic/UntyppedAttribute.java,v 1.2 2001/12/27 17:35:38 cedric Exp $
 * $Revision: 1.2 $
 * $Date: 2001/12/27 17:35:38 $
 * $Author: cedric $
 *
 */


package org.apache.struts.tiles;

/**
 * Common implementation of attribute definition. 
 */
public class UntyppedAttribute implements AttributeDefinition {

    /**
     * Role associated to this attribute
     */
  protected String role;
  protected Object value;

    /**
     * Constructor.
     */
  public UntyppedAttribute( Object value )
    {
    this.value = value;
    }

    /**
     * Constructor.
     */
  public UntyppedAttribute( Object value, String role )
    {
    this.value = value;
    this.role = role;
    }

    /**
     * Get role.
     */
  public String getRole()
    {
    return role;
    }

    /**
     * Set role.
     */
  public void setRole(String role)
    {
    this.role = role;
    }

  /**
   * Get value.
   */
  public Object getValue()
    {
    return value;
    }

  /**
   * Set value.
   */
  public void setValue( Object value )
    {
    this.value = value;
    }
    
    /**
     * Get String representation of this object.
     */
  public String toString()
    {
    return value.toString();
    }

}
