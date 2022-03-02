# _QueryDSL_

<br>

## _QueryDSL 설정과 검증_   
* `build.gradle`에 주석을 참고해서 querydsl 설정 추가   

    ```Java
    buildscript {
        ext {
            queryDslVersion = "5.0.0"
        }
    }

    plugins {
        id 'org.springframework.boot' version '2.6.3'
        id 'io.spring.dependency-management' version '1.0.11.RELEASE'
        //querydsl 추가
        id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
        id 'java'
    }

    group = 'study'
    version = '0.0.1-SNAPSHOT'
    sourceCompatibility = '11'

    configurations {
        compileOnly {
            extendsFrom annotationProcessor
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
        implementation 'org.springframework.boot:spring-boot-starter-web'
        implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.8'

        //querydsl 추가
        implementation "com.querydsl:querydsl-jpa:${queryDslVersion}"
        annotationProcessor "com.querydsl:querydsl-apt:${queryDslVersion}"

        compileOnly 'org.projectlombok:lombok'
        runtimeOnly 'com.h2database:h2'
        annotationProcessor 'org.projectlombok:lombok'

        //테스트에서 lombok 사용
        testCompileOnly 'org.projectlombok:lombok'
        testAnnotationProcessor 'org.projectlombok:lombok'

        testImplementation 'org.springframework.boot:spring-boot-starter-test'
    }

    tasks.named('test') {
        useJUnitPlatform()
    }

    //querydsl 추가 시작
    def querydslDir = "$buildDir/generated/querydsl"
    querydsl {
        jpa = true
        querydslSourcesDir = querydslDir
    }
    sourceSets {
        main.java.srcDir querydslDir
    }
    configurations {
        querydsl.extendsFrom compileClasspath
    }
    compileQuerydsl {
        options.annotationProcessorPath = configurations.querydsl
    }
    //querydsl 추가 끝
    ```

<br>

* ### _검증용 엔티티 생성_

    ```Java
    @Entity
    @Getter @Setter
    public class Hello {
        
        @Id @GeneratedValue
        private Long id;
    }
    ```

<br>

* ### _검증용 Q타입 생성_
  * Gradle InteliJ 사용법
    * Gradle -> Tasks -> build -> clean
    * Gradle -> Tasks -> other -> comlieQuerydsl
  * Gradle 콘솔 사용법
    * ./gradlew clean compileQuerydsl
  * Q타입 생성 확인 
    * build -> generated -> querydls
    * study.querydsl.entity.QHello.java 파일이 생성되어 있어야 한다.
  * 참고
    >Q타입은 컴파일 시점에 자동으로 생성되므로 버전관리(GIT)에 포함하지 않는 것이 좋다.   
    앞서 설정에서 생성 위치를 gradle build 폴더 아래 생성되도록 했기 때문데 이 부분도 자연스럽게 해결된다.   
    (대부분 gradle build 폴더를 git에 포함하지 않는다.)   

<br>

## _스프링 부트 설정 - JPA, DB_
* `application.yml`
    ```yml
    spring:
    datasource:
        url: jdbc:h2:tcp://localhost/~/querydsl
        username: sa
        password:
        driver-class-name: org.h2.Driver
    jpa:
        hibernate:
        ddl-auto: create
        properties:
        hibernate:
    #        show_sql: true
            format_sql: true

    logging.level:
    org.hibernate.SQL: debug
    #  org.hibernate.type: trace  
    ```
  * 참고
    >모든 로그 출력은 가급적 로거를 통해 남겨야 한다.   
    `show_sql`옵션은 `System.out`에 하이버네이트 실행 SQL을 남긴다.   
    `org.hibernate.SQL`옵션은 logger를 통해 하이버네이트 실행 SQL을 남긴다.

<br>
<br>
<br>

# _예제 도메인 모델_

<br>

## _예제 도베인 모델과 동작 확인_
* ### _Member Entity_
    ```Java
    @Entity
    @NoArgsConstructor(access = PROTECTED)
    @ToString(of = {"id", "username", "age"})
    @Getter @Setter
    public class Member {
        
        @Id @GeneratedValue(strategy = IDENTITY)
        @Column(name = "member_id")
        private Long id;
        private String username;
        private int age;

        @ManyToOne(fetch = LAZY)
        @JoinColumn(name = "team_id")
        private Team team;

        public Member(String username) {
            this(username, 0);
        }

        public Member(String username, int age) {
            this(username, age, null)
        }

        public Member(String username, int age, Team team) {
            this.username = username;
            this.age = age;
            if (team != null) {
                changeTeam(team);
            }
        }

        public void changeTeam(Team team) {
            this.team = team;
            team.getMembers().add(this);
        }
    }
    ```
  * 롬복 설명
    * `@Setter`: 실무에서는 가급적 Setter는 사용하지 않는다.
    * `@NoArgsConstructor(accescc = AccessLevel.PROTECTED`: 기본 생성자를 막고 싶은데, JPA 스팩상 PROTECTED로 열어두어야 한다.
    * `@ToString`: 가급적 내부 필드만(열어두어야 한다.)

<br>
<br>
<br>

# _기본 문법_

<br>

## _시작 - JPQL vs QueryDSL_

<br>

## _기본 Q-Type 황용_

<br>

## _검색 조건 쿼리_

<br>

## _결과 조회_

<br>

## _정렬_

<br>

## _페이징_

<br>

## _집합_

<br>

## _조인 - 기본 조인_

<br>

## _조인 - on절_

<br>

## _조인 - 페치조인_

<br>

## _서브 쿼리_

<br>

## _Case문_

<br>

## _상수, 문자 더하기_

