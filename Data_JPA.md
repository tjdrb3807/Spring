# _프로젝트 환경설정_

<br>

## _라이브러리 살펴보기_
* ### _gradle 의존관계 보기_
  * `./gradlew.dependencise --configuration compileClasspath`

<br>

* ### _스프링 부트 라이브러리 살펴보기_
  * spring-boot-starter-web
    * spirng-boot-starter-tomcat: 톰캣(웹서버)
    * spirng-webmvc: 스프링 웹 MVC
  * spring-boot-starter-data-jpa
    * spring-boot-starter-aop
    * spring-boot-starter-jdbc
      * HikariCP 커넥션 풀
    * hibernate + JPA
    * spring-data-jpa: 스프링 데이터 JPA
  * spring-boot-starter(공통): 스프링 부트 + 스프링 코어 + 로깅
    * spring-boot
      * spring-core
    * spring-boot-starter-logging
      * logback, slf4j

<br>

* ### _테스트 라이브러리_
  * spring-boot-starter-test
    * junit: 테스트 프레임워크, 스프링 부트 2.2부터 junit5(jupiter)사용
      * 과거 버전은 vintage
    * mockito: 목 라이브러리
    * assertj: 테스트 코드를 좀더 편하게 작성하게 도와주는 라이브러리
      * ` implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.8.0'`
    * sping-test: 스프링 통합 테스트 지원
  * 핵심 라이브러리
    * 스프링 MVC
    * 스프링 ORM
    * JPA, 하이버네이트
    * 스프링 데이터 JPA
  * 기타 라이브러리
    * H2 데이터베이스 클라이언트
    * 커넥션 풀: 부트 기본은 HikariCP
    * 로깅 SLF4J & LogBack
    * 테스트

<br>

## _H2 데이터베이스 설치_

<br>

* ### _개발이나 테스트 용도로 가볍고 편리한 DB, 웹 화면 제공_
  * 권한주기: `chmod 755 h2.sh`
  * 데이터베이스 파일 생성 방법
    * `jdbc:h2:~/datajpa`(최초 한번)
    * `~/datajpa.mv.db`파일 생성 확인
    * 이후 부터는 `jdbc:h2:tcp://localhost/~/datajpa`이렇게 접속

<br>

## _스프링 데이터 JPA와 DB 설정, 동작확인_
* ### _application.yml_
    ```yml
    spring:
        datasource:
            url: jdbc:h2:tcp://localhost/~/datajpa
            username: sa
            password:
            driver-class-name: org.h2.Driver
    
        jpa:
            hibernamte:
                ddl-auto: create
            properties:
                hibername:
                #show_sql: true
                format_sql: true

    logging.level:
        org.hibernate.SQL: debug
        #org.hibernate.type: trace
    ```
  * spring.jap.hibernate.ddl-auto: create
    * 이 옵션은 애플리케이션 실행 시점에 테이블을 drop 하고, 다시 생성한다
  * 참고
    >모든 로그 출력은 가급적 로거를 통해 남겨야 한다.    
    `show_sql` 옵션은 `System.out`에 하이버네이트 실행 SQL을 남긴다.   
    `org.hibernamte.SQL` 옵션은 logger를 통해 하이버네이트 실행 SQL을 남긴다.   

<br>
<br>
<br>

# _예제 도메인 모델_

<br>

## _예제 도메인 모델과 동작 확인_
* ### _엔티티 클래스_
    ![](img/img338.png)

<br>

* ### _ERD_
    ![](img/img339.png)

<br>

* ### _MemberEntity_
    ```Java
    @Entity
    @Getter
    @NoArgsConstructor(asscess = PROTECTED)
    @ToString(of = {"id", "username", "age"})
    public class Member {

        @Id
        @GeneratedValue(strategy = IDENTITY)
        @Column(name = "member_id")
        private Long id;
        private String username;
        private int age;

        @ManyToOne(fetch = LAZY)
        @JoinColumn(name = "team_id")
        private Team team;

        public Member(Stirng username) {
            this.username = username;
        }

        public Member(String username, int age) {
            this.username = username;
            this.age = age;
        }

        public Member(String username, int age, Team team) {
            this.username = username;
            this.age = age;
            if (team != null) {
                changeTeam(team);
            }
        }

        /*
        * 양방향 연관관계 편의 메서드
        */
        public void changeTeam(Team team) {
            this.team = team;
            team.getMembers().add(this);
        }
    }
    ```
  * 롬복 설명
    * @Setter: 실무에서는 가급적 Setter는 사용하지 않기
    * @NoArgsConstructor AccessLevel.PROTECTED: 기본 생성자를 막고싶은데, JPA 스펙상 PROTECTED로 열어두어야 한다.
    * @ToString은 가급적 내부 필드만(연관관계 없는 필드)
  * `changeTeam()`으로 양방향 연관관계 한번에 처리(연관관계 편의 메서드)

<br>

* ### _TeamEntity_
    ```Java
    @Entity
    @Getter
    @NoArgsConstructor(access = PROTECTED)
    @ToString(of = {"id", "name"})
    public class Team {

        @Id
        @GeneratedValue(strategy = IDENTITY)
        @Column(name = "team_id")
        private Long id;
        private String name;

        @OneToMany(mappedBy = "team")
        private List<Member> members = new ArrayList<>();

        public Team(String name) {
            this.name = name;
        }
    }
    ``` 
  * Member와 Team은 양방향 연관관계, `Member.team`이 연관관계의 주인, `Team.members`는 연관관계의 주인이 아니다. 따라서 `Member.team`이 데이터베이스 외래키 값을 변경, 반대편은 읽기만 간능

<br>

* ### _TestCode_
    ```Java
    @SpringBootTest
    public class MemberTest {

        @PersistneceContext
        EntityManager entityManager;

        @Test
        @Transactional
        @Rollback(false)
        void testEntity() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");

            entityManger.persist(teamA);
            entityManger.persist(teamB);

            Member member1 = new Member("member1", 10, teamA);
            Member member2 = new Member("member2", 20, teamA);
            Member member3 = new Member("member3", 30, teamB);
            Member member4 = new Member("member4", 40, teamB);

            entityManger.persist(member1);
            entityManger.persist(member2);
            entityManger.persist(member3);
            entityManger.persist(member4);

            //초기화
            entityManger.flush();
            entityManger.clear();

            //확인
            List<Member> members = entityManger.createQuery("select m from Member m", Member.class)
                    .getResultList();

            for (Member member : members) {
                System.out.println("member = " + member);
                System.out.println("-> member.team = " + member.getTeam());
            }
        }
    }
    ```

<br>
<br>
<br>

# _공통 인터페이스 기능_
  * 순수 JPA 기반 리포지토리 만들기
  * 스프링 데이터 JPA 공통 인터페이스 소개 
  * 스프링 데이터 JPA 공통 인터페이스 활용

<br>

