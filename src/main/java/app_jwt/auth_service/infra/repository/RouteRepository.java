package app_jwt.auth_service.infra.repository;

import app_jwt.auth_service.domain.entity.Route;
import app_jwt.auth_service.domain.enums.EstadoRuta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    boolean existsByCodigoAndEmpresaIdAndActivoTrue(String codigo, Long empresaId);
    Page<Route> findByEmpresaIdAndActivoTrue(Long empresaId, Pageable pageable);
    List<Route> findByEmpresaIdAndEstadoAndActivoTrue(Long empresaId, EstadoRuta estado);
}
