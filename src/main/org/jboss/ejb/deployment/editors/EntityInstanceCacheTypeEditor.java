/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment.editors;

import java.beans.*;

import com.dreambean.awt.editors.TagsEditor;

/**
 *   Editor for selecting resource manager type
 *      
 *   @see DDEditor
 *   @author $Author: oberg $
 *   @version $Revision: 1.2 $
 */
public class EntityInstanceCacheTypeEditor
   extends TagsEditor
{
   // Constructors --------------------------------------------------
   public EntityInstanceCacheTypeEditor()
   {
      super("org/jboss/ejb/deployment/editors/entityinstancecache.properties");
   }
}
