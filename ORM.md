* ### 데이터 베이스 방언
  * 방언: SQL 표준을 지키지 않는 특정 데이터베이스만의 고유한 기능 
  * JPA는 특정 데이터베이스에 종속되지 않는다.
  * 각각의 데이터베이스가 제공하는 SQL 문법과 함수는 조금씩 다르다 
    * 가변 문자: MySQL은 VARCHAR, Oracle은 VARCHAR2
    * 문자열을 자르는 함수: SQL 표준은 SUBSTRING(), Oracle은 SUBSTR()
    * 페이징: MySQL은 LIMIT, Oracle은 ROWNUM
      ![](img/img261.png) 
  * `hibernate.dialect` 속성에 지정
    * H2: org.hibernate.dialect.H2Dialect
    * Oracle 10g: org.hibername.dialect.Oracle10gDialect
    * MySQL: org.hibernate.dialect.MySQL5InnoDBDialect
    * 하이버네이트는 40가지 이상의 데이터베이스 방언 지원
* ### 트랜잭션(Transaction)
  * `데이터베이스의 상태를 변화`시키기 위해서 수행하는 `작업의 단위`
    * 데이터베이스의 상태를 변화시킨다?
    * SQL을 이용해서 데이터베이스를 접근하는 것을 의미한다.
      * SELECT
      * INSERT
      * DELETE
      * UPDATE
    * 작업 단위
      * 많은 SQL 명령문들을 사람이 정의하는 기준에 따라 정하는 것을 의미한다.
        * 작업의 단위는 질의어 한 문장이 아니다.
  * 트랜잭션의 특징
    * `원자성(Atomictiy)`
      * 트랜잭션이 데이터베이스에 모두 반영되던가, 아니면 전혀 반영되지 않아야 한다
      * 트랜잭션은 사람이 설계한 논리적인 작업 단위로서, 일처리는 작업단위 별로 이루어 져야 사람이 다루는데 무리가 없다.
      * 만약 트랜잭션 단위로 데이터가 처리되지 않는다면, 설계한 사람은 데이터 처리 시스템을 이해하기 힘들 뿐만 아니다, 오작동 했을시 원인을 찾기가 매우 힘들어진다.
    * `일관성(Consistnecy)`
      * 트랜잭션의 작업 처리 결과가 항상 일관성 있어야 한다
      * 트랜잭션이 진행되는 동안에 데이터베이스가 변경 되더라도 업데이트된 데이터베이스로 트랜잭션이 진행되는것이 아니라, 처음에 트랜잭션을 진행 하기 위해 참조한 데이터베이스로 진행된다.
      * 이렇게 함으로써 각 사용자는 일관성 있는 데이터를 볼 수 있을 것이다.
    * `독립성(Isolation)`
      * 둘 이상의 트랜잭션이 동시에 실행되고 있을 경우 어떤 하나의 트랜잭션이라도, 다른 트랜잭션의 연산에 개입할 수 없다
      * 하나의 특정 트랜잭션이 완료될떄까지, 다른 트랜잭션이 특정 트랜잭션의 결과를 참조할 수 없다.
    * `지속성(Durability)`
      * 트랜잭션이 성공적으로 완료됐을 경우, 결과는 영구적으로 반영되어야 한다.
  * 트랜잭션의 Commit, Rollback 연산
    * Commit
      * 하나의 트랜잭션이 성공적으로 끝나고, 데이터베이스가 일관성있는 상태에 있을 떄, 하나의 트랜잭션이 끝났다는 것을 알려주기위해 사용하는 연산
      * 수행했던 트랜잭션이 로그에 저장되며, 후에 Rollback 연산을 수행했었던 트랜잭션단위로 하는 것을 도와준다
    * Rollback
      * 하나의 트랜잭션 처리가 비정상적으로 종료되어 트랜잭션의 원자성이 깨진경우, 트랜잭션을 처음부터 다시 시작하거나, 트랜잭션의 부분적으로만 연산된 결과를 다시 취소시킨다
* ### JPA 구동 방식
  ![](img/img262.png)  
    * Persistence.class 에서 시작
    * META-INF/persistence.xml의 설정 정보들을 읽어서 EntitiyManagerFactory.class를 생성한다
    * 필요할 떄마다 EntityManagerFactory에서 EntityManager를 호출해서 실행한다
        ```Java
        package hellojpa;

        public class JpaMain {

            public static void main(String[] args) {
                EntityManagerFactory entityManagerFatory = Persistence.createEntityManagerFactory("hello");

                EntityManager entityManager = entityManagerFatory.createEntityManager();
                //code
                entityManager.clear();

                entityManagerFatory.close();
            }
        }
        ```
        * Persistence.createEntityManagerFatory(Persistence-unitname)
* ### 객체와 테이블을 생성하고 매핑
  ```Java
  package hellojpa;

  @Entity
  public class Member{

      @Id
      private Long id;
      private String name;

      //Getter, Setter ...
  }
  ``` 
  * h2 에서 JDBC URL을 persistence에서 설정한 URL 와 동일하게 맞춪다
    ```
    create table Member(
        id bigint not null,
        name varchar(255),
        primary key (id)
    );
    ```
* ###  실습 - 회원 저장
  * 주의
    * `엔티티 매니저 팩토리`는 하나만 생성해서 애플리케이션 전체에서 공유
    * `엔티티 매니저`는 쓰레드간에 공유X (사용하고 버려야 한다)
    * `JPA의 모든 데이터 변경은 트랜잭션 안에서 실행` 
  * 회원 등록
    ```Java
    package hellojpa;

    public class JpaMain {

        public static void main(String[] args){

            EntityManagerFatory entitiManagerFatory = Persistence.createEntityManagerFactory("hello");

            EntityManager entityManager = entitiManagerFatory.createEntityManager();

            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();

            Member member = new Member();
            member.setId(940415L);
            member.setName("전성규");

            entityManager.persist(member);

            transaction.commit();

            entitiManager.clear();

            entitiManagerFatory.close();
        }
    }
    ```
  * 회원 조회
    ```Java
    package hellojpa;

    public class JpaMain {

        public static void main(String[] args) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

            EntityManager em = emf.createEntityManager();

            EntityTransaction tx = em.getTransaction();
            tx.begin();

            try {
                Member findMember = em.find(Member.class, 1L);
                System.out.println("findMember.Id = " + findMember.getId());
                System.out.println("findMember.Name = " + findMember.getName());

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
            } finally {
                em.clear();
            }
            emf.close();
        }
    }
    ```
  * 회원 삭제
    ```Java
    package hellojpa;

    public class JpaMain {

        public static void main(String[] args) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

            EntityManager em = emf.createEntityManager();

            EntityTransaction tx = em.getTransaction();
            tx.begin();

            try {
                Member findMember = em.find(Member.class, 1L);
                em.remove(findMember);

                tx.commit();
            } catch (Exception e) {
                tx.rollback();
            } finally {
                em.clear();
            }
            emf.close();
        }
    }
    ```
  * 회원 수정
    ```Java
    package hellojpa;

    public class JpaMain {

        public static void main(String[] args) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

            EntityManager em = emf.createEntityManager();

            EntityTransaction tx = em.getTransaction();
            tx.begin();

            try {
                Member findMember = em.find(Member.class, 1L);
                findMember.setName("HelloJPA");
                
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
            } finally {
                em.clear();
            }
            emf.close();
        }
    }
    ```   
---
* ### 영속성 컨텍스트
  * #### JAP 에서 가장 중요한 2가지
    * 객체와 관계형 데이터베이스 매핑하기(Object Relation Mapping)
    * `영속성 컨텍스트`
  * #### 엔티티 매니저 팩토리와 엔티티 매니저
    ![](img/img263.png)
  * #### 영속성 컨텍스트
    * JPA를 이해하는데 가장 중요한 언어
    * `"엔티티를 영구 저장하는 환경"`이라는 뜻
    * `EntityManager.persist(entity)`
      * persist()를 정확하게 정의하자면 entity를 DB에 저장하는 것이 아니라, 영속성 컨텍스트라는 곳에 저장하는것이다!
      * 영속성 컨텍스트는 눈에 보이지 않는 논리적인 개념이다
      * 엔티티 매니저(EntityManager)를 통해서 영속성 컨텍스트에 접근한다.
* ### 엔티티의 생명 주기
  ![](img/img264.png) 
  * 비영속(new/transient)
    * 영속성 컨텍스트와 전혀 관계가 없는 `새로운`상태
  * 영속(managed)
    * 영속성 컨텍스트에 `관리`되는 상태
  * 준영속(detached)
    * 영속성 컨텍스트에 저장되었다가 `분리`된 상태
  * 삭제(removed)
    * `삭제`된 상태
  * #### 비영속
    ![](img/img265.png) 
    ```Java
    public class JpaMain{

        public static void main(String[] args){
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

            EntityManager em = emf.createEntityManager();

            EntityTransaction tx = em.getTransaction();
            tx.begin();

            try{
                //객체를 생성한 상태(비영속)
                Member member = new Member();
                member.setId("member1");
                member.setName("회원1");

                tx.commit();
            } catch(Exception e){
                tx.rollback();
            } finally{
                em.clear();
            }
            emf.close();
        }
    }
    ```
    * 객체를 생성만 해둔 상태 JPA 와 전혀 관계가 없다
  * #### 영속
    ![](img/img266.png) 
    ```Java
    public class JpaMain {

      public static void main(String[] args) {
          EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

          EntityManager em = emf.createEntityManager();

          EntityTransaction tx = em.getTransaction();
          tx.begin();

          try {
              //객체를 생성한 상태(비영속)
              Member member = new Member();
              member.setId(1L);
              member.setName("HelloJPA");

              //객체를 저장한 상태(영속)
              System.out.println("=== BEFORE ===");
              em.persist(member);
              System.out.println("=== AFTER ===");
              
              tx.commit();
          } catch (Exception e) {
              tx.rollback();
          } finally {
              em.clear();
          }
          emf.close();
      }
    }
    ```
    * 결과
      ```
      === BEFORE ===
      === AFTER ===
      Hibernate: 
          /* insert hellojpa.Member
              */ insert 
              into
                  Member
                  (name, id) 
              values
                  (?, ?)
      ``` 
  * #### 준 영속, 삭제
    ```Java
    //회원 엔티티를 영속성 컨텍스트에서 분리, 준영속 상태
    em.detach(member);
    ```
    ```Java
    //객체를 삭제한 상태(삭제)
    em.remove(member);
    ```
  * #### 영속성 컨텍스트의 이점
    * 1차 캐시
    * 동일성(identity)보장
    * 트랜잭션을 지원하는 쓰기 지연(transactional wirte-behind)
    * 변경 감지(Dirty Checking)
    * 지연 로딩(Lazy Loading)
    * 애플리케이션이랑 DB 사이에 무엇인가 중간 계층이 존재한다
    * 중간에 무엇인가 있으므로 인하여 버퍼링이나 캐싱등의 이점을 누릴 수 있다
* ### 엔티티 조회
  ![](img/img267.png) 
  ```Java
  //엔티티를 생성한 상태(비영속)
  Member member = new Member();
  member.setId("member1");
  member.setUsername("회원1");

  //엔티티 영속
  em.persist(member);
  ```
  * #### 1차 캐시에서 조회
    ![](img/img268.png)
    ```Java
    try {
        //비영속
        Member member = new Member();
        member.setId(101L);
        member.setName("HelloJPA");

        //영속(DB에 저장되는 것이 아니다)
        System.out.println("=== BEFORE ===");
        em.persist(member);
        System.out.println("=== AFTER ===");

        Member findMember = em.find(Member.class, 101L);
        System.out.println("findMember.id = " + findMember.getId());
        System.out.println("findMember.name = " + findMember.getName());

        tx.commit();
    } catch (Exception e) {
        tx.rollback();
    } finally {
        em.clear();
    }
    ```
    * em.persist(member);
      * DB가 아닌 영속성 컨텍스트의 1차 캐시에 저장이 된다
      * PK는 @Id로 지정한 id가 되며, Entity(값/value)는 em.persist(member)에서 member 객체 자체가 된다
    * em.find(Member.class, 101L)
      * JPA 는 DB를 조회하는 것이 아니라 1차 캐시를 조회한다
    * 결과
        ```
        === BEFORE ===
        === AFTER ===
        //select query가 나가지 않았다
        findMember.id = 101
        findMember.name = HelloJPA
        Hibernate: 
            /* insert hellojpa.Member
                */ insert 
                into
                    Member
                    (name, id) 
                values
                    (?, ?)        
        ```
        * em.find(Member.class, 101L) 에서 select query가 나가지 않았다.
          * em.persist(member)는 DB가 아닌 영속석 컨텍스트의 1차 캐시에 저장되었기 때문이다
        * em.find(Member.class, 101L)
          PK(101L)로 조회 하면 1차적으로 영속성 컨텍스트 1차 캐쉬를 조회하고 해당 PK와 같은 캐시가 존재할 경우(영속) 그 값을 가져온다 조회할 PK가 1차캐시에 없을 경우(비영속) DB에서 조회하기 시작한다.
  * #### 데이터베이스에서 조회
    ![](img/img269.png)
    ```Java
    try {
        //영속(DB에 저장되는 것이 아니다)
        Member findMember1 = em.find(Member.class, 101L);
        Member findMember2 = em.find(Member.class, 101L);
                
        tx.commit();
    } catch (Exception e) {
        tx.rollback();
    } finally {
        em.clear();
    }
    ```
    * 결과
    ```
    Hibernate: 
        select
            member0_.id as id1_0_0_,
            member0_.name as name2_0_0_ 
        from
            Member member0_ 
        where
            member0_.id=?
    ``` 
    * select query 가 한 번만 나갔다
    * 101L을 처음 가지고 올 떄 JPA 가 DB에서 가지고(select query) 오면서 영속석 컨텍스트 1차 캐시에 올려둔다
    * 두 번쨰 101L을 조회할 떄 JAP 가 영속성 컨텍스트의 1차 캐시를 조회해서 정보를 가져오므로 두 번째 조회에서는 select query가 나가지 않는다
* ### 영속 엔티티의 동일설 보장
    ```Java
    try {
        //영속(DB에 저장되는 것이 아니다)
        Member findMember1 = em.find(Member.class, 101L);
        Member findMember2 = em.find(Member.class, 101L);

        System.out.println("result = " + (findMember1 == findMember2));

        tx.commit();
    } catch (Exception e) {
        tx.rollback();
    } finally {
        em.clear();
    }
    ```
    * 결과
    ```
    Hibernate: 
        select
            member0_.id as id1_0_0_,
            member0_.name as name2_0_0_ 
        from
            Member member0_ 
        where
            member0_.id=?
    result = true
    ```  
    * 1차 캐시로 반복 가능한 읽기(REPEATBLE READ)등급의 트랜잭션 격리 수준을 데이터베이스가 아닌 애플리케이션 차원에서 제공
* ### 엔티티 등록 트랜젝션을 지원하는 쓰기 지연
  ```Java
  public static void main(String[] args) {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

    EntityManager em = emf.createEntityManager();

    //엔티티 매니저는 데이터 변경시 트랜잭션을 시작해야 한다
    EntityTransaction tx = em.getTransaction();
    tx.begin(); //트랜잭션 시작

    try {
        Member memberA = new Member(150L, "A");
        Member memberB = new Member(160L, "B");

        em.persist(member1);
        em.persist(member2);
        System.out.println("==================");
        //여기까지 INSERT SQL을 데이터베이스에 보내지 않느다.
            
        //커밋하는 순간 데이터베이스에 INERT SQL을 보낸다
        tx.commit(); // 트랜잭션 커밋
    } catch (Exception e) {
        tx.rollback();
    } finally {
        em.clear();
    }
    emf.close();
  }
  ```
  * 결과
  ```
  ==================
  Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (name, id) 
        values
            (?, ?)
  Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (name, id) 
        values
            (?, ?)
  ``` 
  * em.persist(memberA);
    ![](img/img270.png)
    * em.persistence(memberA) -> memberA 가 1차 캐시에 들어간다 ->  동시에 JPA가 엔티티(memberA)를 분석해서 INSERT SQL 생성 -> 쓰기 지연 SQL 저장소에 쌍아둔다
  * em.persist(memberB);
    ![](img/img271.png)
    * em.persistence(memberB) -> memberB 가 1차 캐시에 들어간다 -> 동시에 JPA가 엔티티(memberB)를 분석해서 INSERT SQL 생성 -> 쓰기 지연 SQL 저장소에 쌓아둔다
  * transaction.commit();
    ![](img/img272.png)
    * transaction.commit() -> 쓰기 지연 SQL에 있던 애들이 flush(JAP용어)되면서 DB로 날라간고 실제 DB Transection이 발생
* ### 엔티티 수정 - 변경 감지
  ![](img/img273.png)
  ```Java
  EntityManager em = emf.createEntityManager();

  EntityTransaction tx = em.getTransaction();
  tx.begin();

  try {
      // 영속 엔티티 조회
      Member member = em.find(Member.class, 150L);
      // 영속 엔티티 데이터 수정
      member.setName("zzzzzz");

      System.out.println("==================");

      tx.commit();
  } catch (Exception e) {
      tx.rollback();
  } finally {
      em.clear();
  }
  emf.close();
  }
  ```
  * 결과
  ```
  Hibernate: 
      select
          member0_.id as id1_0_0_,
          member0_.name as name2_0_0_ 
      from
          Member member0_ 
      where
          member0_.id=?
  ==================
  Hibernate: 
      /* update
          hellojpa.Member */ update
              Member 
          set
              name=? 
          where
              id=?
  ``` 
  * UPDATE SQL 날라갔다
  * 수정 이후에 다시 em.persist(member);를 써서 data 가 변경되면 DB에 변경해주어야 하는거 아닌가?
  * JPA의 목적은 java 컬랙션 다루듯이 객채를 다루는 것이다
  * 영속성 컨텍스트 안에 비밀이 담겨있다
  * JPA는 tarnsaction.commit()시점에 영속석 컨텍스트 내부적으로 flush()가 호출된다 
  * 1차 캐시에는 @id, Entity, 스냅샷 이 존재한다
    * 스냅샷: 값을 읽어온 최초의 시점(영속성 컨택스트에 들어온)의 상태를 스냅샷으로 떠둔다?(보관한다)
  * JAP는 1차 캐시의 @id, Entity, 스냅샷을 비교한다
  * Entity가 스냅샷이랑 비교해서 변경되었다면 UPDATE Query를 생성하여 쓰기 지연 SQL 저장소에 저장한다
* ### 엔티티 삭제
  ```Java
  //삭제 대상 엔티티 조회
  Member memberA = em.find(Member.class, "memberA");
  em.remove(memberA); //엔티티 삭제
  ```
* ### 플러시
  * 영속성 컨텍스트의 변경내용을 데이터베이스에 반영
  * 간단하게 말해서 쌓아두었던 SQL이 DB로 날라가는것을 말한다
  * 영속성 컨텍스트의 변경 사항과 DB를 맞추는 작업이라 할 수 있다
  * 주의
   * flush() 가 호출된다 해서 DB Transaction Commit이 발생하는 것이 아니라 flush() 는 Query를 보내는 역할을 하고 transcation.commit()에서 commit 이 이루어진다
  * 변경 감지
  * 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
  * 쓰지 지연 SQL 저장소의 쿼리를 데이터베이스에 전송(등록, 수정, 삭제 쿼리) 
