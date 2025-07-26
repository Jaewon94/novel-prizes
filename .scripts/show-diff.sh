#!/bin/bash

# 변경사항 diff 표시 스크립트
# 사용법: ./scripts/show-diff.sh [파일경로]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="$PROJECT_ROOT/.backup"

if [ ! -d "$BACKUP_DIR" ]; then
    echo "❌ 백업 디렉토리를 찾을 수 없습니다. preview-changes.sh를 먼저 실행하세요."
    exit 1
fi

echo "📋 변경사항 요약:"
echo "===================="

# 특정 파일이 지정된 경우
if [ $# -eq 1 ]; then
    FILE_PATH="$1"
    BACKUP_FILE="$BACKUP_DIR/$FILE_PATH"
    CURRENT_FILE="$PROJECT_ROOT/$FILE_PATH"
    
    if [ -f "$BACKUP_FILE" ] && [ -f "$CURRENT_FILE" ]; then
        echo "📄 파일: $FILE_PATH"
        echo "---"
        diff -u "$BACKUP_FILE" "$CURRENT_FILE" | head -50 || echo "변경사항 없음"
    else
        echo "❌ 파일을 찾을 수 없습니다: $FILE_PATH"
    fi
    exit 0
fi

# 모든 변경된 파일 찾기
CHANGED_FILES=$(find "$PROJECT_ROOT" -type f \( -name "*.java" -o -name "*.md" -o -name "*.yml" -o -name "*.yaml" -o -name "*.json" -o -name "*.sh" -o -name "*.gradle" \) -not -path "*/.git/*" -not -path "*/.backup/*" -not -path "*/build/*" -not -path "*/target/*" -not -path "*/node_modules/*")

CHANGES_FOUND=false

for file in $CHANGED_FILES; do
    RELATIVE_PATH="${file#$PROJECT_ROOT/}"
    BACKUP_FILE="$BACKUP_DIR/$RELATIVE_PATH"
    
    if [ -f "$BACKUP_FILE" ]; then
        if ! diff -q "$BACKUP_FILE" "$file" > /dev/null 2>&1; then
            CHANGES_FOUND=true
            echo ""
            echo "📄 변경된 파일: $RELATIVE_PATH"
            echo "---"
            # 처음 20줄만 표시
            diff -u "$BACKUP_FILE" "$file" | head -20
            echo "..."
        fi
    else
        # 새로 생성된 파일
        if [ -f "$file" ]; then
            CHANGES_FOUND=true
            echo ""
            echo "📄 새 파일: $RELATIVE_PATH"
            echo "---"
            head -10 "$file"
            echo "..."
        fi
    fi
done

# 삭제된 파일 찾기
find "$BACKUP_DIR" -type f \( -name "*.java" -o -name "*.md" -o -name "*.yml" -o -name "*.yaml" -o -name "*.json" -o -name "*.sh" -o -name "*.gradle" \) | while read backup_file; do
    RELATIVE_PATH="${backup_file#$BACKUP_DIR/}"
    CURRENT_FILE="$PROJECT_ROOT/$RELATIVE_PATH"
    
    if [ ! -f "$CURRENT_FILE" ]; then
        CHANGES_FOUND=true
        echo ""
        echo "🗑️  삭제된 파일: $RELATIVE_PATH"
    fi
done

if [ "$CHANGES_FOUND" = false ]; then
    echo "변경사항이 없습니다."
fi

echo ""
echo "===================="
echo "전체 diff를 보려면: git diff"
echo "특정 파일 diff: ./scripts/show-diff.sh [파일경로]"
echo ""