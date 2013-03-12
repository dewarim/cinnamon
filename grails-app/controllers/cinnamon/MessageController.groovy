package cinnamon

import grails.plugins.springsecurity.Secured
import cinnamon.i18n.UiLanguage
import cinnamon.i18n.Message
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element

@Secured(["hasRole('_superusers')"])
class MessageController extends BaseController {

    def create () {
        return [defaultLanguage: UiLanguage.findByIsoCode('und'),
                messageId: params.messageId,
                translation: params.translation]
    }

    def list () {
        def distinctMessages = getDistinctMessages()
        log.debug("distinctMessages: ${countDistinctMessages()}")
        return [messageCount: countDistinctMessages(),
                distinctMessages: distinctMessages,
        ]
    }

    def updateList () {
        def distinctMessages = getDistinctMessages()
        log.debug("distinctMessages: ${countDistinctMessages()}")
        render(template: 'list_table', model: [messageCount: countDistinctMessages(),
                distinctMessages: distinctMessages])
    }

    protected Integer countDistinctMessages() {
        def c = Message.createCriteria()
        return c.list {
            projections {
                distinct 'message'
            }
        }.size()
    }

    protected List getDistinctMessages() {
        setListParams()
        def c = Message.createCriteria()
        return c.list {
            projections {
                distinct 'message'
            }
            order('message')
            maxResults(params.max)
            firstResult(params.offset)
        }
    }

    def show () {
        def msg = (Message) inputValidationService.checkObject(Message.class, params.id)
        def messages = Message.findAllByMessage(msg.message)
        if (messages.isEmpty()) {
            return redirect(action: 'list')
        }

        return [msg: msg,
                messageId: msg.message,
                translations: getTranslationsMap(messages),
                languages: getLanguageList(),
        ]
    }

    protected Map getTranslationsMap(messages) {
        def msgList = messages
        def translations = [:]
        msgList.each {msg ->
            translations.put msg.language.isoCode, msg.translation
        }
        return translations
    }

    protected List getLanguageList() {
        //return Language.list(sort:'isoCode').findAll{! (it.isoCode.equals('zxx') || it.isoCode.equals('mul'))}
        // changed return, because Grails no longer accepts sort:isoCode as valid.
        return UiLanguage.list().sort {a, b -> a.isoCode <=> b.isoCode }.findAll {!(it.isoCode.equals('zxx') || it.isoCode.equals('mul'))}
    }

    def edit () {
        def msg = (Message) inputValidationService.checkObject(Message.class, params.id)
        def msgList = Message.findAllByMessage(msg.message)
        return [msg: msg,
                messageId: msg.message,
                translations: getTranslationsMap(msgList),
                languages: getLanguageList(),
                msgList: msgList,
        ]
    }

    def updateTranslationAjax () {
        UiLanguage language = UiLanguage.get(params.language)
        Message msg = Message.findByLanguageAndMessage(language, params.messageId)
        if (msg) {
            msg.translation = params.translation
            log.debug("translation updated: " + msg.dump())
            msg.save()
            return render(text: message(code: 'translation.update.success'),
                    contentType: "text/plain", encoding: "UTF-8")
        }
        else {
            msg = new Message(params.messageId, language, params.translation)
            msg.save(failOnError: true)
            return render(text: message(code: 'message.translation.added'),
                    contentType: "text/plain", encoding: "UTF-8")
        }
    }

    def update () {
        log.debug("called update with " + params.dump())

        if (params.messageId?.length() == 0) {
            return redirect(action: 'list')
        }

        def messageId = params.messageId
        def messages = Message.findAllByMessage(params.oldMessageId)
        log.debug("messages: " + messages)
        try {
            messages.each { msg ->
                msg.message = messageId
                if (msg.save(failOnError: true, flush: true)) {
                    flash.message = message(code: "message.update.success", args: [messageId.encodeAsHTML()])
                }
                else {
                    log.warn("update of message failed without an exception.")
                }
            }
            return redirect(action: 'show', params: [messageId: messageId, id: Message.findByMessage(messageId).id])
        }
        catch (Exception e) {
            flash.message = message(code: "message.update.fail", args: [e.getLocalizedMessage()])
            return redirect(action: 'edit', params: [messageId: messageId])
        }
    }

    def delete () {
        def msg = (Message) inputValidationService.checkObject(Message.class, params.id)
        def messages = Message.findAllByMessage(msg.message)
        try {
            messages.each {it.delete()}
        }
        catch (Exception e) {
            flash.message = e.getLocalizedMessage()
            return redirect(action: 'list')
        }

        flash.message = message(code: 'message.delete.success', args: [msg.message.encodeAsHTML()])
        return redirect(action: 'list')
    }

    /**
     * Called after the 'save' button in create.gsp is called
     */
    def save () {
        def msgId = params.messageId
        def translation = params.translation

        UiLanguage language = (UiLanguage) inputValidationService.checkObject(UiLanguage.class, params.languageId)
        if (Message.findByMessageAndLanguage(msgId, language)) {
            flash.message = message(code: 'error.duplicate.message.id')
            return redirect(action: 'create')
        }

        def msg = new Message(msgId, language, translation)
        try {
            msg.save(failOnError: true, flush: true)
        }
        catch (Exception e) {
            flash.message = e.getLocalizedMessage()
            return redirect(action: 'create')
        }

        return redirect(action: 'show', params: [messageId: msg.message, id: msg.id])
    }

    def selectExportLanguage () {
        render(template: 'selectExportLanguage', model: [languages: getLanguageList()])
    }

