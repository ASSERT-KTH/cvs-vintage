/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.proxy.ejb.handle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.spi.HandleDelegate;

import org.jboss.logging.Logger;

/**
 * <P>Implementation of the javax.ejb.spi.HandleDelegate interface</P>
 * 
 * <P>The HandleDelegate interface is implemented by the EJB container. 
 * It is used by portable implementations of javax.ejb.Handle and
 * javax.ejb.HomeHandle. It is not used by EJB components or by client components.
 * It provides methods to serialize and deserialize EJBObject and EJBHome
 * references to streams.</P>
 * 
 * <P>The HandleDelegate object is obtained by JNDI lookup at the reserved name
 * "java:comp/HandleDelegate".</P> 
 *
 * @author  Dimitris.Andreadis@jboss.org
 * @version $Revision: 1.1 $
 */
public class HandleDelegateImpl
    implements HandleDelegate
{
   private static Logger log = Logger.getLogger(HandleDelegateImpl.class);
   /**
    * CTOR
    */
   public HandleDelegateImpl()
   {
      if (log.isTraceEnabled())
         log.trace("CTOR");
   }
   
   // HandleDelegate implementation ---------------------------------
  /**
   * <P>Serialize the EJBObject reference corresponding to a Handle.</P>
   *
   * <P>This method is called from the writeObject method of portable
   * Handle implementation classes. The ostream object is the same
   * object that was passed in to the Handle class's writeObject.</P>
   *
   * @param ejbObject - The EJBObject reference to be serialized.
   * @param oostream - The output stream.
   * @exception java.io.IOException - The EJBObject could not be serialized
   *                                  because of a system-level failure.
   */
   public void writeEJBObject(EJBObject ejbObject, ObjectOutputStream oostream)
      throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("writeEJBObject"); 
      
      // upcast
      java.io.OutputStream ostream = oostream;
      
      // downcast
      org.omg.CORBA_2_3.portable.OutputStream ostream23 =
        (org.omg.CORBA_2_3.portable.OutputStream) ostream;
        
      ostream23.write_abstract_interface(ejbObject);
   }
   
   /**
    * <P>Deserialize the EJBObject reference corresponding to a Handle.</P>
    *
    * <P>readEJBObject is called from the readObject method of portable
    * Handle implementation classes. The istream object is the same object
    * that was passed in to the Handle class's readObject. When readEJBObject
    * is called, istream must point to the location in the stream at which
    * the EJBObject reference can be read. The container must ensure that
    * the EJBObject reference is capable of performing invocations immediately
    * after deserialization.</P>
    * 
    * @param oistream - The input stream.
    * @return The deserialized EJBObject reference.
    * @exception java.io.IOException - The EJBObject could not be deserialized
    *                                  because of a system-level failure.
    * @exception java.lang.ClassNotFoundException - The EJBObject could not be
    *                                               deserialized because some
    *                                               class could not be found.
    */
   public EJBObject readEJBObject(ObjectInputStream oistream)
      throws IOException, ClassNotFoundException
   {
      if (log.isTraceEnabled())
         log.trace("readEJBObject"); 
      
      // upcast
      java.io.InputStream istream = oistream;
      
      // downcast
      org.omg.CORBA_2_3.portable.InputStream istream23 =
        (org.omg.CORBA_2_3.portable.InputStream) istream;
        
      return (EJBObject) istream23.read_abstract_interface();
   }

   /**
    * <P>Serialize the EJBHome reference corresponding to a HomeHandle.</P>
    *
    * <P>This method is called from the writeObject method of portable HomeHandle
    * implementation classes. The ostream object is the same object that was
    * passed in to the Handle class's writeObject.</P>
    *
    * @param ejbHome - The EJBHome reference to be serialized.
    * @param oostream - The output stream.
    * @exception java.io.IOException - The EJBObject could not be serialized
    *                                  because of a system-level failure.
    */
   public void writeEJBHome(EJBHome ejbHome, ObjectOutputStream oostream)
      throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("writeEJBHome"); 
      
      // upcast
      java.io.OutputStream ostream = oostream;
      
      // downcast
      org.omg.CORBA_2_3.portable.OutputStream ostream23 =
        (org.omg.CORBA_2_3.portable.OutputStream) ostream;
        
      ostream23.write_abstract_interface(ejbHome);      
   }

   /**
    * <P>Deserialize the EJBHome reference corresponding to a HomeHandle.</P>
    *
    * <P>readEJBHome is called from the readObject method of portable HomeHandle
    * implementation classes. The istream object is the same object that was
    * passed in to the HomeHandle class's readObject. When readEJBHome is called,
    * istream must point to the location in the stream at which the EJBHome reference
    * can be read. The container must ensure that the EJBHome reference is capable
    * of performing invocations immediately after deserialization.</P>
    *
    * @param oistream - The input stream.
    * @return The deserialized EJBHome reference.
    * @exception java.io.IOException - The EJBHome could not be deserialized because
    *                                  of a system-level failure.
    * @exception java.lang.ClassNotFoundException - The EJBHome could not be deserialized
    *                                               because some class could not be found.
    */
   public EJBHome readEJBHome(ObjectInputStream oistream)
     throws IOException, ClassNotFoundException
   {
      if (log.isTraceEnabled())
         log.trace("readEJBHome"); 
      
      // upcast
      java.io.InputStream istream = oistream;
      
      // downcast
      org.omg.CORBA_2_3.portable.InputStream istream23 =
        (org.omg.CORBA_2_3.portable.InputStream) istream;
        
      return (EJBHome) istream23.read_abstract_interface();
   }
}
