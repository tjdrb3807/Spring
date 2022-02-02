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



* ### 엔티티 매핑
* ### 객체와 테이블 매핑
* @Entity
* @Table
```Java
@Entity
@Table(name = "MBR")
public class Member {
```
* 실행
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
* form MBR...
* ### 데이터 베이스 스키마 자동 생성
* 애플리케이션 로딩시점에 CREATA 문으로 DB를 생성하고 시작하게 할 수 있다
  * 보통은 테이블을 다 만들어 두고 객체로 돌아와서 개발을 하지만
  * 이렇게 됐을 경우 장점은 JPA는 객체에 맵핑을 다 해두게 되면 애플리케이션이 로딩될 때 필요한 테이블들을 다 만들어준다
* ### 필드와 컬럼 매핑
```Java
package hellojpa;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Member {

    @Id //PK Mapping
    private Long id;

    @Column(name = "name", nullable = false, columnDefinition = "varchar(100) default 'EMPTY'") //DB Column Name
    private String username; //Object Name
    private Integer age; //다른 타입을 사용할 수도 있다(가장 적절한 Type 이 DB에 생성된다)

    //Object 가 Enum 일 경우
    //DB에는 Enum Type 이 존재하지 않는다
    //DB에 Enum Type 을 넣고싶은 경우 @Enumerated 를 사용
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    //날짜 타입
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate; //생성 일자

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate; //수정 일자

    private LocalDate test1;
    private LocalDateTime test2;

    //VARCHAR 를 넘어서는 큰 Type을 넣고싶은 경우
    @Lob
    private String description;

    //DB랑 관계없이 Memory 영역안에서 해결하고 싶은 경우
    @Transient
    private int temp;

    public Member(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTemp() {
        return temp;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }
}
``` 
* ### @Column
* insertable, updatable
* nullable(DDL)
  * nullable = true(default)
  * false로 할 경우 NOT NULL 제약조건이 걸리게된다
* unigue 
  * 잘 사용하지는 않는다
  * 이름을 반영하기 어렵다
* columnDefinition
  ```Java
  @Column(name = "name", nullable = false, columnDefinition = "varchar(100) default 'EMPTY'")  
  ```
* ### @Enumerated
* Enum 이 변경되었을 떄 ORDINAL 에 중복이 발생할 수 있다.. 
* default가 ORDINAL 이므로 EnumTypes.STRING 을 반드시 쓰도록 습관을 갖자
* ### 기본 키 매핑
* DB 가 값을 자동으로 생성해서 할당하는 방법을 사용할떄는 @GeneratedValue 를 사용한다
* @GeneratedValue(strategy = Generation.AUTO) _ default
    * 데이터베이스 방언에 맞춰서 자동으로 생성된다
```
   @Id //PK Mapping
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private String id;
```    
```
    try {
            Member member = new Member();
            member.setName("C");

            em.persist(member);

            tx.commit();
```
```
Hibernate: 
    
    create table Member (
       id varchar(255) generated by default as identity,
        name varchar(255) not null,
        primary key (id)
    )
```
방언은 MySQL로 바꾸면 auto increment 가 된다
* SEQUENCE
Long 를 사용해야 하는 이유
```
@Id //PK Mapping
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
```
```
Hibernate: 
    call next value for hibernate_sequence
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (name, id) 
        values
            (?, ?)
```
* SEQUENCE 전량 매핑
```
@Entity
@SequenceGenerator(name = "MEMBER_SEQ_GENERATOR",
sequenceName = "MEMBER_SEQ")
public class Member {

    @Id //PK Mapping
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_SEQ_GENERATOR")
    private Long id;
```
```
Hibernate: create sequence MEMBER_SEQ start with 1 increment by 50
```
* TABLE 전략
```

@Entity
@TableGenerator(
        name = "MEMBER_SEQ_GENERATOR",
        table = "MY_SEQUENCES",
        pkColumnValue = "MEMBER_SEQ", allocationSize = 1)
public class Member {

    @Id //PK Mapping
    @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "MEMBER_SEQ_GENERATOR")
    private Long id;
```
```
    create table MY_SEQUENCES (
       sequence_name varchar(255) not null,
        next_val bigint,
        primary key (sequence_name)
    )
```
* 권장하는 식별자 전략
자연키: 비즈니스 적으로 의미있는 번호, 주민등록번호나 전화번호 등등...
대채키: sequence, uuid ...
* IDENTITY 전략
id에 값을 넣지안고 DB에 insert를 해야한다
insert query 에 null 로 DB넘어오면 그떄 값을 세팅해준다(DB가)
id값을 알 수 있는 시점은 DB에 들어가봐야 알 수 있다
그런데 JPA 에서 영속성 컨텍스트에서 관리되기 위해서는 무조건 PK값이 있어야 한다
그런데 IDNTITY전략시 PK값은 DB에 들어가봐야 알 수 있기 떄문에 제약이 생긴다 
그래서 보통은 transaction.commit() 시점에 영속성 컨텍스트에서 DB로 쿼리를 보내지만  IDENTITY 전략의 경우에만 특별하게 em.persist(entity)를 호출하는 시점에 바로 insert query를 DB에 보낸다
```

        try {
            Member member = new Member();
            member.setName("C");

            System.out.println("================");
            em.persist(member);
            System.out.println("member.id = " + member.getId());
            System.out.println("================");

            tx.commit();
```
```
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
values 에 id값이 null 로 되어 있으며 DB는 id 값이 1도 되어 있으며 JPA 내부정으로 id의 1 값을 select 해서 가져오며 영속성 컨택스트에 1이라 세팅이 들어간다
* SEQUENCE 전략 특징
```

