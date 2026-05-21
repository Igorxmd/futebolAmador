package com.example.futebolamador.ui.jogos

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.futebolamador.R
import com.example.futebolamador.data.JogoEntity

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
    private val brasoes: Map<String, String> = emptyMap(),
    private val onApostar: (JogoEntity) -> Unit,
) : RecyclerView.Adapter<JogoAdapter.JogoViewHolder>() {

    inner class JogoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMandante: TextView = view.findViewById(R.id.txtMandante)
        val txtVisitante: TextView = view.findViewById(R.id.txtVisitante)
        val txtPlacar: TextView = view.findViewById(R.id.txtPlacar)
        val txtData: TextView = view.findViewById(R.id.txtData)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val imgMandante: ImageView = view.findViewById(R.id.imgMandante)
        val imgVisitante: ImageView = view.findViewById(R.id.imgVisitante)
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
        val btnApostar: Button = view.findViewById(R.id.btnApostar)
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

        holder.btnEditar.visibility = if (podeEditar) View.VISIBLE else View.GONE
        holder.btnDeletar.visibility = if (podeEditar) View.VISIBLE else View.GONE

        val brasaoMandante = brasoes[jogo.timeMandanteId]
        if (!brasaoMandante.isNullOrEmpty()) {
            try { holder.imgMandante.setImageURI(Uri.parse(brasaoMandante)) }
            catch (e: Exception) { holder.imgMandante.setImageResource(android.R.drawable.ic_menu_myplaces) }
        } else {
            holder.imgMandante.setImageResource(android.R.drawable.ic_menu_myplaces)
        }

        val brasaoVisitante = brasoes[jogo.timeVisitanteId]
        if (!brasaoVisitante.isNullOrEmpty()) {
            try { holder.imgVisitante.setImageURI(Uri.parse(brasaoVisitante)) }
            catch (e: Exception) { holder.imgVisitante.setImageResource(android.R.drawable.ic_menu_myplaces) }
        } else {
            holder.imgVisitante.setImageResource(android.R.drawable.ic_menu_myplaces)
        }

        // Reset do texto do botão (RecyclerView reutiliza views)
        holder.btnIntervalo.text = "Intervalo"

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
        holder.btnApostar.setOnClickListener { onApostar(jogo) }

        holder.btnApostar.visibility =
            if (jogo.status in listOf("Agendado", "Em andamento")) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = jogos.size

    fun atualizarLista(novaLista: List<JogoEntity>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = jogos.size
            override fun getNewListSize() = novaLista.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                jogos[oldPos].id == novaLista[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                jogos[oldPos] == novaLista[newPos]
        })
        jogos = novaLista
        diff.dispatchUpdatesTo(this)
    }
}