package com.example.futebolamador.ui.times

import android.net.Uri
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
        val imgBrasao: ImageView = view.findViewById(R.id.imgBrasao)
        val nomeText: TextView = view.findViewById(R.id.txtNome)
        val cidadeText: TextView = view.findViewById(R.id.txtCidade)
        val fundacaoText: TextView = view.findViewById(R.id.txtFundacao)
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
        holder.fundacaoText.text = if (time.dataFundacao.isNotEmpty())
            "Fundado em: ${time.dataFundacao}" else ""

        // Mostra o brasão se tiver, senão mostra ícone padrão
        if (time.brasaoUri.isNotEmpty()) {
            try {
                holder.imgBrasao.setImageURI(Uri.parse(time.brasaoUri))
            } catch (e: Exception) {
                holder.imgBrasao.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            // Sem brasão: mostra fundo cinza com ícone discreto
            holder.imgBrasao.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.btnEditar.setOnClickListener { onEditar(time) }
        holder.btnDeletar.setOnClickListener { onDeletar(time) }
    }

    override fun getItemCount() = times.size

    fun atualizarLista(novaLista: List<TimeEntity>) {
        times = novaLista
        notifyDataSetChanged()
    }
}