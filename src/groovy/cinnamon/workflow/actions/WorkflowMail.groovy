package cinnamon.workflow.actions

import cinnamon.global.ConfThreadLocal

/**
 * Send a mail message from a finished workflow to a user. 
 */
class WorkflowMail {
    
    def mailService
    
    def create(String address, String subject, String body){
        def mailFrom = ConfThreadLocal.conf.getField('//mail/from', '"Cinnamon Workflow Server"<noreply@localhost>')
        mailService.sendMail(mailFrom, address, subject, body)
    }    
    
}
