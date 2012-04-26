package cinnamon.index.queryBuilder;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.xmlparser.DOMUtils;
import org.apache.lucene.xmlparser.ParserException;
import org.apache.lucene.xmlparser.QueryBuilder;
import org.w3c.dom.Element;

/**
 * The WildcardQueryBuilder is an extension to Lucene-XML-Query-Parser.
 * It adds the ability to search for Terms with leading wildcards. 
 *
 */
public class WildcardQueryBuilder implements QueryBuilder {

	@Override
	public Query getQuery(Element e) throws ParserException {
	    String field= DOMUtils.getAttributeWithInheritanceOrFail(e, "fieldName");
 		String value= DOMUtils.getNonBlankTextOrFail(e);
  		return new WildcardQuery(new Term(field, value));
	}

}
