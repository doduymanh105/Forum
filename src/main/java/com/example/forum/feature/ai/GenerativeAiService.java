package com.example.forum.feature.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class GenerativeAiService {

    private final ChatClient chatClient;

    public GenerativeAiService(ChatClient.Builder chatClientBuilder){
        this.chatClient=chatClientBuilder.build();
    }

    public String summarizeText(String context){
        String prompt = String.format(
                """
                        You are an AI assistant for a forum.
                                Read the following text and summarize its core content.
                                FORMATTING REQUIREMENTS:
                                - Return the result entirely in Markdown format.
                                - Summarize the main content by dividing it into clear and concise bullet points.
                                - Use **bold** for important keywords, concepts, or key points.
                                - Focus only on the essential information.
                                - Do not add information that is not present in the original text.
                                Text: %s
                        """
                , context
        );
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

}
