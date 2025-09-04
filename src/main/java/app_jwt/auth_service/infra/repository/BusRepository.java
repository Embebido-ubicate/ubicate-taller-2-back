package app_jwt.auth_service.infra.repository;

import app_jwt.auth_service.domain.entity.Bus;
import app_jwt.auth_service.domain.enums.EstadoBus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {

    // Buscar por placa
    Optional<Bus> findByPlaca(String placa);

    // Buses activos de una empresa específica
    Page<Bus> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);

    // Buses por estado de una empresa
    List<Bus> findByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoBus estado);

    // Contar buses activos por empresa
    Long countByEmpresaIdAndActivoTrue(Long empresaId);

    // Verificar si existe placa (para validaciones)
    boolean existsByPlacaAndActivoTrue(String placa);

    // Buscar por empresa y activos (sin paginación)
    List<Bus> findByEmpresaIdAndActivoTrue(Long empresaId);

    // Query personalizada para estadísticas
    @Query("SELECT b.estado, COUNT(b) FROM Bus b WHERE b.empresaId = :empresaId AND b.activo = true GROUP BY b.estado")
    List<Object[]> findBusStatsByEmpresaId(@Param("empresaId") Long empresaId);
}