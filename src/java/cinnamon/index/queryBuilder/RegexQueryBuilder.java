package cinnamon.index.queryBuilder;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RegexpQuery;

import org.apache.lucene.queryparser.xml.DOMUtils;
import org.apache.lucene.queryparser.xml.ParserException;
import org.apache.lucene.queryparser.xml.QueryBuilder;
import org.w3c.dom.Element;

/**
 * The RegexQueryBuilder is an extension to Lucene-XML-Query-Parser.
 * It adds the ability to search for Terms with complex regexes. 
 *
 */
public class RegexQueryBuilder implements QueryBuilder {

	@Override
	public Query getQuery(Element e) throws ParserException {
	    String field= DOMUtils.getAttributeWithInheritanceOrFail(e, "fieldName");
 		String value= DOMUtils.getNonBlankTextOrFail(e);
  		return new RegexpQuery(new Term(field, value));
	}

}
