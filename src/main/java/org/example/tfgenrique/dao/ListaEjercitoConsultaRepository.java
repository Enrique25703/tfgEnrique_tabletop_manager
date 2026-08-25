package org.example.tfgenrique.dao;

import java.util.List;
import java.util.Optional;

import org.example.tfgenrique.entity.VersionListaEjercito;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

/**
 * Read model for the current version of each army list.
 *
 * <p>The JSON_TABLE query is deliberately isolated here: it is a MySQL-specific
 * optimization for the list screen and does not belong to entity persistence.</p>
 */
public interface ListaEjercitoConsultaRepository extends Repository<VersionListaEjercito, Long> {

    @Query(value = """
            SELECT le.id AS listaId, le.nombre AS nombreLista, sj.codigo AS formatoJuego,
                   le.nombre_faccion_snapshot AS faccion,
                   JSON_UNQUOTE(JSON_EXTRACT(vle.datos_lista, '$.ejercito')) AS ejercito,
                   le.puntos_actuales AS puntosActuales, le.limite_puntos AS limitePuntos,
                   vle.numero_version AS numeroVersion
            FROM versiones_lista_ejercito vle
            INNER JOIN listas_ejercito le ON le.id = vle.lista_ejercito_id
            INNER JOIN sistemas_juego sj ON sj.id = le.sistema_juego_id
            INNER JOIN usuarios u ON u.id = le.propietario_usuario_id
            WHERE u.nombre_usuario = :nombreUsuario
              AND vle.numero_version = le.numero_version_actual
            ORDER BY le.actualizado_en DESC, le.id DESC
            """, nativeQuery = true)
    List<ListaGuardadaProjection> buscarListasActualesPorUsuario(String nombreUsuario);

    @Query(value = """
            SELECT le.id AS listaId, jt.nombre AS nombreUnidad, jt.roles AS roles,
                   jt.puntosBase AS puntosBase, jt.categoria AS categoria
            FROM versiones_lista_ejercito vle
            INNER JOIN listas_ejercito le ON le.id = vle.lista_ejercito_id
            INNER JOIN sistemas_juego sj ON sj.id = le.sistema_juego_id
            INNER JOIN usuarios u ON u.id = le.propietario_usuario_id
            CROSS JOIN JSON_TABLE(vle.datos_lista, '$.unidades[*]' COLUMNS (
                nombre VARCHAR(255) PATH '$.nombre', roles VARCHAR(255) PATH '$.roles',
                puntosBase INT PATH '$.puntosBase', categoria VARCHAR(100) PATH '$.categoria'
            )) AS jt
            WHERE u.nombre_usuario = :nombreUsuario
              AND sj.codigo = 'WH40K_11'
              AND vle.numero_version = le.numero_version_actual
            ORDER BY le.actualizado_en DESC, le.id DESC
            """, nativeQuery = true)
    List<UnidadListaGuardadaProjection> buscarUnidadesDeListasActualesPorUsuario(String nombreUsuario);

    @Query(value = """
            SELECT le.id AS listaId, le.nombre AS nombreLista, sj.codigo AS formatoJuego,
                   le.nombre_faccion_snapshot AS faccion,
                   JSON_UNQUOTE(JSON_EXTRACT(vle.datos_lista, '$.ejercito')) AS ejercito,
                   le.puntos_actuales AS puntosActuales, le.limite_puntos AS limitePuntos,
                   vle.numero_version AS numeroVersion, vle.datos_lista AS datosListaJson
            FROM versiones_lista_ejercito vle
            INNER JOIN listas_ejercito le ON le.id = vle.lista_ejercito_id
            INNER JOIN sistemas_juego sj ON sj.id = le.sistema_juego_id
            INNER JOIN usuarios u ON u.id = le.propietario_usuario_id
            WHERE u.nombre_usuario = :nombreUsuario AND le.id = :listaId
              AND vle.numero_version = le.numero_version_actual
            """, nativeQuery = true)
    Optional<ListaExportacionProjection> buscarListaActualParaExportar(String nombreUsuario, Long listaId);

    interface ListaGuardadaProjection {
        Long getListaId();
        String getNombreLista();
        String getFormatoJuego();
        String getFaccion();
        String getEjercito();
        Integer getPuntosActuales();
        Integer getLimitePuntos();
        Integer getNumeroVersion();
    }

    interface UnidadListaGuardadaProjection {
        Long getListaId();
        String getNombreUnidad();
        String getRoles();
        Integer getPuntosBase();
        String getCategoria();
    }

    interface ListaExportacionProjection {
        Long getListaId();
        String getNombreLista();
        String getFormatoJuego();
        String getFaccion();
        String getEjercito();
        Integer getPuntosActuales();
        Integer getLimitePuntos();
        Integer getNumeroVersion();
        String getDatosListaJson();
    }
}
