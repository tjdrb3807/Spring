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
* ### JPA 구동 방식
  ![](img/img262.png)  
    * Persistence.class 에서 시작
    * META-INF/persistence.xml의 설정 정보들을 읽어서 EntitiyManagerFactory.class를 생성한다
    * 필요할 떄마다 EntityManagerFactory에서 EntityManager를 호출해서 실행한다
        ```Java
        package hellojpa;

        public class JpaMain {

            public static void main(String[] args) {
                EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

                EntityManager entityManager = emf.createEntityManager();
                //code
                entityManager.clear();

                emf.close();
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

        public static void main(String[] args) {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

            EntityManager em = emf.createEntityManager();

            EntityTransaction tx = em.getTransaction();
            tx.begin();

            try {
                Member member = new Member();
                member.setId(2L);
                member.setName("HelloB");

                em.persist(member); //member 저장

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
    ![](img/img264.png)
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
    * 지연 로등(Lazy Loading)
    * 애플리케이션이랑 DB 사이에 무엇인가 중간 계층이 존재한다
    * 중간에 무엇인가 있으므로 인하여 버퍼링이나 캐싱등의 이점을 누릴 수 있다
* ### 엔티티 조회, 1차 캐시
  ![](img/img267.png) 

  
* em.persist(member); 를 하게되면 ket 는 @Id 지정한 id 가 되며 값(Entity/value)는 em.persist(member);에서 memeber 객체 자체가 값이 된다
* em.find(Mebmer.class, "member1"); 을 하게되면 JPA 는 DB를 조회하는것이 아니라 1차 캐시를 조회한다
```Java
package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

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
        emf.close();
    }
}

```
* 결과
```
=== BEFORE ===
=== AFTER ===
findMember.id = 101
//select query가 나가지 않았다
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
* select query 가 나가지 않았따
  * em.persist(member)에서 1차 캐시에 저장되었기 때문이다
  * em.find(Member.class, 101L); 같은 PK(101L)로 조회했기 때문에 DB가 아닌 1차 캐시에 있는 것을 조회해서 가져온다
```Java
package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

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
        emf.close();
    }
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
* query 가 한 번만 나갔다
* 101L을 가지고 올 떄 JPA 가 DB에서 가지고 오면서 영속석 컨텍스트에 올려둔다
* 두 번쨰 101L을 조회할 떄 JAP 가 영속성 컨텍스트의 1차 캐시를 조회해서 정보를 가져오므로 두 번째 조회에서는 select query가 한 번만 찍힌다
* ### 영속 엔티티의 동일설 보장
```Java
package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

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
        emf.close();
    }
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
* 엔티티 등록 트랜젝션을 지원하는 쓰기 지연
* em.persistence(memberA) -> memberA 가 1차 캐시에 들어간다 ->  동시에 JPA가 엔티티(memberA)를 분석해서 INSERT SQL 생성 -> 쓰기 지연 SQL 저장소에 쌍아둔다
* em.persistence(memberB) -> memberB 가 1차 캐시에 들어간다 -> 동시에 JPA가 엔티티(memberB)를 분석해서 INSERT SQL 생성 -> 쓰기 지연 SQL 저장소에 쌍하둔다
* transaction.commit() -> 쓰기 지연 SQL에 있던 애들이 flush(JAP용어)되면서 DB로 날라간고 실제 DBTransection 이 발생
```Java
package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            //영속(DB에 저장되는 것이 아니다)
            Member member1 = new Member(150L, "A");
            Member member2 = new Member(160L, "B");

            em.persist(member1);
            em.persist(member2);
            System.out.println("==================");
            
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
* 버퍼링을 구현하는 장점을 가지게 된다
* ### 엔티티 수정 - 변경 감지
```Java
package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            //영속(DB에 저장되는 것이 아니다)
            Member member = em.find(Member.class, 150L);
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
* update SQL 날라갔다
* 수정 이후에 다시 em.persist(member);를 써서 data 가 변경되면 DB에 반경해주어야 하는거 아닌가?
* JPA의 목적은 java 컬랙션 다루듯이 객채를 다루는 것이다
* 영속성 컨텍스트 안에 비밀이 담겨있다
* JPA는 tarnsaction.commit()시점에 영속석 컨텍스트 내부적으로 flush()가 호출된다 
* 1차 캐시에는 @id, Entity, 스냅샷 이 존재한다
  * 스냅샷: 값을 읽어온 최초의 시점(영속성 컨택스트에 들어온)의 상태를 스냅샷으로 떠둔다?(보관한다)
* JAP는 1차 캐시의 @id, Entity, 스냅샷을 비교한다
* Entity가 스냅샷이랑 비교해서 변경되었다면 UPDATE Query를 생성하여 쓰기 지연 SQL 저장소에 저장한다
* #### 플러시
* 간단하게 말해서 쌓아두었던 SQL이 DB로 날라가는것을 말한다
* 영속성 컨텍스트의 변경 사항과 DB를 맞추는 작업이라 할 수 있다
* 주의
* flush() 가 호출된다 해서 DBTransactionCommit이 발생하는 것이 아니라 flush() 는 Query를 보내는 역할을 하고 transcation.commit()에서 commit 이 이루어진다
* ### 영속석 컨텍스트를 flush() 하는 법
* 직접호출(강제 호출)
```Java
package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

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
* * JPA를 통해서 DB를 조회했을 시에 영속성 컨텍스트에 없으면 그게 영속성이 된다
* ### 준영속 상태로 만드는 방법
```Java
package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

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
* ### @Column
* 
