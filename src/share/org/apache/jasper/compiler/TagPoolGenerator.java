/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/compiler/TagPoolGenerator.java,v 1.5 2004/02/23 06:22:36 billbarker Exp $
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

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import org.apache.jasper.Constants;

/**
 * This class generates tag pooling related information.  Specifically,
 * it generates code to declare tag pools and to obtain tag pools
 * during jsp initialization.
 *
 * @author Casey Lucas <clucas@armassolutions.com>
 * @see org.apache.jasper.runtime.TagPoolManager
 */
public class TagPoolGenerator extends GeneratorBase
    implements ClassDeclarationPhase, InitMethodPhase {

    // Tag related info.
    // Some of them aren't used now, but might be in the future.
    private String prefix;
    private String shortTagName;
    private TagLibraryInfo tli;
    private TagInfo ti;
    private Hashtable attrs;

    // Computed pool name and pool name as a valid variable name
    private String poolName;
    private String poolVarName;

    private final static String POOL_VARIABLE_NAME_PREFIX = "_jspx_tagPool_";
    /**
     * No default constructor.
     */
    private TagPoolGenerator() {
    }

    /**
     * Common constructor with enough information to generate code.
     *
     * @param prefix
     * @param shortTagName
     * @param attrs
     * @param tli
     * @param ti
     */
    public TagPoolGenerator(String prefix, String shortTagName,
                           Hashtable attrs, TagLibraryInfo tli,
                           TagInfo ti) {
        this.prefix = prefix;
        this.shortTagName = shortTagName;
        this.tli = tli;
        this.ti = ti;
        this.attrs = attrs;
        this.poolName = getPoolName(tli, ti, attrs);
        this.poolVarName = getPoolVariableName(poolName);
    }


    /**
     * This method returns a unique pool name based on the given
     * TagLibraryInfo, TagInfo, and set of tag attributes.  Tag
     * attribute order does not affect the returned name.
     * 
     * @param tli
     * @param ti
     * @param attributes
     * @return unique pool name based on parameters
     */
    public static String getPoolName(TagLibraryInfo tli, TagInfo ti, Hashtable attributes) {
        return getSafeVariableName(tli.getURI() + "_" + ti.getTagName() + getStringFromAttributes(attributes));
    }


    /**
     * This method returns a unique pool variable name given
     * TagLibraryInfo, TagInfo and set of tag attributes.
     * 
     * @param tli
     * @param ti
     * @param attributes
     * @return unique pool variable name based on parameters
     */
    public static String getPoolVariableName(TagLibraryInfo tli, TagInfo ti, Hashtable attributes) {
        return getPoolVariableName(getPoolName(tli, ti, attributes));
    }


    /**
     * This method returns a unique pool variable name given
     * a unique pool name
     * 
     * @param poolName
     * @return unique pool variable name
     */
    public static String getPoolVariableName(String poolName) {
        return getSafeVariableName(POOL_VARIABLE_NAME_PREFIX + poolName);
    }


    /**
     * This method generates code from based on the jsp.  During
     * class declaration phase, it declares a tag pool for this
     * tag.  During the initilization phase, it generates code
     * to lookup a pool from the tag pool manager.
     *
     * @param writer
     * @param phase
     */
    public void generate(ServletWriter writer, Class phase) {
        if (ClassDeclarationPhase.class.isAssignableFrom(phase)) {
            writer.println(Constants.JSP_RUNTIME_PACKAGE +
			   ".TagHandlerPool " + poolVarName + " = null;");
        } else if (InitMethodPhase.class.isAssignableFrom(phase)) {
            writer.println("if (" + TagPoolManagerGenerator.MANAGER_VARIABLE + " != null) {");
            writer.pushIndent();
            writer.println(poolVarName + " = ");
            writer.pushIndent();
            writer.println(TagPoolManagerGenerator.MANAGER_VARIABLE + ".getPool(\"" + poolName + "\",");
            writer.println(ti.getTagClassName() + ".class);");
            writer.popIndent();
            writer.popIndent();
            writer.println("}");
        }
    }


    /**
     * This method generates a string based on a set of tag attributes.
     * It sorts the attributes by name then concatenates them.
     * 
     * @param attributes
     * @return string based on tag attributes
     */
    private static String getStringFromAttributes(Hashtable attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        } else {
            Vector sortedAttributes = new Vector(attributes.size());

            Enumeration elements = attributes.keys();
            int i;
            while (elements.hasMoreElements()) {
                String attributeName = (String) elements.nextElement();
                for (i=0; i<sortedAttributes.size(); i++) {
                    if (attributeName.compareTo((String) sortedAttributes.elementAt(i)) < 0) {
                        break;
                    }
                }
                sortedAttributes.insertElementAt(attributeName, i);
            }

            // cat the attributes
            StringBuffer buffer = new StringBuffer();
            for (i=0; i<sortedAttributes.size(); i++) {
                buffer.append('_');
                buffer.append(sortedAttributes.elementAt(i));
            }

            return buffer.toString();
        }
    }


    /**
     * Constant that holds the characters that can be valid first
     * characters of a java variable.
     */
    private static final String VALID_FIRST_CHARS =
         "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";


    /**
     * Constant that holds the characters that can be used as in
     * a valid java variable name -- non first char case.
     */
    private static final String VALID_CHARS = VALID_FIRST_CHARS + "1234567890";


    /**
     * This method generates a string that can be used as a java
     * variable name.  Characters that cant be used are replaced
     * with an underscore.
     * 
     * @param s
     * @return string that can be used as java variable name
     */
    private static String getSafeVariableName(String s) {
        StringBuffer buffer = new StringBuffer();
        String compareAgainst = VALID_FIRST_CHARS;
        for (int i=0; i<s.length(); i++) {
            if (i == 1) {
                compareAgainst = VALID_CHARS;
            }

            if (compareAgainst.indexOf(s.charAt(i)) != -1) {
                buffer.append(s.charAt(i));
            } else {
                buffer.append('_');
            }
        }

        return buffer.toString();
    }
}
