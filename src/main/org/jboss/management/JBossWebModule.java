/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javax.management.j2ee.Servlet;
import javax.management.j2ee.WebModule;

import java.security.InvalidParameterException;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision$
*/
public class JBossWebModule
  extends JBossJ2EEModule
  implements WebModule
{
  // Attributes ----------------------------------------------------

  private List mServlets = new ArrayList();

  // Constructors --------------------------------------------------

  /**
  * @param pName Name of the Web Module
  * @param pServlets List of Servlets for this Module
  *
  * @throws InvalidParameterException If given list is null or empty
  **/
  public JBossWebModule( String pName, Servlet[] pServlets ) {
    super( pName );
    setServlets( pServlets );
  }

  // Public --------------------------------------------------------

  public Servlet[] getServlets() {
    return (Servlet[]) mServlets.toArray( new Servlet[ 0 ] );
  }
  
  public Servlet getServlet( int pIndex ) {
    if( pIndex >= 0 && pIndex < mServlets.size() )
    {
      return (Servlet) mServlets.get( pIndex );
    }
    else
    {
      return null;
    }
  }
  
  public String toString() {
    return "JBossWebModule[ " +
      "Servlets: " + mServlets +
      " ]";
  }

  // Protected -----------------------------------------------------

  /**
  * Sets a new list of Servlets
  *
  * @param pServlets New list of Servlets to be set
  *
  * @throws InvalidParameterException If given list is null or empty
  **/
  public void setServlets( Servlet[] pServlets )
    throws
      InvalidParameterException
  {
    if( pServlets == null || pServlets.length == 0 ) {
      throw new InvalidParameterException( "Servlets may not be null or empty" );
    } else {
      mServlets = new ArrayList( Arrays.asList( pServlets ) );
    }
  }
   
}
