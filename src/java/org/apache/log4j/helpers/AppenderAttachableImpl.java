/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */

package org.apache.log4j.helpers;

import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;

import org.apache.log4j.Appender;
import java.util.Vector;
import java.util.Enumeration;

/**
   A straightforward implementation of the {@link AppenderAttachable}
   interface.

   @author Ceki G&uuml;lc&uuml;
   @since version 0.9.1 */
public class AppenderAttachableImpl implements AppenderAttachable {
  
  /** Array of appenders. */
  protected Vector  appenderList;

  /**
     Attach an appender. If the appender is already in the list in
     won't be added again.
  */
  public
  void addAppender(Appender newAppender) {
    // Null values for newAppender parameter are strictly forbidden.
    if(newAppender == null)
      return;
    
    if(appenderList == null) {
      appenderList = new Vector(1);
    }
    if(!appenderList.contains(newAppender))
      appenderList.addElement(newAppender);
  }

  /**
     Call the <code>doAppend</code> method on all attached appenders.  */
  public
  int appendLoopOnAppenders(LoggingEvent event) {
    int size = 0;
    Appender appender;

    if(appenderList != null) {
      size = appenderList.size();
      for(int i = 0; i < size; i++) {
	appender = (Appender) appenderList.elementAt(i);
	appender.doAppend(event);
      }
    }    
    return size;
  }


  /**
     Get all attached appenders as an Enumeration. If there are no
     attached appenders <code>null</code> is returned.
     
     @return Enumeration An enumeration of attached appenders.
   */
  public
  Enumeration getAllAppenders() {
    if(appenderList == null)
      return null;
    else 
      return appenderList.elements();    
  }

  /**
     Look for an attached appender named as <code>name</code>.

     <p>Return the appender with that name if in the list. Return null
     otherwise.  
     
   */
  public
  Appender getAppender(String name) {
     if(appenderList == null || name == null)
      return null;

     int size = appenderList.size();
     Appender appender;
     for(int i = 0; i < size; i++) {
       appender = (Appender) appenderList.elementAt(i);
       if(name.equals(appender.getName()))
	  return appender;
     }
     return null;    
  }

  /**
     Remove all previously attached appenders.
  */
  public
  void removeAllAppenders() {
    if(appenderList != null) {
      int len = appenderList.size();      
      for(int i = 0; i < len; i++) {
	Appender a = (Appender) appenderList.elementAt(i);
	a.close();
      }
      appenderList.removeAllElements();
      appenderList = null;      
    }
  }


  /**
     Remove the appender passed as parameter form the list of attached
     appenders.  */
  public
  void removeAppender(Appender appender) {
    if(appender == null || appenderList == null) 
      return;
    appenderList.removeElement(appender);    
  }


 /**
    Remove the appender with the name passed as parameter form the
    list of appenders.  
  */
  public
  void removeAppender(String name) {
    if(name == null || appenderList == null) return;
    int size = appenderList.size();
    for(int i = 0; i < size; i++) {
      if(name.equals(((Appender)appenderList.elementAt(i)).getName())) {
	 appenderList.removeElementAt(i);
	 break;
      }
    }
  }

}
