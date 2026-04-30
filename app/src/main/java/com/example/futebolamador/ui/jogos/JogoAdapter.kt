package com.example.futebolamador.ui.jogos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.futebolamador.R
import com.example.futebolamador.data.JogoEntity
import java.text.SimpleDateFormat
import java.util.*

class JogoAdapter(
    private var jogos: List<JogoEntity> = emptyList(),
    private val onEditar: (JogoEntity) -> Unit,
    private val onDeletar: (JogoEntity) -> Unit,
    private val onIniciar: (JogoEntity) -> Unit,
    private val onIntervalo: (JogoEntity) -> Unit,
    private val onFinalizar: (JogoEntity) -> Unit,
    private val onGolMandante: (JogoEntity, Int) -> Unit,
    private val onGolVisitante: (JogoEntity, Int) -> Unit
) : RecyclerView.Adapter<JogoAdapter.JogoViewHolder>() {

    inner class JogoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMandante: TextView = view.findViewById(R.id.txtMandante)
        val txtVisitante: TextView = view.findViewById(R.id.txtVisitante)
        val txtPlacar: TextView = view.findViewById(R.id.txtPlacar)
        val txtData: TextView = view.findViewById(R.id.txtData)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val btnIniciar: Button = view.findViewById(R.id.btnIniciar)
        val layoutBotoesControle: View = view.findViewById(R.id.layoutBotoesControle)
        val btnIntervalo: Button = view.findViewById(R.id.btnIntervalo)
        val btnFinalizar: Button = view.findViewById(R.id.btnFinalizar)
        val layoutBotoesGol: View = view.findViewById(R.id.layoutBotoesGol)
        val btnMaisMandante: Button = view.findViewById(R.id.btnMaisMandante)
        val btnMenosMandante: Button = view.findViewById(R.id.btnMenosMandante)
        val btnMaisVisitante: Button = view.findViewById(R.id.btnMaisVisitante)
        val btnMenosVisitante: Button = view.findViewById(R.id.btnMenosVisitante)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditarJogo)
        val btnDeletar: ImageButton = view.findViewById(R.id.btnDeletarJogo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JogoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jogo, parent, false)
        return JogoViewHolder(view)
    }

    override fun onBindViewHolder(holder: JogoViewHolder, position: Int) {
        val jogo = jogos[position]

        holder.txtMandante.text = jogo.timeMandanteNome
        holder.txtVisitante.text = jogo.timeVisitanteNome
        holder.txtData.text = "${jogo.data} ${jogo.hora}"

        when (jogo.status) {
            "Agendado" -> {
                holder.txtPlacar.text = "vs"
                holder.txtStatus.text = "Agendado"
                holder.txtStatus.setBackgroundColor(Color.parseColor("#1565C0"))
                holder.btnIniciar.visibility = View.VISIBLE
                holder.layoutBotoesControle.visibility = View.GONE
                holder.layoutBotoesGol.visibility = View.GONE
            }
            "Em Andamento" -> {
                holder.txtPlacar.text = "${jogo.placarMandante} x ${jogo.placarVisitante}"
                holder.txtStatus.text = "Em Andamento"
                holder.txtStatus.setBackgroundColor(Color.parseColor("#43A047"))
                holder.btnIniciar.visibility = View.GONE
                holder.layoutBotoesControle.visibility = View.VISIBLE
                holder.layoutBotoesGol.visibility = View.VISIBLE
            }
            "Intervalo" -> {
                holder.txtPlacar.text = "${jogo.placarMandante} x ${jogo.placarVisitante}"
                holder.txtStatus.text = "Intervalo"
                holder.txtStatus.setBackgroundColor(Color.parseColor("#FFB300"))
                holder.btnIniciar.visibility = View.GONE
                holder.layoutBotoesControle.visibility = View.VISIBLE
                holder.layoutBotoesGol.visibility = View.GONE
                holder.btnIntervalo.text = "Retomar"
            }
            "Concluído" -> {
                holder.txtPlacar.text = "${jogo.placarMandante} x ${jogo.placarVisitante}"
                holder.txtStatus.text = "Concluído"
                holder.txtStatus.setBackgroundColor(Color.parseColor("#757575"))
                holder.btnIniciar.visibility = View.GONE
                holder.layoutBotoesControle.visibility = View.GONE
                holder.layoutBotoesGol.visibility = View.GONE
            }
            else -> {
                holder.txtPlacar.text = "vs"
                holder.txtStatus.text = jogo.status
                holder.txtStatus.setBackgroundColor(Color.parseColor("#9E9E9E"))
                holder.btnIniciar.visibility = View.GONE
                holder.layoutBotoesControle.visibility = View.GONE
                holder.layoutBotoesGol.visibility = View.GONE
            }
        }

        holder.btnIniciar.setOnClickListener { onIniciar(jogo) }
        holder.btnIntervalo.setOnClickListener { onIntervalo(jogo) }
        holder.btnFinalizar.setOnClickListener { onFinalizar(jogo) }
        holder.btnMaisMandante.setOnClickListener { onGolMandante(jogo, 1) }
        holder.btnMenosMandante.setOnClickListener { onGolMandante(jogo, -1) }
        holder.btnMaisVisitante.setOnClickListener { onGolVisitante(jogo, 1) }
        holder.btnMenosVisitante.setOnClickListener { onGolVisitante(jogo, -1) }
        holder.btnEditar.setOnClickListener { onEditar(jogo) }
        holder.btnDeletar.setOnClickListener { onDeletar(jogo) }
    }

    override fun getItemCount() = jogos.size

    fun atualizarLista(novaLista: List<JogoEntity>) {
        jogos = novaLista
        notifyDataSetChanged()
    }
}