package org.watts.shared.service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.watts.security.user.model.Usuario;
import org.watts.security.user.repository.UsuarioRepository;

import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final UsuarioRepository usuarioRepository;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender,
                        UsuarioRepository usuarioRepository) {
        this.mailSender = mailSender;
        this.usuarioRepository = usuarioRepository;
    }

    @Async
    public void enviarNotificacionMovimiento(String asunto, String contenidoHtml) {
        if (this.mailSender == null) {
            System.out.println("ADVERTENCIA: No se ha configurado el servicio de correo. Se omite el envío.");
            return;
        }

        List<Usuario> admins = usuarioRepository.findByRolNombre("ADMIN");

        for (Usuario admin : admins) {
            if (admin.getEmail() == null) continue;

            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom("notificaciones@wattscycling.es");
                helper.setTo("serloiarc@alu.edu.gva.es");
                helper.setSubject(asunto);
                helper.setText(contenidoHtml, true);

                mailSender.send(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
