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


/**
  This class is a class representing ExtensionEvents.
  @author Celso Pinto (cpinto@yimports.com)
 */
public class ExtensionEvent
{

  private String eventId = null;
  private AbstractExtensionPoint source = null;
    
  /**
    Event constructor
    @param source the event source
    @param eventId the event id
  */
  public ExtensionEvent(AbstractExtensionPoint source,String eventId)
  {
    this.source = source;
    this.eventId = eventId;
  }
  
  /**
    Getter for event source
    @return event source
  */
  public AbstractExtensionPoint getSource()
  {
    return source;
  }
  
  /**
    Getter for event id
    @return event id
  */
  public String getEventId()
  {
    return eventId;
  }
  
}
