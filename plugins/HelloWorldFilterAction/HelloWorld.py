import javax.swing as swing
import org.columba.mail.filter.plugins as plugins
#from org.columba.mail.filter import *


class HelloWorld(plugins.AbstractFilterAction):
	
	def getCommand(self, frameController, filterAction, sourceFolder, uids):
		s = filterAction.get("param")
		swing.JOptionPane.showMessageDialog(None,"Hello World:"+s)	
		return None
	




