package org.jboss.aspect.model;

import java.util.Collection;
import java.util.LinkedList;

public class Aspect
{
   String name;
   String targetClass;
   LinkedList interceptors = new LinkedList();

   public Collection getInterceptors()
   {
      return interceptors;
   }
   public void add( Interceptor i) {
      interceptors.add( i );
   }
   public void remove( Interceptor i) {
      interceptors.remove( i );
   }

   public String getName()
   {
      return name;
   }

   public String getTargetClass()
   {
      return targetClass;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public void setTargetClass(String targetClass)
   {
      this.targetClass = targetClass;
   }

}
