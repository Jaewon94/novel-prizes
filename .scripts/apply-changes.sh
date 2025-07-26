#!/bin/bash

# 변경사항 승인 및 적용 스크립트
# 사용법: ./scripts/apply-changes.sh [커밋메시지]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="$PROJECT_ROOT/.backup"

if [ ! -d "$BACKUP_DIR" ]; then
    echo "❌ 백업 디렉토리를 찾을 수 없습니다."
    exit 1
fi

echo "✅ 변경사항을 승인합니다..."

# 변경사항 요약 표시
"$SCRIPT_DIR/show-diff.sh"

# 커밋 메시지 설정
COMMIT_MSG="${1:-🤖 Claude Code 변경사항 적용

🤖 Generated with [Claude Code](https://claude.ai/code)

Co-Authored-By: Claude <noreply@anthropic.com>}"

# Git에 변경사항 추가
echo "📝 Git에 변경사항 추가 중..."
git add .

# 변경사항이 있는지 확인
if git diff --cached --quiet; then
    echo "ℹ️  커밋할 변경사항이 없습니다."
else
    echo "💾 변경사항을 커밋 중..."
    git commit -m "$COMMIT_MSG"
    echo "✅ 커밋 완료!"
fi

# 백업 디렉토리 정리
echo "🧹 백업 파일 정리 중..."
rm -rf "$BACKUP_DIR"

# stash 정리 (가장 최근 stash가 우리 것인지 확인)
LATEST_STASH=$(git stash list | head -1 | grep "claude-preview" || echo "")
if [ -n "$LATEST_STASH" ]; then
    echo "🗑️  임시 stash 정리 중..."
    git stash drop stash@{0}
fi

echo ""
echo "🎉 변경사항이 성공적으로 적용되었습니다!"
echo "최근 커밋을 확인하려면: git log --oneline -5"
echo ""