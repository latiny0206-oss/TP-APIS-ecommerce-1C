package com.trekking.ecommerce.service.impl;

import com.trekking.ecommerce.dto.CarritoRequest;
import com.trekking.ecommerce.dto.CheckoutRequest;
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
import com.trekking.ecommerce.service.EmailService;
import com.trekking.ecommerce.service.UsuarioService;
import com.trekking.ecommerce.service.VarianteProductoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final VarianteProductoService varianteProductoService;
    private final DescuentoRepository descuentoRepository;
    private final OrdenRepository ordenRepository;
    private final ItemOrdenRepository itemOrdenRepository;
    private final UsuarioService usuarioService;
    private final EmailService emailService;

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
        boolean tieneCarritoActivo = carritoRepository
                .findByUsuarioIdAndEstado(request.getUsuarioId(), EstadoCarrito.ACTIVO).isPresent()
                || carritoRepository
                .findByUsuarioIdAndEstado(request.getUsuarioId(), EstadoCarrito.VACIO).isPresent();
        if (tieneCarritoActivo) {
            throw new BusinessRuleException("El usuario ya tiene un carrito activo");
        }
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
        if (carrito.getEstado() == EstadoCarrito.CONVERTIDO || carrito.getEstado() == EstadoCarrito.ABANDONADO) {
            throw new BusinessRuleException("No se pueden agregar items a un carrito en estado " + carrito.getEstado());
        }
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

        int nuevaCantidad = item.getCantidad() + cantidad;
        int stockDisponible = variante.getStock() != null ? variante.getStock() : 0;
        if (stockDisponible < nuevaCantidad) {
            throw new BusinessRuleException("Sin stock disponible para esta combinación");
        }

        item.setCantidad(nuevaCantidad);
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
        VarianteProducto variante = item.getVariante();
        int stockDisponible = variante.getStock() != null ? variante.getStock() : 0;
        if (stockDisponible < cantidad) {
            throw new BusinessRuleException("Sin stock disponible para esta combinación");
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

    private static final BigDecimal SHIPPING_THRESHOLD = new BigDecimal("80000");
    private static final BigDecimal SHIPPING_COST      = new BigDecimal("10000");

    @Override
    @Transactional
    public Orden realizarCompra(Long idCarrito, CheckoutRequest checkoutRequest) {
        Carrito carrito = findById(idCarrito);
        List<ItemCarrito> items = obtenerItems(idCarrito);
        if (items.isEmpty()) {
            throw new BusinessRuleException("No se puede realizar la compra: el carrito id " + idCarrito + " está vacío");
        }

        if (checkoutRequest == null) {
            throw new BusinessRuleException("Los datos de envío son obligatorios");
        }
        if (checkoutRequest.getNombreDestinatario() == null || checkoutRequest.getNombreDestinatario().isBlank()) {
            throw new BusinessRuleException("El nombre del destinatario es obligatorio");
        }
        if (checkoutRequest.getDireccion() == null || checkoutRequest.getDireccion().isBlank()) {
            throw new BusinessRuleException("La dirección de envío es obligatoria");
        }
        if (checkoutRequest.getCiudad() == null || checkoutRequest.getCiudad().isBlank()) {
            throw new BusinessRuleException("La ciudad es obligatoria");
        }
        if (checkoutRequest.getProvincia() == null || checkoutRequest.getProvincia().isBlank()) {
            throw new BusinessRuleException("La provincia es obligatoria");
        }
        if (checkoutRequest.getCodigoPostal() == null || checkoutRequest.getCodigoPostal().isBlank()) {
            throw new BusinessRuleException("El código postal es obligatorio");
        }
        if (checkoutRequest.getTelefono() == null || checkoutRequest.getTelefono().isBlank()) {
            throw new BusinessRuleException("El teléfono es obligatorio");
        }
        if (checkoutRequest.getMetodoPago() == null || checkoutRequest.getMetodoPago().isBlank()) {
            throw new BusinessRuleException("El método de pago es obligatorio");
        }

        // subtotal after discount
        BigDecimal subtotalConDesc = calcularTotal(idCarrito);
        // El umbral de envío gratis se evalúa sobre el subtotal SIN descuento:
        // un cupón no puede quitar el envío gratis ya ganado por monto de compra
        BigDecimal subtotalSinDesc = items.stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal costoEnvio = subtotalSinDesc.compareTo(SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : SHIPPING_COST;
        BigDecimal totalConEnvio = subtotalConDesc.add(costoEnvio).setScale(2, RoundingMode.HALF_UP);

        String metPago = checkoutRequest.getMetodoPago();
        EstadoOrden estadoInicial = metPago.startsWith("TARJETA") ? EstadoOrden.CONFIRMADA : EstadoOrden.PENDIENTE;

        Orden.OrdenBuilder builder = Orden.builder()
                .usuario(carrito.getUsuario())
                .carrito(carrito)
                .descuento(carrito.getDescuento())
                .fechaCreacion(LocalDateTime.now())
                .montoFinal(totalConEnvio)
                .estado(estadoInicial)
                .nombreDestinatario(checkoutRequest.getNombreDestinatario())
                .direccion(checkoutRequest.getDireccion())
                .ciudad(checkoutRequest.getCiudad())
                .provincia(checkoutRequest.getProvincia())
                .codigoPostal(checkoutRequest.getCodigoPostal())
                .telefono(checkoutRequest.getTelefono())
                .metodoPago(metPago);

        Orden ordenGuardada = ordenRepository.save(builder.build());

        List<ItemOrden> itemsOrden = items.stream()
                .map(item -> ItemOrden.builder()
                        .orden(ordenGuardada)
                        .variante(item.getVariante())
                        .cantidad(item.getCantidad())
                        .precioAlMomento(item.getPrecioUnitario())
                        .build())
                .toList();
        itemOrdenRepository.saveAll(itemsOrden);

        for (ItemCarrito item : items) {
            varianteProductoService.descontarStock(item.getVariante().getId(), item.getCantidad());
        }

        carrito.setEstado(EstadoCarrito.CONVERTIDO);
        carrito.setMontoTotal(BigDecimal.ZERO);
        carritoRepository.save(carrito);
        itemCarritoRepository.deleteAll(items);

        // Enviar email de confirmación de compra
        try {
            StringBuilder itemsTableRows = new StringBuilder();
            for (ItemOrden item : itemsOrden) {
                String nombreProducto = item.getVariante().getProducto().getNombre();
                String color = item.getVariante().getColor();
                String talla = item.getVariante().getTalla();
                Integer cantidad = item.getCantidad();
                BigDecimal precioUnitario = item.getPrecioAlMomento();
                BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));

                itemsTableRows.append(String.format(
                    "<tr>" +
                    "  <td style=\"padding: 10px; border-bottom: 1px solid #edf2f7; color: #4a5568;\">%s (Color: %s, Talle: %s)</td>" +
                    "  <td style=\"padding: 10px; border-bottom: 1px solid #edf2f7; text-align: center; color: #4a5568;\">%d</td>" +
                    "  <td style=\"padding: 10px; border-bottom: 1px solid #edf2f7; text-align: right; color: #4a5568;\">$%,.2f</td>" +
                    "  <td style=\"padding: 10px; border-bottom: 1px solid #edf2f7; text-align: right; font-weight: bold; color: #2d3748;\">$%,.2f</td>" +
                    "</tr>",
                    nombreProducto, color, talla, cantidad, precioUnitario.doubleValue(), subtotal.doubleValue()
                ));
            }

            String descuentoInfo = "";
            if (ordenGuardada.getDescuento() != null) {
                descuentoInfo = String.format(
                    "<p style=\"margin: 5px 0; font-size: 14px; color: #e53e3e;\">" +
                    "  <strong>Cupón Aplicado:</strong> %s" +
                    "</p>",
                    ordenGuardada.getDescuento().getCodigo()
                );
            }

            String htmlBody = "<div style=\"font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 650px; margin: 0 auto; padding: 25px; border: 1px solid #e2e8f0; border-radius: 8px; background-color: #ffffff;\">"
                    + "  <div style=\"text-align: center; padding-bottom: 20px; border-bottom: 2px solid #edf2f7;\">"
                    + "    <h1 style=\"color: #2d3748; margin: 0; font-size: 24px;\">¡Gracias por tu compra! 🏔️</h1>"
                    + "    <p style=\"color: #718096; margin: 5px 0 0 0;\">Pedido #" + ordenGuardada.getId() + " - Confirmación de Recepción</p>"
                    + "  </div>"
                    + "  "
                    + "  <div style=\"padding: 20px 0; color: #4a5568; line-height: 1.6;\">"
                    + "    <p style=\"font-size: 16px;\">Hola <strong>" + ordenGuardada.getNombreDestinatario() + "</strong>,</p>"
                    + "    <p style=\"font-size: 16px;\">Queremos confirmarte que hemos recibido tu pedido con éxito y ya estamos trabajando en él. A continuación, encontrarás los detalles de tu compra:</p>"
                    + "    "
                    + "    <div style=\"background-color: #f7fafc; padding: 15px; border-radius: 6px; margin: 20px 0; border: 1px solid #edf2f7;\">"
                    + "      <h3 style=\"margin-top: 0; color: #2d3748; font-size: 16px; border-bottom: 1px solid #e2e8f0; padding-bottom: 5px;\">Resumen del Pedido</h3>"
                    + "      <p style=\"margin: 5px 0;\"><strong>Método de Pago:</strong> " + ordenGuardada.getMetodoPago() + "</p>"
                    + "      <p style=\"margin: 5px 0;\"><strong>Estado del Pedido:</strong> " + ordenGuardada.getEstado() + "</p>"
                    + "      " + descuentoInfo
                    + "    </div>"
                    + "    "
                    + "    <div style=\"background-color: #f7fafc; padding: 15px; border-radius: 6px; margin: 20px 0; border: 1px solid #edf2f7;\">"
                    + "      <h3 style=\"margin-top: 0; color: #2d3748; font-size: 16px; border-bottom: 1px solid #e2e8f0; padding-bottom: 5px;\">Datos de Envío / Entrega</h3>"
                    + "      <p style=\"margin: 5px 0;\"><strong>Destinatario:</strong> " + ordenGuardada.getNombreDestinatario() + "</p>"
                    + "      <p style=\"margin: 5px 0;\"><strong>Dirección:</strong> " + ordenGuardada.getDireccion() + "</p>"
                    + "      <p style=\"margin: 5px 0;\"><strong>Ciudad / Provincia:</strong> " + ordenGuardada.getCiudad() + ", " + ordenGuardada.getProvincia() + " (CP: " + ordenGuardada.getCodigoPostal() + ")</p>"
                    + "      <p style=\"margin: 5px 0;\"><strong>Teléfono:</strong> " + ordenGuardada.getTelefono() + "</p>"
                    + "    </div>"
                    + "    "
                    + "    <h3 style=\"color: #2d3748; font-size: 18px; margin-top: 25px; margin-bottom: 10px;\">Productos</h3>"
                    + "    <table style=\"width: 100%; border-collapse: collapse; margin-bottom: 20px;\">"
                    + "      <thead>"
                    + "        <tr style=\"background-color: #f7fafc;\">"
                    + "          <th style=\"padding: 10px; border-bottom: 2px solid #edf2f7; text-align: left; color: #718096; font-size: 13px;\">Producto</th>"
                    + "          <th style=\"padding: 10px; border-bottom: 2px solid #edf2f7; text-align: center; color: #718096; font-size: 13px;\">Cant.</th>"
                    + "          <th style=\"padding: 10px; border-bottom: 2px solid #edf2f7; text-align: right; color: #718096; font-size: 13px;\">Unitario</th>"
                    + "          <th style=\"padding: 10px; border-bottom: 2px solid #edf2f7; text-align: right; color: #718096; font-size: 13px;\">Total</th>"
                    + "        </tr>"
                    + "      </thead>"
                    + "      <tbody>"
                    + "        " + itemsTableRows.toString()
                    + "      </tbody>"
                    + "    </table>"
                    + "    "
                    + "    <div style=\"text-align: right; margin-top: 20px; padding-top: 15px; border-top: 2px solid #edf2f7;\">"
                    + "      <span style=\"font-size: 18px; color: #4a5568;\">Total Final:</span>"
                    + "      <span style=\"font-size: 24px; font-weight: bold; color: #2b6cb0; margin-left: 10px;\">$" + String.format("%,.2f", ordenGuardada.getMontoFinal().doubleValue()) + "</span>"
                    + "    </div>"
                    + "  </div>"
                    + "  "
                    + "  <div style=\"text-align: center; padding-top: 20px; border-top: 1px solid #edf2f7; font-size: 12px; color: #a0aec0; margin-top: 30px;\">"
                    + "    <p style=\"margin: 0;\">Gracias por elegir a Cumbre. ¡Esperamos verte pronto en la montaña! 🏔️</p>"
                    + "    <p style=\"margin: 5px 0 0 0;\">© 2026 Cumbre E-commerce. Todos los derechos reservados.</p>"
                    + "  </div>"
                    + "</div>";

            emailService.sendEmail(ordenGuardada.getUsuario().getEmail(), "Confirmación de Compra - Pedido #" + ordenGuardada.getId() + " 🛒", htmlBody);
        } catch (Exception e) {
            log.error("Error enviando email de confirmación de compra: {}", e.getMessage());
        }

        return ordenGuardada;
    }

    @Override
    @Transactional
    public Carrito aplicarDescuentoPorCodigo(Long idCarrito, String codigo) {
        Carrito carrito = findById(idCarrito);
        if (carrito.getEstado() == EstadoCarrito.CONVERTIDO || carrito.getEstado() == EstadoCarrito.ABANDONADO) {
            throw new BusinessRuleException("No se puede modificar un carrito en estado " + carrito.getEstado());
        }
        Descuento descuento = descuentoRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un cupón con código: " + codigo));
        if (!estaVigente(descuento)) {
            throw new BusinessRuleException("Este cupón está desactivado o expirado");
        }
        carrito.setDescuento(descuento);
        carrito.setMontoTotal(calcularTotal(idCarrito));
        return carritoRepository.save(carrito);
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
