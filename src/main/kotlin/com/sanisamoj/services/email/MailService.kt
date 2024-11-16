package com.sanisamoj.services.email

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.config.GlobalContext.ACTIVATE_ACCOUNT_LINK_ROUTE
import com.sanisamoj.config.MailBuilder
import com.sanisamoj.data.models.dataclass.SendEmailData
import com.sanisamoj.data.models.interfaces.MailRepository

class MailService(
    private val mailRepository: MailRepository = GlobalContext.getMailRepository()
) {

    fun sendConfirmationTokenEmail(name: String, token: String, to: String) {
        val activationLink = "${ACTIVATE_ACCOUNT_LINK_ROUTE}?token=$token&email=$to"
        val text: String = MailBuilder.buildConfirmationTokenMail(name, activationLink)
        val topic: String = GlobalContext.globalWarnings.activateYourAccount
        val sendEmailData = SendEmailData(to, topic, text, true)
        mailRepository.sendEmail(sendEmailData)
    }

    fun sendAccountActivationMail(username: String, to: String) {
        val text: String = MailBuilder.buildAccountActivationMail(username)
        val topic: String = GlobalContext.globalWarnings.welcome
        val sendEmailData = SendEmailData(to, topic, text, true)
        mailRepository.sendEmail(sendEmailData)
    }

}