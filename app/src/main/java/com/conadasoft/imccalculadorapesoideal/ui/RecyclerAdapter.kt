package com.conadasoft.imccalculadorapesoideal.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.conadasoft.imccalculadorapesoideal.databinding.ItemFavoritosBinding

class RecyclerAdapter(private val pesos: List<List<String>>) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
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
            binding.idFecha.text = formatoFecha(fecha)
            binding.idPeso.text = peso

            val ideal = pesoIdeal()

            var diferencia = peso.toFloat() - ideal
            if (diferencia == 0f)
                binding.idPesoResultado.text="Tienes el peso ideal"
            else if (diferencia > 0)
                binding.idPesoResultado.text="Tienes sobrepeso de $diferencia kgs."
            else {
                diferencia = -diferencia
                binding.idPesoResultado.text = "Tienes desnutrición de $diferencia kgs."
            }
        }

        private fun pesoIdeal() : Int{
            var ideal = 0

            if (InicioFragment.sexo ==0)
                return 0
            else {
                if (InicioFragment.sexo == 1) {  //Hombre
                    ideal = when (InicioFragment.altura) {
                        in 152..156 -> 51
                        in 157..161 -> 54
                        in 162..166 -> 57
                        in 167..171 -> 63
                        in 172..176 -> 66
                        in 177..181 -> 69
                        in 182..186 -> 74
                        in 187..191 -> 77
                        in 192..220 -> 80
                        else -> 0
                    }
                } else {  // Mujer
                    ideal = when (InicioFragment.altura) {
                        in 152..156 -> 49
                        in 157..161 -> 52
                        in 162..166 -> 55
                        in 167..171 -> 59
                        in 172..176 -> 63
                        in 177..181 -> 67
                        in 182..186 -> 71
                        in 187..191 -> 75
                        in 192..220 -> 78
                        else -> 0
                    }
                }
            }
            return ideal
        }

        private fun formatoFecha(fecha: String): String {
            val partes = fecha.split("-")
            val meses = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")

            return "${partes[2]} ${meses[partes[1].toInt() - 1]} ${partes[0]}"
        }
    }
}