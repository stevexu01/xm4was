# Prerequisites #

  * Local installations of WebSphere 6.1, 7.0 and 8.5 (8.5.0.x or 8.5.5.x). You can use [WebSphere for Developers](http://www.myeclipseide.com/module-htmlpages-display-pid-448.html) versions. Note that you need to update them to recent fixpack levels: using the original releases (especially 6.1.0.0 and 7.0.0.0) will result in build failures.
  * Subversion.
  * Maven 3.x (tested up to 3.2.1)

# Create a P2 repository from the WebSphere runtime bundles #

The XM4WAS Maven (Tycho) build is set up to pull WebSphere dependencies from a P2 repository. Since no public repository exists, it is necessary to create one locally and populate it with the bundles from the WebSphere versions that are supported by XM4WAS, i.e. 6.1, 7.0 and 8.5. An additional problem is that different WAS versions can contain OSGi bundles with the same symbolic name and version, but that are not identical. To fix this problem, it is necessary to modify the bundle versions to include the WAS version.

XM4WAS provides a tool to perform these actions in a single run. The source code can be found at the following location:

https://xm4was.googlecode.com/svn/buildutils/plugin-importer

After checking out the source code from the Subversion repository, build the tool using the following command:

`mvn clean package`

This produces an executable JAR file. The tool expects as arguments the installation directories of the 3 WebSphere versions followed by the URL of the P2 repository to create. The command should look as follows:

`java -jar target/plugin-importer-1-SNAPSHOT.jar /opt/IBM/WebSphere/AppServer-6.1/ /opt/IBM/WebSphere/AppServer-7.0/ /opt/IBM/WebSphere/AppServer-8.5/ file:///path/to/was_repo`

The tool will also download a couple of JARs from an Eclipse P2 repository. If you are behind an HTTP proxy, you may therefore need to set the `http.proxyHost` and `http.proxyPort` system properties accordingly.

# Maven setup #

## Configure the P2 repository location in `settings.xml` ##

The XM4WAS build uses the `was.p2.repo.url` property to locate the P2 repository created in the previous step. You can configure it in `settings.xml` as follows:

```
    <profiles>
        <profile>
            <id>was-p2</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <was.p2.repo.url>file:///path/to/was_repo</was.p2.repo.url>
            </properties>
        </profile>
    </profiles>
```

## Set `JAVA_HOME` ##

The project must be built using the JDK that comes with WAS 7.0 or 8.5. Set the `JAVA_HOME` accordingly (e.g. `/opt/IBM/WebSphere/AppServer/java`).

# Eclipse setup #

**Note:** Eclipse reports incorrectly "Package xxx does not exist in the plugin" errors for the ibm-jre-extensions module because of [bug\_259959](https://bugs.eclipse.org/bugs/show_bug.cgi?id=259959)

## Create a new workspace ##

Configuring Eclipse to work with the XM4WAS sources requires some changes to the workspace settings. It is therefore recommended to create a new workspace.

## Add the WAS JDK and make it the workspace default ##

  1. In the Eclipse preferences, select _Java_ > _Installed JREs_.
  1. Click _Add_.
  1. Select _Standard VM_ and click _Next_.
  1. Set the _JRE home_ to the location of the WAS JDK (7.0 or 8.5). This is the `java` folder under the WebSphere installation directory (e.g. `/opt/IBM/WebSphere/AppServer/java`).
  1. Set the _JRE name_ to _WAS x.y_ (replace _x.y_ with the WebSphere version) and click _Finish_.
  1. Check _WAS x.y_ in the list of installed JREs to make it the workspace default.
  1. In _Java_ > _Installed JREs_ > _Execution Environments_ , select the WAS JDK for the execution environments J2SE-1.5 and JavaSE-1.6

## Creating a target platform definition for WAS ##

  1. In the Eclipse preferences, select _Plug-in Development_ > _Target Platform_.
  1. Click _Add_.
  1. Select _Nothing: Start with an empty target definition_ and click _Next_.
  1. Enter the name for the target platform: _WAS_.
  1. In the _Locations_ tab, click _Add_.
  1. Select _Directory_ and click _Next_.
  1. Set the _Location_ to the P2 repository created earlier and click _Finish_.
  1. In the _Content_ tab, deselect any previously installed version of the XM4WAS plugins (if applicable).
  1. Click _Finish_.
  1. Select the _WAS_ definition to make it the active target platform for the workspace.

## Deploying the plugins directly to WebSphere ##

The plugins can be deployed directly into WebSphere by exporting them as _Deployable plug-ins and fragments_. The _Destination_ must be set to the WAS installation directory; Eclipse will automatically deploy the plugins to the `plugins` sub-directory.

**Note:** Do not deploy `bootstrapstub`, `jmx-client-connector` or any of the test fragments to WebSphere! Only deploy the `com.googlecode.xm4was.*` plugins.

When redeploying, execute the `osgiCfgInit` (see [here](http://publib.boulder.ibm.com/infocenter/wasinfo/v7r0/topic/com.ibm.websphere.base.doc/info/aes/ae/rxml_osgicfginit_script.html)) script in the WAS profile used for testing before starting the server. Otherwise not all changes will be taken into account.