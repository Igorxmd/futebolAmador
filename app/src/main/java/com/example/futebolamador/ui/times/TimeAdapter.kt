package com.example.futebolamador.ui.times

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.futebolamador.R
import com.example.futebolamador.data.TimeEntity

class TimeAdapter(
    private var times: List<TimeEntity> = emptyList(),
    private val onClicar: (TimeEntity) -> Unit,
    private val onEditar: (TimeEntity) -> Unit,
    private val onDeletar: (TimeEntity) -> Unit,
    private val podeEditar: Boolean = false
) : RecyclerView.Adapter<TimeAdapter.TimeViewHolder>() {

    inner class TimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgBrasao: ImageView = view.findViewById(R.id.imgBrasao)
        val nomeText: TextView = view.findViewById(R.id.txtNome)
        val cidadeText: TextView = view.findViewById(R.id.txtCidade)
        val fundacaoText: TextView = view.findViewById(R.id.txtFundacao)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnDeletar: ImageButton = view.findViewById(R.id.btnDeletar)
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
        holder.fundacaoText.text = if (time.dataFundacao.isNotEmpty())
            "Fundado em: ${time.dataFundacao}" else ""

        if (time.brasaoUri.isNotEmpty()) {
            try {
                holder.imgBrasao.setImageURI(Uri.parse(time.brasaoUri))
            } catch (e: Exception) {
                holder.imgBrasao.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.imgBrasao.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.btnEditar.visibility = if (podeEditar) View.VISIBLE else View.GONE
        holder.btnDeletar.visibility = if (podeEditar) View.VISIBLE else View.GONE

        // Clique no card inteiro abre o perfil
        holder.itemView.setOnClickListener { onClicar(time) }
        holder.btnEditar.setOnClickListener { onEditar(time) }
        holder.btnDeletar.setOnClickListener { onDeletar(time) }
    }

    override fun getItemCount() = times.size

    // CORRIGIDO: DiffUtil para evitar redesenho total e piscar na tela
    fun atualizarLista(novaLista: List<TimeEntity>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = times.size
            override fun getNewListSize() = novaLista.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                times[oldPos].id == novaLista[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                times[oldPos] == novaLista[newPos]
        })
        times = novaLista
        diff.dispatchUpdatesTo(this)
    }
}