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

package org.apache.tomcat.util.io;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class RecycleBufferedInputStream extends BufferedInputStream {
    public RecycleBufferedInputStream( InputStream is ) {
	super( is );
    }

    public void setInputStream( InputStream is ) {
	this.count=0;
	this.in=is;
    }

    public void recycle() {
	this.in=null;
	this.count=0;
    }
}
