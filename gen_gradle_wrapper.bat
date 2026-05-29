@echo off
chcp 65001 >nul 2>&1
title 生成 Gradle Wrapper (8.7)

:: 检查是否安装了 gradle
gradle -v >nul 2>&1
if %errorlevel% neq 0 (
    echo ? 未检测到 gradle 命令，请先安装 Gradle 并配置环境变量。
    echo 下载：https://services.gradle.org/distributions/gradle-8.7-bin.zip
    pause
    exit /b 1
)

echo ? 开始生成 Gradle Wrapper 8.7...
gradle wrapper --gradle-version 8.7 --distribution-type bin

if %errorlevel% equ 0 (
    echo.
    echo ?? 生成成功！
    echo 已生成：
    echo - gradlew.bat
    echo - gradlew
    echo - gradle/wrapper/gradle-wrapper.jar
    echo - gradle/wrapper/gradle-wrapper.properties
) else (
    echo ? 生成失败，请检查网络或 Gradle 安装。
)

pause