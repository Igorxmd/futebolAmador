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

        swipeRefresh.setOnRefreshListener {
            viewModel.carregarTodos()
        }

        onApostar = { jogo ->
            val bundle = Bundle()
            bundle.putString("jogoId", jogo.id)
            findNavController().navigate(
                R.id.action_jogosFragment_to_apostasJogoFragment, bundle)
        },

        lifecycleScope.launch {
            val perfil = SessaoManager.perfilAtual()
            val podeEditar = perfil == "admin" || perfil == "comissao"
            fab.visibility = if (podeEditar) View.VISIBLE else View.GONE

            // Carrega mapa de brasões dos times
            val times = com.example.futebolamador.repository.TimeRepository().getTodos()
            val brasoes = times.associate { it.id to it.brasaoUri }

            adapter = JogoAdapter(
                onEditar = { jogo ->
                    val bundle = Bundle()
                    bundle.putString("jogoId", jogo.id)
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
                onIniciar = { jogo -> tratarIniciar(jogo) },
                onIntervalo = { jogo -> tratarIntervalo(jogo) },
                onFinalizar = { jogo -> tratarFinalizar(jogo) },
                onGolMandante = { jogo, delta ->
                    val novo = (jogo.placarMandante + delta).coerceAtLeast(0)
                    viewModel.atualizar(jogo.copy(placarMandante = novo))
                },
                onGolVisitante = { jogo, delta ->
                    val novo = (jogo.placarVisitante + delta).coerceAtLeast(0)
                    viewModel.atualizar(jogo.copy(placarVisitante = novo))
                },
                podeEditar = podeEditar,
                brasoes = brasoes
            )

            recycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = this@JogosFragment.adapter
            }

            viewModel.todos.observe(viewLifecycleOwner) { lista ->
                swipeRefresh.isRefreshing = false
                adapter.atualizarLista(lista)
                layoutVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                recycler.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        fab.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("jogoId", "")
            findNavController().navigate(
                R.id.action_jogosFragment_to_jogoDetalheFragment, bundle)
        }

        view.findViewById<BottomNavigationView>(R.id.bottomNavJogos).apply {
            selectedItemId = R.id.nav_jogos
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_times -> {
                        findNavController().navigateUp()
                        true
                    }
                    else -> true
                }
            }
            R.id.nav_ranking -> {
            findNavController().navigate(R.id.action_global_rankingFragment)
            true
        }
        }
    }

    private fun horaAtual(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    // CORRIGIDO: calcula minutos usando timestamp real em vez de subtração de strings,
    // evitando erro na virada da meia-noite
    private fun minutosDesdeInicio(horarioInicio: String): Int {
        if (horarioInicio.isEmpty()) return 0
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val inicio = sdf.parse(horarioInicio) ?: return 0
            val agora = sdf.parse(horaAtual()) ?: return 0
            var diff = ((agora.time - inicio.time) / 60000).toInt()
            // Corrige virada de meia-noite (ex: início 23:50, agora 00:10)
            if (diff < 0) diff += 24 * 60
            diff
        } catch (e: Exception) { 0 }
    }

    private fun tratarIniciar(jogo: JogoEntity) {
        val minutos = minutosDesdeInicio(jogo.hora.padStart(5, '0').let { jogo.hora })
        // Compara hora atual com hora agendada
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val agora = sdf.parse(horaAtual())
        val agendado = try { sdf.parse(jogo.hora) } catch (e: Exception) { null }
        val antesDoHorario = agora != null && agendado != null && agora.before(agendado)

        if (antesDoHorario) {
            AlertDialog.Builder(requireContext())
                .setTitle("Iniciar antes do horário")
                .setMessage("O jogo está agendado para ${jogo.hora}. Deseja iniciar mesmo assim?")
                .setPositiveButton("Sim") { _, _ -> iniciarJogo(jogo) }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Iniciar Jogo")
                .setMessage("${jogo.timeMandanteNome} x ${jogo.timeVisitanteNome}\nDeseja iniciar?")
                .setPositiveButton("Sim") { _, _ -> iniciarJogo(jogo) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun iniciarJogo(jogo: JogoEntity) {
        viewModel.atualizar(jogo.copy(status = "Em Andamento", horarioInicio = horaAtual()))
    }

    private fun tratarIntervalo(jogo: JogoEntity) {
        if (jogo.status == "Intervalo") {
            AlertDialog.Builder(requireContext())
                .setTitle("Retomar Jogo")
                .setMessage("Deseja retomar o jogo?")
                .setPositiveButton("Sim") { _, _ ->
                    viewModel.atualizar(jogo.copy(status = "Em Andamento"))
                }
                .setNegativeButton("Cancelar", null)
                .show()
            return
        }
        val minutos = minutosDesdeInicio(jogo.horarioInicio)
        if (minutos < 45) {
            AlertDialog.Builder(requireContext())
                .setTitle("Intervalo antes do tempo")
                .setMessage("Apenas $minutos minutos de jogo. O intervalo ocorre após 45min. Tem certeza?")
                .setPositiveButton("Sim") { _, _ -> registrarIntervalo(jogo) }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Intervalo")
                .setMessage("Deseja registrar o intervalo?")
                .setPositiveButton("Sim") { _, _ -> registrarIntervalo(jogo) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun registrarIntervalo(jogo: JogoEntity) {
        viewModel.atualizar(jogo.copy(status = "Intervalo", horarioIntervalo = horaAtual()))
    }

    private fun tratarFinalizar(jogo: JogoEntity) {
        val minutos = minutosDesdeInicio(jogo.horarioInicio)
        if (minutos < 105) {
            AlertDialog.Builder(requireContext())
                .setTitle("Finalizar antes do tempo")
                .setMessage("Apenas $minutos minutos. Mínimo são 105 min (45+15+45). Tem certeza?")
                .setPositiveButton("Sim") { _, _ -> finalizarJogo(jogo) }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Finalizar Jogo")
                .setMessage("${jogo.timeMandanteNome} ${jogo.placarMandante} x ${jogo.placarVisitante} ${jogo.timeVisitanteNome}\nConfirmar?")
                .setPositiveButton("Sim") { _, _ -> finalizarJogo(jogo) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun finalizarJogo(jogo: JogoEntity) {
        viewModel.atualizar(jogo.copy(status = "Concluído", horarioFim = horaAtual()))
    }

    override fun onResume() {
        super.onResume()
        viewModel.carregarTodos()
    }
}