/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.jasper.compiler;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;


/**
 * This class generates tag pooling related information.  Specifically,
 * it generates code to declare tag pools and to obtain tag pools
 * during jsp initialization.
 *
 * @author Casey Lucas <clucas@armassolutions.com>
 * @see TagPoolManager
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
     * @return
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
     * @return
     * @see getPoolName
     */
    public static String getPoolVariableName(TagLibraryInfo tli, TagInfo ti, Hashtable attributes) {
        return getPoolVariableName(getPoolName(tli, ti, attributes));
    }


    /**
     * This method returns a unique pool variable name given
     * a unique pool name
     *
     * @param poolName
     * @return
     * @see getPoolName
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
            writer.println("org.apache.jasper.runtime.TagHandlerPool " + poolVarName + " = null;");
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
     * @return
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
     * @return
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
