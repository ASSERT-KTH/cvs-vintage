/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */
package org.jboss.copy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 */
public final class PropertyCopier implements Copier
{
   private static final String LOOP_PLACE_HOLDER = "LOOP_PLACE_HOLDER";
   private ObjectCopier objectCopier;
   private Class type;
   private Constructor constructor;
   private SortedSet constructorArgs = 
      new TreeSet(new ConstructorArgComparator());
   private Collection properties = new HashSet();

   public PropertyCopier(final ObjectCopier objectCopier, final Class type)
   {
      this.objectCopier = objectCopier;
      this.type = type;
   }

   public Object copy(final Object source, IdentityHashMap referenceMap)
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
         if(copy == LOOP_PLACE_HOLDER)
         {
            throw new CopyException("Reference loop deteted in constructor " +
                  "arguments.  A constructor arguemnt can not reference the " +
                  "yet to be constructed object");
         }
         return copy;
      }

      if(constructor == null)
      {
         initConstructor();
      }

      // put a loop place holder in the reference map so we can detect
      // constructor args that refer back to the yet to be constructed object
      referenceMap.put(source, LOOP_PLACE_HOLDER);

      // get the constructor arguments
      Object[] args = new Object[constructorArgs.size()];
      Iterator constructorArgIterator = constructorArgs.iterator();
      for(int i = 0; constructorArgIterator.hasNext(); i++)
      {
         Property property = (Property)constructorArgIterator.next();
         args[i] = objectCopier.copy(property.get(source), referenceMap);
      }

      // construct the new object
      try
      {
         copy = constructor.newInstance(args);
      }
      catch(InvocationTargetException e)
      {
         throw new CopyException("Exception in constructor", 
               e.getTargetException());
      }
      catch(Exception e)
      {
         throw new CopyException("Exception while invokeing copy constructor",
               e);
      }

      // put the new copy in the reference map
      referenceMap.put(source, copy);

      // copy the properties over
      for(Iterator iterator = properties.iterator(); iterator.hasNext(); )
      {
         Property property = (Property)iterator.next();
         Object value = property.get(source);
         value = objectCopier.copy(value, referenceMap);
         property.set(copy, value);
      }
      return copy;
   }

   public void addProperty(Property property)
   {
      if(property.isDestinationConstructorArg())
      {
         constructorArgs.add(property);
      }
      else
      {
         properties.add(property);
      }
   }

   public void addMethodToMethodProperty(
         String sourceMethodName, 
         String destinationMethodName)
   {
      Property property = new Property(type);
      property.setSourceMethodName(sourceMethodName);
      property.setDestinationMethodName(destinationMethodName);
      properties.add(property);
   }
         
   public void addMethodToFieldProperty(
         String sourceMethodName, 
         String destinationFieldName)
   {
      Property property = new Property(type);
      property.setSourceMethodName(sourceMethodName);
      property.setDestinationFieldName(destinationFieldName);
      properties.add(property);
   }
         
   public void addMethodToConstructorArgProperty(
         String sourceMethodName, 
         int destinationConstructorArg)
   {
      Property property = new Property(type);
      property.setSourceMethodName(sourceMethodName);
      property.setDestinationConstructorArg(destinationConstructorArg);
      constructorArgs.add(property);
   }

   public void addFieldToMethodProperty(
         String sourceFieldName, 
         String destinationMethodName)
   {
      Property property = new Property(type);
      property.setSourceFieldName(sourceFieldName);
      property.setDestinationMethodName(destinationMethodName);
      properties.add(property);
   }
         
   public void addFieldToFieldProperty(
         String sourceFieldName, 
         String destinationFieldName)
   {
      Property property = new Property(type);
      property.setSourceFieldName(sourceFieldName);
      property.setDestinationFieldName(destinationFieldName);
      properties.add(property);
   }
         
   public void addFieldToConstructorArgProperty(
         String sourceFieldName, 
         int destinationConstructorArg)
   {
      Property property = new Property(type);
      property.setSourceFieldName(sourceFieldName);
      property.setDestinationConstructorArg(destinationConstructorArg);
      constructorArgs.add(property);
   }
         
   private void initConstructor()
   {
      // get the argument types
      Class[] argTypes = new Class[constructorArgs.size()];
      Iterator constructorArgIterator = constructorArgs.iterator();
      for(int i = 0; constructorArgIterator.hasNext(); i++)
      {
         Property property = (Property)constructorArgIterator.next();
         argTypes[i] = property.getType();
      }

      // get the constructor
      try
      {
         constructor = type.getConstructor(argTypes);
      }
      catch(Exception e)
      {
         String argTypeNames = "[";
         constructorArgIterator = constructorArgs.iterator();
         for(int i = 0; constructorArgIterator.hasNext(); i++)
         {
            if(i > 0)
            {
               argTypeNames += ", ";
            }
            Property property = (Property)constructorArgIterator.next();
            argTypeNames += property.getType();
         }
         argTypeNames += "]";

         throw new CopyException("Class does not have specified constructor:" +
               " type=" + type.getName() +
               " constructorArgTypes=" + argTypeNames);
      }
   }

   private static final class ConstructorArgComparator implements Comparator
   {
      public int compare(Object one, Object two)
      {
         int argOne = ((Property)one).getDestinationConstructorArg();
         int argTwo = ((Property)two).getDestinationConstructorArg();
         
         if(argOne < argTwo)
         {
            return -1;
         }
         else if(argOne == argTwo)
         {
            return 0;
         }
         else
         {
            return 1;
         }
      }

      public boolean equals(Object object)
      {
         return object instanceof ConstructorArgComparator;
      }
   }
}

