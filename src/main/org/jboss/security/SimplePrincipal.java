/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security;

import java.security.Principal;

public class SimplePrincipal implements Principal, java.io.Serializable
{
  private String name;

  public SimplePrincipal(String name)
  {
    this.name = name;
  }

  public boolean equals(Object another)
  {
    if (name == null)
      return (another == null);  
    if ((another == null) || !(another instanceof SimplePrincipal))
      return false;
    return name.equals( another.toString() );
  }

  public int hashCode() {
    return (name == null ? 0 : name.hashCode());
  }

  public String toString()
  {
    return name;
  }

  public String getName()
  {
    return name;
  }
} 
