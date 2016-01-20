# Available plugins #

XM4WAS provides the following plugins for WebSphere Application Server. The JAR files for these plugins can be found in the `plugins` directory in the binary distribution.

| Plugin | Description |
|:-------|:------------|
| `com.googlecode.xm4was.commons` | Contains common code used by the other plugins |
| `com.googlecode.xm4was.ibm-jre-extensions` | Exposes internal classes of the IBM JRE to other plugins |
| `com.googlecode.xm4was.clmon` | ClassLoaderMonitorPlugin |
| `com.googlecode.xm4was.jmx` | JmxPlugin   |
| `com.googlecode.xm4was.logging` | LoggingPlugin |
| `com.googlecode.xm4was.pmi` | PmiPlugin   |
| `com.googlecode.xm4was.ejbmon.*` | Offers MBean to check initialization of stateless session beans |
| `com.googlecode.xm4was.threadmon` | Monitoring of unmanaged threads |


Note that it is not necessary to install all plugins. Only `com.googlecode.xm4was.commons` and, from version 0.4.0.beta12 upwards, `com.googlecode.xm4was.ibm-jre-extensions` are mandatory because they are used by the other plugins.

# Manual installation/upgrade #

## First installation ##

After selecting the set of plugins that you want to install, copy the corresponding JARs to the existing `plugins` directory under the WebSphere installation directory (e.g. `/opt/IBM/WebSphere/AppServer`). To enable the plugins, restart the WebSphere instance(s). Please refer to the documentation of the individual plugins to select the plugins you need.

## Upgrading ##

To upgrade from a previous XM4WAS version, first delete all `com.googlecode.xm4was.*` JAR files from the `plugins` directory. Then copy the new JARs as described in the previous section. Before restarting WebSphere, execute the `bin/osgiCfgInit.(sh|bat)` script found in the profile directory (e.g. `/opt/IBM/WebSphere/AppServer/profiles/AppSrv01`).

**Note:** restarting the server(s) without prior execution of `osgiCfgInit` may result in failures (typically `ClassNotFoundException`s) during server startup.

# Installation/upgrade using the `install.sh` script #

The `install.sh` script shipped with the binary distribution automates the steps described above. To use the script, unzip the binary distribution and execute the script with the user account that is the owner of the WebSphere installation. The script requires a single argument, which is the WebSphere installation directory (e.g. `/opt/IBM/WebSphere/AppServer`).

# Uninstallation #

To uninstall the plugins, simply delete the corresponding JAR files from the `plugins` directory under the WebSphere installation directory and restart WebSphere.

# Troubleshooting #

## Checking that the plugins are loaded ##

During startup, most of the plugins will emit at least one log message at INFO level. If the plugins are loaded correctly, then these log messages should appear in `SystemOut.log`. Note that all messages emitted by the XM4WAS plugins have a key that starts with `XM` and that identifies the plugin. Here are some typical messages that can be seen during startup:

```xml

[18/06/12 23:19:22:812 CEST] 00000000 JmxConnector  I   XMJMX0001I: Starting the JRMP JMX connector on service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi
[18/06/12 23:19:22:843 CEST] 00000000 JmxConnector  I   XMJMX0004I: JRMP JMX connector security not enabled.
[18/06/12 23:19:23:359 CEST] 00000000 PlatformMXBea I   XMJMX0101I: Registered 14 platform MXBeans
...
[18/06/12 23:19:25:109 CEST] 00000000 LoggingServic I   XMLOG0001I: Extended Logging Service started.
...
[18/06/12 23:19:26:734 CEST] 00000000 AccessChecker I   XMJMX0105I: Access control for platform MXBeans not enabled
...
[18/06/12 23:19:45:265 CEST] 00000000 ClassLoaderMo I   XMCLM0001I: Class loader monitor started
[18/06/12 23:19:45:578 CEST] 00000000 ProcStatsComp I   XMPMI0101I: The /proc filesystem doesn't exist on this platform
...
[18/06/12 23:20:37:468 CEST] 00000013 ClassLoaderMo I   XMCLM0003I: Class loader stats: created=19; stopped=1; destroyed=0```

## Determining why the plugins are not loaded ##

To determine why an XM4WAS plugin can't be loaded, start the `osgiConsole` tool shipped with WebSphere and use the `ss` (short status) command to list the available plugins. Note that WebSphere doesn't need to be running to use that tool. If the plugins are loaded correctly, then they should all appear in the list produced by `ss` with status `RESOLVED`, as shown in the following example:

```xml

osgi> ss
id      State       Bundle
...
6       RESOLVED    com.googlecode.xm4was.clmon_0.3.0
7       RESOLVED    com.googlecode.xm4was.commons_0.3.0
8       RESOLVED    com.googlecode.xm4was.jmx_0.3.0
9       RESOLVED    com.googlecode.xm4was.logging_0.3.0
10      RESOLVED    com.googlecode.xm4was.pmi_0.3.0
...```

If there is a problem, then the state will be `INSTALLED` instead:

```xml

6       INSTALLED   com.googlecode.xm4was.clmon_0.3.0```

To determine why the bundle for a given plugin can't be resolved, attempt to start it using the `start` command with the bundle ID as parameter:

```xml

osgi> start 6
org.osgi.framework.BundleException: The bundle could not be resolved. Reason: Missing Constraint: Import-Package: com.googlecode.xm4was.commons; version="0.0.0"
at org.eclipse.osgi.framework.internal.core.BundleHost.startWorker(BundleHost.java:294)
at org.eclipse.osgi.framework.internal.core.AbstractBundle.start(AbstractBundle.java:256)
at org.eclipse.osgi.framework.internal.core.FrameworkCommandProvider._start(FrameworkCommandProvider.java:239)
at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:60)
at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:37)
at java.lang.reflect.Method.invoke(Method.java:611)
at org.eclipse.osgi.framework.internal.core.FrameworkCommandInterpreter.execute(FrameworkCommandInterpreter.java:145)
at org.eclipse.osgi.framework.internal.core.FrameworkConsole.docommand(FrameworkConsole.java:293)
at org.eclipse.osgi.framework.internal.core.FrameworkConsole.console(FrameworkConsole.java:278)
at org.eclipse.osgi.framework.internal.core.FrameworkConsole.run(FrameworkConsole.java:213)
at java.lang.Thread.run(Thread.java:736)```

In the example shown here, the `com.googlecode.xm4was.clmon` plugin can't be loaded because the `com.googlecode.xm4was.commons` plugin is not installed.