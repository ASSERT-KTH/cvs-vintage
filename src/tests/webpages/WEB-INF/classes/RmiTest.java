/* ****************************************************************************
 *
 *  Time-stamp: <2000-03-15 14:02:07 johan>
 *
 * ****************************************************************************/

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

/**
 * Test servlet.
 *
 * @created 2000-03-02
 * @author Johan Georg Granstr&oumlm 
 **/
public final class RmiTest extends HttpServlet
{
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        main(null);
        res.setContentType("text/html");
        PrintWriter w = new PrintWriter(res.getWriter());
        w.println("OK");
        w.close();
    }
    
    public static void main(String[] args) throws ServletException, IOException {
        try {
            Registry r = LocateRegistry.createRegistry(1099);
        } catch (RemoteException e) {
            // JSDK 2.0 compatible...
            e.printStackTrace();
            throw new ServletException(e.getMessage());
        }
        System.out.println("OK");
    }
}
