/*
 * Created on 10.09.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.columba.core.logging;

import org.apache.log4j.Logger;
import org.columba.ristretto.log.RistrettoLoggerInterface;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ColumbaRistrettoLogger implements RistrettoLoggerInterface {
	Logger log;

	public ColumbaRistrettoLogger(Logger log) {
		this.log = log;
	}
	/* (non-Javadoc)
		 * @see org.columba.ristretto.log.RistrettoLoggerInterface#debug(java.lang.String)
		 */
	public void debug(String message) {

		log.debug(message);
	}

	/* (non-Javadoc)
	 * @see org.columba.ristretto.log.RistrettoLoggerInterface#error(java.lang.String)
	 */
	public void error(String message) {

		log.error(message);
	}

	/* (non-Javadoc)
	 * @see org.columba.ristretto.log.RistrettoLoggerInterface#info(java.lang.String)
	 */
	public void info(String message) {

		log.info(message);
	}

	/* (non-Javadoc)
	 * @see org.columba.ristretto.log.RistrettoLoggerInterface#warn(java.lang.String)
	 */
	public void warn(String message) {

		log.warn(message);
	}
}
