/*
 * $Header: /tmp/cvs-vintage/struts/src/examples/org/apache/struts/webapp/examples/CustomFormBean.java,v 1.2 2004/03/14 06:23:50 sraeburn Exp $
 * $Revision: 1.2 $
 * $Date: 2004/03/14 06:23:50 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
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


package org.apache.struts.webapp.examples;

import org.apache.struts.config.FormBeanConfig;

/**
 * Custom FormBeanConfig to demonstrate usage.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/14 06:23:50 $
 */

public final class CustomFormBean extends FormBeanConfig {


    // --------------------------------------------------- Instance Variables


    /**
     * An example String property.
     */
    private String example = "";


    // ----------------------------------------------------------- Properties


    /**
     * Return the example String.
     */
    public String getExample() {

	return (this.example);

    }


    /**
     * Set the example String.
     *
     * @param example The new example String.
     */
    public void setExample(String example) {

        this.example = example;

    }

}