    def exportMessages () {
        UiLanguage language = (UiLanguage) inputValidationService.checkObject(UiLanguage.class, params.language)
        response.setHeader("Content-disposition", "attachment; filename=${language.isoCode}.xml");
        render(template: "exportMessages", contentType: 'application/xml',
                model: [messages: Message.findAllByLanguage(language), language: language, outputService: outputService]
        )
    }

    def exportMessagesXliff () {
        UiLanguage language = (UiLanguage) inputValidationService.checkObject(UiLanguage.class, params.language)
        UiLanguage targetLanguage = (UiLanguage) inputValidationService.checkObject(UiLanguage.class, params.targetLanguage)
        response.setHeader("Content-disposition", "attachment; filename=${language.isoCode}.xlf");
        render(template: "exportMessagesXliff", contentType: 'application/xliff+xml',
                model: [messages: Message.findAllByLanguage(language),
                        language: language,
                        targetLanguage: targetLanguage,
                        outputService: outputService,
                ]
        )
    }

    def showUploadForm () {
        render(template: 'showUploadForm')
    }

    def importMessages () {
        try {
            def upload = request.getFile('messages')
            if (!upload) {
                throw new RuntimeException('error.upload.fail')
            }
            File savedUpload = File.createTempFile("messageImport_", ".xml")
            upload.transferTo(savedUpload)

            def xml = new XmlParser().parse(savedUpload)
            // detect document type: either our homebrew format or xliff:
            if (xml.name().toString().contains('xliff')) {
                log.debug("xliff detected")
                importXliff(xml)
                return
            }

            def lang = xml.attribute('isoCode')
            if (!lang) {
                throw new RuntimeException('error.message.import.language')
            }
            UiLanguage language = UiLanguage.findByIsoCode(lang)
            if (!language) {
                throw new RuntimeException('error.message.import.language')
            }
            def existingMessageList = Message.findAllByLanguage(language)
            def existingMessageMap = [:]
            existingMessageList.each {m ->
                existingMessageMap.put(m.message, m)
            }
            def seenMessageMap = [:]
            def newMessages = 0
            def deletedMessages = 0
            def unchangedMessages = 0
            def updatedMessages = 0
            xml.message?.each {msg ->
                String id = msg.id.text()
                String translation = msg.translation.text()

                if (existingMessageMap.containsKey(id)) {
                    Message m = existingMessageMap.get(id)
                    if (m.translation.equals(translation)) {
                        // do nothing, message already exists in db
                        unchangedMessages++
                    }
                    else {
                        m.translation = translation
                        updatedMessages++
                    }
                    seenMessageMap.put(id, m)
                }
                else {
                    // it is a new message!
                    Message newMess = new Message(id, language, translation)
                    newMess.save()
                    newMessages++
                }
            }
            existingMessageMap.each {key, val ->
                if (!seenMessageMap.containsKey(key)) {
                    log.debug("delete no longer used message: ${key}")
                    val.delete()
                    deletedMessages++
                }
            }
            def importResults = g.render(template: 'importResults', model: [newMessages: newMessages, deletedMessages: deletedMessages,
                    unchangedMessages: unchangedMessages, updatedMessages: updatedMessages
            ])

            def distinctMessages = getDistinctMessages()
            return render(view: 'list', model: [importResults: importResults,
                    messageCount: distinctMessages.size(),
                    distinctMessages: distinctMessages,])
        }
        catch (Exception e) {
            flash.message = message(code: e.message)
            redirect(action: 'list', controller: 'message')
        }
    }

    protected void importXliff(xml) {
        def lang = xml.file.'@target-language'.text()
        log.debug("target language = $lang")
        if (!lang) {
            throw new RuntimeException('error.message.import.language')
        }
        UiLanguage language = UiLanguage.findByIsoCode(lang)
        if (!language) {
            throw new RuntimeException('error.message.import.language')
        }
        def existingMessageList = Message.findAllByLanguage(language)
        def existingMessageMap = [:]
        existingMessageList.each {m ->
            existingMessageMap.put(m.message, m)
        }
        def seenMessageMap = [:]
        def newMessages = 0
        def deletedMessages = 0
        def unchangedMessages = 0
        def updatedMessages = 0
        xml.file.body.'trans-unit'.each {transUnit ->
            def id = transUnit.'@resname'
            log.debug("message id: $id")
            def translation = transUnit.target.text()

            if (existingMessageMap.containsKey(id)) {
                Message m = (Message) existingMessageMap.get(id)
                if (m.translation.equals(translation)) {
                    // do nothing, message already exists in db
                    unchangedMessages++
                }
                else {
                    m.translation = translation
                    updatedMessages++
                }
                seenMessageMap.put(id, m)
            }
            else {
                // it is a new message!
                Message newMess = new Message(id, language, translation)
                newMess.save()
                newMessages++
            }
        }
        existingMessageMap.each {key, val ->
            if (!seenMessageMap.containsKey(key)) {
                log.debug("delete no longer used message: ${key}")
                val.delete()
                deletedMessages++
            }
        }
        def importResults = g.render(template: 'importResults', model: [newMessages: newMessages, deletedMessages: deletedMessages,
                unchangedMessages: unchangedMessages, updatedMessages: updatedMessages
        ])

        def distinctMessages = getDistinctMessages()
        render(view: 'list', model: [importResults: importResults,
                messageCount: distinctMessages.size(),
                distinctMessages: distinctMessages,])
    }

    //---------------------------------------------------
    // Cinnamon XML Server API
    @Secured(["isAuthenticated()"])
    def listXml() {
        Document doc = DocumentHelper.createDocument()
        Element root = doc.addElement("messages");
        Message.list().each {msg ->
            msg.toXmlElement(root)
        }
        return render(contentType: 'application/xml', text: doc.asXML())
    }

}
