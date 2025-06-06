package com.enterprise.agents.slack.service;

import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.slack.model.SlackOAuthToken;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.Conversation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlackService {
    private final SlackOAuthTokenRepository tokenRepository;

    public void saveToken(SlackOAuthToken token) {
        tokenRepository.save(token);
    }

    public boolean isConnected(String enterpriseId) {
        return tokenRepository.findByEnterpriseId(enterpriseId).isPresent();
    }

    public List<Conversation> getChannels(String enterpriseId) {
        SlackOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "Slack not connected"));

        try {
            MethodsClient client = Slack.getInstance().methods(token.getBotAccessToken());
            ConversationsListResponse response = client.conversationsList(
                    ConversationsListRequest.builder()
                            .types("public_channel,private_channel")
                            .build()
            );

            if (!response.isOk()) {
                throw new OAuthException("api_error", response.getError());
            }

            return response.getChannels();
        } catch (IOException | SlackApiException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public void sendMessage(String enterpriseId, String channelId, String message) {
        SlackOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "Slack not connected"));

        try {
            MethodsClient client = Slack.getInstance().methods(token.getBotAccessToken());
            ChatPostMessageResponse response = client.chatPostMessage(
                    ChatPostMessageRequest.builder()
                            .channel(channelId)
                            .text(message)
                            .build()
            );

            if (!response.isOk()) {
                throw new OAuthException("api_error", response.getError());
            }
        } catch (IOException | SlackApiException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }
} 