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
package org.columba.core.filemonitor;

import java.io.File;

/**
 * @author Celso Pinto <cpinto@yimports.com>
 */
public interface FileObserver
{

  /**
    The changeCode is FILE_CHANGED when a file that is being observed (or
    a file contained in an observed directory) has been changed
  */
  public static final int FILE_CHANGED  =  1 << 0;

  /**
    The changeCode is FILE_ADDED when a new file was added to an observed
    directory
  */
  public static final int FILE_ADDED    =  1 << 1;

  /**
    The changeCode is FILE_REMOVED when an observedFile has been removed
    or a file contained in an observed directory has been removed.
  */
  public static final int FILE_REMOVED  =  1 << 2;

  /**
     Notify an observer that a monitored file has changed, was deleted or
     created. This is a synchronous call so implementations must either be fast
     or handle the changes in a separate thread
     @param file the file path
     @param changeCode can be one of FILE_CHANGED,FILE_ADDED,FILE_REMOVED
   */
  public void fileChanged(File file,int changeCode);


  /**
     Notify the observer that the FileMonitorService is being interrupted.
     If the FileMonitorService comes back up at a latter time, it's the
     observer's responsability to attach to it again
   */
  public void stoppingService();

}
