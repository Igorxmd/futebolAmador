package com.example.futebolamador.ui.jogos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futebolamador.R
import com.example.futebolamador.auth.SessaoManager
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class RankingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_ranking, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialToolbar>(R.id.toolbarRanking)
            .setNavigationOnClickListener { findNavController().popBackStack() }

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerRanking)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            val ranking = SessaoManager.getRanking()

            recycler.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    object : RecyclerView.ViewHolder(
                        LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_ranking, parent, false)
                    ) {}

                override fun getItemCount() = ranking.size

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val usuario = ranking[position]
                    val posicao = position + 1

                    holder.itemView.findViewById<TextView>(R.id.txtPosicao).text =
                        when (posicao) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> "#$posicao"
                        }
                    holder.itemView.findViewById<TextView>(R.id.txtNomeRanking).text =
                        usuario["nome"] ?: "Usuário"
                    holder.itemView.findViewById<TextView>(R.id.txtPerfilRanking).text =
                        when (usuario["perfil"]) {
                            "admin" -> "⭐ Admin"
                            "comissao" -> "🏟️ Comissão"
                            else -> "👤 Torcedor"
                        }
                    holder.itemView.findViewById<TextView>(R.id.txtPontosRanking).text =
                        usuario["pontos"] ?: "0"
                }
            }
        }
    }
}