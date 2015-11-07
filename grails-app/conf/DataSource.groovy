dataSource {
    pooled = true
}

hibernate {
    cache.use_second_level_cache = false
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "none" // one of 'create', 'create-drop', 'update', 'validate', ''
//            url = "jdbc:h2:mem:devDb;MVCC=TRUE"
            url = 'jdbc:postgresql://localhost/demo?user=cinnamon&password=cinnamon'
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            username = 'sa'
            password = ''
//            url = 'jdbc:postgresql://localhost/demo?user=cinnamon&password=cinnamon'
            url = "jdbc:h2:mem:testDb"
        }
    }
    production {
        dataSource {
            dbCreate = "none"
            pooled = true
            properties {
               maxActive = -1
               minEvictableIdleTimeMillis=30000
               timeBetweenEvictionRunsMillis=30000
               numTestsPerEvictionRun=3
               testOnBorrow=true
               testWhileIdle=true
               testOnReturn=true
               validationQuery="SELECT 1"
            }
        }
    }
}
