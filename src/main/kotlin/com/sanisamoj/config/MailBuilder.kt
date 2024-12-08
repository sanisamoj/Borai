package com.sanisamoj.config

import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.li
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.html.ul
import kotlinx.html.unsafe

object MailBuilder {

    private val activationAccountMailCssStyles = """
        body {
            font-family: Arial, sans-serif;
            background-color: #ffffff;
            margin: 0;
            padding: 0;
        }
        .container {
            max-width: 600px;
            margin: 50px auto;
            background-color: #ffffff;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        h1 {
            color: #333333;
            text-align: center;
        }
        p, h3 {
            color: #333333;
            line-height: 1.6;
        }
        ul {
            padding-left: 15px; /* Reduz o recuo dos itens da lista */
            margin-left: 0; /* Remove qualquer margem extra à esquerda */
        }
        li {
            margin-bottom: 8px; /* Adiciona um espaço entre os itens */
        }
        .button {
            display: block;
            width: 200px;
            margin: 20px auto;
            padding: 10px;
            text-align: center;
            background-color: #007bff;
            color: #ffffff;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
        }
        .footer {
            text-align: center;
            color: #666666;
            font-size: 12px;
            margin-top: 20px;
        }
        .info {
            margin-top: 20px;
            padding: 15px;
            border: 1px solid #ddd;
            border-radius: 5px;
            background-color: #f9f9f9;
        }
    """.trimIndent()

    fun buildConfirmationTokenMail(username: String, activationLink: String): String {
        return createHTML().html {
            head {
                title("Confirmação de Conta")
                style {
                    unsafe { +activationAccountMailCssStyles }
                }
            }
            body {
                div("container") {
                    h1 { +"Olá, $username!" }
                    p {
                        +"Obrigado por se juntar ao Borai! Por favor, confirme seu email para ativar sua conta. Clique no botão abaixo:"
                    }
                    a(href = activationLink, classes = "button") { +"Ativar Conta" }
                    p {
                        +"Este link é válido por 5 minutos. Se não solicitou este registro, ignore este email."
                    }
                    div("footer") {
                        +"© 2024 Sanisamoj. Todos os direitos reservados."
                    }
                }
            }
        }
    }

    fun buildAccountActivationMail(username: String): String {
        return createHTML().html {
            head {
                title("Ativação de Conta")
                style {
                    unsafe { +activationAccountMailCssStyles }
                }
            }
            body {
                div("container") {
                    h1 { +"Bem-vindo ao Borai, $username!" }
                    p {
                        +"Obrigado por se registrar no Borai! Sua conta foi ativada com sucesso."
                    }
                    div("info") {
                        h2 { +"Sobre o Borai" }
                        p {
                            +"Este é um sistema de gerenciamento e armazenamento de eventos. No qual você consegue descobrir eventos próximos a você, marcar presença, comentar avaliar e etc no eventos."
                        }
                        h3 { +"Funcionalidades Básicas" }
                        ul {
                            li { +"Criar e gerenciar eventos." }
                            li { +"Marcar presença em eventos." }
                            li { +"Comentar em eventos criados." }
                            li { +"Avaliar eventos e contas Promoters." }
                            li { +"Buscar eventos a partir de filtros específicos." }
                            li { +"Receber pontuações/Insígnias em condições específicas de uso da plataforma." }
                        }
                    }
                    p {
                        +"Atenciosamente,"
                        br()
                        +"Equipe Sanisamoj"
                    }
                    div("footer") {
                        +"© 2024 Sanisamoj. Todos os direitos reservados."
                    }
                }
            }
        }
    }

}