/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/taglib/nested/NestedPropertyHelper.java,v 1.12 2003/02/06 00:26:11 arron Exp $
 * $Revision: 1.12 $
 * $Date: 2003/02/06 00:26:11 $
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.struts.taglib.nested;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.Tag;

import org.apache.struts.taglib.html.FormTag;
import org.apache.struts.taglib.html.Constants;

/** A simple helper class that does everything that needs to be done to get the
 * nested tag extension to work. Knowing what tags can define the lineage of
 * other tags, a tag asks of it to look up to the next nested tag and get it's
 * relative nested property.
 *
 * With all tags keeping track of themselves, we only have to seek to the next
 * level, or parent tag, were a tag will append a dot and it's own property.
 *
 * @author Arron Bates
 * @since Struts 1.1
 * @version $Revision: 1.12 $ $Date: 2003/02/06 00:26:11 $
 */ 
public class NestedPropertyHelper {
  
  /* key that the tags can rely on to set the details against */
  public static final String NESTED_INCLUDES_KEY = "<nested-includes-key/>";
  
  /** Sets the passed reference to the session object, and returns any reference
   * that was already there
   * @param request User's request object
   * @param reference New reference to put into the session
   */
  public static final NestedReference setIncludeReference(HttpServletRequest request,
          NestedReference reference) {
    /* get the old one if any */
    NestedReference nr = (NestedReference)
            request.getAttribute(NESTED_INCLUDES_KEY);
    if (reference != null) {
      /* put in the new one */
      request.setAttribute(NESTED_INCLUDES_KEY, reference);
    } else {
      /* null target, just remove it */
      request.removeAttribute(NESTED_INCLUDES_KEY);
    }
    /* return the old */
    return nr;
  }
  
  
  
  /** 
   * The working horse method.
   * This method works its way back up though the tag tree until it reaches
   * a nested tag from which we're meant to be nesting against.
   * It identifies nested tags by their implementing interfaces and via this
   * mechanism might not have to traverse the entire way up the tree to get
   * to the root tag.
   *
   * @param tag The tag to start with
   * @return The parent tag to which we're nesting against
   */
  public static Tag getNestingParentTag(NestedTagSupport tag) {
    Tag namedTag = (Tag)tag;
    Tag parentTag = null;
    
    /* loop all parent tags until we get one that can be nested against  */
    do {
      namedTag = namedTag.getParent();
      if (namedTag instanceof NestedParentSupport ||
          namedTag instanceof FormTag) {
        parentTag = namedTag;
      }
    } while (parentTag == null && namedTag != null);
    
    if (namedTag == null) {
      // need to spit some chips
    }
    
    return parentTag;
  }
  
  
  
  /**
   * Providing a property and the nested tag's parent, this method will return
   * the qualified nested name. It also checks for a relative property to make
   * sure it's handled correctly.
   *
   * @param property String of the property to get the nesting version of
   * @param parentTag the nested tag's nesting parent.
   * @return String of the fully qualified nesting property
   */
  public static String getNestedProperty(String property, Tag parentTag) {

    // return if there's nothing to play with.
    if (property == null) { return null; }

    /* if we're just under a root tag no work required */
    if (parentTag instanceof FormTag) {
      /* don't forget to take care of the relative properties */
      if (property.indexOf('/') == -1) {
        return property;
      } else {
        return property.substring(property.indexOf('/')+1, property.length());
      }
    }
    
    if (!(parentTag instanceof NestedParentSupport)) {
      // need to spit chips
    }
    
    NestedParentSupport nestedParent = (NestedParentSupport)parentTag;

    if (nestedParent != null) {
      /* return dot notated property from the parent */
      if (property.indexOf('/') == -1) {
        property = nestedParent.getNestedProperty() +"."+ property;
      } else {
        property = getRelativeProperty(property,nestedParent.getNestedProperty());
      }
    }

    /* Some properties may be at the start of their hierarchy */
    if (property.startsWith(".")) {
      return property.substring(1, property.length());
    }
    return property;
  }
  
  
  /** A convenience method to provide the property straight from a tag using
   * its getProperty() method
   *
   * @param tag the whose property property is to be qualified by nesting
   * @return String of the fully qualified nesting property
   */
  public static String getNestedProperty(NestedPropertySupport tag) {
    Tag parentTag = getNestingParentTag(tag);
    return getNestedProperty(tag.getProperty(), parentTag);
  }
  
  
  
