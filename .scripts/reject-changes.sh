#!/bin/bash

# 변경사항 거부 및 롤백 스크립트
# 사용법: ./scripts/reject-changes.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="$PROJECT_ROOT/.backup"

if [ ! -d "$BACKUP_DIR" ]; then
    echo "❌ 백업 디렉토리를 찾을 수 없습니다."
    exit 1
fi

echo "❌ 변경사항을 거부합니다..."

# 변경사항 요약 표시
"$SCRIPT_DIR/show-diff.sh"

echo ""
read -p "정말로 모든 변경사항을 되돌리시겠습니까? (y/N): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "취소되었습니다."
    exit 0
fi

echo "🔄 변경사항을 롤백 중..."

# Git 상태 확인 및 리셋
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "📝 Git 변경사항 리셋 중..."
    git reset --hard HEAD
    git clean -fd
fi

# 백업에서 파일 복원
echo "💾 백업에서 파일 복원 중..."
rsync -av --delete --exclude='.git' "$BACKUP_DIR/" "$PROJECT_ROOT/" > /dev/null

# 백업 디렉토리 정리
echo "🧹 백업 파일 정리 중..."
rm -rf "$BACKUP_DIR"

# stash 복원 (가장 최근 stash가 우리 것인지 확인)
LATEST_STASH=$(git stash list | head -1 | grep "claude-preview" || echo "")
if [ -n "$LATEST_STASH" ]; then
    echo "📦 이전 상태 복원 중..."
    git stash pop stash@{0}
fi

echo ""
echo "✅ 모든 변경사항이 성공적으로 롤백되었습니다!"
echo "현재 상태를 확인하려면: git status"
echo ""