/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;

import org.jboss.ejb.DeploymentException;

/**
 * Contains information about ejb-ql queries.
 * 
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */
public class QueryMetaData extends MetaData {
	// Constants -----------------------------------------------------
	public final static String REMOTE = "Remote";
	public final static String LOCAL = "Local";

	public final static String HOME = "Home";
	public final static String LOCAL_HOME = "LocalHome";
	
	// Attributes ----------------------------------------------------
	private String description;
	private String methodName;
	private String methodIntf;
	private ArrayList methodParams;
	private String resultTypeMapping;
	private String ejbQl;
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	public QueryMetaData () {
		methodParams = new ArrayList();
	}
	
	// Public --------------------------------------------------------
	
	public String getDescription() {
		return description;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public String getMethodIntf() {
		return methodIntf;
	}
	
	public Iterator getMethodParams() {
		return methodParams.iterator();
	}
	
  	public String getResultTypeMapping() {
		return resultTypeMapping;
	}

  	public String getEjbQl() {
		return ejbQl;
	}

    
	public void importEjbJarXml(Element element) throws DeploymentException {
		// description
		description = getElementContent(getOptionalChild(element, "description"));
		
		// query-method sub-element
		Element queryMethod = getUniqueChild(element, "query-method");
		
		// method name
		methodName = getElementContent(getUniqueChild(queryMethod, "method-name"));
		
		// method interface
		methodIntf = getElementContent(getOptionalChild(queryMethod, "method-intf"));
		if(methodIntf!=null && !HOME.equals(methodIntf) && !LOCAL_HOME.equals(methodIntf)) {
			throw new DeploymentException("result-type-mapping must be '" + 
							HOME + "', '" + 
							LOCAL_HOME + "', if specified");
		}

		// method params
		Element methodParamsElement = getUniqueChild(queryMethod, "method-params");
		Iterator iterator = getChildrenByTagName(methodParamsElement, "method-param");			
		while (iterator.hasNext()) {
			methodParams.add(getElementContent((Element)iterator.next()));
		}

		// result type mapping
		resultTypeMapping = getElementContent(getOptionalChild(element, "result-type-mapping"));
		if(resultTypeMapping == null) {
			resultTypeMapping = LOCAL;
		}
		if(!REMOTE.equals(resultTypeMapping) &&
				!LOCAL.equals(resultTypeMapping)) {
			throw new DeploymentException("result-type-mapping must be '" + REMOTE + "' or '" + LOCAL + "', if specified");
		}

		ejbQl = getElementContent(getUniqueChild(element, "ejb-ql"));
	}		

}
