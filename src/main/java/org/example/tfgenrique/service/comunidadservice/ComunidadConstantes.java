package org.example.tfgenrique.service.comunidadservice;

import java.time.format.DateTimeFormatter;

class ComunidadConstantes {
    static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";
    static final String ROL_PROPIETARIO = "PROPIETARIO"; // Compatibilidad con datos anteriores.
    static final String ROL_USUARIO = "USUARIO";
    static final String ESTADO_AFILIACION_ACTIVA = "ACTIVA";
    static final String ESTADO_AFILIACION_INACTIVA = "INACTIVA";
    static final String ESTADO_EVENTO_ABIERTO = "ABIERTO";
    static final String ESTADO_EVENTO_CANCELADO = "CANCELADO";
    static final String ESTADO_INSCRIPCION_CONFIRMADA = "CONFIRMADA";
    static final String ESTADO_INVITACION_ABIERTA = "ABIERTA";
    static final String TIPO_EVENTO_COMUNIDAD = "COMUNIDAD";
    static final String SISTEMA_CLASIFICACION_SUIZO = "SUIZO";
    static final String FORMATO_40K = "WH40K_11";
    static final String FORMATO_AOS = "AOS_4";
    static final String PRIVACIDAD_PUBLICA = "PUBLICA";
    static final String PRIVACIDAD_PRIVADA = "PRIVADA";
    static final String ESTADO_SOLICITUD_PENDIENTE = "PENDIENTE";
    static final String ESTADO_SOLICITUD_ACEPTADA = "ACEPTADA";
    static final String ESTADO_SOLICITUD_RECHAZADA = "RECHAZADA";
    static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    static final DateTimeFormatter FORMATO_TITULO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ComunidadConstantes() {
    }

    static boolean esAdministrador(String rol) {
        return ROL_ADMINISTRADOR.equalsIgnoreCase(rol) || ROL_PROPIETARIO.equalsIgnoreCase(rol);
    }
}
