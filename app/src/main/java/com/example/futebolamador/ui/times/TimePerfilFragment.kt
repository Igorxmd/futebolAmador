package com.example.futebolamador.ui.times

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futebolamador.R
import com.example.futebolamador.ui.jogos.JogoAdapter
import com.example.futebolamador.ui.jogos.JogoViewModel
import com.google.android.material.appbar.MaterialToolbar
import android.widget.ImageView
import kotlinx.coroutines.launch

class TimePerfilFragment : Fragment() {

    private val timeViewModel: TimeViewModel by viewModels()
    private val jogoViewModel: JogoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_time_perfil, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timeId = arguments?.getString("timeId") ?: return

        view.findViewById<MaterialToolbar>(R.id.toolbarTimePerfil)
            .setNavigationOnClickListener { findNavController().popBackStack() }

        lifecycleScope.launch {
            val time = timeViewModel.getPorIdFirestore(timeId) ?: return@launch

            view.findViewById<MaterialToolbar>(R.id.toolbarTimePerfil).title = time.nome
            view.findViewById<TextView>(R.id.txtNomePerfil).text = time.nome
            view.findViewById<TextView>(R.id.txtCidadePerfil).text = time.cidade
            view.findViewById<TextView>(R.id.txtFundacaoPerfil).text =
                if (time.dataFundacao.isNotEmpty()) "Fundado em: ${time.dataFundacao}" else ""

            if (time.brasaoUri.isNotEmpty()) {
                try {
                    view.findViewById<ImageView>(R.id.imgBrasaoPerfil)
                        .setImageURI(Uri.parse(time.brasaoUri))
                } catch (e: Exception) {}
            }
        }

        // Carrega jogos do time e calcula estatísticas
        jogoViewModel.carregarTodos()
        jogoViewModel.todos.observe(viewLifecycleOwner) { todosJogos ->
            val jogosDoTime = todosJogos.filter {
                it.timeMandanteId == timeId || it.timeVisitanteId == timeId
            }
            val concluidos = jogosDoTime.filter { it.status == "Concluído" }

            var vitorias = 0
            var empates = 0
            var derrotas = 0

            concluidos.forEach { jogo ->
                val ehMandante = jogo.timeMandanteId == timeId
                val golsPro = if (ehMandante) jogo.placarMandante else jogo.placarVisitante
                val golsContra = if (ehMandante) jogo.placarVisitante else jogo.placarMandante
                when {
                    golsPro > golsContra -> vitorias++
                    golsPro == golsContra -> empates++
                    else -> derrotas++
                }
            }

            view.findViewById<TextView>(R.id.txtTotalJogos).text = jogosDoTime.size.toString()
            view.findViewById<TextView>(R.id.txtVitorias).text = vitorias.toString()
            view.findViewById<TextView>(R.id.txtEmpates).text = empates.toString()
            view.findViewById<TextView>(R.id.txtDerrotas).text = derrotas.toString()

            val recycler = view.findViewById<RecyclerView>(R.id.recyclerJogosTime)
            val txtSemJogos = view.findViewById<TextView>(R.id.txtSemJogos)

            if (jogosDoTime.isEmpty()) {
                txtSemJogos.visibility = View.VISIBLE
                recycler.visibility = View.GONE
            } else {
                txtSemJogos.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                recycler.layoutManager = LinearLayoutManager(requireContext())
                recycler.adapter = JogoAdapter(
                    jogos = jogosDoTime,
                    onEditar = {},
                    onDeletar = {},
                    onIniciar = {},
                    onIntervalo = {},
                    onFinalizar = {},
                    onGolMandante = { _, _ -> },
                    onGolVisitante = { _, _ -> },
                    podeEditar = false
                )
            }
        }
    }
}