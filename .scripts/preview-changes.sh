#!/bin/bash

# Cursor 스타일 변경사항 미리보기 스크립트
# 사용법: ./scripts/preview-changes.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="$PROJECT_ROOT/.backup"
STASH_NAME="claude-preview-$(date +%s)"

echo "🔍 변경사항 미리보기 시작..."

# 백업 디렉토리 생성
mkdir -p "$BACKUP_DIR"

# 현재 상태를 Git stash에 저장 (변경사항이 있는 경우에만)
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "📦 현재 변경사항을 stash에 저장 중..."
    git stash push -u -m "$STASH_NAME" || {
        echo "❌ Git stash 실패. 변경사항이 없을 수 있습니다."
    }
fi

# 현재 상태를 백업에 저장
echo "💾 현재 상태를 백업 중..."
rsync -av --exclude='.git' --exclude='node_modules' --exclude='build' --exclude='target' --exclude='.backup' "$PROJECT_ROOT/" "$BACKUP_DIR/" > /dev/null

echo "✅ 미리보기 준비 완료!"
echo ""
echo "이제 Claude가 변경사항을 적용할 수 있습니다."
echo "변경 완료 후 다음 명령어를 사용하세요:"
echo "  승인: ./scripts/apply-changes.sh"
echo "  거부: ./scripts/reject-changes.sh"
echo ""