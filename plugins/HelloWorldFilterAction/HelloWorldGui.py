from org.columba.mail.gui.config.filter import *
from org.columba.mail.gui.config.filter.plugins import DefaultActionRow
import javax.swing as swing


class HelloWorldGui (DefaultActionRow):
	t = swing.JTextField(20)

	def __init__(self, actionList, filterAction):
		DefaultActionRow.__init__(self, actionList, filterAction)

	def initComponents(self):
		DefaultActionRow.initComponents(self)
		DefaultActionRow.addComponent(self, self.t)
		
	def updateComponents(self, updateModel):
		DefaultActionRow.updateComponents(self, updateModel)
		
		if updateModel != 0 :
			# model->gui		
			s = DefaultActionRow.getFilterAction(self).get("param")
			self.t.setText(s)
		else:
			# gui->model		
			s = self.t.getText()
			DefaultActionRow.getFilterAction(self).set("param",s)
		
	