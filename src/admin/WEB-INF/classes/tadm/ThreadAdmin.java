package tadm;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 *
 */
public class ThreadAdmin extends TagSupport {
    
    public ThreadAdmin() {}

    public int doStartTag() throws JspException {
	return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
	return EVAL_PAGE;
    }

    // -------------------- Implementation --------------------

    public static Thread[] findThreads() {
	Thread thr=Thread.currentThread();
	ThreadGroup tg=thr.getThreadGroup();
	while( tg.getParent() != null ) {
	    tg=tg.getParent();
	}
	// tg is now the top thread group
	int count=tg.activeGroupCount() + 5;
	ThreadGroup childs[] = new ThreadGroup[count];

	int tcount=tg.activeCount() + 5;
	Thread threads[]=new Thread[tcount];

	int realC=tg.enumerate( threads, true );
	Thread realThreads[]=new Thread[realC ];
	System.arraycopy( threads, 0, realThreads, 0, realC );
	return realThreads;
    }
}
