package org.apache.jasper34.utils;

import com.g1440.naf.util.SimpleCache;
/**
 * 	Interface for objects that have an expiration.
 * 	Used by the <i>SimpleCache</i> class to expire objects stored
 * 	in it.  Objects that wish to be expired when stored in
 * 	the cache should implement this interface.  It is not
 * 	necessary to implement this interface to store objects
 * 	in a <i>SimpleCache</i>.
 * 	<br></br>
 * 	@author Mel Martinez
 * 	@see <{org.apache.jasper34.utils.TableCache}>
*/
public interface Expirable {

	/**
 * 		Returns true if this object should be expired from the cache.
 * 		<p>
 * 		@return true if this object should be expired from the cache; false otherwise
	*/
	public boolean isExpired();

}