/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

/** A representation of the web-app.xml and jboss-web.xml deployment
 * descriptors as used by the AbstractWebContainer web container integration
 * support class.
 *      
 * @see XmlLoadable
 * @see org.jboss.web.AbstractWebContainer

 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @version $Revision: 1.4 $
 */
public class WebMetaData implements XmlLoadable
{
    private HashMap resourceReferences = new HashMap();
    private ArrayList environmentEntries = new ArrayList();
    private HashMap ejbReferences = new HashMap();
    private ArrayList securityRoleReferences = new ArrayList();
    private String securityDomain;

    public WebMetaData()
    {
    }

    /** Return an iterator of the env-entry mappings.
    @return Iterator of EnvEntryMetaData objects.
    */
    public Iterator getEnvironmentEntries()
    {
        return environmentEntries.iterator();
    }
    /** Return an iterator of the ejb-ref mappings.
    @return Iterator of EjbRefMetaData objects.
    */
    public Iterator getEjbReferences()
    {
        return ejbReferences.values().iterator();
    }
    /** Return an iterator of the resource-ref mappings.
    @return Iterator of ResourceRefMetaData objects.
    */
    public Iterator getResourceReferences()
    {
        return resourceReferences.values().iterator();
    }
    /** Return the optional security-domain jboss-web.xml element.
    @return The jndiName of the security manager implementation that is
        responsible for security of the web application. May be null if
        there was no security-domain specified in the jboss-web.xml
        descriptor.
    */    
    public String getSecurityDomain()
    {
        return securityDomain;
    }

    public void importXml(Element element) throws Exception
    {
        String rootTag = element.getOwnerDocument().getDocumentElement().getTagName();
        if( rootTag.equals("web-app") )
        {
            importWebXml(element);
        }
        else if( rootTag.equals("jboss-web") )
        {
            importJBossWebXml(element);
        }
    }

    /** Parse the elements of the web-app element used by the integration layer.
    */
    protected void importWebXml(Element webApp) throws Exception
    {
        // Parse the web-app/resource-ref elements
        Iterator iterator = MetaData.getChildrenByTagName(webApp, "resource-ref");
        while( iterator.hasNext() )
        {
            Element resourceRef = (Element) iterator.next();
            ResourceRefMetaData resourceRefMetaData = new ResourceRefMetaData();
            resourceRefMetaData.importEjbJarXml(resourceRef);
            resourceReferences.put(resourceRefMetaData.getRefName(), resourceRefMetaData);
        }

        // Parse the web-app/env-entry elements
        iterator = MetaData.getChildrenByTagName(webApp, "env-entry");
        while( iterator.hasNext() )
        {
            Element envEntry = (Element) iterator.next();
            EnvEntryMetaData envEntryMetaData = new EnvEntryMetaData();
            envEntryMetaData.importEjbJarXml(envEntry);
            environmentEntries.add(envEntryMetaData);
        }

        // Parse the web-app/ejb-ref elements
        iterator = MetaData.getChildrenByTagName(webApp, "ejb-ref");
        while( iterator.hasNext() )
        {
            Element ejbRef = (Element) iterator.next();
            EjbRefMetaData ejbRefMetaData = new EjbRefMetaData();
            ejbRefMetaData.importEjbJarXml(ejbRef);
            ejbReferences.put(ejbRefMetaData.getName(), ejbRefMetaData);
        }
    }

    /** Parse the elements of the jboss-web element used by the integration layer.
    */
    protected void importJBossWebXml(Element jbossWeb) throws Exception
    {
        // Parse the jboss-web/securityDomain element
        Element securityDomainElement = MetaData.getOptionalChild(jbossWeb, "security-domain");
        if( securityDomainElement != null )
            securityDomain = MetaData.getElementContent(securityDomainElement);

        // Parse the jboss-web/resource-ref elements
        Iterator iterator = MetaData.getChildrenByTagName(jbossWeb, "resource-ref");
        while( iterator.hasNext() )
        {
            Element resourceRef = (Element) iterator.next();
            String resRefName = MetaData.getElementContent(MetaData.getUniqueChild(resourceRef, "res-ref-name"));
            String jndiName = MetaData.getElementContent(MetaData.getUniqueChild(resourceRef, "jndi-name"));
            ResourceRefMetaData resourceRefMetaData = (ResourceRefMetaData) resourceReferences.get(resRefName);
            if( resourceRefMetaData == null )
            {
                throw new DeploymentException("resource-ref " + resRefName + " found in jboss-web.xml but not in web-app.xml");
            }
            resourceRefMetaData.setResourceName(jndiName);
        }

        // Parse the jboss-web/ejb-ref elements
        iterator = MetaData.getChildrenByTagName(jbossWeb, "ejb-ref");
        while( iterator.hasNext() )
        {
            Element ejbRef = (Element) iterator.next();
            String ejbRefName = MetaData.getElementContent(MetaData.getUniqueChild(ejbRef, "ejb-ref-name"));
            EjbRefMetaData ejbRefMetaData = (EjbRefMetaData) ejbReferences.get(ejbRefName);
            if( ejbRefMetaData == null )
            {
                throw new DeploymentException("ejb-ref " + ejbRefName + " found in jboss-web.xml but not in web-app.xml");
            }
            ejbRefMetaData.importJbossXml(ejbRef);
        }
    }
}
