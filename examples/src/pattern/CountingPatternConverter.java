/*
 * Copyright 1999,2005 The Apache Software Foundation.
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

package pattern;

import org.apache.log4j.pattern.LoggingEventPatternConverter;
import org.apache.log4j.spi.LoggingEvent;


public final class CountingPatternConverter extends LoggingEventPatternConverter {
  
  private int counter = 0;
  
  public CountingPatternConverter() {
     super("Count", "count");
  }

  public void format(final LoggingEvent event, final StringBuffer toAppendTo) {
    toAppendTo.append(String.valueOf(++counter));
  }
  
}
