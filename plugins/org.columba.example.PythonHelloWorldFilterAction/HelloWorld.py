import javax.swing as swing
import org.columba.mail.filter.plugins as plugins
#from org.columba.mail.filter import *
import os;

class HelloWorld(plugins.AbstractFilterAction):
	
	def getCommand(self, filterAction, sourceFolder, uids):
		s = filterAction.get("param")
		swing.JOptionPane.showMessageDialog(None,"Hello World:"+s)
		print "curdir: "+os.curdir
		print "pardir: "+os.pardir
		print "sep: "+os.sep
		print "line separator: "+os.linesep
		return None
	




