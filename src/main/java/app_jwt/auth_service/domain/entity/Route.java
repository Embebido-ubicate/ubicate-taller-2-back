package app_jwt.auth_service.domain.entity;

import app_jwt.auth_service.domain.enums.EstadoRuta;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rutas", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"codigo", "empresa_id"})
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(length = 80)
    private String origen;

    @Column(length = 80)
    private String destino;

    @Column(length = 9)
    private String colorHex;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String polyline;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoRuta estado = EstadoRuta.ACTIVA;

    @Builder.Default
    private Boolean activo = true;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "fecha_creacion")
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToMany
    @JoinTable(
            name = "ruta_buses",
            joinColumns = @JoinColumn(name = "ruta_id"),
            inverseJoinColumns = @JoinColumn(name = "bus_id")
    )
    @Builder.Default
    private Set<Bus> buses = new HashSet<>();

    @PreUpdate
    private void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
