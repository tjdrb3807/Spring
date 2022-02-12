# _프로젝트 환경설정_

<br>

## _프로젝트 생성_
* ### 사용 기능   
  * $Web$
    >implementation 'org.springframework.boot:spring-boot-starter-web' 
  * $Thymeleaf$
    >implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'   
  * $JPA$ 
    >implementation 'org.springframework.boot:spring-boot-starter-data-jpa'   
  * $H2$ 
    >runtimeOnly 'com.h2database:h2'   
  * $Lombok$ 
    >compileOnly 'org.projectlombok:lombok'   
     annotationProcessor 'org.projectlombok:lombok'
  * $Validation$
    >implementation 'org.springframework.boot:spring-boot-starter-validation'   
    
<br>

## _JPA와 DB설정, 동작확인_
* ### _YAML_
  * `main/resources/application.yml`
      ```yml
      spring:
      datasource:
          url: jdbc:h2:tcp://localhost/~/japshop
          username: sa
          password:
          dirver-class-name: org.h2.Driver

          jpa:
          hibernate:
              ddl-auto: create
          properties:
              hibername:
                #show_sql: true
              format_sql: true

      logging:
      level:
          org.hibername.SQL: debug
          org.hibername.type: trace    
      ```
  * `spring.jpa.hibername.ddl-auto: create`
    * 이 옵션은 애플리케이션 실행 시점에 테이블을 drop하고, 다시 생성한다.
    * update, create-drop, none
  * 참고
    >모든 로그 출력은 가급적 로거를 통해 남겨야 한다.   

    >`show_sql` 옵션은 `System.out`에 하이버네이트 실행 SQL을 남긴다.   

    >`org.hibernate.SQL` 옵션은 logger를 통해 하이버네이트 실행 SQL을 남긴다.
  * 주의   
    >`application.yml`같은 `yml`파일은 띄어쓰기(스페이스)2칸으로 계층을 만든다.   

    >따라서 띄어쓰끼 2칸틀 필수로 기입해야 한다.   

    >예를 들어서 위 코드의 `datasource`는 `spring:` 하위에 있고 앞에 띄어쓰기 2칸이 있으므로 `spring.datasource`가 된다.

* ### _쿼리 파라미터 로그 남기기_
  * SQL 실행 파라미터를 로그로 남기기 위해 build.gradle에 다음 라이브러리 추가   
    >com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.8.0
  * 외부 라이브러리 사용
    >https://github.com/gavlyukovskiy/spring-boot-data-source-decorator   
  * 참고
    >쿼리 파라미터를 로그로 남기는 외부 라이브러리는 시스템 자원을 사용하므로, 개발 단계에서는 편하게 사용해도 된다.   
    하지만 운영시스템에 적용하려면 꼭 성능테스트를 하고 사용하는 것이 좋다.   
     
<br>
<br>
<br>


# _도메인 분석 설계_

<br>

## _요구사항 분석_

<br>

## _도메인 모델과 테이블 설계_

<br>

## _엔티티 클래스 개발_

<br>

## _엔티티 설계시 주의점_

<br>
<br>
<br>

# _애플리케이션 구현 준비_

<br>

## _구현 요구사항_

<br>

## _애플리케이션 아키텍처_

<br>
<br>
<br>

# _회원 도메인 개발_

<br>

## _회원 리포지토리 개발_

<br>

## _회원 서비스 개발_

<br>

## _회원 기능 테스트_

<br>
<br>
<br>

# _상품 도메인 개발_

<br>

## _상품 엔티티 개발(비즈니스 로직 추가)_

<br>

## _상품 리포지토리 개발_

<br>

## _상품 서비스 개발_

<br>
<br>
<br>

# _주문 도메인 개발_

<br>

## _주문, 주문상품 엔티티 개발_

<br>

## _주문 리포지토리 개발_

<br>

## _주문 서비스 개발_

<br>

## _주문 기능 테스트_

<br>

## _주문 검색 기능 개발_

<br>
<br>
<br>

# _웹 계층 개발_

<br>

## _홈 화면과 레이아웃_

<br>

## _회원 등록_

<br>

## _회원 목록 조회_

<br>

## _상품 등록_

<br>

## _상품 수정_

<br>

## _변경 감지와 병합(merge)_

<br>

## _상품 주문_

<br>

## _주문 목록 검색, 취소_






