package org.apache.strutsel.taglib.html;

import java.util.Enumeration;
import java.util.HashMap;
import org.apache.strutsel.taglib.utils.JspTagTestCase;
import org.apache.strutsel.taglib.utils.TestHelper;
import org.apache.strutsel.taglib.utils.DOMHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.GenericServlet;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Element;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionFormBean;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ApplicationConfig;
import org.apache.struts.config.FormBeanConfig;

public class TestELFormTag extends JspTagTestCase 
{

    protected static final String   ACTION_NAME   = "stuff";
    protected static final String   FORM_BEAN_CLASS   =
        "org.apache.strutsel.taglib.utils.TestFormBean";

    protected static final String REQUIRED_NAME_VALUE_KEY   = 
            "RequiredNameValue";
    protected static final String REQUIRED_METHOD_VALUE_KEY   = 
            "RequiredMethodValue";
    protected static final String REQUIRED_ACTION_VALUE_KEY   = 
            "RequiredActionValue";

    protected ELFormTag elFormTag   = null;

    private static Log  log   = LogFactory.getLog(TestELFormTag.class);

    public TestELFormTag(String theName) {
        super(theName);
    }

    public static void main(String[] args) {
        junit.awtui.TestRunner.main(
                new String[] { TestELFormTag.class.getName() });
    }

    public static Test suite() {
        return new TestSuite(TestELFormTag.class);
    }

    public void setUp() {
        elFormTag = new ELFormTag();
        elFormTag.setPageContext(pageContext);
    }

    /**
     * Converts an action name into the path associated with it.  The basic
     * contents of this function was copied from
     * "FormTag.getActionMappingName()".
     */
    protected  String   convertActionNameToPath(String actionName)
    {
        String value = actionName;

        int questionLoc = actionName.indexOf("?");
        if (questionLoc >= 0)
            value = value.substring(0, questionLoc);

        int slashLoc = value.lastIndexOf("/");
        int periodLoc   = value.lastIndexOf(".");

        if ((periodLoc >= 0) && (periodLoc > slashLoc))
            value = value.substring(0, periodLoc);

        if (!value.startsWith("/"))
            value = "/" + value;

        return (value);
    }

    /**
     * Return the form action converted into a server-relative URL.
     * <p>
     * This is copied from FormTag, with the small change of passing the action
     * name as a parameter, and calling
     * <code>convertActionNameToPath(actionName)</code> instead of the
     * original's <code>getActionMappingName()</code>.
     */
    protected String getActionMappingURL(String actionName) {

        HttpServletRequest request =
            (HttpServletRequest) pageContext.getRequest();
        StringBuffer value = new StringBuffer(request.getContextPath());
        ApplicationConfig config = (ApplicationConfig)
            pageContext.getRequest().getAttribute(Action.APPLICATION_KEY);
        if (config != null) {
            value.append(config.getPrefix());
        }

        // Use our servlet mapping, if one is specified
        String servletMapping = (String)
            pageContext.getAttribute(Action.SERVLET_KEY,
                                     PageContext.APPLICATION_SCOPE);
        if (servletMapping != null) {
            String queryString = null;
            int question = actionName.indexOf("?");
            if (question >= 0) {
                queryString = actionName.substring(question);
            }
            String actionMapping = convertActionNameToPath(actionName);
            if (servletMapping.startsWith("*.")) {
                value.append(actionMapping);
                value.append(servletMapping.substring(1));
            } else if (servletMapping.endsWith("/*")) {
                value.append(servletMapping.substring
                             (0, servletMapping.length() - 2));
                value.append(actionMapping);
            } else if (servletMapping.equals("/")) {
                value.append(actionMapping);
            }
            if (queryString != null) {
                value.append(queryString);
            }
        }

        // Otherwise, assume extension mapping is in use and extension is
        // already included in the action property
        else {
            if (!actionName.startsWith("/")) {
                value.append("/");
            }
            value.append(actionName);
        }

        // Return the completed value
        return (value.toString());
    }

//     protected void setupServletEnvironment()
//         throws ServletException
//     {
//         ActionServlet   actionServlet  = new ActionServlet();
//         actionServlet.init(pageContext.getServletConfig());
//         actionServlet.init();

//         ApplicationConfig  appConfig   = new ApplicationConfig("");

//         pageContext.setAttribute(Action.APPLICATION_KEY, appConfig,
//                                  PageContext.APPLICATION_SCOPE);
//     }
    
