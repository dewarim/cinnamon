/*
 * Copyright (c) 2012 Ingo Wiarda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE
 */
package cinnamon

import cinnamon.global.PermissionName
import cinnamon.index.SearchableDomain
import cinnamon.interfaces.Accessible
import cinnamon.interfaces.Ownable
import cinnamon.interfaces.XmlConvertable

/**
 * The ItemService is responsible for finding "items" for a user.
 * An item may be of any class which is indexed by the LuceneActor.
 * From the perspective of the classes using
 * the itemService, those are all items which fulfill the item interface.
 *
 */
class ItemService {

    def grailsApplication

    Integer countItems(className) {
        def items = grailsApplication.getDomainClass(className).clazz.findAll("""from $className a
         """.replaceAll('\n', ' ')
                , [:])
        return items.size()
    }

    List fetchItems(String className, Map params) {
        def items
        if (params.sort) {
            log.debug("sorting by $params.sort")
            def clacks = grailsApplication.getDomainClass(className).clazz
            def sortProperty = ''
            if (grailsApplication.getDomainClass(className).hasProperty(params.sort)) {
                if (params.order?.equals('desc')) {
                    sortProperty = "order by a.${params.sort} desc"
                }
                else {
                    sortProperty = "order by a.${params.sort} asc"
                }
            }
            def query = """from $className a
          $sortProperty
         """.replaceAll('\n', ' ')

            items = clacks.findAll(query, [:], [offset: params.offset, max: params.max])
        }
        else {
            items = grailsApplication.getDomainClass(className).clazz.findAll("""from $className a
         """.replaceAll('\n', ' ')
                    , [:], params)
        }
        return items
    }

    def fetchItem(String className, id) {
        if(id == null){
            return null
        }
        def item = grailsApplication.getDomainClass(className).clazz.find("""from $className a where a.id=:id
         """.replaceAll('\n', ' ')
                    , [id:Long.valueOf(id)])
        return item
    }

    def fetchItem(SearchableDomain domain, id) {
        return fetchItem(domain.name, id)
    }

    def fetchItemsFromIdList(String className, Collection<Long> ids) {
        if(ids == null || ids.isEmpty()){
            return null
        }
        def item = grailsApplication.getDomainClass(className).clazz.findAll("""from $className a where a.id in (:ids)
         """.replaceAll('\n', ' ')
                    , [ids:ids.asList()])
        return item
    }

    Map filterItemIdMap(itemIdMap, SearchableDomain domain){
        def itemMap = new HashMap<String, Set>()
        def keySet = itemIdMap.keySet()
        if(domain){
            // limit search results to a specific domain:
            keySet = keySet.findAll{it == domain.name}
        }
        keySet.each {domainClass ->
            log.debug("domainClass: $domainClass")
            def idSet = itemIdMap.get(domainClass)
            if (idSet?.size() > 0) {
                log.debug("found idSet")
                def itemList = fetchItemsFromIdList(domainClass, idSet)
                def itemSet = itemMap.get(domainClass)
                if(! itemSet){
                    itemSet = new HashSet()
                    itemMap.put(domainClass, itemSet)
                }
                itemSet.addAll(itemList)
            }
            else{
                log.debug("no idSet for $domainClass")
            }
        }
        return itemMap
    } 
    
    Set filterItemsToSet(itemIdMap, SearchableDomain domain, Validator validator){
        def itemSet = new HashSet<XmlConvertable>()
        def keySet = itemIdMap.keySet()
        if(domain){
            // limit search results to a specific domain:
            keySet = keySet.findAll{it == domain.name}
        }
        keySet.each {domainClass ->
            log.debug("domainClass: $domainClass")
            def idSet = itemIdMap.get(domainClass)
            if (idSet?.size() > 0) {
                log.debug("found idSet")
                def itemList = fetchItemsFromIdList(domainClass, idSet)
                if (validator){
                    Permission browsePermission = Permission.findByName(domain.browsePermission)
                    itemList = itemList.findAll{item ->
                        if (item instanceof Accessible){
                            /*
                             * We may someday index objects that do not have an Owner,
                             * which makes ACL-checking impossible.
                             */
                            validator.check_acl_entries(item.acl, browsePermission, (Ownable) item)
                        }
                    }
                }
                itemSet.addAll(itemList)
            }
            else{
                log.debug("no idSet for $domainClass")
            }
        }
        return itemSet
    }

}
