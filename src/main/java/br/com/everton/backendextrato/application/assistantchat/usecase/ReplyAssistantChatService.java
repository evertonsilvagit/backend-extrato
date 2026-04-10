package br.com.everton.backendextrato.application.assistantchat.usecase;

import br.com.everton.backendextrato.application.assistantchat.port.in.ReplyAssistantChatUseCase;
import br.com.everton.backendextrato.domain.assistantchat.AssistantReply;
import br.com.everton.backendextrato.dto.AssistantChatMessageRequest;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ReplyAssistantChatService implements ReplyAssistantChatUseCase {

    @Override
    public AssistantReply execute(String userName, String ownerEmail, List<AssistantChatMessageRequest> messages) {
        String latestMessage = extractLatestUserMessage(messages);
        String normalizedMessage = normalize(latestMessage);
        String ownerContext = ownerEmail != null && !ownerEmail.isBlank()
                ? " no contexto de " + ownerEmail
                : "";

        if (normalizedMessage.isBlank()) {
            return new AssistantReply(buildWelcomeMessage(userName, ownerContext), defaultSuggestions(), "contextual-backend");
        }

        if (containsAny(normalizedMessage, "dashboard", "saldo", "painel", "visao geral")) {
            return new AssistantReply(
                    "O dashboard consolida entradas, contas e dividas do escopo ativo" + ownerContext
                            + ". Ele destaca entradas do mes, contas previstas, dividas abertas, saldo projetado e distribuicao por categorias para leitura rapida do caixa.",
                    List.of("Como funciona o saldo projetado?", "Quais dados o dashboard carrega em paralelo?", "Como o escopo compartilhado afeta o dashboard?"),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "entrada", "receita", "renda", "clt", "pj")) {
            return new AssistantReply(
                    "O modulo de entradas permite cadastrar receitas recorrentes por tipo, hoje com foco em CLT e PJ. Cada entrada pode ter vigencia por meses, dias de recebimento e tratamento de imposto para leitura liquida do valor no produto.",
                    List.of("Como a taxa de imposto entra no calculo?", "Quais campos existem em uma entrada?", "Como isso aparece no mobile?"),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "conta", "vencimento", "categoria de conta", "bills")) {
            return new AssistantReply(
                    "As contas recorrentes organizam despesas mensais com descricao, valor, dia de pagamento, categoria e meses de vigencia. Esse modulo sustenta previsao, alertas de vencimento e analise por categoria no dashboard.",
                    List.of("Como funcionam as categorias de conta?", "Como as notificacoes usam o vencimento?", "Quais partes desse fluxo ja estao no backend?"),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "divida", "passivo", "categoria de divida", "debts")) {
            return new AssistantReply(
                    "O modulo de dividas registra passivos abertos por descricao, valor e categoria. Ele ajuda a enxergar concentracao de risco por grupo e hoje entra no painel consolidado como total de dividas abertas e distribuicao por categoria.",
                    List.of("Como priorizar dividas no produto?", "Como as categorias de divida sao gerenciadas?", "O sistema ja possui quitacao parcial?"),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "compartilh", "acesso", "owner", "escopo", "leitura")) {
            return new AssistantReply(
                    "O produto suporta compartilhamento de leitura entre usuarios. O owner ativo altera o contexto de leitura dos modulos suportados, e o backend resolve esse owner para garantir que apenas perfis autorizados consultem dados compartilhados.",
                    List.of("Como alternar entre meu perfil e um perfil compartilhado?", "Quais telas respeitam ownerEmail?", "Quais permissoes ainda faltam evoluir?"),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "notific", "push", "venc", "alerta")) {
            return new AssistantReply(
                    "O Extrato possui infraestrutura para subscriptions web push e mobile push. O objetivo principal e lembrar vencimentos e permitir testes de notificacao, reduzindo atraso em pagamentos e aumentando recorrencia de uso.",
                    List.of("Como testar notificacoes no projeto?", "Qual a diferenca entre web push e mobile push aqui?", "Que endpoint dispara notificacoes?"),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "backend", "ia", "ai", "llm", "openai", "assistente")) {
            return new AssistantReply(
                    "Para evoluir isso para AI real no backend, o melhor caminho e manter este endpoint como contrato estavel e substituir a geracao deterministica por um servico de LLM. O contexto inicial pode incluir PRD, modulos suportados, owner ativo, permissoes e, numa segunda fase, dados consolidados do usuario para respostas mais personalizadas.",
                    List.of("Que contexto enviar para o modelo?", "Como separar respostas sobre produto e respostas com dados do usuario?", "Como plugar OpenAI sem quebrar a UI?"),
                    "contextual-backend"
            );
        }

        return new AssistantReply(
                "Posso ajudar a navegar pelas funcionalidades do Extrato e tambem pela estrategia de AI no backend. Hoje eu conheco o mapa funcional do projeto: autenticacao, dashboard, entradas, contas, dividas, acessos compartilhados, notificacoes, projecoes, PF/PJ, empresa, notas fiscais e mobile com voz.",
                defaultSuggestions(),
                "contextual-backend"
        );
    }

    private String buildWelcomeMessage(String userName, String ownerContext) {
        String greeting = (userName != null && !userName.isBlank()) ? "Oi, " + userName + ". " : "Oi. ";
        return greeting
                + "Este chat ja esta ligado ao backend do Extrato e pronto para evoluir para um provedor de AI. "
                + "Por enquanto ele responde com base nas funcionalidades reais do projeto"
                + ownerContext
                + " e pode te orientar sobre dashboard, entradas, contas, dividas, empresa, acessos, notificacoes, projecoes, PF/PJ, notas fiscais e mobile.";
    }

    private List<String> defaultSuggestions() {
        return List.of(
                "Explique todas as funcionalidades do projeto",
                "Como integrar um modelo de AI real nesse backend?",
                "Quais modulos hoje ainda vivem so no frontend?"
        );
    }

    private String extractLatestUserMessage(List<AssistantChatMessageRequest> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        Optional<AssistantChatMessageRequest> latestUserMessage = messages.stream()
                .filter(message -> message != null && "user".equalsIgnoreCase(message.role()))
                .reduce((first, second) -> second);

        return latestUserMessage.map(AssistantChatMessageRequest::content).orElse("");
    }

    private boolean containsAny(String input, String... keywords) {
        for (String keyword : keywords) {
            if (input.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }
}