@Entity
@SequenceGenerator(
        name = "MEMBER_SEQ_GENERATOR",
        sequenceName = "MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
        initialValue = 1, allocationSize = 1)
public class Member {

    @Id //PK Mapping
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "MEMBER_SEQ_GENERATOR")
    private Long id;
```
```
Hibernate: create sequence MEMBER_SEQ start with 1 increment by 1
```
1부터 시작해서 1씩 증가시켜
```
================
Hibernate: 
    call next value for MEMBER_SEQ
member.id = 1
================
```
DB에서 PK 값을 가져온다음 Member 의 id에 값을 넣어준다 
그 다음에 영속성 컨텍스트에 저장을 한다
insert query 는 commit 시점에 날라간다
그런데 이렇게 계속 네트워크를 여러번 타게 되면 성능저하를 고려하게 된다..
그렇다면 어떻게 성능 최적화를 할 수 있을까??
* allocationSize (default = 50)
call next 한 번 할때 미리 50개의 size 를 db에 올려두고 ???   
```
@Entity
@SequenceGenerator(
        name = "MEMBER_SEQ_GENERATOR",
        sequenceName = "MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
        initialValue = 1, allocationSize = 50)
```
```
try {
            Member member1 = new Member();
            member1.setName("A");

            Member member2 = new Member();
            member2.setName("B");

            Member member3 = new Member();
            member3.setName("C");

            System.out.println("===================");

            em.persist(member1);  //1, 51
            em.persist(member2);  //Mem 에서 호출
            em.persist(member3);  //Mem 에서 호출

            System.out.println("member1 = " + member1.getId());
            System.out.println("member2 = " + member2.getId());
            System.out.println("member3 = " + member3.getId());

            System.out.println("===================");

            tx.commit();
```
```
===================
Hibernate: 
    call next value for MEMBER_SEQ
Hibernate: 
    call next value for MEMBER_SEQ
member1 = 1
member2 = 2
member3 = 3
===================
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
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (name, id) 
        values
            (?, ?)
```
MEMBER_SEQ가 두 번 호출되는 이유
처음 호출되면 DB SEQ = 1   | AP = 1
두번 호출됨녀 DB SEQ = 51  | AP = 2
세번 호출됨녀 DB SEQ = 51  | AP = 3
무슨 말인가,,?
나는 50개씩 메모리를 써야하는데 처음 호출해봤더니 1이다..
뭔가 문제가 있나보다 하고 한 번 더 호출
* 객체를 테이블에 맞추어 모델링
* 단방향 연관관계
* 객체 지향 모델링
객체 연관관계에서 TEAM_ID 가 아니라 Team 의 참조값을 그대로 가져왔다
* 기존
```
try {
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeamId(team.getId());
            em.persist(member);

            Member findMember = em.find(Member.class, member.getId());
            Long findTeamId = findMember.getTeamId();
            Team findTeam = em.find(Team.class, findTeamId);

            tx.commit();
```
* 수정
```
try {
            //저장
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeam(team);
            em.persist(member);

            //DB에서 가져오고 싶은 경우
            em.flush();
            em.clear();

            Member findMember = em.find(Member.class, member.getId());
            Team findTeam = findMember.getTeam();
            System.out.println("findTeam = " + findTeam.getName());

            tx.commit();
```
```
try {
            //저장
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeam(team);
            em.persist(member);

            //DB에서 가져오고 싶은 경우
            em.flush();
            em.clear();

            Member findMember = em.find(Member.class, member.getId());

            List<Member> members = findMember.getTeam().getMembers();
            for (Member m : members) {
                System.out.println("member = " + m.getUsername());
            }

            tx.commit();
```
* 양방향 연관관계와 연관관계 주인
* 양방향 매핑
단방향 매핑이던 양뱡향 매핑이던 테이블 연관관계에는 변화가 없다!!!
맴버에서 팀을 알고싶으면 MEMBER Table 의 TEAM_ID(FK)를 TEAM Table에 TEAM_ID(PK)를 JOIN 하면된다
반대로 팀입장에서 팀에 어떤 맴버들이 소속되어 있는지 알고싶으면 TEAM Table의 TEAM_ID(PK)를 MEMBER Table의 TEAM_ID(FK)와 JOIN하면 된다
`즉 테이블의 연관관계는 외래키 하나로 양방향이 다 성립된다!!!`
사실상 테이블의 연관관계에는 방향이라는 개념이 없다고 볼 수 있다
문제는 객체의 연관관계...
단방향 매핑에서는 Member 에 Team team 필드가 있으므로 멤버에서 팀을 알 수는 있었지만 팀에서 멤버를 알 수 없었다
그래서 양방향 객체 연관관계를 성립시키기 위해서는 Team 필드에 List members를 넣어줘야 양방향 매핑이 성립된다
* 연관관계의 주인과 mappedBy
* 객체와 테이블이 관계를 맺는 차이
객체와 테이블간에 연관관계를 맺는 차이를 이해해야 한다
앙방향 매핑에서 객체 연관관계의 진실은 단방향 연관관계 2개이다
회원 -> 팀 / 팀 -> 회원 이 둘을 억지로 양방향 연관관계라 한 것이다, 즉 참조가 각 필드마다 있어야 한다
그러나 양방향 매핑에서 테이블 연관관계는 TEAM_ID(FK) 로 양방향 연관관계가 성립된다
즉 테이블 연관관계에 있어서는 FK 하나로 모든 연관관계가 성립된다
* 둘중 하나로 외래 키를 관리해야 한다
만약 맴버를 바꾸고 싶거나 새로운 팀에 들어가고싶은 상황이 주어졌다고 가정해보자
그렇다면 객체 연관관계에서 Member의 Team team 값을 바꿔야 하나, Team의 List members 의 값을 바꿔야 하나? 하는 딜레마가 생기게 된다.
* 연관관계 주인
* 누구를 주인으로?
```
    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;
```
```
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
```
Team 에 List members 에는 mappedBy가 지정되어 있다, 즉 주인이 아리란 소리며, mappedBy = "team" 이라 지정하므로 Member의 team 에 의해 매핑되었다는 것을 해석을 통해서도 직관적으로 알 수 있다
그렇다면 다른 상황에서는 누구를 주인으로 지정해야 되나?
영한이가 정해주는 가이드 라인: FK가 있는 곳을 주인으로 정해라!!!
왜?? 
DB 입장에서는 FK 가 있는 곳이 "다" 이며, FK가 없는 곳이 "1"이다
그 말은 DB의 N쪽이 연관관계 주인이 되어야 성능 이슈도 없고 설계도 깔끔하다네..
* 양방향 매핑시 가장 많이 하는 실수
연관관계 주인에 값을 입력하지 않음
```Java
   try {
            //저장
            Member member = new Member();
            member.setUsername("member1");
            em.persist(member);

            Team team = new Team();
            team.setName("TeamA");
            team.getMembers().add(member);
            em.persist(team);

            //DB에서 가져오고 싶은 경우
            em.flush();
            em.clear();
            
            tx.commit();
```
insert query 2번
```
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (TEAM_ID, USERNAME, MEMBER_ID) 
        values
            (?, ?, ?)
Hibernate: 
    /* insert hellojpa.Team
        */ insert 
        into
            Team
            (name, TEAM_ID) 
        values
            (?, ?)
```
MEMBER Table 에 TEAM_ID의 값이 null 이네,,,?
연관관계의 주인은 Member.team 인데 jpaMain에서 `team.getMembers().add(member);` 는 mappedBy 로 읽기 전용이다.
즉 JPA 에서 UPDATE, INSERT 할 떄 mappedBy의 변경은 신경쓰지 않는다(읽기 전용)
```Java
        try {
            //저장
            Team team = new Team();
            team.setName("TeamA");
//            team.getMembers().add(member);
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeam(team);
            em.persist(member);

            //DB에서 가져오고 싶은 경우
            em.flush();
            em.clear();

            tx.commit();
```
연관관계 주인에 값을 주입 하므로 (member.setTeam(team)) DB에 정상적으로 값이 입력되었다
* 앙방향 매핑시 연관관계의 주인에 값을 입력해야 한다
순수한 객체 관계를 고려하면 항상 양쪽다 값을 입력해야 한다.
```Java

        try {
            //저장
            Team team = new Team();
            team.setName("TeamA");
//            team.getMembers().add(member);
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeam(team);
            em.persist(member);

            //DB에서 가져오고 싶은 경우
            em.flush();
            em.clear();

            Team findTeam = em.find(Team.class, team.getId());
            List<Member> members = findTeam.getMembers();
            for (Member m : members) {
                System.out.println("m = " + m.getUsername());
            }

            tx.commit();
```
```
m = member1
```
List<Member> members = findTeam.getMembers();
            for (Member m : members) {
                System.out.println("m = " + m.getUsername());
            }
이 부분에서 
```
 select
        members0_.TEAM_ID as TEAM_ID3_0_0_,
        members0_.MEMBER_ID as MEMBER_I1_0_0_,
        members0_.MEMBER_ID as MEMBER_I1_0_1_,
        members0_.TEAM_ID as TEAM_ID3_0_1_,
        members0_.USERNAME as USERNAME2_0_1_ 
    from
        Member members0_ 
    where
        members0_.TEAM_ID=?
```
select query 가 나가게 된다
즉 JAP 가 members 의 데이터를 끌고오는 시점에 쿼리를 한번 날리다
근데 객체 관계를 고려하면 이 부분은 문제가 된다
```Java
try {
            //저장
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeam(team);
            em.persist(member);

            team.getMembers().add(member);

            //DB에서 가져오고 싶은 경우
//            em.flush();
//            em.clear();

            Team findTeam = em.find(Team.class, team.getId());  //1차 캐시
            List<Member> members = findTeam.getMembers();
            for (Member m : members) {
                System.out.println("m = " + m.getUsername());
            }

            tx.commit();
```
flush, clear 를 주석처리하면 team은 1차 캐시에 저장되어있는 상태이므로
```
     try {
            //저장
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeam(team);
            em.persist(member);

//            team.getMembers().add(member);

            //DB에서 가져오고 싶은 경우
//            em.flush();
//            em.clear();

            Team findTeam = em.find(Team.class, team.getId());  //1차 캐시
            List<Member> members = findTeam.getMembers();
            System.out.println("====================");
            for (Member m : members) {
                System.out.println("m = " + m.getUsername());
            }
            System.out.println("====================");

            tx.commit();

```
```
====================
====================
```
다시 말하자면 team 이 영속성 컨텍스트의 1차 캐시에 들어가 있는 상태이며 team에 컬렉션이 아무것도 없다 
따라서 객체지향적으로 생각하면 양쪽 다 값을 세팅해주는것이 올바르다
//            team.getMembers().add(member); 의 주석을 풀어서 확인하자
* 양방향 연관관계 주의 - 실습
연관관계 편ㅇ늬 메소드를 생성하자
```Java
public void setTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
```
this 는 나 자신의 인스턴스 즉 Member
```Java
        try {
            //저장
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setUsername("member1");
            member.setTeam(team);  //**
            em.persist(member);

            //DB에서 가져오고 싶은 경우
            em.flush();
            em.clear();

            Team findTeam = em.find(Team.class, team.getId());  //1차 캐시
            List<Member> members = findTeam.getMembers();
            System.out.println("====================");
            for (Member m : members) {
                System.out.println("m = " + m.getUsername());
            }
            System.out.println("====================");

            tx.commit();
```
* 영한이의 개인적인 취향
연관관계 편의 매서드나 JPA 상태를 변경하는 메소드는 set을 잘 안쓴다
set이 Java의 getter setter 관례떄문에 로직이 없는 단순한 상황에서만 사용
```Java
public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
```