* ### 영속석 컨텍스트를 flush() 하는 법
  * #### 직접호출(강제 호출)
    ```Java
    EntityManager em = emf.createEntityManager();

    EntityTransaction tx = em.getTransaction();
    tx.begin();

    try {
        //영속(DB에 저장되는 것이 아니다)
        Member member = new Member(200L, "member200");
        em.persist(member);

        em.flush();
            
        System.out.println("==================");

        tx.commit();
    } catch (Exception e) {
        tx.rollback();
    } finally {
        em.clear();
    }
    emf.close();
    }
    ```
    * 결과
    ```
    Hibernate: 
        /* insert hellojpa.Member
            */ insert 
            into
                Member
                (name, id) 
            values
                (?, ?)
    ==================
    ```
    * ======== 전에 insert query가 나간것을 확인할 수 있다
    * flush()하는 순간에 query 가 DB에 반영된다
    * flush()를 한다고 1차 캐시가 지워지는 것은 아니다
    * 쓰지 지연 SQL 저장소의 query문을 DB에 반영시킨다고 생각하면 될것이다
    * JPA를 통해서 DB를 조회했을 시에 영속성 컨텍스트에 없으면 그게 영속성이 된다
  * #### 트랜젝션 커밋 - 플러시 자동 호출
  * #### JPQL 쿼리 실행 - 플러시 자동 호출
    ```Java
    em.persist(memberA);
    em.persist(memberB);
    em.persist(memberC);

    //중간에 JPQL 실행
    query = em.createQuery("select m form Member m", Member.class);
    List<Member> members = query.getResultList();
    ```
  * #### 플러시 모드 옵션
    ```Java
    em.setFlushMode(FlusModeTpye.COMMIT)
    ```
    * FlushModeType.AUTO
      * 커밋이나 쿼리를 실행할 때 플러시(기본값)
    * FlushModeType.COMMIT
      * 커밋할 때만 플러시
  * #### 플러시는!
    * 영속성 컨텍스트를 비우지 않는다.
    * 영속성 컨텍스트의 변경내용을 데이터베이스에 동기화한다
    * 트랜잭션이라는 작업 단위가 중요하다 -> 커밋 직전에만 동기화 하면 된다.
* ### 준영속 상태로 만드는 방법
  * 영속 -> 준영속
  * 영속 상태의 엔티티가 영속성 컨텍스트에서 분리(detached)
  * 영속성 컨텍스트가 제공하는 기능을 사용 못함
    ```Java
    EntityManager em = emf.createEntityManager();

    EntityTransaction tx = em.getTransaction();
    tx.begin();

    try {
        //영속(DB에 저장되는 것이 아니다)
        Member member = em.find(Member.class, 150L);
        member.setName("AAAAA");

        em.detach(member);

        System.out.println("==================");

        tx.commit();
    } catch (Exception e) {
        tx.rollback();
    } finally {
        em.clear();
    }
    emf.close();
    }
    ```
    * 결과
    ```
    Hibernate: 
        select
            member0_.id as id1_0_0_,
            member0_.name as name2_0_0_ 
        from
            Member member0_ 
        where
            member0_.id=?
    ==================
    ```
    * Updata query가 나간 기록이 없다 
    * em.detach(member);를 하므로 인하여 영속석 컨텍스트에서 관리하지 않기위함, 즉 JPA가 더이상 관리하지 않는다
    * em.detach(entity)
      * 특정 엔티티만 준영속 상태로 전환
    * em.cler()
      * 영속성 컨텍스트를 완전히 초기화
    * em.close()
      * 영속성 컨텍스트를 종료


---
---
* ## 엔티티 매핑
* ### 엔티티 매핑 소개
  * 객체와 테이블 매핑: @Entity, @Table
  * 필드와 컬럼 매핑: @Column
  * 기본 키 매핑: @Id
  * 연관관계 매핑: @ManyToOne, @JoinColumn
* ### 객체와 테이블 매핑
  * #### @Entity
    * `@Entity`가 붙은 클래스는 JPA가 관리하며, 이를 `엔티티`라 부른다
    * JPA를 사용해서 테이블과 매핑할 클래스는 `@Entity`필수
    * 주의
      * `기본 생성자 필수`(파라미터가 없는 public 또는 protected 생성자)
      * final 클래스, enum, interface, inner 클래스 사용X
      * 저장할 필드에 final 사용X
  * #### @Entity 속성 정리
    * 속성: name
      * JPA에서 사용할 엔티티 이름을 지정한다.
      * 기본값: 클래스 이름을 그대로 사용(예: Member)
      * 같은 클래스 이름이 없으면 가급적 기본값을 사용한다.
  * #### @Table
    * `@Table`은 엔티티와 매핑할 테이블을 지정한다
      * `name`: 매핑할 테이블 이름(기본값 - 엔티티 이름을 사용)
      * `catalog`: 데이터베이스 catalog 매핑
      * `schema`: 데이터베이스 schema 매핑
      * `uniqueConstraints(DLL)`: DLL 생성 시에 유니크 제약 조건 생성
  * ### 예제 코드
    ```Java
    @Entity
    @Tabel(name = "MBR")
    public class Member{ }
    ```
  * ### 실행 결과
    ```
    Hibernate: 
      select
          member0_.id as id1_0_0_,
          member0_.name as name2_0_0_ 
      from
          MBR member0_ 
      where
          member0_.id=?
    ```   
    * `@Table(name = "MBR")` 애노테이션 설정을 통해서 `form MBR member0_` MBR 테이블와 Member 엔티티가 매핑된 것을 확인할 수 있다.
* ### 데이터베이스 스키마 자동 생성
  * DDL을 애플리케이션 실행 시점에 자동 생성
    * DDL(Data Definition Language) - 데이터 정의어
      * 데이터베이스를 정의하는 언어를 말하며 데이터를 생성하거나 수정, 삭제 등 데이터의 전체 골격을 결정하는 역할의 언어
      * CREATE: 데이터 베이스, 테이블 등을 생성하는 역할 
      * ALTER: 테이블을 수정하는 역할 
      * DROP: 데이터베이스, 테이블을 삭제하는 역할 
      * TRUNCATE: 테이블을 초기화 시키는 역할
  * 테이블 중심 -> 객체 중심
  * 데이터베이스 방언을 활용해서 데이터베이스에 맞는 적절한 DDL 생성
    * 애플리케이션 로딩시점에 CREATE 문으로 DB를 생성하고 시작하게 할 수 있다.
    * 보통은 테이블을 다 만들어두고 객체로 돌아와서 개발을 하지만, 이 경우의 장접은 JPA가 객체에 매핑설정을 해두게 되면 애플리케이션이 로딩될 때 필요한 테이블을 만들어준다.
  * 이렇게 `생성된 DDL은 개발 장비에서만 사용`
  * 생성된 DDL은 운영서버에서는 사용하지 않거나, 적절히 다듬은 후 사용
  * #### 속성
    * `/META-INF/persistence.xml/<property name="hibernate.hbm2ddl.auto" value=" " />`
    `create`: 기존테이블 삭제 후 다시 생성(DROP + CREATE)
    `create-drop`: create와 같으나 종료시점에 테이블 DROP
    `update`: 변경부분만 반영(운영 DB에서는 사용하면 안됨)
    `validate`: 엔티티와 테이블이 정상 매핑되었는지만 확인
    `none`: 사용하지 않음
  * #### 주의
    * `운영 장비에는 절대 create, create-drop, update 를 사용하면 안된다.`
    * 개발 초기 단계는 create 또는 update
    * 테스트 서버는 update 또는 validate
    * 스테이징과 운영 서버는 validate 또는 none
  * #### DDL 생성 기능
    * 제약조건 추가: 회원 이름은 필수, 10자 초과X
      ```Java
      @Column(nullable = false, length = 10)
      ```
    * 유니크 제약조건 추가
      ```Java
      @Table(uniqueConstraints = {@UniqueConstraint(name = "NAME_AGE_UNIQUE", columnNames = ("NAME", "AGE"))})
      ```  
    * DDL 생성 기능은 DDL을 자동 생성할 떄만 사용된다, JPA의 실행 로직에는 영향을 주지 않는다.
* ### 필드와 컬럼 매핑
  * #### 요구사항 추가
    1. 회원은 일반 회원과 관리자로 구분해야 한다
    2. 회원 가입일과 수정일이 있어야 한다
    3. 회원을 설명할 수 있는 필드가 있어야 한다. 이 필드는 길이 제한이 없다. 
  * #### 예제 코드
    ```Java
    @Entity
    public class Member{

        //PK Mapping
        @Id 
        private Long id;

        @Colum(name = "name")  //DB Column Name
        private String name;   //Entity Filed Name
        private Integer age;   //다른 타입을 사용할 수 있다(가장 적절한 Type으로 DB에 생성)

        //Entity Filed Type equal Enum
        //DB에는 Enum Type이 존재하지 않는다
        //DB에 Enum Type을 넣고싶은 경우 @Enumerated 사용
        @Enumerated(EnumType.STRING) //반드시 EnumTpye.STRING!!!
        private RoleType roleType;
        
        @Temporal(TemporalType.TIMESTAMP)
        private Date createDate;

        @Temporal(TemporalType.TIMESTAMP)
        private Date lastModifiedDate;

        //VARCHAR를 넘어서는 큰 Type을 DB에 넣고싶은 경우 @Lob
        @Lob
        private String decription;

        //DB랑 관계없이 Memory 영역안에서 해결
        @Transient
        private int temp;

        //Getter, Setter...
    }
    ```  
  * #### 매핑 애노테이션 정리
    * hibername.hbm2ddl.auto
    * `@Column`: 컬럼 매핑
    * `@Temporal`: 날짜 타입 매핑
    * `@Enumerated`: enum 타입 매핑
    * `@Lob`: BLOB, CLOB 매핑
    * `@Transient`" 특정 필드를 컬럼에 매핑하지 않음(매핑 무시)
  * #### @Column
    * `name`: 필드와 매핑할 테이블의 컬럼 이름 (기본값: 객체 필드 이름)
    * `insertable`, `updatable`: 등록, 변경 가능 여부 (기본값: TRUE)
    * `nullable(DDL)`: null값의 허용 여부를 설정한다. false로 설정하면 DDL 생성 시에 NOT NULL 제약조건이 붙는다 (기본값: nullable = true)
    * `unique(DDL)`: @Table의 nuiqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약 조건을 걸 떄 사용한다
      * 이름을 반영하기 어렵기 떄문에 자주 사용하지 않는다
    * `columnDefinition(DDL)`: 데이터베이스 컬럼 정보를 직접 줄 수 있다 (기본값: 필드의 자바 타입과 방언 정보를 사용)
      ```Java
      @Column(name = "name", nullable = false, columnDefinition = "varchar(100) default 'EMPTY'")
      ```
    * `length(DDL)`: 문자 길이 제약조건, String 타입에만 사용한다 (기본값: 255)
    * `precision, scale(DDL)`: BigDecimal 타입에서 사용한다 (BigInteger도 사용할 수 있다.) precision은 소수점을 포함한 전체 자리수를, scale은 소수의 자릿수다. 참고로 double, float 타입에는 정용되지 않는다. 아주 큰 숫자나 정밀한 소수를 다루어야 할 때만 사용한다 (기본값: persision = 19, scale = 2)
  * #### @Enumerated
    * 자바 enum 타입을 매핑할 떄 사용
    * `주의! ORDINAL 사용X`
      * Java의 Enum이 변경되었을 때 ORDINAL 에 KEY는 같지만 VALUE가 다른 중복이 발생할 수 있다.
    * `value`
      * `EnumType.ORDINAL`: enum 순서를 데이터베이스에 저장
      * `EnumType.STRING`: enum 이름을 데이터베이스에 저장
    * 기본값
      * EnumType.OREINAL
      * EnumType.STRING을 반드시 쓰도록 습관을 갖자
  * #### Temporal
    * 날짜 타임(java.util.Date, java.util.Calendar)을 매핑할 때 사용
    * 참고: LocalDate, LocalDateTime을 사용할 떄는 생량 가능(최신 하이버네이트 지원)
    * `valeu`
      * `TemporalType.DATE`: 날짜, 데이터베이스 date 타입과 매핑
        * 예) 2022-02-03
      * `TemporalType.TIME`: 시간, 데이터베이스 time 타입과 매핑
        * 예) 14:05:12
      * `TemporalType.TIMESTAMP`: 날짜와 시간, 데이터베이스 timestamp 타입과 매핑
        * 예) 2022-02-03 14:05:12
  * #### @Lob
    * 데이터베이스 BLOB, CLOB 타입과 매핑
    * @Lob에는 지정할 수 있는 속성이 없다.
    * 매핑하는 필드 타입이 문자면 CLOB매핑, 나머지는 BLOB매핑
      * `CLOB`: String, char[], java.sql.CLOB
      * `BLOB`: byte[], java.sql.BLOB
  * #### @Transient
    * 필드 매핑X
    * 데이터베이스에 저장X, 조회X
    * 주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 떄 사용
* ### 기본 키 매핑
  * #### 기본 키 매핑 어노테이션
    * `@Id`
    * `@GeneratedValue`
      ```Java
      @Id //PK Mapping 
      @GeneratedValue(strategy = GenerationType.AUTO) //default
      private String id;
      ``` 
      ```Java
      try{
          Member member = new Mmeber();
          member.setName("memberA");

          entityManager.persist(member);

          transaction.commit();
      }
      ```
      ```SQL
      Hibernate: 
          
          create table Member (
            id varchar(255) generated by default as identity,
              name varchar(255) not null,
              primary key (id)
          )      
      ```
      * /META-INT/persistence.xml/ 의 방언은 MySQL로 변경하면 방언에 따라 자동으로 auto increment로 변경된다
  * #### 기본 키 매핑 방법
    * 직접 할당: `@Id만 사용`
    * 자동 생성: `@GeneratedValue`
      * `IDENTITY`: 데이터베이스에 위임, MySQL
      * `SEQUENCE`: 데이터베이스 시퀀스 오브젝트 사용, ORACLE
        * `@SequenceGenerator`필요
      * `TABLE`: 키 생성용 테이블 사용, 모든 DB에서 사용
        * `@TableGenerator`필요
      * `AUTO`: 방언에 따라 자동 지정, 기본값
  * #### IDENTITY 전략 - 특징
    * 기본 키 생성을 데이터베이스에 위임
    * 주로 MySQL, PostgreSQL, SQL Server, DB2에서 사용
      * 예) MySQL의 AUTO_INCREMENT
    * JPA는 보통 트랜잭션 커밋 시점에 INSERT SQL 실행
    * AUTO_INCREMENT는 데이터베이스에 INSERT SQL을 실행한 이후에 ID값을 알 수 있음
    * IDENTITY 전략은 entityManager.persist() 시점에 즉시 INSERT SQL을 실행하고 DB에서 식별자를 조회
  * #### IDENTITY 전략 - 매핑
    ```Java
    @Entity
    public class Member{

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    }
    ``` 
    ```Java
    try {
        Member member = new Member();
        member.setName("C");

        System.out.println("================");
        entityManager.persist(member);
        System.out.println("member.id = " + member.getId());
        System.out.println("================");

        transaction.commit();    
    }
    ```
    * Member.id에 값을 초기화하지 않은 상태에서 DB에 INSERT 해야한다.
    * INSERT SQL에 id값이 null로 DB에 넘어오면 그때 DB가 해당 값을 세팅한다.
    * JPA에서 영속성 컨텍스트 1차 캐시에서 관리되기 위해서는 반드시 PK값이 필요하다.
    * id 값(PK)을 알 수 있는 시점은 DB에 들어갔을 떄라는 제약이 발생한다
    * 이런 제약을 해결하기 위해서 일반적으로는 transaction.commit() 시점에 영속성 컨텍스트에서 DB로 Query SQL을 보내지만, IDENTITY 전략의 경우에만 특별하게 emtityManager.persist(entity)를 호출하는 시점에 바로 INSERT SQL을 DB에 보낸다    
      ```SQL
      ================
      Hibernate: 
          /* insert hellojpa.Member
              */ insert 
              into
                  Member
                  (id, name) 
              values
                  (null, ?)
      member.id = 1
      ================
      ```
      * DB는 INSERT SQL에 있는 values의 id값이 null인 것을 확인하고 매핑된 테이블의 id값을 1로 세팅한다
      * JPA는 내부적으로 테이블의 id 값 1을 select 해서 가져오며, 영속성 컨텍스트 1차 캐시에 PK(id)의 value를 1로 세팅한다
  * #### SEQUENCE 전략 - 특징
    * 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트
    * Oracle, PostgreSQL, DB2, H2 데이터베이스에서 사용
  * #### SEQUENCE 전략 - 매핑
    ```Java
    @Entity
    @SequenceGenerator(
        name = "MEMBER_SEQ_GENERATOR",
        sequenceName = "MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
        initialValue = 1, allocationSize = 1)
    public class Member{

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "MEMBER_SEQ_GENERATOR")
        private Long id;
    }
    ```
  * #### SEQUENCE - @SequenceGenerator
    * `주의: allocationSize default Value = 50`
    * `name`: 식별자 생성기 이름 (Default Value: 필수)
    * `sequenceName`: 데이터베이스에 등록되어 있는 시퀀스 이름 (Default Value: hibernate_sequence) 
    * `initialValue`: DDL 생성 시에만 사용됨, 시퀀스 DDL을 생성할 때 처음 1 시작하는 수를 지정한다 (Default Value: 1)
    * `allocationSize`: 시퀀스 한 번 호출에 증가하는 수(성능 최적화에 사용됨)
      * `데이터베이스 시퀀스 값이 하나씩 증가하도록 설정되어 있으면 이 값을 반드시 1로 설정해야 한다` `(Default Value: 50)`
    * `catalog`, `schema`: 데이터베이스 catalog, schema 이름
  * #### SEQUENCE 전략과 최적화
    ```Java
    @Entity
    @SequenceGenerator(
        name = "MEMBER_SEQ_GENERATOR",
        sequenceName = "MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
        initialValue = 1, allocationSize = 1)
    public class Member{

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "MEMBER_SEQ_GENERATOR")
        private Long id;
    }
    ```
    ```Java
    try {
        Member member = new Member();
        member.setName("C");

        System.out.println("================");
        entityManager.persist(member);
        System.out.println("member.id = " + member.getId());
        System.out.println("================");

        transaction.commit();    
    }
    ```
    ```SQL
      /* 1부터 시작해서 1씩 증가 */
      Hibernate: create sequence MEMBER_SEQ start with 1 increment by 1

      ================
      Hibernate: 
          call next value for MEMBER_SEQ
      member.id = 1
      ================
      ```
      * DB에서 id(PK)값을 가져온다음 Member.id에 값을 넣어준다
      * 그 다음 영속성 컨텍스트에 1차 캐시에 저장한다
      * INSERT SQL은 transaction.commit() 시점에 나간다
      * 하지만 이렇게 계속 네트워크를 여러번 타게 되면 성능저하를 고려하게 된다...
    * ##### allocationSize (default: 50)
      * 최초에 call next value를 할때 미리 allocationSize 만큼의 Sequence를 db에 올려둔다
        ```Java
        try {
            Member member1 = new Member();
            member1.setId("tjdrb3807");
            member1.setName("전성규");
            member1.setRolType(RolType.USER);
            member1.setCreateDate(LocalDateTime.now());
      
            Member member2 = new Member();
            member2.setId("tjdwo1386");
            member2.setName("김성재");
            member2.setRolType(RolType.ADMIN);
            member2.setCreateDate(LocalDateTime.now());

            Member member3 = new Member();
            member3.setId("alsdud1234");
            member3.setName("이민영");
            member3.setRolType(RolType.MEMBER);
            member3.setCreateDate(LocalDateTime.now());

            System.out.println("======== persist startLine ========");

            entityManager.persist(member1);
            entityManager.persist(member2);
            entityManager.persist(member3);

            System.out.println("member1.pk = " + member1.getPk());
            System.out.println("member2.pk = " + member2.getPk());
            System.out.println("member3.pk = " + member3.getPk());

            System.out.println("======== persist endLine ========");

            System.out.println("======== commit startLine ========");

            transaction.commit();

            System.out.println("======== commit endLine ========");
        }
        ```  
        ```SQL
        ======== persist startLine ========
        Hibernate: 
            call next value for MEMBER_SEQ
        Hibernate: 
            call next value for MEMBER_SEQ
        member1.pk = 1
        member2.pk = 2
        member3.pk = 3
        ======== persist endLine ========
        ======== commit startLine ========
        Hibernate: 
            /* insert hellojpa.Member
                */ insert 
                into
                    MEMBER
                    (CREATE_DATE, ID, NAME, ROLTYPE, UPDATE_DATE, PK) 
                values
                    (?, ?, ?, ?, ?, ?)
        Hibernate: 
            /* insert hellojpa.Member
                */ insert 
                into
                    MEMBER
                    (CREATE_DATE, ID, NAME, ROLTYPE, UPDATE_DATE, PK) 
                values
                    (?, ?, ?, ?, ?, ?)
        Hibernate: 
            /* insert hellojpa.Member
                */ insert 
                into
                    MEMBER
                    (CREATE_DATE, ID, NAME, ROLTYPE, UPDATE_DATE, PK) 
                values
                    (?, ?, ?, ?, ?, ?)
        ======== commit endLine ========
        ```       
        * MEMBER_SEQ가 두 번 호출되는 이유
        * 첫 번째 호출: DB SEQ = 1  | AP = 1
        * 두 번쨰 호출: DB SEQ = 51 | AP = 2
        * 세 번쨰 호출: DB SEQ = 51 | AP = 3
        * allocationSize = 50으로 설정해서 50개의 메모리를 써야하는데, 처음 호출해봤더니 1이다... 뭔가 문제가 있다 생각하고 한번 더 호출...
        * 인강 한번더 듣기 
  * #### TABLE 전략
    * 키 생성 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉내내는 전략
    * 장점: 모든 데이터베이스에 적용 가능
    * 단점: 성능
  * #### TABLE 전략 - 매핑 
    ```Java
    @Entity
    @TableGenerator(name = "MEMBER_SEQ_GENERATOR",
        table = "MY_SEQUENCES",
        pkColumnValue = "MEMBER_SEQ", allocationSize = 1)
    public class Member{

        @Id
        @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "MEMBER_SEQ_GENERATOR")
        private Long id;
    }
    ```
    ```SQL
    create table MY_SEQUENCE(
        sequence_name varchar(255) not null,
        next_val bigint,
        primary key(sequence_name)
    )
    ```     
  * #### @TableGenerator - 속성
    * `name`: 식별자 생성기 이름 (Default Value: 필수)
    * `table`: 키생성 테이블명 (Default Value: hibernate_sequences)
    * `pkColumnName`: 시퀀스 컬럼명 (Default Value: sequence_name)
    * `valueColumnNa`: 시퀀스 값 컬럼명 (Default Value: next_val)
    * `pkColumnValue`: 키로 사용할 값 이름 (Default Value: Entity Name)
    * `initialVlaue`: 초기 값, 마지막으로 생성된 값이 기준 (Default Value: 0)
    * `allocationSize`: 시퀀스 한 번 호출에 증가하는 수(성능 최적화에 사용) (`Default Value: 50`)
    * `catalog`, `schema`: 데이터베이스 catalog, schema 이름
    * `uniqueConstrains(DDL)`: 유니크 제약 조건을 지정할 수 있다.
  * #### 권장하는 식별자 전략
    * `기본 키 제약 조건`: null 아님, 유일, `변하면 안된다`.
    * 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다. 대리키(대체키)를 사용하자
      * 자연키: 비즈니스 적으로 의미있는 KEY, 주민등록번호나 전화번호 등등...
      * 대체키: SEQUENCE, UUID...
    * 예를 들어 주민등록번호도 기본 키로 적절하지 않다.
    * `권장: Long형 + 대체키 + 키 생성전략 사용`
