/*
 * $Header: /tmp/cvs-vintage/struts/contrib/tiles/src/share/org/apache/struts/taglib/tiles/util/Attic/TagUtils.java,v 1.2 2001/12/27 17:35:37 cedric Exp $
 * $Revision: 1.2 $
 * $Date: 2001/12/27 17:35:37 $
 * $Author: cedric $
 *
 */

package org.apache.struts.taglib.tiles.util;

import org.apache.commons.beanutils.PropertyUtils;

import org.apache.struts.taglib.tiles.ComponentConstants;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsUtil;
import org.apache.struts.tiles.NoSuchDefinitionException;
import org.apache.struts.tiles.FactoryNotFoundException;
import org.apache.struts.tiles.DefinitionsFactoryException;

import org.apache.struts.action.Action;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.ServletException;

import javax.servlet.jsp.PageContext;
import java.lang.reflect.InvocationTargetException;


  /**
   * Collection of utilities.
   * This class also serve as an interface between Components and Struts. If
   * you want to rip away Struts, simply reimplement some methods in this class.
   * You can copy them from Struts.
   */
public class TagUtils {

    /** Debug flag */
  static public final boolean debug = true;
     /**
     * Get scope value from string value
     * @param scopeName scope as a String
     * @param default returned value, if not found.
     * @return scope as an int, or defaultValue if scope is null.
     * @throw JspException Scoe name is not recognize as a valid scope.
     */
  static public int getScope( String scopeName, int defaultValue ) throws JspException
    {
    if(scopeName==null)
      return defaultValue;
    else if( scopeName.equals("request") )
      {
      return PageContext.REQUEST_SCOPE;
      }
    else if( scopeName.equals("page") )
      {
      return PageContext.PAGE_SCOPE;
      }
    else if( scopeName.equals("session") )
      {
      return PageContext.SESSION_SCOPE;
      }
    else if( scopeName.equals("application") )
      {
      return PageContext.APPLICATION_SCOPE;
      }
    else if( scopeName.equals("component") )
      {
      return ComponentConstants.COMPONENT_SCOPE;
      }
    else if( scopeName.equals("template") )
      {
      return ComponentConstants.COMPONENT_SCOPE;
      }
    else
      {
      throw new JspException( "Error - scope translation tag : unrecognized scope '"
                             + scopeName
                             + "'" );
      }
  }

    /**
     * Return the value of the specified property of the specified bean,
     * no matter which property reference format is used, with no
     * type conversions.
     *
     * @param bean Bean whose property is to be extracted
     * @param name Possibly indexed and/or nested name of the property
     *  to be extracted
     *
     * @exception IllegalAccessException if the caller does not have
     *  access to the property accessor method
     * @exception InvocationTargetException if the property accessor method
     *  throws an exception
     * @exception NoSuchMethodException if an accessor method for this
     *  propety cannot be found
     */
    public static Object getProperty(Object bean, String name)
	      throws IllegalAccessException, InvocationTargetException,
	             NoSuchMethodException
     {
	   return (PropertyUtils.getProperty(bean, name));
     }


    /**
     * Retrieve bean from page context, using specified scope.
     * If scope is not setted, use findAttribute.
     * @beanName Name of bean to retrieve
     * @scopeName Scope or null. If null, bean is searched using findAttribute.
     * @pageContext Current pageContext.
     * @return requested bean or null if not found.
     * @throw JspException If scopeName is not recognize.
     *
     */
    public static Object retrieveBean( String beanName, String scopeName, PageContext pageContext )
      throws JspException
      {
      if( scopeName == null )
        {
        return findAttribute( beanName, pageContext );
        } // end if

        // Default value doesn't matter because we have already check it
      int scope = getScope( scopeName, PageContext.PAGE_SCOPE );
      //return pageContext.getAttribute( beanName, scope );
      return getAttribute( beanName, scope, pageContext);
      }

    /**
     * Search attribute in different contexts.
     * First, check in component context, then use pageContext.findAttribute().
     * @beanName Name of bean to retrieve
     * @pageContext Current pageContext.
     * @return requested bean or null if not found.
     */
    public static Object findAttribute( String beanName, PageContext pageContext )
      {
      Object attribute;
      ComponentContext compContext = ComponentContext.getContext(pageContext.getRequest());
      if( compContext != null )
        {
        attribute =  compContext.findAttribute( beanName, pageContext );
        if( attribute != null )
          return attribute;
        } // end if

        // Search in pageContext scopes
      return pageContext.findAttribute( beanName );
      }

    /**
     * Get object from requested context. Return null if not found.
     * Context can be "component" or normal jsp contexts.
     * @beanName Name of bean to retrieve
     * @scope Scope from which bean must be retrieved.
     * @pageContext Current pageContext.
     * @return requested bean or null if not found.
     */
    public static Object getAttribute( String beanName, int scope, PageContext pageContext )
      {
      if( scope == ComponentConstants.COMPONENT_SCOPE )
        {
        ComponentContext compContext = ComponentContext.getContext(pageContext.getRequest());
        return compContext.getAttribute( beanName );
        }
      return pageContext.getAttribute( beanName, scope );
      }

