package com.example.futebolamador.ui.times

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.futebolamador.R
import com.example.futebolamador.data.TimeEntity

class TimeDetalheFragment : Fragment() {

    private val viewModel: TimeViewModel by viewModels()

    private lateinit var editNome: EditText
    private lateinit var editCidade: EditText
    private lateinit var editCor: EditText

    private var timeAtual: TimeEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_time_detalhe, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editNome = view.findViewById(R.id.editNome)
        editCidade = view.findViewById(R.id.editCidade)
        editCor = view.findViewById(R.id.editCor)

        val timeId = arguments?.getInt("timeId") ?: 0

        if (timeId > 0) {
            timeAtual = viewModel.getPorId(timeId)
            timeAtual?.let { time ->
                editNome.setText(time.nome)
                editCidade.setText(time.cidade)
                editCor.setText(time.corPrimaria)
            }
        }

        view.findViewById<Button>(R.id.btnSalvar).setOnClickListener {
            salvar()
        }

        view.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun salvar() {
        val nome = editNome.text.toString().trim()
        val cidade = editCidade.text.toString().trim()
        val cor = editCor.text.toString().trim().ifEmpty { "#1E88E5" }

        if (nome.isEmpty()) {
            editNome.error = "Informe o nome do time"
            return
        }
        if (cidade.isEmpty()) {
            editCidade.error = "Informe a cidade"
            return
        }

        if (timeAtual != null) {
            viewModel.atualizar(timeAtual!!.copy(nome = nome, cidade = cidade, corPrimaria = cor))
            Toast.makeText(requireContext(), "Time atualizado!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.inserir(TimeEntity(nome = nome, cidade = cidade, corPrimaria = cor))
            Toast.makeText(requireContext(), "Time cadastrado!", Toast.LENGTH_SHORT).show()
        }

        findNavController().popBackStack()
    }
}