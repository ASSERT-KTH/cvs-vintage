/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @version $Revision: 1.4 $
 */
public class MethodMetaData extends MetaData {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
	private String methodName;
	private String ejbName;
	
	private boolean intf = false;
	private boolean home;
    
	private boolean param = false;
	private ArrayList paramList = new ArrayList();

	private byte transactionType;
	
	private Set permissions;
	
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public MethodMetaData () {
	}
	
    // Public --------------------------------------------------------
	
	public String getMethodName() { return methodName; }
	
	public String getEjbName() { return ejbName; }
	
	public boolean isHomeMethod() { return home; }
    public boolean isRemoteMethod() { return !home; }
    
	public boolean isIntfGiven() { return intf; }
	
    public boolean isParamGiven() { return param; }
	
	public Iterator getParams() { return paramList.iterator(); }

	public byte getTransactionType() { return transactionType; }
	
	public void setTransactionType(byte type) {
	    transactionType = type;
	}

	public Set getRoles() { return permissions; }
	
	public void setRoles(Set perm) { permissions = perm; }
	
	
	public boolean patternMatches(String name, Class[] arg, boolean remote) {
		return patternMatches(name, getClassNames(arg), remote);
	}
	
	public boolean patternMatches(String name, String[] arg, boolean remote) {
		
		// the wildcard matches everything
		if (getMethodName().equals("*")) return true;
		
		if (! getMethodName().equals(name)) {
	    	// different names -> no
			return false;
			
		} else {
			// we have the same name
			// next check: home or remote
			if (isIntfGiven() && (isRemoteMethod() != remote)) return false;
				
			if (! isParamGiven()) {
		    	// no param given in descriptor -> ok
				return true;
			} else {
				// we *have* to check the parameters
				return sameParams(arg, (String[])paramList.toArray());
			}
		}
	}
			
	
    public void importEjbJarXml(Element element) throws DeploymentException {
		methodName = getElementContent(getUniqueChild(element, "method-name"));
		ejbName = getElementContent(getUniqueChild(element, "ejb-name"));
		
		Element intfElement = getOptionalChild(element, "method-intf");
		if (intfElement != null) {
			intf = true;
			String homeRemote = getElementContent(intfElement);
			if (homeRemote.equals("Home")) {
				home = true;
			} else if (homeRemote.equals("Remote")) {
				home = false;
			} else {
				throw new DeploymentException("method-intf tag should be 'Home' or 'Remote'");
			}
		}
		
		Element paramsElement = getOptionalChild(element, "method-params");
	    if (paramsElement != null) {
			param = true;
			Iterator paramsIterator = getChildrenByTagName(paramsElement, "method-param");
			while (paramsIterator.hasNext()) {
				paramList.add(getElementContent((Element)paramsIterator.next()));
			}
		}
	}		
    
	// Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    private static String[] getClassNames(Class[] source) {
        String out[] = new String[source.length];
        for(int i=0; i<out.length; i++)
            out[i] = source[i].getName();
        return out;
    }

	private boolean sameParams(String[] list1, String[] list2) {
        if(list1.length != list2.length) return false;
        for(int i=0; i<list1.length; i++)
            if(!list1[i].equals(list2[i]))
                return false;
        return true;
    }

    // Inner classes -------------------------------------------------
}