* ### 실전 예제 - 1.요구사항 분선과 기본 매핑
  * #### 요구사항 분석
    * 회원은 상품을 주문할 수 있다.
    * 주문 시 여러 종류의 상품을 선택할 수 있다.
  * #### 기능 목록
    * 회원 기능
      * 회원 등록
      * 회원 조회
    * 상품 기능
      * 상품 등록
      * 상품 수정
      * 상품 조회
    * 주문 기능
      * 상품 주문
      * 주문내역 조회
      * 주문 취소
  * #### 도메인 도멜 분석
    ![](img/img274.png) 
    * `회원과 주문의 관계`: `회원`은 여러 번 `주문`할 수 있다(일대다)
    * `주문과 상품의 관계`: `주문`할 때 여러 `상품`을 선택할 수 있다. 반대로 같은 `삼품`도 여러 번 주문될 수 있다. 주문상품 이라는 모델을 만들어서 다대다 관계를 일대다, 다대일 관계로 품어냄
  * #### 테이블 설계
    ![](img/img275.png)
  * #### 엔티티 설계와 매핑
    ![](img/img276.png) 
  * #### 데이터 중심 설계의 문제점
    * 현재 방식은 객체 설계를 테이블 설계에 맞춘 방식
    * 테이블의 외래키를 객체에 그대로 가져옴
    * 객체 그래프 탐색이 불가능
    * 참조가 없으므로 UML도 잘못됨
---
---      
<br>
<br>

* # _연관관계 매핑 기초_
* ## _목표_
  * `객체와 테이블 연관관계의 차이를 이해`
  * `객체의 참조와 테이블의 외래 키를 매핑`
  * 용어 이해
    * `방향`(Direction): 단방향, 양방향
    * `다중성`(Multiplicity): 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대대(N:M)
    * `연관관계의 주인`(Owner): 객체 양방향 연관관계는 관리 주인이 필요하다.
<br>
<br>

* ## _연관관계가 필요한 이유_
  ![](img/img277.png)
  * ### 예제 시나리오
    * 회원과 팀이 있다
    * 회원은 하나의 팀에만 소속될 수 있다
    * 회원과 팀은 다대일 관계다. 
  <br>
  <br>

  * ### 객체를 테이블에 맞추어 모델링(연관관계가 없는 객체)
    ![](img/img278.png)
  <br>

  * ### 참조 대신에 외래 키를 그대로 사용
    ```Java
    @Entity
    public class Member {

        @Id
        @Column(name = "member_id")
        @GeneratedValue
        private Long id;
        private String name;
        private Long teamId;

        //default Constructor

        //Getter, Setter
    }
    ```
    ```Java
    @Entity
    public class Team {
        
        @Id
        @Column(name = "team_id")
        @GeneratedValue
        private Long id;
        private String name;
        
        //default Constructor

        //Getter, Setter
    }
    ```
  <br>

  * ### 외래 키 식별자를 직접 다룸
    ```Java
    try {
        //팀 저장
        Team team = new Team();
        team.setName("TeamA");

        entityManager.persist(team);

        //회원 저장
        Member member = new Member();
        member.setName("Jeon");
        member.setTeamId(team.getId());

        entityManager.persist(member);

        transaction.commit();
    } 
    ``` 
  <br>


  * ### 식별자로 다시 조회, 객체지향적인 방법이 아니다..
    ```Java
      //조회
      Member findMember = entityManager.find(Member.class, member.getId());
      System.out.println("findMember.Id = " + findMember.getId());
      System.out.println("findMember.TeamId = " + findMember.getTeamId());
      System.out.println("findMember.Name = " + findMember.getName());

      //연관 관계가 없음
      Team findTeam = entityManager.find(Team.class, team.getId());
      System.out.println("findTeam.Id = " + findTeam.getId());
      System.out.println("findTeam.Name = " + findTeam.getId());
    ``` 
  <br>

  * ### 객체를 테이블에 맞추어 데이터 중심으로 모델링하면, 협력 관계를 만들 수 없다
    * `테이블은 외래 키로 조인`을 사용해서 연관된 테이블을 찾는다
    * `객체는 참조`를 사용해서 연관된 객체를 찾는다
    * 테이블과 객체 사이에는 이러한 큰 간격이 있다.
<br>
<br>

* ## _단방향 연관관계_
  * ### 객체 지향 모델링(객체 연관관계 사용)
    ![](img/img279.png)
    * 객체 연관관계에서 Team.id가 아닌 Team의 참조값을 그대로 가져온다
  <br>

  * ### 객체의 참조와 테이블의 외래 키를 매핑
    ```Java
    @Entity
    public class Member {

        @Id
        @Column(name = "member_id")
        @GeneratedValue
        private Long id;
        private String name;

        @ManyToOne
        @JoinColumn(name = "team_id")
        private Team team;

        //default Structor

        //Getter, Setter
    }
    ```    
  * ### ORM 매핑
    ![](img/img280.png)
  * ### 연관관계 저장
    ```Java
    try {
        //팀 저장
        Team team = new Team();
        team.setName("TeamA");
        entityManager.persist(team);

        //회원 저장
        Member member = new Member();
        member.setName("Jeon");
        member.setTeam(team);  //단방향 연관관계 설정, 참조 저장
        entityManager.persist(member);

        transaction.commit();
    }
    ```  
    ```SQL
    alter table Member 
       add constraint FK5nt1mnqvskefwe0nj9yjm4eav 
       foreign key (team_id) 
       references Team
    ```
  <br>

  * ### 참조로 연관관계 조회 - 객체 그래프 탐색
    ```Java
    entityManager.flush();
    emtityManager.clear();
  
    //조회
    Member findMember = entityManager.find(Member.class, member.getId());

    //참조를 사용해서 연관관계 조회
    Team findTeam = findMember.getTeam();
    System.out.println("findTeam = " + findTeam.getName());
    ```
    ```SQL
    select
        member0_.member_id as member_i1_0_0_,
        member0_.team_id as team_id3_0_0_,
        member0_.username as username2_0_0_,
        team1_.team_id as team_id1_1_1_,
        team1_.name as name2_1_1_ 
    from
        Member member0_ 
    left outer join
        Team team1_ 
            on member0_.team_id=team1_.team_id 
    where
        member0_.member_id=?
    ``` 
    * 영속석 컨텍스트의 1차 캐시가 아닌 DB에서 데이터를 직접 가져오고 싶은 경우
      * `entityManager.flush();`
        * 영속석 컨텍스트의 전송 지연 SQL에 쌓인 Query SQL을 DB에 보낸다
      * `entityManager.clear();`
        * 영속성 컨텍스트의 1차 캐시의 내용을 모두 지운다
  <br>
  <br>

  * ### 연관관계 수정
    ```Java
    try {
        Team team = new Team();
        team.setName("teamB");

        entityManager.persist(team);

        Member findMember = entityManager.find(Member.class, 2L);
        findMember.setTeam(team);

        entityManager.persis(memberl;
    }
    ```
    ```SQL
    insert 
          into
              Team
              (name, team_id) 
          values
              (?, ?)

    update
              Member 
          set
              team_id=?,
              username=? 
          where
              member_id=?
    ```
<br>
<br>

* ## _양방향 연관관계와 연관관계의 주인_
  * ### 양방향 매핑
    ![](img/img281.png)
    * `테이블 연관관계`
      * 단방향 매핑과 양방향 매핑의 테이블 연관관계에는 차이가 없다.
      * 멤버 테이블에서 멤버의 팀을 알고싶으면 
        * MEMBER.TEAM_ID(FK)를 TEMA.TEAM_ID(PK)와 JOIN
      * 팀 테이블에서 팀에 소속된 멤버를 알고싶으면
        * TEAM.TEAM_ID(PK)를 MEMBER.TEAM_ID(FK)와 JOIN
      * `즉 테이블은 외래키 하나로 양방향 연관관계가 성립힌다.`
        * 사실상 테이블의 연관관계에는 방향이라는 개념이 없다고 볼 수 있다. 
    * `객체 연관관계`
      * 단방향 매핑에서는 Member Entity에 Team team 필드(참조)가 있으므로 멤버의 팀을 확인할 수 있다.
      * 하지만 역으로 Team Entity에는 Member에 대한 필드(참조)가 없으므로 팀에 소속된 멤버를 확인할 수 없다.
      * 그러므로 객체의 양방향 연관관계를 성립시키기 위해서는 `Tean Entity에 List members 컬렉션을 넣어주어야 양방향 매핑이 성립`된다
  <br>
  <br> 
  * ### Member Entity는 당방향과 동일 
    ```Java
    @Entity
    public class Member {

        @Id
        @Column(name = "member_id")
        @GeneratedValue
        private Long id;

        @ManyToOne
        @JoinColumn(name = "team_id")
        private Team team;
        private String name;

        //default Structor
        
        //Getter, Setter
    }
    ```    
  * ### Team 엔티티는 컬렉션 추가
    ```Java
    @Entity
    public class Team {

        @Id
        @Column(name = "team_id")
        @GeneratedValue
        private Long id;

        @OneToMany(mappedBy = "team")
        private List<Member> members = new ArrayList<>();
        private String name;

        //default Constructor

        //Getter, Setter
    }
    ```
  * ### 반대 방향으로 객체 그래프 탐색
    ```Java
    try {
        Team findTeam = entityManager.find(Team.class, 3L);
        List<Member> members = findTeam.getMembers();
        for (Member member : members) {
            System.out.println("id = " + member.getId());
            System.out.println("team = " + member.getTeam().getName());
            System.out.println("username = " + member.getUsername());
        }
            
        transaction.commit();
    }
    ```
  <br>

  * ### 객체와 테이블이 관계를 맺는 차이
    ![](img/img282.png) 
    * `객체 연관관계 = 2개`
      * 회원 -> 팀 연관관계 1개(단방향)
      * 팀 -> 회원 연관관계 1개(단방향)
    * `테이블 연관관계 = 1개`
      * 회원 <-> 팀의 연관관계 1개(양방향)
  <br>
  <br>

  * ### 객체의 양방향 관계
    * 객체의 `양방향 관계는 사실 양방향 관계가 아니라 서로 다른 단방향 관계 2개다.`
    * 객체를 양방향으로 참조하려면 `단방향 연관관계를 2개` 만들어야 한다.
    * 즉 참조가 각 엔티티마다 존재해야 한다
      ```Java
      class A{
          B b;
      }

      class B{
          A a;
      }
      ``` 
      * A -> B(a.getB())
      * B -> A(b.getA())
  <br>
  <br> 

  * ### 테이블의 양방향 연관관계
    * 테이블은 `외래 키 하나`로 두 테이블의 연관관계를 관리
    * MEMBER.TEAM_ID(FK) 외래 키 하나로 양방향 연관관계를 가진다(양쪽으로 조인할 수 있다.)
      ```SQL
      SELECT * FROM MEMBER M JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID
      SELECT * FROM TEAM T JOIN MEMBER M ON T.TEAM_ID = M.TEAM_ID
      ``` 
  <br>
  <br>

  * ### 둘 중 하나로 외래 키를 관리해야 한다.
    ![](img/img283.png)
    * 만약 멤버를 바꾸고 싶거나 새로운 팀에 들어가고싶은 상황이 주어졌다고 가정해보자. 그렇다면 객체 연관관계에서 Member Entity의 Team team의 필드값을 변경해야하나, Team Entity의 List members 컬렉션의 값을 변경해야 하나? 하는 딜레마가 생기게 된다
  <br>
  <br>

  * ### 연관관계의 주인(Owner)
    * `양방향 매핑 규칙`
      * 객체의 두 관계중 하나를 연관관계의 주인으로 지정
      * `연관관계의 주인만 외래 키를 관리(등록, 수정)`
      * `주인이 아닌쪽은 읽기만 가능`
      * 주인은 mappedBy 속성을 사용해서는 안된다
      * 주인이 아니면 mappedBy 속성으로 주인 지정(어떤 참조 필드에 의하여 매핑되었다)
  <br>
  <br>
   
  * ### 누구를 주인으로?
    ![](img/img284.png) 
    * `FK가 존재하는 테이블과 매핑된 Entity의 참조(필드)가 객체의 양방향 연관관계의 주인`이다.
    * 여기서는 Member.team이 연관관계의 주인
    <br>

    ```Java
    @Entity
    public class Member {

        @ManyToOne
        @JoinColumn(name = "team_id")
        private Team team;
    }
    ```
    ```Java
    @Entity
    public class Team {

        @OneToMany(mappedBy = "team")
        private List<Member> members = new ArrayList<>();
    }
    ``` 
    * 연관관계의 주인 필드에는 mappedBy를 설정할 수 없다.
    * Tean Entity List members 컬렉션에는 mappedBy가 지정되어있는 것으로 보아 List members는 연관관계의 주인이 아니라는 것을 확인할 수 있다.
    * `mappedBy = "team"`이라 지정하므로서 Member Entity의 team 필드에 의해 매핑되었다는 것을 해석을 통해 직관적으로 알 수 있다.
    * 그런다면 다른 상황에서는 어떤 필드를 연관관계의 주인으로 지정해야하는가?
      * 영햔이가 정해준 가이드라인: `FK가 있는 테이블과 매핑된 엔티티의 필드(참조)를 연관관계의 주인으로 정해라!!!`
      * DB 입장에서는 FK가 있는 곳이 "N"이며, FK 가 없는 곳이 "1"이다.
      * 그 말은 DB의 "N"쪽이 연관관계의 주인이 되어야 성능 이슈가 없고 설계도 깔끔해진다하네...
  <br>
  <br>

  * ### 양방향 매핑시 가장 많이 하는 실수(연관관계의 주인에 값을 입력하지 않음)
    ```Java
    try {
        Member member = new Member();
        member.setUsername("전성규");

        entityManager.persist(member);

        Team team = new Team();
        team.setName("강남대학교");
        team.getMembers().add(member);

        entityManager.persist(team);

        transaction.commit();
    }
    ``` 
    ![](img/img322.png)
    * MEMBER TABLE에 있는 TEAM_ID의 값이 null인것을 확인할 수 있다
    * 연관관계의 주인은 Member Entity의 Team team필드(참조)이며, Team Entity의 List members 컬렉션은 연관관계의 주인이 아니다.
    * `team.getMembers().add(member);`는 mappedBy로 지정된 읽기 전용이다.
    * 즉 JAP에서 UPDATE, INSERT Query를 날릴때 mappedBy로 지정된 참조의 변경은 고려하지 않는다.
  <br>
  <br>

  * ### 양방향 매핑시 연관관계의 주인에 값을 입력해야 한다(순수한 객체 관계를 고려하면 항상 양쪽다 값을 입력해야 한다.)
    ```Java
    try {
        Team team = new Team();
        team.setName("강남대학교");

        entityManager.persist(team);

        Member member = new Member();
        member.setUsername("전성규");
        member.setTeam(team); //연관관계 주인에 값을 설정

        entityManager.persist(member);

        transaction.commit();
    }
    ``` 
    ![](img/img323.png)
    * 연관관계의 주인 필드에 값을 주입(`member.setTeam(team);`)하므로 DB의 MEMBER.TEAM_ID의 값이 정상적으로 입력되었다.
  <br>
  <br>

  * ### 양방향 연관관계의 주의 - 실습
    * `순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자`
      ```Java
      try {
          Team team = new Team();
          team.setName("강남대학교");

          entityManager.persist(team);

          Member member = new Member();
          member.setUsername("전성규");
          member.setTeam(team);

          entityManager.persist(member);

          entityManager.flush();
          entityManager.clear();

          Team findTeam = entityManager.find(Team.class, team.getId());

          System.out.println("========= Select Query Start Line ==========");
          List<Member> members = findTeam.getMembers();
          for (Member m : members) {
              System.out.println("m = " + m.getTeam().getName());
          }
          System.out.println("========= Select Query End Line ==========");

          transaction.commit();
      }
      ``` 
      ```SQL
      ========= Select Query Start Line ==========
          select
              members0_.team_id as team_id3_0_0_,
              members0_.member_id as member_i1_0_0_,
              members0_.member_id as member_i1_0_1_,
              members0_.team_id as team_id3_0_1_,
              members0_.username as username2_0_1_ 
          from
              Member members0_ 
          where
              members0_.team_id=?
      m = 강남대학교
      ========= Select Query End Line ==========
      ```  
      * `entityManger.flush()`, `entityManager.clear()`
        * 영속성 컨텍스트에는 Member Entity와 Team Entity의 데이터는 전부 사라지게된다.
      * `entityManager.find(Team.class, team.getId())`
        * DB로부터 Team Entity의 데이터를 가져오며, 영속성 컨텍스트 1차 캐시에 Team Entity의 데이터를 등록한다
      * `findTeam.getMembers()`
        * Member Entity에 대한 데이터가 없으므로 JPA가 DB로 Select Query를 보내서 조회한 Team에 해당하는 Member의 id의 필드값과 일치하는 Member 테이블의 데이터를 가져온다(select Query 발생)
        * Member Entity를 조회(역방향 조회)
      * 하지만 객체 관계를 고려하면 이 부분은 문제가 발생한다
        ```Java
        try {
            Team team = new Team();
            team.setName("강남대학교");

            entityManager.persist(team);

            Member member = new Member();
            member.setUsername("전성규");
            member.setTeam(team);

            entityManager.persist(member);

            //entityManager.flush();
            //entityManager.clear();

            Team findTeam = entityManager.find(Team.class, team.getId());

            System.out.println("========= Select Query Start Line ==========");
            List<Member> members = findTeam.getMembers();
            for (Member m : members) {
                System.out.println("m = " + m.getTeam().getName());
            }
            System.out.println("========= Select Query End Line ==========");

            transaction.commit();
        }
        ```
        ```SQL
        ========== Select Query Start Line ==========
        ========== Select Query End Line ==========
        ```
        * `entityManager.flush()`, `entityManager.clear()`주석처리
          * Team Entity와 Member Entity의 데이터는 영속성 컨텍스트의 1차 캐시에 저장되어있는 상태가 된다.
        * team.members 컬렉션에는 아무 데이터도 존재하지 않는다.
        * 이러한 결과를 초래하므로, 양방향 연관관계 설정에서 객체지향을 고려한다면 양방향 다 값을 설정하는것이 올바르다
    <br>

    * `연관관계 편의 메소드를 생성하자`
      ```Java
      @Entity
      public class Member{

          @ManyToOne
          @JoinColumn(name ="team_id")
          private Team team;

          public void changeTeam(Team team) {
              this.team = team;
              team.getMembers().add(this);
          }
      }
      ``` 
      * this는 나 자신의 인스턴스 즉, Member
      ```Java
      try {
          Team team = new Team();
          team.setName("TeamA");
          entityManager.persist(team);

          Member member = new Member();
          member.setName("Jeon");
          member.setAge(29);
          member.changeTeam(team);
          entityManager.persist(member);
      }
      ```      
      * 영한이의 개인적인 취향
        * 연관관계 편의 메소드나 JPA 상태를 변경하는 메소드는 setXXX를 잘 사용하지 않는다.
        * setXXX가 Java의 getter, setter 관례때문에 로직이 없는 단순한 상황에서만 사용한다.
    * 양방향 매핑시에 무한 루프를 조심하자
      * 예: toString(), lombok, JSON 생성 라이브러리
  <br>
  <br>

  * ### 양방향 매핑 정리
    * `단방향 매핑만으로도 이미 연관관계 매핑은 완료`
    * 양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐
    * JPQL에서 역방향으로 탐색할 일이 많음
    * 단방향 매핑을 잘 하고 양방향은 필요할 때 추가해도 된다(테이블에 영향을 주지 않는다)
  <br>
  <br>

  * ### 연관관계 주인을 정하는 기준
    * 비즈니스 로직을 기준으로 연관관계의 주인을 선택하면 안된다
    * `연관관계의 주인은 외래 키의 위치를 기준으로 정해야 한다`
