# Synopsis #

Registers the platform MXBeans in WebSphere's MBean server and optionally adds a JRMP JMX connector.

# Installation #

See PluginInstallation for general instructions about plugin installation.

The features provided by this plugin apply to application server processes, nodes agents and deployment managers.

# Dependencies #

The `com.googlecode.xm4was.jmx` plugin only depends on the `com.googlecode.xm4was.commons` plugin.

# Features #

## Cross-registration of platform MXBeans ##

The plugin registers the platform MXBeans in WebSphere's MBean server, so that they can be accessed by a remote client using one of the JMX connectors provided by WebSphere. See WebSphereJmx for more information about the platform MXBeans and why accessing them remotely requires cross-registration in WebSphere's MBean server. See [VisualVMHowTo](VisualVMHowTo.md) for an example of how to use this feature to monitor a WebSphere JVM using standard JMX tools.

To ensure compatibility with tools such as JConsole and VisualVM, the platform MXBeans are not routable. This means that they can only be accessed using a direct connection to the WebSphere instance. It is not possible to access them through an administrative agent; in particular it is not possible to connect to the deployment manager and access the platform MXBeans on an application server in the cell.

The platform MXBeans registered by the plugin are properly secured. If security is enabled in the WebSphere cell, then access to the MXBeans requires `monitor` role for operations that don't modify the state of the JVM and `operator` role for operations that modify the state of the JVM or that may have a significant performance impact.

## JRMP JMX connector ##

This feature is currently incomplete and undocumented. It is disabled by default. It will be documented in a future release.

# Known issues and limitations #

  * The JRMP JMX connector should not be enabled on an administrative agent (deployment manager or node agent) because it is affected by the same flaw as the JSR-160 RMI connector described in JmxClientConnector.