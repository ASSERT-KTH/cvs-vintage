/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Hashtable;

import javax.management.ObjectName;

import javax.management.j2ee.J2EEManagedObject;

import org.jboss.system.Service;

/**
 * MBean Mangement Inteface for {@link org.jboss.management.j2ee.J2EEManagedObject
 * J2EEManagedObject}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.2 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
 **/
public interface J2EEManagedObjectMBean
   extends J2EEManagedObject, Service
{

   // -------------------------------------------------------------------------
   // Static
   // -------------------------------------------------------------------------  
   
   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public ObjectName getObjectName();

   public ObjectName getParent();
   
   public void setParent( ObjectName pParent )
      throws
         InvalidParentException;
   
   public void addChild( ObjectName pChild );
   
   public void removeChild( ObjectName pChild );
   
}
