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
 * Represents one ejb-relation element found in the ejb-jar.xml
 * file's relationships elements.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public class RelationMetaData extends MetaData {
	/** Name of the relation. Loaded from the ejb-relation-name element. */
	private String relationName;
	
	/** 
	 * The left relationship role. Loaded from an ejb-relationship-role.
	 * Left/right assignment is completely arbitrary.
	 */
	private RelationshipRoleMetaData left;

	/** 
	 * The right relationship role. Loaded from an ejb-relationship-role.
	 * Left/right assignment is completely arbitrary.
	 */
	private RelationshipRoleMetaData right;		
	
	/** 
	 * Gets the relation name. 
	 * Relation name is loaded from the ejb-relation-name element.
	 */
	public String getRelationName() {
		return relationName;
	}
	
	/** 
	 * Gets the left relationship role. 
	 * The relationship role is loaded from an ejb-relationship-role.
	 * Left/right assignment is completely arbitrary.
	 */
	public RelationshipRoleMetaData getLeftRelationshipRole() {
		return left;
	}
	
	/** 
	 * Gets the right relationship role.
	 * The relationship role is loaded from an ejb-relationship-role.
	 * Left/right assignment is completely arbitrary.
	 */
	public RelationshipRoleMetaData getRightRelationshipRole() {
		return right;
	}
	
   public void importEjbJarXml (Element element) throws DeploymentException {
	   // name
		relationName = getElementContent(getOptionalChild(element, "ejb-relation-name"));
		
		// left role
		Iterator iter = getChildrenByTagName(element, "ejb-relationship-role");
		if(iter.hasNext()) {
			left = new RelationshipRoleMetaData();
			left.importEjbJarXml((Element) iter.next());
		} else {
			throw new DeploymentException("Expected 2 ejb-relationship-role roles but found none");
		}
		
		// right role
		if(iter.hasNext()) {
			right = new RelationshipRoleMetaData();
			right.importEjbJarXml((Element) iter.next());
		} else {
			throw new DeploymentException("Expected 2 ejb-relationship-role but only found one");
		}

		// assure there are only two ejb-relationship-role elements
		if(iter.hasNext()) {
			throw new DeploymentException("Expected only 2 ejb-relationship-role but found more then 2");
		}
		
		// verify cascade delete
		if(left.isCascadeDelete() && right.isMultiplicityMany()) {
			throw new DeploymentException("cascade-delete is only allowed in ejb-relationship-role where the other role has a multiplicity One");
		}
		if(right.isCascadeDelete() && left.isMultiplicityMany()) {
			throw new DeploymentException("cascade-delete is only allowed in ejb-relationship-role where the other role has a multiplicity One");
		}
	}
}
