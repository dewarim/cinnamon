/*
* based upon:
* http://forum.springsource.org/showthread.php?p=132448
* http://www.leebutts.com/2008/07/switchable-grails-datasource.html
*/
package humulus

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource

import javax.sql.DataSource

class SwitchableDataSource extends AbstractRoutingDataSource{

    protected DataSource determineTargetDataSource() {
        return super.determineTargetDataSource();
    }

    protected Object determineCurrentLookupKey() {
        def env = EnvironmentHolder.getEnvironment()
        if (!env) {
            log.debug("empty EnvironmentHolder - using first environment in list.")
            /*
            In a perfect world, this would not be needed, as the very first call to
            getDataSource would happen _after_ the EnvironmentHolder has been initialized.
            Sadly, during Grails start up, this method is called three times and it looks like
            a lot of work to find out how to configure this black magic.
            (see: http://burtbeckwith.com/blog/?p=312 )

            Note that while you can have multiple Environments, you can connect only
            to one _type_ of database, as the HibernateDialectDetectorFactoryBean currently
            (Grails 1.3.7) does - as far as I know - only run once. So,
            if you configure (in humulus-config.groovy) several different databases,
            the second one will be called with the database dialect of the first one
            found by Hibernate. With hsqldb vs PostgresSQL this means you will have much fun
            debugging statements like "FEHLER: Syntaxfehler bei »$1« Position: 12 postgresql"

            Environment is initialized properly by RepositoryLoginFilter on each request,
            to prevent a user from accidentally getting access to the first configured
            database. We can not simply return null, because this is forbidden by
            AbstractRoutingDataSource (it results in an Exception being thrown if no database
            connection can be configured).
            */
            def firstEnv = Environment.list()[0]?.id
            if(! firstEnv){
                throw new RuntimeException("No Environment was found!")
            }
            return firstEnv
        }
        return env?.id
    }
}