<br>
<br>

* ## _실전 예제 - 2.연관관계 매핑 시작_
  * ### 테이블 구조
    ![](img/img285.png) 
  * ### 객체 구조
    ![](img/img286.png) 
<br>

---
---
<br>

* # _다양한 연관관계 매핑_
<br>

* ## 연관관계 매핑시 고려사항 3가지
  * 다중성
    * 다대일: `@ManyToOne`
    * 일대다: `@OneToMany`
    * 일대일: `@OneToOne`
    * 다대다: `@ManyToMany`
  * 단방향, 양방향
    * 테이블
      * 외래 키 하나로 양쪽 조인 가능
      * 사실 방향이라는 개념이 없다
    * 객체
      * 참조용 필드가 있는 쪽으로만 참조 가능
      * 한쪽만 참조하면 단방향
      * 양쪽이 서로 참조하면 양방향
        * Member Entity에 Team에 대한 참조(필드)가 있고, Team Entity에 Member에 대한 참조가 있다면, 참조 입장에서는 단방향이 두 개 있는 것이다.
        * 마치 양쪽에서 참조를 거니까 양방향인것처럼 보일뿐, 사실을 단방향이 두 개 있는 것이다.
  * 연관관계의 주인
    * 테이블을 외래 키 하나로 두 테이블이 연관관계를 맺는다.
    * 객체 양방향 관계는 A -> B, B -> A 처럼 참조가 2군데
    * `객체 양방향 관계는 참조가 2군데` 있음, `둘중 테이블의 외래 키 를 관리할 곳을 지정`해야한다.
    * 연관관계의 주인: 외래 키를 관리하는 참조
    * 주인의 반대편: 외래 키에 영향을 주지 않음, 단순 조회만 가능하다.
<br>

* ## 다대일[N:1]
  <br>

  * ### 다대일 단방향
    ![](img/img289.png)
    * DB입장에서 연관관계는 MEMBER(N) : TEAM(1)이며, `'N'쪽에 외래키(FK)가 존재해야 한다.`
    * `외래키(FK)가 존재하는 테이블과 매핑된 Entity에 역방향 Entity의 참조를 넣어두고 매핑한다.`
      ```Java
      @Entity
      public class Member{

          @Id
          @Column(name = "member_id")
          @GeneratedValue
          private Long id;

          @ManyToOne
          @JoinColum(name = "team_id")
          private Team team;
          private String name;

          //default Constructor

          //Getter, Setter
      }
      ``` 
      ```Java
      @Entity
      public class Team{

          @Id
          @Column(name = "team_id")
          @GeneratedValue
          private Long id;

          //default Constructor

          //Getter, Setter
      }
      ```
    <br>
    
    * ### 다다일 단방향 정리
      * 가장 많이 사용되는 연관관계
      * 다대일의 반대는 일대다   
    <br>
  
    * ### 다대일 양방향
      ![](img/img290.png)
      ```Java
      @Entity
      public class Team{

          @Id
          @Column(name = "team_id")
          @GeneratedValue
          private Long int;

          @OneToMany(mappedBy = "team")
          private Lisg<Member> members = new ArrayList<>();

          //default Constructor

          //Getter, Setter
      }
      ``` 
      * 역방항 Entity에 연관관계 주인 Entity 참조를 추가한다
      * 역방향쪽에 연관관계를 추가한다해서 테이블에 영향을 미치지 않는다.
    <br>

    * ### 다대일 양방향 정리
      * 외래 키가 있는 쪽이 연관관계의 주인이다.
      * 양쪽을 서로 참조하도록 개발
  <br>

  * ## _일대다[1:N]_
    * ### 일대다 단방향
      ![](img/img291.png)
      * 테이블 연관관계는 TEAM(1) : MEMBER(N)이며, 다대일 단방향 연관관계처럼 FK가 존재하는 테이블과 매핑된 Entity가 연관관계의 주인이 아닌 `Team Entity가 연관관계의 주인`으로 설정된 모델이다.
      * 즉 Team Entity(1)가 연관관계의 주인으로서 `주 방향에서 외래키를 관리`한다
      * 영한이는 이 모델 사용을 권장하지 않는다.
        * Team Entity가 List Members 컬렉션을 갖고있으며, Member Entity는 Team을 알고싶지 않다는 전제조건이 깔려있다.
        * DB 입장에서는 무조건 테이블 연관관계 `'N'방향에 FK가 세팅`되어야 한다
        * 위의 예제 경우 `Team Entity의 List members의 값을 수정하였을때 다른 테이블(MEMBER)에 있는 FK(TEAM_ID를 update 처리`해야 한다
      ```Java
      @Entity
      public class Memeber{

          @Id
          @Column(name = "member_id")
          @GeneratedValue
          private Long id;
          private String username;

          //default Constructor

          //Getter, Setter
      }
      ``` 
      
      ```Java
      @Entity
      public class Team{

          @Id
          @Column(name = "team_id")
          @GeneratedValue
          private Long id;

          @OneToMany
          @JoinColumn(name = "team_id")
          private List<Member> members = new ArrayList<>();
          private String name;

          //default Constructor

          //Getter, Setter
      }
      ```
      ```Java
        try {
            Member member = new Member();
            member.setUsername("전성규");

            entityManager.persist(member);

            Team team = new Team();
            team.setName("강남대학교");
            team.getMembers().add(member);

            entityManager.persist(team);

            transaction.commit();
        }
      ```
      * `team.getMembers().add(member);`
        * TEAM 테이블에 INSERT 될 내용이 아니다..
      ```SQL
      update
              Member 
          set
              TEAM_ID=? 
          where
              MEMBER_ID=?
      ```
      * UPDATE Query가 추가로 나가는 이유
        * `Team team = new Team()`, `team.setName("TeamA")`
          * 연관관계가 바뀌더라도 이 부분을 수정할떄는 그냥 TEAM 테이블에 INSERT 된다.
        * `team.getMembers().add(member)`
          * Team Entity를 저장하는데 TEAM TABLE의 TEAM_ID를 SQL로 할 방법이 없다...
          * 옆테이블(MEMBER)에 UPDATE Qurey를 보내는 방법 밖에 없다.
          * 따라서 UPDATE Query가 한번 더 호출되는 것이다(성능상의 큰 차이는 없다)
      * 영한이가 이 방법을 잘 사용하지 않는 이유
        * 실무에서는 최소 수십개의 테이블이 엮여서 돌아가는 상황이다. 
        * 이러한 상황에서 다른 테이블의 UPDATE Query가 날아감으로 인하여 운영상의 문제와 복잡도를 초례한다
        * 그래서 영한이는 `일대다 단방향을 하게되면 다대일 양방향을 사용하는 전략`을 세운다
    * ### 일대다 단방향 정리
      * 일대다 단방향은 일대다(1:N)에서 `일(1)이 연관관계의 주인`이다
      * 테이블 일대다 관계는 항상 `다(N) 쪽에 외래 키가 있다`
      * 객체와 테이블의 차이 때문에 반대편 테이블의 외래 키를 관리하는 특이한 구조
      * `@JoinColumn`을 반드시 사용해야한다. 그렇지 않으면 조인 테이블 방식을 사용함(중간에 테이블 하나 추가함)
      * 일대다 단방향 매핑의 단점
        * `주인   Entity가 관리하는 FK가 다른 테이블에 존재`한다
        * 연관관계 관리를 위해 추가로 UPDATE SQL 실행
      * 일대다 단방향 매핑보다는 `다대일 양방향 매핑을 사용`하자
    * ### 일대다 양방향
      ![](img/img292.png)
      ```Java
      @Entity
      public class Team{

          @Id
          @Column(name = "tema_id")
          @GeneratedValue
          private Long id;
          private Sring name;

          @OneToMany
          @JoinColumn(name = "team_id")
          private List<Member> members = new ArrayList<>();

          //default Constructor

          //Getter, Setter
      }
      ``` 
      ```Java
      @Entity
      public class Member{

          @Id
          @Column(name = "member_id")
          @GeneratedValue
          private Long id;

          private String username;

          @ManyToOne
          @JoinColumn(name = "team_id", insertable = false, updatable = false)
          private Team team;

          //default Constructor

          //Getter, Setter
      }
      ```
    * 일대다 양방향 정리
      * 이러한 매핑은 공식적으로 존재하지 않는다
      * 읽기 전용 필드를 사용해서 양방향 처럼 사용하는 방법
        * `@JoinColumn(insertable = false, updatable = false)`
      * `다대일 양방향을 사용하자`
  <br>

  * ## _일대일[1:1]_
    * ### 일대일 관계
      * 일대일 관계는 그 반대도 일대일
      * 주 테이블이나 대상 테이블 중에 외래 키 선택 가능
        * 주 테이블에 외래 키
        * 대상 테이블에 외래 키
      * `외래 키에 데이터베이스 유니크(UNI) 제약조건 추가`
    * ### 일대일: 주 테이블에 외래 키 단방향
      ![](img/img293.png)
      ```Java
      @Entity
      public class Member{

          @Id
          @Column(name = "member_id")
          @GeneratedValue
          private Long id;

          @OneToOne
          @JoinColumn(name = "locker_id")
          private Locker locker;

          private String username;

          //default constructor

          //Getter, Setter
      }
      ```
      ```Java
      @Entity
      public class Locker{

          @Id
          @Column(name = "locker_id")
          @GeneratedValue
          private Long id;

          private String name;

          //default Constructor

          //Getter, Setter
      }
      ```
      ```SQL 
      create table Locker (
        locker_id bigint not null,
          name varchar(255),
          primary key (locker_id)
      )

      create table Member (
        member_id bigint not null,
          username varchar(255),
          locker_id bigint,
          team_id bigint,
          primary key (member_id)
      )
      ```   
      * MEMBER Table에 LOCKER_ID 가 들어간 것을 확인할 수 있다.
      * 다대일(@ManyToOne) 단방향 매핑과 유사
    <br>

    * ### 일대일: 주 테이블에 외래 키 양방향  
      ![](img/img294.png)  
      ```Java
      @Entity

      public classs Locker{

          @Id
          @Column(name = "locker_id")
          @GeneratedValue
          private Long id;

          private String name;

          @OneToOne(mappedBy = "locker")
          private Member member;

          //default Constructor

          //Getter, Setter
      }
      ``` 
      * 다대일 양향뱡 매핑 처럼 `외래 키가 있는 곳이 연관관계의 주인`
      * `반대편은 mappedBy 적용`
    * ### 일대일: 대상 테이블에 외래 키 당방향
      ![](img/img295.png) 
      * `단방향 관계는 JPA 지원X`
    * ### 일대일: 대상 테이블에 외래 키 양방향
      ![](img/img296.png) 
      * Locker Entity의 Member member 참조를 연관관계의 주인으로 잡아서 연결
      * 일대일 관계는 자신 엔티티의 외래 키는 직접 관리해야 한다.
    * ### 일대일 정리
      * 주 테이블에 외래 키
        * 주 객체가 대상 객체의 참조를 가지는 것 처럼, 주 테이블에 외래 키를 두고 대상 테이블을 찾음
        * 객체지향 개발자 선호
        * JPA 매핑 편리
        * 장점: 주 테이블에만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
        * 단점: `값이 없으면 외래 키에 null 허용`
      * 대상 테이블에 외래 키
        * 대상 테이블에 외래 키가 존재
        * 전통적인 데이터베이스 개발자 선호
        * 장점: 주 데이블과 대상 테이블을 일대일에서 일대다 관꼐로 변경할 때 테이블 구조 유지
        * 단정: 프록시 기능의 한계로 지연 로딩으로 설정해도 항상 즉시 로딩됨
  * ## _다대다[N:M]_
    * ### 다대다
      ![](img/img297.png)
      * 실무에서는 사용해서는 안되며, 왜 사용하면 안되는지에 초점을 맞추도록!
      * 관계형 데이터베이스는 `정규화된 테이블 2개로 다대다 관계를 표현할 수 없다`
      * `연결 테이블을 추가해서 일대다, 다대일 관계로 풀어내야한다. `
      ![](img/img298.png)
      * 객체는 컬렉션을 사용해서 객체 2개로 다대다 관계를 성립시킬 수 있다.
      * `@ManyToMany`사용
      * `@JoinTable`로 연결 테이블 지정
      * 다대다 매핑: 단방향, 양방향 가능
    * ### 다대다 단방향
      ```Java
      @Entity
      public class Product{

          @Id
          @Column(name = "product_id")
          @GeneratedValue
          private Long id;

          private Stirng name;

          //default Construcotr

          //Getter, Setter
      }
      ``` 
      ```Java
      @Entity
      public class Member{
      
          @Id
          @Column(name = "member_id")
          @GeneratedValue
          private Long id;

          @ManyToMany
          @JoinTable(name = "Member_Product")
          private List<Product> products = new ArrayList<>();

          private String name;

          //default Constructor

          //Getter, Setter
      }
      ```      

      ```SQL
      Hibernate: 
          
          create table MEMBER_PRODUCT (
            Member_MEMBER_ID bigint not null,
              products_id bigint not null
          )      
      ```
      ```SQL
      Hibernate: 
          
          alter table MEMBER_PRODUCT 
            add constraint FKc6hsxwm11n18ahnh5yvbj62cf 
            foreign key (products_id) 
            references Product
      Hibernate: 
          
          alter table MEMBER_PRODUCT 
            add constraint FK4ibylolqmostllrjdc147aowv 
            foreign key (Member_MEMBER_ID) 
            references Member
      ```
      * 외래 키 제약조건으로 생기는 SQL
    <br> 

    * ### 다대다 양방향
      ```Java
      @Entity
      public class Product{

          @Id
          @Column(name = "product_id")
          @GeneratedValue
          private Long id;

          @ManyToMany(mappedBy = "products")
          private List<Member> members = new ArrayList<>();

          
          private Stirng name;

          //default Construcotr

          //Getter, Setter

      }
      ``` 
    * 다대다 매핑의 한계
      * 편리해 보이지만 실무에서 사용할 수 없다.
      * 연결 테이블이 단순히 연결만 하고 끝나지 않는다.
      * 매핑 정보만 연결 테이블에 들어가고 추가적인 테이터를 담을 수 없다.
      * 개발자가 생각하지 못한 Query가 나간다
    * 다대다 한계 극복
      ![](img/img299.png) 
      * 연결 테이블용 엔티티 추가(연결 테이블을 엔티티로 승격)
      * `@ManyToMany` -> `@OneToMany`, `@ManyToOne`
        ```Java
        @Entity
        public class MemberProduct{

            @Id
            @Column(name = "orders")
            @GeneratedValue
            private Long id;

            @ManyToOne
            @JoinColumn(name = "member_id")
            private Member member;

            @ManyToOne
            @JoinColumn(name = "product_id")
            private Product product;

            private String orderAmount;

            private LocalDateTime orderDate;

            //default Constructor

            //Getter, Setter

        }
        ``` 
        ```Java
        @Entity
        public class Member{

            ...

            @OneToMany(mappedBy = "member")
            private List<MemberProduct> memberProducts = new ArrayList<>();
        }
        ```
        ```Java
        @Entity
        public class Product{

            ...

            @OneToMany(mappedBy = "product")
            private List<MemberProduct> memberProducts = new ArrayList<>();
        }
        ```
  * ### 실전 예제 - 3.다양한 연관관계 매핑
    * 배송, 카테고리 추가 - 엔티티
      ![](img/img300.png)  
      * 주문과 배송은 1:1(@OneToOne)
      * 상품과 카테고리는 N:M(@ManyToMany)
    * 배송, 카테고리 추가 - ERD
      ![](img/img301.png)   
    * 배송, 카테고리 추가 - 엔티티 상세
      ![](img/img302.png)   
    * N:M 관계는 1:N, N:1로
      * 테이블의 N:M 관계는 중간 테이블을 이용해서 1:N, N:1
      * 실전에서는 중간 테이블이 단순하지 않다
      * @ManyToMany는 제약: 필드 추가X, 엔티티 테이블 불일치
      * 실전에서는 `@ManyToMany 사용X`
    * @JoinColumn
      * 외래 키를 매핑할 때 사용
        ![](img/img303.png) 
    * @ManyToOne - 주요 속성
      * 다대일 관계 매핑
        ![](img/img304.png)
    * @OneToMany - 주요 속성
      * 다대일 관계 매핑
        ![](img/img305.png)        
---
---

<br>
<br>

