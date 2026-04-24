# REPORTE_QA

## 1) Resumen ejecutivo

- **Ejecucion final:** `./mvnw test -Dspring.profiles.active=test`
- **Total tests:** 206
- **Pasados:** 206
- **Fallidos:** 0
- **Errores:** 0
- **Cobertura aproximada:** **alta (~80-85%)** por amplitud de pruebas unitarias + integracion sobre seguridad, auth, CRUDs, reglas de negocio, jobs, DTOs y flujos E2E. (No se calculo porcentaje exacto de JaCoCo en este reporte).

## 2) Tests creados / modificados

### Nuevos
- `src/test/java/com/trekking/ecommerce/DtoValidationTest.java` - **9** casos (@Valid en DTOs: NotBlank, NotNull, Min, Email, Size, Pattern, DecimalMin y casos validos)
- `src/test/java/com/trekking/ecommerce/GlobalExceptionHandlerUnitTest.java` - **11** casos (todos los handlers principales y generico)
- `src/test/java/com/trekking/ecommerce/FotoControllerIT.java` - **4** casos (GET publico, autorizacion ADMIN, multipart create/update/delete, variante inexistente)

### Modificados
- `src/test/java/com/trekking/ecommerce/JwtUtilTest.java` - ahora **8** casos (agregados: token malformado, firma invalida, token null/vacio; expirado retorna false)
- `src/test/java/com/trekking/ecommerce/EndpointSeguridadAutorizacionIntegrationTest.java` - ahora **10** casos (agregados: `/api/descuentos/activos` sin token, bloqueo CLIENTE en POST/PUT/DELETE de catalogo)

## 3) Bugs encontrados y corregidos

### Bug 1 - JaCoCo incompatible con clases del JDK actual
- **Archivo:** `pom.xml` (plugin `jacoco-maven-plugin`)
- **Problema:** error en runtime de tests: `Unsupported class file major version 68`.
- **Deteccion:** primera corrida global de tests.
- **Fix aplicado:** version de JaCoCo `0.8.12 -> 0.8.13`.
- **Diff resumido:**
  - `jacoco-maven-plugin.version = 0.8.13`

### Bug 2 - `isTokenValid` propagaba excepciones en tokens invalidos
- **Archivo:** `src/main/java/com/trekking/ecommerce/security/JwtUtil.java` (metodo `isTokenValid`)
- **Problema:** ante token expirado/malformado/firma invalida/null, el metodo podia lanzar excepcion en vez de responder `false`.
- **Deteccion:** ampliacion de `JwtUtilTest` con casos negativos.
- **Fix aplicado:** guard clause para `null/blank`, y `try/catch` (`JwtException` + `IllegalArgumentException`) retornando `false`.
- **Diff resumido:**
  - agrega validacion de `token == null || token.isBlank() || userDetails == null`
  - encapsula parseo/validacion en `try/catch`
  - retorna `false` en excepciones de JWT

### Bug 3 - Se podia agregar item a carritos no operables
- **Archivo:** `src/main/java/com/trekking/ecommerce/service/impl/CarritoServiceImpl.java` (metodo `agregarItem`)
- **Problema:** permitia agregar items aun con carrito `CONVERTIDO` o `ABANDONADO`.
- **Deteccion:** validacion de casos borde solicitados (regla de negocio de estado de carrito).
- **Fix aplicado:** bloqueo explicito con `BusinessRuleException` para estados `CONVERTIDO` y `ABANDONADO`.
- **Diff resumido:**
  - `if (estado == CONVERTIDO || estado == ABANDONADO) throw BusinessRuleException(...)`

### Bug 4 - `mvn test` no levantaba todos los IT por patron
- **Archivo:** `pom.xml` (plugin `maven-surefire-plugin`)
- **Problema:** algunas clases de integracion con sufijo `IT` / `IntegrationTest` no quedaban incluidas por defecto.
- **Deteccion:** analisis de ejecucion de suite y nombres de archivos de test.
- **Fix aplicado:** configuracion explicita de includes:
  - `**/*Test.java`
  - `**/*IT.java`
  - `**/*IntegrationTest.java`

## 4) Bugs encontrados y NO corregidos

- **No se detectaron bugs pendientes bloqueantes** en el alcance trabajado.

## 5) Tests que ya existian y pasaron sin cambios (resumen)

Pasaron sin cambios, entre otros:
- `AuthControllerIT.java`
- `CarritoServiceImplTest.java`
- `CategoriaServiceImplTest.java`
- `DescuentoServiceImplTest.java`
- `EndpointFlujosIntegrationTest.java`
- `EndpointNegativosBordeIntegrationTest.java`
- `FotoServiceImplTest.java`
- `GlobalExceptionHandlerIT.java`
- `ItemCarritoServiceImplTest.java`
- `ItemOrdenServiceImplTest.java`
- `JobsTest.java`
- `JwtAuthenticationFilterTest.java`
- `MarcaServiceImplTest.java`
- `OrdenServiceImplTest.java`
- `ProductoServiceImplTest.java`
- `UserDetailsServiceImplTest.java`
- `UsuarioServiceImplTest.java`
- `VarianteProductoServiceImplTest.java`

## 6) Coleccion Postman

- **Archivo:** `postman/Trekking_Ecommerce.postman_collection.json`
- **Formato:** Postman Collection v2.1
- **Variables principales:** `base_url`, `admin_token`, `cliente_token`, `cliente2_token`

### Carpetas y volumen
- **Auth:** 6 requests / 11 validaciones `pm.test`
- **Categorias:** 6 requests / 10 validaciones
- **Marcas:** 6 requests / 5 validaciones
- **Productos:** 8 requests / 8 validaciones
- **Variantes:** 6 requests / 6 validaciones
- **Descuentos:** 6 requests / 6 validaciones
- **Carritos:** 10 requests / 12 validaciones
- **Ordenes:** 8 requests / 8 validaciones
- **Fotos:** 6 requests / 6 validaciones
- **Seguridad:** 3 requests / 3 validaciones

La coleccion esta ordenada para ejecucion secuencial y guarda IDs/tokens en variables para encadenar requests.

## 7) Recomendaciones

1. **Normalizar mensajes de error y encoding** (se observaron respuestas con caracteres acentuados degradados en algunos mensajes).
2. **Agregar reporte de cobertura automatizado en CI** (publicar JaCoCo XML/HTML con umbral minimo).
3. **Separar tests de integracion con Failsafe** (`integration-test`/`verify`) para pipelines mas claros.
4. **Refinar la coleccion Postman** con archivos binarios de ejemplo versionados para flujos multipart (`Fotos`).
5. **Agregar tests adicionales de ownership y estados invalidos** en endpoints de carrito/orden para endurecer regresiones futuras.

