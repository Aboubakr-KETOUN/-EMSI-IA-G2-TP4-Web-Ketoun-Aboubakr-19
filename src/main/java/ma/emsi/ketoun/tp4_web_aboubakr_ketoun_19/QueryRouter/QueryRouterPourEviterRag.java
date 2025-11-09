package ma.emsi.ketoun.tp4_web_aboubakr_ketoun_19.QueryRouter;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.QueryRouter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QueryRouterPourEviterRag implements QueryRouter {

    ChatModel model;
    ContentRetriever contentRetriever;

    public QueryRouterPourEviterRag(ChatModel model, ContentRetriever contentRetriever) {
        this.model = model;
        this.contentRetriever = contentRetriever;
    }

    private final PromptTemplate promptTemplate = PromptTemplate.from(
            "Est-ce que la requête '{{requete}}' porte sur l'IA ? " +
                    "Réponds seulement par 'oui', 'non' ou 'peut-être'."
    );

    @Override
    public List<ContentRetriever> route(Query query) {

        String prompt = promptTemplate.apply(Map.of("requete", query.text())).text();
        String response = model.chat(prompt);

        if (response.toLowerCase().contains("non")) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(contentRetriever);
        }
    }
}