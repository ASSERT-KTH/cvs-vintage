/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws.deployment.editors;

import java.beans.*;
import java.beans.beancontext.*;
import java.util.*;

import com.dreambean.awt.editors.TagsEditor;

import org.jboss.ejb.plugins.jaws.deployment.*;

/**
 *   Editor for selecting resource manager type
 *      
 *   @see DDEditor
 *   @author $Author: oberg $
 *   @version $Revision: 1.2 $
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
         JawsEnterpriseBeans eb = (JawsEnterpriseBeans)((BeanContextServices)bcc.getBeanContext()).getService(bcc, bcc, JawsEnterpriseBeans.class, null, bcc);
         JawsEjbJar jar = (JawsEjbJar)eb.getBeanContext();
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
         e.printStackTrace();
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
            JawsEnterpriseBeans eb = (JawsEnterpriseBeans)((BeanContextServices)getBeanContext()).getService(this, this, JawsEnterpriseBeans.class, null, this);
            JawsEjbJar jar = (JawsEjbJar)eb.getBeanContext().getBeanContext();
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