* # _고급 매핑_
* ## _상속관계 매핑_ 
 
  * ### 상속관계 매핑
    * 관계형 데이터베이스는 상속 관계가 존재하지 않는다.
    * 슈퍼타입, 서브타입 관계라는 모델링 기법이 객체 상속과 유사     
      <br>

      ![](img/img306.png)  
      <br>

    * 상속관계 매핑: 객체의 상속 구조와 DB의 슈퍼타입 서브타입 관계를 매핑
    * 슈퍼타입 서브타입 논리 모델을 실제 물리 모델로 구현하는 방법
      * 각각 테이블로 변환 -> `조인 전략`
      * 통합 테이블로 변환 -> `단일 테이블 전략`
      * 서브타입 테이블로 변환 -> `구현 클래스마다 테이블 전략`
  * ### 주요 어노테이션
    * `@Inheritance(strategy = InheritanceType.XXX)`
      * `JOINED`: 조인 전략
      * `SINGLE_TABLE`: 단일 테이블 전략 
      * `TABLE_PER_CLASS`: 구현 클래스마다 테이블 전략
    * `@DiscriminatorColumn(name = "DTYPE")`
    * `@DiscriminatorValue("XXX")`
  <br> 

  * ### 조인 전략
    <br>

    ![](img/img307.png)    
    * ITEM, ALBUM, MOVIE, BOOK 테이블을 만들어서 데이터를 나누고, 조인으로 구성한다
    * 자식 테이블이 부모 테이블의 PK를 받아서 PK + FK로 사용하는 전략
      * 조회할 떄 JOIN을 자주 사용한다 
    * INSERT는 두 번 나가며 PK가 같으므로 PK, FK로 조인해서 데이터를 가져온다
    * 구분을 하기위해 ITEM 에 구분하는 컬럼 DTYPE을 둔다
      * 객체는 타입으로 구분할 수 있지만 테이블은 타입 개념이 존재하지 않는다
      * 타입글 구분할 컬럼을 추가한다
    <br>

      ```Java
      @Entity
      public abstract class Item{

          @Id
          @Column(name = "item_id")
          @GeneratedValue
          private Long id;

          private String name;

          private int price;

          //default Constructor

          //Getter, Setter
      }
      ``` 
      <br>

      ```Java
      @Entity
      public class Album extends Item {

          private String artist;

          //default Constructor

          //Getter, Setter
      }
      ```
      <br>

      ```Java
      @Entity
      public class Movie extends Item{

          private String director;

          private String actor;

          //default Constructor

          //Getter, Setter
      }
      ```
      <br>

      ```Java
      @Entity
      public class Book extends Item{

          private String author;

          private String isbn;

          //default Constructor

          //Getter, Setter
      }
      ```
      <br>

      ```SQL
      Hibernate: 
          
          create table Item (
            DTYPE varchar(31) not null,
              item_id bigint not null,
              name varchar(255),
              price integer not null,
              author varchar(255),
              isbn varchar(255),
              artiest varchar(255),
              actor varchar(255),
              director varchar(255),
              primary key (item_id)
          )
      ```
      * JPA의 기본 전략이 Single Table 이므로 CREATE문이 단일 테이블 전략으로 생성된다. 
      * 조인 전략으로 변경
        <br>

        ```Java
        @Entity
        @Inheritnace(strategy = InheritnaceType.JOINED)
        public class Item {

            ...
        }
        ```
        <br>

        ```SQL
        Hibernate: 
            
            create table Album (
              artiest varchar(255),
                item_id bigint not null,
                primary key (item_id)
            )
        Hibernate: 
            
            create table Book (
              author varchar(255),
                isbn varchar(255),
                item_id bigint not null,
                primary key (item_id)
            )

        Hibernate: 
            
            create table Item (
              item_id bigint not null,
                name varchar(255),
                price integer not null,
                primary key (item_id)
            )

        Hibernate: 
            
            create table Movie (
              actor varchar(255),
                director varchar(255),
                item_id bigint not null,
                primary key (item_id)
            )
        ```
        <br>

      * 실행
      <br>

        ```Java
        try {
            Movie movie = new Movie();
            movie.setDirector("봉준호");
            movie.setActor("송강호");
            movie.setName("기생충");

            entityManager.persist(movie);

            transaction.commint();
        } 
        ```
        <br>
        
        ```SQL
        Hibernate: 
            /* insert hellojpa.mapping.Movie
                */ insert 
                into
                    Item
                    (name, price, item_id) 
                values
                    (?, ?, ?)
        Hibernate: 
            /* insert hellojpa.mapping.Movie
                */ insert 
                into
                    Movie
                    (actor, director, item_id) 
                values
                    (?, ?, ?)
        ```
        <br>

      * 조회
        ```Java
        try {
            Movie movie = new Movie();
            movie.setDirector("봉준호");
            movie.setActor("송강호");
            movie.setName("기생충");
            movie.setPrice(10000);

            entityManager.persist(movie);

            entityManager.flush();
            entityManager.clear();

            Movie findMovie = entityManager.find(Movie.class, movie.getId());
            System.out.println("findMovie = " + findMovie);
        }
        ``` 
        <br>

        ```SQL
        Hibernate: 
            select
                movie0_.item_id as item_id1_2_0_,
                movie0_1_.name as name2_2_0_,
                movie0_1_.price as price3_2_0_,
                movie0_.actor as actor1_3_0_,
                movie0_.director as director2_3_0_ 
            from
                Movie movie0_ 
            inner join
                Item movie0_1_ 
                    on movie0_.item_id=movie0_1_.item_id 
            where
                movie0_.item_id=?
        findMovie.name = 기생충
        ```
        <br>
        
        * 조회할때 join이 필요하다면 join을 지원하며 INSERT SQL을 두번 날리는 것 까지 지원한다
        <br>

      * DTYPE
        ```Java
        @Entity
        @Inheritance(strategy = InheritanceType.JOINED)
        @DiscriminatorColumn
        public abstract Item {

            ...
        }
        ```
        <br>

        ```SQL
        Hibernate: 
            
            create table Item (
              DTYPE varchar(31) not null,
                item_id bigint not null,
                name varchar(255),
                price integer not null,
                primary key (item_id)
            )
        ```
        ![](img/img308.png)
        * `@DiscriminatorColumn`의 default는 Entity 이름이다
        * 이름은 변경하고 싶다면
        <br>

          ```Java
          @Entity
          @Inheritance(strategy = InheritnaceType.JOINED)
          @DiscriminatorValue("M")
          public class Movie extends Item {

            ...
          }
          ``` 
      * 장점
        * 테이블 정규화(테이터가 정규화)
        * 외래 키 참조 무결성 제약 조건 활용가능
          * 제약 조건을 Item에 걸어서 맞출 수 있다
        * 저장공간 효율화
      * 단점
        * 조회시 조인을 많이 사용, 성능 저하
        * 저회 쿼리가 복잡함
        * 데이터 저장시 INSERT SQL 2번 호출
  * 단일 테이블 전략
    ![](img/img309.png)
    * 논리 모델을 한 테이블로 전부 합치고 ALBUM, MOVIE, BOOK을 구분할 컬럼(DTYPE)을 둔다
      ```Java
      @Entity
      @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
      public abstract class Item {

          ...
      }
      ```  
      <br>

      ```SQL
      Hibernate: 
          
          create table Item (
            DTYPE varchar(31) not null,
              item_id bigint not null,
              name varchar(255),
              price integer not null,
              author varchar(255),
              isbn varchar(255),
              artiest varchar(255),
              actor varchar(255),
              director varchar(255),
              primary key (item_id)
          )
      ```      
      * 자식 타입의 테이블은 생성되지 않는다
      ```SQL
      Hibernate: 
          /* insert hellojpa.mapping.Movie
              */ insert 
              into
                  Item
                  (name, price, actor, director, DTYPE, item_id) 
              values
                  (?, ?, ?, ?, 'Movie', ?)
      ``` 
      * INSERT SQL을 한 번만 내보낸다
      * `@DiscriminatorColumn`이 없더도 DTYPE 이 생성된다(구분하려면 필수)
      * 생각해보면 JpaMain을 변경하지 않고 `@DiscriminatorColumn`만 변경했을 뿐인데 조인 전략에서 단일 전략으로 적용되는 것을 확인할 수 있다. 이는 상속매핑의 엄청난 장점이라 할 수 있다
    * 구현 클래스마다 테이블 전략
      ```Java
      @Entity
      @Inheritance(strategy = InheritanceTpye.TABLE_PER_CLASS)
      public abstract class Item {

          ...
      }
      ``` 
      * 각 테이블마다 모든 컬럼은 둔다(중복을 허용한다)
      * `@DiscriminatorColumn`이 의미가 없으므로 생략할 수 있다
      * 부모 클래스를 추상 클래스로 정의
        * `public class Item` 이라 하면 Item 을 상속과 상관없이 독단적으로 쓰는 경우가 있을 수 있다.
        * 이를 방지하기 위해 부모 클래스를 추상클래스(`abstract`)로 설정하여 테이블이 생성되지 않는 확인할 수 있다
      ```SQL
      Hibernate: 
          
          create table Album (
            id bigint not null,
              name varchar(255),
              price integer not null,
              artist varchar(255),
              primary key (id)
          )
      Hibernate: 
          
          create table Book (
            id bigint not null,
              name varchar(255),
              price integer not null,
              author varchar(255),
              isbn varchar(255),
              primary key (id)
          )
      Hibernate: 
          
          create table Movie (
            id bigint not null,
              name varchar(255),
              price integer not null,
              actor varchar(255),
              director varchar(255),
              primary key (id)
          )
      ``` 
      <br>

      * 표면적으로는 이 전략이 괸장히 편리한 전략이라 보일 수 있지만 이 전략에는 치명적인 단점이 존재한다
        ```Java
        try {
            Movie movie  = new Movie();
            movie.setDirector("봉준호");
            movie.setActor("송강호");
            movie.setName("기생충");
            movie.setPrice(10000);

            entityManager.persist(movie);

            entityManager.flush();
            entityManager.clear();

            Item findItem = entityManager.fine(Item.class, movie.getId());
            System.out.println("item = " + item);

            transaction.commint();
        }
        ``` 
        ```SQL
        select union all...
        ```
        * 데이터를 넣을때와 데이터를 조회시 파라미터에 명확한 자식 Entity를 지정해서 조회할 떄는 괜찮은데, 조회시 부모 Entity로 조회를 하게되면 `union all` 로 모든 테이블에 데이터가 있는지 조회하게 되므로 너무 비효율적인 동작이 발생하게 된다.
  * ### @MappedSuperclass
    ![](img/img310.png)
    <br>

    * 공통 매핑 정보가 필요할 떄 사용(id, name)
    * 상속관계 매핑이 아니다.
    * 엔티티가 아니다.
    * 테이블과 매핑이 아니다.
      * 테이블과 관계가 없으며, 단순히 엔티티가 공통으로 사용하는 매핑 정보를 모으는 역할을 한다
      * 주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보를 모을 때 사용한다.
      * DB는 완전히 분리되어 있지만 객체 입장에서는 공통속성 필드를 상속으로 사용하고 싶은 경우 속성만 상속 받아서 사용한다
        ```Java
        @MappedSuperclass
        public abstract class BaseEntity {

            private String createBy;
            private LocalDateTime createDate;
            private String lasgModifiedBy;
            private LocalDateTime lasgModifiedDate;

            //Getter, Setter
        }
        ```
        <br>

        ```Java
        @Entity
        public class Member extends BaseEntity {

            ...
        }
        ```
        <br>

        ```Java
        @Entity
        public class Team extends BaseEntity {
            
            ...
        }
        ```
        ```Java
        try {

            Member member = new Member();
            member.setName("MemberA");
            membre.setCreate("Kim");
            member.setCreateDate(LocalDateTiem.now());

            entityManager.persist(member);

            entityManager.flush();
            entityManager.close();

            transaction.commit();
        }
        ```
        ```SQL
        create table Member (
          MEMBER_ID bigint not null,
            createBy varchar(255),
            createDate timestamp,
            lastModifiedBy varchar(255),
            lastModifiedDate timestamp,
            USERNAME varchar(255),
            LOCKER_ID bigint,
            TEAM_ID bigint,
            primary key (MEMBER_ID)
        )
        create table Team (
          TEAM_ID bigint not null,
            createBy varchar(255),
            createDate timestamp,
            lastModifiedBy varchar(255),
            lastModifiedDate timestamp,
            name varchar(255),
            primary key (TEAM_ID)
        )
        ```
        * `create table BaseEntity` Query가 없는것을 확인할 수 있다.
        * `Column(name = "")`으로 테이블 컬럼 명을 지정할 수 있다. 
    * 부모 클래스를 상속 받는 `자식 클래스에 매핑 정보만 제공`한다
    * 조회, 검색이 불가능 하다(`entityManager.fine(BaseEntity) 불가능`)
    * 직접 생성해서 사용할 일이 없으므로 `추상 클래스 권장`
    * 참고
      * `@Entity`클래스는 엔티티나 `@MappedSuperclass`로 지정한 클래스만 상속이 가능하다
  * ### 실정 예제 - 4.상속관계 매핑

---
---
<br>
<br>

# _프록시와 연관관계 관리_
## _프록시_   
객체는 객체 그래프로 연관된 객체들을 참조를 사용해서 탐색한다.   
하지만 객체는 DB에 저장되어 있으므로 DB로 부터 객체의 데이터를 가져오기위해 SQL을 직접 다루면 처음 실행하는 SQL에 따라 객체 그래프 탐색 범위가 정해지게되며, 객체 그래프로 연관된 객체를 탐색하기에 한계가 발생한다.   
JPA Entity는 이러한 문제를 해결해가 위해 프록시라는 기술을 사용한다.   
`프록시를 사용하면 연관된 객체를 처음부터 DB에서 조회하는 것이 아니라, 실제 사용하는 시점에 즉 원하는 시점에 SQL을 호출해서 DB에서 원하는 데이터를 조회 할 수 있다.`
### _프록시 기초_ 
__entityManager.find() vs entityManager.getReference()__   
    
기존까지는 영속성 컨텍스트를 통해서 데이터베이스에서 데이터를 가져오기 위해서는 `entityManager.find()`메소드를 사용했다. 이번 Chepter에서는 추가적으로 데이터베이스에서 데이터를 가져오는 `entityManager.getReference()`메소드에 대하여 알아보며 두 메소드의 차이점을 확인해보도록한다.   

* `entityManager.find()`: 데이터베이스를 통해서 실제 엔티티를 조회해서 반환   
* `entityManager.getReference()`: 데이터베이스 조회를 미루는 가짜 엔티티(Proxy Entity)를 조회해서 반환   

기존까지는 find()메소드를 호출해서 엔티티를 반환받아 사용해왔다. 그렇다면 getReference()를 호출해서 반환받은 객체는 무엇일까?   
```Java
try {
    Member proxyMemberEntity = entityManager.getReference(Member.class, 5L);
      
    System.out.println("proxyMemberEntity = " + proxyMemberEntity.getClass());

    transaction.commit();
}  
```
```
proxyMemberEntity = class hellojpa.relationmapping.Member$HibernateProxy$XtW2jaMf
```
>보시다시피 getReferencr()로 조회해서 반환받은 객체의 클레스 이름은 기존까지 사용해도던 일반적인 엔티티의 이름이 아닌것을 확인할 수 있다.   
`entityManger.getReference()`를 호출하여 데이터베이스로부터 가져온 엔티티는 실제 엔티티가 아닌 가짜 즉 `프록시 엔티티`이다.    

다음 코드를 통해서 `entityManager.getReference()`의 동작 원리를 파악하도록 하자.
* 코드 
  ```Java
  try {
      System.out.println("=========== Start getReference() ===========");
      Member proxyMemberEntity = entityManager.getReference(Member.class, 5L);
      System.out.println("============ End getReference() ============");

      System.out.println("ID: " + proxyMemberEntity.getId());

      System.out.println("=========== Start Select SQL ===========");
      System.out.println("Username: " + proxyMemberEntity.getUsername());
      System.out.println("=========== End Select SQL ===========");
              
      transaction.commit();
  }
  ```
* 출력 결과와 코드 분석   

  다음 출력 결과를 통해 세가지 사실을 파악할 수 있다. 
  ```
  =========== Start getReference() ===========
  ============ End getReference() ============
  ```
  >위의 코드작성 목적은 데이터베이스에 있는 MEMBER 테이블에서 기본 키(PK) 값이 5인 Member 엔티티를 조회해서 그 데이터를 가져오는 것이었다.   
  그러기 위해서는 서버로부터 SELECT SQL이 나가는 것을 기대했지만 보시다시피 `entityManager.getReference(Member.class, 5L)`호출시점에 아무런 SQL문이 나가지 않았다. 이는 `getReference()`로 조회해서 반환받은것이 기존까지 사용하던 엔티티가 아닌 `프록시 엔티티`라 나타나는 현상이다.
  ```
  ID: 5
  ```
  >`findMember.getId()`를 호출하는 시점에 select SQL이 호출되지 않았다.   
  이는 데이터베이스의 엔티티를 프록시로 조회할때 자연스럽게 나타나는 출력 결과이다.   
  기존에 `entityManager.getReference(Member.class, 5L)`을 입력해서 DB를 조회할 때 식별자 값(PK)을 파라미터로 전달했다. 이후 데이터베이스로부터 반환받는 프록시 엔티티는 이 식별자 값을 보관한다. 따라서 이미 식별자 값을 가지고 있으므로 식별자 값을 조회하는 `findMember.getId()`에서는 SQL이 호출되지 않으며, 프록시를 초기화 하지도 않는다.   
  프록시 초기화에 대해서는 추후에 배우도록 한다.   
  ```SQL
  =========== Start Select SQL ===========
  Hibernate: 
      select
          member0_.member_id as member_i1_1_0_,
          member0_.locker_id as locker_i3_1_0_,
          member0_.team_id as team_id4_1_0_,
          member0_.username as username2_1_0_,
          locker1_.locker_id as locker_i1_0_1_,
          locker1_.name as name2_0_1_,
          team2_.team_id as team_id1_4_2_,
          team2_.name as name2_4_2_ 
      from
          Member member0_ 
      left outer join
          Locker locker1_ 
              on member0_.locker_id=locker1_.locker_id 
      left outer join
          Team team2_ 
              on member0_.team_id=team2_.team_id 
      where
          member0_.member_id=?
  Username: Jeon
  =========== End Select SQL ===========
  ```
  >드디에 위의 코드에서 SQL문이 출력된 결과를 확인해 볼 수 있다.   
  현재 반환받은 프록시(proxyMemberEntity)의 username필드에는 값이 존재하지 않는다. 정확히는 필드가 존재하지 않는다. 따라서 `proxyMemberEntity.getUsername()`이 호출시 username값을 DB에 조회하기 위해 select SQL이 호출된다.   
### _프록시 특징_   
프록시는 실제 클래스를 상속받아 만들어진 엔티티이므로 실제 클래스와 겉모양이 같다.  
이 말은 즉, 실제 클래의 필드와 메소드를 프록시도 똑같이 구성하고 있다고 할 수 있는데 정확한 프록시의 구성은 실제 클레스와는 조금 다르다.   

![](img/img314.png) 
위의 그림을 보면 프록시는 실제 클래스에 존재하는 메소드는 동일하게 가지고 있지만 `Entity target` 이라는 실제 클래스에서는 볼 수 없었던 참조필드가 보인다.   
이는 실제 엔티티를 가리키는 즉 참조하는 역할을 하며 초기값은 `null`로 세팅되어있다.  
또한 `getReference()`로 반환받은 프록시 엔티티의 메소드를 호출하면 프록시 엔티티는 실제 엔티티에게 권한읜 위임하여 실제 엔티티의 메소드가 호출된다.   
하지만 프록시 엔티티의 `Entity target`의 초기값은 `null`로 세팅되어있는데 어떻게 실제 엔티티를 찾아서 `Entity target`의 값을 초기화하는 것일까?   
이는 뒤에 나오는 프록시 객체의 초기와 에서 알아도보록 한다.  
참고로 개발자 입장에서는 실제 엔티티와 프록시 엔티티를 구분하지 않고 사용하면 된다.   

### _프록시 객체의 초기화_   
![](img/img315.png)



  

          
 
