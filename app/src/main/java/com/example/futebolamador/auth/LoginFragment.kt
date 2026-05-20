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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 9001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_login, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Se já está logado, vai direto para os times
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_timesFragment)
            return
        }

        val editEmail = view.findViewById<EditText>(R.id.editEmail)
        val editSenha = view.findViewById<EditText>(R.id.editSenha)

        view.findViewById<Button>(R.id.btnEntrar).setOnClickListener {
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener {
                    findNavController().navigate(R.id.action_loginFragment_to_timesFragment)
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "E-mail ou senha incorretos", Toast.LENGTH_SHORT).show()
                }
        }

        view.findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(requireActivity(), gso)
            startActivityForResult(client.signInIntent, RC_SIGN_IN)
        }

        view.findViewById<Button>(R.id.btnCadastrar).setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_cadastroFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { result ->
                        val user = result.user!!
                        val db = FirebaseFirestore.getInstance()
                        val ref = db.collection("usuarios").document(user.uid)
                        ref.get().addOnSuccessListener { doc ->
                            if (!doc.exists()) {
                                ref.set(mapOf(
                                    "nome" to (user.displayName ?: ""),
                                    "email" to (user.email ?: ""),
                                    "perfil" to "torcedor"
                                ))
                            }
                            findNavController().navigate(R.id.action_loginFragment_to_timesFragment)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Erro ao entrar com Google", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}