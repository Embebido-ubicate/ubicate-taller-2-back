package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.bus.*;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.enums.EstadoBus;
import app_jwt.auth_service.infra.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusService {

    private final BusRepository busRepository;

    @Transactional
    public BusResponse createBus(CreateBusRequest request, Long empresaId) {
        // Validar que no exista la placa
        if (busRepository.existsByPlacaAndActivoTrue(request.getPlaca())) {
            throw new RuntimeException("Ya existe un bus con la placa: " + request.getPlaca());
        }

        Bus bus = Bus.builder()
                .placa(request.getPlaca().toUpperCase())
                .modelo(request.getModelo())
                .capacidad(request.getCapacidad())
                .anio(request.getAnio())
                .color(request.getColor())
                .empresaId(empresaId)
                .estado(EstadoBus.INACTIVO)
                .activo(true)
                .build();

        Bus savedBus = busRepository.save(bus);

        log.info("Bus creado exitosamente - Placa: {}, Empresa ID: {}",
                request.getPlaca(), empresaId);

        return BusResponse.from(savedBus);
    }

    @Transactional(readOnly = true)
    public Page<BusResponse> getBusesByEmpresa(Long empresaId, Pageable pageable) {
        Page<Bus> buses = busRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);

        return buses.map(BusResponse::from);
    }

    @Transactional(readOnly = true)
    public List<BusResponse> getBusesByEstado(Long empresaId, EstadoBus estado) {
        List<Bus> buses = busRepository.findByEmpresaIdAndEstadoAndActivoTrue(empresaId, estado);

        return buses.stream()
                .map(BusResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BusResponse getBusById(Long busId, Long empresaId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

        // Verificar que pertenece a la empresa
        if (!bus.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para acceder a este bus");
        }

        if (!bus.getActivo()) {
            throw new RuntimeException("Bus no disponible");
        }

        return BusResponse.from(bus);
    }

    @Transactional
    public BusResponse updateBus(Long busId, UpdateBusRequest request, Long empresaId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

        // Verificar permisos
        if (!bus.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para modificar este bus");
        }

        if (!bus.getActivo()) {
            throw new RuntimeException("No se puede modificar un bus inactivo");
        }

        // Actualizar campos si están presentes
        if (request.getModelo() != null) {
            bus.setModelo(request.getModelo());
        }
        if (request.getCapacidad() != null) {
            bus.setCapacidad(request.getCapacidad());
        }
        if (request.getAnio() != null) {
            bus.setAnio(request.getAnio());
        }
        if (request.getColor() != null) {
            bus.setColor(request.getColor());
        }
        if (request.getEstado() != null) {
            bus.setEstado(request.getEstado());
        }

        Bus updatedBus = busRepository.save(bus);

        log.info("Bus actualizado - ID: {}, Placa: {}", busId, bus.getPlaca());

        return BusResponse.from(updatedBus);
    }

    @Transactional
    public void deleteBus(Long busId, Long empresaId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

        // Verificar permisos
        if (!bus.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para eliminar este bus");
        }

        // Soft delete
        bus.setActivo(false);
        busRepository.save(bus);

        log.info("Bus eliminado (soft delete) - ID: {}, Placa: {}", busId, bus.getPlaca());
    }

    @Transactional
    public BusResponse changeEstadoBus(Long busId, EstadoBus nuevoEstado, Long empresaId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

        // Verificar permisos
        if (!bus.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para modificar este bus");
        }

        if (!bus.getActivo()) {
            throw new RuntimeException("No se puede cambiar el estado de un bus inactivo");
        }

        bus.setEstado(nuevoEstado);
        Bus updatedBus = busRepository.save(bus);

        log.info("Estado del bus cambiado - ID: {}, Nuevo estado: {}", busId, nuevoEstado);

        return BusResponse.from(updatedBus);
    }

    @Transactional(readOnly = true)
    public BusStatsResponse getBusStats(Long empresaId) {
        List<Object[]> estadisticas = busRepository.findBusStatsByEmpresaId(empresaId);
        Long totalBuses = busRepository.countByEmpresaIdAndActivoTrue(empresaId);

        Map<String, Long> estadoPorCantidad = new HashMap<>();
        Long activos = 0L, inactivos = 0L, enRuta = 0L, enMantenimiento = 0L;

        for (Object[] stat : estadisticas) {
            EstadoBus estado = (EstadoBus) stat[0];
            Long cantidad = (Long) stat[1];

            estadoPorCantidad.put(estado.name(), cantidad);

            switch (estado) {
                case ACTIVO -> activos = cantidad;
                case INACTIVO -> inactivos = cantidad;
                case EN_RUTA -> enRuta = cantidad;
                case MANTENIMIENTO -> enMantenimiento = cantidad;
            }
        }

        return BusStatsResponse.builder()
                .totalBuses(totalBuses)
                .busesActivos(activos)
                .busesInactivos(inactivos)
                .busesEnRuta(enRuta)
                .busesEnMantenimiento(enMantenimiento)
                .estadoPorCantidad(estadoPorCantidad)
                .build();
    }

    @Transactional
    public BusResponse updateBusLocation(Long busId, UpdateLocationRequest request, Long empresaId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

        if (!bus.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para actualizar este bus");
        }

        bus.setLatitud(request.getLatitud());
        bus.setLongitud(request.getLongitud());
        bus.setVelocidad(request.getVelocidad());
        bus.setUltimaUbicacion(LocalDateTime.now());

        Bus updatedBus = busRepository.save(bus);
        return BusResponse.from(updatedBus);
    }

    @Transactional(readOnly = true)
    public List<BusResponse> getBusesWithLocation(Long empresaId) {
        List<Bus> buses = busRepository.findByEmpresaIdAndActivoTrueAndLatitudIsNotNull(empresaId);
        return buses.stream()
                .map(BusResponse::from)
                .collect(Collectors.toList());
    }
}