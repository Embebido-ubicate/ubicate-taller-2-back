package app_jwt.auth_service.domain.entity;

import app_jwt.auth_service.domain.enums.CategoriaLicencia;
import app_jwt.auth_service.domain.enums.EstadoConductor;
import app_jwt.auth_service.domain.enums.TurnoConductor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conductores", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"numero_licencia"}),
        @UniqueConstraint(columnNames = {"usuario_id"})
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Conductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "numero_licencia", unique = true, nullable = false, length = 20)
    private String numeroLicencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_licencia", nullable = false)
    private CategoriaLicencia categoriaLicencia;

    @Column(name = "fecha_vencimiento_licencia", nullable = false)
    private LocalDate fechaVencimientoLicencia;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TurnoConductor turno = TurnoConductor.MAÑANA;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoConductor estado = EstadoConductor.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_asignado_id")
    private Bus busAsignado;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Campos adicionales para el dashboard
    @Column(name = "experiencia_años")
    private Integer experienciaAnios;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @PreUpdate
    private void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Helper method para verificar si la licencia está vencida
    public boolean isLicenciaVencida() {
        return fechaVencimientoLicencia.isBefore(LocalDate.now());
    }

    // Helper method para verificar si la licencia vence pronto (30 días)
    public boolean isLicenciaPorVencer() {
        return fechaVencimientoLicencia.isBefore(LocalDate.now().plusDays(30));
    }
}