/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/config/ConfigRuleSet.java,v 1.1 2001/12/26 23:14:50 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2001/12/26 23:14:50 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
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


package org.apache.struts.config;


import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;


/**
 * <p>The set of Digester rules required to parse a Struts application
 * configuration file (<code>struts-config.xml</code>).</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2001/12/26 23:14:50 $
 * @since Struts 1.1
 */

public class ConfigRuleSet extends RuleSetBase {


    // --------------------------------------------------------- Public Methods


    /**
     * <p>Add the set of Rule instances defined in this RuleSet to the
     * specified <code>Digester</code> instance, associating them with
     * our namespace URI (if any).  This method should only be called
     * by a Digester instance.  These rules assume that an instance of
     * <code>org.apache.struts.config.ApplicationConfig</code> is pushed
     * onto the evaluation stack before parsing begins.</p>
     *
     * @param digester Digester instance to which the new Rule instances
     *  should be added.
     */
    public void addRuleInstances(Digester digester) {

        digester.addObjectCreate
            ("struts-config/data-sources/data-source",
             "org.apache.struts.config.DataSourceConfig",
             "className");
        digester.addSetProperties
            ("struts-config/data-sources/data-source");
        digester.addSetNext
            ("struts-config/data-sources/data-source",
             "addDataSourceConfig",
             "org.apache.struts.config.DataSourceConfig");

        digester.addSetProperty
            ("struts-config/data-sources/data-source/set-property",
             "property", "value");

        digester.addObjectCreate
            ("struts-config/action-mappings/action",
             "org.apache.struts.config.ActionConfig",
             "className");
        digester.addSetProperties
            ("struts-config/action-mappings/action");
        digester.addSetNext
            ("struts-config/action-mappings/action",
             "addActionConfig",
             "org.apache.struts.config.ActionConfig");

        digester.addSetProperty
            ("struts-config/action-mappings/action/set-property",
             "property", "value");

        digester.addObjectCreate
            ("struts-config/action-mappings/action/forward",
             "org.apache.struts.config.ForwardConfig",
             "className");
        digester.addSetProperties
            ("struts-config/action-mappings/action/forward");
        digester.addSetNext
            ("struts-config/action-mappings/action/forward",
             "addForwardConfig",
             "org.apache.struts.config.ForwardConfig");

        digester.addSetProperty
            ("struts-config/action-mappings/action/forward/set-property",
             "property", "value");

        digester.addObjectCreate
            ("struts-config/controller",
             "org.apache.struts.config.ControllerConfig",
             "className");
        digester.addSetProperties
            ("struts-config/controller");
        digester.addSetNext
            ("struts-config/controller",
             "setControllerConfig",
             "org.apache.struts.config.ControllerConfig");

        digester.addSetProperty
            ("struts-config/controller/set-property",
             "property", "value");

        digester.addObjectCreate
            ("struts-config/form-beans/form-bean",
             "org.apache.struts.config.FormBeanConfig",
             "className");
        digester.addSetProperties
            ("struts-config/form-beans/form-bean");
        digester.addSetNext
            ("struts-config/form-beans/form-bean",
             "addFormBeanConfig",
             "org.apache.struts.config.FormBeanConfig");

        digester.addSetProperty
            ("struts-config/form-beans/form-bean/set-property",
             "property", "value");

        digester.addObjectCreate
            ("struts-config/global-forwards/forward",
             "org.apache.struts.config.ForwardConfig",
             "className");
        digester.addSetProperties
            ("struts-config/global-forwards/forward");
        digester.addSetNext
            ("struts-config/global-forwards/forward",
             "addForwardConfig",
             "org.apache.struts.config.ForwardConfig");

        digester.addSetProperty
            ("struts-config/global-forwards/forward/set-property",
             "property", "value");

        digester.addObjectCreate
            ("struts-config/message-resources",
             "org.apache.struts.config.MessageResourcesConfig",
             "className");
        digester.addSetProperties
            ("struts-config/message-resources");
        digester.addSetNext
            ("struts-config/messageResources",
             "setMessageResourcesConfig",
             "org.apache.struts.config.MessageResourcesConfig");

        digester.addSetProperty
            ("struts-config/message-resources/set-property",
             "property", "value");

    }

}
