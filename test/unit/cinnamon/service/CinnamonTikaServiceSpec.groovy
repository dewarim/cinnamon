package cinnamon.service

import cinnamon.Metaset
import cinnamon.MetasetType
import cinnamon.tika.CinnamonTikaService
import cinnamon.utils.ParamParser
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import spock.lang.Specification
import tikaParser.TikaService

/**
 */
@TestFor(CinnamonTikaService)
@Mock([MetasetType, Metaset])
@TestMixin(DomainClassUnitTestMixin)
class CinnamonTikaServiceSpec extends Specification{
    
    
    def cmnTikaService = new CinnamonTikaService()
    
    def 'parse a word file and create a metaset'(){
        setup:
        cmnTikaService.tikaService = new TikaService()
        def metasetType = new MetasetType("tika", null)
        def metaset = new Metaset(null, metasetType)
        def content = new File('test/resources/wordDocumentForTikaTest.docx') 
        
        when:
        cmnTikaService.parseFile(content, metaset)
        
        then:
        // related bug: parsing a word document would result in a metaset inside a metaset.
        ParamParser.parseXmlToDocument(metaset.content, 'fail').selectNodes('//metaset').size() == 1
        // make sure that status=empty attribute is removed once we add the Tika generated content.
        ParamParser.parseXmlToDocument(metaset.content, 'fail').selectSingleNode('/metaset/@status')?.stringValue != 'empty'
        
    }
    
    
}
