package com.easynote.ai.model.Response;



import com.easynote.ai.model.Message;

import java.util.List;

public class ChatCompletionResponse {
    private List<Choice> choices;

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public static class Choice{
        private Message message;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }
}
