/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
 *   @version $Revision: 1.1 $
 */
public class ContainerTypeEditor
   extends TagsEditor
{
   // Constructors --------------------------------------------------
   public ContainerTypeEditor()
   {
      super("org/jboss/ejb/deployment/editors/containertypes.properties");
   }
}
