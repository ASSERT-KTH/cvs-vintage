/*
 */


package org.apache.struts.tiles.actions;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentDefinition;
import org.apache.struts.tiles.DefinitionsFactoryException;
import org.apache.struts.tiles.DefinitionsUtil;
import org.apache.struts.tiles.FactoryNotFoundException;
import org.apache.struts.tiles.NoSuchDefinitionException;


/**
 * <p>An <strong>Action</strong> that dispatches to a Tiles Definition
 * that is named by the request parameter whose name is specified
 * by the <code>parameter</code> property of the corresponding
 * ActionMapping.
 * This action is usefull in following situations :
 * <li>
 * <ul>To associate an Url to a definition</ul>
 * <ul>To use Struts <link> tag on a definition</ul>
 * </li>
 * <p>To configure the use of this action in your
 * <code>struts-config.xml</code> file, create an entry like this:</p>
 *
 * <code>
 *   &lt;action path="/saveSubscription"
 *           type="org.apache.struts.tiles.actions.DefinitionDispatcherAction"
 *           parameter="def"/&gt;
 *     &lt;forward name="success"   path="anything" //&gt;
 *     &lt;forward name="error"     path="path.to.error.page" //&gt;
 * </code>
 *
 * <p>which will use the value of the request parameter named "def"
 * to pick the appropriate definition name.
 * <p>  The value for success doesn't matter. The forward will forward to
 * appropriate definition.
 * <p> The value for error should denote a valid jsp path or definition name.
 *
 * @author Niall Pemberton <niall.pemberton@btInternet.com>
 * @author Craig R. McClanahan
 * @author Cedric Dumoulin
 * @version $Revision: 1.3 $ $Date: 2002/11/16 06:04:28 $
 */

public final class DefinitionDispatcherAction extends Action {

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param servlet The ActionServlet making this request
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward perform(
                                 ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
                          throws IOException, ServletException
    {
        // Identify the request parameter containing the method name
        // If none defined, use "def"
        String parameter = mapping.getParameter();
        if (parameter == null) {
          parameter = "def";
        }

        // Identify the method name to be dispatched to
        String name = request.getParameter(parameter);
        if (name == null)
          {
          String msg = "Definition dispatcher action : can't get parameter '"
                           + parameter + "'.";
          printError( response, msg );
          return (null);
          }

        // Try to dispatch to requested definition
      try
        {
          // Read definition from factory, but we can create it here.
        ComponentDefinition definition = DefinitionsUtil.getDefinition( name, request, getServlet().getServletContext() );
        System.out.println("get Definition " + definition );
        DefinitionsUtil.setActionDefinition( request, definition);
        }
       catch( FactoryNotFoundException ex )
        {
        printError( response, "Error - DefinitionDispatcherAction : Can't get definition factory.");
        return (mapping.findForward("error"));
        }
       catch( NoSuchDefinitionException ex )
        {
        printError( response, "Error - DefinitionDispatcherAction : Can't get definition '" + name +"'.");
        return (mapping.findForward("error"));
        }
       catch( DefinitionsFactoryException ex )
        {
        printError( response, "Error - DefinitionDispatcherAction : General Factory error '" + ex.getMessage() +"'.");
        return (mapping.findForward("error"));
        }
       catch( Exception ex )
        {
        printError( response, "Error - DefinitionDispatcherAction : General error '" + ex.getMessage() +"'.");
        return (mapping.findForward("error"));
        }

   return (mapping.findForward("success"));

   }

  protected void printError( HttpServletResponse response, String msg )
      throws IOException
    {
    response.setContentType("text/plain");
    PrintWriter writer = response.getWriter();
    writer.println(msg);
    writer.flush();
    writer.close();
    }
}

