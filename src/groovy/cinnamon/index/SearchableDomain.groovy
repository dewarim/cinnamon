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

/**
 *
 * List of searchable domains - used to restrict search forms which apply only to a single domain class,
 * like "cinnamon.Folder". 
 *
 */
public enum SearchableDomain {

    OSD([name: 'cinnamon.ObjectSystemData']),
    FOLDER([name: 'cinnamon.Folder'])

    String name

    SearchableDomain(Map map) {
        map.each {k, v ->
            this."$k" = v
        }
    }

    String getName() {
        return name
    }

    /**
     * Fetch the SearchableDomain by the given name.
     * This is an enhancement over the default "valueOf(name)" method because it
     * can return null instead of throwing an exception.
     * @param name the name of the domain enum instance.
     * @return the SearchableDomain by the given name or null.
     */
    static SearchableDomain fetchDomainByName(String name){
        return SearchableDomain.values().find{it.name == name}
    }

    /**
     * Fetch the SearchableDomain by the enum element name.
     * This is an enhancement over the default "valueOf(name)" method because it
     * can return null instead of throwing an exception.
     * @param name the name of the domain enum instance.
     * @return the SearchableDomain by the given name or null.
     */
    static SearchableDomain fetchEnum(String name){
        return SearchableDomain.values().find{it.name() == name}
    }

}