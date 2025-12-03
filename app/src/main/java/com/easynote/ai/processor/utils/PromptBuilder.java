package com.easynote.ai.processor.utils;

import com.easynote.ai.core.TaskType;

public class PromptBuilder {
    public static String getSystemPrompt(TaskType taskType){
        switch(taskType){
            case TRANSLATE:
                return"你是专业翻译助手，请将用户输入的文本翻译成英文，保持原意准确且自然流畅，只返货处理后的文本。";
            case POLISH:
                return "你是文本润色专家，请优化用户输入的文本，提升语言丰富度、表达流畅度和专业性，保留原意，只返回处理后的文本。";
            case SUMMARY:
                return "你是总结助手，请提炼用户输入文本的核心观点，用简洁的语言总结（不超过200字），只返回处理后的文本。";
            case CORRECT:
                return "你是语法纠错专家，请检查用户输入文本的语法错误、拼写错误，并给出修正建议，只返回处理后的文本。";
            default:
                return "你是智能助手，请根据用户需求处理文本。";
        }
    }
}