## _순수 JPA 기반 리포지토리 만들기_
* ### _기본 CRUD_
  * 저장
  * 변경 -> 변경감지 사용
  * 삭제
  * 전체 조회
  * 단건 조회
  * 카운트
  * 참고
    >JPA에서 수정은 변경감지 기능을 사용하면 된다.   
    트랜잭션 안에서 엔티티를 조회한 다음에 데이터를 변경하면, 트랜잭션 종료 시점에 변경감지 기능이 작동해서 변경된 엔티티를 감지하고 UPDATE SQL을 실행한다. 

<br>

* ### _MemberJapRepository_
    ```Java
    @Repository
    @RequiredArgsConstructor
    public class MemberJpaRepository {

        private final EntityManger entityManger;

        public Long save(Member member) {
            entityManager.persist(member);

            return member.getId();
        }

        public void delete(Member member) {
            entityManager.remove(member);
        }

        public List<Member> findAll() {
            return entityManager.createQuery("select m from Member m", Member.class)
                    .getResultList();
        }

        public Optional<Member> findById(Long id) {
            Member member = entityManger.find(Member.class, id);

            return Optional.ofNullable(member);
        }

        public Member find(Long id) {
            return entityManger.find(Member.class, id);
        }

        public long count() {
            return entityManger.createQuery("select count(m) from Member m", Long.class)
                .getSingleResult();
        }   
    }
    ```

<br>

* ### _TeamJpaRepository_
    ```Java
    @Repository
    @RequiredArgsConstructor
    public class TeamJpaRespoitory {

        private final EntityManager entityManger;

        public Long save(Team team) {
            entityManger.persist(team)

            return team.getId();
        }

        public void delete(Team team) {
            entityManger.remove(Team team);
        }

        public List<Team> findAll() {
            return entityManger.createQuery("select t from Team f", Team.class)
                    .getResultList();
        }

        public Optional<Team> findById(Long id) {
            Team team = entityManger.find(Team.class, id);

            return Optional.ofNullable(team);
        }

        public Team find(Long id) {
            return entityManager.find(Team.class, id);
        }

        public long count() {
            return entityManger.createQuery("select count(t) from Team t", Long.class)
                    .getSingleResult();
        }

    }
    ```

<br>

* ### _TestCode_
    ```Java
    @SpringBootTest
    @Transactional
    public class memberJpaRepositoryTest {

        @Autowired
        MemberJpaRepository memberJpaRepository;

        @Test
        void testMember() {
            Member member = new Member("memberA");
            Long savedMemberId = memberJpaRepository.save(member);

            Member findMember = memberJpaRepository.find(savedMemberId);

            assertThat(findMember.getId()).isEqualTo(member.getId());
            assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

            assertThat(findMember).isEqualTo(member);  //JPA 엔티티 동일성 보장
        }

        @Test
        void basicCRUD() {
            Member member1 = new Member("member1");
            Member member2 = new Member("member2");

            memberJpaRepository.save(member1);
            memberJpaRepository.save(member2);

            //단건 조회 검증
            Member findMember1 = memberjpaRepository.findById(member1.getId()).get();
            Member findMember2 = memberjpaRepository.findById(member2.getId()).get();

            assertThat(findMember1).isEqualTo(member1);
            assertThat(findMember2).isEqualTo(member2);

            //리스트 조회 검증
            List<Member> all = memberJpaRepository.findAll();
            assertThat(all.size()).isEqualTo(2);

            //삭제 검증
            memberJpaRepository.delete(member1);
            memberJpaRepository.delete(member2);

            long deleteCount = memberJpaRepository.count();
            assertThat(deleteCount).isEqualTo(0);
        }
    }
    ```
  * 기본 CRUD를 검증한다.

<br>

## _공통 인터페이스 설정_
* ### _JavaConfig 설정 - 스프링 부트 사용시 생략 가능_
    ```Java
    @Configuration
    @EnableJpaRepositories(basePackages = "jpabook.jpashop.repositroy")
    public class AppConfig{}
    ```
  * 스프링 부트 사용시 `@SpringBootApplication`위치를 지정(해당 패키지와 하위 패키지 인식)
  * 만약 위치가 달라지면 `@EnableJpaRepositries`필요

<br>

* ### _스프링 데이터 JPA가 구현 클래스 대신 생성_
    ![](img/img340.png)
  * `org.springframework.data.repository.Repository`를 구현한 클래스는 스캔 대상
    * MemberRepository 인터페이스가 동작하는 이유
    * 실제 출력해보기(Proxy)
    * memberRepository.getClass() -> class com.sun.proxyXXX
  * `@Repository` 애노테이션 생량 가능
    * 컴포넌트 스캔을 스프링 데이터 JPA가 자동으로 처리
    * JPA 예외를 스프링 예외로 변환하는 과정도 자동으로 처리

<br>

## _공통 인터페이스 적용_   
순수한 JPA로 구현한 `MemberJpaRepository` 대신에 스프링 데이터 JPA가 제공하는 공통 인터페이스 사용   

<br>

* ### _스프링 데이터 JPA 기반 MemberRepository_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long> {}
    ```
  * Generic
    * T: 엔티티 타입
    * ID: 식별자 타임(PK)

<br>

* ### _MemberRepositoryTest_
    ```Java
    @SpringBootTest
    @Transactional
    public class MemberRepository {

        @Autowired
        MemberRepository memberRepository;

        @Test
        void testMember() {
            //given
            Member member = new Member("memberA");
            Long savedMember = memberRepository.save(member);

            //when
            Member findMember = memberRepository.findById(savedMember.getId()).get();

            //then
            assertThat(findMember.getId()).isEqualTo(member.getId());
            assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
            assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장
        }

        @Test
        void basicCRUD() {
            //given
            Member member1 = new Member("member1");
            Member member2 = new Member("member2");

            memberRepository.save(member1);
            memberRepository.save(member1);

            //단건 조회 검증
            Member findMember1 = memberRepository.findById(member1.getId()).get();
            Member findMember2 = memberRepository.findById(member2.getId()).get();

            assertThat(findMember1).isEqualTo(member1);
            assertThat(findMember2).isEqualTo(member2);

            //리스트 조회 검증
            List<Member> all =  memberRepository.findAll();
            assertThat(all.size()).isEqualTo(2);

            //카운트 검증
            long count = memberRepository.count();
            assertThat(count).isEqualTo(2);

            //삭제 검증
            memberRepository.delete(member1);
            memberRepository.delete(member2);

            assertThat(memberRepository.count()).isEqualTo(0);
        }
    }
    ```
  * 기존 순수 JPA기반 테스트에서 사용했던 코드를 그대로 스프링 데이터 JPA 리포지토리 기반 테스트로 변경해도 동일한 방식으로 동작
  
<br>

* ### _TeamRepository 생성_
    ```Java
    public interface TeamRepository extends JpaRepository<Team, Long>{}
    ```

<br>

## _공통 인터페이스 분석_
* JpaRepository 인터페이스: 공통 CRUD 제공
* 제네릭은 `<엔티티 타임, 식별자 타입>` 설정
* `JpaRepository` 공통 기능 인터페이스
    ```Java
    public interface JpaRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID>{
        ...
    }
    ```
* `JpaRepository`를 사용하는 인터페이스 
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long>{}
    ```

