/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx;

import java.io.Serializable;

/**
* Object returned to the client to use it as an handler to an object created
* and stored on the server side. The server side implementation has to look
* for instances of these class and then to replace them by the objec thez
* refer to.
*
* @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
**/
public class ObjectHandler 
	implements Serializable
{

	// Constants -----------------------------------------------------

	// Static --------------------------------------------------------
	/** Unique number generator **/
	private int							sNextUniqueNumber = 0;

	// Members -------------------------------------------------------
	/**
	* Server reference to ensure that this object is handled on this
	* server
	**/
	private String						mServerReference;
	/** Object index used within the server to reference the effectiv object **/
	private int							mObjectReference;

	// Constructors --------------------------------------------------
	/**
	* Creates an Object Handler
	*
	* @param pServerReference			Server Reference which should be unique
	*									within the network
	*
	* @return							Object handler which can be used to
	*									reference a object on the server side
	*/
	public ObjectHandler(
		String pServerReference
	) {
		if( pServerReference == null ) {
			throw new IllegalArgumentException( "Server Reference must not be null" );
		}
		mServerReference = pServerReference;
		mObjectReference = sNextUniqueNumber++;
	}
	
	// Public --------------------------------------------------------
	/**
	* @return							Server Reference
	**/
	public String getServerReference() {
		return mServerReference;
	}
	/**
	* @return							Unique object reference
	*/
	public int getObjectReference() {
		return mObjectReference;
	}
	
	/**
	* Checks if two Object Handler are equal even when the went over the net
	*
	* @pTest							Object to test against
	*
	* @return							True the given object is instance
	*									of Object Handler and have the same
	*									Server and Object Reference
	**/
	public boolean equals( Object pTest ) {
		if( pTest instanceof ObjectHandler ) {
			ObjectHandler lTest = (ObjectHandler) pTest;
			return
				getServerReference().equals( lTest.getServerReference() ) &&
				getObjectReference() == lTest.getObjectReference();
		}
		return false;
	}
	/**
	* @return							Hash code for this Object Handler which
	*									is just the sum of Server and Object
	*									reference
	*/
	public int hashCode() {
		return
			getServerReference().hashCode() +
			getObjectReference();
	}
}
