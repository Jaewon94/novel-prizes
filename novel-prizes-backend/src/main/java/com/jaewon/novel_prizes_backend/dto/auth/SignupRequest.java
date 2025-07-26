package com.jaewon.novel_prizes_backend.dto.auth;

import com.jaewon.novel_prizes_backend.entity.UserType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원가입 요청 DTO (Data Transfer Object)
 * 
 * 목적:
 * - 새로운 사용자 등록을 위한 정보 전달
 * - 회원가입 데이터의 무결성 및 보안 검증
 * - 사용자 입력 표준화 및 정규화
 * 
 * 검증 전략:
 * - 이메일 중복 검사는 서비스 레이어에서 처리
 * - 비밀번호 복잡성 검증 (대소문자, 숫자, 특수문자 포함)
 * - 닉네임 중복 및 금칙어 검사는 서비스 레이어에서 처리
 * 
 * 아키텍처 패턴: DTO Pattern + Validation Pattern
 * 보안 전략: Input Validation + Sanitization
 * 
 * 포트폴리오 포인트:
 * - 포괄적인 입력 검증으로 데이터 품질 보장
 * - 정규식을 활용한 복잡한 비밀번호 정책 구현
 * - 사용자 친화적인 에러 메시지 제공
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupRequest {

    /**
     * 사용자 이메일 주소 (로그인 ID로 사용)
     * 
     * 검증 조건:
     * - 표준 이메일 형식 준수
     * - 필수 입력
     * - 최대 100자 제한
     * - 중복 검사는 서비스 레이어에서 수행
     * 
     * 추가 처리:
     * - 대소문자 통일 (소문자로 저장)
     * - 공백 제거 (trim)
     */
    @NotBlank(message = "이메일은 필수 입력입니다.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다.")
    private String email;

    /**
     * 사용자 비밀번호
     * 
     * 복잡성 요구사항:
     * - 최소 8자, 최대 100자
     * - 영문 대문자 1개 이상
     * - 영문 소문자 1개 이상
     * - 숫자 1개 이상
     * - 특수문자 1개 이상 (@$!%*?&)
     * 
     * 보안 고려사항:
     * - BCrypt로 해싱 후 저장
     * - 평문 비밀번호는 요청 처리 후 즉시 메모리에서 제거
     */
    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "비밀번호는 영문 대소문자, 숫자, 특수문자(@$!%*?&)를 각각 1개 이상 포함해야 합니다."
    )
    private String password;

    /**
     * 비밀번호 확인
     * 
     * 목적:
     * - 사용자의 비밀번호 입력 오류 방지
     * - 클라이언트 측 검증과 서버 측 이중 검증
     * 
     * 검증:
     * - password 필드와 일치 여부 확인
     * - 서비스 레이어에서 추가 검증 수행
     */
    @NotBlank(message = "비밀번호 확인은 필수 입력입니다.")
    private String passwordConfirm;

    /**
     * 사용자 닉네임
     * 
     * 검증 조건:
     * - 2자 이상 20자 이하
     * - 한글, 영문, 숫자, 일부 특수문자만 허용
     * - 공백은 중간에만 허용 (앞뒤 공백 제거)
     * 
     * 추가 검증 (서비스 레이어):
     * - 중복 닉네임 검사
     * - 금칙어 필터링
     * - 특수 문자 조합 제한
     */
    @NotBlank(message = "닉네임은 필수 입력입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
    @Pattern(
        regexp = "^[가-힣a-zA-Z0-9._-]+$",
        message = "닉네임은 한글, 영문, 숫자, '.', '_', '-'만 사용할 수 있습니다."
    )
    private String nickname;

    /**
     * 사용자 타입 (독자/작가)
     * 
     * 타입별 권한:
     * - READER: 소설 조회, 리뷰 작성, 즐겨찾기 등
     * - AUTHOR: READER 권한 + 소설 연재, 수정, 삭제 등
     * 
     * 기본값: READER
     * 변경: 가입 후 프로필에서 작가 전환 신청 가능
     */
    @NotNull(message = "사용자 타입을 선택해주세요.")
    private UserType userType;

    /**
     * 이용약관 동의 여부
     * 
     * 법적 요구사항:
     * - 개인정보처리방침 동의 (필수)
     * - 서비스 이용약관 동의 (필수)
     * - 마케팅 정보 수신 동의 (선택)
     * 
     * 검증:
     * - 필수 약관 동의 여부 확인
     * - 동의 시점 및 IP 주소 기록 (서비스 레이어)
     */
    @NotNull(message = "이용약관에 동의해주세요.")
    private Boolean termsAgreed;

    /**
     * 마케팅 정보 수신 동의 여부 (선택)
     * 
     * 용도:
     * - 이벤트 및 프로모션 알림
     * - 신작 소설 추천
     * - 서비스 업데이트 공지
     * 
     * 기본값: false (명시적 동의만 허용)
     */
    @Builder.Default
    private Boolean marketingAgreed = false;

    /**
     * 보안을 위한 toString 메서드 오버라이드
     * 
     * 민감 정보 보호:
     * - 비밀번호 및 비밀번호 확인 필드 마스킹
     * - 로그 파일 및 디버그 출력에서 보안 정보 차단
     * 
     * @return 민감 정보를 마스킹한 문자열 표현
     */
    @Override
    public String toString() {
        return "SignupRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", passwordConfirm='[PROTECTED]'" +
                ", nickname='" + nickname + '\'' +
                ", userType=" + userType +
                ", termsAgreed=" + termsAgreed +
                ", marketingAgreed=" + marketingAgreed +
                '}';
    }

    /**
     * 비밀번호 일치 여부 검증 메서드
     * 
     * @return true: 비밀번호 일치, false: 불일치
     */
    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }

    /**
     * 이메일 정규화 메서드
     * 
     * 처리:
     * - 소문자 변환
     * - 앞뒤 공백 제거
     * 
     * @return 정규화된 이메일 주소
     */
    public String getNormalizedEmail() {
        return email != null ? email.toLowerCase().trim() : null;
    }

    /**
     * 닉네임 정규화 메서드
     * 
     * 처리:
     * - 앞뒤 공백 제거
     * - 연속된 공백을 단일 공백으로 변환
     * 
     * @return 정규화된 닉네임
     */
    public String getNormalizedNickname() {
        return nickname != null ? nickname.trim().replaceAll("\\s+", " ") : null;
    }
}