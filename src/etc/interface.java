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
*   @version $Revision: 1.4 $
*   Revisions:
*
*   <p><b>Revisions:</b>
*
*   <p><b>yyyymmdd author:</b>
*   <ul>
*   <li> explicit fix description (no line numbers but methods) go beyond the cvs commit message
*   </ul>
*    eg: 
*   <p><b>20010516 marc fleury:</b>
*   <ul>
*   <li> Ask all developers to clearly document the Revision, changed the header.  
*   </ul>
*/


// DO NOT USE "TAB" TO INDENT CODE USE *3* SPACES FOR PORTABILITY AMONG EDITORS

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
