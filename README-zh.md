# llama-pinyinIME

简体中文 / [English](./README.md)

Android Pinyin IME for port of Facebook's LLaMA model in C/C++

演示DEMO：

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/6a5d17a7-1a80-4a31-869d-0758bd161ddb

## 内容列表

- [背景](#背景)

- [安装](#安装)

- [使用说明](#使用说明)

- [示例](#示例)

- [相关仓库](#相关仓库)

- [维护者](#维护者)

- [如何贡献](#如何贡献)

- [使用许可](#使用许可)

## 背景

[**llama-jni**](https://github.com/shixiangcap/llama-jni)使用`JNI`技术实现了[**llama.cpp**](https://github.com/ggerganov/llama.cpp)部分常用函数的进一步封装，有效地支持了Android设备上的移动应用程序对于存储在本地的大型语言模型的直接使用。

`llama-pinyinIME`即为[**llama-jni**](https://github.com/shixiangcap/llama-jni)的一个典型用例。

通过为`谷歌拼音输入法`新增输入栏组件，`llama-pinyinIME`可以本地化地提供基于大型语言模型的AI辅助输入服务，且无需联网。

`llama-pinyinIME`的目标包括：

1. 针对`谷歌拼音输入法`的新特性开发，用户仅需在输入法的输入栏中输入需要大型语言模型完成的任务，提交后即可在真正的目标输入位置观察到推理结果的流式打印。
2. 针对输入栏中内置Prompt功能的调用，以便用户快速切换并使用存储在本地文件中的Prompt文本，用于执行翻译、语法修正、一般问答等自定义文本任务。
3. 针对输入栏的输入模式切换，除一般输入和本地大型语言模型推理输入之外，`llama-pinyinIME`还提供了基于[Openai API](https://platform.openai.com/docs/api-reference/chat)的联网辅助输入模式，且同样支持自定义本地Prompt文本。
4. 针对`llama-pinyinIME`中的应用设置，提供与[**llama.cpp**](https://github.com/ggerganov/llama.cpp)相关的多种模型和输入参数等选项的同等支持。
5. 针对`谷歌拼音输入法`中如中文输入、光标、候选词等其他必要适配特性的优化。

## 安装

`llama-pinyinIME`支持制作可用的`Android安装包`，其打包过程同时需要[NDK 和 CMake](https://developer.android.google.cn/studio/projects/install-ndk?hl=zh-cn#default-version)的支持，相关工具配置信息如下。

```gradle
apply plugin: 'com.android.application'

android {
    // 和 libs/android.jar 保持一致
    compileSdkVersion 25
    buildToolsVersion "26.0.2"

    aaptOptions {
        noCompress 'dat'
    }

    defaultConfig {
        minSdkVersion 23
        // 和 libs/android.jar 保持一致
        // noinspection ExpiredTargetSdkVersion
        targetSdkVersion 25
        versionCode 1
        versionName "1.0.0"
        applicationId "com.sx.llama.pinyinime"

        externalNativeBuild {
            cmake {
                cppFlags "-Wall"
                abiFilters 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
            }
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.txt'
        }
    }

    externalNativeBuild {
        cmake {
            path 'src/main/cpp/CMakeLists.txt'
            version '3.22.1'
        }
    }

    lintOptions {
        checkReleaseBuilds false
        // Or, if you prefer, you can continue to check for errors in release builds,
        // but continue the build even when errors are found:
        abortOnError false
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    android.applicationVariants.all { variant ->
        variant.outputs.all {
            outputFileName = "ST_Pinyin_V${defaultConfig.versionName}.apk"
        }
    }
}

dependencies {
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    implementation 'com.alibaba:fastjson:1.2.83'
    implementation 'cz.msebera.android:httpclient:4.5.8'
    provided files('libs/android.jar')
}
```

## 使用说明

### 准备工作

`llama-pinyinIME`并不包含模型文件，请自行准备大型语言模型文件，且其需要得到[**llama.cpp**](https://github.com/ggerganov/llama.cpp)中[指定版本](https://github.com/ggerganov/llama.cpp/releases/tag/master-7e4ea5b)的支持。

Android外部存储设备上的移动应用程序专用文件夹中，需要存储必要的大型语言模型文件（如：[GPT4All](https://github.com/shixiangcap/llama-pinyinIME/tree/main/app/src/main/cpp/llama#using-gpt4all)），假设其路径为：

```sh
/storage/emulated/0/Android/data/com.sx.llama.pinyinime/ggml-vic7b-q5_0.bin
```

则[PinyinIME.java](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/java/com/sx/llama/pinyinime/PinyinIME.java)中的以下一段代码需要与它的文件名对应：

```java
private String modelName = "ggml-vic7b-q5_0.bin";
```

专用文件夹中还需存储必要的Prompt文本文件（如：[1.txt](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/cpp/llama/prompts/1.txt)），假设其路径为：

```sh
/storage/emulated/0/Android/data/com.sx.llama.pinyinime/1.txt
```

倘若需要使用[Openai API](https://platform.openai.com/docs/api-reference/chat)的联网功能，则制作`Android安装包`之前，[PinyinIME.java](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/java/com/sx/llama/pinyinime/PinyinIME.java)中的以下一段代码需要填入可用的`API Key`：

```java
private String openaiKey = "YOUR_OPENAI_API_KEY";
```

### 运行

在`Android Studio`中选择 AVD，然后点击 **Run** 图标 <img src="https://developer.android.google.cn/static/studio/images/buttons/toolbar-run.png?hl=zh-cn" class="inline-icon" alt="">。经过必要的系统设置后，用户即可在输入栏左侧通过按钮快速调整`llama-pinyinIME`的运行模式。

<img src="https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/b9f3f0c1-e265-4123-9b2a-59b2bd5d49e4" width="33%"/> <img src="https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/ee876d87-f62c-4944-b9af-19b2fa4d5e43" width="33%"/> <img src="https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/86abe544-a30e-44ed-869d-01feb26b8eaa" width="33%"/>

- **注意**：后续演示基于内存为**12GB**的虚拟模拟器完成，而基于真实物理设备的测试结果表明，现有硬件的推理速度远未达到应用标准，`llama-pinyinIME`只能作为有关技术路线的验证原型，完成水平仅供参考。

## 示例

### 一般输入模式

`llama-pinyinIME`的一般使用方法和的Android设备上的其他输入法大体相当，同样支持中文、英文和标点符号输入，且在此基础上构建出移动应用程序同样可以支持真实物理设备上的安装与使用。

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/3456e18e-51c3-435c-b772-0c4fb0594056

### 基于本地大型语言模型推理的输入模式

基于Android外部存储设备上的移动应用程序专用文件夹中存储的Prompt文本文件，用户仅需在`llama-pinyinIME`输入栏输入文本内容前加上其`文件名+空格`（不加任何内容则默认[1.txt](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/cpp/llama/prompts/1.txt)），点击最左侧提交图标后即可直接使用。

事实上，用户可以自定义任意多的Prompt文本文件以应对多种输入推理任务与场景，调用相应文件名后等价的[**llama.cpp**](https://github.com/ggerganov/llama.cpp)命令为：

```sh
./main -m "/storage/emulated/0/Android/data/com.sx.llama.pinyinime/ggml-vic7b-q5_0.bin" -n 256 --repeat_penalty 1.0 --color -i -r "User:" -f "/storage/emulated/0/Android/data/com.sx.llama.pinyinime/1.txt"
./main -m "/storage/emulated/0/Android/data/com.sx.llama.pinyinime/ggml-vic7b-q5_0.bin" -n 256 --repeat_penalty 1.0 --color -i -r "User:" -f "/storage/emulated/0/Android/data/com.sx.llama.pinyinime/2.txt"
```

以[1.txt](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/cpp/llama/prompts/1.txt)的语法修正任务和[2.txt](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/cpp/llama/prompts/2.txt)的翻译任务和为例，`llama-pinyinIME`的实际打印效果如下：

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/a6c979ed-ada6-4257-af50-67faf0eb488c

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/759f9507-3364-4ad9-a03b-2feef28c9bf7

### 基于Openai API的联网辅助输入模式

该模式由于将文本推理交由[Openai API](https://platform.openai.com/docs/api-reference/chat)完成，响应效果已经达到了可用水平，且同样支持对于本地Prompt文本文件的直接调用。

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/d52e132e-4712-49ed-a26f-b5d55ed9d12e

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/d68ee539-55f9-4183-9f2a-dfe9393273cb

## 相关仓库

- [LLaMA](https://github.com/facebookresearch/llama) — LLaMA模型的推理代码。
- [llama.cpp](https://github.com/ggerganov/llama.cpp) — 基于C/C++的Facebook LLaMA模型接口。
- [llama-jni](https://github.com/shixiangcap/llama-jni) — 针对基于C/C++的Facebook LLaMA模型接口实现的Android JNI。

## 维护者

[@shixiangcap](https://github.com/shixiangcap)

## 如何贡献

非常欢迎你的加入！[提一个Issue](https://github.com/shixiangcap/llama-pinyinIME/issues)或者提交一个Pull Request。

### 贡献者

感谢以下参与项目的人：
<a href="https://github.com/orgs/shixiangcap/people"><img src="https://avatars.githubusercontent.com/u/134358037" height=20rem/></a>

## 使用许可

[MIT](LICENSE) © shixiangcap
