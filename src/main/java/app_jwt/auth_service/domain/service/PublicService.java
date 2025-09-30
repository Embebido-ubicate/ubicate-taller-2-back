package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.bus.BusResponse;
import app_jwt.auth_service.domain.dtos.route.RouteResponse;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Route;
import app_jwt.auth_service.domain.enums.EstadoBus;
import app_jwt.auth_service.domain.enums.EstadoRuta;
import app_jwt.auth_service.infra.repository.BusRepository;
import app_jwt.auth_service.infra.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;

    @Transactional(readOnly = true)
    public List<BusResponse> getBusesActivos(Long empresaId) {
        List<Bus> buses;

        if (empresaId != null) {
            buses = busRepository
                    .findByEmpresaIdAndActivoTrueAndLatitudIsNotNullAndLongitudIsNotNullWithRoute(empresaId);
        } else {
            buses = busRepository.findAllActivoTrueAndLatitudIsNotNullWithRoute();
        }

        return buses.stream()
                .filter(b -> b.getEstado() == EstadoBus.EN_RUTA || b.getEstado() == EstadoBus.ACTIVO)
                .map(BusResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BusResponse getBusUbicacion(Long busId) {
        Bus bus = busRepository.findByIdWithRoute(busId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bus no encontrado"));

        if (!Boolean.TRUE.equals(bus.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Bus no disponible");
        }

        return BusResponse.from(bus);
    }

    // Nuevos métodos para rutas
    @Transactional(readOnly = true)
    public List<RouteResponse> getAllRutas(Long empresaId) {
        List<Route> rutas;

        if (empresaId != null) {
            rutas = routeRepository
                    .findByEmpresaIdAndActivoTrueAndEstadoWithBuses(empresaId, EstadoRuta.ACTIVA);
        } else {
            rutas = routeRepository.findAllActivoTrueAndEstado(EstadoRuta.ACTIVA);
        }

        return rutas.stream()
                .map(RouteResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RouteResponse getRutaById(Long rutaId) {
        Route ruta = routeRepository.findByIdWithBuses(rutaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ruta no encontrada"));

        if (!Boolean.TRUE.equals(ruta.getActivo()) || ruta.getEstado() != EstadoRuta.ACTIVA) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Ruta no disponible");
        }

        return RouteResponse.from(ruta);
    }
}