package com.example.futebolamador.ui.times

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.futebolamador.R
import com.example.futebolamador.auth.SessaoManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TimesFragment : Fragment() {

    private val viewModel: TimeViewModel by viewModels()
    private lateinit var adapter: TimeAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_times, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab = view.findViewById<FloatingActionButton>(R.id.fabAdicionarTime)
        val layoutVazio = view.findViewById<View>(R.id.layoutVazioTimes)
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerTimes)
        swipeRefresh = view.findViewById(R.id.swipeRefreshTimes)
        swipeRefresh.setColorSchemeColors(
            resources.getColor(android.R.color.holo_green_dark, null))

        lifecycleScope.launch {
            val perfil = SessaoManager.perfilAtual()
            val podeEditar = perfil == "admin" || perfil == "comissao"
            fab.visibility = if (podeEditar) View.VISIBLE else View.GONE

            adapter = TimeAdapter(
                onClicar = { time ->
                    // Abre tela de perfil do time
                    val bundle = Bundle()
                    bundle.putString("timeId", time.id)
                    findNavController().navigate(
                        R.id.action_timesFragment_to_timePerfilFragment, bundle)
                },
                onEditar = { time ->
                    val bundle = Bundle()
                    bundle.putString("timeId", time.id)
                    findNavController().navigate(
                        R.id.action_timesFragment_to_timeDetalheFragment, bundle)
                },
                onDeletar = { time ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Remover Time")
                        .setMessage("Deseja remover ${time.nome}?")
                        .setPositiveButton("Sim") { _, _ ->
                            viewModel.deletar(time)
                            Snackbar.make(view, "Time removido", Snackbar.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                },
                podeEditar = podeEditar
            )

            recycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = this@TimesFragment.adapter
            }

            viewModel.todos.observe(viewLifecycleOwner) { lista ->
                swipeRefresh.isRefreshing = false
                adapter.atualizarLista(lista)
                layoutVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                recycler.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        swipeRefresh.setOnRefreshListener {
            viewModel.carregarTodos()
        }

        fab.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("timeId", "")
            findNavController().navigate(
                R.id.action_timesFragment_to_timeDetalheFragment, bundle)
        }

        view.findViewById<MaterialToolbar>(R.id.toolbarTimes).apply {
            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_perfil) {
                    findNavController().navigate(R.id.action_timesFragment_to_perfilFragment)
                    true
                } else false
            }
        }

        view.findViewById<BottomNavigationView>(R.id.bottomNav).apply {
            selectedItemId = R.id.nav_times
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_jogos -> {
                        findNavController().navigate(R.id.action_timesFragment_to_jogosFragment)
                        true
                    }
                    R.id.nav_ranking -> {
                        findNavController().navigate(R.id.action_timesFragment_to_rankingFragment)
                        true
                    }
                    else -> true
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.carregarTodos()
    }
}