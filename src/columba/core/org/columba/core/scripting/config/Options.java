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
package org.columba.core.scripting.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

/**
    @author Celso Pinto (cpinto@yimports.com)
 */
public class Options
    extends DefaultItem
{

    public static final int DEFAULT_POOLING_INTERVAL = 5, INTERVAL_TIME_UNIT = 1000; // seconds

    static final String POLLING_INTERVAL_KEY = "poll_interval", POLLING_ENABLED_KEY = "polling_enabled";

    private static final String POLLING_INTERVAL_PATH = "/" + POLLING_INTERVAL_KEY, POLLING_ENABLED_PATH = "/" + POLLING_ENABLED_KEY;

    private List observers = new ArrayList();

    public Options(XmlElement aRoot)
    {
        super(aRoot);
    }

    public int getPollInterval()
    {
        // TODO: key can't be null
        return getIntegerWithDefault(POLLING_INTERVAL_PATH, null, DEFAULT_POOLING_INTERVAL);
    }

    public boolean isPollingEnabled()
    {
        // TODO: key can't be null
        return getBooleanWithDefault(POLLING_ENABLED_PATH, null, true);
    }

    public void setPollInterval(int interval)
    {
        int old = getPollInterval();

        setInteger(POLLING_INTERVAL_PATH, null, interval);

        if (old != interval)
        {
            for (Iterator it = observers.iterator(); it.hasNext();)
                ((OptionsObserver) it.next()).pollingIntervalChanged(interval);
        }

    }

    public void setPollingEnabled(boolean poll)
    {
        boolean old = isPollingEnabled();
        setBoolean(POLLING_ENABLED_PATH, null, poll);
        if (old != poll)
        {
            for (Iterator it = observers.iterator(); it.hasNext();)
                ((OptionsObserver) it.next()).pollingStateChanged(poll);
        }
    }

    public void addObserver(OptionsObserver observer)
    {
        if (!observers.contains(observer)) observers.add(observer);
    }

    public void removeObserver(OptionsObserver observer)
    {
        observers.remove(observer);
    }

    void setDefaultData()
    {
        setPollInterval(DEFAULT_POOLING_INTERVAL);
        setPollingEnabled(true);
    }

    public int getInternalPollingInterval()
    {
        return getPollInterval() * INTERVAL_TIME_UNIT;
    }
}
