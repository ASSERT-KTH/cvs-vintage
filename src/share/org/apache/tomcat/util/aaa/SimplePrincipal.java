/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.util.aaa;

import java.security.Principal;

public class SimplePrincipal implements Principal {
    private String name;

    public SimplePrincipal(String name) {
	this.name = name;
    }

    /**
     * Returns true if the specified Object represents the
     * same principal (i.e. a Principal with the same name)
     *
     * @param another Another Principal instance
     * @return true if another is a Principal with the same name
     */
    public boolean equals(Object another) {
	return another instanceof Principal &&
	    ((Principal) another).getName().equals(getName());
    }
    
    /**
     * Returns the principal's name.
     *
     * @return The principal's name
     */
    public String getName() {
	return name;
    }
    
    /**
     * Returns the principal's name.
     *
     * @return The principal's name
     */
    public String toString() {
	return getName();
    }
}
