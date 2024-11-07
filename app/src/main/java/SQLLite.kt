import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class SQLLite(ctx: Context?) : SQLiteOpenHelper(ctx, "diario.db", null, 1) {
    private val diario =
        "CREATE TABLE diario (id INTEGER PRIMARY KEY AUTOINCREMENT, fecha DATE, peso FLOAT)"
    private lateinit var c: Cursor
    private var database: SQLiteDatabase? = null
    private var cantidad = 0

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(diario)
    }

    override fun onUpgrade(db: SQLiteDatabase, versionAnterior: Int, versionNueva: Int) {
        db.execSQL("DROP TABLE IF EXISTS diario")

        db.execSQL(diario)
    }

    fun inicia(db: SQLiteDatabase?) {
        if (db != null) database = db
    }

    fun Count(): Int {
        return cantidad
    }

    fun insertaDiario(fecha: String, peso: Float) {
        try {
            database!!.execSQL("INSERT INTO diario (fecha, peso) VALUES ('$fecha', $peso)")
        } catch (e: Exception) {
            Log.e("ERROR", "inserta diario: $e")
        }
    }

    fun Select(busca: String): Array<Array<String?>> {
        var busca = busca
        var datos: Array<Array<String?>>

        busca = if (busca != "") " WHERE $busca"
        else ""
        val sql = "SELECT * FROM diario $busca ORDER BY fecha DESC"

        c = database!!.rawQuery(sql, null)
        c.moveToFirst()

        cantidad = c.getCount()
        datos = Array(cantidad) { arrayOfNulls(3) }

        try {
            if (cantidad > 0) {
                for (i in 0 .. cantidad) {
                    for (j in 0..2) datos[i][j] = c.getString(j)

                    c.moveToNext()
                }
            }
        } catch (e: Exception) {
            Log.d("ERROR", "Provoco el error: $e")
        }
        return datos
    }

    fun cierra(database: SQLiteDatabase) {
        database.close()
    }
}