<br>

* ### _공통 인터페이스 구성_
    ![](img/img341.png)

<br>

* ### _주의_
  * `T findOne(ID)` -> `Optional<T> findById(ID)` 변경

<br>

* ### _제네릭 타입_
  * `T`: 엔티티
  * `ID`: 엔티티의 식별자 타입
  * `S`: 엔티티와 그 자식 타입

<br>

* ### _주요 메서드_
  * `save(S)`: 새로운 엔티티를 저장하고 이미 있는 엔티티는 병합한다.
  * `delete(T)`: 엔티티 하나를 삭제한다. 내부에서 `EntityManager.remove()`호출
  * `findById(ID)`: 엔티티 하나를 조회한다. 내부에서 `EntityManger.find()`호출
  * `getOnd(ID)`: 엔티티를 프록시로 조회한다. 내부에서 `EntityManger.getRegerence()`호출
  * `findAll(...)`: 모든 엔티티를 조회한다. 정렬(`Sort`)이나 페이징(`Pageable`)조건을 파라미터로 제공할 수 있다.
  * 참고
    >`JpaRepository`는 대부분의 공통 메서드를 제공한다. 

<br>
<br>
<br>

# _쿼리 메소드 기능_
  * `쿼리 메서드 기능 3가지`
    * 메서드 이름으로 쿼리 생성
    * 메서드 이름으로 JPA NamedQuery 호출
    * `@Query`어노테이션을 사용해서 리퍼지토리 인터페이스에 쿼리 직접 정의

<br>

## _메소드 이름으로 쿼리 생성_   
메서드 이름으로 분석해서 JPQL 쿼리 실행   
이름과 나이를 기준으로 회원을 조회하려면?   

<br>

* ### _순수 JPA Repository_
    ```Java
    @Repository
    @RequiredArgsConstructor
    public class MemberJpaRepository {

        private final EntityManager entityManager;

        public List<Member> findByUsernameAndAgeGreaterThan(String username, int age) {
            return entityManager.createQuery("select m from Member m where m.username = :username and m.age > :age", Member.class)
                    .setParameter("username", username);
                    .setParameter("age", age);
                    .getResultList();
        }
    }
    ```

<br>

* ### _순수 JPA TestCode_
    ```Java
    @SpringBootTest
    @Transactional
    public class MemberJpaRepositoryTest {

        @Test
        void findUsernameAndAgeGreaterThan() {
            //given
            Member member1 = new Member("AAA", 10);
            Member member2 = new Member("AAA", 20);

            memberJpaRepository.save(member1);
            memberJpaRepository.save(member2);

            //when
            List<Member> result = memberJpaRepository.findByUsernameAndAgeGreaterThan("AAA", 9);

            //then
            assertThat(result.get(0).getUsername()).isEqualTo("AAA");
            assertThat(result.get(0).getAge()).isEqualTo(10);
            assertThat(result.size()).isEqualTo(1);
        }
    }
    ```

<br>

* ### _스프링 데이터 JPA_
    ```Java
    public interface MemberRepositroy extends JpaRepository<Member, Long> {

        List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
    }
    ```
  * 스프링 데이터 JPA는 메소드 이름을 분석해서 JPQL을 생성하고 실행

<br>

* ### 쿼리 메서드 필터 조건
  * [스프링 데이터 JPA 공식 문서 참고](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation)

<br>

* ### _스프링 데이터 JPA가 제공하는 쿼리 메서드 기능_
  * [조회: find...By, read...By, query...By get...By,](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation)
    * 예) findHelloBy 처럼 ...에 식별하기 위한 내용(설명)이 들어가도 된다.
  * COUNT: count...By 반환타입 `long`
  * EXISTS: exists...By 반환타입 `boolean`
  * 삭제: delete....By, remove...By 반환타입 `long`
  * DISTINCT: findDistinct, findMemberDistinctBy
  * [LIMIT: findFirst, findTop, findTop3](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result)
  * 참고
    >이 기능은 엔티티의 필드명이 변경되면 인터페이스에 정의한 메서드 이름도 꼭 함께 변경해야 한다.   
    그렇지 않으면 애플리케이션을 시작하는 시점에 오류가 발생한다.   
    이렇게 애플리케이션 로딩 시점에 오류를 인지할 수 있는 것이 스프링 데이터 JPA의 매우 큰 장점이다.    

<br>

## _JPA NamedQuery_
* JPA의 NamedQuery를 호출할 수 있다.
* `@NamedQuery` 어노테이션으로 Named쿼리 정의
    ```Java
    @Entity
    @NamedQuery(
            name = "Member.findByUsername",
            query = "select m from Member m where = :username")
    public class Member {

        ...
    }
    ```

<br>

* ### _JPA를 직접 사용해서 NamedQuery 호출_
    ```Java
    @Repository
    @RequiredArgsConstructor
    public class MemberJapRepository {

        public List<Member> findByUsername(String username) {
            return entityManager.createNamedQuery("Member.findByUsername", Member.classs)
                    .setParameter("username", username)
                    .getResultList();
        }
    }
    ```

<br>

* ### _스프링 데이터 JPA로 NamedQuery 사용_
    ```Java
    @Query(name = "Member.findByUersname")
    List<Member> findByUsername(@Param("username") String username);
    ```
  * `@Query`를 생략하고 메서드 이름으로만 Named 쿼리를 호출할 수 있다.

<br>

