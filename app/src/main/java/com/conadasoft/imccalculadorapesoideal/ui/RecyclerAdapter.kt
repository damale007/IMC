package com.conadasoft.imccalculadorapesoideal.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.conadasoft.imccalculadorapesoideal.R
import com.conadasoft.imccalculadorapesoideal.databinding.ItemFavoritosBinding
import com.conadasoft.imccalculadorapesoideal.ui.InicioFragment.Companion.medidaPeso
import kotlin.math.abs

class RecyclerAdapter(private val pesos: List<List<String>>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoritosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pesos[position][1], pesos[position][2])
    }

    override fun getItemCount() = pesos.size


    class ViewHolder(private val binding: ItemFavoritosBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fecha: String, peso: String) {
            val context = binding.idPeso.context
            binding.idFecha.text = formatoFecha(context, fecha)
            binding.idPeso.text = peso

            binding.idUnidadPeso.text = context.resources.getStringArray(R.array.m_peso)[medidaPeso]

            val ideal = pesoIdeal()
            var mensaje = ""
            var diferencia: Int = (peso.toFloat() - ideal).toInt()
            if (diferencia == 0)
                mensaje = "(" + context.resources.getString(R.string.pesoIdealT) + ")"
            else if (diferencia > 0)
                mensaje = "( +" + diferencia + " " + context.resources.getStringArray(R.array.m_peso)[medidaPeso] + ")"
            else {
                diferencia = -diferencia
                mensaje = "( -" + diferencia + " " + context.resources.getStringArray(R.array.m_peso)[medidaPeso] + ")"
            }
            binding.idPesoResultado.text = mensaje
        }

        private fun pesoIdeal() : Int{
            var ideal = 0

            if (InicioFragment.sexo ==0)
                return 0
            else {
                if (InicioFragment.sexo == 1) {  //Hombre
                    ideal = when (InicioFragment.altura) {
                        in 152f..156f -> 51
                        in 157f..161f -> 54
                        in 162f..166f -> 57
                        in 167f..171f -> 63
                        in 172f..176f -> 66
                        in 177f..181f -> 69
                        in 182f..186f -> 74
                        in 187f..191f -> 77
                        in 192f..220f -> 80
                        else -> 0
                    }
                } else {  // Mujer
                    ideal = when (InicioFragment.altura) {
                        in 152f..156f -> 49
                        in 157f..161f -> 52
                        in 162f..166f -> 55
                        in 167f..171f -> 59
                        in 172f..176f -> 63
                        in 177f..181f -> 67
                        in 182f..186f -> 71
                        in 187f..191f -> 75
                        in 192f..220f -> 78
                        else -> 0
                    }
                }
            }
            return ideal
        }

        private fun formatoFecha(context: Context,fecha: String): String {
            val partes = fecha.split("-")

            return partes[2] + " " + context.resources.getStringArray(R.array.meses)[partes[1].toInt() -1] + " " + partes[0]
        }
    }
}