package com.example.futebolamador.ui.times

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.futebolamador.R
import com.example.futebolamador.data.TimeEntity

class TimeAdapter(
    private var times: List<TimeEntity> = emptyList(),
    private val onEditar: (TimeEntity) -> Unit,
    private val onDeletar: (TimeEntity) -> Unit
) : RecyclerView.Adapter<TimeAdapter.TimeViewHolder>() {

    inner class TimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val corView: ImageView = view.findViewById(R.id.imgCor)
        val nomeText: TextView = view.findViewById(R.id.txtNome)
        val cidadeText: TextView = view.findViewById(R.id.txtCidade)
        val btnEditar: View = view.findViewById(R.id.btnEditar)
        val btnDeletar: View = view.findViewById(R.id.btnDeletar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time, parent, false)
        return TimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val time = times[position]

        holder.nomeText.text = time.nome
        holder.cidadeText.text = time.cidade

        // Aplica a cor do time no círculo
        try {
            holder.corView.setColorFilter(Color.parseColor(time.corPrimaria))
        } catch (e: Exception) {
            holder.corView.setColorFilter(Color.parseColor("#1E88E5"))
        }

        holder.btnEditar.setOnClickListener { onEditar(time) }
        holder.btnDeletar.setOnClickListener { onDeletar(time) }
    }

    override fun getItemCount() = times.size

    // Atualiza a lista e redesenha o RecyclerView
    fun atualizarLista(novaLista: List<TimeEntity>) {
        times = novaLista
        notifyDataSetChanged()
    }
}