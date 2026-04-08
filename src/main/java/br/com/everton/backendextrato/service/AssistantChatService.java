package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.AssistantChatMessageRequest;
import br.com.everton.backendextrato.dto.AssistantChatResponse;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AssistantChatService {

    public AssistantChatResponse reply(String userName, String ownerEmail, List<AssistantChatMessageRequest> messages) {
        String latestMessage = extractLatestUserMessage(messages);
        String normalizedMessage = normalize(latestMessage);
        String ownerContext = ownerEmail != null && !ownerEmail.isBlank()
                ? " no contexto de " + ownerEmail
                : "";

        if (normalizedMessage.isBlank()) {
            return new AssistantChatResponse(
                    buildWelcomeMessage(userName, ownerContext),
                    defaultSuggestions(),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "dashboard", "saldo", "painel", "visao geral")) {
            return new AssistantChatResponse(
                    "O dashboard consolida entradas, contas e dividas do escopo ativo" + ownerContext
                            + ". Ele destaca entradas do mes, contas previstas, dividas abertas, saldo projetado e distribuicao por categorias para leitura rapida do caixa.",
                    List.of(
                            "Como funciona o saldo projetado?",
                            "Quais dados o dashboard carrega em paralelo?",
                            "Como o escopo compartilhado afeta o dashboard?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "entrada", "receita", "renda", "clt", "pj")) {
            return new AssistantChatResponse(
                    "O modulo de entradas permite cadastrar receitas recorrentes por tipo, hoje com foco em CLT e PJ. Cada entrada pode ter vigencia por meses, dias de recebimento e tratamento de imposto para leitura liquida do valor no produto.",
                    List.of(
                            "Como a taxa de imposto entra no calculo?",
                            "Quais campos existem em uma entrada?",
                            "Como isso aparece no mobile?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "conta", "vencimento", "categoria de conta", "bills")) {
            return new AssistantChatResponse(
                    "As contas recorrentes organizam despesas mensais com descricao, valor, dia de pagamento, categoria e meses de vigencia. Esse modulo sustenta previsao, alertas de vencimento e analise por categoria no dashboard.",
                    List.of(
                            "Como funcionam as categorias de conta?",
                            "Como as notificacoes usam o vencimento?",
                            "Quais partes desse fluxo ja estao no backend?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "divida", "passivo", "categoria de divida", "debts")) {
            return new AssistantChatResponse(
                    "O modulo de dividas registra passivos abertos por descricao, valor e categoria. Ele ajuda a enxergar concentracao de risco por grupo e hoje entra no painel consolidado como total de dividas abertas e distribuicao por categoria.",
                    List.of(
                            "Como priorizar dividas no produto?",
                            "Como as categorias de divida sao gerenciadas?",
                            "O sistema ja possui quitacao parcial?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "compartilh", "acesso", "owner", "escopo", "leitura")) {
            return new AssistantChatResponse(
                    "O produto suporta compartilhamento de leitura entre usuarios. O owner ativo altera o contexto de leitura dos modulos suportados, e o backend resolve esse owner para garantir que apenas perfis autorizados consultem dados compartilhados.",
                    List.of(
                            "Como alternar entre meu perfil e um perfil compartilhado?",
                            "Quais telas respeitam ownerEmail?",
                            "Quais permissoes ainda faltam evoluir?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "notific", "push", "venc", "alerta")) {
            return new AssistantChatResponse(
                    "O Extrato possui infraestrutura para subscriptions web push e mobile push. O objetivo principal e lembrar vencimentos e permitir testes de notificacao, reduzindo atraso em pagamentos e aumentando recorrencia de uso.",
                    List.of(
                            "Como testar notificacoes no projeto?",
                            "Qual a diferenca entre web push e mobile push aqui?",
                            "Que endpoint dispara notificacoes?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "projec", "simul", "parcel", "compra futura")) {
            return new AssistantChatResponse(
                    "O modulo de projecoes simula compras futuras considerando renda mensal, gastos fixos, entrada, parcelas, juros e prioridade. Pelo estado atual, ele vive no frontend com persistencia local por usuario e escopo, sem backend dedicado.",
                    List.of(
                            "Quais partes de projecoes ainda nao foram para o backend?",
                            "Como o modulo identifica meses criticos?",
                            "Vale migrar projecoes para a API?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "pf/pj", "pf pj", "banco", "separacao", "reembolso")) {
            return new AssistantChatResponse(
                    "O modulo PF/PJ ajuda a separar caixa pessoal e empresarial. Ele permite registrar misturas, transferencias, reembolsos, comprovantes e pendencias operacionais, mas hoje tambem funciona com armazenamento local no frontend.",
                    List.of(
                            "Como esse modulo apoia o fechamento mensal?",
                            "Quais dados ainda estao so no navegador?",
                            "Qual seria o proximo passo para levar PF/PJ ao backend?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "empresa", "cnpj", "contador", "faturamento")) {
            return new AssistantChatResponse(
                    "A area de empresa centraliza cadastro fiscal, contato, cobranca, PIX, banco principal, endereco e observacoes operacionais. Diferente de projecoes e PF/PJ, esse modulo ja possui persistencia no backend e respeita o escopo ativo.",
                    List.of(
                            "Quais campos da empresa ja sao persistidos?",
                            "Como o owner compartilhado funciona nessa tela?",
                            "Como integrar isso em um assistente de AI?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "nota fiscal", "notas fiscais", "invoice", "faturamento")) {
            return new AssistantChatResponse(
                    "O projeto ja inclui um modulo de notas fiscais e importacao, alinhado ao fluxo PJ. Ele complementa a organizacao financeira com dados de faturamento e pode virar uma boa fonte de contexto para respostas futuras de AI no backend.",
                    List.of(
                            "Como o modulo de notas fiscais se conecta com empresa?",
                            "Que contexto financeiro a AI poderia extrair daqui?",
                            "Quais endpoints existem para notas fiscais?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "mobile", "voz", "whatsapp", "expo")) {
            return new AssistantChatResponse(
                    "O app mobile cobre login, dashboard, entradas, contas, dividas, categorias e notificacoes push. Um diferencial importante e a criacao por voz, que transforma fala em rascunho estruturado antes da confirmacao do usuario.",
                    List.of(
                            "Como funciona a captura por voz?",
                            "Quais modulos existem no app mobile?",
                            "O backend ja suporta esse fluxo de voz?"
                    ),
                    "contextual-backend"
            );
        }

        if (containsAny(normalizedMessage, "backend", "ia", "ai", "llm", "openai", "assistente")) {
            return new AssistantChatResponse(
                    "Para evoluir isso para AI real no backend, o melhor caminho e manter este endpoint como contrato estavel e substituir a geracao deterministica por um servico de LLM. O contexto inicial pode incluir PRD, modulos suportados, owner ativo, permissoes e, numa segunda fase, dados consolidados do usuario para respostas mais personalizadas.",
                    List.of(
                            "Que contexto enviar para o modelo?",
                            "Como separar respostas sobre produto e respostas com dados do usuario?",
                            "Como plugar OpenAI sem quebrar a UI?"
                    ),
                    "contextual-backend"
            );
        }

        return new AssistantChatResponse(
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

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).trim();
    }
}
