/*
 * $Header: /tmp/cvs-vintage/struts/src/test/org/apache/struts/upload/MultipartTestSuite.java,v 1.4 2004/03/14 06:23:53 sraeburn Exp $
 * $Revision: 1.4 $
 * $Date: 2004/03/14 06:23:53 $
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

package org.apache.struts.upload;

import junit.framework.Test;
import junit.framework.TestSuite;

public class MultipartTestSuite extends TestSuite {

    public static final void main(String args[]) {
        junit.textui.TestRunner.run(MultipartTestSuite.suite());
        System.exit(0);
    }

    public static Test suite() {
        TestSuite suite = new MultipartTestSuite();
        suite.addTest(new TestSuite(MultipartBoundaryInputStreamTest.class));

        return suite;
    }

}
