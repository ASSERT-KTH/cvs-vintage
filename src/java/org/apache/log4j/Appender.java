/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.log4j;

import org.apache.log4j.spi.Component;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;


/**
 * Implement this interface for your own strategies for outputting log
 * statements.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public interface Appender extends Component {
  
  /**
   * An appender must be activated before it can be used.
   */
  public void activate();
  
  /**
   * Add a filter to the end of the filter list.
   *
   * @since 0.9.0
   */
  void addFilter(Filter newFilter);

  /**
   * Returns the head Filter. The Filters are organized in a linked list and
   * so all Filters on this Appender are available through the result.
   *
   * @return the head Filter or null, if no Filters are present
   *
   * @since 1.1
   */
  public Filter getFilter();

  /**
   * Clear the list of filters by removing all the filters in it.
   *
   * @since 0.9.0
   */
  public void clearFilters();

  /**
   * Release any resources allocated within the appender such as file handles,
   * network connections, etc.
   * 
   * <p>
   * It is a programming error to append to a closed appender.
   * </p>
   *
   * @since 0.8.4
   */
  public void close();

  
  /**
   * Is this appender closed?
   *
   * @since 1.3
   */
  public boolean isClosed();

  /**
   * Is this appender in working order?
   *
   * @since 1.3
   */
  public boolean isActive();
  
  
  /**
   * Log in <code>Appender</code> specific way. When appropriate, Loggers will
   * call the <code>doAppend</code> method of appender implementations in
   * order to log.
   */
  public void doAppend(LoggingEvent event);

  /**
   * Get the name of this appender. The name uniquely identifies the appender.
   */
  public String getName();

  /**
   * Set the {@link ErrorHandler} for this appender.
   *
   * @since 0.9.0
   */
  //public void setErrorHandler(ErrorHandler errorHandler);

  /**
   * Returns the {@link ErrorHandler} for this appender.
   *
   * @since 1.1
   */
  //public ErrorHandler getErrorHandler();

  /**
   * Set the {@link Layout} for this appender.
   *
   * @since 0.8.1
   */
  public void setLayout(Layout layout);

  /**
   * Returns this appenders layout.
   *
   * @since 1.1
   */
  public Layout getLayout();

  /**
   * Set the name of this appender. The name is used by other components to
   * identify this appender.
   *
   * @since 0.8.1
   */
  public void setName(String name);
  


  public void setLoggerRepository(LoggerRepository repository) throws IllegalStateException;
  
}
