#!/bin/bash

APP_NAME="tak-0.0.1-SNAPSHOT.jar"
APP_DIR="/home/ubuntu/BE/build/libs"
LOG_FILE="/home/ubuntu/BE/build/tak.log"

echo "Stopping existing application..."
if pgrep -f $APP_NAME > /dev/null; then
  pkill -f $APP_NAME
  echo "Existing application stopped."
else
  echo "No running application found."
fi

echo "Starting new application..."
nohup java -jar $APP_DIR/$APP_NAME > $LOG_FILE 2>&1 &
echo "Application started successfully. Logs: $LOG_FILE"