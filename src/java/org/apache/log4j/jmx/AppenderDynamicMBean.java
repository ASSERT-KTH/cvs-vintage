
package org.apache.log4j.jmx;

import java.lang.reflect.Constructor;
import org.apache.log4j.*;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.OptionHandler;

import java.util.Vector;
import java.util.Hashtable;
import java.lang.reflect.Method;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.Attribute;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;

import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.RuntimeOperationsException;
import javax.management.ReflectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import java.beans.Introspector;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

public class AppenderDynamicMBean extends AbstractDynamicMBean {

  private MBeanConstructorInfo[] dConstructors = new MBeanConstructorInfo[1];
  private Vector dAttributes = new Vector();
  private String dClassName = this.getClass().getName();

  private Hashtable dynamicProps = new Hashtable(5);
  private MBeanOperationInfo[] dOperations = new MBeanOperationInfo[2];
  private String dDescription = 
     "This MBean acts as a management facade for log4j appenders.";

  // This category instance is for logging.
  private static Logger cat = Logger.getLogger(AppenderDynamicMBean.class);

  // We wrap this appender instance.
  private Appender appender;

  public  AppenderDynamicMBean(Appender appender) throws IntrospectionException {
    this.appender = appender;
    buildDynamicMBeanInfo();
  }

  private 
  void buildDynamicMBeanInfo() throws IntrospectionException {
    Constructor[] constructors = this.getClass().getConstructors();
    dConstructors[0] = new MBeanConstructorInfo(
             "AppenderDynamicMBean(): Constructs a AppenderDynamicMBean instance",
	     constructors[0]);


    BeanInfo bi = Introspector.getBeanInfo(appender.getClass());
    PropertyDescriptor[] pd = bi.getPropertyDescriptors();
    
    int size = pd.length;

    for(int i = 0; i < size; i++) {
      String name = pd[i].getName();
      Method readMethod =  pd[i].getReadMethod();
      Method writeMethod =  pd[i].getWriteMethod();
      if(readMethod != null) {
	Class returnClass = readMethod.getReturnType();
	if(isSupportedType(returnClass)) {
	  String returnClassName;
	  if(returnClass.isAssignableFrom(Priority.class)) {
	    returnClassName = "java.lang.String";
	  } else {
	    returnClassName = returnClass.getName();
	  }
	  
	  dAttributes.add(new MBeanAttributeInfo(name,
						 returnClassName,
						 "Dynamic",
						 true,
						 writeMethod != null,
						 false));
	  dynamicProps.put(name, new B(readMethod, writeMethod));
	}      
      }
    }

    MBeanParameterInfo[] params = new MBeanParameterInfo[0];

    dOperations[0] = new MBeanOperationInfo("activateOptions",
					    "activateOptions(): add an appender",
					    params, 
					    "void", 
					    MBeanOperationInfo.ACTION);

    params = new MBeanParameterInfo[1];
    params[0] = new MBeanParameterInfo("layout class", "java.lang.String", 
				       "layout class");

    dOperations[1] = new MBeanOperationInfo("setLayout",
					    "setLayout(): add a layout",
					    params, 
					    "void", 
					    MBeanOperationInfo.ACTION);
  }

  private
  boolean isSupportedType(Class clazz) {
    if(clazz.isPrimitive()) {
      return true;
    }

    if(clazz == String.class) {
      return true;
    }


    if(clazz.isAssignableFrom(Priority.class)) {
      return true;
    }
    
    return false;

    
  }



  public 
  MBeanInfo getMBeanInfo() {
    cat.debug("getMBeanInfo called.");

    MBeanAttributeInfo[] attribs = new MBeanAttributeInfo[dAttributes.size()];
    dAttributes.toArray(attribs);

    return new MBeanInfo(dClassName,
			 dDescription,
			 attribs,
			 dConstructors,
			 dOperations,
			 new MBeanNotificationInfo[0]);
  }

  public 
  Object invoke(String operationName, Object params[], String signature[])
    throws MBeanException,
    ReflectionException {
   
    if(operationName.equals("activateOptions") && 
                     appender instanceof OptionHandler) {
      OptionHandler oh = (OptionHandler) appender;
      oh.activateOptions();
      return "Options activated.";
    } else if (operationName.equals("setLayout")) {
      Layout layout = (Layout) OptionConverter.instantiateByClassName((String)
								      params[0],
								      Layout.class,
								      null);
      appender.setLayout(layout);
      registerLayoutMBean(layout);
    }
    return null;
  }

