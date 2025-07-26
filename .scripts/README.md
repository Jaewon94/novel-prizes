# Cursor 스타일 Diff 검토 도구

Claude Code와 함께 사용할 수 있는 변경사항 미리보기 및 검토 도구입니다.

## 🛠️ 도구 설명

### 1. `preview-changes.sh`
변경사항 적용 전 현재 상태를 백업하고 미리보기를 준비합니다.

```bash
./scripts/preview-changes.sh
```

### 2. `show-diff.sh` 
변경된 파일들의 diff를 보여줍니다.

```bash
# 모든 변경사항 보기
./scripts/show-diff.sh

# 특정 파일의 변경사항 보기
./scripts/show-diff.sh src/main/java/com/example/SomeClass.java
```

### 3. `apply-changes.sh`
변경사항을 승인하고 Git 커밋으로 저장합니다.

```bash
# 기본 커밋 메시지로 적용
./scripts/apply-changes.sh

# 커스텀 커밋 메시지로 적용
./scripts/apply-changes.sh "feat: 새로운 기능 추가"
```

### 4. `reject-changes.sh`
변경사항을 거부하고 이전 상태로 롤백합니다.

```bash
./scripts/reject-changes.sh
```

## 🔄 사용 워크플로우

1. **변경 전 미리보기 준비**
   ```bash
   ./scripts/preview-changes.sh
   ```

2. **Claude가 변경사항 적용**
   - Claude Code에게 변경사항 요청
   - Claude가 파일들을 수정

3. **변경사항 검토**
   ```bash
   ./scripts/show-diff.sh
   ```

4. **변경사항 승인 또는 거부**
   ```bash
   # 승인하는 경우
   ./scripts/apply-changes.sh
   
   # 거부하는 경우  
   ./scripts/reject-changes.sh
   ```

## 💡 사용 예시

```bash
# 1. 변경 전 준비
$ ./scripts/preview-changes.sh
🔍 변경사항 미리보기 시작...
💾 현재 상태를 백업 중...
✅ 미리보기 준비 완료!

# 2. Claude가 파일 수정 후 변경사항 확인
$ ./scripts/show-diff.sh
📋 변경사항 요약:
====================

📄 변경된 파일: src/main/java/com/example/User.java
---
@@ -1,5 +1,8 @@
 public class User {
     private String name;
     private String email;
+    private String phone;
+    private LocalDateTime createdAt;
 }

# 3. 변경사항 승인
$ ./scripts/apply-changes.sh "feat: User 엔티티에 phone과 createdAt 필드 추가"
✅ 변경사항을 승인합니다...
💾 변경사항을 커밋 중...
🎉 변경사항이 성공적으로 적용되었습니다!
```

## 🚨 주의사항

- 스크립트 실행 전에 중요한 작업은 미리 커밋해두세요
- `.backup` 디렉토리는 일시적으로 생성되며 자동으로 정리됩니다
- Git stash를 사용하므로 기존 stash가 있다면 주의하세요

## 🔧 커스터마이징

파일 확장자나 제외할 디렉토리를 수정하려면 각 스크립트의 `find` 명령어 부분을 편집하세요.

```bash
# 현재 설정 (show-diff.sh에서)
find "$PROJECT_ROOT" -type f \( -name "*.java" -o -name "*.md" -o -name "*.yml" -o -name "*.yaml" -o -name "*.json" -o -name "*.sh" -o -name "*.gradle" \)

# 다른 확장자 추가 예시
find "$PROJECT_ROOT" -type f \( -name "*.java" -o -name "*.md" -o -name "*.py" -o -name "*.js" -o -name "*.ts" \)
```