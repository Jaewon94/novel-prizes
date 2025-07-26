package com.jaewon.novel_prizes_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 로그인 요청 DTO (Data Transfer Object)
 * 
 * 목적:
 * - 클라이언트에서 전송하는 로그인 정보를 안전하게 전달
 * - 입력 데이터 검증을 통한 보안 강화
 * - API 문서화 및 타입 안전성 확보
 * 
 * 검증 규칙:
 * - 이메일 형식 검증 및 필수 입력
 * - 비밀번호 길이 제한 및 필수 입력
 * - 공백 및 null 값 차단
 * 
 * 아키텍처 패턴: DTO Pattern
 * 보안 고려사항: 민감 정보 로깅 방지
 * 
 * 포트폴리오 포인트:
 * - Bean Validation을 활용한 입력 검증
 * - 보안을 고려한 toString 메서드 제외
 * - RESTful API 표준을 준수한 요청 구조
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * 사용자 이메일 주소
     * 
     * 검증 조건:
     * - 이메일 형식 준수 (@, 도메인 포함)
     * - 필수 입력 (null, 빈 문자열, 공백 불허)
     * - 최대 100자 제한
     * 
     * 예시: "user@example.com"
     */
    @NotBlank(message = "이메일은 필수 입력입니다.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다.")
    private String email;

    /**
     * 사용자 비밀번호
     * 
     * 검증 조건:
     * - 필수 입력 (null, 빈 문자열, 공백 불허)
     * - 최소 8자, 최대 100자 제한
     * - 실제 비밀번호 복잡성 검증은 서비스 레이어에서 처리
     * 
     * 보안 고려사항:
     * - toString 메서드에서 제외하여 로그에 노출 방지
     * - 요청 처리 후 즉시 메모리에서 제거 권장
     */
    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
    private String password;

    /**
     * 보안을 위한 toString 메서드 오버라이드
     * 
     * 목적:
     * - 로그 파일에 비밀번호가 노출되는 것을 방지
     * - 디버깅 시 민감 정보 보호
     * - 개발자의 실수로 인한 정보 유출 차단
     * 
     * @return 비밀번호를 마스킹한 문자열 표현
     */
    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}