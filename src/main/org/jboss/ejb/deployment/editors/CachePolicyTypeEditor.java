/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment.editors;

import com.dreambean.awt.editors.TagsEditor;

/**
 *  Editor for selecting the cache policy
 *      
 *	@author Simone Bordet (simone.bordet@compaq.com)
 *  @version $Revision: 1.2 $
 */
public class CachePolicyTypeEditor
   extends TagsEditor
{
   // Constructors --------------------------------------------------
   public CachePolicyTypeEditor()
   {
      super("org/jboss/ejb/deployment/editors/cachepolicy.properties");
   }
}
