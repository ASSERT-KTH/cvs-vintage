/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/mock/MockFormBean.java,v 1.4 2004/03/14 06:23:52 sraeburn Exp $
 * $Revision: 1.4 $
 * $Date: 2004/03/14 06:23:52 $
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


package org.apache.struts.mock;


import java.util.HashMap;
import java.util.Map;
import org.apache.struts.action.ActionForm;


/**
 * <p>General purpose form bean for unit tests.</p>
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/14 06:23:52 $
 */

public class MockFormBean extends ActionForm {


    public MockFormBean() {
        this(null);
    }


    public MockFormBean(String stringProperty) {
        this.stringProperty = stringProperty;
    }


    protected boolean booleanProperty = false;

    public boolean getBooleanProperty() {
        return (this.booleanProperty);
    }

    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }


    public Map getMapProperty() {
        HashMap map = new HashMap();
        map.put("foo1", "bar1");
        map.put("foo2", "bar2");
        return (map);
    }


    protected String stringProperty = null;

    public String getStringProperty() {
        return (this.stringProperty);
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }


}
