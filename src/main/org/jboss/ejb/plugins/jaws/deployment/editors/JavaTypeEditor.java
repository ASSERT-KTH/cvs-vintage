/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws.deployment.editors;

import java.beans.*;

import com.dreambean.awt.editors.TagsEditor;

/**
 *   Editor for selecting resource manager type
 *      
 *   @see DDEditor
 *   @author $Author: oberg $
 *   @version $Revision: 1.3 $
 */
public class JavaTypeEditor
   extends TagsEditor
{
   // Constructors --------------------------------------------------
   public JavaTypeEditor()
   {
      super("org/jboss/ejb/plugins/jaws/deployment/editors/javatypes.properties");
   }
}
