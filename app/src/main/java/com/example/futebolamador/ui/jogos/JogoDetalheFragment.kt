package com.example.futebolamador.ui.jogos

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.futebolamador.R
import com.example.futebolamador.data.JogoEntity
import com.example.futebolamador.data.TimeEntity
import com.example.futebolamador.repository.TimeRepository
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class JogoDetalheFragment : Fragment() {

    private val viewModel: JogoViewModel by viewModels()
    private lateinit var spinnerMandante: Spinner
    private lateinit var spinnerVisitante: Spinner
    private lateinit var editData: EditText
    private lateinit var editHora: EditText

    private var times: List<TimeEntity> = emptyList()
    private var jogoAtual: JogoEntity? = null
    private var aplicandoMascaraData = false
    private var aplicandoMascaraHora = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_jogo_detalhe, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinnerMandante = view.findViewById(R.id.spinnerMandante)
        spinnerVisitante = view.findViewById(R.id.spinnerVisitante)
        editData = view.findViewById(R.id.editData)
        editHora = view.findViewById(R.id.editHora)

        view.findViewById<MaterialToolbar>(R.id.toolbarJogo)
            .setNavigationOnClickListener { findNavController().popBackStack() }

        // Máscara data DD/MM/AAAA
        editData.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (aplicandoMascaraData) return
                aplicandoMascaraData = true
                var texto = s.toString().replace("/", "")
                if (texto.length > 8) texto = texto.substring(0, 8)
                val formatado = StringBuilder()
                for (i in texto.indices) {
                    formatado.append(texto[i])
                    if (i == 1 || i == 3) formatado.append("/")
                }
                editData.setText(formatado)
                editData.setSelection(formatado.length)
                aplicandoMascaraData = false
            }
        })

        // Máscara hora HH:MM
        editHora.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (aplicandoMascaraHora) return
                aplicandoMascaraHora = true
                var texto = s.toString().replace(":", "")
                if (texto.length > 4) texto = texto.substring(0, 4)
                val formatado = StringBuilder()
                for (i in texto.indices) {
                    formatado.append(texto[i])
                    if (i == 1) formatado.append(":")
                }
                editHora.setText(formatado)
                editHora.setSelection(formatado.length)
                aplicandoMascaraHora = false
            }
        })

        // Carrega times via coroutine
        lifecycleScope.launch {
            times = TimeRepository().getTodos()
            val nomesTimes = times.map { it.nome }
            val adapterTimes = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, nomesTimes)
            adapterTimes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMandante.adapter = adapterTimes
            spinnerVisitante.adapter = adapterTimes

            // Se for edição, preenche os campos
            val jogoId = arguments?.getString("jogoId") ?: ""
            if (jogoId.isNotEmpty()) {
                jogoAtual = viewModel.getPorIdFirestore(jogoId)
                jogoAtual?.let { jogo ->
                    val indexMandante = times.indexOfFirst { it.id == jogo.timeMandanteId }
                    val indexVisitante = times.indexOfFirst { it.id == jogo.timeVisitanteId }
                    if (indexMandante >= 0) spinnerMandante.setSelection(indexMandante)
                    if (indexVisitante >= 0) spinnerVisitante.setSelection(indexVisitante)
                    editData.setText(jogo.data)
                    editHora.setText(jogo.hora)
                }
            }
        }

        view.findViewById<Button>(R.id.btnSalvarJogo).setOnClickListener { salvar() }
        view.findViewById<Button>(R.id.btnCancelarJogo).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun salvar() {
        if (times.isEmpty()) {
            Toast.makeText(requireContext(), "Cadastre times primeiro!", Toast.LENGTH_SHORT).show()
            return
        }

        val mandante = times[spinnerMandante.selectedItemPosition]
        val visitante = times[spinnerVisitante.selectedItemPosition]
        val data = editData.text.toString().trim()
        val hora = editHora.text.toString().trim()

        if (mandante.id == visitante.id) {
            Toast.makeText(requireContext(), "Escolha times diferentes!", Toast.LENGTH_SHORT).show()
            return
        }
        if (data.length < 10) {
            editData.error = "Informe a data completa"
            return
        }
        if (hora.length < 5) {
            editHora.error = "Informe a hora completa"
            return
        }

        val jogo = JogoEntity(
            id = jogoAtual?.id ?: "",
            timeMandanteId = mandante.id,
            timeVisitanteId = visitante.id,
            timeMandanteNome = mandante.nome,
            timeVisitanteNome = visitante.nome,
            data = data,
            hora = hora.ifEmpty { "00:00" },
            placarMandante = jogoAtual?.placarMandante ?: 0,
            placarVisitante = jogoAtual?.placarVisitante ?: 0,
            status = jogoAtual?.status ?: "Agendado",
            horarioInicio = jogoAtual?.horarioInicio ?: "",
            horarioIntervalo = jogoAtual?.horarioIntervalo ?: "",
            horarioFim = jogoAtual?.horarioFim ?: ""
        )

        if (jogoAtual != null) {
            viewModel.atualizar(jogo)
            Toast.makeText(requireContext(), "Jogo atualizado!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.inserir(jogo)
            Toast.makeText(requireContext(), "Jogo agendado!", Toast.LENGTH_SHORT).show()
        }
        findNavController().popBackStack()
    }
}