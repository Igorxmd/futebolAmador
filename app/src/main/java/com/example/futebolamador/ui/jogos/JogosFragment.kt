package com.example.futebolamador.ui.jogos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futebolamador.R
import com.example.futebolamador.data.JogoEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

        adapter = JogoAdapter(
            onEditar = { jogo ->
                val bundle = Bundle()
                bundle.putInt("jogoId", jogo.id)
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
            }
        )

        view.findViewById<RecyclerView>(R.id.recyclerJogos).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@JogosFragment.adapter
        }

        viewModel.todos.observe(viewLifecycleOwner) { lista ->
            adapter.atualizarLista(lista)
        }

        view.findViewById<FloatingActionButton>(R.id.fabAdicionarJogo).setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("jogoId", 0)
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
        }
    }

    private fun horaAtual(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun horaParaMinutos(hora: String): Int {
        return try {
            val partes = hora.split(":")
            partes[0].toInt() * 60 + partes[1].toInt()
        } catch (e: Exception) { 0 }
    }

    private fun minutosDesdeInicio(horarioInicio: String): Int {
        if (horarioInicio.isEmpty()) return 0
        val agora = horaParaMinutos(horaAtual())
        val inicio = horaParaMinutos(horarioInicio)
        return agora - inicio
    }

    private fun tratarIniciar(jogo: JogoEntity) {
        val agora = horaParaMinutos(horaAtual())
        val agendado = horaParaMinutos(jogo.hora)

        if (agora < agendado) {
            AlertDialog.Builder(requireContext())
                .setTitle("Iniciar antes do horário")
                .setMessage("O jogo está agendado para ${jogo.hora}. Deseja iniciar agora mesmo assim?")
                .setPositiveButton("Sim") { _, _ -> iniciarJogo(jogo) }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Iniciar Jogo")
                .setMessage("${jogo.timeMandanteNome} x ${jogo.timeVisitanteNome}\nDeseja iniciar o jogo?")
                .setPositiveButton("Sim") { _, _ -> iniciarJogo(jogo) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun iniciarJogo(jogo: JogoEntity) {
        viewModel.atualizar(jogo.copy(
            status = "Em Andamento",
            horarioInicio = horaAtual()
        ))
    }

    private fun tratarIntervalo(jogo: JogoEntity) {
        // Se status for "Intervalo", é retomar
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
                .setMessage("Apenas $minutos minutos de jogo. O intervalo normalmente ocorre após 45 minutos. Tem certeza?")
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
        viewModel.atualizar(jogo.copy(
            status = "Intervalo",
            horarioIntervalo = horaAtual()
        ))
    }

    private fun tratarFinalizar(jogo: JogoEntity) {
        val minutos = minutosDesdeInicio(jogo.horarioInicio)

        if (minutos < 105) {
            AlertDialog.Builder(requireContext())
                .setTitle("Finalizar antes do tempo")
                .setMessage("Apenas $minutos minutos desde o início. O tempo mínimo é de 105 minutos (45 + 15 de intervalo + 45). Tem certeza que deseja finalizar?")
                .setPositiveButton("Sim") { _, _ -> finalizarJogo(jogo) }
                .setNegativeButton("Cancelar", null)
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle("Finalizar Jogo")
                .setMessage("Deseja finalizar o jogo?\n${jogo.timeMandanteNome} ${jogo.placarMandante} x ${jogo.placarVisitante} ${jogo.timeVisitanteNome}")
                .setPositiveButton("Sim") { _, _ -> finalizarJogo(jogo) }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun finalizarJogo(jogo: JogoEntity) {
        viewModel.atualizar(jogo.copy(
            status = "Concluído",
            horarioFim = horaAtual()
        ))
    }

    override fun onResume() {
        super.onResume()
        viewModel.carregarTodos()
    }
}