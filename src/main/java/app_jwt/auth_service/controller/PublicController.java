package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.dtos.bus.BusResponse;
import app_jwt.auth_service.domain.dtos.route.RouteResponse;
import app_jwt.auth_service.domain.service.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final PublicService publicService;

    @GetMapping("/buses/activos")
    public ResponseEntity<List<BusResponse>> getBusesActivos(
            @RequestParam(required = false) Long empresaId) {
        return ResponseEntity.ok(publicService.getBusesActivos(empresaId));
    }

    @GetMapping("/buses/{busId}")
    public ResponseEntity<BusResponse> getBusUbicacion(@PathVariable Long busId) {
        return ResponseEntity.ok(publicService.getBusUbicacion(busId));
    }

    // Nuevos endpoints para rutas
    @GetMapping("/rutas")
    public ResponseEntity<List<RouteResponse>> getAllRutas(
            @RequestParam(required = false) Long empresaId) {
        return ResponseEntity.ok(publicService.getAllRutas(empresaId));
    }

    @GetMapping("/rutas/{rutaId}")
    public ResponseEntity<RouteResponse> getRutaById(@PathVariable Long rutaId) {
        return ResponseEntity.ok(publicService.getRutaById(rutaId));
    }
}