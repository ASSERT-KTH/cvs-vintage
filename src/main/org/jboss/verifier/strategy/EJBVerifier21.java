/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

// $Id: EJBVerifier21.java,v 1.1 2003/12/02 16:13:04 cgjung Exp $

package org.jboss.verifier.strategy;

// standard imports
import java.util.Arrays;
import java.util.Iterator;
import java.lang.reflect.Method;

// non-standard class dependencies
import org.jboss.metadata.SessionMetaData;
import org.jboss.verifier.Section;

/**
 * EJB 2.1 bean verifier.
 *
 * @author <a href="mailto:christoph.jung@infor.de">Christoph G. Jung</a>
 *
 * @version $Revision: 1.1 $
 * @since   02.12.2003
 */

public class EJBVerifier21 extends EJBVerifier20
{
   protected Class serviceEndpointInterface;

   /*
    * Constructor
    */
   public EJBVerifier21(VerificationContext context)
   {
      super(context);
   }

   /*
    ***********************************************************************
    *
    *    IMPLEMENTS VERIFICATION STRATEGY INTERFACE
    *
    ***********************************************************************
    */
   public void checkSession(SessionMetaData session)
   {
      boolean localOrRemoteExists = false;
      boolean verified = false;

      if (!verifyBean(session))
         return;

      verified = verifySessionBean(session);

      if (hasRemoteInterfaces(session))
      {
         // Check remote interfaces
         localOrRemoteExists = true;
         verified = verified && verifySessionRemote(session);
         verified = verified && verifySessionHome(session);
      }

      if (hasLocalInterfaces(session))
      {
         // Check local interfaces
         localOrRemoteExists = true;
         verified = verified && verifySessionLocal(session);
         verified = verified && verifySessionLocalHome(session);
      }

      if (hasServiceEndpointInterfaces(session))
      {
         // Check local interfaces
         localOrRemoteExists = true;
         verified = verified && verifyServiceEndpoint(session);
      }

      // The session bean MUST implement either a remote home and
      // remote, or local home and local interface, or the service endpoint
      // interface.  It MAY implement a
      // remote home, remote, local home, local interface or service endpoint
      // interface
      //
      // Spec 7.10.1
      //
      if (!localOrRemoteExists)
      {
         fireSpecViolationEvent(session, new Section("7.11.1.x"));
         verified = false;
      }

      if (verified)
      {
         // All OK; full steam ahead
         fireBeanVerifiedEvent(session);
      }
   }

   /**
    * Finds java.rmi.Remote interface from the class
    */
   public boolean hasRemoteInterface(Class c)
   {
      return isAssignableFrom("java.rmi.Remote", c);
   }

   /**
    * Verify Session Bean Service Endpoint
    * @param session
    * @return
    */
   protected boolean verifyServiceEndpoint(SessionMetaData session)
   {
      boolean status = true;

      // The service endpoint interface MUST extend the java.rmi.Remote
      // interface.
      //
      // Spec 7.11.9
      //
      if (!hasRemoteInterface(serviceEndpointInterface))
      {
         fireSpecViolationEvent(session, new Section("7.11.9.x"));
         status = false;
      }

      // Method arguments defined in the service-endpoint interface MUST be
      // of valid types for RMI/JAXRPC.
      //
      // Method return values defined in the remote interface MUST
      // be of valid types for RMI/JAXRPC.
      //
      // Methods defined in the remote interface MUST include
      // java.rmi.RemoteException in their throws clause.
      //
      // Spec 7.11.9
      //
      Iterator it =
         Arrays.asList(serviceEndpointInterface.getMethods()).iterator();
      while (it.hasNext())
      {
         Method method = (Method) it.next();

         if (!hasLegalJAXRPCArguments(method))
         {
            fireSpecViolationEvent(session, method, new Section("7.11.9.x"));
            status = false;
         }

         if (!hasLegalJAXRPCReturnType(method))
         {
            fireSpecViolationEvent(session, method, new Section("7.11.9.x"));
            status = false;
         }

         if (!throwsRemoteException(method))
         {
            fireSpecViolationEvent(session, method, new Section("7.11.9.x"));
            status = false;
         }
      }

      // For each method defined in the remote interface, there MUST be
      // a matching method in the session bean's class. The matching
      // method MUST have:
      //
      //  - the same name
      //  - the same number and types of arguments, and the same
      //    return type
      //  - All the exceptions defined in the throws clause of the
      //    matching method of the session bean class must be defined
      //    in the throws clause of the method of the remote interface
      //
      // Spec 7.10.5
      //
      it =
         Arrays
            .asList(serviceEndpointInterface.getDeclaredMethods())
            .iterator();
      while (it.hasNext())
      {
         Method remoteMethod = (Method) it.next();

         if (!hasMatchingMethod(bean, remoteMethod))
         {
            fireSpecViolationEvent(
               session,
               remoteMethod,
               new Section("7.11.9.x"));

            status = false;
         }
         else
         {
            try
            {
               Method beanMethod =
                  bean.getMethod(
                     remoteMethod.getName(),
                     remoteMethod.getParameterTypes());

               if (!hasMatchingReturnType(remoteMethod, beanMethod))
               {
                  fireSpecViolationEvent(
                     session,
                     remoteMethod,
                     new Section("7.11.9.x"));
                  status = false;
               }

               if (!hasMatchingExceptions(beanMethod, remoteMethod))
               {
                  fireSpecViolationEvent(
                     session,
                     remoteMethod,
                     new Section("7.11.59.x"));
                  status = false;
               }
            }
            catch (NoSuchMethodException ignored)
            {
            }
         }
      }

      return status;
   }

   /**
    * @param method
    * @return
    */
   protected boolean hasLegalJAXRPCReturnType(Method method)
   {
      return isJAXRPCType(method.getReturnType());
   }

   /**
    * @param class1
    * @return
    */
   protected boolean isJAXRPCType(Class class1)
   {
      // TODO this should be implemented along the jaxrpc spec
      return isRMIIDLValueType(class1);
   }

   /**
    * @param method
    * @return
    */
   protected boolean hasLegalJAXRPCArguments(Method method)
   {
      Class[] params = method.getParameterTypes();

      for (int i = 0; i < params.length; ++i)
      {
         if (!isJAXRPCType(params[i]))
            return false;
      }

      return true;
   }

   /**
    * Check whether the bean has declared service endpoint interfaces and whether
    * we can load the defined classes
    * @param bean
    * @return <code>true</code> if everything went alright
    */
   protected boolean hasServiceEndpointInterfaces(SessionMetaData bean)
   {
      boolean status = true;
      String seiName = bean.getServiceEndpoint();

      if (seiName == null)
         return false;

      // Verify the <service-endpoint> class
      try
      {
         serviceEndpointInterface = classloader.loadClass(seiName);
      }
      catch (ClassNotFoundException cnfe)
      {
         fireSpecViolationEvent(
            bean,
            new Section(
               "23.2",
               "Class not found on '" + seiName + "': " + cnfe.getMessage()));
         status = false;
      }

      return status;
   }

}
/*
vim:ts=3:sw=3:et
*/
