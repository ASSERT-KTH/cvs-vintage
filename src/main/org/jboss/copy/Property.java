/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */
package org.jboss.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 */
public class Property
{
   private Class classType;
   private Class propertyType;
   private Method sourceMethod;
   private Field sourceField;
   private int destinationConstructorArg = -1;
   private Method destinationMethod;
   private Field destinationField;

   public Property(final Class classType)
   {
      this.classType = classType;
   }

   public void setSourceMethodName(String methodName)
   {
      try
      {
         sourceMethod = classType.getMethod(methodName, null);
      }
      catch(Exception e)
      {
         throw new CopyException("Class does not have the specified no " +
               "argument method: methodName=" + methodName);
      }
      propertyType = sourceMethod.getReturnType();
      sourceField = null;
   }

   public void setSourceFieldName(String fieldName)
   {
      try
      {
         sourceField = classType.getField(fieldName);
      }
      catch(Exception e)
      {
         throw new CopyException("Class does not have the specified field: " +
               "fieldName=" + fieldName);
      }
      propertyType = sourceField.getType();
      sourceMethod = null;
   }

   public boolean isDestinationConstructorArg()
   {
      return destinationConstructorArg >= 0;
   }

   public int getDestinationConstructorArg()
   {
      return destinationConstructorArg;
   }

   public void setDestinationConstructorArg(int destinationConstructorArg)
   {
      this.destinationConstructorArg = destinationConstructorArg;
      destinationMethod = null;
      destinationField = null;
   }

   public void setDestinationMethodName(String methodName)
   {
      Method method = null;
      try
      {
         method = classType.getMethod(
               methodName, 
               new Class[] {propertyType});
      }
      catch(Exception e)
      {
         throw new CopyException("Class does not have the specified no " +
               "argument method: methodName=" + methodName);
      }

      if(!method.getReturnType().isAssignableFrom(propertyType))
      {
         throw new CopyException("Deistination field can not be assigned" +
               " from source type: " +
               " methodName=" + methodName +
               " sourceType=" + propertyType.getName());
      }
      destinationMethod = method;
      destinationConstructorArg = -1;
      destinationField = null;
   }

   public void setDestinationFieldName(String fieldName)
   {
      Field field = null;
      try
      {
         field = classType.getField(fieldName);
      }
      catch(Exception e)
      {
         throw new CopyException("Class does not have the specified no " +
               "argument method: fieldName=" + fieldName);
      }

      if(!field.getType().isAssignableFrom(propertyType))
      {
         throw new CopyException("Deistination field can not be assigned" +
               " from source type: " +
               " fieldName=" + fieldName +
               " sourceType=" + propertyType.getName());
      }
      destinationField = field;
      destinationConstructorArg = -1;
      destinationMethod = null;
   }

   public Class getType() 
   {
      return propertyType;
   }

   public Object get(Object source)
   {
      if(sourceMethod != null)
      {
         try
         {
            return sourceMethod.invoke(source, null);
         }
         catch(InvocationTargetException e)
         {
            throw new CopyException("Exception in source method", 
                  e.getTargetException());
         }
         catch(Exception e)
         {
            throw new CopyException("Exception while invoking " +
                  "source method",
                  e);
         }
      }
      else if(sourceField != null)
      {
         try 
         {
            return sourceField.get(source);
         }
         catch(Exception e)
         {
            throw new CopyException("Exception while setting " +
                  "source field",
                  e);
         }
      }
      else
      {
         throw new CopyException("Property does not have a source method " +
               " or field");
      }
   }

   public void set(Object destination, Object value)
   {
      if(destinationMethod != null)
      {
         try
         {
            destinationMethod.invoke(destination, new Object[] {value});
         }
         catch(InvocationTargetException e)
         {
            throw new CopyException("Exception in destination method", 
                  e.getTargetException());
         }
         catch(Exception e)
         {
            throw new CopyException("Exception while invoking " +
                  "destination method",
                  e);
         }
      }
      else if(destinationField != null)
      {
         try 
         {
            destinationField.set(destination, value);
         }
         catch(Exception e)
         {
            throw new CopyException("Exception while setting " +
                  "destination field",
                  e);
         }
      }
      else
      {
         throw new CopyException("Property does not have a destination " +
               " method or field");
      }
   }
}

