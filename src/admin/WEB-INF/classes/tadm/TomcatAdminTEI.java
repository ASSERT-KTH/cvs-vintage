package tadm;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

public class TomcatAdminTEI extends TagExtraInfo {

    public VariableInfo[] getVariableInfo(TagData data) {
	return (new VariableInfo[] {
	    new VariableInfo("cm", "org.apache.tomcat.core.ContextManager",
			     true,  VariableInfo.AT_BEGIN),
	    new VariableInfo("ctx", "org.apache.tomcat.core.Context",
			     true,  VariableInfo.AT_BEGIN),
	    new VariableInfo("module",
			     "org.apache.tomcat.core.BaseInterceptor",
			     true,  VariableInfo.AT_BEGIN)
	});

    }


}
