/*
 * $Header: /tmp/cvs-vintage/struts/contrib/tiles/src/share/org/apache/struts/taglib/tiles/Attic/PutTagParent.java,v 1.1 2001/08/01 14:36:41 cedric Exp $
 * $Revision: 1.1 $
 * $Date: 2001/08/01 14:36:41 $
 * $Author: cedric $
 *
 */

package org.apache.struts.taglib.tiles;

import javax.servlet.jsp.JspException;

/**
 * Tag Class implementing this interface can contains nested PutTag.
 * This interface define a method calls by nested tag.
 */
public interface PutTagParent {
  /**
   * Process the nested tag.
   * @param nestedTag Nested to process.
   */
  void processNestedTag(PutTag nestedTag ) throws JspException;

}
