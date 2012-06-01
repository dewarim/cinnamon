package cinnamon

import org.dom4j.Element
import org.dom4j.DocumentHelper

class FormatController {

    def list(){
        List<Format> formats = new ArrayList<Format>();
        if (params.id) {
            // format by id
            Format f = Format.get(params.id);
            // add format only if no name has been passed or id and name match
            if (f != null) {
                formats.add(f);
            }
        } else if (formats.name) {
            Format format = Format.findByName(params.name);
            if (format != null) {
                formats.add(format);
            }
        } else {
            log.debug("no id or name given");
            formats = Format.list();
        }

        log.debug("formats.size() = " + formats.size());
        def doc = DocumentHelper.createDocument()        
        Element root = doc.addElement("formats");
        formats.each{format ->
            root.add(Format.asElement("format", format));
        }
        return render(contentType: 'application/xml', text: doc.asXML())
    }
    
}
