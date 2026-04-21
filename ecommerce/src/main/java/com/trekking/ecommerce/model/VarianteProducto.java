package com.trekking.ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trekking.ecommerce.model.enums.Estacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"itemsCarrito", "itemsOrden", "fotos"})
@EqualsAndHashCode(exclude = {"itemsCarrito", "itemsOrden", "fotos"})
@Entity
@Table(name = "variante_producto",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_variante_producto_color_talla",
                columnNames = {"id_producto", "color", "talla"}
        ))
public class VarianteProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_variante")
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String color;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String talla;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String material;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal peso;

    @NotNull
    @Column(nullable = false)
    private Integer stock;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estacion estacion;

    @JsonIgnore
    @OneToMany(mappedBy = "variante")
    private List<ItemCarrito> itemsCarrito;

    @JsonIgnore
    @OneToMany(mappedBy = "variante")
    private List<ItemOrden> itemsOrden;

    @JsonIgnore
    @OneToMany(mappedBy = "variante")
    private List<Foto> fotos;
}

