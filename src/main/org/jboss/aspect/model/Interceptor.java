package org.jboss.aspect.model;

import java.util.Collection;
import java.util.LinkedList;

public class Interceptor
{
   String classname;
   
   LinkedList attributes = new LinkedList();
   LinkedList interfaceFilters = new LinkedList();
   LinkedList methodFilters = new LinkedList();
   
   public Collection getAttributes() {
      return attributes;
   }     
   public void add( Attribute attribute) {
      attributes.add( attribute );
   }
   public void remove( Attribute attribute) {
      attributes.remove( attribute );
   }
   

   public Collection getInterfaceFilters() {
      return interfaceFilters;
   }
   public void add( InterfaceFilter i) {
      interfaceFilters.add( i );
   }
   public void remove( InterfaceFilter i) {
      interfaceFilters.remove( i );
   }
   
   public Collection getMethodFilters() {
      return methodFilters;
   }     
   public void add( MethodFilter m) {
      methodFilters.add( m );
   }
   public void remove( MethodFilter m) {
      methodFilters.remove( m );
   }

   
   public String getClassname()
   {
      return classname;
   }

   public void setClassname(String classname)
   {
      this.classname = classname;
   }

}
