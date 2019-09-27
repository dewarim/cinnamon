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
package cinnamon.index

import cinnamon.Validator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import cinnamon.ItemService

/**
 *
 */
class LuceneResult {

    transient Logger log = LoggerFactory.getLogger(this.class)

    List<String> resultMessages = []
    Boolean failed = false
    String errorMessage

    Map<String, Set<Long>> itemIdMap = new HashMap<String, Set<Long>>()
    Map<Long, Map<String, String>> idFieldMap = new HashMap<>()


    /**
     * Filter the search results (which may already be filtered by the ResultCollector) by optional SearchableDomain
     * and return a map of className::itemSet (currently unordered)
     * @param domain optional searchable domain
     * @param itemService the itemService, which is called for each item for filtering.
     * @return a map className::itemSet with search results.
     */
    Map filterResultToMap(SearchableDomain domain, ItemService itemService) {
        return itemService.filterItemIdMap(itemIdMap, domain)
    }

    /**
     * Filter the search results (which may already be filtered by the ResultCollector) by optional SearchableDomain
     * and return an itemSet (currently unordered)
     * @param domain optional searchable domain
     * @param itemService the itemService, which is called for each item for filtering.
     * @param validator A validator object to further filter the search results by their ACL
     * @return a map className::itemSet with search results.
     */
    Set filterResultToSet(SearchableDomain domain, ItemService itemService, Validator validator) {
        return itemService.filterItemsToSet(itemIdMap, domain, validator)
    }


    @Override
    public String toString() {
        return "LuceneResult{" +
                "resultMessages=" + resultMessages +
                ", failed=" + failed +
                ", errorMessage='" + errorMessage + '\'' +
                ", itemIdMap=" + itemIdMap +
                ", idFieldMap=" + idFieldMap +
                '}';
    }
}