    /**
     * Locate and return the specified property of the specified bean, from
     * an optionally specified scope, in the specified page context.
     *
     * @param pageContext Page context to be searched
     * @param beanName Name of the bean to be retrieved
     * @param beanProperty Name of the property to be retrieved, or
     *  <code>null</code> to retrieve the bean itself
     * @param beanScope Scope to be searched (page, request, session, application)
     *  or <code>null</code> to use <code>findAttribute()</code> instead
     *
     * @exception JspException if an invalid scope name
     *  is requested
     * @exception JspException if the specified bean is not found
     * @exception JspException if accessing this property causes an
     *  IllegalAccessException, IllegalArgumentException,
     *  InvocationTargetException, or NoSuchMethodException
     */
  static public Object getRealValueFromBean( String beanName, String beanProperty, String beanScope, PageContext pageContext)
           throws JspException
    {
    try
      {
      Object realValue;
        Object bean = retrieveBean( beanName, beanScope, pageContext );
        if( bean != null && beanProperty != null )
            realValue = getProperty( bean, beanProperty );
           else
            realValue = bean;   // value can be null
      return realValue;
      }
     catch( NoSuchMethodException ex )
      {
      throw new JspException( "Error - component.PutAttributeTag : Error while retrieving value from bean '"
                              + beanName + "' with property '"
                              + beanProperty + "' in scope '"
                              + beanScope + "'. (exception : "
                              + ex.getMessage() );
      }
     catch( InvocationTargetException ex )
      {
      throw new JspException( "Error - component.PutAttributeTag : Error while retrieving value from bean '"
                              + beanName + "' with property '"
                              + beanProperty + "' in scope '"
                              + beanScope + "'. (exception : "
                              + ex.getMessage() );
      }
     catch( IllegalAccessException ex )
      {
      throw new JspException( "Error - component.PutAttributeTag : Error while retrieving value from bean '"
                              + beanName + "' with property '"
                              + beanProperty + "' in scope '"
                              + beanScope + "'. (exception : "
                              + ex.getMessage() );
      }
    }

    /**
     * Store bean in requested context.
     * If scope is null, save in REQUEST_SCOPE context.
     *
     * @param pageContext current pageContext
     * @param name Name of the bean
     * @param scope Scope under which bean is saved (page, request, session, application)
     *  or <code>null</code> to store in <code>request()</code> instead
     * @param value Bean value to store
     *
     * @exception JspException if an invalid scope name
     *  is requested
     */
  static public void setAttribute( PageContext pageContext, String name, Object value, String scope)
           throws JspException
    {
        if (scope == null)
            pageContext.setAttribute(name, value, PageContext.REQUEST_SCOPE);
        else if (scope.equalsIgnoreCase("page"))
            pageContext.setAttribute(name, value, PageContext.PAGE_SCOPE);
        else if (scope.equalsIgnoreCase("request"))
            pageContext.setAttribute(name, value, PageContext.REQUEST_SCOPE);
        else if (scope.equalsIgnoreCase("session"))
            pageContext.setAttribute(name, value, PageContext.SESSION_SCOPE);
        else if (scope.equalsIgnoreCase("application"))
            pageContext.setAttribute(name, value, PageContext.APPLICATION_SCOPE);
        else {
            throw new JspException( "Error - bad scope name '" + scope + "'");
        }
    }

    /**
     * Store bean in REQUEST_SCOPE context.
     *
     * @param pageContext current pageContext
     * @param name Name of the bean
     * @param value Bean value to store
     *
     * @exception JspException if an invalid scope name
     *  is requested
     */
  static public void setAttribute( PageContext pageContext, String name, Object beanValue)
           throws JspException
    {
    pageContext.setAttribute(name, beanValue, PageContext.REQUEST_SCOPE);
    }

    /**
     * Save the specified exception as a request attribute for later use.
     *
     * @param pageContext The PageContext for the current page
     * @param exception The exception to be saved
     */
  public static void saveException(PageContext pageContext, Throwable exception)
    {
    pageContext.setAttribute(Action.EXCEPTION_KEY, exception, PageContext.REQUEST_SCOPE);
    }

    /**
     * Get component definition by its name.
     * @name Definition name
     * @param pageContext The PageContext for the current page
     * @throws JspException -
     */
  public static ComponentDefinition getComponentDefinition(String name, PageContext pageContext)
    throws JspException
    {
    try
      {
      return  DefinitionsUtil.getDefinition(name, pageContext);
      }
     catch( NoSuchDefinitionException ex )
        {
        throw new JspException ( "Error : Can't get component definition for '"
                               + name
                               + "'. Check if this name exist in component definitions." );
        }
     catch( FactoryNotFoundException ex )
        { // factory not found.
        throw new JspException ( ex.getMessage() );
        } // end catch
     catch( DefinitionsFactoryException ex )
        {
        if(debug)
          ex.printStackTrace( );
          // Save exception to be able to show it later
        saveException( pageContext, ex);
        throw new JspException ( ex.getMessage() );
        } // end catch
    }

}





















