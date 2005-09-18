************** Plugin System **************

 example plugin-files which must be placed in a plugin directory
---------------------------------------------------------------

- build_example_properties (build properties)
- plugin_example.xml (plugin registration)

To make this work you have to edit at least the "plugin_id" property in
build_example_properties. Then rename to build.properties. 
Also copy the plugin_example.xml to your plugin directory. Edit it
to register the implemented extensions and supply some more information
about your plugin.

See also the Columba homepage for more information on the plugin development
process.



 ant command to build a single plugin
---------------------------------------

!You must run the build process from within the main columba directory!

The plugin_dir property must be set to the name of the plugin.

Example: "ant -Dplugin_dir=org.columba.example.HelloWorldAction plugin"


 ant command to build all plugins 
----------------------------------
!You must run the build process from within the main columba directory!

Note: This includes only the plugins that are listed in the plugins/build.xml script.

"ant plugins"

