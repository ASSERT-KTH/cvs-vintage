/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.security.Principal;
import java.util.Set;

public interface RealmMapping
{
	public boolean doesUserHaveRole( Principal principal, Set roleNames );
}
