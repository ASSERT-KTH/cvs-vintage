/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/html/FileTag.java,v 1.4 2004/03/14 06:23:46 sraeburn Exp $
 * $Revision: 1.4 $
 * $Date: 2004/03/14 06:23:46 $
 *
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


package org.apache.struts.taglib.html;


/**
 * Custom tag for input fields of type "file".
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/14 06:23:46 $
 */

public class FileTag extends BaseFieldTag {


    /**
     * Construct a new instance of this tag.
     */
    public FileTag() {

	super();
	this.type = "file";

    }


}