    public void testPlain()
        throws ServletException, JspException 
    {
        ActionServlet   actionServlet  = new ActionServlet();
        actionServlet.init(pageContext.getServletConfig());
        actionServlet.init();

        ApplicationConfig  appConfig   = new ApplicationConfig("");

        pageContext.setAttribute(Action.APPLICATION_KEY, appConfig,
                                 PageContext.APPLICATION_SCOPE);
        
        ActionMapping   mapping  = new ActionMapping();
        mapping.setName(ACTION_NAME);
        mapping.setPath(convertActionNameToPath(mapping.getName()));

        ActionFormBean  formBean =
            new ActionFormBean(mapping.getName(), FORM_BEAN_CLASS);

        appConfig.addActionConfig(mapping);
        appConfig.addFormBeanConfig(formBean);

        HttpServletResponse response   =
            (HttpServletResponse)pageContext.getResponse();

        String encodedURL  =
            response.encodeURL(getActionMappingURL(mapping.getName()));

        response.addHeader(REQUIRED_NAME_VALUE_KEY, ACTION_NAME);
        response.addHeader(REQUIRED_METHOD_VALUE_KEY, "POST");
        response.addHeader(REQUIRED_ACTION_VALUE_KEY, encodedURL);

        elFormTag.setAction(ACTION_NAME);

        int startTagReturn = elFormTag.doStartTag();
        int endTagReturn   = elFormTag.doEndTag();
    }

    public void endPlain(com.meterware.httpunit.WebResponse testResponse) {
        try {
            TestHelper.printResponse(testResponse);

            Element docElement = testResponse.getDOM().getDocumentElement();
            DOMHelper.printNode(docElement);

            HashMap attrMap = new HashMap();

            DOMHelper.recordFoundAttributes(testResponse.getDOM(), 
                                            "/html/body/form", attrMap);
            DOMHelper.verifyAttributesPresent(attrMap,
                                              new String[] { "name", "method",
                                                             "action" }, 
                                              false);
            checkAttrValue(attrMap, testResponse, REQUIRED_NAME_VALUE_KEY, 
                           "form", "name");
            checkAttrValue(attrMap, testResponse, REQUIRED_METHOD_VALUE_KEY, 
                           "form", "method");
            checkAttrValue(attrMap, testResponse, REQUIRED_ACTION_VALUE_KEY, 
                           "form", "action");
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }

    public void testMethod()
        throws ServletException, JspException 
    {
        ActionServlet   actionServlet  = new ActionServlet();
        actionServlet.init(pageContext.getServletConfig());
        actionServlet.init();

        ApplicationConfig  appConfig   = new ApplicationConfig("");

        pageContext.setAttribute(Action.APPLICATION_KEY, appConfig,
                                 PageContext.APPLICATION_SCOPE);
        
        ActionMapping   mapping  = new ActionMapping();
        mapping.setName(ACTION_NAME);
        mapping.setPath(convertActionNameToPath(mapping.getName()));

        ActionFormBean  formBean =
            new ActionFormBean(mapping.getName(), FORM_BEAN_CLASS);

        appConfig.addActionConfig(mapping);
        appConfig.addFormBeanConfig(formBean);

        HttpServletResponse response   =
            (HttpServletResponse)pageContext.getResponse();

        String encodedURL  =
            response.encodeURL(getActionMappingURL(mapping.getName()));

        response.addHeader(REQUIRED_NAME_VALUE_KEY, ACTION_NAME);
        response.addHeader(REQUIRED_METHOD_VALUE_KEY, "GET");
        response.addHeader(REQUIRED_ACTION_VALUE_KEY, encodedURL);

        elFormTag.setAction(ACTION_NAME);
        elFormTag.setMethod("GET");

        int startTagReturn = elFormTag.doStartTag();
        int endTagReturn   = elFormTag.doEndTag();
    }

    public void endMethod(com.meterware.httpunit.WebResponse testResponse) {
        try {
            TestHelper.printResponse(testResponse);

            Element docElement = testResponse.getDOM().getDocumentElement();
            DOMHelper.printNode(docElement);

            HashMap attrMap = new HashMap();

            DOMHelper.recordFoundAttributes(testResponse.getDOM(), 
                                            "/html/body/form", attrMap);
            DOMHelper.verifyAttributesPresent(attrMap,
                                              new String[] { "name", "method",
                                                             "action" }, 
                                              false);
            checkAttrValue(attrMap, testResponse, REQUIRED_NAME_VALUE_KEY, 
                           "form", "name");
            checkAttrValue(attrMap, testResponse, REQUIRED_METHOD_VALUE_KEY, 
                           "form", "method");
            checkAttrValue(attrMap, testResponse, REQUIRED_ACTION_VALUE_KEY, 
                           "form", "action");
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        }
    }
}
