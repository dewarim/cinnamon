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

import cinnamon.ObjectSystemData

import groovyx.gpars.actor.DefaultActor
import humulus.Environment
import humulus.EnvironmentHolder

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import cinnamon.Folder

/**
 * A background Actor class which is responsible for mass re-indexing of content. This actor starts
 * upon server restart and receives one command from the LuceneService to look for work in the configured
 * repositories.
 */
class LuceneBackgroundActor extends
        DefaultActor {

    Logger log = LoggerFactory.getLogger(this.class)
    LuceneActor luceneActor
    List repositories

    LuceneBackgroundActor() {}

    LuceneBackgroundActor(LuceneActor actor) {
        luceneActor = actor
    }

    void onDeliveryError(msg) {
        log.warn("Could not deliver msg: $msg")
    }

    protected void act() {
        loop {
            react { command ->
                try {
                    log.debug("LuceneBackgroundActor received: $command")
                    def result = new LuceneResult()
                    def env = Environment.list().find {it.dbName == command.repository}
//                    log.debug("found env: $env")
                    EnvironmentHolder.setEnvironment(env)
                    switch (command.type) {
                        case CommandType.RE_INDEX: result = reIndex(command); break
                    }
                    log.debug("reply & finish")
                    reply result
                }
                catch (Exception e) {
                    log.debug("Failed to act on command:", e)
                }
            }
        }
    }

    LuceneResult reIndex(IndexCommand command) {
        def osdCounter = 0
        def folderCounter = 0
        repositories.each {repository ->
            def osds = []
            ObjectSystemData.withTransaction {
                osds = ObjectSystemData.findAll("from ObjectSystemData o where o.indexOk is NULL")
            }
            osdCounter += osds.size()
            osds.each {osd ->
                ObjectSystemData.withTransaction {
                    osd = ObjectSystemData.get(osd.id)
                    log.debug("going to update osd #${osd.id}")
                    def cmd = new IndexCommand(indexable: osd, repository: repository, type: CommandType.UPDATE_INDEX, reloadIndexable: true)
                    luceneActor.sendAndWait(cmd)
                }
            }
            def folders = []
            Folder.withTransaction {
                folders = Folder.findAll("from Folder f where f.indexOk is NULL")
            }
            folderCounter += folders.size()
            folders.each {folder ->
                log.debug("going to update folder #${folder.id}")
                def cmd = new IndexCommand(indexable: folder, repository: repository, type: CommandType.UPDATE_INDEX, reloadIndexable: true)
                luceneActor.sendAndWait(cmd)
            }
        }
        def resultMessages = ["Updated osds: ${osdCounter}", "Updated folders. ${folderCounter}"]
        def luceneResult = new LuceneResult(resultMessages: resultMessages)
        return luceneResult
    }

}