* ### _스프링 데이터 JPA로 Named 쿼리 호출_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long> { /* 여기 선언한 Member 도메인 클래스 */

        List<Member> findByUersname(@Param("username") String username);
    }
    ```
  * 스프링 데이터 JPA는 선언한 `"도메인 클래스 + .(점) + 메서드 이름"`으로 Named 쿼리를 찾아서 실행
  * 만약 실행할 Named 쿼리가 없으면 메서드 이름으로 쿼리 생성 전략을 사용한다.
  * 필요하면 전략을 변경할 수 있지만 권장하지 않는다.
    * [참고](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-lookup-strategies)
    * 참고
      >스프링 데이터 JPA를 사용하면 실무에서 Named Query를 직접 등록해서 사요하는 일은 드물다.   
      대신 `@Query`를 사용해서 리퍼지토리 메소드에 쿼리를 직접 정의한다.    

<br>

## _@Query, 리포지토리 메소드에 쿼리 정의하기_
* ### _메서드에 JPQL 쿼리 작성_
    ```Java
    public interface MemberRepository extends JpaRepositroy<Member, Long> {

        @Query("select m from Member m where m.username = :username and m.age = :age")
        List<Member> findUser(@Param("username") String username, @Param("age") int age);
    }
    ```
  * `@org.springframework.data.jpa.repository.Query`어노테이션을 사용
  * 실행할 메서드에 정적 쿼리를 직접 작성하므로 이름 없는 Named 쿼리라 할 수 있음.
  * JPA Named 쿼리처럼 애플리케이션 실행 시점에 문법 오류를 발견할 수 있다(매우 큰 장점)
  * 참고
    >실무에서는 메서드 이름으로 쿼리 생성 기능은 파라미터가 증가하면 매서드 이름이 매우 지저분해진다.   
    따라서 `@Query`기능을 자주 사용하게 된다.    

<br>

## _@Query, 값, DTO 조회하기_
* ### _단순히 값 하나를 조회_
    ```Java
    public interface MemberRepository extends JpaRepositroy<Member, Long> {

        @Query("select m.username from Member m")
        List<String> findUsernameList();
    }
    ```
  * JPA 값 타임(`@Embedded`)도 이 방식으로 조회할 수 있다.

<br>

* ### _DTO로 직접 조회_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long> {

        @Query("select new data.datajpa.dto.MemberDto(m.id, m.username, t.name)" +
                " from Member m join m.team t")
        List<MemberDto> findMemberDto();
    }
    ```
  * 주의!
    >DTO로 직접 조회 하려면 JPA의 `new`명령어를 사용해야 한다.   
    그리고 다음와 같이 생성자가 맞는 DTO가 필요하다(JPA와 사용방식이 동일하다.)   
    
    ```Java
    @Data
    public class MemberDto {

        private Long id;
        private String username;
        private String teamName;

        public MemberDto(Long id, String username, String teamName) {
            this.id = id;
            this.username = username;
            this.teamName = teamName;
        }
    }
    ```
<br>

## _파라미터 바인딩_
* 위치 기반
* 이름 기반
    ```Java
    select m from Member m where m.username = ?0 //위치 기반
    select m from Member m where m.username = :name //이름 기반
    ```

<br>

* ### _파라미터 바인딩_
    ```Java
    public interface MemberRepositroy extends JpaRepositroy<Member Long> {

        @Query("select m from Member m where m.username = :username")
        List<Member> findMembers(@Param("username") String username);
    }
    ```
  * 참고
    >코드 가동성과 유지보수를 위해 이름 기반 파라미터 바인딩을 사용하자.   
    위치기반은 순서 실수가 바꾸면...

<br>

* ### _컬렉션 파라미터 바인딩_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long> {

        @Query("select m from Member where m.username in :names")
        List<Member> findByNames(@Param("names") List<String> names);
    }
    ```
<br> 

## _반환 타입_
* ### _스프링 데이터 JPA는 유연한 반환 타입 지원_
    ```Java
    List<Member> findListByUsername(String name); //컬렉션
    Member findMemberByUsername(String name); //단건
    Optional<Member> findOptionalByUsername(String name); //단건 Optional
    ```
  * [스프링 데이터 JPA 공식 문서](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-return-types)

* ### _TestCode_  
    ```Java
    @Test
    void returnType() {
        //given
        Member member = new Member("Jeon", 29);
        memberRepository.save(member);

        //when
        List<Member> findList = memberRepository.findListByUsername("없는 회원 이름");
        Member findMember = memberRepository.findMemberByUsername("없는 회원 이름");
        Optional<Member> findOptional = memberRepository.findOptrionalByUsername("없는 회원 이름");

        //then
        assertThat(findList.size()).isEqualTo(0);
        assertThat(findMember).isEqualTo(null);
        assertThat(findOptional).isEqualTo(Optional.empty());
    }
    ```
  
<br>

* ### _조회 결과가 많거나 없으면?_
  * 컬렉션
    * 결과 없음: 빈 컬렉션 반환
  * 단건 조회
    * 결과가 없음: `null` 반환
    * 결과 2건 이상: `javax.persistence.NonUniqueResultException` 예외 발생
  * 참고
    >단건으로 지정한 메서드를 호출하면 스프링 테이터 JPA는 내부에서 JPQL의 `Query.getSingleResult()`메서드를 호출한다.   
    이 메서드를 호출했을 때 조회 결과가 없으면 `javax.persistence.NoRewultException`예외가 발생하는데 개발자 입장에서는 다루기가 상당히 불편하다.   
    스프링 데이터 JPA는 단건을 조회할 때 이 예외가 발생하면 예외를 무시하고 대신에 `null`을 반환한다.    

<br>

## _순수 JPA 페이징과 정렬_   
JPA에서 페이징을 어떻게 할 것인가?   
다음 조건으로 페이징과 정렬을 사용하는 예제 코드를 보자.   
* 검색 조건: 나이가 10살
* 정렬 조건: 이름으로 내림 차순
* 페이징 조건: 첫 번째 페이지, 페이지당 보여줄 데이터는 3건

<br>

* ### _JPA페이징 리포지토리 코드_
    ```Java
    @Repository
    @RequiredArgsConstructor
    public class MemberJpaRepository {

        public List<Member> findByPage(int age, int offset, int limit) {
            return entityManager.createQuery("select m from Member m where m.age = :age order by m.username desc", Member.class)
                    .setParameter("age", age)
                    .setFirstResult(offset)
                    .setMaxResult(limit)
                    .getResultList();
        }

        public long totalCount(ints age) {
            return entityManager.createQuery("select count(m) from Member m where m.age = :age", Long.class)
                    .setPrarameter("age", age)
                    .getSingleResult();
        }
    }
    ```

<br>

* ### _JPA 페이징 테스트 코드_
    ```Java
    @Test
    void paging() {
        //given
        memberJpaRespsitory.save(new Member("member1", 10));
        memberJpaRespsitory.save(new Member("member2", 10));
        memberJpaRespsitory.save(new Member("member3", 10));
        memberJpaRespsitory.save(new Member("member4", 10));
        memberJpaRespsitory.save(new Member("member5", 10));

        int age = 10;
        int offset = 0;
        int limit = 3;

        //when
        List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
        long totalCount = memberJpaRepository.totalCount(age);

        //페이지 계산 공식 적용...
        //totalPage = totalCount / size ...
        //마지막 페이지 ...
        //최초 페이지 ...

        //then
        assertThat(members.size()).isEqualTo(3);
        assertThat(totalCount).isEqualTo(5);
    }
    ```

<br>

## _스프링 데이터 JPA 페이징과 정렬_
* ### _페이징과 정렬 파라미터_
  * `org.springframework.data.domain.Sort`: 정렬 기능
  * `org.springframework.data.domain.pageable`: 페이징 기능(내부에 `Sort` 포함)
  
<br>

* ### _특별한 반환 타입_
  * `org.springframework.data.domain.Page`: 추가 count쿼리 결과를 포함하는 페이징
  * `org.springframework.data.domain.Slice`: 추가 count쿼리 없이 다음 페이지만 확인 가능
    * 내부적으로 limit + 1 조회
  * `List`(자바 컬렉션): 추가 count 쿼리 없이 결과만 반환

<br>

* ### _페이징과 정렬 사용 예제_
    ```Java
    Page<Member> findByUsername(String name, Pageagle pageable);  //count 쿼리 사용
    Slice<Member> findByUsername(String name, Pageable pageable); //count 쿼리 사용 안함
    List<Member> findByUsername(String name, Pageable pageable);  //count 쿼리 사용 안함
    List<Member> findByUsername(String name, Sort sort);
    ```
  * 다음 조건으로 페이징과 정렬을 사용하는 예제 코드를 보자
    * 검색 조건: 나이가 10살 
    * 정렬 조건: 이름으로 내림차순
    * 페이징 조건: 첫 번째 페이지, 페이지당 보여줄 데이터는 3건

<br>

* ### _Page 사용 예제 정의 코드_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long> {

        Page<Member> findByPage(int age, Pageable pageable);
    }
    ```

