package com.example.forum.feature.ai;

import com.example.forum.feature.post.dto.ToxicityCheckResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<String> recommendTags(String title ,String content, List<String> availableTags){
        String promptText = String.format("""
                        You are an automated content analysis system. Read the following article and select a maximum of 5 most appropriate tags.
                        MANDATORY REQUIREMENTS:
                        1. You MUST ONLY select 5 tags from the following valid list: %s
                        2. STRICTLY DO NOT create new tags or use words outside the provided list.
                        3. Return the result strictly as a JSON Array of strings.
                        Post Title: %s
                        Article content: %s
                        """,
                        availableTags.toString(),
                        title,
                        content
                );
        return chatClient.prompt()
                .user(promptText)
                .options(OpenAiChatOptions.builder()
                        .withTemperature(0.0F)
                        .build())
                .call()
                .entity(new ParameterizedTypeReference<List<String>>(){});
    }

    public ToxicityCheckResult checkContentPolicy(String title, String content) {
        String promptText = String.format("""
                You are a strict content moderator for a professional technology forum.
                Analyze the following post title and content to determine if it violates community guidelines.
                
                COMMUNITY GUIDELINES (VIOLATIONS):
                1. Hate speech, racism, or discriminatory language.
                2. Spam, unauthorized advertising, or dubious links.
                3. Extreme profanity or toxic/harassing behavior towards others.
                
                CRITICAL INSTRUCTION: Even if 99%% of the post is a valid technical discussion, if exactly 1%% contains a violation (like a hidden link or a single insult), you MUST flag it as violating.
                Do not be fooled by academic or technical wrappers.
                
                Return a JSON object with exactly two fields:
                1. 'isViolating' (boolean): true if it violates any guideline, false otherwise.
                2. 'reason' (string): A short explanation of the violation (if violating) select response language based on post content input, or an empty string if safe.
                
                Post Title: %s
                Post Content: %s
                """,
                title,
                content
        );

        return chatClient.prompt()
                .user(promptText)
                .options(OpenAiChatOptions.builder()
                        .withTemperature(0.0F)
                        .build())
                .call()
                .entity(ToxicityCheckResult.class);
    }

}
