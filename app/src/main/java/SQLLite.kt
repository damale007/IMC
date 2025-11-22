import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.conadasoft.imccalculadorapesoideal.Diario

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

    fun arreglaFecha(id: Int, fecha: String){
        val fechaCorrecta = corrigeFecha(fecha)
        try {
            database!!.execSQL("UPDATE diario SET fecha = $fechaCorrecta WHERE id = $id")
        } catch (e: Exception) {
            Log.e("ERROR", "actualiza fecha: $e")
        }

    }

    private fun corrigeFecha(fecha: String): String {
        // 1. Dividimos el string en partes.
        val partes = fecha.split("-")

        // 2. Comprobación de seguridad: si la fecha no tiene 3 partes, la devolvemos como está.
        if (partes.size != 3) {
            return fecha
        }

        // 3. Usamos padStart para añadir un '0' al inicio si es necesario.
        val year = partes[0]
        val month = partes[1].padStart(2, '0')
        val day = partes[2].padStart(2, '0')

        // 4. Unimos todo usando una plantilla de string, que es más limpio.
        return "$year-$month-$day"
    }

    fun actualizaDiario(id: Int, peso: Float) {
        try {
            database!!.execSQL("UPDATE diario SET peso = $peso WHERE id = $id")
        } catch (e: Exception) {
            Log.e("ERROR", "actualiza diario: $e")
        }
    }

    fun Select(busca: String, orden: Boolean = true): ArrayList<Diario> {
        var datos: ArrayList<Diario> = ArrayList()
        var cond = ""
        var ordenar: String

        if (orden)
            ordenar = "DESC"
        else
            ordenar = "ASC"

        if (busca != "")
            cond = "WHERE $busca"
        else
            cond = ""

        val sql = "SELECT * FROM diario $cond ORDER BY id $ordenar"

        Log.d("SQL", sql)
        c = database!!.rawQuery(sql, null)
        c.moveToFirst()

        cantidad = c.count-1
        //datos = Array(cantidad) { arrayOfNulls(3) }

        try {
            if (cantidad > 0) {
                for (i in 0 .. cantidad) {
                    val diario =  Diario(c.getInt(0), c.getString(1), c.getFloat(2))

                    c.moveToNext()
                    datos.add(diario)
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

