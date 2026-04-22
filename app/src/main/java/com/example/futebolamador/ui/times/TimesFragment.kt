package com.example.futebolamador.ui.times

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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TimesFragment : Fragment() {

    private val viewModel: TimeViewModel by viewModels()
    private lateinit var adapter: TimeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_times, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TimeAdapter(
            onEditar = { time ->
                val bundle = Bundle()
                bundle.putInt("timeId", time.id)
                findNavController().navigate(
                    R.id.action_timesFragment_to_timeDetalheFragment, bundle
                )
            },
            onDeletar = { time ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Remover Time")
                    .setMessage("Deseja remover ${time.nome}?")
                    .setPositiveButton("Sim") { _, _ ->
                        viewModel.deletar(time)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )

        view.findViewById<RecyclerView>(R.id.recyclerTimes).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TimesFragment.adapter
        }

        viewModel.todos.observe(viewLifecycleOwner) { lista ->
            adapter.atualizarLista(lista)
        }

        view.findViewById<FloatingActionButton>(R.id.fabAdicionarTime).setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("timeId", 0)
            findNavController().navigate(
                R.id.action_timesFragment_to_timeDetalheFragment, bundle
            )
        }
    }

    // Recarrega a lista ao voltar para a tela
    override fun onResume() {
        super.onResume()
        viewModel.carregarTodos()
    }
}