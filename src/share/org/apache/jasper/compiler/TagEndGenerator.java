/*
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
import java.util.Stack;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.VariableInfo;


/**
 * Custom tag support.
 *
 * @author Anil K. Vijendran
 */
public class TagEndGenerator
    extends TagGeneratorBase
    implements ServiceMethodPhase
{
    String prefix, shortTagName;
    TagLibraryInfo tli;
    TagInfo ti;
    Hashtable attrs;
    TagLibraries libraries;

    public TagEndGenerator(String prefix, String shortTagName,
                           Hashtable attrs, TagLibraryInfo tli,
                           TagInfo ti, TagLibraries libraries,
                           Stack tagHandlerStack, Hashtable tagVarNumbers)
    {
        setTagHandlerStack(tagHandlerStack);
        setTagVarNumbers(tagVarNumbers);
        this.prefix = prefix;
        this.shortTagName = shortTagName;
        this.tli = tli;
        this.ti = ti;
        this.attrs = attrs;
	this.libraries = libraries;
    }

    public void generate(ServletWriter writer, Class phase) {
        TagVariableData tvd = tagEnd();
        String thVarName = tvd.tagHandlerInstanceName;
        String evalVarName = tvd.tagEvalVarName;
        String exceptionCheckName = tvd.tagExceptionCheckName;

        VariableInfo[] vi = ti.getVariableInfo(new TagData(attrs));

        Class tagHandlerClass =
	    libraries.getTagCache(prefix, shortTagName).getTagHandlerClass();
        boolean implementsBodyTag = BodyTag.class.isAssignableFrom(tagHandlerClass);

	writer.popIndent();

        if (implementsBodyTag)
            writer.println("} while ("+thVarName+".doAfterBody() == javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_TAG);");
        else
            writer.println("} while (false);");

        declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN);

        if (implementsBodyTag) {
            writer.popIndent(); // try

            /** FIXME: REMOVE BEGIN */
            //              writer.println("} catch (Throwable t) {");
            //              writer.pushIndent();

            //              writer.println("System.err.println(\"Caught: \");");
            //              writer.println("t.printStackTrace();");

            //              writer.popIndent();
            /** FIXME: REMOVE END */

            writer.println("} finally {");
            writer.pushIndent();
            writer.println("if ("+evalVarName+" != javax.servlet.jsp.tagext.Tag.EVAL_BODY_INCLUDE)");
            writer.pushIndent();
            writer.println("out = pageContext.popBody();");
            writer.popIndent();

            writer.popIndent();
            writer.println("}");
        }

	writer.popIndent(); // EVAL_BODY
	writer.println("}");

	writer.println("if ("+thVarName+".doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE)");
	writer.pushIndent(); writer.println("return;"); writer.popIndent();

	writer.popIndent(); // try

        /** FIXME: REMOVE BEGIN */
        //          writer.println("} catch (Throwable t) {");
        //          writer.pushIndent();

        //          writer.println("System.err.println(\"Caught: \");");
        //          writer.println("t.printStackTrace();");
        //          writer.popIndent();
        /** FIXME: REMOVE END */

        writer.println("} catch (Throwable throwable) {");
        writer.pushIndent();
        writer.println(exceptionCheckName + " = true;");
        writer.println("throw throwable;");
        writer.popIndent();
        writer.println("}");
        writer.popIndent();
        writer.println("} finally {");
        writer.pushIndent();
        String poolName = TagPoolGenerator.getPoolVariableName(tli, ti, attrs);
        writer.println("if (" + poolName + " != null && " + thVarName + " != null) {");
        writer.pushIndent();
        writer.println(poolName + ".releaseHandler(" + thVarName + ", " + exceptionCheckName + ");");
        //writer.println(poolName + ".releaseHandler(" + thVarName + ");");
        writer.popIndent();
        writer.println("} else {");
        writer.pushIndent();
        writer.println("if (" + thVarName + " != null) {");
        writer.pushIndent();
	writer.println(thVarName+".release();");
        writer.popIndent();
        writer.println("}");
        writer.popIndent();
        writer.println("}");
	writer.popIndent();
	writer.println("}");

        // Need to declare and update AT_END variables here.
        declareVariables(writer, vi, true, true, VariableInfo.AT_END);
    }
}
