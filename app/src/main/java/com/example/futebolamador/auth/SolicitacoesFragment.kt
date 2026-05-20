package com.example.futebolamador.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futebolamador.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SolicitacoesFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_solicitacoes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialToolbar>(R.id.toolbarSolicitacoes)
            .setNavigationOnClickListener { findNavController().popBackStack() }

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerSolicitacoes)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        carregarSolicitacoes(recycler)
    }

    private fun carregarSolicitacoes(recycler: RecyclerView) {
        lifecycleScope.launch {
            try {
                val resultado = db.collection("solicitacoes")
                    .whereEqualTo("status", "pendente")
                    .get().await()

                val solicitacoes = resultado.documents

                if (solicitacoes.isEmpty()) {
                    Toast.makeText(requireContext(),
                        "Nenhuma solicitação pendente", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                recycler.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                        object : RecyclerView.ViewHolder(
                            LayoutInflater.from(parent.context)
                                .inflate(R.layout.item_solicitacao, parent, false)
                        ) {}

                    override fun getItemCount() = solicitacoes.size

                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        val doc = solicitacoes[position]
                        holder.itemView.findViewById<TextView>(R.id.txtNomeSolicitante).text =
                            doc.getString("nome") ?: "Sem nome"
                        holder.itemView.findViewById<TextView>(R.id.txtEmailSolicitante).text =
                            doc.getString("email") ?: ""

                        holder.itemView.findViewById<Button>(R.id.btnAprovar).setOnClickListener {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Aprovar")
                                .setMessage("Aprovar ${doc.getString("nome")} como comissão técnica?")
                                .setPositiveButton("Sim") { _, _ ->
                                    lifecycleScope.launch {
                                        val uid = doc.getString("uid") ?: return@launch
                                        // Atualiza perfil do usuário
                                        db.collection("usuarios").document(uid)
                                            .update("perfil", "comissao").await()
                                        // Atualiza status da solicitação
                                        db.collection("solicitacoes").document(doc.id)
                                            .update("status", "aprovado").await()
                                        Toast.makeText(requireContext(),
                                            "Conta aprovada!", Toast.LENGTH_SHORT).show()
                                        carregarSolicitacoes(recycler)
                                    }
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        }

                        holder.itemView.findViewById<Button>(R.id.btnRejeitar).setOnClickListener {
                            AlertDialog.Builder(requireContext())
                                .setTitle("Rejeitar")
                                .setMessage("Rejeitar solicitação de ${doc.getString("nome")}?")
                                .setPositiveButton("Sim") { _, _ ->
                                    lifecycleScope.launch {
                                        db.collection("solicitacoes").document(doc.id)
                                            .update("status", "rejeitado").await()
                                        Toast.makeText(requireContext(),
                                            "Solicitação rejeitada.", Toast.LENGTH_SHORT).show()
                                        carregarSolicitacoes(recycler)
                                    }
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao carregar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}