package app_jwt.auth_service.controller;

import app_jwt.auth_service.domain.dtos.bus.*;
import app_jwt.auth_service.domain.enums.EstadoBus;
import app_jwt.auth_service.domain.service.BusService;
import app_jwt.auth_service.infra.security.JwtService;
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
@RequestMapping("/api/buses")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('EMPRESA')")
public class BusController {

    private final BusService busService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<BusResponse> createBus(
            @Valid @RequestBody CreateBusRequest request,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        BusResponse response = busService.createBus(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<BusResponse>> getBuses(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        Page<BusResponse> buses = busService.getBusesByEmpresa(empresaId, pageable);
        return ResponseEntity.ok(buses);
    }

    @GetMapping("/{busId}")
    public ResponseEntity<BusResponse> getBusById(
            @PathVariable Long busId,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        BusResponse bus = busService.getBusById(busId, empresaId);
        return ResponseEntity.ok(bus);
    }

    @PutMapping("/{busId}")
    public ResponseEntity<BusResponse> updateBus(
            @PathVariable Long busId,
            @Valid @RequestBody UpdateBusRequest request,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        BusResponse response = busService.updateBus(busId, request, empresaId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{busId}")
    public ResponseEntity<ApiResponse> deleteBus(
            @PathVariable Long busId,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        busService.deleteBus(busId, empresaId);
        return ResponseEntity.ok(new ApiResponse("Bus eliminado exitosamente", true));
    }

    @PatchMapping("/{busId}/estado")
    public ResponseEntity<BusResponse> changeEstadoBus(
            @PathVariable Long busId,
            @RequestParam EstadoBus estado,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        BusResponse response = busService.changeEstadoBus(busId, estado, empresaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<BusResponse>> getBusesByEstado(
            @PathVariable EstadoBus estado,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        List<BusResponse> buses = busService.getBusesByEstado(empresaId, estado);
        return ResponseEntity.ok(buses);
    }

    @GetMapping("/stats")
    public ResponseEntity<BusStatsResponse> getBusStats(Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        BusStatsResponse stats = busService.getBusStats(empresaId);
        return ResponseEntity.ok(stats);
    }

    private Long getEmpresaIdFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return 1L;
    }

    @PutMapping("/{busId}/location")
    public ResponseEntity<BusResponse> updateBusLocation(
            @PathVariable Long busId,
            @Valid @RequestBody UpdateLocationRequest request,
            Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        BusResponse response = busService.updateBusLocation(busId, request, empresaId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ubicaciones")
    public ResponseEntity<List<BusResponse>> getBusesWithLocation(Authentication authentication) {
        Long empresaId = getEmpresaIdFromAuth(authentication);
        List<BusResponse> buses = busService.getBusesWithLocation(empresaId);
        return ResponseEntity.ok(buses);
    }

}
