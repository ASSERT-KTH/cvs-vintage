/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/share/org/apache/strutsel/taglib/tiles/ELPutTag.java,v 1.3 2004/03/14 07:15:04 sraeburn Exp $
 * $Revision: 1.3 $
 * $Date: 2004/03/14 07:15:04 $
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

package org.apache.strutsel.taglib.tiles;

import org.apache.struts.taglib.tiles.PutTag;
import javax.servlet.jsp.JspException;
import org.apache.strutsel.taglib.utils.EvalHelper;

/**
 * Put an attribute in enclosing attribute container tag.
 * Enclosing attribute container tag can be : &lt;insert&gt; or &lt;definition&gt;.
 * Exception is thrown if no appropriate tag can be found.
 * Put tag can have following atributes :
 * <li>
 * <ul>name : Name of the attribute</ul>
 * <ul>value | content : value to put as attribute</ul>
 * <ul>type : value type. Only valid if value is a String and is set by
 * value="something" or by a bean.
 * Possible type are : string (value is used as direct string),
 * page | template (value is used as a page url to insert),
 * definition (value is used as a definition name to insert)</ul>
 * <ul>direct : Specify if value is to be used as a direct string or as a
 * page url to insert. This is another way to specify the type. It only apply
 * if value is set as a string, and type is not present.</ul>
 * <ul>beanName : Name of a bean used for setting value. Only valid if value is not set.
 * If property is specified, value come from bean's property. Otherwise, bean
 * itself is used for value.</ul>
 * <ul>beanProperty : Name of the property used for retrieving value.</ul>
 * <ul>beanScope : Scope containing bean. </ul>
 * <ul>role : Role to check when 'insert' will be called. If enclosing tag is
 * &lt;insert&gt;, role is checked immediately. If enclosing tag is
 * &lt;definition&gt;, role will be checked when this definition will be
 * inserted.</ul>
 * </li>
 * Value can also come from tag body. Tag body is taken into account only if
 * value is not set by one of the tag attributes. In this case Attribute type is
 * "string", unless tag body define another type.
 *<p>
 * This class is a subclass of the class
 * <code>org.apache.struts.taglib.tiles.PutTag</code> which provides most of
 * the described functionality.  This subclass allows all attribute values to
 * be specified as expressions utilizing the JavaServer Pages Standard Library
 * expression language.
 *
 * @version $Revision: 1.3 $
 */
public class ELPutTag extends PutTag {

    /**
     * Instance variable mapped to "name" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String nameExpr;
    /**
     * Instance variable mapped to "value" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String valueExpr;
    /**
     * Instance variable mapped to "content" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String contentExpr;
    /**
     * Instance variable mapped to "direct" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String directExpr;
    /**
     * Instance variable mapped to "type" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String typeExpr;
    /**
     * Instance variable mapped to "beanName" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String beanNameExpr;
    /**
     * Instance variable mapped to "beanProperty" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String beanPropertyExpr;
    /**
     * Instance variable mapped to "beanScope" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String beanScopeExpr;
    /**
     * Instance variable mapped to "role" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    private String roleExpr;

    /**
     * Getter method for "name" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getNameExpr() { return (nameExpr); }
    /**
     * Getter method for "value" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getValueExpr() { return (valueExpr); }
    /**
     * Getter method for "content" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getContentExpr() { return (contentExpr); }
    /**
     * Getter method for "direct" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getDirectExpr() { return (directExpr); }
    /**
     * Getter method for "type" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getTypeExpr() { return (typeExpr); }
    /**
     * Getter method for "beanName" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getBeanNameExpr() { return (beanNameExpr); }
    /**
     * Getter method for "beanProperty" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getBeanPropertyExpr() { return (beanPropertyExpr); }
    /**
     * Getter method for "beanScope" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getBeanScopeExpr() { return (beanScopeExpr); }
    /**
     * Getter method for "role" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public String getRoleExpr() { return (roleExpr); }

    /**
     * Setter method for "name" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setNameExpr(String nameExpr) { this.nameExpr = nameExpr; }
    /**
     * Setter method for "value" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setValueExpr(String valueExpr) { this.valueExpr = valueExpr; }
    /**
     * Setter method for "content" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setContentExpr(String contentExpr) { this.contentExpr = contentExpr; }
    /**
     * Setter method for "direct" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setDirectExpr(String directExpr) { this.directExpr = directExpr; }
    /**
     * Setter method for "type" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setTypeExpr(String typeExpr) { this.typeExpr = typeExpr; }
    /**
     * Setter method for "beanName" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setBeanNameExpr(String beanNameExpr) { this.beanNameExpr = beanNameExpr; }
    /**
     * Setter method for "beanProperty" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setBeanPropertyExpr(String beanPropertyExpr) { this.beanPropertyExpr = beanPropertyExpr; }
    /**
     * Setter method for "beanScope" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setBeanScopeExpr(String beanScopeExpr) { this.beanScopeExpr = beanScopeExpr; }
    /**
     * Setter method for "role" tag attribute.
     * (Mapping set in associated BeanInfo class.)
     */
    public void setRoleExpr(String roleExpr) { this.roleExpr = roleExpr; }

    /**
     * Resets attribute values for tag reuse.
     */
    public void release()
    {
        super.release();
        setNameExpr(null);
        setValueExpr(null);
        setContentExpr(null);
        setDirectExpr(null);
        setTypeExpr(null);
        setBeanNameExpr(null);
        setBeanPropertyExpr(null);
        setBeanScopeExpr(null);
        setRoleExpr(null);
    }
    
    /**
     * Process the start tag.
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {
        evaluateExpressions();
        return (super.doStartTag());
    }
    
    /**
     * Processes all attribute values which use the JSTL expression evaluation
     * engine to determine their values.
     *
     * @exception JspException if a JSP exception has occurred
     */
    private void evaluateExpressions() throws JspException {
        String  string  = null;

        if ((string = EvalHelper.evalString("name", getNameExpr(),
                                            this, pageContext)) != null)
            setName(string);
        if ((string = EvalHelper.evalString("value", getValueExpr(),
                                            this, pageContext)) != null)
            setValue(string);
        if ((string = EvalHelper.evalString("content", getContentExpr(),
                                            this, pageContext)) != null)
            setContent(string);
        if ((string = EvalHelper.evalString("direct", getDirectExpr(),
                                            this, pageContext)) != null)
            setDirect(string);
        if ((string = EvalHelper.evalString("type", getTypeExpr(),
                                            this, pageContext)) != null)
            setType(string);
        if ((string = EvalHelper.evalString("beanName", getBeanNameExpr(),
                                            this, pageContext)) != null)
            setBeanName(string);
        if ((string = EvalHelper.evalString("beanProperty", getBeanPropertyExpr(),
                                            this, pageContext)) != null)
            setBeanProperty(string);
        if ((string = EvalHelper.evalString("beanScope", getBeanScopeExpr(),
                                            this, pageContext)) != null)
            setBeanScope(string);
        if ((string = EvalHelper.evalString("role", getRoleExpr(),
                                            this, pageContext)) != null)
            setRole(string);
    }
}
