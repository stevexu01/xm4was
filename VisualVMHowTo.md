# Prerequisites #

  * An installation of [VisualVM](http://visualvm.java.net/).
  * An installation of WebSphere Application Server. Since only a couple of libraries are required, there is no need to install WAS locally. The required libraries can be copied from another system.

The procedure described in this document has been tested with the following product versions:
  * VisualVM 1.3.2, 1.3.3 and 1.3.4 (older versions should work as well).
  * Libraries from WebSphere 7.0, 8.0 and 8.5.
  * Connections to WebSphere 6.1, 7.0, 8.0 and 8.5.

# Adding the XM4WAS JMX client connector JARs (Required) #

Add the following JARs to the `platform/lib` folder in the VisualVM installation directory:
  * `jmx-client-connector-<version>.jar` from the XM4WAS distribution.
  * The following JARs from a WAS 7.0, 8.0 or 8.5 installation:
    * `runtimes/com.ibm.ws.admin.client_*.jar`
    * `runtimes/com.ibm.ws.ejb.thinclient_*.jar`
    * `runtimes/com.ibm.ws.orb_*.jar`
    * `java/jre/lib/ibmpkcs.jar`
    * `java/jre/lib/ext/ibmkeycert.jar`
    * `java/jre/lib/ext/ibmjceprovider.jar`
Note that it is **not** required that the version of the WebSphere libraries matches the version of the server to connect to. However, it is strongly recommended to use the libraries from a recent fix pack level of any of the supported WAS releases.

If you are planning to use the VisualVM-MBeans plugin and you are using the libraries from WAS 8.0 or 8.5, then you should also add the following JAR to `platform/lib`:
  * `plugins/javax.j2ee.management.jar`
This JAR contains the classes from the `javax.management.j2ee` package and is necessary to display the attribute values of certain WebSphere MBeans. In WAS 7.0 these classes are already included in the admin client JAR.

# Setting system properties (Required) #

Open the `etc/visualvm.conf` file and add the following option to the `default_options` property:

```
-J-Djmx.remote.protocol.provider.pkgs=com.googlecode.xm4was.jmx.client
```

# Installing the com.googlecode.xm4was.jmx plugin on WAS (Optional) #

VisualVM is typically used to monitor the JVM. However, the platform MXBeans that give access to the relevant data are not registered in WebSphere's MBean server. Install the [com.googlecode.xm4was.jmx plugin](JmxPlugin.md) to register these MBeans and to enable JVM monitoring in VisualVM.

This should enable CPU, heap, class and thread monitoring as shown in the following screenshot:

![http://xm4was.googlecode.com/svn/wiki/visualvm-monitor-was.png](http://xm4was.googlecode.com/svn/wiki/visualvm-monitor-was.png)

# Installing the VisualVM-MBeans plugin (Optional) #

WebSphere exposes a rich set of MBeans that allow to monitor and manage various parts of the server. To get access to these MBeans, install the [VisualVM-MBeans](http://visualvm.java.net/mbeans_tab.html) plugin. This plugin integrates JConsole's MBeans tab functionality into VisualVM, as shown in the following screenshot:

![http://xm4was.googlecode.com/svn/wiki/visualvm-mbeans-was.png](http://xm4was.googlecode.com/svn/wiki/visualvm-mbeans-was.png)

# Enabling the SSL signer exchange prompt for SOAP (Optional) #

If you plan to use SOAP to connect to WebSphere instances with security enabled, then you may want to enable automatic management of the trust store for that protocol. This is supported in XM4WAS 0.3.1 and above. For more details refer to the [jmx-client-connector documentation](JmxClientConnector.md).

To configure this for VisualVM, create `etc/java.security` (under the VisualVM installation directory) with the following content:

```
ssl.SocketFactory.provider=com.googlecode.xm4was.jmx.client.WSSSLSocketFactory
```

Then open the `etc/visualvm.conf` file and change the `default_options` property to set the `java.security.properties` system property to the location of the file created in the previous step. Under Windows [you need to specify the absolute path of the file](http://stackoverflow.com/questions/13346214/how-to-configure-a-netbeans-rcp-application-with-a-custom-java-security-file), while under Linux, you can use the `BASEDIR` variable to avoid that:

```
-J-Djava.security.properties=${BASEDIR}/etc/java.security
```

# Creating connections to WebSphere #

To add a WebSphere instance to VisualVM, simply create a remote JMX connection that uses a JMX service URL with of one of the forms described in the [documentation](JmxClientConnector.md) of jmx-client-connector:

![http://xm4was.googlecode.com/svn/wiki/visualvm-add-jmx-connection.png](http://xm4was.googlecode.com/svn/wiki/visualvm-add-jmx-connection.png)

Note that when security is enabled, the signer certificate for the WebSphere instance must be added to the trust store. The connector will propose to add the certificate automatically:

![http://xm4was.googlecode.com/svn/wiki/signer-exchange.png](http://xm4was.googlecode.com/svn/wiki/signer-exchange.png)

This always works for RMI connections. For SOAP connections, this only works if you implemented the configuration changes described in the previous section. If this is not the case, then the certificate must be added manually. Alternatively, add an RMI connection first so that the certificate is added automatically (the same certificate is used for RMI and SOAP).

Another important thing to keep in mind when using SOAP is that the proxy settings configured in VisualVM also apply to the SOAP connection to WebSphere.

# Creating a custom VisualVM distribution #

For legal reasons, the XM4WAS project cannot distribute a customized VisualVM distribution that includes all the changes described in this document. However, since VisualVM is packaged as a simple ZIP file, you can easily create such a customized distribution yourself. In that case you may want to consult [this article](http://veithen.blogspot.be/2012/11/installing-visualvm-plug-ins-into.html) for the necessary steps to include the VisualVM-MBeans plug-in in that distribution.

# Known issues and limitations #

  * CPU sampling is not available with VisualVM 1.3.3, but works with 1.3.2 and 1.3.4.
  * The memory sampling feature in VisualVM relies on MBeans that are only available in Sun/Oracle JREs. Therefore this feature will not be available when connecting to a full WebSphere profile (which only supports IBM JREs). Since it is possible to run the WebSphere 8.5 Liberty Profile on a Sun/Oracle JRE, it may be possible to use the memory sampling feature in that setup (but this has not been tested).