  /**
   * This method works its way back up though the tag tree until it reaches
   * a nested tag which we can get a reliable source of the bean name.
   *
   * @param tag The tag to start with
   * @return name of the nesting bean we're using
   */
  public static String getNestedNameProperty(NestedTagSupport tag) {
    
    Tag namedTag = (Tag)tag;
    String defaultName = null;
    // see if we're already in the right location
    if (namedTag instanceof NestedNameSupport) {
	    String name = ((NestedNameSupport)namedTag).getName();
        // return if we already have a name and not just default
        if (name != null) {
            if (name.equals(Constants.BEAN_KEY)) {
                defaultName = name;
            } else {
                return name;
            }
        }
    }

    /* loop all parent tags until we get one which
       gives a reliable bean name  */
    do {
      namedTag = namedTag.getParent();
    } while ( namedTag != null &&
              !(namedTag instanceof FormTag) &&
              !(namedTag instanceof NestedParentSupport) );
    
    if (namedTag == null) {
        if (defaultName != null) {
            return defaultName;
        }
        // now there's an issue
    }
    
    String nameTemp = null;
    if (namedTag instanceof FormTag) {
      nameTemp = ((FormTag)namedTag).getBeanName();
    } else if (namedTag instanceof NestedParentSupport) {
      nameTemp = ((NestedParentSupport)namedTag).getName();
    }
    return nameTemp;
  }
  
  
  /**
   * A convenience method by which a tag can just pass itself in and have all of
   * its relevant properties set for it.
   *
   * @param tag The nested tag whose properties are to be set
   */
  public static void setNestedProperties(NestedPropertySupport tag) {
    
    /* get and set the relative property */
    String property = getNestedProperty(tag);
    tag.setProperty(property);
   
    /* if the tag implements NestedNameSupport, set the name for the tag also */
    if (tag instanceof NestedNameSupport && property != null) {
      String name = getNestedNameProperty(tag);
      ((NestedNameSupport)tag).setName(name);
    }
  }
  
  
    
  /* This property, providing the property to be appended, and the parent tag
   * to append the property to, will calculate the stepping of the property
   * and return the qualified nested property 
   *
   * @param property the property which is to be appended nesting style
   * @param parent the "dot notated" string representing the structure
   * @return qualified nested property that the property param is to the parent
   */
  private static String getRelativeProperty(String property, String parent) {
    
    /* Special case... reference my parent's nested property.
       Otherwise impossible for things like indexed properties */
    if ("./".equals(property) || "this/".equals(property)) {
      return parent;
    }
    /* remove the stepping from the property */
    String stepping;
    
    /* isolate a parent reference */
    if (property.endsWith("/")) {
      stepping = property;
      property = "";
    } else {
      stepping = property.substring(0,property.lastIndexOf('/')+1);
      /* isolate the property */
      property = property.substring(property.lastIndexOf('/')+1,property.length());
    }
    
    if (stepping.startsWith("/")) {
      /* return from root */
      return property;
    } else {
      /* tokenize the nested property */
      StringTokenizer proT = new StringTokenizer(parent, ".");
      int propCount = proT.countTokens();

      /* tokenize the stepping */
      StringTokenizer strT = new StringTokenizer(stepping, "/");
      int count = strT.countTokens();
      
      if (count >= propCount) {
        /* return from root */
        return property;
        
      } else {
        /* append the tokens up to the token difference */
        count = propCount - count;
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < count; i++) {
          result.append(proT.nextToken());
          result.append('.');
        }
        result.append(property);
        
        /* parent reference will have a dot on the end. Leave it off */
        if (result.charAt(result.length()-1) == '.') {
          return result.substring(0,result.length()-1);
        } else {
          return result.toString();
        }
      }
    }
  }
}
