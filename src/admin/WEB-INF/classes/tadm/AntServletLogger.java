package tadm;
import org.apache.tools.ant.*;

import java.io.*;
import javax.servlet.http.*;

public class AntServletLogger implements BuildLogger {
    protected java.io.Writer out;
    protected java.io.PrintWriter err;

    protected int msgOutputLevel;

    private long startTime;

    protected static String lSep = System.getProperty("line.separator");

    protected boolean emacsMode = false;

    public AntServletLogger() {
	
    }
    
    public void setMessageOutputLevel(int level) {
        this.msgOutputLevel = level;
    }

    public void setWriter(java.io.Writer output) {
        this.out = output;
	this.err = new java.io.PrintWriter(output);
    }

    public void setEmacsMode(boolean emacsMode) {
        this.emacsMode = emacsMode;
    }


    public void setOutputPrintStream(PrintStream output) {
	System.out.println("What the heck ");
    }

    public void setErrorPrintStream(PrintStream err) {
	System.out.println("What the heck ");     
    }

    public void buildStarted(BuildEvent event) {
        startTime = System.currentTimeMillis();
    }

    public void buildFinished(BuildEvent event) {
        try {
	    Throwable error = event.getException();
	    
	    if (error == null) {
		out.write(lSep + "BUILD SUCCESSFUL");
	    }
	    else {
		err.write(lSep + "BUILD FAILED" + lSep);
		
		if (error instanceof BuildException) {
		    err.write(error.toString());
		    
                Throwable nested = ((BuildException)error).getException();
                if (nested != null) {
                    nested.printStackTrace(err);
                }
		}
		else {
		    error.printStackTrace(err);
		}
	    }
	    
	    out.write(lSep + "Total time: " +
		      (System.currentTimeMillis() - startTime));
	    out.flush();
	} catch( IOException ex ) {
	    ex.printStackTrace();
	}
    }

    public void targetStarted(BuildEvent event) {
        try {
	    out.write("<h3>"+ event.getTarget().getName() + "</h3>");
	    out.flush();
	} catch(IOException ex ) {
	    ex.printStackTrace();
	}
    }

    public void targetFinished(BuildEvent event) {
        try {
	    out.write("<hr>");
	    out.flush();
	} catch(IOException ex ) {
	    ex.printStackTrace();
	}
    }

    public void taskStarted(BuildEvent event) {
	
    }
    
    public void taskFinished(BuildEvent event) {

    }

    public void messageLogged(BuildEvent event) {
        try {
	    if( event.getPriority() > 2 )
		return;

	    out.write("\n<br>");
	    // Print the message
	    String msg=event.getMessage();
	    if( msg.startsWith( "ERROR" )) {
		out.write("<font color='red'>");
	    }
	    if( msg.startsWith("GOT" )) {
		out.write("<pre>");
	    }
	    if( msg.startsWith("FAIL" )) {
		out.write("</pre>");
	    }
	    out.write(event.getMessage());
	    
	    if( msg.startsWith( "ERROR" )) {
		out.write("</font>");
	    }
	    out.flush();
	} catch(IOException ex ) {
	    ex.printStackTrace();
	}
    }

}