<br>

* ### _Page 사용 예제 실행 코드_
    ```Java
    @Test
    void page() {
        //given
        memberRePository.svae(new Member("member1", 10));
        memberRePository.svae(new Member("member2", 10));
        memberRePository.svae(new Member("member3", 10));
        memberRePository.svae(new Member("member4", 10));
        memberRePository.svae(new Member("member5", 10));

        //when
        PageRequest pageRequset = PageRequset.of(0, 3, Sort.Direction.DESC, "username");
        Page<Member> page = memberRepository(10, pageRequest);

        //then
        List<Member> content = page.getContent();  //조회된 데이터
        assertThat(content.size()).isEqualTo(3);   //조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5);  //전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0);  //페이지 번호
        assertThat(page.getTotalPage()).isEqualTo(2);  //전체 페이지 번호
        assertThat(page.isFrist()).isTrue();  //천번째 항복인가?
        assertThat(page.hasNext()).isTrue();  //다음 페이지가 있는가?
    }
    ```
  * 두 번째 파라미터로 받은 `Pageable`은 인터페이스 이다.
    * 따라서 실제 사용할 때는 해당 인터페이스를 구현한 `org.springframework.data.domain.pageRequest`객체를 사용한다.
  * `pageRequest` 생성자의 첫 번째 파라미터에는 현재 페이지를, 두 번째 파라미터에는 조회할 데이터 수를 입력한다.
    * 여기에 추가로 정보도 파라미터로 사용할 수 있다. 참고로 페이지는 0부터 시작한다.
  * 주의
    >Page는 1부터 시작이 아니라 0부터 시작이다.

<br>

* ### _Page 인터페이스_
    ```Java
    public interface Page<T> extends Slice<T> {

        int getTotalPages();  //전체 페이지 수
        long getTotalElements();  //전체 데이터 수
        <U> Page<U> map<Function<? super T, ? extends U> converter);  //변환기
    }
    ```

<br>

* ### _Slice 인터페이스_
    ```Java
    public interface Slice<T> extends Streamable<T> {

        int getNumber();            //현재 페이지 번호
        int getSize();              //페이지 크기
        int getNumberOfElements();  //현재 페이지에 나올 데이터 수
        List<T> getContent();       //조회된 데이터
        boolean hasContent();       //조회된 데이터 존재 여부
        Sort getSort();             //정렬 정보
        boolean isFrist();          //현재 페이지가 첫 페이지 인지 여부
        boolean isLast();           //현재 페이지가 마지막 페이지 인지 여부
        boolean hasNext();          //다음 페이지 여부
        boolean hasPrevious();      //이전 페이지 여부
        Pageable getPageable();     //페이지 요청 정보
        Pageable nextPageable();    //다음 페이지 객체
        Pageable previousPageable();//이전 페이지 객체
        <U> Slice<U> map(Function<? super T, ? extends U> converter);  //변환기
    }
    ```

<br>

* ### _참고: count 쿼리를 다음과 같이 분리할 수 있음_
    ```Java
    @Query(value = "select m from Member m", 
          countQuery = "select count(m.username) from Member m")
    Page<Member> findMemberAllCountBy(Pageable pageable);
    ```

  * [_Top, First 사용 참고_](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result)
  * `List<Member> findTop3By();`

<br>

* ### _페이지를 유지하면서 엔티티를 DTO로 변환하기_
    ```Java
    Page<Member> page = memberRepositroy.findByAge(10, pageRequset);
    page<MemberDto> dtoPage = page.map(m -> new MemberDto());
    ```

<br>

* ### _실습_
  * Page
  * Slice(count X)추가로 limit + 1을 조회한다. 그래서 다음 페이지 여부 확인(최근 모바일 리스트 생각해보면 됨)
  * List (count X)
  * 카운트 쿼리 분리(이건 복잡한 sql에서 사용, 데이터는 left join, 카운트는 left join 안해도 된다.)
    * 실무에서 매우 중요!!!
  * 참고
    >전체 count 쿼리는 매우 무겁다. 

<br>

## _벌크성 수정 쿼리_
* ### _JPA를 사용한 벌크성 수정 쿼리_
    ```Java
    @Repositroy
    @RequiredArgsConstructor
    public class MemberJpaRepository {

        private final EntityMamager entityManager;

        public int bulkAgePlus(int age) {
            return entityManager.createQuery("update Member m set m.age = m.age + 1 where m.age >= :age")
                  .setParameter("age", age);
                  .excuteUpdate();
        }
    }
    ```
  * `excuteUpdate()`
    * 데이터베이스에서 데이터를 추가(`Insert`), 삭제(`Delete`), 수정(`Update`)하는 SQL문을 실행.
    * 메서드의 반환값은 해당 SQL문 실행에 영향을 받을 row의 수 

<br>

