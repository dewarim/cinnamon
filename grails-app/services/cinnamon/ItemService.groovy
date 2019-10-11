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

    def fetchItemsFromIdList(String className, Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return null
        }
        return grailsApplication.getDomainClass(className).clazz.findAll("from $className a where a.id in (:ids)", [ids: ids.asList()])
    }

    Map filterItemIdMap(itemIdMap, SearchableDomain domain) {
        def itemMap = new HashMap<String, Set>()
        def keySet = itemIdMap.keySet()
        if (domain) {
            // limit search results to a specific domain:
            keySet = keySet.findAll { it == domain.name }
        }
        keySet.each { domainClass ->
            log.debug("domainClass: $domainClass")
            def idSet = itemIdMap.get(domainClass)
            if (idSet?.size() > 0) {
                idSet = reduceSet(idSet)
                log.debug("found idSet")
                def itemList = fetchItemsFromIdList(domainClass, idSet)
                def itemSet = itemMap.get(domainClass)
                if (!itemSet) {
                    itemSet = new HashSet()
                    itemMap.put(domainClass, itemSet)
                }
                itemSet.addAll(itemList)
            } else {
                log.debug("no idSet for $domainClass")
            }
        }
        return itemMap
    }

    Set filterItemsToSet(itemIdMap, SearchableDomain domain, Validator validator) {
        def itemSet = new HashSet<XmlConvertable>()
        def keySet = itemIdMap.keySet()
        if (domain) {
            // limit search results to a specific domain:
            keySet = keySet.findAll { it == domain.name }
        }
        keySet.each { domainClass ->
            log.debug("domainClass: $domainClass")
            Set idSet = itemIdMap.get(domainClass)
            if (idSet?.size() > 0) {
                log.debug("found idSet")
                idSet = reduceSet(idSet)
                def itemList = fetchItemsFromIdList(domainClass, idSet)
                if (validator) {
                    Permission browsePermission = Permission.findByName(domain.browsePermission)
                    itemList = itemList.findAll { item ->
                        if (item instanceof Accessible) {
                            /*
                             * We may someday index objects that do not have an Owner,
                             * which makes ACL-checking impossible.
                             */
                            validator.check_acl_entries(item.acl, browsePermission, (Ownable) item)
                        }
                    }
                }
                itemSet.addAll(itemList)
            } else {
                log.debug("no idSet for $domainClass")
            }
        }
        return itemSet
    }

    /*
     * PostgreSQL driver will only accept 2^15 elements in a parameter list.
     * So we limit results to this amount - future versions may use batch queries to
     * really return all items found or filter the ids according to page/page_size
     */

    Set reduceSet(Set idSet) {
        if (idSet.size() > 32767) {
            def reducedSet = new HashSet()
            reducedSet.addAll(idSet.asList().subList(0, 32767))
            return reducedSet
        }
        return idSet
    }

}
