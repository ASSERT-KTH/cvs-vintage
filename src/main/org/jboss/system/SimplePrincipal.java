/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

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
    if ((another == null) || !(another instanceof SimplePrincipal))
      return false;
    return name.equals( another.toString() );
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
