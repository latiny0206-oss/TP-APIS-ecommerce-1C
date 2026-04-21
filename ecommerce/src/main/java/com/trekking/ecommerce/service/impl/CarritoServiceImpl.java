package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.CarritoRequest;
import com.trekking.ecommerce.exception.BusinessRuleException;
import com.trekking.ecommerce.exception.ResourceNotFoundException;
import com.trekking.ecommerce.model.Carrito;
import com.trekking.ecommerce.model.Descuento;
import com.trekking.ecommerce.model.ItemCarrito;
import com.trekking.ecommerce.model.ItemOrden;
import com.trekking.ecommerce.model.Orden;
import com.trekking.ecommerce.model.VarianteProducto;
import com.trekking.ecommerce.model.enums.EstadoCarrito;
import com.trekking.ecommerce.model.enums.EstadoDescuento;
import com.trekking.ecommerce.model.enums.EstadoOrden;
import com.trekking.ecommerce.model.enums.EstadoProducto;
import com.trekking.ecommerce.model.enums.TipoDescuento;
import com.trekking.ecommerce.repository.CarritoRepository;
import com.trekking.ecommerce.repository.DescuentoRepository;
import com.trekking.ecommerce.repository.ItemCarritoRepository;
import com.trekking.ecommerce.repository.ItemOrdenRepository;
import com.trekking.ecommerce.repository.OrdenRepository;
import com.trekking.ecommerce.service.CarritoService;
import com.trekking.ecommerce.service.UsuarioService;
import com.trekking.ecommerce.service.VarianteProductoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final VarianteProductoService varianteProductoService;
    private final DescuentoRepository descuentoRepository;
    private final OrdenRepository ordenRepository;
    private final ItemOrdenRepository itemOrdenRepository;
    private final UsuarioService usuarioService;

    @Override
    @Transactional(readOnly = true)
    public List<Carrito> findAll() {
        return carritoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Carrito> findAllConItems() {
        return carritoRepository.findAllConItems();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Carrito> findByUsuario(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Carrito> findByUsuarioConItems(Long usuarioId) {
        return carritoRepository.findByUsuarioIdConItems(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Carrito findById(Long id) {
        return carritoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carrito", id));
    }

    @Override
    @Transactional
    public Carrito create(CarritoRequest request) {
        Carrito carrito = Carrito.builder()
                .usuario(usuarioService.findEntityById(request.getUsuarioId()))
                .descuento(resolverDescuento(request.getDescuentoId()))
                .estado(EstadoCarrito.VACIO)
                .montoTotal(BigDecimal.ZERO)
                .build();
        return carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public Carrito update(Long id, CarritoRequest request) {
        Carrito actual = findById(id);
        actual.setUsuario(usuarioService.findEntityById(request.getUsuarioId()));
        actual.setDescuento(resolverDescuento(request.getDescuentoId()));
        actual.setMontoTotal(calcularTotal(id));
        return carritoRepository.save(actual);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        findById(id);
        carritoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ItemCarrito agregarItem(Long idCarrito, Long idVariante, Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new BusinessRuleException("La cantidad debe ser al menos 1");
        }
        Carrito carrito = findById(idCarrito);
        VarianteProducto variante = varianteProductoService.findById(idVariante);

        if (variante.getProducto().getEstado() != EstadoProducto.ACTIVO) {
            throw new BusinessRuleException("El producto '" + variante.getProducto().getNombre() + "' no está disponible");
        }

        ItemCarrito item = itemCarritoRepository.findByCarritoIdAndVarianteId(idCarrito, idVariante)
                .orElse(ItemCarrito.builder()
                        .carrito(carrito)
                        .variante(variante)
                        .cantidad(0)
                        .precioUnitario(variante.getPrecio())
                        .build());

        item.setCantidad(item.getCantidad() + cantidad);
        item.setPrecioUnitario(variante.getPrecio());
        ItemCarrito saved = itemCarritoRepository.save(item);

        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setMontoTotal(calcularTotal(idCarrito));
        carritoRepository.save(carrito);
        return saved;
    }

    @Override
    @Transactional
    public void eliminarItem(Long idCarrito, Long idItemCarrito) {
        findById(idCarrito);
        ItemCarrito item = itemCarritoRepository.findById(idItemCarrito)
                .orElseThrow(() -> new ResourceNotFoundException("ItemCarrito", idItemCarrito));
        if (!item.getCarrito().getId().equals(idCarrito)) {
            throw new BusinessRuleException("El item id " + idItemCarrito + " no pertenece al carrito id " + idCarrito);
        }
        itemCarritoRepository.delete(item);
        Carrito carrito = findById(idCarrito);
        carrito.setMontoTotal(calcularTotal(idCarrito));
        carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public ItemCarrito actualizarItem(Long idCarrito, Long idItemCarrito, Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new BusinessRuleException("La cantidad debe ser al menos 1");
        }
        findById(idCarrito);
        ItemCarrito item = itemCarritoRepository.findById(idItemCarrito)
                .orElseThrow(() -> new ResourceNotFoundException("ItemCarrito", idItemCarrito));
        if (!item.getCarrito().getId().equals(idCarrito)) {
            throw new BusinessRuleException("El item id " + idItemCarrito + " no pertenece al carrito id " + idCarrito);
        }
        item.setCantidad(cantidad);
        ItemCarrito saved = itemCarritoRepository.save(item);
        Carrito carrito = findById(idCarrito);
        carrito.setMontoTotal(calcularTotal(idCarrito));
        carritoRepository.save(carrito);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotal(Long idCarrito) {
        Carrito carrito = findById(idCarrito);
        List<ItemCarrito> items = itemCarritoRepository.findByCarritoId(idCarrito);
        BigDecimal subtotal = items.stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Descuento descuento = carrito.getDescuento();
        if (descuento == null || !estaVigente(descuento)) {
            return subtotal.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal descuentoAplicado;
        if (descuento.getTipo() == TipoDescuento.FIJO) {
            descuentoAplicado = descuento.getValor().min(subtotal);
        } else {
            descuentoAplicado = subtotal.multiply(descuento.getValor()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        return subtotal.subtract(descuentoAplicado).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void vaciarCarrito(Long idCarrito) {
        Carrito carrito = findById(idCarrito);
        itemCarritoRepository.deleteAll(obtenerItems(idCarrito));
        carrito.setMontoTotal(BigDecimal.ZERO);
        carrito.setEstado(EstadoCarrito.VACIO);
        carritoRepository.save(carrito);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemCarrito> obtenerItems(Long idCarrito) {
        findById(idCarrito);
        return itemCarritoRepository.findByCarritoId(idCarrito);
    }

    @Override
    @Transactional
    public int vaciarCarritosAbandonados(int diasInactividad) {
        LocalDateTime limite = LocalDateTime.now().minusDays(diasInactividad);
        List<Carrito> inactivos = carritoRepository.findByEstadoAndFechaUltimaModificacionBefore(EstadoCarrito.ACTIVO, limite);
        if (inactivos.isEmpty()) {
            return 0;
        }
        List<ItemCarrito> todosLosItems = inactivos.stream()
                .flatMap(c -> itemCarritoRepository.findByCarritoId(c.getId()).stream())
                .toList();
        itemCarritoRepository.deleteAllInBatch(todosLosItems);
        inactivos.forEach(c -> {
            c.setEstado(EstadoCarrito.ABANDONADO);
            c.setMontoTotal(BigDecimal.ZERO);
        });
        carritoRepository.saveAll(inactivos);
        return inactivos.size();
    }

    /**
     * Realiza el checkout del carrito:
     * 1. Valida que no esté vacío.
     * 2. Descuenta el stock de cada variante (bug fix).
     * 3. Crea la Orden con snapshot de precios.
     * 4. Marca el carrito como CONVERTIDO.
     */
    @Override
    @Transactional
    public Orden realizarCompra(Long idCarrito) {
        Carrito carrito = findById(idCarrito);
        List<ItemCarrito> items = obtenerItems(idCarrito);
        if (items.isEmpty()) {
            throw new BusinessRuleException("No se puede realizar la compra: el carrito id " + idCarrito + " está vacío");
        }

        BigDecimal total = calcularTotal(idCarrito);

        Orden orden = Orden.builder()
                .usuario(carrito.getUsuario())
                .carrito(carrito)
                .descuento(carrito.getDescuento())
                .fechaCreacion(LocalDateTime.now())
                .montoFinal(total)
                .estado(EstadoOrden.PENDIENTE)
                .build();
        Orden ordenGuardada = ordenRepository.save(orden);

        List<ItemOrden> itemsOrden = items.stream()
                .map(item -> ItemOrden.builder()
                        .orden(ordenGuardada)
                        .variante(item.getVariante())
                        .cantidad(item.getCantidad())
                        .precioAlMomento(item.getPrecioUnitario())
                        .build())
                .toList();
        itemOrdenRepository.saveAll(itemsOrden);

        // Descontar stock por cada ítem (bug fix)
        for (ItemCarrito item : items) {
            varianteProductoService.descontarStock(item.getVariante().getId(), item.getCantidad());
        }

        carrito.setEstado(EstadoCarrito.CONVERTIDO);
        carrito.setMontoTotal(BigDecimal.ZERO);
        carritoRepository.save(carrito);
        itemCarritoRepository.deleteAll(items);

        return ordenGuardada;
    }

    private Descuento resolverDescuento(Long descuentoId) {
        if (descuentoId == null) {
            return null;
        }
        return descuentoRepository.findById(descuentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Descuento", descuentoId));
    }

    private boolean estaVigente(Descuento descuento) {
        LocalDate hoy = LocalDate.now();
        return descuento.getEstado() == EstadoDescuento.ACTIVO
                && !hoy.isBefore(descuento.getFechaInicio())
                && !hoy.isAfter(descuento.getFechaFin());
    }
}
