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

import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.Collector
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Scorer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * // code partially copied from project cinnamon (cinnamon-cms.de, LGPL)
 */
class ResultCollector extends Collector {

    transient Logger log = LoggerFactory.getLogger(this.class)

    SearchableDomain domain

    Set<Document> documents = new HashSet<Document>();
    IndexSearcher searcher;
    IndexReader reader;
    Integer docBase;
    LinkedHashSet<Integer> hits = new LinkedHashSet<Integer>();

    public ResultCollector(IndexSearcher searcher){
        this.searcher = searcher;
    }

    public ResultCollector(){

    }

    @Override
    public void collect(int doc) {
        hits.add(doc);
        try{
            Document d = searcher.doc(doc+docBase);
            documents.add(d);
        }
        catch (IOException e) {
            log.warn("ResultCollector.collect encountered an IOException:",e);
        }
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
    throws IOException {
        this.reader = reader; // ignore findBugs.warning: this field is included for possible future use.
        this.docBase = docBase;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
        // ignore scorer
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
    Map getItemIdMap(){
        Map<String, Set<Long>> itemIdMap = new HashMap<String, Set<Long>>()
        documents.each{doc ->
            String domainClass = doc.getFieldable("javaClass").stringValue()
            if(domain && domain.name != domainClass){
                log.debug("filter result from wrong domain $domainClass")
                return
            }
            Long id = Long.parseLong(doc.getFieldable("hibernateId").stringValue())
            def idSet = itemIdMap.get(domainClass)
            if(idSet){
                idSet.add(id)
            }
            else{
                idSet = new HashSet<Long>()
                idSet.add(id)
                itemIdMap.put(domainClass, idSet)
            }
        }
        return itemIdMap
    } 

    /**
     * Given a specific SearchableDomain-class, this method will search all documents found for
     * their stored field values of the supplied fields list.
     * 
     * @param myDomain the SearchableDomain upon which the results are based. If null, use SearchableDomain.OSD
     * @param fields List of fields for which the content should be stored.
     * @return a map, build as: Map(id,Map(fieldName,fieldValue)) 
     */
    Map getIdFieldMap(SearchableDomain myDomain, List<String> fields){        
        domain == myDomain ?: SearchableDomain.OSD
        Map<Long, Map<String, String>> idFieldMap = new HashMap()
        documents.each{doc ->
            String domainClass = doc.getFieldable("javaClass").stringValue()
            if(domain.name != domainClass){
                return
            }
            
            Long id = Long.parseLong(doc.getFieldable("hibernateId").stringValue())            
            def fieldMap = [:]
            fields.each{fieldName ->
                def field = doc.getFieldable(fieldName)
                if (field && field.stored){
                    fieldMap.put(fieldName, field.stringValue())
                }
            }
            idFieldMap.put(id,fieldMap)
        }
        return idFieldMap
    }
    
}
