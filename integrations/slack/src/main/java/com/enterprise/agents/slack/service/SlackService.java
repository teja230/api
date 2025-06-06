package com.enterprise.agents.slack.service;

import com.enterprise.agents.common.exception.OAuthException;
import com.enterprise.agents.slack.model.SlackOAuthToken;
import com.enterprise.agents.slack.repository.SlackOAuthTokenRepository;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.ConversationType;
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

    public List<String> getChannels(String enterpriseId) {
        SlackOAuthToken token = tokenRepository.findByEnterpriseId(enterpriseId)
                .orElseThrow(() -> new OAuthException("not_connected", "Slack not connected"));
        try {
            MethodsClient client = Slack.getInstance().methods(token.getBotAccessToken());
            ConversationsListResponse response = client.conversationsList(
                    ConversationsListRequest.builder()
                            .types(List.of(ConversationType.PUBLIC_CHANNEL, ConversationType.PRIVATE_CHANNEL))
                            .build()
            );
            if (!response.isOk()) {
                throw new OAuthException("api_error", response.getError());
            }
            // Return channel IDs as List<String>
            return response.getChannels().stream().map(Conversation::getId).toList();
        } catch (IOException | SlackApiException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public boolean sendMessage(String enterpriseId, String channelId, String message) {
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
            return true;
        } catch (IOException | SlackApiException e) {
            throw new OAuthException("api_error", e.getMessage(), e);
        }
    }

    public SlackOAuthToken exchangeCodeForToken(String code, String enterpriseId) {
        // TODO: Implement Slack OAuth token exchange
        return new SlackOAuthToken();
    }

    public SlackOAuthToken refreshToken(String enterpriseId) {
        // TODO: Implement Slack token refresh
        return new SlackOAuthToken();
    }

    public List<String> getUsers(String enterpriseId) {
        // TODO: Implement user list retrieval
        return List.of();
    }
} 