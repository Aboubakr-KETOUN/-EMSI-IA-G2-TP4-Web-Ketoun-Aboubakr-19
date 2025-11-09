package ma.emsi.ketoun.tp4_web_aboubakr_ketoun_19.LLM;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import ma.emsi.ketoun.tp4_web_aboubakr_ketoun_19.Interfaces.Assistant;

public class LlmClient {
    String systemRole;
    Assistant assistant;
    ChatMemory memory;



    public LlmClient(){
        String llmKey = System.getenv("GEMINI_Key");
        if (llmKey == null) {
            System.err.println("No Key!");
            return;
        }
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(llmKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.3)
                .build();

        this.memory = MessageWindowChatMemory.withMaxMessages(10);
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(memory)
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

