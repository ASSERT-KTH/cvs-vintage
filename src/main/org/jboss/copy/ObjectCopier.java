/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */
package org.jboss.copy;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @jmx:mbean name="jboss:service=ObjectCopier"
 *       extends="org.jboss.system.ServiceMBean"
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 */
public class ObjectCopier 
extends ServiceMBeanSupport 
implements ObjectCopierMBean, Copier
{
   private Map copiers = new HashMap();
   private List interfaces = new ArrayList();

   public ObjectCopier()
   {
      PropertyCopier propertyCopier;

      // java.awt.*
      // addCopier(java.awt.Color.class, ImmutableCopier.COPIER);
      // addCopier(java.awt.Dimension.class, 
      //       new CopyConstructorCopier(java.awt.Dimension.class));
      // addCopier(java.awt.Point.class, 
      //       new CopyConstructorCopier(java.awt.Point.class));
      // 
      // propertyCopier = new PropertyCopier(this, java.awt.Polygon.class);
      // propertyCopier.addFieldToConstructorArgProperty("xpoints", 0);
      // propertyCopier.addFieldToConstructorArgProperty("ypoints", 1);
      // propertyCopier.addFieldToConstructorArgProperty("npoints", 2);
      // addCopier(java.awt.Polygon.class, propertyCopier);

      // java.awt.geom.*
      // addCopier(java.awt.geom.AffineTransform.class,
      //       new DeepCloneCopier(java.awt.geom.AffineTransform.class));
      // addCopier(java.awt.geom.Arc2D.Double.class,
      //       new DeepCloneCopier(java.awt.geom.Arc2D.Double.class));
      // addCopier(java.awt.geom.Arc2D.Float.class,
      //       new DeepCloneCopier(java.awt.geom.Arc2D.Float.class));
      // addCopier(java.awt.geom.Area.class,
      //       new DeepCloneCopier(java.awt.geom.Area.class));
      // addCopier(java.awt.geom.CubicCurve2D.Double.class,
      //       new DeepCloneCopier(java.awt.geom.CubicCurve2D.Double.class));
      // addCopier(java.awt.geom.CubicCurve2D.Float.class,
      //       new DeepCloneCopier(java.awt.geom.CubicCurve2D.Float.class));
      // addCopier(java.awt.geom.Ellipse2D.Double.class,
      //       new DeepCloneCopier(java.awt.geom.Ellipse2D.Double.class));
      // addCopier(java.awt.geom.Ellipse2D.Float.class,
      //       new DeepCloneCopier(java.awt.geom.Ellipse2D.Float.class));
      // addCopier(java.awt.geom.Line2D.Double.class,
      //       new DeepCloneCopier(java.awt.geom.Line2D.Double.class));
      // addCopier(java.awt.geom.Line2D.Float.class,
      //       new DeepCloneCopier(java.awt.geom.Line2D.Float.class));
      // addCopier(java.awt.geom.Point2D.Double.class,
      //       new DeepCloneCopier(java.awt.geom.Point2D.Double.class));
      // addCopier(java.awt.geom.Point2D.Float.class,
      //       new DeepCloneCopier(java.awt.geom.Point2D.Float.class));
      // addCopier(java.awt.geom.QuadCurve2D.Double.class,
      //       new DeepCloneCopier(java.awt.geom.QuadCurve2D.Double.class));
      // addCopier(java.awt.geom.QuadCurve2D.Float.class,
      //       new DeepCloneCopier(java.awt.geom.QuadCurve2D.Float.class));
      // addCopier(java.awt.geom.Rectangle2D.Double.class,
      //       new DeepCloneCopier(java.awt.geom.Rectangle2D.Double.class));
      // addCopier(java.awt.geom.Rectangle2D.Float.class,
      //       new DeepCloneCopier(java.awt.geom.Rectangle2D.Float.class));
      // addCopier(java.awt.geom.RoundRectangle2D.Double.class,
      //       new DeepCloneCopier(java.awt.geom.RoundRectangle2D.Double.class));
      // addCopier(java.awt.geom.RoundRectangle2D.Float.class,
      //       new DeepCloneCopier(java.awt.geom.RoundRectangle2D.Float.class));

      // java.io.*
      addCopier(java.io.Serializable.class, new SerializeCopier());

      // java.lang.*
      addCopier(Boolean.class, ImmutableCopier.COPIER);
      addCopier(Boolean.TYPE, ImmutableCopier.COPIER);
      addCopier(Byte.class, ImmutableCopier.COPIER);
      addCopier(Byte.TYPE, ImmutableCopier.COPIER);
      addCopier(Character.class, ImmutableCopier.COPIER);
      addCopier(Character.TYPE, ImmutableCopier.COPIER);
      addCopier(Double.class, ImmutableCopier.COPIER);
      addCopier(Double.TYPE, ImmutableCopier.COPIER);
      addCopier(Float.class, ImmutableCopier.COPIER);
      addCopier(Float.TYPE, ImmutableCopier.COPIER);
      addCopier(Integer.class, ImmutableCopier.COPIER);
      addCopier(Integer.TYPE, ImmutableCopier.COPIER);
      addCopier(Long.class, ImmutableCopier.COPIER);
      addCopier(Long.TYPE, ImmutableCopier.COPIER);
      addCopier(Object.class, this);
      addCopier(Short.class, ImmutableCopier.COPIER);
      addCopier(Short.TYPE, ImmutableCopier.COPIER);
      addCopier(String.class, ImmutableCopier.COPIER);

      propertyCopier = new PropertyCopier(this, StringBuffer.class);
      propertyCopier.addMethodToConstructorArgProperty("toString", 0);
      addCopier(StringBuffer.class, propertyCopier);
      
      // java.math.*
      addCopier(java.math.BigDecimal.class, ImmutableCopier.COPIER);
      addCopier(java.math.BigInteger.class, ImmutableCopier.COPIER);

      // java.net.*
      addCopier(java.net.InetAddress.class, ImmutableCopier.COPIER);
      addCopier(java.net.URL.class, ImmutableCopier.COPIER);

      // java.rmi.*
      addCopier(java.rmi.MarshalledObject.class, ImmutableCopier.COPIER);

      // java.rmi.server.*
      addCopier(java.rmi.MarshalledObject.class, ImmutableCopier.COPIER);

      // java.sql.*
      addCopier(java.sql.Date.class, 
            new DeepCloneCopier(java.sql.Date.class));
      addCopier(java.sql.Time.class, 
            new DeepCloneCopier(java.sql.Time.class));
      addCopier(java.sql.Timestamp.class, 
            new DeepCloneCopier(java.sql.Timestamp.class));

      // java.util.*
      addCopier(java.util.ArrayList.class, new UtilCollectionsCopier(this));
      addCopier(java.util.BitSet.class, 
            new DeepCloneCopier(java.util.BitSet.class));
      addCopier(java.util.Date.class, 
            new DeepCloneCopier(java.util.Date.class));
      addCopier(java.util.GregorianCalendar.class, 
            new DeepCloneCopier(java.util.GregorianCalendar.class));
      addCopier(java.util.HashMap.class, new UtilCollectionsCopier(this));
      addCopier(java.util.HashSet.class, new UtilCollectionsCopier(this));
      addCopier(java.util.Hashtable.class, new UtilCollectionsCopier(this));
      addCopier(java.util.LinkedList.class, new UtilCollectionsCopier(this));
      addCopier(java.util.Stack.class, new UtilCollectionsCopier(this));
      addCopier(java.util.TreeMap.class, new UtilCollectionsCopier(this));
      addCopier(java.util.TreeSet.class, new UtilCollectionsCopier(this));
      addCopier(java.util.Vector.class, new UtilCollectionsCopier(this));
      addCopier(java.util.WeakHashMap.class, new UtilCollectionsCopier(this));
   }
   
   /**
    * @jmx.managed-operation
    */
   public void addCopier(Class clazz, Copier copier)
   {
      Object oldCopier = copiers.put(clazz, copier);

      if(oldCopier == null && clazz.isInterface())
      {
         interfaces.add(clazz);
      }
   }
   
   /**
    * @jmx.managed-operation
    */
   public Object copy(Object source)
   {
      return copy(source, new IdentityHashMap());
   }

   public Object copy(Object source, IdentityHashMap referenceMap)
   {
      // nothing returns nothing
      if(source == null)
      {
         return null;
      }
      
      // check the reference map first
      Object copy = referenceMap.get(source);
      if(copy != null)
      {
         return copy;
      }
      
      // get the source type 
      Class type = source.getClass();

      // if this is a (non-subclassed) instance of java.lang.Object. just
      // return a new object
      if(type == Object.class) 
      {
         copy = new Object();
         referenceMap.put(source, copy);
         return copy;
      } 
   
      // if this is an array we need to use the array copier
      if(type.isArray())
      {
         return copyArray(source, referenceMap);
      }

      Copier copier = null;

      // check if the object knows how to copy it self
      if(source instanceof Copier) 
      {
         copier = (Copier)source;
      }

      // check the registered copiers
      if(copier == null)
      {
         copier = (Copier)copiers.get(type);
      }

      // if there is no copier registered check the defaults
      if(copier == null)
      {
         // Try to get a copier based on an interface
         for(Iterator iterator = interfaces.iterator(); 
               copier == null && iterator.hasNext(); )
         {
            Class supportedInterface = (Class)iterator.next();
            if(supportedInterface.isAssignableFrom(type))
            {
               copier =  (Copier)copiers.get(supportedInterface);
               copiers.put(type, copier);
            }
         }
      }

      // If we still don't have a copier, throw an exception
      if(copier == null)
      {
         throw new CopyException("Class is not copyable:  " + type.getName());
      }

      // use the copier
      return copier.copy(source, referenceMap);
   }
   
   private Object copyArray(Object source, IdentityHashMap referenceMap)
   {
      // nothing gets you nothing
      if(source == null)
      {
         return null;
      }

      // check the reference map first
      Object[] referenceCopy = (Object[]) referenceMap.get(source);
      if(referenceCopy != null)
      {
         return referenceCopy;
      }
 
      if(source instanceof Object[])
      {
         Object[] objectArray = (Object[])source;
         
         Class sourceClass = objectArray.getClass();
         Object[] copy = (Object[]) Array.newInstance(
               sourceClass.getComponentType(),
               objectArray.length);

         // put the new copy in the reference map
         referenceMap.put(objectArray, copy);

         // copy each entry 
         boolean isMultiDimensional = sourceClass.getComponentType().isArray();
         for(int i = 0; i < objectArray.length; i++)
         {
            // if this is multidimensional we can just skip to copyArray
            if(isMultiDimensional)
            {
               copy[i] = copyArray(objectArray[i], referenceMap);
            }
            else
            {
               copy[i] = copy(objectArray[i], referenceMap);
            }
         }
         return copy;
      }
      else if(source instanceof boolean[])
      {
         boolean[] booleanArray = (boolean[])source;
         boolean[] copy = new boolean[booleanArray.length];
         for(int i = 0; i < booleanArray.length; i++)
         {
            copy[i] = booleanArray[i];
         }
         referenceMap.put(booleanArray, copy);
         return copy;
      }
      else if(source instanceof byte[])
      {
         byte[] byteArray = (byte[])source;
         byte[] copy = new byte[byteArray.length];
         for(int i = 0; i < byteArray.length; i++)
         {
            copy[i] = byteArray[i];
         }
         referenceMap.put(byteArray, copy);
         return copy;
      }
      else if(source instanceof char[])
      {
         char[] charArray = (char[])source;
         char[] copy = new char[charArray.length];
         for(int i = 0; i < charArray.length; i++)
         {
            copy[i] = charArray[i];
         }
         referenceMap.put(charArray, copy);
         return copy;
      }
      else if(source instanceof double[])
      {
         double[] doubleArray = (double[])source;
         double[] copy = new double[doubleArray.length];
         for(int i = 0; i < doubleArray.length; i++)
         {
            copy[i] = doubleArray[i];
         }
         referenceMap.put(doubleArray, copy);
         return copy;
      }
      else if(source instanceof float[])
      {
         float[] floatArray = (float[])source;
         float[] copy = new float[floatArray.length];
         for(int i = 0; i < floatArray.length; i++)
         {
            copy[i] = floatArray[i];
         }
         referenceMap.put(floatArray, copy);
         return copy;
      }
      else if(source instanceof int[])
      {
         int[] intArray = (int[])source;
         int[] copy = new int[intArray.length];
         for(int i = 0; i < intArray.length; i++)
         {
            copy[i] = intArray[i];
         }
         referenceMap.put(intArray, copy);
         return copy;
      }
      else if(source instanceof long[])
      {
         long[] longArray = (long[])source;
         long[] copy = new long[longArray.length];
         for(int i = 0; i < longArray.length; i++)
         {
            copy[i] = longArray[i];
         }
         referenceMap.put(longArray, copy);
         return copy;
      }
      else if(source instanceof short[])
      {
         short[] shortArray = (short[])source;
         short[] copy = new short[shortArray.length];
         for(int i = 0; i < shortArray.length; i++)
         {
            copy[i] = shortArray[i];
         }
         referenceMap.put(shortArray, copy);
         return copy;
      }
      else
      {
         throw new CopyException(
               "Source is not an array: " +
               source.getClass().getName());
      }
   }
}
