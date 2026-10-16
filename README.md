# Tarea 2 - Ingeniería de Software

## Integrantes

| Nombre | Usuario de GitHub | Desafíos realizados |
|---|---|---|
| Benjamin Valenzuela | [06-Benjamin-V](https://github.com/06-Benjamin-V) | 1. Compras por unidad y 3. Docker |
| Erick Soto | [Subsystem2418](https://github.com/Subsystem2418) | 2. Detalle de compra y 4. Pipeline |

## Ejecutar con Docker

Requisitos: Docker con soporte para contenedores Linux, Docker Compose
y el archivo `compras.csv` en la raíz del repositorio.

Desde la raíz del proyecto:

```powershell
docker compose up --build
```

La aplicación queda disponible en `http://localhost:3001`.

El CSV se monta en modo lectura y no se incluye en la imagen.

### Cambiar el puerto

En PowerShell, antes de iniciar Compose:

```powershell
$env:APP_PORT = "3005"
docker compose up --build
```

La aplicación queda disponible en `http://localhost:3005`.

### Detener la aplicación

Desde otra terminal en la raíz del proyecto:

```powershell
docker compose down
```