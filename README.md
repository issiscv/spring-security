# spring-security
스프링 시큐리티 학습을 위한 프로젝트

## 스프링 시큐리티 주요 아키텍쳐
### 1. DelegatingFilterProxy
- 서블릿 컨테이너와 스프링 컨테이너(어플리케이션 컨텍스트) 사이의 링크를 제공하는 ServletFilter이다.
  특정한 이름을 가진 스프링 빈을 찾아 그 빈에게 요청을 위임한다.
- Why? 서블릿 필터(서블릿 컨테니어)와 시큐리티 필터(스프링 컨테이너)는 서로 다른 컨테이너에서 생성되고 동작한다.
![delegating](https://user-images.githubusercontent.com/66157892/148637802-7a69247f-54ed-4a46-82e2-ec453249289d.PNG)
#### 요청 순서
- Servlet Filter가 요청을 DelegatingFilterProxy로 전달한다.
- DelegatingFilterProxy는 해당 요청을 스프링 컨테이너에 생성 된 Filter를 구현한 스프링 빈에 위임한다.
springSecurityFilterChain 이름으로 생성된 빈을 ApplicationContext에서 찾아 위임을 요청 (실제 보안 처리를 하지 않음)
#### FilterChainProxy
- springSecurityFilterChain의 이름으로 생성되는 필터 빈이다.
  DelegatingFilterProxy로부터 요청을 위임 받고 실체로 보안을 처리한다.
  스프링 시큐리티 초기화 시 생성되는 필터들을 관리하고 제어한다.
  ![container](https://user-images.githubusercontent.com/66157892/148638129-4511e780-8441-4dc6-8feb-a69744696ffb.PNG)

### 필터 초기화와 다중 설정 클래스
- 스프링 시큐리티 설정 클래스가 두개일 경우 FilterChainProxy 내부의 securityFilterChains 리스트에 securityFilterChain를 순서대로 저장한다.
- @Order를 통해 순서를 정하고 좁은 범위의 설정 클래스 부터 수서를 정한다.
  ![다중설정](https://user-images.githubusercontent.com/66157892/148639385-fdfc37fa-371e-4e7b-843f-bff461da249a.PNG)
-  아래 그림의 경우 SecurityFilterChains에 SecurityFilterChain이 2개가 존재한다 각 체인에는 구성된 필터가 다르다.
  ![configs](https://user-images.githubusercontent.com/66157892/148639387-19a42ab1-b110-4688-87ab-d2178454064e.PNG)
- 사용자 요청 URL과 매핑되는 필터 체인을 골라 필터가 초기화 된다.

## 3. Authentication
- 인증: 당신이 누구인지 증명하는 것
- 사용자의 인증정보를 저장하는 토큰 개념(구현체: UsernamePasswordAuthenticationToken)
- 인증 요청 시 Authentication 객체에 id와 password를 담고 인증 검증을 거친다.
- 인증 후 최종 인증 결과는 (User 객체, 권한정보를) 담고 SecurityContextHolder에 저장되어 전역적으로 참조 가능


      Authentication authentication = SecurityContexHolder.getContext().getAuthentication()

### 구조
- principal : 사용자 아이디 혹은 User 객체를 저장
- credentials : 사용자 비밀번호
- authorities : 인증된 사용자의 권한 목록
- details : 인증 부가 정보
- Authenticated : 인증 여부
- 인증 여부를 제외하면 대부분의 타입이 Object


## 4. SecurityContextHolder, SecurityContext
### SecurityContext
- Authentication 객체가 저장되는 보관소로 필요 시 언제든지 Authentication 객체를 꺼내어 쓸 수 있도록 제고되는 클래스
- ThreadLocal 에 저장되어 아무 곳에서나 참조가 가능
- 인증이 완료되면 HttpSession 에 저장되어 어플리케이션 전반에 걸쳐 전역적인 참조가 가능하다.
### 인증 시 저장되는 객체의 이해
- SecurityContextHolder - SecurityContext - Authentication - User
- 위의 순서대로 앞의 객체가 뒤의 객체를 보관한다.
### SecurityContextHolder
- SecurityContext 를 저장하는 저장소
- Securitycontext 객체 저장 방식
  1. MODE_THREADLOCAL : 스레드당 SecurityContext 객체를 할당, 기본값
  2. MODE_INHERITABLETHREADLOCAL : 메인 스레드와 자식 스레드에 관하여 동일한 SecurityContext 를 유지
  3. MODE_GLOBAL :  응용 프로그램에서 단 하나의 SecurityContext를 저장한다


    Authentication authentication = SecurityContextHolder.getContext().getAuthentication()

## 5. SecurityContextPersistenceFilter
- SecurityContextPersistenceFilter 란 객체의 생성, 저장, 조회 등의 LifeCycle 을 담당하는 Filter 입니다.
  SecurityContextRepository 를 통하여 매 요청의 HttpSession 에 존재하는 SecurityContext 의 존재 유무 등을 판별하여 존재하지 않을 경우 비어 있는, 존재하는 경우는 해당 SecurityContext 를 전 처리하여 SecurityContextHolder 에 저장하여 줍니다.
  ![persistence](https://user-images.githubusercontent.com/66157892/148670517-6db14b73-618f-4a2f-b7a7-52d717693c10.PNG)
### SecurityContext 객체의 생성, 저장, 조회
-	익명 사용자<br>
     새로운 SecurityContext 객체를 생성하여 SecurityContextHolder 에 저장
     AnonymousAuthenticationFilter 에서 AnonymousAuthenticationToken 객체를 SecurityContext 에 저장
-	인증 시<br>
     새로운 SecurityContext 객체를 생성하여 SecurityContextHolder 에 저장
     UsernamePasswordAuthenticationFilter 에서 인증 성공 후 SecurityContext 에 UsernamePasswordAuthentication 객체를 SecurityContext 에 저장
     인증이 최종 완료되면 Session 에 SecurityContext 를 저장
-	인증 후<br>
     Session 에서 SecurityContext 꺼내어 SecurityContextHolder 에서 저장
     SecurityContext 안에 Authentication 객체가 존재하면 계속 인증을 유지한다
-	최종 응답 시 공통<br>
     SecurityContextHolder.clearContext()

## 6. Authentication Flow: 인증 흐름 이해

1. UsernamePasswordAuthenticationFilter 가 AuthenticationManager 에게 Authentication 객체를 전달한다.
2. AuthenticationManager 는 인증의 전반적인 관리를 하고, 인증을 처리할 수 있는 AuthenticationProvider 를 찾는다.
3. AuthenticationProvider 가 UserDetailsService 를 통해 service 계층에서 Repository 계층을 접근하여 id 에 해당하는 
   회원 객체를 Authentication 객체에 저장하여 권한정보와 저장한다. 
4. Authentication 객체를 AuthenticationManager 에 전달 후 다시 UsernamePasswordAuthenticationFilter 에 전달 후 SecurityContext 에 저장한다.
  ![flow](https://user-images.githubusercontent.com/66157892/148671187-a33953c6-9bd5-47d3-bce0-94d61114b4bf.PNG)

## 7. AuthenticationManager: 인증 관리자
- AuthenticationManager 는 인자로 Authentication 객체를 받는다.
  Authentication 객체에 id, pw를 저장 후 AuthenticationManager 에 보내 인증 과정을 거친다. 
  구현체는 ProviderManager 이다. 해당 인증을 처리해줄 수 있는 Provider 를 찾는다. 
  해당 인증을 처리해줄 수 있는 Provider 가 없으면, 부모 ProviderManager 에서 계속 탐색할 수 있다. 
  인증 성공 시 provider 가 manager 에 인증 성공 Authentication 객체를 전달

## 8. AuthenticationProvider
- AuthenticationProvider 에서 실제 인증에 대한 부분을 처리하는데, 인증 전의 Authentication 객체를 받아서 인증이 완료된 객체를 반환하는 역할을 한다.
  아래와 같은 AuthenticationProvider 인터페이스를 구현해서 Custom 한 AuthenticationProvider 을 작성해서 AuthenticationManager에 등록하면 된다.
- supports(authentication)<br>
  AuthenticationManager 가 보내준 Authentication 객체를 이 AuthenticationProvider 가 인증 가능한 클래스인지 확인하는 메서드다.
  → UsernamePasswordAuthenticationToken 이 ProviderManager 에 도착한다면 ProviderManager 는 자기가 갖고 있는 AuthenticationProvider 목록을 순회하면서 '너가 이거 해결해줄 수 있어?' 하고 물어보고(supports()) 해결 가능하다고 TRUE를 리턴 해주는 AuthenticationProvider에게 authenticate() 메서드를 실행한다
![provider](https://user-images.githubusercontent.com/66157892/148720386-8a806a30-100e-41e1-9ac4-ea9f93a59aa9.PNG)

## 9. Authorization, FilterSecurityInterceptor
### Authorization
- 당신에게 무엇이 허가 되었는지 증명하는 것
  ![인가](https://user-images.githubusercontent.com/66157892/148721773-a5616401-d2cb-4320-94e6-1cef8a810ab7.PNG)

### FilterSecurityInterceptor
- 마지막에 위치한 필터로써 인증된 사용자에 대하여 특정 요청의 승인/거부 여부를 최종적으로 결정
- 인증객체 없이 보호자원에 접근을 시도할 경우 AuthenticationException 을 발생
- 인증 후 자원에 접근 가능한 권한이 존재하지 않을 경우 AccessDeniedException 을 발생
- 권한 제어 방식 중 HTTP 자원의 보안을 처리하는 필터
- 권한 처리를 AccessDecisionManager에게 맡김
#### 인가 흐름
![인가 필터](https://user-images.githubusercontent.com/66157892/148721924-57199bec-cadd-4b61-a602-566d98a8ae6f.PNG)
1. 모든 필터를 통과 후 마지막에 위치한 FilterSecurityInterceptor 에게 요청이 온다.
2. 인증 객체가 Session 에 있는지 없는지 확인한다. 없으면 AuthenticationException 예외를 터트린다.
3. SecurityMetadataSource 를 통해 사용자가 요청한 자원에 필요한 권한 정보를 조회해서 전달한다.
4. 권한 정보가 필요 없으면 자원 접근 허용을 한다.
5. 접근하는 자원에 권한이 필요할 경우 AccessDecisionManager 를 통해 AccessDecisionVoter 에게 심의를 요청하고 승인/거부를 결정한다.
6. AccessDecisionManager 가 접근 승인을 할 겨우 자원 접근 허용한다. 접근이 거불 될 경우 AccessDeniedException 예외를 터트린다.

## 10. 인가 결정 심의자-AccessDecisionManager, AccessDecisionVoter
### AccessDecisionManager
- 인증 정보, 요청정보, 권한정보를 이용해서 사용자의 자원접근을 허용할 것인지 거부할 것인지를 최종 결정하는 주체
- 여러 개의 Voter 들을 가질 수있으며 Voter 들로부터 접근허용, 거부, 보류에 해당하는 각각의 값을 리턴받고 판단 및 결정
- 최종 접근 거부  시 예외 발생
#### 접근 결정의 세가지 유형
- AffirmativeBased<br>
  여러개의 voter 클래스 중 하나라도 접근 허가 시 접근 허가로 판단.
- ConsensusBased<br>
  다수결에 의해 최종 결정을 판단한다.
- UnanimousBased<br>
  모든 voter 가 만장일치 하여야 접근을 승인한다.
  
### AccessDecisionVoter
- 판단을 심사하는 것

#### Voter 가 권한 부여 과정에서 판단하는 자료
- Authentication - 인증 정보(user)
- FilterInvocation – 요청 정보 (antMatcher("/user"))
- ConfigAttributes - 권한 정보 (hasRole("USER"))

### 결정 방식
- ACCESS_GRANTED : 접근허용(1)
- ACCESS_DENIED : 접근 거부(0)
- ACCESS_ABSTAIN : 접근 보류(-1)

### AccessDecisionManager, AccessDecisionVoter 의 흐름
![accessmanager](https://user-images.githubusercontent.com/66157892/148723210-1360d381-379e-43de-aa98-c405e8e6f615.PNG)
