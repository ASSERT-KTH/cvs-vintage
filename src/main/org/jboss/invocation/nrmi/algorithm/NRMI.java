/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.nrmi.algorithm;

/**
 * The entry point to the NRMI algorithm by external users.
 * Implementsall the main steps of NRMI.
 * Since all the methods are static, it makes no sense to instantiate
 * this class.
 * @author <a href="mailto:tilevich@cc.gatech.edu">Eli Tilevich</a>
 * @version $Revision: 1.1 $
 */
public abstract class NRMI {

/**
 * Creates linear map representations for all copy-restore arguments specified by true values
 * in passByCopyRestore.
 * @param passByCopyRestore specifies which arguments are passed by copy-restore
 * @param arguments array of all arguments in a method
 */
public static synchronized Object[][] createLinearRepresentations (boolean[] passByCopyRestore, 
                                                                      Object[] arguments) {
Object[][] linearRepresentations = new Object[arguments.length][];
for (int i = 0; i < arguments.length; ++i) {
	if (passByCopyRestore[i]) {
		ArrayRepresenter representer = new ArrayRepresenter (arguments[i]);
		linearRepresentations[i] = representer.getRepresentation();
	}
} //for
return linearRepresentations;
} //createLinearRepresentation

/**
 * Performs the last step of the algorithm replaying the server changes on the client.
 * @param passByCopyRestore specifies which arguments are passed by copy-restore
 * @param newLinearRepresentations linear map representations created on the server
 * @param originalLinearRepresentations linear map representations created on the client
 */
public static synchronized void performRestore (boolean[] passByCopyRestore,
                                                   Object[][] newLinearRepresentations,
                                                   Object[][] originalLinearRepresentations) {
try {

for (int i = 0; i < passByCopyRestore.length; ++i) {
if (passByCopyRestore[i]) {	
	DFObjectWalker walker = new DFObjectWalker ();
	NRMICopyCallback callback = new NRMICopyCallback (newLinearRepresentations[i], 
	                                                  originalLinearRepresentations[i]);
	walker.walkObject (newLinearRepresentations[i], callback);	
} //if
} //for
} catch (org.jboss.invocation.nrmi.algorithm.NRMICopyCallback.Exception ex) {
	ex.printStackTrace();
}
} //performRestore

}