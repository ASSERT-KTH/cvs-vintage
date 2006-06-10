/*
  The contents of this file are subject to the Mozilla Public License Version 1.1
  (the "License"); you may not use this file except in compliance with the 
  License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
  
  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
  for the specific language governing rights and
  limitations under the License.

  The Original Code is "The Columba Project"
  
  The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
  Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
  
  All Rights Reserved.
*/
package org.columba.core.scripting.extensions;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;


/**
  @author Celso Pinto (cpinto@yimports.com)
 */
public class ExtensionEventBus
{
  /*TODO create javadocs for class */
  
  private static ExtensionEventBus self = null;
  private Map eventMap = null;
  
  private ExtensionEventBus()
  {
    eventMap = new TreeMap();
  }
  
  public static ExtensionEventBus getInstance()
  {
    if (self == null)
      self = new ExtensionEventBus();
      
    return self;
  }

  public void registerEvent(String eventId)
  {
    if (!eventMap.containsKey(eventId))
      eventMap.put(eventId,new LinkedList());
      
  }
  
  public void unregisterEvent(String eventId)
  {
    eventMap.remove(eventId);
  }
  
  public void registerListener(ExtensionEventListener listener, String eventId)
  {
  
    registerEvent(eventId);
    List listeners = getListeners(eventId);
    if (listeners == null || listeners.contains(listener))
      return;
      
    listeners.add(listener);
      
  }
  
  public void unregisterListener(ExtensionEventListener listener,String eventId)
  {
    List listeners = getListeners(eventId);
    if (listeners == null)
      return;
      
    listeners.remove(listener);
    
  }
  
  public List getListeners(String eventId)
  {
    return (List)eventMap.get(eventId);
  }
  
  public Enumeration enumAvailableEvents()
  {
    return (new Vector(eventMap.keySet()).elements());
  }
  
  public void fireEvent(ExtensionEvent event)
  {
    /* XXX should this be fired by a different thread? */
    List listeners = getListeners(event.getEventId());
    if (listeners == null)
      return ;
    
    for(Iterator it = listeners.iterator();it.hasNext();)
      ((ExtensionEventListener)it.next()).eventFired(event);
      
  }
}
