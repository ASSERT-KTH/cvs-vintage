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
 
  Abstract class for extension points. <br>
  Developers should ensure that their Extension Points provide a unique identifier
  and, if possible, make those identifiers easy to find.<br>
  <br>
  TODO maybe create a unique place to define extension point id's?
  
  @author Celso Pinto (cpinto@yimports.com)
  
 */
public abstract class AbstractExtensionPoint
{
  /* TODO add javadocs */
  
  /**
    Use to indicate that the action must be inserted at the beginning of the
    action stack
  */
  public static final int POSITION_BEGINNING  = 1 << 0;
  
  /**
    Use to indicate that the action must be added at the end of the action
    stack.
  */
  public static final int POSITION_END        = 1 << 1;
  
  private String id = null;
  
  /**
    Default constructor. Every extension point must garantee that it's identifier
    is unique.
    @param id the extension point identifier
  */
  public AbstractExtensionPoint(String id)
  {
    this.id = id;
  }
  
  /**
    Getter for the extension point id
    @return the extension point id
  */
  public String getId()
  {
    return id;
  }
  
}
