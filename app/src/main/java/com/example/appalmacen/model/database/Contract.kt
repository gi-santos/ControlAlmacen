package com.example.appalmacen.model.database

object Contract {

    // Nombres de tablas
    const val TABLE_USUARIOS = "usuarios"
    const val TABLE_PRODUCTOS = "productos"
    const val TABLE_ALBARANES = "albaranes"
    const val TABLE_PERFILES = "perfiles"
    const val TABLE_PROVEEDORES = "proveedores"

    // Columnas Usuario
    object UsuarioColumns {
        const val ID = "id"
        const val NOMBRE = "nombre"
        const val FOTO = "foto"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val ES_ADMIN = "es_admin"
        const val HABILITADO = "habilitado"
    }

    // Columnas Producto
    object ProductoColumns {
        const val ID = "id"
        const val NOMBRE = "nombre"
        const val IMAGEN = "imagen"
        const val CANTIDAD = "cantidad"
        const val CANTIDAD_MINIMA = "cantidad_minima"
        const val HABILITADO = "habilitado"
        const val FECHA_ULTIMA_INTERACCION = "fecha_ultima_interaccion"
    }

    // Columnas Albaran
    object AlbaranColumns {
        const val ID = "id"
        const val CIF = "cif"
        const val NOMBRE_PROVEEDOR = "nombre_proveedor"
        const val IMPORTE = "importe"
        const val PAGADO = "pagado"
        const val FECHA_PAGO = "fecha_pago"
        const val FECHA = "fecha"
        const val IMAGEN_PATH = "imagen_path"
    }

    // Columnas Perfil
    object PerfilColumns {
        const val ID = "id"
        const val NOMBRE = "nombre"
        const val DESCRIPCION = "descripcion"
    }

    // Columnas Proveedor
    object ProveedorColumns {
        const val ID = "id"
        const val NOMBRE = "nombre"
        const val CIF = "cif"
        const val TELEFONO = "telefono"
        const val EMAIL = "email"
    }
}