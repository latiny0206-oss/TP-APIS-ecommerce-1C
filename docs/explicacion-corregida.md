# Diagrama de arquitectura

El proyecto se estructuró siguiendo un modelo de arquitectura por capas. El objetivo principal fue garantizar la separación de responsabilidades, organizando el código en el paquete raíz `com.trekking.ecommerce` de la siguiente manera:

* **Controladores y DTOs:** La capa de entrada (`controller/`) gestiona los recursos REST y delega el trabajo pesado a los servicios. Implementamos `dto/` para las respuestas y peticiones, lo que nos permite proteger las entidades JPA y no exponer la estructura interna de la base de datos hacia afuera.

* **Lógica de Negocio:** Centralizada en `service/` (con sus implementaciones en `impl/`). Aquí es donde manejamos las reglas de validación, la orquestación de datos y la gestión de transacciones con la anotación `@Transactional`.

* **Acceso a Datos y Modelos:** Usamos `repository/` con Spring Data JPA para las consultas y `model/` para las entidades del dominio (incluyendo enums como `EstadoCarrito` o `RolUsuario`).

* **Componentes Transversales:** Se crearon paquetes específicos para la seguridad (`security/` y `config/`), el manejo global de errores (`exception/` con un `@RestControllerAdvice`) y tareas automáticas (`job/`) para limpiar carritos o vencer cupones de descuento.

## 1.1 Configuración de Seguridad (Security Filter Chain)

La seguridad está centralizada en `SecurityBeansConfig.java`. Definimos una política sin estado basada en tokens JWT, lo que significa que el servidor no guarda sesiones; cada request se valida de forma independiente. El flujo de autenticación funciona así:

* **Filtro JWT:** Creamos un `JwtAuthenticationFilter` que intercepta cada pedido antes que el filtro por defecto de Spring. Extrae el token del header `Authorization`, valida la firma con la clase `JwtUtil` y carga al usuario en el contexto de seguridad si todo es correcto.

* **Autorización:** El acceso está segmentado. Los endpoints de autenticación, Swagger y el catálogo público (productos, categorías, marcas, variantes y fotos) son `permitAll()`. Las operaciones de escritura y gestión de usuarios quedan reservadas para `ROLE_ADMIN`, mientras que el proceso de compra requiere estar logueado.

* **Protección Extra:** Además de la configuración por URL, activamos `@EnableMethodSecurity` para poner restricciones más finas directamente en los métodos de los controladores mediante `@PreAuthorize`.

## 1.2 Estrategia de Persistencia

Para el manejo de datos elegimos Hibernate (incluido por Spring Boot 3.5.12 como dependencia transitiva en su versión 6.x) a través de Spring Data JPA. La configuración es flexible según el entorno de ejecución:

* **Entornos:** Usamos `application-local.properties` con base H2 para desarrollo rápido y `application-mysql.properties` para la base de datos definitiva en MySQL 8.

* **Mapeo:** Las entidades utilizan Lombok para reducir el código repetitivo (boilerplate). Definimos relaciones `@OneToMany` y `@ManyToOne`, usando `@JsonIgnore` estratégicamente para evitar los bucles infinitos cuando se envían datos al frontend.

* **Consultas:** La mayoría de la interacción con la BD se resuelve con las consultas derivadas de los repositorios, aunque para optimizar la carga de relaciones y evitar el problema N+1 implementamos `@Query` con `LEFT JOIN FETCH` en `CarritoRepository` y `OrdenRepository`.

---

## Changelog

| Sección | Qué decía antes | Qué dice ahora | Motivo |
|---------|----------------|----------------|--------|
| 1.1 — bullet Autorización | "el catálogo público (productos, marcas, fotos) son `permitAll()`" | "el catálogo público (productos, categorías, marcas, variantes y fotos) son `permitAll()`" | `GET /api/categorias/**` y `GET /api/variantes/**` también están marcados como `permitAll()` en `SecurityBeansConfig.java` líneas 84–87. |
| 1.2 — párrafo de apertura | "elegimos Hibernate 6 a través de Spring Data JPA" | "elegimos Hibernate (incluido por Spring Boot 3.5.12 como dependencia transitiva en su versión 6.x) a través de Spring Data JPA" | Hibernate no está declarado como dependencia directa en `pom.xml`; su versión es gestionada por Spring Boot. Presentarlo como elección directa era impreciso. |
| 1.2 — bullet Entornos | "Usamos `application-local.yml` ... y `application-mysql.yml`" | "Usamos `application-local.properties` ... y `application-mysql.properties`" | Los archivos reales tienen extensión `.properties`. No existe ningún archivo `.yml` en `src/main/resources/`. |
| 1.2 — bullet Consultas | "para filtros complejos de búsqueda (por categoría o estado) implementamos `@Query` personalizadas" | "para optimizar la carga de relaciones y evitar el problema N+1 implementamos `@Query` con `LEFT JOIN FETCH` en `CarritoRepository` y `OrdenRepository`" | Las `@Query` del proyecto están en `CarritoRepository` y `OrdenRepository` y su propósito es hacer `LEFT JOIN FETCH`. Los filtros por categoría y estado en `ProductoRepository` usan query methods derivados (`findByCategoriaId`, `findByEstado`), sin ninguna `@Query`. |
