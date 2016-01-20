# Internationalization #

## Log message ##

Log messages must be defined using a `ListResourceBundle` and the implementing class must be placed in a package that is exported by the bundle. Resource bundles defined using property files will not work.

# PMI #

## Make custom PMI modules work on the deployment manager ##

  * The `type` attribute in the stats template must correspond to the location (resource name) of the template file.
  * The package containing the template(s) must be exported by the bundle. To avoid split packages, the package name must be unique (this was not the case before XM4WAS 0.3.0).

# Troubleshooting #

## WebSphere component (WsComponent) startup ##

Useful trace spec: `com.ibm.ws.runtime.*=all`

Common mistakes:
  * `plugin.xml` not included in the bundle (see `build.properties`).