#!/bin/bash

# === CONFIG ===
PROJECT_DIR="./shop"
LOG_FILE="$PROJECT_DIR/mvn_output.log"
BOT_TOKEN="your_telegram_bot_token"
CHAT_ID="your_telegram_chat_id"

# === STEP 1: Run Tests and Save Log ===
echo "🔧 Running Maven tests locally..."
cd "$PROJECT_DIR" || exit 1
mvn clean test | tee mvn_output.log
cd - || exit 1

# === STEP 2: Compile & Run Java Telegram Sender ===
echo "📤 Sending test logs to Telegram..."
javac SendTelegram.java || { echo "❌ Compilation failed!"; exit 1; }
java SendTelegram "$LOG_FILE" "$BOT_TOKEN" "$CHAT_ID"

# === STEP 3: Prepare Git Push ===
echo "📦 Preparing to push logs to GitHub..."
git add "$LOG_FILE"
git commit -m "🧪 Add test log for GitHub Actions Telegram notify"
git push origin main

echo "✅ Done! Message sent locally and GitHub workflow triggered."