* ### _스프링 데이터 JPA를 사용한 벌크성 수정 쿼리_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long> {

        @Modifying(clearAutomaticlly = true)
        @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
        int bulkAgePlus(@Param("age") ing age);
    }
    ```

<br>

* ### _스프링 데이터 JPA를 사용한 벌크성 수정 쿼리 테스트_
    ```Java
    @Test
    void bulkAgePlus() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when  
        int resultCount = memberRepository.bulkAgePlus(20);

        //then
        assertThat(resultCount).isEqualTo(3);
    }
    ```
  * 벌크설 수정, 삭제 쿼리는 `@Modifying` 어노테이션을 사용
    * 사용하지 않는다면 다음 예외 발생
    * `org.hibernate.hql.internal.QueryExcutionRequestException: Not supported for DML operations`
  * 벌크설 쿼리를 실행하고 나서 영속성 컨텍스트 초기화: `@Modifying(clearAutomatically = true)`
    * 이 옵션의 기본값은 `false`
    * 이 옵션 없이 회원을 `findBy`로 다시 조회하면 영속성 컨텍스트에 과거 값이 남아서 문제가 될 수 있다.
    * 만약 다시 조회해야 하면 꼭 영속성 컨텍스트를 초기화 하자.
  * 참고
    >벌크 연산은 영속성 컨텍스트를 무시하고 실행하기 떄문에, 영속성 컨텍스트에 있는 엔티티의 상태와 DB에 엔티티 상태가 달라질 수 있다.   
    벌크 연산 이후 연속성 컨텍스트를 비워야 한다.     
    1. 영속성 컨텍스트에 엔티티가 없는 상태에서 벌크 연산을 먼저 실행한다.   
    2. 부득이하게 영속성 컨텍스트에 엔티티가 있으면 벌크 연산 직후 영속성 컨텍스트를 초기화 한다.    
<br>

## _@EntityGraph_
연관된 엔티티들을 SQL 한번에 조회하는 방법   
member -> team은 지연로딩(LAZY)관계이다.    
따라서 team의 데이터를 조회할 때 마다 쿼리가 실행된다(N + 1 문제 발생)

<br>

* ### _지연로딩에 의한 N + 1문제_
    ```Java
    @Test
    void findMmeberLazy() {
        //given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        memberRepository.sava(new Member("member1", 10, teamA));
        memberRepository.sava(new Member("member2", 20, teamB));

        //영속성 컨텍스트를 비워 다음 조회시 DB에 직접 Query를 날려 조회하도록 설정한다.
        entityManager.flush();
        entityManager.clear();

        //when
        List<Member> findMembers = memberRepository.findAll();

        //then
        for (Member member : findMembers) {
            System.out.println("member = " + member.getTeam().getName());
        }
    }
    ```
  * 연관된 엔티티를 한번에 조회하려면 페치 조인이 필요하다.

<br>

* ### _JPQL 페치 조인_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long> {

        @Query("select m from Member m fetch join m.team")
        List<Member> findMemberFetchJoin();
    }
    ```
 * 스프링 데이터 JPA는 JPA가 제공하는 엔티티 그래프 기능을 편리하게 사용하도록 도와준다.
 * 이 기능을 사용하면 JPQL 없이 페치 조인을 사용할 수 있다.(JPQL + 엔티티 그래프도 가능)
 
<br>

* ### _EntityGraph_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long> {

        //공통 메서드 오버라이드
        @Overried
        @EntityGraph(attributePaths = {"team"})
        List<Member> findAll();

        //JPQL + 엔티티 그래프
        @EntityGraph(attributePaths = {"team"})
        @Query("select m from Member m")
        List<Member> findMemberEntityGraph();

        //메서드 이름으로 쿼리에서 특히 편리
        @EntityGraph(attributePaths = {"team"})
        List<Member> findEntityGraphByUsername();
    }
    ```

<br>

* ### _EntityGraph 정리_
  * 사실상 페치 조인(Fetch Join)의 간편 버전
  * left outer join 사용

<br>

* ### _NamedEntityGraph 사용 방법_
    ```Java
    @NamedEntityGraph(name = "Member.all", attributeNodes = @NamedAttributeNode("team"))
    @Entity
    public class Member {

        ...
    }
    ```
    ```Java
    @EntityGraph("Member.all")
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();
    ```

<br>

## _JPA Hint & Lock_
* ### _JPA Hint_
  * JPA 쿼리 힌트(SQL 힌트가 아니라 JPA 구현체에게 제공하는 힌트)

<br>

* ### _쿼리 힌트 사용_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long> {

        @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly"), value = "true")
        Member findReadOnlyByUsername(String username);
    }
    ```

<br>

* ### _쿼리 힌틑 사용 확인_
    ```Java
    @Test
    void queryHint() {
        //given
        memberRepository.sava(new Member("member1", 20));

        entityManger.flush();
        entityManger.clear();

        //when
        Member member = memberRepository.findReadOnlyByUsername("member1");
        member.setUsername("member2");

        entityManager.flush();  //Update Query 실행X
        
    }
    ```

<br>

