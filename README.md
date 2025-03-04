# ChatGPT Realtime API WebRTC Sample App for Android

## 概要

このアプリは、ChatGPT の Realtime API を Android アプリケーションから WebRTC 経由で利用するためのサンプルコードです。このアプリは、Stream の WebRTC SDK を使用して、Android デバイスと ChatGPT の Realtime API との間にリアルタイム通信チャネルを確立しま
す。これにより、ユーザーは Android アプリからリアルタイムに ChatGPT と対話することができます。

このサンプルアプリは、以下の技術要素を組み合わせています。

*   **ChatGPT Realtime API:** OpenAI が提供する、リアルタイムな対話を実現するための API。
*   **WebRTC:** ブラウザやモバイルアプリ間でリアルタイム通信を実現するための技術。
*   **Stream WebRTC SDK:** Stream が提供する、WebRTC を簡単に利用するための Android SDK。

## 機能

*   **リアルタイム対話:** Android アプリから ChatGPT の Realtime API とリアルタイムに対話できます。
*   **ログ表示:** 画面下部にログ表示エリアがあり、送信したメッセージと ChatGPT からの応答がリアルタイムに表示されます。

## 使用方法

1.  **API Key入力:** 画面上部のテキスト入力ボックスにChatGPTのAPI KEYを入力します。
2.  **接続操作:** 画面中央の「接続開始」をクリックして、RealtimeAPIに接続します。
3.  **ログ確認:** 画面下部のログ表示エリアに、ChatGPT からのログがリアルタイムに表示されます。

## 開発環境

*   **Android Studio:** 最新バージョン
*   **Kotlin:** 最新バージョン
*   **Gradle:** 最新バージョン
*   **Android SDK:** API レベル 28 以上
*   **Jetpack:**
    *   ViewModel
    *   Lifecycle
    *   Activity
*   **Hilt:** 依存性の注入
*   **Kotlin Coroutines:** 非同期処理
*   **StateFlow:** 状態管理
*   **Stream WebRTC SDK:** リアルタイム通信
*   **ChatGPT Realtime API:** リアルタイム対話
*   **Retrofit:** ネットワーク通信
*   **OkHttp:** ネットワーク通信
*   **Kotlinx Serialization:** JSON のシリアライズ/デシリアライズ

## ライセンス

[MIT License](LICENSE)