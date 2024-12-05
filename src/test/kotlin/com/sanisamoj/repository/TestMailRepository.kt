package com.sanisamoj.repository

import com.sanisamoj.data.models.dataclass.SendEmailData
import com.sanisamoj.data.models.interfaces.MailRepository

class TestMailRepository: MailRepository {
    override fun sendEmail(sendEmailData: SendEmailData) {
        println(sendEmailData)
    }
}