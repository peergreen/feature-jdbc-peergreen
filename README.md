Peergreen JDBC
=======================

Requirements
---------------

* OSGi JDBC Service Specification (`ow2-jdbc-service-1.0-spec`)
* OSGi JNDI Service Specification (`osgi-jndi-service-1.0-spec`)

Usage
-----------

The components are intended to be used through ConfigAdmin (even if other means are usable, CA is just easier).

Simply provides a CA XML file with the following content:

    <configuraton-factory pid="com.peergreen.jdbc.internal.datasource.DataSourceService" xml:id="myDS">
      <property name="datasource.name">jdbc/myDS</property>
      <property name="driverClass">com.peergreen.db.h2.H2Driver</property>
      <property name="use.jndi">true</property>

      <property name="url">jdbc:test</property>
      <property name="username">scott</property>
      <property name="password">tiger</property>
    </configuration-factory>


