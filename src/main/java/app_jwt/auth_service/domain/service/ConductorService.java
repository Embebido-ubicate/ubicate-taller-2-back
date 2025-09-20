package app_jwt.auth_service.domain.service;

import app_jwt.auth_service.domain.dtos.conductor.*;
import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.entity.Conductor;
import app_jwt.auth_service.domain.entity.Usuario;
import app_jwt.auth_service.domain.enums.EstadoConductor;
import app_jwt.auth_service.domain.enums.Role;
import app_jwt.auth_service.domain.enums.TurnoConductor;
import app_jwt.auth_service.infra.repository.BusRepository;
import app_jwt.auth_service.infra.repository.ConductorRepository;
import app_jwt.auth_service.infra.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConductorService {

    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;
    private final BusRepository busRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ConductorResponse createConductor(CreateConductorRequest request, Long empresaId) {
        log.info("Creando conductor para empresa ID: {}", empresaId);

        // Validaciones
        if (usuarioRepository.findByCorreo(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (conductorRepository.existsByNumeroLicenciaAndActivoTrue(request.getNumeroLicencia())) {
            throw new RuntimeException("Ya existe un conductor con ese número de licencia");
        }

        // Crear usuario
        Usuario usuario = Usuario.builder()
                .username(request.getEmail())
                .correo(request.getEmail())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .telefono(request.getTelefono())
                .dni(request.getDni())
                .password(passwordEncoder.encode("TempPass123!"))
                .role(Role.CHOFER)
                .build();

        Usuario savedUsuario = usuarioRepository.save(usuario);

        // Validar bus si está asignado
        Bus busAsignado = null;
        if (request.getBusAsignadoId() != null) {
            busAsignado = busRepository.findById(request.getBusAsignadoId())
                    .orElseThrow(() -> new RuntimeException("Bus no encontrado"));
            if (!busAsignado.getEmpresaId().equals(empresaId)) {
                throw new RuntimeException("El bus no pertenece a su empresa");
            }
        }

        // Crear conductor
        Conductor conductor = Conductor.builder()
                .usuario(savedUsuario)
                .numeroLicencia(request.getNumeroLicencia().toUpperCase())
                .categoriaLicencia(request.getCategoriaLicencia())
                .fechaVencimientoLicencia(request.getFechaVencimientoLicencia())
                .turno(request.getTurno())
                .estado(EstadoConductor.ACTIVO)
                .busAsignado(busAsignado)
                .empresaId(empresaId)
                .fechaIngreso(LocalDate.now())
                .activo(true)
                .build();

        Conductor savedConductor = conductorRepository.save(conductor);
        log.info("Conductor creado - Licencia: {}", savedConductor.getNumeroLicencia());

        return ConductorResponse.from(savedConductor);
    }

    @Transactional(readOnly = true)
    public Page<ConductorResponse> getConductores(Long empresaId, Pageable pageable) {
        Page<Conductor> conductores = conductorRepository.findByEmpresaIdAndActivoTrue(empresaId, pageable);
        return conductores.map(ConductorResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ConductorResponse> searchConductores(Long empresaId, String searchTerm, Pageable pageable) {
        Page<Conductor> conductores = conductorRepository.searchConductores(empresaId, searchTerm, pageable);
        return conductores.map(ConductorResponse::from);
    }

    @Transactional(readOnly = true)
    public ConductorResponse getConductorById(Long conductorId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId) || !conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para acceder a este conductor");
        }

        return ConductorResponse.from(conductor);
    }

    @Transactional
    public ConductorResponse updateConductor(Long conductorId, UpdateConductorRequest request, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId) || !conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        // Actualizar campos
        if (request.getTelefono() != null) {
            conductor.getUsuario().setTelefono(request.getTelefono());
        }
        if (request.getFechaVencimientoLicencia() != null) {
            conductor.setFechaVencimientoLicencia(request.getFechaVencimientoLicencia());
        }
        if (request.getTurno() != null) {
            conductor.setTurno(request.getTurno());
        }
        if (request.getEstado() != null) {
            conductor.setEstado(request.getEstado());
        }
        if (request.getBusAsignadoId() != null) {
            Bus bus = busRepository.findById(request.getBusAsignadoId())
                    .orElseThrow(() -> new RuntimeException("Bus no encontrado"));
            if (!bus.getEmpresaId().equals(empresaId)) {
                throw new RuntimeException("El bus no pertenece a su empresa");
            }
            conductor.setBusAsignado(bus);
        }

        Conductor updatedConductor = conductorRepository.save(conductor);
        return ConductorResponse.from(updatedConductor);
    }

    @Transactional
    public void deleteConductor(Long conductorId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para eliminar este conductor");
        }

        conductor.setActivo(false);
        conductorRepository.save(conductor);
    }

    @Transactional
    public ConductorResponse cambiarEstado(Long conductorId, EstadoConductor estado, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId) || !conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        conductor.setEstado(estado);
        Conductor updatedConductor = conductorRepository.save(conductor);
        return ConductorResponse.from(updatedConductor);
    }

    @Transactional
    public ConductorResponse asignarBus(Long conductorId, Long busId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId) || !conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus no encontrado"));

        if (!bus.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("El bus no pertenece a su empresa");
        }

        conductor.setBusAsignado(bus);
        Conductor updatedConductor = conductorRepository.save(conductor);
        return ConductorResponse.from(updatedConductor);
    }

    @Transactional
    public ConductorResponse removerBus(Long conductorId, Long empresaId) {
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado"));

        if (!conductor.getEmpresaId().equals(empresaId) || !conductor.getActivo()) {
            throw new RuntimeException("No tiene permisos para modificar este conductor");
        }

        conductor.setBusAsignado(null);
        Conductor updatedConductor = conductorRepository.save(conductor);
        return ConductorResponse.from(updatedConductor);
    }

    @Transactional(readOnly = true)
    public List<ConductorResponse> getConductoresByEstado(Long empresaId, EstadoConductor estado) {
        List<Conductor> conductores = conductorRepository.findByEmpresaIdAndEstadoAndActivoTrue(empresaId, estado);
        return conductores.stream().map(ConductorResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConductorResponse> getConductoresByTurno(Long empresaId, TurnoConductor turno) {
        List<Conductor> conductores = conductorRepository.findByEmpresaIdAndTurnoAndActivoTrue(empresaId, turno);
        return conductores.stream().map(ConductorResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConductorStatsResponse getStats(Long empresaId) {
        Long total = conductorRepository.countByEmpresaIdAndActivoTrue(empresaId);
        Long activos = conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.ACTIVO);
        Long inactivos = conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.INACTIVO);
        Long vacaciones = conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.VACACIONES);
        Long suspendidos = conductorRepository.countByEmpresaIdAndEstadoAndActivoTrue(empresaId, EstadoConductor.SUSPENDIDO);
        Long conBus = conductorRepository.countByEmpresaIdAndBusAsignadoIsNotNullAndActivoTrue(empresaId);
        Long sinBus = conductorRepository.countByEmpresaIdAndBusAsignadoIsNullAndActivoTrue(empresaId);
        Long licenciasVencidas = conductorRepository.countLicenciasVencidas(empresaId);
        Long licenciasPorVencer = conductorRepository.countLicenciasPorVencer(empresaId, LocalDate.now().plusDays(30));

        return ConductorStatsResponse.builder()
                .totalConductores(total)
                .conductoresActivos(activos)
                .conductoresInactivos(inactivos)
                .conductoresVacaciones(vacaciones)
                .conductoresSuspendidos(suspendidos)
                .conductoresConBus(conBus)
                .conductoresSinBus(sinBus)
                .licenciasVencidas(licenciasVencidas)
                .licenciasPorVencer(licenciasPorVencer)
                .build();
    }
}