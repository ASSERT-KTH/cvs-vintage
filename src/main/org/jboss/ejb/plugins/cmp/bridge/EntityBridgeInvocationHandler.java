/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.bridge;

import java.lang.reflect.Method;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.FinderException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.proxy.compiler.InvocationHandler;

/**
 * EntityBridgeInvocationHandler is the invocation hander used by the CMP 2.x
 * dynamic proxy. This class only interacts with the EntityBridge. The main
 * job of this class is to deligate invocation of abstract methods to the
 * appropriate EntityBridge method.
 *
 * Life-cycle:
 *      Tied to the life-cycle of an entity bean instance.
 *
 * Multiplicity:
 *      One per cmp entity bean instance, including beans in pool.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 1.23 $
 */
public class EntityBridgeInvocationHandler implements InvocationHandler
{
   private final Class beanClass;
   private final Map fieldMap;
   private final Map selectorMap;
   private EntityEnterpriseContext ctx;

   /**
    * Creates an invocation handler for the specified entity.
    */
   public EntityBridgeInvocationHandler(Map fieldMap, Map selectorMap, Class beanClass)
   {
      this.beanClass = beanClass;
      this.fieldMap = fieldMap;
      this.selectorMap = selectorMap;
   }

   public void setContext(EntityEnterpriseContext ctx)
   {
      if(ctx != null && !beanClass.isInstance(ctx.getInstance()))
      {
         throw new EJBException("Instance must be an instance of beanClass");
      }
      this.ctx = ctx;
   }

   public Object invoke(Object proxy, Method method, Object[] args)
      throws FinderException
   {
      // todo find a better workaround
      // CMP/CMR field bridges are mapped to its abstract method names because of the bug
      // in reflection introduced in Sun's 1.4 JVM, i.e. when an abstract class C1 extends a super class C2
      // and implements interface I and C2 and I both declare method with the same signature M,
      // C1.getMethods() will contain M twice.
      // ejbSelect methods are mapped to Method objects instead. Because ejbSelect methods having the same name
      // might have different signatures. Hopefully, the probability of an ejbSelect method to appear in an interface
      // is lower.

      String methodName = method.getName();

      BridgeInvoker invoker = (BridgeInvoker) fieldMap.get(methodName);
      if(invoker == null)
      {
         //invoker = (BridgeInvoker) selectorMap.get(methodName);
         invoker = (BridgeInvoker) selectorMap.get(method);

         if(invoker == null)
         {
            throw new EJBException("Method is not a known CMP field " +
               "accessor, CMR field accessor, or ejbSelect method: " +
               "methodName=" + methodName);
         }
      }

      try
      {
         return invoker.invoke(ctx, method, args);
      }
      catch(RuntimeException e)
      {
         throw e;
      }
      catch(FinderException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new EJBException("Internal error", e);
      }
   }

   // Inner

   public interface BridgeInvoker
   {
      Object invoke(EntityEnterpriseContext ctx, Method method, Object[] args) throws FinderException, Exception;
   }

   public static class FieldGetInvoker implements BridgeInvoker
   {
      private final FieldBridge field;

      public FieldGetInvoker(FieldBridge field)
      {
         this.field = field;
      }

      public Object invoke(EntityEnterpriseContext ctx, Method method, Object[] args)
      {
         // In the case of ejbHome methods there is no context, but ejb home
         // methods are only allowed to call selectors.
         if(ctx == null)
         {
            throw new EJBException("EJB home methods are not allowed to " +
               "access CMP or CMR fields: methodName=" + method.getName());
         }

         return field.getValue(ctx);
      }
   }

   public static class FieldSetInvoker implements BridgeInvoker
   {
      private final FieldBridge field;

      public FieldSetInvoker(FieldBridge field)
      {
         this.field = field;
      }

      public Object invoke(EntityEnterpriseContext ctx, Method method, Object[] args)
      {
         // In the case of ejbHome methods there is no context, but ejb home
         // methods are only allowed to call selectors.
         if(ctx == null)
         {
            throw new EJBException("EJB home methods are not allowed to " +
               "access CMP or CMR fields: methodName=" + method.getName());
         }

         field.setValue(ctx, args[0]);
         return null;
      }
   }
}
