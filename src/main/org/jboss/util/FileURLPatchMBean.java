/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

import java.lang.Object;

/**
 *   Used to show the managment interface of the FileURLPatch object.
 *   @see <related>
 *   @author <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a>.
 *   @version $Revision: 1.2 $
 */
public interface FileURLPatchMBean {
	public void setEnabled(boolean enable);
}
