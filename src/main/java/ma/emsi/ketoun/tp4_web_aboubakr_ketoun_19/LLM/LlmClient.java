package ma.emsi.ketoun.tp4_web_aboubakr_ketoun_19.LLM;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import ma.emsi.ketoun.tp4_web_aboubakr_ketoun_19.Interfaces.Assistant;
import dev.langchain4j.store.embedding.EmbeddingStore;
import ma.emsi.ketoun.tp4_web_aboubakr_ketoun_19.LoggerConfig.LoggerConfig;
import ma.emsi.ketoun.tp4_web_aboubakr_ketoun_19.RAG.EmbeddingStoreCreator;
import java.util.HashMap;
import java.util.Map;

public class LlmClient {
    String systemRole;
    Assistant assistant;
    ChatMemory memory;



    public LlmClient(){
        LoggerConfig.configureLogger();
        String llmKey = System.getenv("GEMINI_KEY");
        if (llmKey == null) {
            System.err.println("No Key!");
            return;
        }

        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(llmKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.3)
                .logRequestsAndResponses(true)
                .build();

        this.memory = MessageWindowChatMemory.withMaxMessages(10);

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore1 = EmbeddingStoreCreator.creerEmbeddingStore("/rag.pdf", embeddingModel);
        EmbeddingStore<TextSegment> embeddingStore2 = EmbeddingStoreCreator.creerEmbeddingStore("/autre.pdf", embeddingModel);

        ContentRetriever contentRetriever1 = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore1)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.5)
                .build();

        ContentRetriever contentRetriever2 = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore2)
                .embeddingModel(embeddingModel)
                .maxResults(2)
                .minScore(0.5)
                .build();

        Map<ContentRetriever, String> retrieverDescriptions = new HashMap<>();
        retrieverDescriptions.put(
                contentRetriever1,
                "Ce retriever contient des informations sur l'intelligence artificielle, le RAG (Retrieval Augmented Generation), les embeddings et les modèles de langage."
        );
        retrieverDescriptions.put(
                contentRetriever2,
                "Ce retriever contient des informations sur une certif oracle."
        );

        QueryRouter queryRouter = new LanguageModelQueryRouter(model, retrieverDescriptions);

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .build();

        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(memory)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
    }

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
        this.memory.clear();
        this.memory.add(SystemMessage.from(systemRole));
    }

    public String PoserQuestion(String question) {
        try {
            return assistant.chat(question);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}

