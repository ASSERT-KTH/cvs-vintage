package org.jboss.management.j2ee;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public abstract class J2EEDeployedObject
   extends J2EEManagedObject
   implements javax.management.j2ee.J2EEDeployedObject
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String mDeploymentDescriptor;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * Constructor taking the Name of this Object
   *
   * @param pName Name to be set which must not be null
   * @param pDeploymentDescriptor
   *
   * @throws InvalidParameterException If the given Name is null
   **/
   public J2EEDeployedObject(
      String pType,
      String pName,
      ObjectName pParent,
      String pDeploymentDescriptor
   )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pType, pName, pParent );
      mDeploymentDescriptor = pDeploymentDescriptor;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String getDeploymentDescriptor() {
      return mDeploymentDescriptor;
   }
   
   public String toString() {
      return "J2EEDeployedObject { " + super.toString() + " } [ " +
         "deployment descriptor: " + mDeploymentDescriptor +
         " ]";
   }
   
}
