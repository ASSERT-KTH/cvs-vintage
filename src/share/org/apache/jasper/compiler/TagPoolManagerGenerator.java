/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/TagPoolManagerGenerator.java,v 1.6 2004/02/23 06:22:36 billbarker Exp $
 *
 *
 *  Copyright 1999-2004 The Apache Software Foundation
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

package org.apache.jasper.compiler;

import org.apache.jasper.runtime.TagPoolManager;
import org.apache.jasper.Constants;

/**
 * This class generates code during the initilization phase that
 * declares and attempts to obtain a TagPoolManager.
 *
 * @author Casey Lucas <clucas@armassolutions.com>
 * @see org.apache.jasper.runtime.TagPoolManager
 */
public class TagPoolManagerGenerator extends GeneratorBase
    implements InitMethodPhase {

    /**
     * variable name generated into java file
     */
    public static final String MANAGER_VARIABLE = "tagPoolManager";

    /**
     * Generate ref to TagPoolManager
     *
     * @param writer
     * @param phase
     */
    public void generate(ServletWriter writer, Class phase) {
        if (InitMethodPhase.class.isAssignableFrom(phase)) {
            writer.println(Constants.JSP_RUNTIME_PACKAGE +
			   ".TagPoolManager " + MANAGER_VARIABLE + " =");
            writer.pushIndent();
            // writer.println("org.apache.jasper.runtime.TagPoolManager.getDefaultPoolManager();");
            writer.println("(" + Constants.JSP_RUNTIME_PACKAGE
			   + ".TagPoolManager) getServletConfig().getServletContext().getAttribute(\"" +
                TagPoolManager.CONTEXT_ATTRIBUTE_NAME + "\");");
            writer.popIndent();
        }
    }
}