* 
    * ### _프록시 객체의 초기화_
      
      * entityManager.getReference(Member.class, member.getId()).getName() 호출시 Member Proxy Entity는 초기와가 된 상태가 아니라면 `Entity target = null`이면 다음 순서를 진행한다.
        1. JPA가 영속성 컨텍스트에 Proxy Entity 초기화 요청
        2. 영속성 컨텍스트가 DB 조회
        3. 실제 Entity(Member)생성 
        4. Entity target = Member Entity 연결 
        * JpaMain에서 두번 호출해서 Query문 출력 회수 확인해볼 것 
    <br>

    * ### _프록시의 특징_
      * Proxy Entity는 처음 사용할 때 한 번만 초기화
      * Proxy Entity를 초기화 할 떄, Proxy Entity가 실제 Entity로 바뀌는 것은 아니다.
        * 초기화되면 Proxy Entity를 통해서 실제 Entity에 접근 가능
          <br>

          ```Java
          try {
              Member member = new Member();
              member.setName("memberA");

              entityManager.persist(member);

              entityManager.flush();
              entityManager.clear();

              Member findMember = entityManager.getReference(Member.class, member.getId());

              Systme.out.println("before findMember = " + findMember.getClass());
              System.out.println("after findMener = " + findMember.getClass());

              transaction.commit();
          }
          ```
          <br>

          ```SQL
          before findMember = class hellojpa.Member$HibernateProxy$iciMAbVb
          after findMember = class hellojpa.Member$HibernateProxy$iciMAbVb
          ```
      <br>

      * Proxy Entity는 원본 Entity를 상속받는다.
        * 따라서 타입 체크시 주의해야한다.(`==` 비교 실패, 대신 `instance of` 사용)
          <br>

          ```Java
          try {
              Member member1 = new Member();
              member1.setName("member1");

              entityManager.persist(member1);

              Member member2 = new Member();
              member2.setName("member2");

              entityManager.persist(member2);

              entityManager.flush();
              entityManager.clear();

              Member m1 = entityManager.find(Member.class, member1.getId());
              Member m2 = entityManager.fine(Member.class, member2.getId());

              System.out.println("m1 == m2: " + (m1.getClass() == m2.getClass()));

              transaction.commit();
          }
          ``` 
          <br>

          ```SQL
          m1 == m2: true
          ```
          <br>

          ```Java
          Member m1 = entityManager.find(Member.class, member.getId());
          Member m2 = entityManager.getReference(Member.class, member.getId());

          Systme.out.println("m1 == m2: " + (m1 == m2));
          ```
          <br>

          ```SQL
          m1 == m2: flase
          ```
          <br>

          ```Java
          try {
              Member member1 = new Member();
              member1.setName("member1");

              entityManager.persist(member1);

              Member member2 = new Member();
              member2.setName("member2");

              entityManager.persist(member2);

              entityManager.flush();
              entityManager.clear();

              Member m1 = entityManager.find(Member.class, member1.getId());
              Member m2 = entityManager.getReference(Member.class, member2.getId());

              locig(m1, m2);

              transaction.commit();
          } catch(Exception e) {
              transaction.rollback();
          } finally {
              entityManager.close();
          }
          entityManagerFatory.clase();

          private static void logic(Member m1, Member m2) {
              System.out.pritln("m1 == m2: " + m1 instanceof Member));
              Systme.out.pritln("m1 == m2: " + m2 instanceof Member));
          }
          ```
      <br>

      * 영속성 컨텍스트에 찾는 Entity 이미 있으면 `entityManager.getReference()`를 호출해도 실제 Entity 반환
        <br>

        ```Java
        try {
            Member member1 = new Member();
            member1.setName("member1");

            entityManager.persist(member1);

            entityManager.flush();
            entityManager.clear();

            Member findMember = entityManager.find(Member.class, member1.getId());

            //영속성 컨텍스트에 올라가게 된다
            System.out.println("findMember = " + findMember.getClass());

            Member reference = entityManager.getReference(Member.class, member1.getId());

            System.out.prtinln("reference = " + reference.getClass());

            transaction.commit();
        }
        ``` 
        <br>

        ```SQL
        indMember = class hellojpa.Member
        reference = class hellojpa.Member
        ```
          <br>

          * reference가 Proxy Entity가 아닌 Member Entity인 두가지 이유가 존재한다
            1. 이미 Member Entity를 영속성 컨텍스트 1차 캐시에 올려두었는데 굳이 Proxy Entity를 가져와봤자 성능상 이점이 없다. 따라서 영속성 컨텍스트에 이미 조회한고자 하는 Entity가 존재한다면 entityManager.getReference()를 호출해도 영속성 컨텍스트 1차 캐시에 존재하는 대상 Entity를 반환해서 성능 최적화를 시킨다
            2. reference가 실제 Entity던 Proxy Entity던 상관없이 JPA는 `==` 비교대상이 한 영속성 컨텍스트에서 가져온 Entity이며, PK가 같으면 항상 `true`를 반환해 주어야 한다. 이것은 JPA가 기본적으로 제공하는 메커니즘중 하나다.(JPA는 항상 한 트랜젝션 안에서의 Entity 동일성을 보장한다)
              <br>
              
              ```Java
              try {
                  Mmeber member = new Member();
                  member.setName("member");

                  entityManager.persist(member);

                  entityManager.flush();
                  entityManager.clear();

                  Member findMember = entityManager.getReference(Member.class, member.getId());
                  System.out.println("findMember = " + findMember.getClass());

                  Member referce = entityManger.getReference(Member.calss, member.getId());
                  System.out.println("reference = " + reference.getClass());

                  transaction.commit();
              }
              ```
              <br>

              ```SQL
              findMember = class hellojpa.Member$HibernateProxy$iNEUVy98
              reference = class hellojpa.Member$HibernateProxy$iNEUVy98
              ```
              * findMember == reference: true를 보장하기 위해서 같은 Proxy가 반환되었다
              <br>
              <br>

              ```Java
              try {
                  Member member = new Member();
                  member.setName("mamber");

                  entityManager.persist(member);

                  entityManeger.flush();
                  entityManager.clear();

                  Member refMember = entityManager.getReference(Member.class, member.getId());
                  System.out.println("refMenber = " + refMember.getClass());

                  Member findMember = entityManager.find(Member.class, member.getId());
                  System.out.println("findMember = " + findMember.getClass());

                  Systme.out.println("refMember == findMember: " + (refMember == findMember));

                  transaction.commit();
              }
              ``` 
              <br>

              ```SQL
              refMember = class hellojpa.Member$HibernateProxy$y3WuDHYI
              Hibernate: 
                  select
                      member0_.MEMBER_ID as MEMBER_I1_3_0_,
                      member0_.createBy as createBy2_3_0_,
                      member0_.createDate as createDa3_3_0_,
                      member0_.lastModifiedBy as lastModi4_3_0_,
                      member0_.lastModifiedDate as lastModi5_3_0_,
                      member0_.USERNAME as USERNAME6_3_0_,
                      member0_.TEAM_ID as TEAM_ID7_3_0_,
                      team1_.TEAM_ID as TEAM_ID1_7_1_,
                      team1_.createBy as createBy2_7_1_,
                      team1_.createDate as createDa3_7_1_,
                      team1_.lastModifiedBy as lastModi4_7_1_,
                      team1_.lastModifiedDate as lastModi5_7_1_,
                      team1_.name as name6_7_1_ 
                  from
                      Member member0_ 
                  left outer join
                      Team team1_ 
                          on member0_.TEAM_ID=team1_.TEAM_ID 
                  where
                      member0_.MEMBER_ID=?
              findMember = class hellojpa.Member$HibernateProxy$y3WuDHYI
              refMember == findMember: true            
              ```
              * `Member findMember = entityManager.find(Member.class, member.getId())`로 조회를 했는데 findMember도 Proxy Entity로 반환되었다!!!
              * Proxy Entity가 한 번 조회가 되면 entityManager.find()호출을 해도 Proxy Entity를 반환한다(true를 보장하기 위함)
              * 중요한 것은 개발과정에서 Proxy Entity던 실제 Entity던 개발에 문제가 없도록 설계하는 것이 중요하다.
      <br>

      * 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 떄, Proxy Entity를 초기화하면 문제 발생(실무에서 정말 많이 겪는 문제)
        <br>

          ```Java
          try {
              Member member = new Member();
              member.setName("member");

              entityManager.persist(member);

              entityManager.flush();
              entityManager.claer();

              Member refMember = entityManager.getReference(Member.class, member.getId());
              System.out.prtinln("refMember = " + refMenber.getClass()); //Proxy

              refMemnber.getName(); //DB에 쿼리가 나가면서 프록시 객체 초기화

              transaction.commit();
          }
          ```
          <br>

          ```SQL
          refMember = class hellojpa.Member$HibernateProxy$6IYlvUiq
          Hibernate: 
              select
                  member0_.MEMBER_ID as MEMBER_I1_3_0_,
                  member0_.createBy as createBy2_3_0_,
                  member0_.createDate as createDa3_3_0_,
                  member0_.lastModifiedBy as lastModi4_3_0_,
                  member0_.lastModifiedDate as lastModi5_3_0_,
                  member0_.USERNAME as USERNAME6_3_0_,
                  member0_.TEAM_ID as TEAM_ID7_3_0_,
                  team1_.TEAM_ID as TEAM_ID1_7_1_,
                  team1_.createBy as createBy2_7_1_,
                  team1_.createDate as createDa3_7_1_,
                  team1_.lastModifiedBy as lastModi4_7_1_,
                  team1_.lastModifiedDate as lastModi5_7_1_,
                  team1_.name as name6_7_1_ 
              from
                  Member member0_ 
              left outer join
                  Team team1_ 
                      on member0_.TEAM_ID=team1_.TEAM_ID 
              where
                  member0_.MEMBER_ID=?
          ```        
          <br>

          * Proxy Entity의 초기화는 연속성 컨텍스트에 의하여 이루어진다. 영속성 컨텍스트를 꺼버린다면?(비영속)
            <br>

            ```Java
            try {
                Member member = new Member();
                member.setName("member");

                entityManager.persist(member);

                entityManager.flush();
                entityManager.clear();

                Member refMember = entityManager.getReference(Member.class, member.getId());
                System.out.println("refMember = " + refMember.getClass()); //Proxy Entity

                entityManager.detach(refMmeber); //영속성 컨텟트스에서 끄집어 낸다, 영속성 컨텍스트에서 관리하지 않는다
                //entityMamager.close();

                System.out.println("refMember = " + refMember.getName());

                transaction.commit();
            } catch(Exception e) {
                transaction.rollback();
                System.out.println("e = " + e);
            } finally {
                entityManager.close();
            }
            entityManagerFactory.close();
            ``` 
            <br>

            ```SQL
            e = org.hibernate.LazyInitializationException: could not initialize proxy [hellojpa.Member#24] - no Session
            ```
            * Hibernate는 org.hibernate.LazyInitializationException 예외를 터트림  
            * 일반적으로 트랜잭션이 시작하고 끝나는 라이브 사이클과 영속성 컨텍스트가 시작하고 끝나는 라이프 사이클을 맞춘다
            * 실무에서 트랜잭션이 끝나고서 Proxy Entity를 조회할 때 보면 `no Session` Error를 확인할 수 있따.
    <br>

    * ### _프록시 확인_ 
      * 프록시 인스턴스의 초기화 여부 확인
        * `entityManagerFactory.getPersistenceUnitUtil().isLoaded(Object entity)`
          <br>

          ```Java
          try {
              Member member = new Member();
              member.setName("member");

              entityManager.persis(member);

              entityManager.flush();
              entityManager.clear();

              Member refMember = entityManager.getReference(Member.class, member.getId());
              System.out.println("refMember = " + refMember.getCalss());

              System.out.println("isLoaded = " + entityManagerFactory.getPersistenceUnitUtil().isLoaded(refMember));

              transaction.commit();
          }
          ``` 
          <br>

          ```SQL
          isLoaded = false
          ```
          <br>

          ```Java
          try {
              Member member = new Member();
              member.setName("member");

              entityManager.persis(member);

              entityManager.flush();
              entityManager.clear();

              Member refMember = entityManager.getReference(Member.class, member.getId());
              System.out.println("refMember = " + refMember.getCalss());
              refMember.getName(); //Proxy Entity initialization

              System.out.println("isLoaded = " + entityManagerFactory.getPersistenceUnitUtil().isLoaded(refMember));

              transaction.commit();
          }
          ```         
          <br>

          ```SQL
          isLoaded = true
          ```
          <br>

          ```Java
          try {
              Member member = new Member();
              member.setName("member");

              entityManager.persist(member);

              entityManager.flush();
              entityManager.clear();

              Member refMember = entityManager.gerReference(Member.class, member.getId());
              System.out.println("refMember = " + refMember.getClass());

              Hibernate.initialize(refMember);  //강제 초기화

              transaction.commit();
          }
          ```
          <br>

          ```SQL
          Hibernate: 
              select
                  member0_.MEMBER_ID as MEMBER_I1_3_0_,
                  member0_.createBy as createBy2_3_0_,
                  member0_.createDate as createDa3_3_0_,
                  member0_.lastModifiedBy as lastModi4_3_0_,
                  member0_.lastModifiedDate as lastModi5_3_0_,
                  member0_.USERNAME as USERNAME6_3_0_,
                  member0_.TEAM_ID as TEAM_ID7_3_0_,
                  team1_.TEAM_ID as TEAM_ID1_7_1_,
                  team1_.createBy as createBy2_7_1_,
                  team1_.createDate as createDa3_7_1_,
                  team1_.lastModifiedBy as lastModi4_7_1_,
                  team1_.lastModifiedDate as lastModi5_7_1_,
                  team1_.name as name6_7_1_ 
              from
                  Member member0_ 
              left outer join
                  Team team1_ 
                      on member0_.TEAM_ID=team1_.TEAM_ID 
              where
                  member0_.MEMBER_ID=?        
          ```
      <br>

      * 프록시 클래스 확인 방법
        * `entity.getClass().getName()`
        * 출력(...Javasist... or HibernateProxy...)
      * 프록시 강제 초기화
        * `org.hibernate.Hibernate.initialize(entity);`
      * 참고
        * JPA 표준은 강제 초기화가 존재하지 않는다
        * 강제 호출: `member.getName()`
  <br>
  <br>

  * ## _즉시 로딩과 지연 로딩_ 
    <br>

    * ### Member Entity를 조회할 떄 Team Entity도 할께 조회해야 할까?
      ![](img/img316.png)
      <br>

      * 단순히 Member Entity 정보만 사용하는 비즈니스 로직
    <br>

    * ### 지연 로딩 LAZY을 사용해서 프록시로 조회
      ```Java
      @Entity
      public class Member extends BaseEntity {

          @Id
          @GaneratedValue
          private Long id;

          @Column(name = "usernmae")
          private String name;

          @ManyToOne(fetch = FetchType.LAZY)
          @JoinColumn(name = "team_id")
          private Team team;

          //defatult Constructor

          //Getter, Setter
      }
      ```  
      * team Reference를 Proxy Entity로 조회한다. 
        * EntityManagerFatory.getPersistenceUnitUtil.isLoaded(findMember.getTeam()))
        * false
      * 즉 Member Entity만 DB에서 조회한다
        * EntityManager.find(Member.class, PK) 호출시 Member Entity에 해당하는 필드에 관한 Select Query가 호출되며, Member Entity와 매핑된 team 참조를 조회하기 위한 Select Query는 호출되지 않는다
        ```Java
        try {
            Member member = new Member();
            member.setName("member");

            entityManager.persist(member);

            entityManager.flush();
            entityManager.clear();

            Member findMember = entityMember.find(Member.class, member.getId());

            transaction.commit();
        }
        ``` 
        <br>

        ```SQL
        select
            member0_.MEMBER_ID as MEMBER_I1_3_0_,
            member0_.createBy as createBy2_3_0_,
            member0_.createDate as createDa3_3_0_,
            member0_.lastModifiedBy as lastModi4_3_0_,
            member0_.lastModifiedDate as lastModi5_3_0_,
            member0_.USERNAME as USERNAME6_3_0_,
            member0_.team_TEAM_ID as team_TEA7_3_0_ 
        from
            Member member0_ 
        where
            member0_.MEMBER_ID=?
        ```
        <br>

        ```Java
        try {
            Team team = new Team();
            team.setName("TeamA");

            entityManager.persist(team);

            Member member = new Member();
            member.setName("member");
            member.setTeam(team);

            entityManager.persis(member);

            entityManager.flush();
            entityManager.clear();

            Member findMember = entityManager.find(Member.class, member.getId());
            System.out.prtinln("findMember = " + findMember.getTeam().getClass());

            System.out.println("=====================")
            findMember.getTeam().getName();
            System.out.println("=====================")

            transaction.commit();
        }
        ```
        <br>

        ```SQL
        select
            member0_.MEMBER_ID as MEMBER_I1_3_0_,
            member0_.createBy as createBy2_3_0_,
            member0_.createDate as createDa3_3_0_,
            member0_.lastModifiedBy as lastModi4_3_0_,
            member0_.lastModifiedDate as lastModi5_3_0_,
            member0_.USERNAME as USERNAME6_3_0_,
            member0_.team_TEAM_ID as team_TEA7_3_0_ 
        from
            Member member0_ 
        where
            member0_.MEMBER_ID=?
        findMember = class hellojpa.Team$HibernateProxy$HKa35KmH
        =======================

        select
            team0_.TEAM_ID as TEAM_ID1_7_0_,
            team0_.createBy as createBy2_7_0_,
            team0_.createDate as createDa3_7_0_,
            team0_.lastModifiedBy as lastModi4_7_0_,
            team0_.lastModifiedDate as lastModi5_7_0_,
            team0_.name as name6_7_0_ 
        from
            Team team0_ 
        where
            team0_.TEAM_ID=?
        =======================
        ```
        * Member Entity를 조회할때 DB에서 Member Entity를 가져왔으며
        * `findMember.getTeam().getCalss()`
          * Team Entity를 호출해서 Team Entity가 Proxy Entity로 호출된것을 확인할 수 있다(초기화 되지 않은 상태)
          * findMember = class hellojpa.Team$HibernateProxy$HKa35KmH
        * Team Proxy Entity를 조회하는 순간 Proxy Entity가 초기화 과정을 진행하여 영속성 컨텍스트에 요청에서 DB를 통해 실제 Entity 객체를 생성하는 것을 확인할 수 있다.
        * 비즈니스 로직상 Member Entity에 있는 값 만을 조회하는 경우가 많아면 이 방법을 선택하는 것이 좋다
    <br>

    * ### 지연 로딩
      ![](img/img317.png)
    <br>

    * ### 지연 로징 LAZY을 사용해서 프록시로 조회
      ![](img/img318.png) 
      * Member member = entityManager.find(Member.class, 1L);
      ![](img/img319.png) 
      * Team team = member.getTeam();
      * team.getName(); //실제 team을 사용하는 시점에 초기화(DB 조회)
    <br>

    * ### Member Entity와 Team Entity을 자주 함께 사용한다면?
      ![](img/img319.png)  
      * 비즈니스 로직상 Member Entity와 Team Entity에 있는 값을 같이 조회하는 경우가 많다면, Member와 Team 각각 따로 쿼리가 두번씩 계속 나가게 되면서 네트워크를 따로 타게된다. 이러한 경우 성능상 손해를 보게된다.
    <br>

    * ### 즉시 로딩 EAGER를 사용해서 함께 조회
      ```Java
      @Entity
      public class Member extends BaseEntity {

          @Id
          @GeneratedValue
          private Long ig;

          @Column(name = "username")
          private String name;

          @ManyToOne(fetch = FetchType.EAGER)
          @JoinColumn(name = "team_id")
          private Team team;

          //default Constructor

          //Getter, Setter
      }
      ``` 
      <br>

      ```Java
      try {
          Team team = new Team();
          team.setName("TeamA");

          entityManager.persist(team);

          Member member = new Member();
          member.setName("member");
          member.setTeam(team);

          entityManager.persis(member);

          entityManager.flush();
          entityManager.clear();

          Member findMember = entityManager.find(Member.class, member.getId());
          System.out.prtinln("findMember = " + findMember.getTeam().getClass());

          System.out.println("=====================")
          findMember.getTeam().getName();
          System.out.println("=====================")

          transaction.commit();
      }      
      ```
      * `Member findMember = entityManager.find(Member.class, member.getId());`
        * Member Entity와 Team Entity를 DB에서 JOIN해서 한번의 Query로 데이터를 가져온다
          ```SQL
          select
              member0_.MEMBER_ID as MEMBER_I1_3_0_,
              member0_.createBy as createBy2_3_0_,
              member0_.createDate as createDa3_3_0_,
              member0_.lastModifiedBy as lastModi4_3_0_,
              member0_.lastModifiedDate as lastModi5_3_0_,
              member0_.USERNAME as USERNAME6_3_0_,
              member0_.team_TEAM_ID as team_TEA7_3_0_,
              team1_.TEAM_ID as TEAM_ID1_7_1_,
              team1_.createBy as createBy2_7_1_,
              team1_.createDate as createDa3_7_1_,
              team1_.lastModifiedBy as lastModi4_7_1_,
              team1_.lastModifiedDate as lastModi5_7_1_,
              team1_.name as name6_7_1_ 
          from
              Member member0_ 
          left outer join
              Team team1_ 
                  on member0_.team_TEAM_ID=team1_.TEAM_ID 
          where
              member0_.MEMBER_ID=?          
          ``` 
        <br>

        * 이 경우에는 즉시로딩(`@ManayToOne(fetch = FetchType.EAGER)`)이므로 별도의 Proxy Entity호출이 발생하지 않느다
          ```SQL
          findMember = class hellojpa.Team
          ```
      <br>

      * `findMember.getTeam().getName();`
        * 이미 초기화가 끝나있는상태(초기화가 필요 없는 상태)이므로 위의 코드를 호출하면 실제 DB에서 가져욘 값을 출력한다.
          ```SQL
          =======================
          System.out.prtinln("team = " + findMember.getTeam().getName())
          으로 변경해서 환인
          =======================
          ```      
    <br>

    * ### 즉시 로딩
      ![](img/img320.png)
    <br>

    * ### 즉시 로딩(EAGER), Member Entity 조회시 항상 Team Entity도 조회
      ![](img/img321.png)    
      * JPA 구현체는 가능하면 조인을 사용해서 SQL 한번에 함께 조회
    <br>

    * ### 프록시와 즉시 로딩 주의
      * `가급적 지연 로딩만 사용(특히 실무에서)`
        * 즉시 로딩을 적용하면 예상하지 못한 SQL이 발생
          * 현제 예제에서는 JOIN TABLE이 2개여서 비교적 간단하게 보이지만, 실무에서는 수십개의 테이블이 돌아가면서 JOIN된 테이블이 여러개가 있는 경우가 다분하다.
          * 따라서 즉시 로딩으로 설정하면 JOIN된 테이블들에 대한 엄청난 길이의 Query문이 발생하게 된다
          * 그러므로 실무에서는 지연 로딩으로 속칭 발라내야 한다
      * `즉시 로딩은 JPQL에서 N + 1 문제를 일으킨다`
        ```Java
        try { 
            Team team  = new Team();
            team.setName("TeamA");

            entityManager.persist(team);

            Member member = new Member();
            member.setName("member");
            member.setTeam(team);

            entityManager.persist(member);

            entityManager.flush();
            entityManager.clear();

            List<Member> members = entityManager.createQuery("select m form Member m", Member.class).getResultList();

            transaction.commit();
        }
        ``` 
        <br>

        ```SQL
        select
            team0_.TEAM_ID as TEAM_ID1_7_0_,
            team0_.createBy as createBy2_7_0_,
            team0_.createDate as createDa3_7_0_,
            team0_.lastModifiedBy as lastModi4_7_0_,
            team0_.lastModifiedDate as lastModi5_7_0_,
            team0_.name as name6_7_0_ 
        from
            Team team0_ 
        where
            team0_.TEAM_ID=?

        select
            team0_.TEAM_ID as TEAM_ID1_7_0_,
            team0_.createBy as createBy2_7_0_,
            team0_.createDate as createDa3_7_0_,
            team0_.lastModifiedBy as lastModi4_7_0_,
            team0_.lastModifiedDate as lastModi5_7_0_,
            team0_.name as name6_7_0_ 
        from
            Team team0_ 
        where
            team0_.TEAM_ID=?        
        ```
        * `@ManyToOne(fetch = FetchType.EAGER)` 즉시 로딩으로 설정했는데 쿼리가 두번 나가는 것을 확인할 수 있다.
        * `entityManager.find()`는 DB TABLE의 PK를 지정해서 데이터를 가져오는 방법이므로 JPA가 내부적으로 다 찾아서 실행할 수 있다.
        * JPQL은 `select m from Member m`이 우선 SQL로 변역(SELECT * FORM MEMBER)이 된다 -> Member Entity만 Select 한다
        * Member Entity를 가져와봤더니 Team Entity가 EAGER로 설전된것을 확인
          * 즉시 로딩(EAGER)이란것은 일단 데이터를 가져올 때 무조건 값이 다 설정되어 있어야 한다.
          * 그렇다면 `select m from Member m`에서 Member 쿼리가 나가고 만약 Member의 개수가 10개면 10개만큼 EAGER를 가져오기위한 별도의 Query가 나가게된다
        * 정리
          1. SQL로 변역(select * from Member)
          2. Member를 가져옴
          3. Member를 보니 EAGER로 설정되어 있으므로 Team도 가져와야함
            * LAZY설정이면 Proxy Entity를 집어 넣으면 된다
            * 즉 EAGER설정 이므로 `List<Member> members = `로 변환하는 시점에 Team의 값이 다 설정되어 있어야 한다
          4. Team의 개수만큼 별도의 SQL(select * form Team where TEAM_ID = xxx)이 나가게 된다
            ```Java
            try {
                Team teamA = new Team();
                teamA.setName("teamA");

                entityManager.persist(teamA);

                Team teamB = new Team();
                teamB.setName("teamB");

                entityManager.persist(teamB);

                Member member1 = new Member();
                member1.setName("member1");
                member1,setTeam(teamA);

                entityManageer.persist(member1);

                Member member2 = new Member();
                member2.setName("member2");
                member2.setTeam(teamB);

                entityManager.persist(member2);

                entityManager.flush();
                entityManager.clear();

                List<Member> members = entityManager.createQuery("select m form Member m", Member.class).getResultList();

                transaction.commit();
            }
            ```
            <br>

            ```SQL
            select
                m 
            from
                Member m */ select
                    member0_.MEMBER_ID as MEMBER_I1_3_,
                    member0_.createBy as createBy2_3_,
                    member0_.createDate as createDa3_3_,
                    member0_.lastModifiedBy as lastModi4_3_,
                    member0_.lastModifiedDate as lastModi5_3_,
                    member0_.USERNAME as USERNAME6_3_,
                    member0_.team_TEAM_ID as team_TEA7_3_ 
                from
                    Member member0_
            ```
            * Member는 한번에 가져온다
            <br>

            ```SQL
            select
                team0_.TEAM_ID as TEAM_ID1_7_0_,
                team0_.createBy as createBy2_7_0_,
                team0_.createDate as createDa3_7_0_,
                team0_.lastModifiedBy as lastModi4_7_0_,
                team0_.lastModifiedDate as lastModi5_7_0_,
                team0_.name as name6_7_0_ 
            from
                Team team0_ 
            where
                team0_.TEAM_ID=?

            select
                team0_.TEAM_ID as TEAM_ID1_7_0_,
                team0_.createBy as createBy2_7_0_,
                team0_.createDate as createDa3_7_0_,
                team0_.lastModifiedBy as lastModi4_7_0_,
                team0_.lastModifiedDate as lastModi5_7_0_,
                team0_.name as name6_7_0_ 
            from
                Team team0_ 
            where
                team0_.TEAM_ID=?
            ``` 
            * Member가 둘이면서 서로 다른 팀이고 영속성 컨텍스트에도 존재하지 않기때문에 Team을 따로 가져와야 한다
      * `@ManayToOne, @OneToOne은 default = EAGER -> LAZY로 설정`
      * @OneToMany, @ManyToMany는 default = LAZY 
  <br>
  <br>

  * ## _지연 로딩 활용_
  <br>
  <br>

  * ## _영속성 전이: CASCADE_
    * ### 영속성 전이: CASCADE
      * 특정 엔티티를 영속 상태(entityManager.persist())로 만들 때 연관된 엔티티도 함께 영속 상태로 만들고 싶을 때 사용한다.
      * 예: 부모 엔티티를 저장할 때 자식 엔티티도 함께 저장
      ```Java
      @Entity
      public class Parent {

          @Id
          @Column(name = "parent_id")
          @GeneratedValue
          private Long id;

          @OneToMany(mappedBy = "parent")
          private List<Child> childList = new ArrayList<>();

          private String name;

          //연관관계 편의 메소드
          public void addChile(Chile chile) {
              childList.add(child);
              child.setParent(this);
          }

          //default Constructor

          //Getter, Setter
      } 
      ``` 
      ```Java
      @Entity
      public class Child {
          
          @Id
          @Column(name = "child_id")
          @GeneratedValue
          private Long id;

          @ManyToOne
          @JoinColumn(name = "parent_id")
          private Parent parent;

          private String name;

          //default Constructor

          //Getter, Setter
      }
      ```
      ```Java
      try {
          Child child1 = new Child();
          Chile child2 = new Child();

          Parent parent = new Parent();
          parent.addChild(child1);
          parent.addChild(child2);

          System.out.println("========= Persist Start =========");
          entityManager.persist(parent);
          entityManager.persist(child1);
          entityManager.persist(child2);
          System.out.println("========= Persist End =========");
      }
      ```
      * 위의 코드는 부모 엔티티(Parent)중심적인 설계가 아니다.
      * 코드를 짤때 부모 엔티티가 자식 엔티티를 관리하며 entityManager.persist(parent)한 번의 호출로 Child Entity들은 자동으로 영속상태가 됐으면 한다.
    * ### 영속성 전이: 저장
      ![](img/img331.png) 
      ```Java
      @Entity
      public class Parent {

          @Id
          @Column(name = "parent_id")
          @GeneratedValue
          private Long id;

          @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
          private List<Child> childList = new ArrayList<>();

          private String name;

          public void addChild(Child child){
              childList.add(child);
              child.setParent(this);
          }

          //default Construtor

          //Getter, Setter
      } 
      ``` 
      ```Java
      try {
          Child child1 = new Child();
          Child child2 = new Child();

          Parent parent = new Parent();
          parent.addChild(child1);
          parent.addChild(child2);

          entityManager.persist(parent);

          transaction.commit();
      }
      ```
    * ### 영속성 전이: CASCADE - 주의!
      * 영속성 전이는 연관관계 매핑을 하는 것과는 아무 관련이 없다.
      * 엔티티를 영속화할 떄 연관된 엔티티도 함께 영속화하는 편리함을 제공할 뿐이다.
      * 그렇다면 언제 사용하는가?
        * 자식엔티티가 하나의 부모 엔티티에 의해서만 관리될 떄는 유용하지만, 자식 엔티티가 하나의 부모 엔티티가 아닌 여러 부모 엔티티에 의하여 관리되어진다면 CASCADE를 사용할 경우 운영이 너무 힘들어지기 때문에 전부 따로 관리해야 한다
        * `즉 소유가자 하나일 때 CASECAED를 사용해도 좋다`
        * 자식 엔티티가 단일 부모 엔티티에 종속절일 때 보통 라이프 사이클이 똑같으므로 CASCASD를 사용하기에 적합하다.
        * 즉 CASCADE를 사용 적합성에는 두 가지 전재가 깔린다
          * 단일 소유자
          * 라이브 사이클이 같음
    * ### CASCADE의 종류
      * `ALL`: `모두 적용`
      * `PERSIST`: `영속`
      * `REMOVE`: `삭제`
      * MERGE: 병합
      * PERRESH: PEFRESH
      * DETACH: DETACH
  <br>
  <br>

  * ## _고아 객체_
    * ### 고아 객체
      * 고아 객체 제거: 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제
        ```Java
        @Entity
        public class Parent {

            @Id
            @Column(name = "parent_id")
            @GeneratedValue
            private Long id;

            @OneToMany(mappedBy = "parent", cascade = CascasdType.ALL,
            orphanRemoval = true)
            private List<Child> childList = new ArrayList<>();

            private String name;

            public void addChild(Child child) {
                childList.add(child);
                child.setParent(this);
            }

            //default Constructor

            //Getter, Setter
        }
        ``` 
        ```Java
        try {
            Child child1 = new Child();
            Child child2 = new Child();

            Parent parent = new Parent();
            parent.addChild(child1);
            parten.addChild(child2);

            entityManger.persist(parent);

            entityManger.flush();
            entityManger.clear();

            Parent findParent = entityManager.find(Parent.class, parent.getId());
            findParent.getChildList().remove(0);

            transaction.commit();
        }
        ```
      * `orthanRemoval = true`
      * Parent parent1 = em.find(Parent.class, id);
      * parent1.getChildren().remove(0); //자식 엔티티를 컬렉션에서 제거
      * DELETE FROM CHILD WHERE ID = ?
    * ### 고아 객체  - 주의
      * 참조가 제거된 엔티티는 다른 곳에서 참조하지 않는 고아 객체로 보고 삭제하는 기능
      * `참조하는 곳이 하나일 떄 사용해야 한다`
      * `특정 엔티티가 개인 소유할 떄 사용한다`
      * @OneToMany, @OneToMany만 가능
      * 참고
        * 개념적으로 부모를 제거하면 자식은 고아가 된다. 따라서 고아 객체 제거 기능을 활성화 하면, 부모를 제거할 때 자식도 함께 제거된다. 이것은 CasecadeType.REMOVE 처럼 도작한다
  <br>
  <br>

  * ## _영속석 전이 + 고아객체, 생명주기_
    * ### 영속적 전이 + 고아 객체, 생명주기
      * `CascadeType.ALL + orphanRemovel=true`
      * 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거
      * 두 옵션을 모두 활성화 하면 부모 엔티티를 통해서 자식의 생명 주기를 관리할 수 있따
      * 도메인 주도 설계(DDD)의 Aggregate Root개념을 구현할 떄 유용하다




       


