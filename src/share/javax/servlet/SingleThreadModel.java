/*
 * $Id: SingleThreadModel.java,v 1.1 1999/10/09 00:20:30 duncan Exp $
 * 
 * Copyright (c) 1995-1999 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * CopyrightVersion 1.0
 */

package javax.servlet;

/**
 * Ensures that servlets handle
 * only one request at a time. This interface has no methods.
 *
 * <p>If a servlet implements this interface, you are <i>guaranteed</i>
 * that no two threads will execute concurrently in the
 * servlet's <code>service</code> method. The servlet container
 * can make this guarantee by synchronizing access to a single
 * instance of the servlet, or by maintaining a pool of servlet
 * instances and dispatching each new request to a free servlet.
 *
 * <p>If a servlet implements this interface, the servlet will
 * be thread safe. However, this interface does not prevent
 * synchronization problems that result from servlets accessing shared
 * resources such as static class variables or classes outside
 * the scope of the servlet.
 *
 *
 * @author	Various
 * @version	$Version$
 *
 */

public interface SingleThreadModel {
}
