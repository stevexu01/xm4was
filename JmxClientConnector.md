# Synopsis #

Connects a standard JMX client to a WebSphere server using the protocols (RMI or SOAP) supported by the AdminClient API. It is known to work with JConsole and [VisualVM](VisualVMHowTo.md).

# Rationale #

There are two ways supported by IBM to connect to a WebSphere server using JMX:
  * Using the proprietary AdminClient API. This API supports multiple protocols, in particular RMI/IIOP and SOAP.
  * Using the JSR-160 RMI connector which implements the standard JMX API. This connector uses a different protocol (namely the protocol defined by JSR-160), although the underlying communication protocol is still IIOP (when connecting to a WebSphere server; other servers may use JRMP).

This means that it is not possible to connect a standard JMX client (i.e. a client that only uses the JMX API and no vendor extensions or proprietary APIs) to WebSphere using any protocol other than JSR-160.

There are a couple of issues with that:
  * IIOP often causes issues if there are firewalls between the client and the server (which is often the case when connecting management or monitoring tools using JMX). On the other hand, SOAP is much more "network friendly".
  * The server side code for the JSR-160 connector in WebSphere is based on code in the JRE which implements the remote notification listener feature literally as described in section 13.4 of the JMX 1.4 specification. This means that when the first remote notification listener is registered, the connector will internally register a notification listener on each and every MBean. This is problematic if one connects to a deployment manager or node agent because the MBean servers in these WebSphere instances federate the MBean servers of downstream servers (e.g. the MBean server in the deployment manager gives access to all MBeans in all WebSphere instances in the cell). The result is that the connector will register listeners on all MBeans in every downstream server. As a side effect, the downstream servers will start propagate all JMX notifications to upstream servers, even if there is no client consuming them. This may cause unnecessary consumption of CPU and network resources.

In addition, both types of client connectors (AdminClient and JSR-160) require setting up a couple of property files (`ssl.client.props` and `sas.client.props`) to be able to connect to a secured WebSphere server. This often requires some tweaking and becomes even more difficult when attempting to run these connectors on a Sun/Oracle JRE.

jmx-client-connector solves these issues in the following way:
  * It provides a bridge between the standard JMX API (namely `javax.management.remote.JMXConnectorProvider`) and the AdminClient API. This makes it possible to use protocols supported by AdminClient with a standard JMX client.
  * It simplifies configuration by automatically creating default `ssl.client.props`, `sas.client.props` and `soap.client.props` files appropriate for IBM and Sun/Oracle JREs.

# Usage #

## Supported JMX service URLs ##

The JMX service URLs for jmx-client-connector have the following form:

```
service:jmx:<protocol>://<host>:<port>/
```

Supported protocols are `wsrmi` and `wssoap`. The following table shows the relationship between the protocol identifier used by jmx-client-connector, the protocol name used by AdminClient and the port to choose from the WebSphere configuration:

| Protocol (jmx-client-connector) | Protocol (AdminClient) | WebSphere port |
|:--------------------------------|:-----------------------|:---------------|
| `wsrmi`                         | `RMI`                  | `ORB_LISTENER_ADDRESS` or `BOOTSTRAP_ADDRESS` |
| `wssoap`                        | `SOAP`                 | `SOAP_CONNECTOR_ADDRESS` |

E.g. for a local WebSphere server configured with the default ports, the possible JMX service URLs would be as follows:

```
service:jmx:wsrmi://localhost:2809/
service:jmx:wssoap://localhost:8880/
```

## Required system properties ##

The `jmx.remote.protocol.provider.pkgs` system property must be set so that JMX can locate the connector:

```
jmx.remote.protocol.provider.pkgs=com.googlecode.xm4was.jmx.client
```

You may also need to change the `java.security.properties` system property in order to enable the signer exchange prompt for SOAP. This is explained in more detail below.

## Required JARs ##

In addition to `jmx-client-connector-<version>.jar`, the following JAR from the WAS runtime must be added to the classpath:

  * `runtimes/com.ibm.ws.admin.client_*.jar`

This is enough when running the connector on an IBM JRE. When running on a Sun/Oracle JRE, the following JARs from the WAS runtime are also required:

  * `runtimes/com.ibm.ws.ejb.thinclient_*.jar`
  * `runtimes/com.ibm.ws.orb_*.jar`
  * `java/jre/lib/ibmpkcs.jar`
  * `java/jre/lib/ext/ibmkeycert.jar`
  * `java/jre/lib/ext/ibmjceprovider.jar`

The first two JARs contain the IBM ORB which is required by AdminClient. `ibmpkcs.jar` and `ibmkeycert.jar` are required to automatically add entries to the trust store (`trust.jks`). Finally, `ibmjceprovider.jar` is required to automatically create the `key.jks` file.

## Recommended server-side configuration ##

When connecting to WebSphere using RMI/IIOP, it is recommended to configure the `com.ibm.CORBA.EnableServerKeepAlive` system property on those instances. Setting this property avoids the problem described in [PK37506](http://www-01.ibm.com/support/docview.wss?uid=swg1PK37506). It enables the `SO_KEEPALIVE` socket option on the server side of IIOP connections. This ensures that broken connections are detected and that the corresponding resources can be released. If this setting is not enabled, a broken connection may result in an IIOP reader thread permanently blocking on a socket read operation in the server.

## Configuration files ##

During the first usage, the connector will automatically create default configuration files in `${user.home}/.xm4was`. This directory will also contain the key and trust stores maintained by the connector. Note that for some part of the configuration, different sets of configuration files are used on IBM and Sun/Oracle JREs.

The locations of the individual configuration files can be changed using the `com.ibm.SSL.ConfigURL`, `com.ibm.CORBA.ConfigURL` and `com.ibm.SOAP.ConfigURL` system properties. Please refer to the WebSphere documentation for more information about these system properties and the content of the configuration files.

## Enabling the SSL signer exchange prompt for SOAP (XM4WAS 0.3.1 and above) ##

When connecting using RMI to a secured WebSphere instance that has an SSL certificate that is not yet trusted, AdminClient will propose to automatically add the certificate to the trust store. This is a particularly useful feature because it greatly simplifies the management of SSL certificates. However, this feature is not supported out of the box for SOAP. To enable it for SOAP it is necessary to configure a custom `SSLSocketFactory` using the `ssl.SocketFactory.provider` security property. Execute the following steps to set this up:
  1. Create a `java.security` file with the following content:
```
ssl.SocketFactory.provider=com.googlecode.xm4was.jmx.client.WSSSLSocketFactory
```
  1. Set the `java.security.properties` system property to the location of the file created in the previous step.

# Known issues and limitations #

  * Configuration files are created in `${user.home}/.xm4was` which is not the correct location on Windows.