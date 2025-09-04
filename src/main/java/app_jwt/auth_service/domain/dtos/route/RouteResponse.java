package app_jwt.auth_service.domain.dtos.route;

import app_jwt.auth_service.domain.entity.Route;
import app_jwt.auth_service.domain.enums.EstadoRuta;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RouteResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String codigo;
    private String origen;
    private String destino;
    private String colorHex;
    private String polyline;
    private EstadoRuta estado;
    private Boolean activo;
    private Long empresaId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private List<Long> busIds;

    public static RouteResponse from(Route r) {
        return RouteResponse.builder()
                .id(r.getId())
                .nombre(r.getNombre())
                .descripcion(r.getDescripcion())
                .codigo(r.getCodigo())
                .origen(r.getOrigen())
                .destino(r.getDestino())
                .colorHex(r.getColorHex())
                .polyline(r.getPolyline())
                .estado(r.getEstado())
                .activo(r.getActivo())
                .empresaId(r.getEmpresaId())
                .fechaCreacion(r.getFechaCreacion())
                .fechaActualizacion(r.getFechaActualizacion())
                .busIds(r.getBuses().stream().map(b -> b.getId()).toList())
                .build();
    }
}
