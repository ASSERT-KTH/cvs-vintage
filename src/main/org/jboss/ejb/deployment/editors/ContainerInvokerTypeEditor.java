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
public class ContainerInvokerTypeEditor
   extends TagsEditor
{
   // Constructors --------------------------------------------------
   public ContainerInvokerTypeEditor()
   {
      super("org/jboss/ejb/deployment/editors/containerinvoker.properties");
   }
}
