package com.example.futebolamador.ui.times

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.futebolamador.R
import com.example.futebolamador.data.TimeEntity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class TimeDetalheFragment : Fragment() {

    private val viewModel: TimeViewModel by viewModels()

    private lateinit var editNome: EditText
    private lateinit var spinnerCidade: Spinner
    private lateinit var layoutOutraCidade: TextInputLayout
    private lateinit var editOutraCidade: EditText
    private lateinit var editDataFundacao: EditText
    private lateinit var imgBrasaoPreview: ImageView
    private lateinit var toolbar: MaterialToolbar

    private var timeAtual: TimeEntity? = null
    private var brasaoUri: String = ""
    private var aplicandoMascara = false

    private val cidades = listOf(
        "Maceió", "Rio Largo", "Marechal Deodoro", "Pilar",
        "São Miguel dos Campos", "Barra de São Miguel",
        "Barra de Santo Antônio", "Messias", "Satuba",
        "Coqueiro Seco", "Santa Luzia do Norte", "Paripueira",
        "Murici", "Atalaia", "Arapiraca", "Outra"
    )

    private val selecionarImagem = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                requireContext().contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                brasaoUri = it.toString()
                imgBrasaoPreview.setImageURI(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_time_detalhe, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editNome = view.findViewById(R.id.editNome)
        spinnerCidade = view.findViewById(R.id.spinnerCidade)
        layoutOutraCidade = view.findViewById(R.id.layoutOutraCidade)
        editOutraCidade = view.findViewById(R.id.editOutraCidade)
        editDataFundacao = view.findViewById(R.id.editDataFundacao)
        imgBrasaoPreview = view.findViewById(R.id.imgBrasaoPreview)
        toolbar = view.findViewById(R.id.toolbar)

        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        // Configura spinner de cidades
        val adapterCidades = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, cidades)
        adapterCidades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCidade.adapter = adapterCidades

        spinnerCidade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                layoutOutraCidade.visibility =
                    if (cidades[pos] == "Outra") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Máscara de data
        editDataFundacao.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (aplicandoMascara) return
                aplicandoMascara = true
                var texto = s.toString().replace("/", "")
                if (texto.length > 8) texto = texto.substring(0, 8)
                val formatado = StringBuilder()
                for (i in texto.indices) {
                    formatado.append(texto[i])
                    if (i == 1 || i == 3) formatado.append("/")
                }
                editDataFundacao.setText(formatado)
                editDataFundacao.setSelection(formatado.length)
                aplicandoMascara = false
            }
        })

        view.findViewById<Button>(R.id.btnSelecionarBrasao).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            selecionarImagem.launch(intent)
        }

        val timeId = arguments?.getString("timeId") ?: ""

        if (timeId.isNotEmpty()) {
            // EDIÇÃO — busca direto do Firestore pelo ID
            toolbar.title = "Editar Time"
            lifecycleScope.launch {
                timeAtual = viewModel.getPorIdFirestore(timeId)
                timeAtual?.let { time ->
                    preencherFormulario(time)
                } ?: run {
                    Snackbar.make(view, "Erro ao carregar time", Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        } else {
            // NOVO TIME
            toolbar.title = "Cadastrar Time"
        }

        view.findViewById<Button>(R.id.btnSalvar).setOnClickListener { salvar(view) }
        view.findViewById<Button>(R.id.btnCancelar).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun preencherFormulario(time: TimeEntity) {
        editNome.setText(time.nome)
        editDataFundacao.setText(time.dataFundacao)
        brasaoUri = time.brasaoUri

        if (brasaoUri.isNotEmpty()) {
            try { imgBrasaoPreview.setImageURI(Uri.parse(brasaoUri)) } catch (e: Exception) {}
        }

        val index = cidades.indexOf(time.cidade)
        if (index >= 0) {
            spinnerCidade.setSelection(index)
        } else {
            spinnerCidade.setSelection(cidades.indexOf("Outra"))
            layoutOutraCidade.visibility = View.VISIBLE
            editOutraCidade.setText(time.cidade)
        }
    }

    private fun salvar(view: View) {
        val nome = editNome.text.toString().trim()
        val cidade = if (spinnerCidade.selectedItem.toString() == "Outra") {
            editOutraCidade.text.toString().trim()
        } else {
            spinnerCidade.selectedItem.toString()
        }
        val dataFundacao = editDataFundacao.text.toString().trim()

        if (nome.isEmpty()) {
            editNome.error = "Informe o nome do time"
            return
        }
        if (cidade.isEmpty()) {
            editOutraCidade.error = "Informe a cidade"
            return
        }

        val time = TimeEntity(
            id = timeAtual?.id ?: "",
            nome = nome,
            cidade = cidade,
            dataFundacao = dataFundacao,
            brasaoUri = brasaoUri
        )

        if (timeAtual != null) {
            viewModel.atualizar(time)
            Snackbar.make(view, "Time atualizado!", Snackbar.LENGTH_SHORT).show()
        } else {
            viewModel.inserir(time)
            Snackbar.make(view, "Time cadastrado!", Snackbar.LENGTH_SHORT).show()
        }

        findNavController().popBackStack()
    }
}