package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.route.CreateRouteRequest;
import app_jwt.auth_service.domain.dtos.route.RouteResponse;
import app_jwt.auth_service.domain.dtos.route.UpdateRouteRequest;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Route;
import app_jwt.auth_service.domain.enums.EstadoRuta;
import app_jwt.auth_service.infra.repository.BusRepository;
import app_jwt.auth_service.infra.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private final RouteRepository routeRepository;
    private final BusRepository busRepository;

    @Transactional
    public RouteResponse create(CreateRouteRequest req, Long empresaId) {
        log.info("=== CREANDO RUTA ===");
        log.info("EmpresaId del auth: {} (tipo: {})", empresaId, empresaId.getClass().getSimpleName());
        log.info("EmpresaId del request: {} (tipo: {})", req.getEmpresaId(), req.getEmpresaId() != null ? req.getEmpresaId().getClass().getSimpleName() : "null");

        // COMENTADA TEMPORALMENTE - NO VALIDAR EMPRESAID
        // Usar solo el empresaId del auth que siempre es 1L
        log.info("SALTANDO validación de empresaId - usando empresaId del auth: {}", empresaId);

        if (routeRepository.existsByCodigoAndEmpresaIdAndActivoTrue(req.getCodigo(), empresaId)) {
            log.error("Código de ruta ya existe: {}", req.getCodigo());
            throw new RuntimeException("Código de ruta ya existe");
        }

        Set<Bus> buses = new HashSet<>();
        if (req.getBusIds() != null && !req.getBusIds().isEmpty()) {
            List<Bus> found = busRepository.findAllById(req.getBusIds());
            found.forEach(b -> {
                if (!b.getEmpresaId().equals(empresaId) || !Boolean.TRUE.equals(b.getActivo()))
                    throw new RuntimeException("Bus no válido para la empresa");
            });
            buses.addAll(found);
            log.info("Buses asignados: {}", buses.size());
        }

        Route r = Route.builder()
                .nombre(req.getNombre())
                .descripcion(req.getDescripcion())
                .codigo(req.getCodigo())
                .origen(req.getOrigen())
                .destino(req.getDestino())
                .colorHex(req.getColorHex())
                .polyline(req.getPolyline())
                .estado(EstadoRuta.ACTIVA)
                .activo(true)
                .empresaId(empresaId) // USAR EL DEL AUTH - SIEMPRE 1L
                .buses(buses)
                .build();

        Route saved = routeRepository.save(r);
        log.info("Ruta creada exitosamente - ID: {}, Código: {}", saved.getId(), saved.getCodigo());
        return RouteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<RouteResponse> list(Long empresaId, Pageable pageable) {
        return routeRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable).map(RouteResponse::from);
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> listByEstado(Long empresaId, EstadoRuta estado) {
        return routeRepository.findByEmpresaIdAndEstadoAndActivoTrue(empresaId, estado).stream().map(RouteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RouteResponse getById(Long id, Long empresaId) {
        Route r = routeRepository.findById(id).orElseThrow(() -> new RuntimeException("Ruta no encontrada"));
        if (!r.getEmpresaId().equals(empresaId) || !Boolean.TRUE.equals(r.getActivo()))
            throw new RuntimeException("Acceso no permitido");
        return RouteResponse.from(r);
    }

    @Transactional
    public RouteResponse update(Long id, UpdateRouteRequest req, Long empresaId) {
        Route r = routeRepository.findById(id).orElseThrow(() -> new RuntimeException("Ruta no encontrada"));
        if (!r.getEmpresaId().equals(empresaId) || !Boolean.TRUE.equals(r.getActivo()))
            throw new RuntimeException("Acceso no permitido");
        if (req.getNombre() != null) r.setNombre(req.getNombre());
        if (req.getDescripcion() != null) r.setDescripcion(req.getDescripcion());
        if (req.getOrigen() != null) r.setOrigen(req.getOrigen());
        if (req.getDestino() != null) r.setDestino(req.getDestino());
        if (req.getColorHex() != null) r.setColorHex(req.getColorHex());
        if (req.getPolyline() != null) r.setPolyline(req.getPolyline());
        if (req.getEstado() != null) r.setEstado(req.getEstado());
        if (req.getBusIds() != null) {
            List<Bus> found = busRepository.findAllById(req.getBusIds());
            found.forEach(b -> {
                if (!b.getEmpresaId().equals(empresaId) || !Boolean.TRUE.equals(b.getActivo()))
                    throw new RuntimeException("Bus no válido para la empresa");
            });
            r.setBuses(new HashSet<>(found));
        }
        Route saved = routeRepository.save(r);
        return RouteResponse.from(saved);
    }

    @Transactional
    public void delete(Long id, Long empresaId) {
        Route r = routeRepository.findById(id).orElseThrow(() -> new RuntimeException("Ruta no encontrada"));
        if (!r.getEmpresaId().equals(empresaId)) throw new RuntimeException("Acceso no permitido");
        r.setActivo(false);
        routeRepository.save(r);
    }
}