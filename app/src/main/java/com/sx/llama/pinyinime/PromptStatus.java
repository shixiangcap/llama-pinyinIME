package com.sx.llama.pinyinime;

public final class PromptStatus {

    public static final int UN_PROMPT = 0;      // 非Prompt模式
    public static final int PROMPT_LOCAL = 1;   // Prompt模式 - 提交后本地处理
    public static final int PROMPT_CLOUD = 2;   // Prompt模式 - 提交后云端处理
}
