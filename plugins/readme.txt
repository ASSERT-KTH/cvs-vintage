************** Plugin Build System **************

 example build-files which can be placed in a plugin directory
---------------------------------------------------------------

- build_example_properties (build properties)
- build_example.xml (ant build script)

To make this work you have to edit at least the "plugin_id" property in
build_example_properties. Then rename to build.properties. Additionally,
rename build_example.xml to build.xml. Simply enter "ant" to build the
plugin.



 ant script to build a single plugin
---------------------------------------

- build_plugin.xml

plugin_src property must be set to the name of the plugin

Example: "ant -f build_plugin.xml -Dplugin_src=org.columba.example.HelloWorldAction release"


 ant script to build all plugins 
----------------------------------

- build.xml

