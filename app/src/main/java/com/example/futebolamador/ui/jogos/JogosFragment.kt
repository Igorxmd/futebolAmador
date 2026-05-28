package com.example.futebolamador.ui.jogos

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futebolamador.R
import com.example.futebolamador.auth.SessaoManager
import com.example.futebolamador.data.JogoEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class JogosFragment : Fragment() {

    private val viewModel: JogoViewModel by viewModels()
    private lateinit var adapter: JogoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_jogos, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAdicionarJogo)
        val layoutVazio = view.findViewById<View>(R.id.layoutVazioJogos)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerJogos)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshJogos)
        swipeRefresh.setColorSchemeColors(
            resources.getColor(android.R.color.holo_green_dark, null))

        swipeRefresh.setOnRefreshListener { viewModel.carregarTodos() }

        lifecycleScope.launch {
            val perfil = SessaoManager.perfilAtual()
            val podeEditar = perfil == "admin" || perfil == "comissao"
            fab.visibility = if (podeEditar) View.VISIBLE else View.GONE

            val times = com.example.futebolamador.repository.TimeRepository().getTodos()
            val brasoes = times.associate { it.id to it.brasaoUri }

            adapter = JogoAdapter(
                onEditar = { jogo ->
                    val bundle = Bundle().apply { putString("jogoId", jogo.id) }
                    findNavController().navigate(
                        R.id.action_jogosFragment_to_jogoDetalheFragment, bundle)
                },
                onDeletar = { jogo ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Remover Jogo")
                        .setMessage("${jogo.timeMandanteNome} x ${jogo.timeVisitanteNome}")
                        .setPositiveButton("Sim") { _, _ -> viewModel.deletar(jogo) }
                        .setNegativeButton("Cancelar", null)
                        .show()
                },
                onIniciar    = { jogo -> tratarIniciar(jogo) },
                onIntervalo  = { jogo -> tratarIntervalo(jogo) },
                onFinalizar  = { jogo -> tratarFinalizar(jogo) },
                onGolMandante = { jogo, delta ->
                    viewModel.atualizar(jogo.copy(
                        placarMandante = (jogo.placarMandante + delta).coerceAtLeast(0)))
                },
                onGolVisitante = { jogo, delta ->
                    viewModel.atualizar(jogo.copy(
                        placarVisitante = (jogo.placarVisitante + delta).coerceAtLeast(0)))
                },
                podeEditar = podeEditar,
                brasoes    = brasoes,
                onApostar  = { jogo ->
                    val bundle = Bundle().apply { putString("jogoId", jogo.id) }
                    findNavController().navigate(
                        R.id.action_jogosFragment_to_apostasJogoFragment, bundle)
                }
            )

            recycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = this@JogosFragment.adapter
            }

            viewModel.todos.observe(viewLifecycleOwner) { lista ->
                swipeRefresh.isRefreshing = false
                adapter.atualizarLista(lista)
                layoutVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                recycler.visibility   = if (lista.isEmpty()) View.GONE  else View.VISIBLE
            }
        }

        fab.setOnClickListener {
            val bundle = Bundle().apply { putString("jogoId", "") }
            findNavController().navigate(R.id.action_jogosFragment_to_jogoDetalheFragment, bundle)
        }

        view.findViewById<BottomNavigationView>(R.id.bottomNavJogos).apply {
            selectedItemId = R.id.nav_jogos
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_times -> {
                        findNavController().navigateUp()
                        true
                    }
                    R.id.nav_ranking -> {
                        findNavController().navigate(R.id.action_jogosFragment_to_rankingFragment)
                        true
                    }
                    else -> true
                }
            }
        }
    }

    private fun horaAtual(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    private fun minutosDesdeInicio(horarioInicio: String): Int {
        if (horarioInicio.isEmpty()) return 0
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val inicio = sdf.parse(horarioInicio) ?: return 0
            val agora  = sdf.parse(horaAtual())  ?: return 0
            var diff = ((agora.time - inicio.time) / 60000).toInt()
            if (diff < 0) diff += 24 * 60
            diff
        } catch (e: Exception) { 0 }
    }

    private fun tratarIniciar(jogo: JogoEntity) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val agora    = sdf.parse(horaAtual())
        val agendado = try { sdf.parse(jogo.hora) } catch (e: Exception) { null }
        val antes    = agora != null && agendado != null && agora.before(agendado)

        val titulo   = if (antes) "Iniciar antes do horário" else "Iniciar Jogo"
        val mensagem = if (antes)
            "O jogo está agendado para ${jogo.hora}. Deseja iniciar mesmo assim?"
        else
            "${jogo.timeMandanteNome} x ${jogo.timeVisitanteNome}\nDeseja iniciar?"

        AlertDialog.Builder(requireContext())
            .setTitle(titulo).setMessage(mensagem)
            .setPositiveButton("Sim") { _, _ -> iniciarJogo(jogo) }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun iniciarJogo(jogo: JogoEntity) =
        viewModel.atualizar(jogo.copy(status = "Em Andamento", horarioInicio = horaAtual()))

    private fun tratarIntervalo(jogo: JogoEntity) {
        if (jogo.status == "Intervalo") {
            AlertDialog.Builder(requireContext())
                .setTitle("Retomar Jogo").setMessage("Deseja retomar o jogo?")
                .setPositiveButton("Sim") { _, _ ->
                    viewModel.atualizar(jogo.copy(status = "Em Andamento")) }
                .setNegativeButton("Cancelar", null).show()
            return
        }
        val minutos = minutosDesdeInicio(jogo.horarioInicio)
        val cedo = minutos < 45
        AlertDialog.Builder(requireContext())
            .setTitle(if (cedo) "Intervalo antes do tempo" else "Intervalo")
            .setMessage(if (cedo)
                "Apenas $minutos minutos de jogo. Intervalo após 45min. Tem certeza?"
            else "Deseja registrar o intervalo?")
            .setPositiveButton("Sim") { _, _ -> registrarIntervalo(jogo) }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun registrarIntervalo(jogo: JogoEntity) =
        viewModel.atualizar(jogo.copy(status = "Intervalo", horarioIntervalo = horaAtual()))

    private fun tratarFinalizar(jogo: JogoEntity) {
        val minutos = minutosDesdeInicio(jogo.horarioInicio)
        val cedo = minutos < 105
        AlertDialog.Builder(requireContext())
            .setTitle(if (cedo) "Finalizar antes do tempo" else "Finalizar Jogo")
            .setMessage(if (cedo)
                "Apenas $minutos minutos. Mínimo 105min. Tem certeza?"
            else "${jogo.timeMandanteNome} ${jogo.placarMandante} x ${jogo.placarVisitante} ${jogo.timeVisitanteNome}\nConfirmar?")
            .setPositiveButton("Sim") { _, _ -> finalizarJogo(jogo) }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun finalizarJogo(jogo: JogoEntity) =
        viewModel.atualizar(jogo.copy(status = "Concluído", horarioFim = horaAtual()))

    override fun onResume() {
        super.onResume()
        viewModel.carregarTodos()
    }
}