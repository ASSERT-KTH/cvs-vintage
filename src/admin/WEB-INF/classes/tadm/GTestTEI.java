package tadm;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

public class GTestTEI extends TagExtraInfo {

    public VariableInfo[] getVariableInfo(TagData data) {
	return (new VariableInfo[] {
	    new VariableInfo("gtestTestRevision",
			     "java.lang.String",
			     true,  VariableInfo.AT_BEGIN),
	    new VariableInfo("gtestTestResults",
			     "java.util.Vector",
			     true,  VariableInfo.AT_BEGIN),
	    new VariableInfo("gtestTestFailures",
			     "java.util.Vector",
			     true,  VariableInfo.AT_BEGIN),
	    new VariableInfo("gtestTestSuccess",
			     "java.util.Vector",
			     true,  VariableInfo.AT_BEGIN),
	    new VariableInfo("gtestTestProperties",
			     "java.util.Hashtable",
			     true,  VariableInfo.AT_BEGIN),
	    new VariableInfo("gtestHttpClients",
			     "java.util.Hashtable",
			     true,  VariableInfo.AT_BEGIN),
	});

    }


}
