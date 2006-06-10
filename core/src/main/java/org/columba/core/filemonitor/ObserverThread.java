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

/*
  TODO
    - make it possible to monitor a directory
    - make it possible to stop monitoring a file
*/

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Celso Pinto <cpinto@yimports.com>
 */
public class ObserverThread
    extends Thread
{

  static final int DEFAULT_POLLING_INTERVAL = 60;

  private final Map<File,Set> watchedFiles;

  private long lastExecution;
  private int pollingInterval;
  private boolean terminate = false;

  public ObserverThread(int interval)
  {

    watchedFiles = new HashMap<File,Set>();
    lastExecution = System.currentTimeMillis();
    pollingInterval = interval;

  }

  public void terminate()
  {
    terminate = true;
  }

  public void monitorFile(File file, FileObserver observer)
  {

    synchronized(watchedFiles)
    {

      Set<FileObserver> observerSet;

      observerSet = watchedFiles.get(file);

      if (observerSet == null)
      {

        observerSet = new HashSet<FileObserver>();
        watchedFiles.put(file,observerSet);

      }
      else
      {

        if (observerSet.contains(observer))
          return;

      }

      observerSet.add(observer);

    }

  }

  public void run()
  {

    while(!terminate)
    {

      performCheck();

      try
      {
        sleep(pollingInterval);
      }
      catch(InterruptedException ex)
      {}

    }

  }

  private void performCheck()
  {

    synchronized(watchedFiles)
    {

      File file;
      Set<FileObserver> observers;

      for(Map.Entry<File,Set> entry : watchedFiles.entrySet())
      {

        file = entry.getKey();
        observers = entry.getValue();

        if (!file.exists())
        {

          for(FileObserver observer : observers)
            observer.fileChanged(file,FileObserver.FILE_REMOVED);

          watchedFiles.remove(file);
          continue;

        }

        if (file.lastModified() <= lastExecution)
          continue;

        for(FileObserver observer : observers)
          observer.fileChanged(file,FileObserver.FILE_CHANGED);

      }

    }

    lastExecution = System.currentTimeMillis();

  }
}
