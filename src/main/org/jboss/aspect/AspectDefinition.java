/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.aspect;

import gnu.regexp.RE;
import gnu.regexp.REException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.aspect.model.Aspect;
import org.jboss.aspect.model.Attribute;
import org.jboss.aspect.model.Interceptor;
import org.jboss.aspect.model.InterfaceFilter;
import org.jboss.aspect.model.MethodFilter;
import org.jboss.aspect.proxy.AspectInitizationException;
import org.jboss.util.Classes;

/**
 * The AspectDefinition holds the aspect definition for a single aspect.
 * <p>
 * 
 * Multiple instances of the same aspect will share the AspectDefinition
 * configuration object.  This class is immutable and, therefore, threadsafe.
 * <p>
 * 
 * AspectDefinition objects can be dynamicaly created at runtime and passed
 * to the AspectFactory to create dynamicaly generated aspects.
 * <p>
 * 
 * @see org.jboss.aspect.AspectFactory#createAspect(AspectDefinition)
 *
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
final public class AspectDefinition {
	
   
	/**
	 * Constructor.
	 * 
	 * @param name The name of the aspect.
	 * @param interceptorNames The list of interceptors 
	 *         that will be used to compose the aspect.
	 * @param config The configurations that will be used to 
	 *         initialize the interceptors
	 * @param targetClassName The name of the optinal aspectObject class that
	 *         this aspect will operate on, can be null.
	 */
	public AspectDefinition(Aspect aspectModel) throws AspectInitizationException {
		this.name = aspectModel.getName();
		ClassLoader cl = Classes.getContextClassLoader();
      
		try {
			this.targetClass = aspectModel.getTargetClass()==null ? null : cl.loadClass(aspectModel.getTargetClass());
		} catch ( Exception e ) {
			throw new AspectInitizationException("Invalid Aspect aspectObject class : "+aspectModel.getTargetClass()+": "+e);
		}
		
      //
      // Create and initialize the interceptors.
      //
		this.interceptors = new IAspectInterceptor[aspectModel.getInterceptors().size()];            
      Iterator iter = aspectModel.getInterceptors().iterator();
		for( int i=0; iter.hasNext() ; i++ ) {
         Interceptor interceptor = (Interceptor)iter.next();
			try {
				this.interceptors[i] = (IAspectInterceptor)cl.loadClass(interceptor.getClassname()).newInstance();
			} catch ( Exception e ) {
				throw new AspectInitizationException("Invalid Aspect Interceptor: "+interceptor.getClassname()+": "+e);
			}
         
         Map config = getConfigMapFor( interceptor );
			this.interceptors[i].init(config);
		}

      //
      // Build the exposed interface list
      //
      this.filteredMethods = new Set[aspectModel.getInterceptors().size()];
      ArrayList interfaceList = new ArrayList(interceptors.length);
      iter = aspectModel.getInterceptors().iterator();
      for( int i=0; iter.hasNext() ; i++ ) {
         Interceptor interceptor = (Interceptor)iter.next();

         ArrayList localExposedList = new ArrayList();
         // 
         // Find out what interfaces were exposed by the interceptor
         //
         if( interceptor.getInterfaceFilters().size() > 0 ) {
   
            // Apply the interface filter now (if needed)
            Iterator iter2 = interceptor.getInterfaceFilters().iterator();
            while( iter2.hasNext() ) {
               InterfaceFilter o  = (InterfaceFilter) iter2.next();
               try {
                  Class t = cl.loadClass(o.getClassname());
   
                  checkExposed( interceptors[i].getInterfaces(), t, "Invalid Aspect Interceptor Interface Filter: "+o.getClassname()+", was not exposed by the interceptor.");
                  
                  // Don't add an interface 2 times...
                  if( interfaceList.contains(t) )
                     continue;
                  
               } catch ( Exception e ) {
                  throw new AspectInitizationException("Invalid Aspect Interceptor: "+o.getClassname()+": "+e);
               }                              
            }
         } else {
            Class interceptorExposed[] = interceptors[i].getInterfaces();
            for( int j=0; j < interceptorExposed.length; j++ ) {
               
               // Don't add an interface 2 times...
               if( interfaceList.contains(interceptorExposed[j]) )
                  continue;
                  
               interfaceList.add( interceptorExposed[j] );
            }
         }
         
         //
         // Add the Interfaces that were exposed by the interceptor
         //         
         Iterator j = localExposedList.iterator();
         while( j.hasNext() ) {
            Object t = j.next();
            
            // Don't add an interface 2 times...
            if( interfaceList.contains(t) )
               continue;
            interfaceList.add(t);
         }
         

         //
         // Build a method filter for the interfaces exposed
         // by this interceptor.
         //
         if( interceptor.getMethodFilters().size() != 0 ) {

            // Build a list of all the methods in the exposed 
            // interfaces
            ArrayList allMethods = new ArrayList();
            j = localExposedList.iterator();
            while( j.hasNext() ) {
               Class t = (Class)j.next();
               
               Method ms[] = t.getMethods();
               for( int k=0; k < ms.length; k++ ) {
                  allMethods.add(ms[k]);
               }
            }

            HashSet exposedMethods = new HashSet();
            Iterator iter2 = interceptor.getMethodFilters().iterator();
            while( iter2.hasNext() ) {
               MethodFilter mf  = (MethodFilter)iter2.next();
               Collection f = applyMethodFilter( mf.getSignature(), allMethods );
               Iterator mfi = f.iterator();
               while( mfi.hasNext() ) {
                  Object o = mfi.next();
                  if( !exposedMethods.contains( o ) )
                     exposedMethods.add( o );
               }
            }
            this.filteredMethods[i] = exposedMethods;
         }         
      }
      interfaces = new Class[interfaceList.size()];
      interfaceList.toArray(interfaces);      

	}

   private Map getConfigMapFor( Interceptor interceptor ) {
      HashMap m = new HashMap();
      Iterator i = interceptor.getAttributes().iterator();
      while( i.hasNext() ) {
         Attribute a = (Attribute)i.next();
         m.put( a.getName(), a.getValue());
      }
      return m;
      
   }

   private void checkExposed(Class list[], Class needle, String errorMessage) throws AspectInitizationException {
      for( int i=0; i < list.length; i++ ) 
         if( needle.equals(list[i]) )
            return;
      throw new AspectInitizationException(errorMessage);
   }
    
   private Collection applyMethodFilter( String filter, Collection methods ) throws AspectInitizationException {
      ArrayList rc = new ArrayList();
      
      try {      
         
         RE filterRE = new RE(filter);
         
         Iterator i = methods.iterator();
         while( i.hasNext() ) {
            Method m = (Method)i.next();
            if( filterRE.isMatch( m.getName()) )
               rc.add( m );
         }
         
      } catch(REException e) {
         throw new AspectInitizationException("Failed to init regular expresion: "+filter);
      }
      
      return rc;
   }
   
   /**
    * Constructor.
    */
   public AspectDefinition(String name, IAspectInterceptor[] interceptors, Class interfaces[], Set filteredMethods[], Class targetClass) {
      this.name=name;
      this.interceptors=interceptors;      
      this.filteredMethods=filteredMethods;
      this.targetClass=targetClass;
      this.interfaces = interfaces;
      
      if( interceptors == null )
         throw new NullPointerException("interceptors cannot be null");
      if( filteredMethods == null )
         throw new NullPointerException("interceptorFilters cannot be null");
      if( interceptors.length != filteredMethods.length )
         throw new IllegalArgumentException("interceptorFilters cannot be null");
   }
   
	final public String                     name;
	final public IAspectInterceptor 		    interceptors[];
   final public Set                        filteredMethods[];
	final public Class                      interfaces[];
	final public Class                      targetClass;
}
