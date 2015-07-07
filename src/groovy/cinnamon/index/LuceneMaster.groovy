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

import groovyx.gpars.actor.DefaultActor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A background Actor class which is responsible for mass re-indexing of content. This actor starts
 * upon server restart and receives one command from the LuceneService to look for work in the configured
 * repositories.
 */
class LuceneMaster extends DefaultActor {

    Logger log = LoggerFactory.getLogger(this.class)
    LuceneActor luceneActor
    Boolean running = true
    Long sleeping = 2000

    void onDeliveryError(msg) {
        log.warn("Could not deliver msg: $msg")
    }

    protected void act() {
        loop {
            react { IndexCommand command ->
                try {
                    log.debug("LuceneMaster received: $command")
                    def result = new LuceneResult()
                    switch (command.type) {
                        case CommandType.START_INDEXING: result = startIndexing(command); break
                        case CommandType.STOP_INDEXING: stopIndexing(); break
                    }
                    log.debug("Indexing finished with command ${command.type}.")
                    reply result
                }
                catch (Exception e) {
                    log.debug("Failed to act on command:", e)
                }
            }
        }
    }

    void stopIndexing(){
        running = false
    }
    
    LuceneResult startIndexing(IndexCommand command) {
        try {
            running = true
            if (!luceneActor) {
                luceneActor = new LuceneActor()
                luceneActor.start()
            }
            IndexCommand cmd = new IndexCommand(type: CommandType.UPDATE_INDEX,
                    repository: command.repository
            )
            updateIndex(cmd)
        }
        catch (Exception e) {
            log.warn("startTicking failed.", e)
        }
        return new LuceneResult()
    }

    void updateIndex(IndexCommand command) {
        def cmd = new IndexCommand(type: CommandType.UPDATE_INDEX, repository: command.repository)
        def c = {updateIndex(cmd)}
        log.debug("updateIndex called.")
        if (running) {
            log.debug("Calling LuceneActor for ${command.repository.name}.")

            luceneActor.sendAndContinue(cmd) { LuceneResult result ->
                log.debug("LuceneActor returned from update of ${command.repository.name}")
                if (result.failed) {
                    log.warn("LuceneActor failed: ${result.resultMessages}")
                }
                else {
                    log.debug("calling updateIndex.")
                    Thread.sleep(sleeping)
                    c.call()
                }                
            }            
        }
    }

   

}
