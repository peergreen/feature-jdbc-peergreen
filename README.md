Peergreen JDBC
=======================

Requirements
---------------

* OSGi JDBC Service Specification (`ow2-jdbc-service-1.0-spec`)
* OSGi JNDI Service Specification (`osgi-jndi-service-1.0-spec`)

Usage
-----------

### '.datasource' files

    driverClass      com.mysql.jdbc.Driver
    url              jdbc:mysql://localhost:3306/springoodb
    datasource.name  jdbc/MyDataSource
    jndi.bind        true
    jdbc.test.statement SELECT ?

    username springoo
    password springoo

Notice that there is no persistent support for theses kind of resources.

### ConfigAdmin

Simply provides a CA XML file with the following content:

    <configuraton-factory pid="com.peergreen.jdbc.internal.datasource.DataSourceDataSource" xml:id="myDS">
      <property name="datasource.name">jdbc/myDS</property>
      <property name="driverClass">com.peergreen.db.h2.H2Driver</property>

      <property name="url">jdbc:test</property>
      <property name="username">scott</property>
      <property name="password">tiger</property>
    </configuration-factory>

### Supported properties

This is common to ConfigAdmin and datasource files deployment:

<https://forge.peergreen.com/git/blob/?f=src/main/java/com/peergreen/jdbc/internal/datasource/Constants.java&r=feature/jdbc/peergreen-jdbc.git&h=1e56aab6a0cf60ca9d067d61679710e5ef381815>

TODO List
---------

* Plug statistics to extract meaningful values from the working pool
* Refactor pool and ManagedConnection to support PooledConnection and XAConnection
* Rework PreparedStatement pool (probably with the help of the new StatementEventListener)
* Switch back to the OW2 Pool API
* Bean injection should support JDBC Wrapper (configuration of wrapped/delegated)
* MBeans (exposing statistics)
* Deployment update (delate, ...)
* Add datasource.type attribute (DM, DS, CP and XA) in '.datasource' to instantiate the right DS component
* Maybe have wait timeout configurable ?
* Add integration test (find JDBC application)

