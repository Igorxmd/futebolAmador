package com.example.futebolamador.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.futebolamador.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    private lateinit var imgFoto: ImageView
    private lateinit var editNome: TextInputEditText
    private lateinit var txtEmail: TextView
    private lateinit var txtPerfil: TextView
    private lateinit var cardSolicitacao: View
    private lateinit var cardAdmin: View
    private var fotoUri: String = ""

    private val selecionarFoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                requireContext().contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                fotoUri = it.toString()
                imgFoto.setImageURI(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_perfil, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgFoto = view.findViewById(R.id.imgFotoPerfil)
        editNome = view.findViewById(R.id.editNomePerfil)
        txtEmail = view.findViewById(R.id.txtEmailPerfil)
        txtPerfil = view.findViewById(R.id.txtPerfilAtual)
        cardSolicitacao = view.findViewById(R.id.cardSolicitacao)
        cardAdmin = view.findViewById(R.id.cardAdmin)

        view.findViewById<MaterialToolbar>(R.id.toolbarPerfil)
            .setNavigationOnClickListener { findNavController().popBackStack() }

        view.findViewById<Button>(R.id.btnSelecionarFoto).setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            selecionarFoto.launch(intent)
        }

        // Carrega dados do usuário
        lifecycleScope.launch {
            val dados = SessaoManager.dadosUsuario()
            val perfil = dados["perfil"] ?: "torcedor"

            editNome.setText(dados["nome"])
            txtEmail.text = "E-mail: ${dados["email"]}"
            txtPerfil.text = "Perfil: ${perfil.replaceFirstChar { it.uppercase() }}"

            fotoUri = dados["fotoUri"] ?: ""
            if (fotoUri.isNotEmpty()) {
                try { imgFoto.setImageURI(Uri.parse(fotoUri)) } catch (e: Exception) {}
            }

            // Mostra card de solicitação só para torcedores
            cardSolicitacao.visibility = if (perfil == "torcedor") View.VISIBLE else View.GONE

            // Mostra painel admin só para admins
            cardAdmin.visibility = if (perfil == "admin") View.VISIBLE else View.GONE

            view.findViewById<TextView>(R.id.txtMeusPontos).text = dados["pontos"] ?: "0"
        }

        view.findViewById<Button>(R.id.btnSalvarPerfil).setOnClickListener {
            val nome = editNome.text.toString().trim()
            if (nome.isEmpty()) {
                editNome.error = "Informe seu nome"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                SessaoManager.atualizarPerfil(nome, fotoUri)
                Toast.makeText(requireContext(), "Perfil atualizado!", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.btnSolicitarTecnico).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Solicitar Conta Técnica")
                .setMessage("Deseja enviar uma solicitação para se tornar parte da comissão técnica?")
                .setPositiveButton("Sim") { _, _ ->
                    lifecycleScope.launch {
                        val enviado = SessaoManager.enviarSolicitacao()
                        if (enviado) {
                            Toast.makeText(requireContext(),
                                "Solicitação enviada! Aguarde aprovação.", Toast.LENGTH_LONG).show()
                            view.findViewById<Button>(R.id.btnSolicitarTecnico).isEnabled = false
                            view.findViewById<Button>(R.id.btnSolicitarTecnico).text = "Solicitação enviada"
                        } else {
                            Toast.makeText(requireContext(),
                                "Você já tem uma solicitação pendente.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        view.findViewById<Button>(R.id.btnVerSolicitacoes).setOnClickListener {
            findNavController().navigate(R.id.action_perfilFragment_to_solicitacoesFragment)
        }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Sair")
                .setMessage("Deseja sair da conta?")
                .setPositiveButton("Sim") { _, _ ->
                    SessaoManager.logout()
                    findNavController().navigate(R.id.action_perfilFragment_to_loginFragment)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}