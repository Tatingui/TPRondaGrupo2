package com.ronda.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio responsable de enviar emails.
 *
 * ¿Por que un servicio separado y no mandar el email directo desde AuthService?
 * Porque asi se respeta el principio de responsabilidad unica (SRP):
 * AuthService maneja la LOGICA de autenticacion (generar OTP, verificar, etc.)
 * y EmailService maneja el COMO se entrega ese codigo al usuario.
 *
 * Si manana quisieras cambiar de Gmail a SendGrid o a SMS, solo tocas este
 * archivo. AuthService ni se entera.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envia el codigo OTP por email.
     *
     * @param destinatario email del usuario
     * @param codigo       el OTP de 6 digitos
     */
    public void enviarOtp(String destinatario, String codigo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Ronda - Tu código de verificación");
            mensaje.setText(
                    "¡Hola!\n\n"
                    + "Tu código de verificación para Ronda es:\n\n"
                    + "    " + codigo + "\n\n"
                    + "Este código expira en 5 minutos.\n"
                    + "Si no solicitaste este código, ignorá este mensaje.\n\n"
                    + "— Equipo Ronda"
            );

            mailSender.send(mensaje);
            log.info("Email OTP enviado exitosamente a {}", destinatario);

        } catch (Exception e) {
            // Logueamos el error pero NO lo propagamos: si el email falla,
            // el OTP igual queda guardado en la DB. El usuario puede pedir
            // reenvio con /otp/resend.
            // En produccion pondrias un sistema de reintentos (retry) o una
            // cola de mensajes (RabbitMQ, etc.), pero para el TPO alcanza con
            // loguear y seguir.
            log.error("Error al enviar email OTP a {}: {}", destinatario, e.getMessage());
        }
    }
}
