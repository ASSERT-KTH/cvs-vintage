/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jms.ra;
/**
 * Helper class to compare strings.
 *
 *
 * Created: Thu Sep 27 10:53:13 2001
 *
 * @author Peter Antman
 * @version $Revision: 1.1 $ $Date: 2001/09/27 13:25:22 $
 */

public class StringUtil  {
   
   /**
    * <p>Compare two strings.
    *
    * <p>Both or one of them may be null.
    *
    * @return true if object equals or intern ==, else false. 
    */
    public static boolean compare(String me, String you) {
	// If both null or intern equals
	if (me == you)
	    return true;
	// if me null and you are not
	if (me == null && you != null)
	    return false;
	// me will not be null, test for equality
	return me.equals(you);
    }
   
} // StringUtil