N + 1 문제
결과가 10개면 N은 10 1은 최적회(select m from Member m")
즉 처음 쿼리 하나를 날렸는데 추가쿼리 N개가 나간다 
```JAva

@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Team team;

```
```SQL
Hibernate: 
    /* select
        m 
    from
        Member m */ select
            member0_.MEMBER_ID as MEMBER_I1_3_,
            member0_.createBy as createBy2_3_,
            member0_.createDate as createDa3_3_,
            member0_.lastModifiedBy as lastModi4_3_,
            member0_.lastModifiedDate as lastModi5_3_,
            member0_.USERNAME as USERNAME6_3_,
            member0_.team_TEAM_ID as team_TEA7_3_ 
        from
            Member member0_
```
team은 프록시로 잡혀있으므로 
N+1 문제가 발생하지 않는다

즉 N+1문제 대안법은 
모든 연관관계를 지연로딩으로 전부 세팅한다 그 이후 세가지 방법이 존재한다
가장 기본적인 방법은  jPQL fetch join: run time에 내가 원하는 애들을 동적으로 선택해서 한번에 가져온다  


---
---
<br>
<br>

# _값 타입_     
## _기본값 타입_
* ### JPA의 데이터 타입 분류
  JPA의 최상위 타입으로는 `엔티티 타입`, `값 타입`이 존재한다.
  * `엔티티 타입`
    * `@Entity`로 정의하는 객체
    * 데이터가 변해도 식별자로 지속해서 추적이 가능하다.
      * 회원 Entity의 키나 나이 값을 변경해도 실별자로 인식이 가능하다
  * `갑 타입`
    * int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
    * 식벌자가 없고 값만 있으므로 변경시 추적이 불가능하다.
      * 숫자 100을 200으로 변경하면 완전히 다른 값으로 대체된다.   
<br>

* ### 값 타입 분류
  값 타입에는 `기본값 타입`과 `임베디드 타입`, `컬렉션 값 타입`이 존재한다.
  * `기본값 타입`
    * 자바 기본 타입(int, double, ...)
      * 자바가 제공하는 기본적인 값을 세팅해서 사용할 수 있는 타입
    * 래퍼 클래스(Integer, Long, ...)
    * String
  * `임베디드 타입`(Embedded Type, 복합 값 타입)
  * `컬렉션 값 타입`(Collection Value Type)   
임베디드 타입과 컬렉션 값 타입은 JPA에서 내부적인 특정 속성을 정의해서 사용해야 한다   
<br>

* ### 기본값 타입   
  $ex.$ String name, int age ...   
  * `생명주기를 Entity에 의존한다.`   
    $ex.$ 회원을 삭제하면 이름, 나이 필드도 함께 삭제된다.
  * `값 타입은 공유해서는 안된다`   
    $ex.$ 회원 이름 변경시 다른 회원의 이름도 함께 변경되면 안된다.   
    $ex.$ Side Effect
  # 
  * ### 참고: 자바의 기본 타입은 절대 공유X
    * int, double같은 기본 타입(Primitive Type)은 절대 공유되서는 안된다.
    * 기본 타입은 항상 값을 복사한다.
    * Integer같은 래퍼 클래스나 String같은 특수한 클래스는 공유 가능한 객체이지만 변경되지 않는다
      * 클레스의 문제점은 Reference를 끌고온다.. 즉 공유가 된다..
# 
* ## _임베디드 타입(복합 값 타입)_
  * ### 임베디드 타입
    * 새로운 값 타입을 직접 정의할 수 있다.
    * JPA는 임베디드 타입(Embedded Type)이라 한다.
    * 주로 기본 값 타입을 모아 만들어서 복합 값 타입이라고도 불린다.
    * int, Stringr과 같은 값 타입이다.
      * 임베디드 타입 역시 엔티티가 아니므로 추적과 변경이 불가능하다.
    * 임베디드 타입 예시
      * Member Entity는 이름, 근무 시작일, 근무 종료일, 주소 도시, 주소 번지, 주소 우편번호 필드를 갖는다.
      * 공통적인 속성이 보인다
        * 근무 시작일, 근무 종료일
        * 주소 도시, 주소 번지, 주소 우편변호
      * 공통적인 필드끼리 클래스 타입으로 만들어서 사용할 수 있지않을까?
      ```Java
      @Entity
      public class Member {

          @Id
          @Column(name = "member_id")
          @GeneratedValue
          private Long id;
          private String name;

          //Period
          private LocalDateTime startDate;
          private LocalDateTime endDate;

          //Address
          private String city;
          private String street;
          private String zipcode;
      }
      ``` 
      ```SQL
      create table Member (
      member_id bigint not null,
      city varchar(255),
      endDate timestamp,
      member_name varchar(255),
      startDate timestamp,
      street varchar(255),
      zipcode varchar(255),
      primary key (member_id)
      )
      ```
      <br>

      ![](img/img324.png) 
  #    
  * ### 임베디드 타입 사용법
    * `@Embeddedable`: 값 타입을 정의하는 곳에 표시한다.
    * `@Embedded`: 값 타입을 사용하는 곳에 표시한다.
    * @Embeddedable, @Embedded 둘중 하나는 생략이 가능하지만, 영한이는 둘 다 기입하는 것을 권장한다.
    * 기본 생성자 코드는 필수로 기입하도록 한다.
      ```Java
      @Embeddeable
      public class Period {

          private LocalDateTiem startDate;
          private LocalDateTiem endDate;

          //default Constructor

          //Getter, Setter
      }
      ``` 
      ```Java
      @Embeddable
      public class Address {

          private String city;
          private String street;
          @Column(name = "ZIPCODE") //가능
          private String zipcode;

          //default Constructor

          //Getter, Setter
      }
      ```
      ```Java
      @Entity
      public class Member {

          @Id
          @Column(name = "member_id")
          @GeneratedValue
          private Long id;
          private String name;

          @Embedded
          private Periode workPeriod;

          @Embedded
          private Address homeAddress;

          //default Constructor

          //Getter, Setter
      }
      ```
      ```SQL
      create table Member (
       member_id bigint not null,
        city varchar(255),
        street varchar(255),
        zipcode varchar(255),
        username varchar(255),
        endDate timestamp,
        startDate timestamp,
        primary key (MEMBER_ID)
      )
      ```
      * 생성된 DB 테이블은 기존과 동일하게 유지되며, Member Entity는 조금 더 객체지향 스럼게 사용할 수 있게됐다.
      ```Java
      try {
          Member member = new Member();
          member.setUsername("userA");
          member.setHomeAddress(new Address("서울시", "서초구", "123-12"));
          member.setWorkPeriod(new Period());

          entityManager.persist(member);
      }
      ``` 
      * DB 확인
  * ### 임베디드 타입의 장접
    * 재사용에 용이하다.
    * 높은 응집도를 갖는다.
    * `Period.isWork()`처럼 해당 값 타입만 사용하는 의미있는 메소드를 만들 수 있다.
    * 임베디드 타입을 포함한 모든 값 타입은, 값 타입을 소유한 Entity에 생명주기를 의존한다.
  * ### 임벧드 타입과 테이블 매핑
    ![](img/img325.png) 
    * 임베디드 타입은 엔티티의 값일 뿐이다.
    * 임베디드 타입을 사용하기 전화 후에 `매핑하는 테이블은 같다.`
      * DB입장에서는 바뀌는 것이 없다.
      * 매핑에만 중점을 두면 된다.
    * 객체와 테이블을 아주 세밀하게(find-grained) 매핑하는 것이 가능하다.
      * DB는 데이터를 잘 관리하는것이 목적이므로 임베디드 타입과 매핑하는 설계가 바람직하다
      * 객체는 데이터 뿐만 아니라 메소드라 하는 기능(행위)까지 가지고 있으므로, 임베디드 타입으로 필드를 임베디드 타입으로 묶었을 때 이들이 많다.
    * 잘 설계한 ORM 애플리케이션은 매핑한 테이블 수보다 클래스의 수가 더 많다.
  #   
  * ### 임베디드 타입과 연관관계
    ![](img/img326.png) 
    * PhoneNmuber 임베디드 타입이 PhoneEntity 엔티티를 가질 수 있다.
    * PhoneNmuber 입장에서는 PhoneEntity의 FK값만 들고 있으면 된다.
  # 
  * ### @AttributeOverride: 속성 재정의
    * 한 Entity에서 같은 값을 타입으로 사용한다면?
      ```Java
      @Entity
      public class Member {

          @Embedded
          private Address homeAddress;

          @Embedded
          private Address workAddress;

          //default Constructor

          //Getter, Setter
      }
      ```
    * 컬럼 명 중속 오류
      ```SQL
      Repeated column Error...
      ```
    * `@AttributeOverrides`, `@AttributeOverride`를 사용해서 컬럼 명 속성을 재정의 한다.
      ```Java
      @Entity
      public class Member {

          @Embedded
          private Address homeAddress;

          @Embedded
          @AttributeOverrieds({
                  @AttributeOverride(name = "city",
                          column = @Column(name = "work_city")),
                  @AttributeOverride(name = "street",
                          column = @Column(name = "work_street")),
                  @AttributeOverried(name = "zipcode",
                          column = @Column(name = "work_zipcode"))
          })
          private Address workAddress;

          //default Constructor

          //Getter, Setter
      }
      ``` 
  #    
  * ### 임베디드 타입과 null
    * 임베디드 타입의 값이 null이면 매핑한 컬럼 값은 모두 null이다.
#      
* ## _값 타입과 불변 객체_
  * 값 타입은 복잡한 객체 세상을 조금이라도 단순화하려고 만든 개념이다. 따라서 값 타입은 단순하고 안전하게 다룰 수 있어야 한다.
  # 
  * ### 값 타입 공유 참조
    * 임베디드 타입 같은 값 타입은 여러 엔티티에서 공유할 수 있지만 위험하다
    * 부작용(Side Effect)이 발생한다.
     
    ![](img/img327.png) 
    * 회원1 Etntiy와 회원2 Entity 가 같은 값 타입인 주소를 사용하고 있다.
    * city의 값 OldCity를 NewCity로 변경하게 되면 회원1 Entity와 회원2 Entity가 들고있는 각각의 테이블의 city Colum의 값이 NewCity로 변경된다.
      ```Java
      try {
          Address address = new Address("city", "street", "zipcode");

          Member memberA = new Member();
          memberA.setUsername("memberA");
          memberA.setHomeAddress(address);

          entityManager.persist(memberA);

          Member memberB = new Member();
          memberB.setUsername("memberB");
          memberB.setHomeAddress(address);

          entityManager.persist(memberB);
      }
      ``` 
      ```SQL
      insert 문이 두 번 호출된 것을 확인할 수 있다.
      memberA, memberB 둘다 똑같은 주소를 들고있다.
      DB 확인
      ```
      ```Java
      try {
          Address address = new Address("city", "street", "zipcode");

          Member memberA = new Member();
          memberA.setUsername("memberA");
          memberA.setHomeAddress(address);

          entityManager.persist(memberA);

          Member memberB = new Member();
          memberB.setUsername("memberB");
          memberB.setHomeAddress(address);

          entityManager.persist(memberB);

          //memberA의 주소만 변경하고 싶은 목적의 코드 작성
          memberA.getHomeAddress().setCity("newCity");
      }
      ```
      ```SQL
      update 쿼리가 두 번 나가는 것을 확인
      DB에 memberA와 memberB의 CITY 컬럼의 값이 똑같이 변경...
      ```
      * 이러한 Side Effect에 의한 버그는 디버깅하기 정말 어렵다..
      * 만약 공유해서 사용하고 싶은 경우에는 Address를 값타입이 아닌 엔티티로 만들어야 한다.
  #
  * ### 값 타입 복사
    * 값 타입의 실제 인스턴스의 값을 공유하는 것은 위험하다.
    * 대신 값(인스턴스)를 복사해서 사용
      ![](img/img328.png)
      * address라는 것이 있다면, newAddress라는 것으로 복사해서 사용해야 한다.
    ```Java
    try {
          Address address = new Address("city", "street", "zipcode");

          Member memberA = new Member();
          memberA.setUsername("memberA");
          memberA.setHomeAddress(address);

          entityManager.persist(memberA);

          Address  copyAddress = Address(address.getCity(), 
          address.getStreet(), address.getZipcode());

          Member memberB = new Member();
          memberB.setUsername("memberB");
          memberB.setHomeAddress(copyAddress);

          entityManager.persist(memberB);   

          memberA.getHomeAddress().setCity("newCity");
    }
    ``` 
    ```SQL
    update 쿼리는 두번 나갈것이고  memberA의 city만 변경된 것을 확인
    ```
  #
  * ### 객체 타입의 한계
    * 항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용을 피할 수 없다.
    * 문제는 임베디드 타입처럼 `직접 정의한 값 타입은 자바의 기본 타입이 아니라 객체 타입`이다.
    * 자바 기본 타입에 값을 대입하면 값을 복사한다.
    * `객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없다.`
    * `객체의 공유 참조는 피할 수 없다.`
    * 실수로 복사한 값을 사용하지 않을경우...
      ```Java
      try {
            Address address = new Address("city", "street", "zipcode");

            Member memberA = new Member();
            memberA.setUsername("memberA");
            memberA.setHomeAddress(address);

            entityManager.persist(memberA);

            Address  copyAddress = Address(address.getCity(), 
            address.getStreet(), address.getZipcode());

            Member memberB = new Member();
            memberB.setUsername("memberB");
            memberB.setHomeAddress(address);

            entityManager.persist(memberB);   

            memberA.getHomeAddress().setCity("newCity");
      }      
      ```    
      * 컴파일러 레벨에서 막을 수 있는 방법이 있는가? --> 객체의 공유 참조는 피할 수 없다.
    * 기본 타임(Primitive Type)
      ```Java
      int a = 10;
      int b = a; //기본 타입은 값을 복사
      b = 4;
      ``` 
      * 기본 타입은 대입연산자(`=`)를 사용하면 `복사`가 된다.
      * 자바의 기본 타입은 값을 복사해서 넘어간다.
    * 객체 타입
      ```Java
      Address a = new Address("Old");
      Address b = a; //객체 타입은 참조를 전달
      b.setCity("New");
      ``` 
      * 객체 타입은 a와 b가 같은 Address라는 인스턴스를 가리킨다.
      * a와 b가 같은 인스턴스를 참조하고 있으므로 한 쪽에서 값을 변경하면 결론적으로 a와 b둘 다 값이 변경된다.
    * 자바의 객체 타입은 참조를 복사해서 넘어간다... 값을 복사하는것도 아니고 참조를 복사해서 뭐하나.. 가리키는 인스턴스가 하나뿐인데...
      * 참조를 막을 수 있는 방법이 없다...
  # 
  * ### 불변 객체
    * 객체 타입을 수정할 수 없게 만들면 `부작용을 원천 차단`할 수 있다.
    * `값 타입은 불면 객체(immutable object)로 설계해야 한다.`
    * `불면 객체`: `생성 시점 이후 절대 값을 변경할 수 없는 객체`
    * 생성자로만 값을 설정하고 수정자(Setter)를 만들지 않으면 된다.
      * Address Entity에 Setter를 전부 지우거나 내부적으로 사용할 떄는 Setter의 접근 지정자를 private으로 설정한다.
      * 만약 이 상태에서 참조가 된 값 타입을 사용하게 되면 컴파일 오류가 발생하면서 컴파일러 레벨에서 오류를 잡을 수 있다.
      * 값을 바꾸고 싶다면?
        ```Java
        try {
            Address address = new Address("city", "street", "zipcode");

            Member memberA = new Member();
            memberA.setUsername("mamberA");
            memberA.setHomeAddress(address);

            entityManager.persist(memberA);

            //변경하고 싶은 Column의 값만 직접 입력
            //필요하다면 내부적으로 copy 메소드를 생성해서 사용
            Address newAddress = new Address("NewCity", address.getStreet(). address.getZipcode());

            //완전히 새롭게 세팅해야 한다
            memberA.setHomeAddress(newAddress);
          
        }
        ```
    * 참고
      * Integer, String은 자바가 제공하는 대표적인 불변 객체이다
    * `불변이라는 작은 제약으로 부작용이라는 큰 재앙을 막을 수있다.`
# 
* ## _값 타입의 비교_
  * ### 값 타입의 비교
    * 값 타입: 인스턴스가 달라도 그 안에 값이 같으면 같은 것으로 봐야 한다
    * `동일성(identity)비교`: 인스턴스의 참조 값을 비교, `==`사용
      * 기본 타입에서 == 비교는 true 가 나온다
      * 객체 타입에서 == 비교는 false가 나온다
    * `동등성(equivalence)비교`: 인스턴스의 값을 비교, `equals()`사용
    * 값 타입은 a.equals(b)를 사용해서 동등성 비교를 해야한다.
    * 값 타입의 equals()메소드를 적적하게 재정의(주로 모든 필드 사용)
      ```Java
      public class ValueMain {

          public static void main(String[] args) {

              int a = 10;
              int b = 20;

              System.out.println("a == b: " + (a == b));

              Address address1 = new Address("city", "street", "zipcode");
              Address address2 = new Address("city", "street", "zipcode");

              System.out.println("address1 == address2: " + (address1 == address2));

              System.out.println("address1 equals address2: " + (address1.equals(address2)));
          }
      }
      ```
      ```
      a == b: true
      address1 == address: false
      address1 equals address2: false
      ```
      * equals() 의 default: `==`
      * equals() 메소드를 Override(재정의)히야한다.   
        ```Java
        @Embeddable
        public class Address {

            private String city;
            private String street;
            private String zipcode;

            private Member member;

            public Address() {
            }

            public Address(String city, String street, String zipcode) {
                this.city = city;
                this.street = street;
                this.zipcode = zipcode;
            }

            //equals는 웬만하면 default로 생성
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Address address = (Address) o;
                return Objects.equals(city, address.city) && Objects.equals(street, address.street) && Objects.equals(zipcode, address.zipcode) && Objects.equals(member, address.member);
            }
        ```     
        ```
        a == b: true
        address1 == address2: false
        address1 equals address2: true
        ```
        * 값 타입들의 비교는 항상 equals를 사용해야 한다
#
* ## _값 타입 컬렉션_
  ![](img/img329.png) 
  * Member Entity가 favoritFoods, addressHistory 두 가지 값타입을 컬렉션으로 가지고 있다.
  * ### 값 타입 컬렉션
    * 값 타입을 하나 이상 저장할 떄 사용한다.
    * `@ElemnetCollection`, `@CollectionTable`을 사용한다.
    * 데이터베이스는 컬렉션을 같은 테이블에 저장할 수 없다.
    * 컬렉션을 저장하기 위한 별도의 테이블이 필요하다.
  #
  * ### 값 타입 컬렉션 사용
    * 값 타입 저장 예제
    * 값 타입 조회 예제
      * 값 타입 컬렉션도 지연 로딩 전략을 사용한다.
    * 값 타입 수정 예제
    * 참고: 값 타입 컬렉션을 영속성 전에(Cascade) + 고아 객체 제거 기능을 필수로 가진다고 볼 수 있다.
  #
  * ### 값 타입 컬렉션의 제약사향
    * 값 타입은 엔티티와 다르게 식별자 개념이 없다.
    * 값은 변경하면 추적이 어렵다.
    * 값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다.
    * 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본 키를 구성해야 한다
      * `null 입력X, 중복 저장X`
  #
  * ### 값 타입 컬렉션 대안
    * 실무에서는 상황에 따라 `값 타입 컬렉션 대신에 일대다 관계를 고려`한다.
    * 일대다 관계를 위한 엔티티를 만들고, 여기에서 값 타입을 사용한다.
    * 영속성 전이(Cascade) + 고아 객체 제거를 사용해서 값 타입 컬렉션 처럼 사용한다.
    * $ex.$ AddressEntity
  #
  * ### 정리
    * `엔티티 타입의 특징` 
      * 식별자O
      * 생명 주기 관리
      * 공유
    * `값 타입 특징`
      * 식별자X
      * 생명 주기를 Entity에 의존
      * 공유하지 않는 것이 안전(복사해서 사용)
      * 불변 객체로 만드는 것이 안전
    * 값 타입은 정말 값 타입이라 판단될 떄만 사용하도록 한다. 
    * 엔티티와 값 타입을 혼동해서 엔티티를 값 타입으로 만들어서는 안된다.
    * 식별자가 필요하고, 지속해서 값을 추적, 변경해야 한다면 그것은 값 타입이 아닌 엔이이이다.
#
* ## _실전 예제 - 6.값 타입 매핑_




값 타입 컬렉션이란 값 타입을 컬렉션에 담아서 사용하는 것을 의미한다
기존에 엔티티를 컬렉션으로 사용한 적이 있다(Parent)에서 확인
이번에는 값타입을 컬렉션으로 넣어서 쓰는것을 값타입 컬렉션이라 한다
Member가 favoritFoods, addressHistory 두가지 값타입을 컬렉션으로 가지고 있다
이것은 DB테이블로 구현할 떄가 문제가 된다
단순하게 값타입이 한개일때는 Mmeber에 필드 속성으로 해서 MEMBER 테이블에 넣었으면 됐다
문제는 이 컬렉션이 DB에 들어가야 하는데 기본적으로 관계형 데이터베이스는 내부적으로 테이블 안에 컬렉션을 답을 구조가 없다.. 전부 Value로 값만 넣을 수 있다...
요즘에야 DB들이 JSON을 지원하면서 가능하긴 한데 기본적으로는 안된다
Member 입장에서 List에 Address나 이런 컬렉션들은 결국 일대 다 개념이다 
따라서 MEMBER에 addressHistory같은 데이터를 보관한다 하면, 별도의 테이블(ADDRESS)을 뽑아야 한다(개념적으로 보면 일대 다)
그래서 DB를 보면 FAVORITE_FOOD는 MEMBER_ID와 FOOD_NAME이 조함되서 PK를 이루어야 하며, ADDRESS의 경우 MEMBER_ID와 값타입(CITY, STREET, ZIPCODE)을 전부 다 묶어서 하나의 PK를 이루어야 한다.
```Java
package hellojpa;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    @Embedded
    private Address homeAddress;

    //참고로 Set이나 List 같은 것들을 사용할 수 있다
    //컬렉현 하위에 있는 인터페이스들을 전부 사용할 수 있다
    @ElementCollection//값타입 컬렉션 지정
    @CollectionTable(name = "FAVORITE_FOOD", //테이블 명 지정
            joinColumns = @JoinColumn(name = "MEMBER_ID") //FK로 잡을것을 지정
    )
    @Column(name = "FOOD_NAME") //값이 하나고 내가 정의한게 아니므로 컬럼명을 지정해준다(예외적)
    private Set<String> favoriteFood = new HashSet();

    //값타입을 컬렉션으로 여러개 저장하고싶어서...
    @ElementCollection
    @CollectionTable(name = "ADDRESS",
            joinColumns = @JoinColumn(name = "MEMBER_ID")
            //Address에 city, street.. 등 내가 지정한 것들이 있으므로 별도의 컬럼명을 지정할 필요는 없다
    )
    private List<Address> addressHistory = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    public Set<String> getFavoriteFood() {
        return favoriteFood;
    }

    public void setFavoriteFood(Set<String> favoriteFood) {
        this.favoriteFood = favoriteFood;
    }

    public List<Address> getAddressHistory() {
        return addressHistory;
    }

    public void setAddressHistory(List<Address> addressHistory) {
        this.addressHistory = addressHistory;
    }
}

```
데이터베이스 컬렉션을 같은 테이블에 저장할 수는 없다
컬렉션들은 기본적으로 일대 대 개념이므로 DB안에는 한 테이블 안에 컬렉션들을 넣을 수 있는 방법이 없다
따라서 정석적으로는 방법이 없으므로 일대 다로 풀어서 별도의 테이블을 만들어내야 한다
그러면서 ADDRESS 테이블 처럼 MEMBER_ID를 조인할 수 있게 조인 키를 넣어주어야 한다
```Java

        try {
            Member member = new Member();
            member.setName("member1");
            member.setHomeAddress(new Address("homeCity", "street", "10000"));

            member.getFavoriteFood().add("치킨");
            member.getFavoriteFood().add("족발");
            member.getFavoriteFood().add("피자");

            member.getAddressHistory().add(new Address("old1", "street", "10000"));
            member.getAddressHistory().add(new Address("old2", "street", "10000"));

            em.persist(member);
            tx.commit();
        }
```
값 타입 컬렉션을 따로 persist하지 않았다 
member만 persist 하면 값 타임 컬렉션들이 다른 테이블임에도 불구하고 자동으로 같이 라이프 사이클이 돌아갔다
왜냐하면 값타입이기 떄문이다
값타임 컬렛션 역시 값타입처럼 본인 스스로의 라이프 사이클이 존재하지 않는다
즉 모든 생명주기가 member에 소속된다
쉽게 말해서 Member에 userneme도 값타입이다 .
조회하는 방법
```Java
try {
            Member member = new Member();
            member.setName("member1");
            member.setHomeAddress(new Address("homeCity", "street", "10000"));

            member.getFavoriteFood().add("치킨");
            member.getFavoriteFood().add("족발");
            member.getFavoriteFood().add("피자");

            member.getAddressHistory().add(new Address("old1", "street", "10000"));
            member.getAddressHistory().add(new Address("old2", "street", "10000"));
            em.persist(member);

            em.flush();
            em.clear();

            System.out.println("-=======================");
            Member findMember = em.find(Member.class, member.getId());
            System.out.println("-=======================");

            tx.commit();
        }
```
member를 조회해서 Member만 가지고 온다
즉 컬렉션들은 전부 지연 로딩이라는 뜻이다
homeAddress는 Member에 소속된 값타입이기 떄문에 같이 불러와진다 
```Java
List<Address> addressHistory = findMember.getAddressHistory();
for(Address address : addressHistory){
  System.out.println("address = " + address).getCity();
} 

Set<String> favoriteFoods = findMember.getFavoritFood();
for(String favoriFood : favoritFoods){
  sout
}
```
* 값 타입 수정 예제
```Java
            System.out.println("-=======================");
            Member findMember = em.find(Member.class, member.getId());

            //homeCity --> newCity
//            findMember.getHomeAddress().setCity("newCity"); //값 타입은 사이드 이펙트가 발생할 수 있으므로 이런식으로 코드를 작성하면 안된다

            Address a = findMember.getHomeAddress();
            findMember.setHomeAddress(new Address("newCity", a.getStreet(), a.getZipcode()));  //값타입은 어드레스 인스턴스 자체를 통체로 갈아 끼워야 한다

            //값타입 컬렉션 업데이트
            //치킨 --> 한식
            //스트링 자체가 값타입이므로 스트링은 통체로 갈아 끼워야 한다 업데이트라는 것 자체를 하면 안된다(할 수도 없고,,,)
            findMember.getFavoriteFood().remove("치킨");
            findMember.getFavoriteFood().add(("한식"));

            System.out.println("-=======================");
```
컬렉션의 값만 변경해도 실제 데이터베이스 쿼리가 날라가면서 뭐가 변경됐는지 알게되며 JPA가 알아서 바꿔준다
핵심은 값타입 컬렉션들은 Member에 의존관계를 맞기는 것이다
```Java
          //값타입이기떄문에 address 인스턴스를 통으로 갈아 끼워야 한다
            //컬렉션마다 다르긴 하지만 기본적으로 컬렉션들을 대상을 찾을 떄 equals를 사용한다
            //따라서 equals, hashcode 가 제대로 구현되어었지 않으면 망한다(기본값을 쓰자)
            findMember.getAddressHistory().remove(new Address("old1", "street", "10000"));
            findMember.getAddressHistory().add(new Address("newCity1", "street", "10000"));
```
MEMBER_ID에 소속된 ADDRESS 테이블을 전부다 지웠다..
그 이후 insert를 두 번한다???
remove에서 old1을 지웠다 old2랑 newCity1이 남아있다

<br>
<br>

* ## _실전 예제 - 6.값 타입 매핑_
---
---



