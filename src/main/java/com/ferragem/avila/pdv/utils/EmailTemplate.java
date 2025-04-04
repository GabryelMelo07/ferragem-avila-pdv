package com.ferragem.avila.pdv.utils;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmailTemplate {

    @Value("${logotipo-email-template}")
    private String logoImgLink;
    
    public String getResetPasswordTemplate(String nome, String link) {
        return """
                    <!DOCTYPE html>
                    <html lang="pt-BR">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body {
                                font-family: Arial, sans-serif;
                                margin: 0;
                                padding: 0;
                                background-color: #e4e4e4;
                            }
                            .container {
                                width: 100%%;
                                max-width: 600px;
                                margin: 0 auto;
                                background-color: #f1f1f1;
                                border-radius: 5px;
                                overflow: hidden;
                                box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
                            }
                            .header {
                                width: 100%%;
                                background-color: #f5cb28;
                                padding: 20px;
                            }
                            .logo-img {
                                width: 80%%;
                                max-height: 200px;
                                object-fit: contain;
                                display: block;
                                margin: 0 auto;
                            }
                            .content {
                                padding: 20px;
                            }
                            .button {
                                display: inline-block;
                                padding: 10px 20px;
                                margin: 20px 0;
                                text-decoration: none;
                                background-color: #f5cb28;
                                color: #191919 !important;
                                border-radius: 5px;
                            }
                            .footer {
                                background-color: #191919;
                                text-align: center;
                                padding: 10px;
                                font-size: 12px;
                                color: #f1f1f1;
                            }
                            @media (max-width: 600px) {
                                .container {
                                    width: 100%%;
                                }
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <img class="logo-img" src="%s" alt="Ferragem Ávila">
                            </div>
                            <div class="content">
                                <h1>Redefinição de Senha</h1>
                                <br>
                                <h2>Olá, %s.</h2>
                                <p>Recebemos um pedido para redefinir sua senha. Clique no botão abaixo para criar uma nova senha.</p>
                                <a href="%s" class="button">Redefinir Senha</a>
                                <p>Se você não solicitou a redefinição de senha, ignore este e-mail.</p>
                                <p>Atenciosamente,<br>PDV - Ferragem Ávila</p>
                            </div>
                            <div class="footer">
                                <p>&copy; %d Ferragem Ávila. Todos os direitos reservados.</p>
                            </div>
                        </div>
                    </body>
                    </html>
                """.formatted(logoImgLink, nome, link, LocalDate.now().getYear());
    }

}
