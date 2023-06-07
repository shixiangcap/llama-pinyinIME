# llama-pinyinIME

Android Pinyin IME for port of Facebook's LLaMA model in C/C++

Demo:

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/74c2b738-365b-4725-a094-2bf790f93e89

## Table of Contents

- [Background](#background)

- [Install](#install)

- [Usage](#usage)

- [Examples](#examples)

- [Related Efforts](#related-efforts)

- [Maintainers](#maintainers)

- [Contributing](#contributing)

- [License](#license)

## Background

[**llama-jni**](https://github.com/shixiangcap/llama-jni) implements further encapsulation of common functions in [**llama.cpp**](https://github.com/ggerganov/llama.cpp) with `JNI`, enabling direct use of large language models (LLM) stored locally in mobile applications on Android devices.

`llama-pinyinIME` is a typical use case of [**llama-jni**](https://github.com/shixiangcap/llama-jni).

By adding an input field component to the `Google Pinyin IME`, `llama-pinyinIME` provides a localized AI-assisted input service based on a LLM without the need for internet connectivity.

The goals of `llama-pinyinIME` include:

1. Developing new features for the `Google Pinyin IME` so that users can enter tasks that require a LLM to complete in the input field of the IME. After submission, users can observe the streaming printout of the results in the actual target input position.
2. Calling the built-in Prompt feature in the input field to allow users to quickly switch and use Prompt text stored in local files for performing tasks such as translation, grammar correction, and general Q&A.
3. Supporting input mode switching in the input field. In addition to general input and local LLM input, `llama-pinyinIME` also provides an internet-assisted input mode based on the [Openai API](https://platform.openai.com/docs/api-reference/chat) and supports custom local Prompt text.
4. Providing support for various models and input parameters related to [**llama.cpp**](https://github.com/ggerganov/llama.cpp) in the application settings of `llama-pinyinIME`.
5. Optimizing necessary adaptation features in the `Google Pinyin IME`, such as Chinese input, cursor control, candidate words, etc.

## Install

`llama-pinyinIME` supports the creation of an `Android application package (APK)`. The packaging process requires the support of [NDK and CMake](https://developer.android.google.cn/studio/projects/install-ndk?hl=zh-cn#default-version). The relevant tool configuration information is as follows.

```gradle
apply plugin: 'com.android.application'

android {
    compileSdkVersion 25
    buildToolsVersion "26.0.2"

    aaptOptions {
        noCompress 'dat'
    }

    defaultConfig {
        minSdkVersion 23
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

## Usage

### Preparations

`llama-pinyinIME` does not contain model files. Please prepare your own LLM, which need to be supported by the [specified version](https://github.com/ggerganov/llama.cpp/releases/tag/master-7e4ea5b) of [**llama.cpp**](https://github.com/ggerganov/llama.cpp).



The necessary LLM (e.g. [GPT4All](https://github.com/shixiangcap/llama-pinyinIME/tree/main/app/src/main/cpp/llama#using-gpt4all)) need to be stored in a dedicated folder for mobile applications on the Android external storage device, assuming the path is

```sh
/storage/emulated/0/Android/data/com.sx.llama.pinyinime/ggml-vic7b-q5_0.bin
```

Then the following piece of code in [PinyinIME.java](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/java/com/sx/llama/pinyinime/PinyinIME.java) needs to correspond to its filename.

```java
private String modelName = "ggml-vic7b-q5_0.bin";
```

The dedicated folder also needs to store the necessary Prompt text file (e.g. [1.txt](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/cpp/llama/prompts/1.txt)), assuming the path is

```sh
/storage/emulated/0/Android/data/com.sx.llama.pinyinime/1.txt
```

If you need to use the [Openai API](https://platform.openai.com/docs/api-reference/chat) for networking, the following code in [PinyinIME.java](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/java/com/sx/llama/pinyinime/PinyinIME.java) needs to be filled with the available `API Key` before creating the `APK`:

```java
private String openaiKey = "YOUR_OPENAI_API_KEY";
```

### Run

Select AVD in `Android Studio` and click on the **Run** icon <img src="https://developer.android.google.cn/static/studio/images/buttons/toolbar-run.png?hl=zh-cn" class="inline-icon" alt="">. After the necessary system settings, users can quickly adjust the running mode of `llama-pinyinIME` with the button on the left side of the input field.

<img src="https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/b9f3f0c1-e265-4123-9b2a-59b2bd5d49e4" width="33%"/> <img src="https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/ee876d87-f62c-4944-b9af-19b2fa4d5e43" width="33%"/> <img src="https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/86abe544-a30e-44ed-869d-01feb26b8eaa" width="33%"/>

- **Note**: The subsequent demonstration is based on a virtual simulator with **12GB** of RAM, while the test results based on real physical devices show that the inference speed of the existing hardware is far from the application standard. `llama-pinyinIME` can only be used as a validation prototype of the technical route, and the completion level is for reference only.

## Examples

### General input mode

The general usage of `llama-pinyinIME` is similar to other IME on Android devices, which also support Chinese, English and punctuation input, and the mobile application built on this basis can also be installed and used on real physical devices.

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/3456e18e-51c3-435c-b772-0c4fb0594056

### Local LLM input mode

Based on the Prompt text file stored in a folder dedicated to mobile applications on the Android external storage device, the user simply enters the text content in the `llama-pinyinIME` input field preceded by `its filename + space` (default [1.txt](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/cpp/llama/prompts/1.txt) if nothing is added), clicks the submit icon on the far left and is ready to use.

In fact, users can customize as many Prompt text files as they want to cope with a variety of input reasoning tasks and scenarios by calling the equivalent [**llama.cpp**](https://github.com/ggerganov/llama.cpp) command after the corresponding file name as

```sh
./main -m "/storage/emulated/0/Android/data/com.sx.llama.pinyinime/ggml-vic7b-q5_0.bin" -n 256 --repeat_penalty 1.0 --color -i -r "User:" -f "/storage/emulated/0/Android/data/com.sx.llama.pinyinime/1.txt"
./main -m "/storage/emulated/0/Android/data/com.sx.llama.pinyinime/ggml-vic7b-q5_0.bin" -n 256 --repeat_penalty 1.0 --color -i -r "User:" -f "/storage/emulated/0/Android/data/com.sx.llama.pinyinime/2.txt"
```

Taking the syntax correction task of [1.txt](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/cpp/llama/prompts/1.txt) and the translation task of [2.txt](https://github.com/shixiangcap/llama-pinyinIME/blob/main/app/src/main/cpp/llama/prompts/2.txt) and as an example, the actual printing effect of `llama-pinyinIME` is as follows

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/a6c979ed-ada6-4257-af50-67faf0eb488c

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/759f9507-3364-4ad9-a03b-2feef28c9bf7

### Openai API assisted input mode

This mode has reached a usable level of response due to the text inference left to the [Openai API](https://platform.openai.com/docs/api-reference/chat), and also supports direct calls to local Prompt text files.

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/d52e132e-4712-49ed-a26f-b5d55ed9d12e

https://github.com/shixiangcap/llama-pinyinIME/assets/41248645/d68ee539-55f9-4183-9f2a-dfe9393273cb

## Related Efforts

- [LLaMA](https://github.com/facebookresearch/llama) — Inference code for LLaMA models.
- [llama.cpp](https://github.com/ggerganov/llama.cpp) — Port of Facebook's LLaMA model in C/C++.
- [llama-jni](https://github.com/shixiangcap/llama-jni) — Android JNI for port of Facebook's LLaMA model in C/C++.

## Maintainers

[@shixiangcap](https://github.com/shixiangcap)

## Contributing

Feel free to dive in! [Open an issue](https://github.com/shixiangcap/llama-pinyinIME/issues) or submit PRs.

### Contributors

This project exists thanks to all the people who contribute.
<a href="https://github.com/orgs/shixiangcap/people"><img src="https://avatars.githubusercontent.com/u/134358037" height=20rem/></a>

## License

[MIT](LICENSE) © shixiangcap
