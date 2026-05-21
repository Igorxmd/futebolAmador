package com.example.futebolamador.ui.jogos

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.futebolamador.R
import com.example.futebolamador.auth.SessaoManager
import com.example.futebolamador.repository.ApostaRepository
import com.example.futebolamador.repository.JogoRepository
import com.example.futebolamador.repository.TimeRepository
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class ApostasJogoFragment : Fragment() {

    private val apostaRepo = ApostaRepository()
    private val jogoRepo = JogoRepository()
    private val timeRepo = TimeRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_apostas_jogo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val jogoId = arguments?.getString("jogoId") ?: return

        view.findViewById<MaterialToolbar>(R.id.toolbarApostasJogo)
            .setNavigationOnClickListener { findNavController().popBackStack() }

        lifecycleScope.launch {
            val jogo = jogoRepo.getPorId(jogoId) ?: return@launch
            val dados = SessaoManager.dadosUsuario()
            val nomeUsuario = dados["nome"] ?: "Usuário"

            // Preenche dados do jogo
            view.findViewById<TextView>(R.id.txtDataJogoAposta).text =
                "${jogo.data} às ${jogo.hora}"
            view.findViewById<TextView>(R.id.txtMandanteAposta).text = jogo.timeMandanteNome
            view.findViewById<TextView>(R.id.txtVisitanteAposta).text = jogo.timeVisitanteNome

            // Define texto dos botões de aposta
            view.findViewById<Button>(R.id.btnApostarMandante).text = jogo.timeMandanteNome
            view.findViewById<Button>(R.id.btnApostarVisitante).text = jogo.timeVisitanteNome

            // Carrega brasões
            val times = timeRepo.getTodos()
            val timeMandante = times.find { it.id == jogo.timeMandanteId }
            val timeVisitante = times.find { it.id == jogo.timeVisitanteId }

            timeMandante?.let {
                if (it.brasaoUri.isNotEmpty()) {
                    try {
                        view.findViewById<ImageView>(R.id.imgMandanteAposta)
                            .setImageURI(Uri.parse(it.brasaoUri))
                    } catch (e: Exception) {}
                }
            }
            timeVisitante?.let {
                if (it.brasaoUri.isNotEmpty()) {
                    try {
                        view.findViewById<ImageView>(R.id.imgVisitanteAposta)
                            .setImageURI(Uri.parse(it.brasaoUri))
                    } catch (e: Exception) {}
                }
            }

            // Carrega contagem de palpites
            atualizarContagem(view, jogoId, jogo.timeMandanteNome, jogo.timeVisitanteNome)

            // Carrega palpite atual do usuário
            val meuPalpite = apostaRepo.meuPalpite(jogoId)
            val txtMeuPalpite = view.findViewById<TextView>(R.id.txtMeuPalpiteAtual)
            val btnCancelar = view.findViewById<Button>(R.id.btnCancelarAposta)

            if (meuPalpite != null) {
                val nomePalpite = when (meuPalpite.palpite) {
                    "mandante" -> jogo.timeMandanteNome
                    "visitante" -> jogo.timeVisitanteNome
                    else -> "Empate"
                }
                txtMeuPalpite.text = "✓ Seu palpite: $nomePalpite"
                txtMeuPalpite.visibility = View.VISIBLE
                btnCancelar.visibility = View.VISIBLE
            }

            // Lógica por status do jogo
            val cardApostar = view.findViewById<View>(R.id.cardApostar)
            val cardResultado = view.findViewById<View>(R.id.cardResultado)

            when (jogo.status) {
                "Agendado" -> {
                    cardApostar.visibility = View.VISIBLE
                    cardResultado.visibility = View.GONE

                    view.findViewById<Button>(R.id.btnApostarMandante).setOnClickListener {
                        fazerAposta(view, jogoId, "mandante", nomeUsuario,
                            jogo.timeMandanteNome, jogo.timeVisitanteNome)
                    }
                    view.findViewById<Button>(R.id.btnApostarEmpate).setOnClickListener {
                        fazerAposta(view, jogoId, "empate", nomeUsuario,
                            jogo.timeMandanteNome, jogo.timeVisitanteNome)
                    }
                    view.findViewById<Button>(R.id.btnApostarVisitante).setOnClickListener {
                        fazerAposta(view, jogoId, "visitante", nomeUsuario,
                            jogo.timeMandanteNome, jogo.timeVisitanteNome)
                    }
                    btnCancelar.setOnClickListener {
                        lifecycleScope.launch {
                            apostaRepo.cancelarAposta(jogoId)
                            txtMeuPalpite.visibility = View.GONE
                            btnCancelar.visibility = View.GONE
                            Toast.makeText(requireContext(),
                                "Palpite cancelado", Toast.LENGTH_SHORT).show()
                            atualizarContagem(view, jogoId,
                                jogo.timeMandanteNome, jogo.timeVisitanteNome)
                        }
                    }
                }
                "Concluído" -> {
                    cardApostar.visibility = View.GONE
                    cardResultado.visibility = View.VISIBLE

                    val resultadoReal = apostaRepo.calcularResultado(
                        jogo.placarMandante, jogo.placarVisitante)

                    val nomeResultado = when (resultadoReal) {
                        "mandante" -> jogo.timeMandanteNome
                        "visitante" -> jogo.timeVisitanteNome
                        else -> "Empate"
                        // Atribui pontos se ainda não foram atribuídos
                        if (meuPalpite != null && !meuPalpite.pontosAtribuidos) {
                            apostaRepo.verificarEAtribuirPontos(
                                aposta = meuPalpite,
                                resultadoReal = resultadoReal,
                                onPontosAtribuidos = {
                                    SessaoManager.adicionarPontos(10)
                                }
                            )
                        }
                    }

                    if (meuPalpite != null) {
                        val acertou = meuPalpite.palpite == resultadoReal
                        view.findViewById<TextView>(R.id.txtResultadoFinal).text =
                            if (acertou) "🎉" else "😔"
                        view.findViewById<TextView>(R.id.txtResultadoMensagem).text =
                            if (acertou) "Você acertou!" else "Você errou!"
                        view.findViewById<TextView>(R.id.txtSeuPalpiteResultado).text =
                            "Resultado: $nomeResultado • Seu palpite: ${
                                when (meuPalpite.palpite) {
                                    "mandante" -> jogo.timeMandanteNome
                                    "visitante" -> jogo.timeVisitanteNome
                                    else -> "Empate"
                                }
                            }"
                    } else {
                        view.findViewById<TextView>(R.id.txtResultadoFinal).text = "⚽"
                        view.findViewById<TextView>(R.id.txtResultadoMensagem).text =
                            "Resultado: $nomeResultado"
                        view.findViewById<TextView>(R.id.txtSeuPalpiteResultado).text =
                            "Você não fez palpite neste jogo"
                    }
                }
                else -> {
                    // Em andamento ou intervalo
                    cardApostar.visibility = View.GONE
                    cardResultado.visibility = View.VISIBLE
                    view.findViewById<TextView>(R.id.txtResultadoFinal).text = "⏱"
                    view.findViewById<TextView>(R.id.txtResultadoMensagem).text =
                        "Jogo em andamento"
                    view.findViewById<TextView>(R.id.txtSeuPalpiteResultado).text =
                        "Aguarde o resultado final"
                }
            }
        }
    }

    private fun fazerAposta(
        view: View, jogoId: String, palpite: String,
        nomeUsuario: String, nomeMandante: String, nomeVisitante: String
    ) {
        lifecycleScope.launch {
            val sucesso = apostaRepo.apostar(jogoId, palpite, nomeUsuario)
            if (sucesso) {
                val nomePalpite = when (palpite) {
                    "mandante" -> nomeMandante
                    "visitante" -> nomeVisitante
                    else -> "Empate"
                }
                val txtMeuPalpite = view.findViewById<TextView>(R.id.txtMeuPalpiteAtual)
                txtMeuPalpite.text = "✓ Seu palpite: $nomePalpite"
                txtMeuPalpite.visibility = View.VISIBLE
                view.findViewById<Button>(R.id.btnCancelarAposta).visibility = View.VISIBLE
                Toast.makeText(requireContext(),
                    "Palpite registrado: $nomePalpite", Toast.LENGTH_SHORT).show()
                atualizarContagem(view, jogoId, nomeMandante, nomeVisitante)
            }
        }
    }

    private suspend fun atualizarContagem(
        view: View, jogoId: String, nomeMandante: String, nomeVisitante: String
    ) {
        val contagem = apostaRepo.contarPalpites(jogoId)
        view.findViewById<TextView>(R.id.txtCountMandante).text =
            contagem["mandante"].toString()
        view.findViewById<TextView>(R.id.txtCountEmpate).text =
            contagem["empate"].toString()
        view.findViewById<TextView>(R.id.txtCountVisitante).text =
            contagem["visitante"].toString()
        view.findViewById<TextView>(R.id.txtNomeMandanteCount).text = nomeMandante
        view.findViewById<TextView>(R.id.txtNomeVisitanteCount).text = nomeVisitante
    }
}