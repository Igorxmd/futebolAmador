package com.example.futebolamador.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.futebolamador.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CadastroFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_cadastro, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        view.findViewById<MaterialToolbar>(R.id.toolbarCadastro)
            .setNavigationOnClickListener { findNavController().popBackStack() }

        val editNome = view.findViewById<EditText>(R.id.editNome)
        val editEmail = view.findViewById<EditText>(R.id.editEmail)
        val editSenha = view.findViewById<EditText>(R.id.editSenha)
        val editConfirmar = view.findViewById<EditText>(R.id.editConfirmarSenha)

        view.findViewById<Button>(R.id.btnCriarConta).setOnClickListener {
            val nome = editNome.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()
            val confirmar = editConfirmar.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (senha != confirmar) {
                Toast.makeText(requireContext(), "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (senha.length < 6) {
                Toast.makeText(requireContext(), "Senha deve ter ao menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, senha)
                .addOnSuccessListener { result ->
                    val user = result.user!!
                    // Salva o usuário no Firestore com perfil "torcedor"
                    FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(user.uid)
                        .set(mapOf(
                            "nome" to nome,
                            "email" to email,
                            "perfil" to "torcedor"
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Conta criada!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_cadastroFragment_to_timesFragment)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Erro: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}