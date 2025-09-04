package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.dtos.bus.ApiResponse;
import app_jwt.auth_service.domain.dtos.route.CreateRouteRequest;
import app_jwt.auth_service.domain.dtos.route.RouteResponse;
import app_jwt.auth_service.domain.dtos.route.UpdateRouteRequest;
import app_jwt.auth_service.domain.enums.EstadoRuta;
import app_jwt.auth_service.domain.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('EMPRESA')")
public class RouteController {

    private final RouteService routeService;

    @PostMapping
    public ResponseEntity<RouteResponse> create(
            @Valid @RequestBody CreateRouteRequest request,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        RouteResponse resp = routeService.create(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping
    public ResponseEntity<Page<RouteResponse>> list(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        return ResponseEntity.ok(routeService.list(empresaId, pageable));
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteResponse> getById(
            @PathVariable Long routeId,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        return ResponseEntity.ok(routeService.getById(routeId, empresaId));
    }

    @PutMapping("/{routeId}")
    public ResponseEntity<RouteResponse> update(
            @PathVariable Long routeId,
            @Valid @RequestBody UpdateRouteRequest request,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        return ResponseEntity.ok(routeService.update(routeId, request, empresaId));
    }

    @DeleteMapping("/{routeId}")
    public ResponseEntity<ApiResponse> delete(
            @PathVariable Long routeId,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        routeService.delete(routeId, empresaId);
        return ResponseEntity.ok(new ApiResponse("Ruta eliminada exitosamente", true));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<RouteResponse>> listByEstado(
            @PathVariable EstadoRuta estado,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        return ResponseEntity.ok(routeService.listByEstado(empresaId, estado));
    }

    private Long getEmpresaIdFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return 1L;
    }
}
