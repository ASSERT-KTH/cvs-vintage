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
    /**
     * This method should return Principal for the bean that may differ 
     * from the original Principal in the operational environment.
     */
    public Principal getPrincipal( Principal principal );

    /**
     * This method checks if the given ("original") Principal has
     * at least on of the roles in the given set.
     */
    public boolean doesUserHaveRole( Principal principal, Set roleNames );
}
