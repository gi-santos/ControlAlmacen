# ControlAlmacen
App Móvil creada con la finalidad de facilitar el control de entradas y salidas de productos de almacenes de pequeñas empresas. Proyecto creado por Giselle Santos y Álvaro Prado.

## Estructura de la Base de Datos (Room)

A continuación se detalla el diseño de la base de datos relacional utilizada en el proyecto:

```mermaid
erDiagram
    USUARIO {
        int id PK
        int perfilId FK
        string nombre
        string email
        string password
        string foto
        boolean esAdmin
        boolean habilitado
    }
    PRODUCTO {
        int id PK
        string nombre
        string imagen
        int cantidad
        int cantidadMinima
        boolean habilitado
        long fechaUltimaInteraccion
    }
    ALBARAN {
        int id PK
        string cif
        string nombreProveedor
        double importe
        boolean pagado
        long fechaPago
        long fecha
        string imagenPath
    }
    PERFIL {
        int id PK
        string nombre
        string descripcion
    }
    PROVEEDOR {
        int id PK
        string nombre
        string cif
        string telefono
        string email
    }

    USUARIO }o--|| PERFIL : "tiene un rol"
    ALBARAN }o--|| PROVEEDOR : "emitido por"
```

### Entidades Principales
- **Usuarios**: Gestión de acceso y roles (Admin/User).
- **Productos**: Control de inventario y stock mínimo.
- **Albaranes**: Registro de facturas y pagos a proveedores.
- **Proveedores**: Información de contacto de suministradores.
- **Perfiles**: Definición de roles del sistema.
