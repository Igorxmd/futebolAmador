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
import android.widget.ImageView

class JogoAdapter(
    private var jogos: List<JogoEntity> = emptyList(),
    private val onEditar: (JogoEntity) -> Unit,
    private val onDeletar: (JogoEntity) -> Unit,
    private val onIniciar: (JogoEntity) -> Unit,
    private val onIntervalo: (JogoEntity) -> Unit,
    private val onFinalizar: (JogoEntity) -> Unit,
    private val onGolMandante: (JogoEntity, Int) -> Unit,
    private val onGolVisitante: (JogoEntity, Int) -> Unit,
    private val podeEditar: Boolean = false,
    private val brasoes: Map<String, String> = emptyMap()
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

        // Oculta editar/deletar para torcedores
        holder.btnEditar.visibility = if (podeEditar) View.VISIBLE else View.GONE
        holder.btnDeletar.visibility = if (podeEditar) View.VISIBLE else View.GONE

        // Carrega brasão mandante
        val brasaoMandante = brasoes[jogo.timeMandanteId]
        if (!brasaoMandante.isNullOrEmpty()) {
            try {
                holder.itemView.findViewById<ImageView>(R.id.imgMandante)
                    .setImageURI(android.net.Uri.parse(brasaoMandante))
            } catch (e: Exception) {}
        }

// Carrega brasão visitante
        val brasaoVisitante = brasoes[jogo.timeVisitanteId]
        if (!brasaoVisitante.isNullOrEmpty()) {
            try {
                holder.itemView.findViewById<ImageView>(R.id.imgVisitante)
                    .setImageURI(android.net.Uri.parse(brasaoVisitante))
            } catch (e: Exception) {}
        }

        when (jogo.status) {
            "Agendado" -> {
                holder.txtPlacar.text = "vs"
                holder.txtStatus.text = "Agendado"
                holder.txtStatus.setBackgroundColor(Color.parseColor("#1565C0"))
                holder.btnIniciar.visibility = if (podeEditar) View.VISIBLE else View.GONE
                holder.layoutBotoesControle.visibility = View.GONE
                holder.layoutBotoesGol.visibility = View.GONE
            }
            "Em Andamento" -> {
                holder.txtPlacar.text = "${jogo.placarMandante} x ${jogo.placarVisitante}"
                holder.txtStatus.text = "Em Andamento"
                holder.txtStatus.setBackgroundColor(Color.parseColor("#43A047"))
                holder.btnIniciar.visibility = View.GONE
                holder.layoutBotoesControle.visibility = if (podeEditar) View.VISIBLE else View.GONE
                holder.layoutBotoesGol.visibility = if (podeEditar) View.VISIBLE else View.GONE
            }
            "Intervalo" -> {
                holder.txtPlacar.text = "${jogo.placarMandante} x ${jogo.placarVisitante}"
                holder.txtStatus.text = "Intervalo"
                holder.txtStatus.setBackgroundColor(Color.parseColor("#FFB300"))
                holder.btnIniciar.visibility = View.GONE
                holder.layoutBotoesControle.visibility = if (podeEditar) View.VISIBLE else View.GONE
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