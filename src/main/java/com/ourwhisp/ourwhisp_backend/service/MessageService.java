package com.ourwhisp.ourwhisp_backend.service;

import com.ourwhisp.ourwhisp_backend.exception.ResourceNotFoundException;
import com.ourwhisp.ourwhisp_backend.model.Message;
import com.ourwhisp.ourwhisp_backend.repository.MessageRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Getter
public class MessageService {

    private final MessageRepository messageRepository;

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public List<Message> getRandomMessages(int limit) {
        List<Message> all = messageRepository.findAll();
        Collections.shuffle(all);
        return all.stream().limit(limit).collect(Collectors.toList());
    }

    public Message getMessageById(String id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
    }

    public Message incrementView(String id) {
        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        msg.setView(msg.getView() == null ? 1 : msg.getView() + 1);
        return messageRepository.save(msg);
    }

    public Message createMessage(Message message) {
        if (message.getCreatAt() == null) {
            message.setCreatAt(Instant.now());
        }
        if (message.getView() == null) {
            message.setView(0L);
        }
        return messageRepository.save(message);
    }

    public void deleteMessage(String id) {
        messageRepository.deleteById(id);
    }
}
