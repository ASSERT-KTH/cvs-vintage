/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws.deployment.editors;

import java.beans.*;
import java.beans.beancontext.*;
import java.util.*;

import com.dreambean.awt.editors.TagsEditor;

import org.jboss.ejb.plugins.jaws.deployment.*;
import org.jboss.logging.Logger;


/**
 *   Editor for selecting resource manager type
 *      
 *   @see DDEditor
 *   @author $Author: oberg $
 *   @version $Revision: 1.5 $
 */
public class TypeMappingEditor
   extends TagsEditor
   implements BeanContextProxy
{
   BeanContextChild bcProxy;
   
   // Constructors --------------------------------------------------
   public TypeMappingEditor()
   {
      super(null, null);
      
      bcProxy = new BCHelper();
   }
   
   public BeanContextChild getBeanContextProxy()
   {
      return bcProxy;
   }
   
   protected void updateTags(BeanContextChildSupport bcc)
   {
      try
      {
         JawsEjbJar jar = (JawsEjbJar)((BeanContextServices)bcc.getBeanContext()).getService(bcc, bcc, JawsEjbJar.class, null, bcc);
         Iterator enum = jar.getTypeMappings().iterator();
         Vector v = new Vector();
         while (enum.hasNext())
         {
            v.addElement(((TypeMapping)enum.next()).getName());
         }
         
         tags = new String[v.size()];
         v.copyInto(tags);
         values = tags;
      } catch (Exception e)
      {
         Logger.exception(e);
      }
   }
   
   class BCHelper
      extends BeanContextChildSupport
   {
      protected void initializeBeanContextResources()
      {
         updateTags(this);
         
         // Add listener
         try
         {
            JawsEjbJar jar = (JawsEjbJar)((BeanContextServices)getBeanContext()).getService(this, this, JawsEjbJar.class, null, this);
            jar.getTypeMappings().addBeanContextMembershipListener(new BeanContextMembershipListener()
            {
               public void childrenAdded(BeanContextMembershipEvent bcme)
               {
                  updateTags(BCHelper.this);
               }
               
               public void childrenRemoved(BeanContextMembershipEvent bcme)
               {
                  updateTags(BCHelper.this);
               }
            });
         } catch (Exception e) {}
      }
   }
}
