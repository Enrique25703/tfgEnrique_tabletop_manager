package org.example.tfgenrique.service.comunidadservice;

import org.example.tfgenrique.dao.InvitacionPartidaComunidadRepository;
import org.example.tfgenrique.entity.AfiliacionComunidad;
import org.example.tfgenrique.entity.Comunidad;
import org.example.tfgenrique.entity.InvitacionPartidaComunidad;
import org.example.tfgenrique.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComunidadInvitacionService {

    private final InvitacionPartidaComunidadRepository invitacionPartidaComunidadRepository;
    private final ComunidadMiembroService comunidadMiembroService;

    public ComunidadInvitacionService(
            InvitacionPartidaComunidadRepository invitacionPartidaComunidadRepository,
            ComunidadMiembroService comunidadMiembroService
    ) {
        this.invitacionPartidaComunidadRepository = invitacionPartidaComunidadRepository;
        this.comunidadMiembroService = comunidadMiembroService;
    }

    @Transactional
    public Long crearInvitacionPartida(
            Long usuarioId,
            Long comunidadId,
            ComunidadService.CrearInvitacionPartidaRequest request
    ) {
        Usuario usuario = comunidadMiembroService.buscarUsuario(usuarioId);
        Comunidad comunidad = comunidadMiembroService.buscarComunidad(comunidadId);
        AfiliacionComunidad afiliacion = comunidadMiembroService.buscarAfiliacionActiva(usuario, comunidad);

        if (!ComunidadConstantes.ROL_USUARIO.equals(afiliacion.getRolComunidad())) {
            throw new IllegalArgumentException("Solo los usuarios normales pueden crear invitaciones a partidas.");
        }
        if (request == null) {
            throw new IllegalArgumentException("No se han recibido los datos de la invitacion.");
        }

        LocalDateTime fecha = request.getFecha();
        String lugar = normalizarTexto(request.getLugar());
        String formatoJuego = normalizarTexto(request.getFormatoJuego());
        String mensaje = normalizarTexto(request.getMensaje());

        if (fecha == null) {
            throw new IllegalArgumentException("Debes indicar una fecha para la invitacion.");
        }
        if (lugar.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un lugar para la partida.");
        }
        if (lugar.length() > 150) {
            throw new IllegalArgumentException("El lugar no puede superar los 150 caracteres.");
        }
        if (formatoJuego.isBlank()) {
            throw new IllegalArgumentException("Debes indicar un formato de juego.");
        }
        if (formatoJuego.length() > 30) {
            throw new IllegalArgumentException("El formato de juego no es válido.");
        }

        InvitacionPartidaComunidad invitacion = new InvitacionPartidaComunidad();
        invitacion.setComunidad(comunidad);
        invitacion.setCreadorUsuario(usuario);
        invitacion.setFormatoJuego(formatoJuego);
        invitacion.setLugar(lugar);
        invitacion.setFechaPropuesta(fecha);
        invitacion.setMensaje(mensaje.isBlank() ? null : mensaje);
        invitacion.setEstado(ComunidadConstantes.ESTADO_INVITACION_ABIERTA);
        invitacion.setCreadaEn(LocalDateTime.now());
        invitacionPartidaComunidadRepository.save(invitacion);

        return comunidad.getId();
    }

    public List<InvitacionPartidaComunidad> buscarInvitaciones(Comunidad comunidad) {
        return invitacionPartidaComunidadRepository.findByComunidadOrderByFechaPropuestaAscCreadaEnDesc(comunidad);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.trim();
    }
}
