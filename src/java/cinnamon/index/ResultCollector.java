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
package cinnamon.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 */
public class ResultCollector implements Collector {

    static Logger log = LoggerFactory.getLogger(ResultCollector.class);

    SearchableDomain domain;

    Set<Document> documents = new HashSet<Document>();
    final IndexSearcher searcher;
    //    Integer docBase;
    Set<Integer> hits = new HashSet<>();

    public ResultCollector(IndexSearcher searcher, SearchableDomain domain) {
        this.searcher = searcher;
        this.domain = domain;
    }

    @Override
    public LeafCollector getLeafCollector(LeafReaderContext leafReaderContext) {

        final int docBase = leafReaderContext.docBase;
        return new LeafCollector() {

            // ignore scorer
            public void setScorer(Scorer scorer) {
            }

            public void collect(int doc) throws IOException {
                log.debug("adding leaf hit for doc id " + doc);
                hits.add(doc + docBase);
                Document d = searcher.doc(doc + docBase);
                documents.add(d);
            }

        };
    }

    @Override
    public boolean needsScores() {
        return false;
    }

    /**
     * Turns the search result into a map of [itemClass,set of itemId] which can be fed to itemService.<br/>
     * Note: we cannot keep the resultCollector around, because its reader may become invalid once
     * the actor is finished with the current request.<br/>
     * Note2: this method currently ignores document scores.<br/>
     * This method uses the domain field (if not null) to filter results not belonging to the selected domain.
     *
     * @return a map of [itemClass,set[itemId]]
     */
    public Map getItemIdMap() {
        Map<String, Set<Long>> itemIdMap = new HashMap<>();
        documents.forEach(doc -> {
            String domainClass = doc.getField("javaClass").stringValue();
            if (domain != null && !domain.getName().equals(domainClass)) {
                log.debug("filter result from wrong domain $domainClass");
                return;
            }
            Long id = Long.parseLong(doc.getField("hibernateId").stringValue());
            Set idSet = itemIdMap.get(domainClass);
            if (idSet != null) {
                idSet.add(id);
            }
            else {
                idSet = new HashSet<Long>();
                idSet.add(id);
                itemIdMap.put(domainClass, idSet);
            }
        });
        return itemIdMap;
    }

    /**
     * Given a specific SearchableDomain-class, this method will search all documents found for
     * their stored field values of the supplied fields list.
     *
     * @param myDomain the SearchableDomain upon which the results are based. If null, use SearchableDomain.OSD
     * @param fields   List of fields for which the content should be stored.
     * @return a map, build as: Map(id,Map(fieldName,fieldValue))
     */
    public Map<Long, Map<String, String>> getIdFieldMap(SearchableDomain myDomain, List<String> fields) {
        if (!domain.equals(myDomain)) {
            domain = SearchableDomain.OSD;
        }
        Map<Long, Map<String, String>> idFieldMap = new HashMap();
        documents.forEach(doc -> {
            String domainClass = doc.getField("javaClass").stringValue();
            if (!domain.getName().equals(domainClass)) {
                return;
            }

            Long id = Long.parseLong(doc.getField("hibernateId").stringValue());
            Map<String, String> fieldMap = new HashMap<>();
            if(fields != null && fields.size() > 0) {
                fields.forEach(fieldName -> {
                    IndexableField field = doc.getField(fieldName);
                    if (field != null && field.stringValue() != null) {
                        fieldMap.put(fieldName, field.stringValue());
                    }
                });
            }
            idFieldMap.put(id, fieldMap);
        });
        return idFieldMap;
    }

}
