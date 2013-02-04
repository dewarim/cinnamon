dataSource {
    pooled = true
    configClass = cinnamon.hibernate.FieldAccessHibernateConfiguration
}

hibernate {
    cache.use_second_level_cache = false
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            configClass = cinnamon.hibernate.FieldAccessHibernateConfiguration
            dbCreate = "none" // one of 'create', 'create-drop', 'update', 'validate', ''
//            url = "jdbc:h2:mem:devDb;MVCC=TRUE"
        }
    }
    test {
        dataSource {
            dbCreate = "update"
//            username = 'cinnamon'
//            password = 'cinnamon'
//            url = 'jdbc:postgresql://localhost/demo?user=cinnamon&password=cinnamon'
            url = "jdbc:h2:mem:testDb"
        }
    }
    production {
        dataSource {
            configClass = cinnamon.hibernate.FieldAccessHibernateConfiguration
            dbCreate = "none"
//            url = "jdbc:h2:prodDb;MVCC=TRUE"
            pooled = true
            properties {
               maxActive = -1
               minEvictableIdleTimeMillis=1800000
               timeBetweenEvictionRunsMillis=1800000
               numTestsPerEvictionRun=3
               testOnBorrow=true
               testWhileIdle=true
               testOnReturn=true
               validationQuery="SELECT 1"
            }
        }
    }
}
