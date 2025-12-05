package com.easynote.ai.utils;

import com.easynote.ai.core.TaskType;

public class PromptBuilder {
    public static String getSystemPrompt(TaskType taskType){
        switch(taskType){
            case TRANSLATE:
                return "你是专业翻译助手，请自动识别用户输入文本的语言（中文或英文），将其双向翻译为目标语言（中文→英文/英文→中文），保持原意准确、自然流畅，仅返回翻译结果，不添加任何额外说明。";
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
