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

import javax.management.j2ee.EJB;
import javax.management.j2ee.EjbModule;

import java.security.InvalidParameterException;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision$
*/
public class JBossEjbModule
  extends JBossJ2EEModule
  implements EjbModule
{
  // Attributes ----------------------------------------------------

  private List mEJBs = new ArrayList();

  // Constructors --------------------------------------------------

  /**
  * @param pName Name of the EjbModule
  * @param pEJBs List of EJBs for this Module
  *
  * @throws InvalidParameterException If given list is null or empty
  **/
  public JBossEjbModule( String pName, EJB[] pEJBs ) {
    super( pName );
    setEjbs( pEJBs );
  }

  // Public --------------------------------------------------------

  public EJB[] getEjbs() {
    return (EJB[]) mEJBs.toArray( new EJB[ 0 ] );
  }
  
  public EJB getEjb( int pIndex ) {
    if( pIndex >= 0 && pIndex < mEJBs.size() )
    {
      return (EJB) mEJBs.get( pIndex );
    }
    else
    {
      return null;
    }
  }
  
  public String toString() {
    return "JBossEjbModule[ " +
      "EJBs: " + mEJBs +
      " ]";
  }

  // Protected -----------------------------------------------------

  /**
  * Sets a new list of EJBs
  *
  * @param pEJBs New list of EJBs to be set
  *
  * @throws InvalidParameterException If given list is null or empty
  **/
  public void setEjbs( EJB[] pEJBs )
    throws
      InvalidParameterException
  {
    if( pEJBs == null || pEJBs.length == 0 ) {
      throw new InvalidParameterException( "EJBs may not be null or empty" );
    } else {
      mEJBs = new ArrayList( Arrays.asList( pEJBs ) );
    }
  }
   
}
