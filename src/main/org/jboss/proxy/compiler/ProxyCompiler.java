/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.proxy.compiler;

import org.jboss.logging.Logger;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.Type;

/**
 * Manages bytecode assembly for dynamic proxy generation.
 *
 * @author Unknown
 * @version $Revision: 1.2 $
 */
public class ProxyCompiler
{

   // Constants -----------------------------------------------------

   public final static  String IMPL_SUFFIX = "$Proxy";
   
   // Attributes ----------------------------------------------------
      
   Class superclass;
   Runtime runtime;
   Class targetTypes[];
   java.lang.reflect.Method methods[];

   Class proxyType;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------

   /**
    * Creates a new <code>ProxyCompiler</code> instance.
    *
    * @param parent a <code>ClassLoader</code> value
    * @param superclass a <code>Class</code> value
    * @param targetTypes[] a <code>Class</code> value
    * @param methods[] a <code>java.lang.reflect.Method</code> value
    */
   ProxyCompiler(ClassLoader parent,
                 Class superclass,
                 Class targetTypes[],
                 java.lang.reflect.Method methods[])
   {
      this.superclass = superclass;
      this.targetTypes = targetTypes;
      this.methods = methods;

      this.runtime = new Runtime( parent );
      this.runtime.targetTypes = targetTypes;
      this.runtime.methods = methods;

      runtime.makeProxyType(this);
   }

   // Public --------------------------------------------------------

   
   // Package protected ---------------------------------------------

   Class getProxyType() {
      return proxyType;
   }
   
   String getProxyClassName() {
      // Note:  We could reasonably put the $Impl class in either
      // of two packges:  The package of Proxies, or the same package
      // as the target type.  We choose to put it in same package as
      // the target type, to avoid name encoding issues.
      //
      // Note that all infrastructure must be public, because the
      // $Impl class is inside a different class loader.
      String tName = targetTypes[0].getName();
      /*
	String dName = Dispatch.class.getName();
	String pkg = dName.substring(0, 1 + dName.lastIndexOf('.'));
	return pkg + tName.substring(1 + tName.lastIndexOf('.')) + IMPL_SUFFIX;
      */
      return tName + IMPL_SUFFIX;
   }
      
   /**
    * Create the implementation class for the given target.
    *
    * @return a <code>byte[]</code> value
    */
   byte[] getCode() {

      final String proxyClassName = getProxyClassName();
      final String superClassName = superclass.getName();
      int icount = 1;		// don't forget ProxyTarget
      for (int i = 0; i < targetTypes.length; i++) {
         Class targetType = targetTypes[i];
         if (targetType.isInterface()) {
            icount++;
         }
      }
      String interfaceNames[] = new String[icount];
      interfaceNames[0] = Proxies.ProxyTarget.class.getName();
      icount = 1;
      for (int i = 0; i < targetTypes.length; i++) {
         Class targetType = targetTypes[i];
         if (targetType.isInterface()) {
            interfaceNames[icount++] = targetType.getName();
         } else if (!superclass.isAssignableFrom(targetType)) {
            throw new RuntimeException("unexpected: " + targetType);
         }
      }

      ClassGen cg = new ClassGen(proxyClassName,
                                 superClassName,
                                 "<generated>",
                                 Constants.ACC_PUBLIC | Constants.ACC_FINAL,
				 interfaceNames);
      
      ProxyImplementationFactory factory = new ProxyImplementationFactory(superClassName, proxyClassName, cg);

      cg.addField(factory.createInvocationHandlerField());
      cg.addField(factory.createRuntimeField());


      cg.addMethod(factory.createConstructor());
      
      // ProxyTarget implementation

      cg.addMethod(factory.createGetInvocationHandler());

      cg.addMethod(factory.createGetTargetTypes());
           
      boolean haveToString = false;
      // Implement the methods of the target types.
      for (int i = 0; i < methods.length; i++) {
         
         java.lang.reflect.Method m = methods[i];
         String name = m.getName();
         Class rTypeClass = m.getReturnType();
         String rTypeName = rTypeClass.getName();
         Type rType = Utility.getType(rTypeClass);
         Type[] pTypes = Utility.getTypes(m.getParameterTypes());

         String[] exceptionNames = getNames(m.getExceptionTypes());

         if (name.equals("toString") && pTypes.length == 0) {
            haveToString = true;
          }

         cg.addMethod(factory.createProxyMethod(name,
                                                i,
                                                rType,
                                                pTypes,
                                                exceptionNames));
      }

      if (!haveToString) {
         cg.addMethod(factory.createToString());
      }

      /*
      try {
         cg.getJavaClass().dump("/tmp/" + proxyClassName + ".class");
      } catch ( java.io.IOException e ) {
      }
      */
      
      return cg.getJavaClass().getBytes();
   }
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   private String[] getNames(Class[] classes) {
      String[] names = new String[classes.length];
      for ( int i = 0;  i < classes.length;  i++ ) {
         names[i] = classes[i].getName();
      }

      return names;
   }
   
   // Inner classes -------------------------------------------------
      
}