  void registerLayoutMBean(Layout layout) {
    if(layout == null)
      return;

    String name = appender.getName()+",layout="+layout.getClass().getName();
    cat.debug("Adding LayoutMBean:"+name);
    ObjectName objectName = null;
    try {
      LayoutDynamicMBean appenderMBean = new LayoutDynamicMBean(layout);
      objectName = new ObjectName("log4j:appender="+name);
      server.registerMBean(appenderMBean, objectName);
      
      dAttributes.add(new MBeanAttributeInfo("appender="+name,
					     "javax.management.ObjectName",
					     "The "+name+" layout.",
					     true,
					     true,
					     false));
      
    } catch(Exception e) {
      cat.error("Could not add DynamicLayoutMBean for ["+name+"].", e);
    }
  }

  protected
  Logger getLogger() {
    return cat;
  }
  

  public 
  Object getAttribute(String attributeName) throws AttributeNotFoundException, 
                                                   MBeanException, 
                                                   ReflectionException {

       // Check attributeName is not null to avoid NullPointerException later on
    if (attributeName == null) {
      throw new RuntimeOperationsException(new IllegalArgumentException(
			"Attribute name cannot be null"), 
       "Cannot invoke a getter of " + dClassName + " with null attribute name");
    }
    
    cat.debug("getAttribute called with ["+attributeName+"].");
    if(attributeName.startsWith("appender="+appender.getName()+",layout")) {
      try {
	return new ObjectName("log4j:"+attributeName );
      } catch(Exception e) {
	cat.error("attributeName", e);
      }
    }

    B b = (B) dynamicProps.get(attributeName);

    //cat.debug("----name="+attributeName+", b="+b);

    if(b != null && b.readMethod != null) {
      try {
	return b.readMethod.invoke(appender, null);
      } catch(Exception e) {
	return null;
      }
    }



    // If attributeName has not been recognized throw an AttributeNotFoundException
    throw(new AttributeNotFoundException("Cannot find " + attributeName + 
					 " attribute in " + dClassName));

  }


  public 
  void setAttribute(Attribute attribute) throws AttributeNotFoundException,
                                                InvalidAttributeValueException,
                                                MBeanException, 
                                                ReflectionException {
    
    // Check attribute is not null to avoid NullPointerException later on
    if (attribute == null) {
      throw new RuntimeOperationsException(
                  new IllegalArgumentException("Attribute cannot be null"), 
		  "Cannot invoke a setter of " + dClassName + 
		  " with null attribute");
    }
    String name = attribute.getName();
    Object value = attribute.getValue();
    
    if (name == null) {
      throw new RuntimeOperationsException(
                    new IllegalArgumentException("Attribute name cannot be null"), 
		    "Cannot invoke the setter of "+dClassName+
		    " with null attribute name");
    }
    

    
    B b = (B) dynamicProps.get(name);

    if(b != null && b.writeMethod != null) {
      Object[] o = new Object[1];

      Class[] params = b.writeMethod.getParameterTypes();
      if(params[0] == org.apache.log4j.Priority.class) {
	value = OptionConverter.toLevel((String) value, 
					(Level) getAttribute(name));
      }
      o[0] = value;

      try {
	b.writeMethod.invoke(appender,  o);
	
      } catch(Exception e) {
	cat.error("FIXME", e);
      }
    } else if(name.endsWith(".layout")) {
      
    } else {
      throw(new AttributeNotFoundException("Attribute " + name +
					   " not found in " + 
					   this.getClass().getName()));
    }
  }  

  public 
  ObjectName preRegister(MBeanServer server, ObjectName name) {
    cat.debug("preRegister called. Server="+server+ ", name="+name);
    this.server = server;
    registerLayoutMBean(appender.getLayout());

    return name;
  }


}


  class B {
    Method readMethod;
    Method writeMethod;

    B(Method readMethod, Method writeMethod) {
      this.readMethod = readMethod;
      this.writeMethod = writeMethod;
    }
    
  }
