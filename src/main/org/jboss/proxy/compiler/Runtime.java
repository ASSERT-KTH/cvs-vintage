/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.proxy.compiler;

import org.jboss.logging.Logger;


/**
 * Manages bytecode assembly for dynamic proxy generation.
 *
 * @author Unknown
 * @version $Revision: 1.1 $
 */
   // this is the only data needed at runtime:
public class Runtime
   extends ClassLoader
{
   public final static String RUNTIME_FN = "runtime";
   
   // These members are common utilities used by ProxyTarget classes.
   // They are all public so they can be linked to from generated code.
   // I.e., they are the runtime support for the code compiled below.
   private ClassLoader parent;

   public Runtime( ClassLoader parent )
   {
      super( parent );
      this.parent = parent;
   }

   Class targetTypes[];
   java.lang.reflect.Method methods[];
   ProxyCompiler compiler;	// temporary!

   public Class[] copyTargetTypes() {
      try {
         return (Class[]) targetTypes.clone();
      } catch (IllegalArgumentException ee) {
         return new Class[0];
      }
   }

   public Object invoke(InvocationHandler invocationHandler, int methodNum, Object values[])
      throws Throwable {
      java.lang.reflect.Method method = methods[methodNum];
      if (method.getName().equals( "writeReplace" ))
         {
            return new ProxyProxy( invocationHandler, copyTargetTypes() );

         }
      return invocationHandler.invoke(null, methods[methodNum], values);
   }


   // the class loading part

   void makeProxyType(ProxyCompiler compiler) {
      
      this.compiler = compiler; // temporary, for use during loading

      byte code[] = compiler.getCode();

      compiler.proxyType = super.defineClass(compiler.getProxyClassName(), code, 0, code.length);
      super.resolveClass(compiler.proxyType);
      // set the Foo$Impl.info pointer to myself
      try {
         java.lang.reflect.Field infoField = compiler.proxyType.getField(RUNTIME_FN);
         infoField.set(null, this);
      } catch (IllegalAccessException ee) {
         throw new RuntimeException("unexpected: "+ee);
      } catch (NoSuchFieldException ee) {
         throw new RuntimeException("unexpected: "+ee);
      }
      compiler = null;
   }

   ClassLoader getTargetClassLoader() {
      return parent;
      /*  for (int i = 0; i < targetTypes.length; i++) {
          ClassLoader cl = targetTypes[i].getClassLoader();
          if (cl != null) {
          return cl;
          }
          }
          return null; */
   }

   public synchronized Class loadClass(String name, boolean resolve)
      throws ClassNotFoundException {
      if (name.endsWith("$Proxy")
          && name.equals(compiler.getProxyClassName())) {
         return compiler.proxyType;
      }
      // delegate to the original class loader
      ClassLoader cl = getTargetClassLoader();
      if (cl == null) {
         return super.findSystemClass(name);
      }
      return cl.loadClass(name);
   }

   public java.io.InputStream getResourceAsStream(String name) {
      // delegate to the original class loader
      ClassLoader cl = getTargetClassLoader();
      if (cl == null) {
         return parent.getSystemResourceAsStream(name);
      }
      return cl.getResourceAsStream(name);
   }

   public java.net.URL getResource(String name) {
      // delegate to the original class loader
      ClassLoader cl = getTargetClassLoader();
      if (cl == null) {
         return parent.getSystemResource(name);
      }
      return cl.getResource(name);
   }

}
