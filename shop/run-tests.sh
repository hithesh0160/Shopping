#!/bin/bash

# Define working directory (modify if needed)
WORKDIR="./shop"

# Step 1: Run Maven tests and generate logs
echo "🔁 Running Maven tests..."
cd "$WORKDIR" || exit 1

mvn clean test | tee mvn_output.log

# Step 2: Extract last 20 lines
tail -n 20 mvn_output.log > last_20.log
cd ..

# Step 3: Git add, commit and push
echo "📤 Pushing test logs to GitHub..."
git add "$WORKDIR/mvn_output.log" "$WORKDIR/last_20.log"
git commit -m "chore: add local test logs for GitHub Actions"
git push origin main

echo "✅ Done! GitHub Actions will now send logs to Telegram (if configured)."
