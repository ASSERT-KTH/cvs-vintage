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
 *	@author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 *  @version $Revision: 1.3 $
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
