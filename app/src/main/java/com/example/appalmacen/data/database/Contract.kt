package com.example.appalmacen.data.database

object Contract {

    // Nombres de tablas
    const val TABLE_USUARIOS = "usuarios"
    const val TABLE_PRODUCTOS = "productos"
    const val TABLE_ALBARANES = "albaranes"
    const val TABLE_PERFILES = "perfiles"

    const val TABLE_INTERACCIONES = "usuario_producto_interaccion"

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
        const val IMAGEN_PATH = "imagen_path"

        const val FECHA_GUARDADO = "fecha_guardado"
    }

    // Columnas Perfil
    object PerfilColumns {
        const val ID = "id"
        const val NOMBRE = "nombre"
        const val DESCRIPCION = "descripcion"
    }

    object InteraccionColumns {
        const val ID = "id"
        const val USUARIO_ID = "usuario_id"
        const val PRODUCTO_ID = "producto_id"
        const val CANTIDAD_ANTERIOR = "cantidad_anterior"
        const val CANTIDAD_NUEVA = "cantidad_nueva"
        const val TIPO_ACCION = "tipo_accion"
        const val TIMESTAMP = "timestamp"
    }
}