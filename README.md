# Tarea 2 - Ingeniería de Software

API en Spring Boot que lee las compras desde un archivo CSV y las convierte a pesos
chilenos usando los indicadores de la API pública **mindicador.cl**.

## Integrantes

| Integrante | Usuario de GitHub | Desafíos realizados |
|---|---|---|
| _Nombre 1_ | `@usuario1` | Desafío 1 y 3 |
| _Nombre 2_ | `@usuario2` | Desafío 2 y 4 |

> Completar con los datos reales: el enunciado exige indicar el nombre, el usuario de
> GitHub de cada integrante y **qué desafío realizó cada uno**.

## Endpoints

### Desafío 2 — Detalle de una compra

```
GET /compras/detalle/{id}
```

Retorna la compra con ese id, convertida a pesos según su unidad.

```sh
curl http://localhost:3001/compras/detalle/1
```

```json
{
  "id": 1,
  "producto": "Notebook",
  "fecha": "2026-09-01",
  "monto": 25.5,
  "unidad": "UF",
  "valor_unidad": 41000.5,
  "monto_pesos": 1045513
}
```

Respuestas de error:

| Caso | Código |
|---|---|
| El id no existe | `404 Not Found` |
| La unidad no es UF ni USD | `400 Bad Request` |
| La API no entrega valor para esa fecha | `502 Bad Gateway` |
| El CSV no se puede leer o es inválido | `500 Internal Server Error` |

### Desafío 1 — Compras por unidad

```
GET /compras/{unidad}      # unidad: uf | dolar
```

## Cómo se obtiene el valor de la UF y del dólar

El enunciado exige el valor de la UF o del dólar **en la fecha de cada compra**, por lo
que se usa la consulta por fecha de la API y no la de resumen:

```
GET https://mindicador.cl/api/{indicador}/{dd-MM-yyyy}
```

- `uf` para las compras en UF.
- `dolar` para las compras en USD.

La respuesta trae `serie: [{fecha, valor}]` y se toma el primer elemento, validando que
su fecha coincida con la fecha solicitada y que el valor sea positivo.

La API pública responde lento o de forma intermitente, por lo que el cliente usa
timeouts (10 s de conexión, 20 s de lectura) y hasta 3 intentos con espera creciente
antes de responder `502`.

## Archivo de compras

`compras.csv` está en la raíz del repositorio y su ruta se configura con la variable de
entorno `CSV_PATH` (por defecto `compras.csv`).

```csv
id,producto,monto,unidad,fecha
1,Notebook,25.5,UF,2026-09-01
```

El archivo se lee en cada petición, por lo que puede cambiarse sin reiniciar la
aplicación ni reconstruir la imagen.

## Ejecutar en local

```sh
./mvnw spring-boot:run
```

La aplicación queda en `http://localhost:3001`.

## Pruebas

```sh
./mvnw test
```

## Docker (Desafío 3)

```sh
docker build -t tarea2 .
docker run --rm -p 3001:3001 -v "$PWD/compras.csv:/app/data/compras.csv:ro" tarea2
```

O con Docker Compose (construye la imagen y levanta todo con un solo comando):

```sh
docker compose up
```

La imagen se construye solo con `docker build` (no hay que compilar antes: el `mvnw`
compila dentro de la imagen), no ejecuta como root (`uid=100(app)`), expone el puerto
`3001` (`APP_PORT`) y define `APP_ENV=production`. El CSV **no** queda dentro de la
imagen: se monta como volumen en `/app/data/compras.csv`.

El JAR se ejecuta sobre un runtime de Java recortado con `jlink` (solo los módulos que la
aplicación usa en vez del JRE completo), con lo que la imagen final pesa **190 MB**,
bajo el límite de 250 MB del enunciado.

## Pipeline (Desafío 4)

`.github/workflows/tarea2.yml` define tres jobs que se ejecutan en orden; si uno falla,
los siguientes no se ejecutan:

1. **test** — ejecuta `mvn test`.
2. **build** — construye la imagen Docker (sin compilar antes).
3. **publish** — solo en push a `main`. Publica en GitHub Container Registry
   (`ghcr.io/<owner>/<repo>`) de forma pública, con los tags `latest` y el SHA corto
   (7 caracteres).

La imagen se publica usando el `GITHUB_TOKEN` que GitHub entrega al workflow, por lo que
no hay credenciales en texto plano en el repositorio.

## Ramas

`main`, `develop`, `feature/compras-unidad`, `feature/compra-detalle`, `docker` y
`pipeline`. Al terminar los desafíos 1 y 2, ambas ramas se integran en `develop`; en los
desafíos 3 y 4, `docker` se integra en `develop` antes que `pipeline`. Al final, `develop`
se integra en `main`.