* ### _쿼리 힌트 Page 추가 예제_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long> {

        @QueryHints(value = {@QueryHint(name = "org.hibernate.readOnly", value = "ture")},
              forCounting = true)
        Page<Member> findByUsernames(String name, Pageable pageable);
    }
    ```
  * `org.springframework.data.jpa.repository.QueryHints` 어노테이션을 사용
  * `forCounting`: 반환 타입으로 `Page`인터페이스를 적용하면 추가로 호출하는 페이징을 위한 count 쿼리도 쿼리 힌트 적용(기본값 true)

* ### _Lock_
    ```Java
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findByUsername(String name);
    ```
  * `org.springframework.data.jpa.repository.Lock` 어노테이션을 사용
  * JPA가 제공하는 락은 JPA 16.1 트탠젝션과 락 절을 참고
<br>
<br>
<br>

# _확장 기능_

<br>

## _사용자 정의 리포지토리 구현_
* 스프링 데이터 JPA 리포지토리는 인터페이스만 정의하고 구현체는 스프링이 자동으로 생성
* 스프링 데이터 JPA가 제공하는 인터페이스는 직접 구현해야 하는 기능이 너무 많다.
* 다양한 이유로 인터페이스의 메서드를 직접 구현하고 싶다면?
  * JPA 직접 사용()
  * 스프링 JDBC Template 사용
  * MyBatis 사용
  * 데이터베이스 커넥션 직접 사용 등등...
  * Querydsl 사용

<br>

* ### _사용자 정의 인터페이스_
    ```Java
    public interface MemberRepositoryCustom {

        List<Member> findMemberCustom();
    }
    ```

<br>

* ### _사용자 정의 인터페이스 구현 클래스_
    ```Java
    @RequiredArgsConstructor
    public class MemberRepositoryImpl implements MemberRepositoryCustom {

        private final EntityManager entityManager;

        @Overried
        public List<Member> findMemberCustom() {
            return entityMenager.createQuery("select m from Member m", Member.class)
                    .getResultList();
        }
    }
    ```

<br>

* ### _사용자 정의 인터페이스 상속_
    ```Java
    public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

        ...
    }
    ```

<br>

* ### _사용자 정의 메서드 호출 코드_
    ```Java
    @Tese
    void custom() {
        memberRepository.findMemberCustom();
    }
    ```

<br>

* ### _사용자 정의 인터페이스 구현 클래스_
  * 규칙: 리포지토리 인테페이스 이름 + `Impl`
  * 스프링 데이터 JPA가 인식해서 스프링 빈으로 등록

<br>

* ### _Impl 대신 다른 이름으로 변경하고 싶으면?_
  * XML 설정
    ```XML
    <repositories base-package = "study.datajpa.repository"
                  repository-impl-postfix = "Impl" />
    ```
  * JavaConfig 설정
    ```Java
    @EnableJpaRepositories(basePackages = "study.datajpa.repository",
                          repositoryImplementationPostfix = "Impl")
    ``` 
  * 참고
    >실무에서는 주로 QueryDSL이나 SpringJdbcTemplate을 함께 사용할 떄 사용자 정의 리포지토리 기능 자주 사용
  * 참고
    >항상 사용자 정의 리포지토리가 필요한 것은 아니다.   
    그냥 임의의 리포지토리를 만들어오면 된다.   
    예를들어 MemberQueryRepository를 인터페이스가 아닌 클래스로 만들고 스프링 빈으로 등록해서 그냥 직접 사용해도 된다.   
    물론 이 경우 스프링 데이터 JPA와는 아무런 관계 없이 별도로 동작한다.   

<br>

* ### _사용자 정의 리포지토리 구현 최신 방식_
  > 스프링 데이터 2.x 부터는 사용자 정의 구현 클래스에 리포지스토리 인터페이스 이름 + `Impl`을 적용하는 대신에 사용자 정의 인터페이스명  + `Impl`방식도 지원한다.   
  예를 들어 위 예제의 `MemberRepositoryImpl` 대신에 `MemberRepositoryCustomImpl`같이 구현해도 된다.   

* ### _최신 사용자 저의 인터페이스 구현 클래스 예제_
    ```Java
    @RequiredArgsConstructor
    public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

        private final EntityManager entityManager;

        @Override
        public List<Member> findMemberCustom() {
            return entityManger.createQuery("select m from Member m", Member.class)
                    .getResultList();
        }
    }
    ```
  >기존 방식보다 이 방식이 사용자 정의 인터페이스 이름과 구현 클래스 이름이 비슷하므로 더 직관적이다.   
  추가로 여러 인터페이스를 분리해서 구현하는 것도 가능하기 때문에 새롭게 변경된 이 방식을 사용하는 것을 더 권장한다.   

<br>

## _Auditing_
* ### _엔티티를 생성, 변경할 때 변경한 사람과 시간을 추적하고 싶으면?_
  * 등록일
  * 수적일
  * 등록자
  * 수정자

<br>

* ### _순수 JPA 사용_
  * 우선 등록일, 수정일 등록
    ```Java
    @MappedSuperclass
    @Getter
    public class JpaBaseEntity {

        @Column(updatable = flase)
        private LocalDateTime createDate;
        private LocalDateTime updateDate;

        @PrePersist
        public void prePersist() {
            LocalDateTiem now = LocalDateTime.now();

            createDate = now;
            updateDate = now;
        }

        @PreUpdate
        public void preUpdate() {
            updateDate = LocalDateTime.now();
        }
    }
    ```
  * 엔티티에 상속
    ```Java
    public class Member extends JpaBaseEntity {

        ...
    }
    ``` 
  * 확인 코드
    ```Java
    @Test
    void jpaEventBaseEntity() throws Exception {
        //given
        Member member = new Member("member1");
        memberRepository.save(member);  //@PrePersist

        Thread.sleep(100);
        member.setUsername("member2");

        entityManager.flush();  //@PreUpdate
        entityManager.clear();

        //when
        Member findMember = memberRepository.findById(member.getId()).get();

        //then
        System.out.println("findMember.createDate = " + findMember.getCreateDate());
        System.out.println("findMember.updateDate = " + findMember.getUpdateDate());
    }
    ``` 
  * JPA 주요 이벤트 어노테이션
    * @PrePersist, @PostPersist
    * @PreUpdate, @PostUpdate

<br>

* ### _스프링 데이터 JPA 사용_
  * 설정
    * `@EnableJpaAuditing` -> 스프링 부트 설정 클래스에 적용해야 한다.
    * `@EntityListeners(AuditingEntityListener.class` -> 엔티티에 적용해야 한다
  * 사용 어노테이션
    * `@CreateDate`
    * `@LastModifideDate`
    * `@CreateBy`
    * `@LastModifiedBy`
  * 스프링 데이터 Auditing 적용 - 등록일, 수정일
    ```Java
    @EntityListneers(AuditingEntityListener.class)
    @MappedSuperclass
    @Getter
    public class BaseEntity {

        @CreateDate
        @Column(updateable = false)
        private LocalDateTime createDate;

        @ListModifiedDate
        private LocalDateTime = lastModifiedDate;
    }
    ```
  * 스프링 데이터 Auditing 적용 - 등록자, 수정자
    ```Java
    @EntityListeners(AuditingEntityListener.class)
    @MappedSuperclass
    public class BaseEntity {

        @CreateBy
        @Column(updateable = false)
        private String createBy;

        @LastModifiedBy
        private String lastModifideBy;
    }
    ```  
  * 등록자, 수정자를 처리해주는 `AuditorAware` 스프링 빈 등록
    ```Java
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(UUID.randomUUID().toString());
    }
    ``` 
    * 실무에서는 세션 정보나, 스프링 시큐리티 로그인 정보에서 ID를 받는다.
  * 참고
    >실무에서 대부분의 엔티티는 등록시간, 수정시간이 필요하지만, 등록자, 수정자는 없을 수도 있다.   
    그래서 다음과 같이 Base 타입을 분리하고, 원하는 타입을 선택해서 상속한다. 
  
  * BaseTimeEntity
    ```Java
    public class BaseTiemEntity {

        @CreateDate
        @Column(updateable = false)
        private LocalDateTime = createDate;

        @LastModifiedDate
        private LocaldateTime = lastModifiedDate;
    }
    ``` 
    ```Java
    @EntityListener(AuditingEntityListener.class)
    @MappedSuperclaas
    @Getter
    public class BaseEntity extends BastTiemEntity {

        @CreateBy
        @Column(updateable = false)
        private String createBy;

        @LastModifiedBy
        private String lastModifiedBy;
    }
    ```
  * 참고
    >저장한시점에 등록일, 등록자는 물론이고, 수정일 수정자도 같이 데이터가 저장된다.   
    데이터가 중복 저장되는 것 같지만, 이렇게 해두면 변경 컬럼만 확인해도 마지막에 업데이트한 유저를 확인할 수 있으므로 유지보수 관점에서 편리하다.   
    이렇게 하지 않으면 변경 컬럼이 `null`일때 등록 컬럼을 또 찾아야 한다.   
    참고로 저장시점에 저장데이터만 입력하고 싶으면 `@EnableJpaAuditing(modifyOnCreate = false)`옵션을 사용하면 된다.   

<br>

* ### _전체 적용_
  * `@EntityListeners(AuditingEntityListener.class)`를 생량하고 스프링 데이터 JPA가 제공하는 이벤트를 엔티티 전체에 적용하려면 orm.xml에 다음과 같이 등록하면 된다.
  * `META-INF/orm.xml`
    ```xml
    <?xml version=“1.0” encoding="UTF-8”?>
    <entity-mappings xmlns=“http://xmlns.jcp.org/xml/ns/persistence/orm”
                     xmlns:xsi=“http://www.w3.org/2001/XMLSchema-instance”
                     xsi:schemaLocation=“http://xmlns.jcp.org/xml/ns/persistence/
    orm http://xmlns.jcp.org/xml/ns/persistence/orm_2_2.xsd”
                     version=“2.2">
        <persistence-unit-metadata>
            <persistence-unit-defaults>
                <entity-listeners>
                    <entity-listener
    class="org.springframework.data.jpa.domain.support.AuditingEntityListener”/>
                </entity-listeners>
            </persistence-unit-defaults>
        </persistence-unit-metadata>
    </entity-mappings>   
    ``` 
<br>

## _Web확장 - 도메인 클래스 컨버터_   
HTTP 파라미터로 넘어온 엔티티의 아이디로 엔티티 객체를 찾아서 바인딩   

* ### _도메인 클래스 컨버터 사용 전_
    ```Java
    @RestController
    @RequiredArgsConstructor
    public class MemberController {

        private final MemberRepository mamberRepository;

        @GetMapptin("/members/{id}")
        public String findMember(@PathVariable("id") Long id) {
            Member member = memberRepository.findById(id).get();

            return member.getUesrname();
        }
    }
    ``` 

<br>

* ### _도메인 클래스 컨버터 사용 후_
    ```Java
    @RestController
    @RequiredArgsConstructor
    public class MemberController {

        private final MemberRepository memberRepository;

        @GetMapping("/members2/{id}")
        public String findMember2(@PathVariable("id") Member member) {
            return member.getUsername();
        }
    }
    ``` 
  * HTTP 요청은 회원 `id`를 받지만, 도메인 클레스 컨버터가 중간에 동작해서 회원 엔티티 객체를 반환
  * 도메인 클래스 컨버터도 리퍼지토리를 사용해서 엔티티를 찾음
  * 주의
    >도메인 클래스 컨버터로 엔티티를 파라미터로 받으면, 이 엔티티는 단순 조회용으로만 사용해야 한다.   
    (트랜젝션이 없는 범위에서 엔티티를 조회했으므로, 엔티티를 변경해도 DB에 반영되지 않는다.)    

<br>

## _Web확장 - 페이징과 정렬_    
스프링 데이터가 제공하는 페이징과 정렬 기능을 스프링 MVC에서 편리하게 사용할 수 있다.   

<br>

* ### _페이징과 정렬 예제_
    ```Java
    @RestController
    @RequiredArgsConstructor
    public class MemberController {

        private final MemberRepository memberRepository;

        @GetMapping("/members")
        public Page<MemberDto> list(Pageable pageable) {

            Page<Member> page = memberRepository.finaAll(pageable);
            Page<MemberDto> map = page.map(m -> new MemberDto(m));

            return map;
        }
    }
    ```
  * 파라미터로 `Pageable`을 받을 수 있다.
  * `Pageable`은 인터페이스, 실제는 `org.springframework.data.domain.PageRequest`객체 생성

<br>

* ### _요청 파라미터_
* $ex$. `/members?page=0&size=3&sort=id,desc&sort=username,desc`
  * page: 현재 페이지, `0부터 시작`
  * size: 한 페이지에 노출한 데이터 건수
  * sort: 정렬 조건을 정의한다.
    * 정렬 속성,정렬 속성...(ASC|DESC), 정렬 방향을 변경하고 싶으면 `sort`파라미터 추가(`asc`생량 가능)

<br>

* ### _기본값_
  * 글로벌 실행: 스프링 부트
    ```Java
    spring.data.web.pageable.default-page-size=20  //기본 페이지 사이즈
    spring.data.seb.pageable.max-page-size=2000    //최대 페이지 사이즈
    ``` 
  * 개별 설정
    ```Java
    @RequestMapping(value = "/member_page", method = RequestMethod.GET)
    public String list(@PageableDefault = 12, sort = "username",
                        direction = Sort.Direction.DESC) Pageable pageable) {
        
        ...
    }
    ```

<br>

* ### _접두사_
  * 페이징 정보가 둘 이상이면 접두사로 구분
  * `@Qualifier`에 접두사명 추가 "(접두사명)_xxx"
  * 예졔: `/member?member_page=0&order_page=1`
    ```Java
    public String list(
            @Qualifier("member") Pageable memberPageable,
            @Qualifier("order") Pageable orderPageable, ...)
    ```

<br>

* ### _Page 내용을 DTO로 변환하기_
  * 엔티티를 API로 노출하면 다양한 문제가 발생한다. 그래서 엔티티를 꼭 DTO로 변환하ㅐ서 반환해야 한다. 
  * Page는 `map()`을 지우너해서 내부 데이터를 다른 것으로 변경할 수 있다. 
    ```Java
    @Data
    public class MemberDto {

        private Long id;
        private String username;

        public MemberDto(Member m) {
            this.id = m.getId();
            this.username = m.getUsername();
        }
    }
    ```
  * `Page.map()`사용
    ```Java
    @GetMapptin("/member")
    public String Page<MemberDto> list(Pageable pageable) {

        Page<Member> page = memberRepository.findAll(pageable);
        //Page<MemberDto> pageDto = page.map(m -> new MemberDto(m));
        Page<MemberDto> pageDto = page.map(MemberDto::new);

        return pageDto;
    }
    ``` 
  * `Page.map()`코드 최적화
    ```Java
    @GetMapping("/members")
    public Page<MemberDto> list(Pageable pageable) {

        return memberRepository.findAll(pageable.map(MemberDto::new));
    }
    ``` 

<br>

* ### _Page를 1부터 시작하기_
  * 스프링 데이터는 Page를 0부터 시작한다.
  * 만약 1부터 시작하려면?
  1. Pageable, Page를 파라미터와 응답 값으로 사용하지 않고, 직접 클래스를 만들어서 처리한다. 그리고 직접 PageRequest(Pageable 구현체)를 생성해서 리포지토리에 넘긴다. 물론 응답값도 Page 대신에 직접 만들어서 제공해야 한다.
  2. `spring.data.web.pageable.one-indexed-parameters`를 `true`로 설정해야 한다. 그런데 이 방법은 web에서 `page`파라미터를 `-1`처리 할 뿐이다. 따라서 응닶갑인 `Page`에 모두 0페이지 인텍스를 사용하는 한계가 있다. 

<br>
<br>
<br>

# _스프링 데이터 JPA 분석_

<br>

## _스프링 데이터 JPA 구현체 분석_
* ### _스프링 데이터 JPA가 제공하는 공통 인터페이스 구현체_
  * `org.springframework.data.jpa.repository.support.SimpleJpaRepository`
    ```Java

  @Repository
  @Transactional(readOnly = true)
  public class SimpleJpaRepository<T, ID> ...{
      @Transactional
      public <S extends T> S save(S entity) {
          if (entityInformation.isNew(entity)) {
              em.persist(entity);
              return entity;
          } else {
              return em.merge(entity);
          } 
      }
      ... 
    }
    ```
  * `@Repository`적용: JPA 예외를 스프링이 추상화한 예외로 변환
  * `@Transactional` 트랜젝션 적용
    * JPA의 모든 변경은 프랜잭션 안에서 동작
    * 스프링 데이터 JPA는 변경(등록, 수정, 삭제) 메서

<br>

## _새로운 엔티티를 구별하는 방법_

<br>
<br>
<br>

# _나머지 기능들_

<br>

## _Sepcifications(명세)_

<br>

## _Query By Example_

<br>

## _Projections_

<br>

## _네이티브 쿼리_



