package br.com.delta.delta_api_postgres.modules.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class PasswordResetEmailSender {
    private final ObjectProvider<JavaMailSender> mailSenders;
    private final String baseUrl;
    private final String from;
    private final String smtpHost;

    public PasswordResetEmailSender(ObjectProvider<JavaMailSender> mailSenders,
                                    @Value("${app.public-base-url}") String baseUrl,
                                    @Value("${app.mail.from:}") String from,
                                    @Value("${spring.mail.host:}") String smtpHost) {
        this.mailSenders = mailSenders;
        this.baseUrl = baseUrl;
        this.from = from;
        this.smtpHost = smtpHost;
    }

    public boolean isConfigured() {
        return !smtpHost.isBlank() && !from.isBlank() && !baseUrl.isBlank() && mailSenders.getIfAvailable() != null;
    }

    public void send(String recipient, String name, String token) {
        JavaMailSender sender = mailSenders.getIfAvailable();
        if (!isConfigured() || sender == null) throw new IllegalStateException("SMTP não configurado.");

        String link = UriComponentsBuilder.fromUriString(baseUrl.strip().replaceAll("/+$", ""))
                .path("/redefinir-senha").queryParam("token", token).build().encode().toUriString();
        String safeName = HtmlUtils.htmlEscape(name == null || name.isBlank() ? "" : name.strip());
        String greeting = safeName.isBlank() ? "Olá!" : "Olá, " + safeName + "!";
        String safeLink = HtmlUtils.htmlEscape(link);
        String html = """
                <!doctype html><html lang="pt-BR"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;background:#101722;font-family:Arial,sans-serif;color:#f7f9fc">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#101722;padding:32px 12px"><tr><td align="center">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:560px;background:#1b2636;border:1px solid #334b69;border-radius:20px">
                      <tr><td style="padding:32px 30px 12px;color:#78a8ff;font-size:28px;font-weight:bold">Delta</td></tr>
                      <tr><td style="padding:0 30px 28px"><h1 style="margin:12px 0 16px;font-size:24px;color:#fff">Redefinição de senha</h1>
                        <p style="font-size:16px;line-height:1.6;color:#d8e3f3">%s Recebemos uma solicitação para criar uma nova senha para sua conta.</p>
                        <p style="margin:28px 0"><a href="%s" style="display:inline-block;padding:14px 22px;border-radius:999px;background:#356ce0;color:#fff;text-decoration:none;font-weight:bold">Criar nova senha</a></p>
                        <p style="font-size:14px;line-height:1.6;color:#b7c8df">O link expira em 30 minutos e só pode ser usado uma vez. Se você não solicitou a redefinição, ignore este e-mail.</p>
                        <p style="font-size:12px;line-height:1.5;color:#8fa4bf;word-break:break-all">Se o botão não funcionar, copie este endereço:<br>%s</p>
                      </td></tr>
                    </table>
                  </td></tr></table>
                </body></html>
                """.formatted(greeting, safeLink, safeLink);
        String plainText = greeting + " Recebemos uma solicitação de redefinição de senha. Acesse: " + link
                + " Este link expira em 30 minutos e só pode ser usado uma vez.";
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject("Redefina sua senha do Delta");
            helper.setText(plainText, html);
            sender.send(message);
        } catch (MessagingException | RuntimeException exception) {
            log.warn("Password recovery email delivery failed.");
            throw new IllegalStateException("Não foi possível enviar o e-mail de recuperação.");
        }
    }
}
