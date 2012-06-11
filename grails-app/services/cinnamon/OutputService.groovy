package cinnamon

class OutputService {

    static transactional = true

    static replacements = ['&':'&amp;', '<':'&lt;', '>':'&gt;', '\'':'&apos;', '"':'&quot;']

    String replaceXmlEntities(String out){
        if(out == null){
            return null
        }
        replacements.each {k,v ->
            out = out.replaceAll(k,v)
        }
        return out
    }
}
