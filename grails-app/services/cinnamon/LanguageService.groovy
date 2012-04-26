package cinnamon

import cinnamon.i18n.Language
import cinnamon.exceptions.CinnamonConfigurationException

class LanguageService {

    /**
     * Find the default language. At the moment, this is hardcoded to "und", the
     * undetermined language.
     * TODO: set default language by Config.
     *
     * @return the default language or a CinnamonConfigurationException if 'und' was not found.
     */
    Language getDefaultLanguage() {
        Language lang = Language.findByIsoCode("und");
        if(! lang){
            throw new CinnamonConfigurationException("No default language configured! You must at least configure 'und' for undetermined language.");
        }
        return lang;
    }
}
