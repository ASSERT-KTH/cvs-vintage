/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package x;

//EXPLICIT IMPORTS
import a.b.C1; // GOOD
import a.b.C2;
import a.b.C3;

// DO NOT WRITE
import a.b.*;  // BAD


/**
*   <description>
*
*   @see <related>
*   @author  <a href="mailto:{email}">{full name}</a>.
*   @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
*   @version $Revision: 1.3 $
*   Revisions:
*
*   yyyymmdd author: explicit fix description (no line numbers but methods) go beyond the cvs commit message
*   eg: 
*   20010516 marc fleury: Ask all developers to clearly document the Revisions, changed the header.  
*/
public interface X
extends Y
{
  // Constants -----------------------------------------------------
  
  // Static --------------------------------------------------------
  
  // Public --------------------------------------------------------
  public ReturnClass doSomething()
  throws ExceptionA, ExceptionB;
  
  // DO NOT USE "TAB" USE 2 SPACES FOR PORTABILITY AMONG EDITORS
}
