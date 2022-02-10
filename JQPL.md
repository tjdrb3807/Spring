# _객체지향 쿼리 언어(JPQL)_
## _객체지향 쿼리 언어 소개_
<br>

* ### _JPA는 다양한 쿼리 방법을 지원_ 
  * JPQL   
  * JPA Citeria   
  * QueryDSL   
  * 네이티브 SQL   
  * JDBC API 직접 사용, MyBatis, SpringJdbcTemplate 함께 사용   
<br>

* ### _JPQL 소개_
  * 가장 단순한 조회 방밥
    * EntityManager.find()
    * 객체 그래프 탐색(a.getB().getC())
  * JPA를 사용하면 엔티티 객체를 중심으로 개발한다
  * 문제는 검색 쿼리
  * 검색을 할 때도 `테이블이 아닌 엔티티 객체를 대상으로 검색`
  * 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
  * 애플리케이션이 필요한 데이터만 DB에서 불러오면 결국 검색 조건이 포함된 SQL이 필요하다.
  * JPA는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어를 제공한다.
  * SQL과 문법이 유사하다.
    * SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
  * JPQL은 엔티티 객체를 대상으로 쿼리
  * SQL은 데이터베이스 테이블을 대상으로 쿼리
  
   
```Java
try {
    List<Member> result = entityManager.createQuery("select m from Member m where m.username like '%kiim%'",
                    Member.class)
            .getResultList();
    for (Member member : result) {
        System.out.println("member = " + member);
    }

    transaction.commit();
}
```
JPQL문에서 Member는 테이블이 아닌 Entity이다
```SQL
Hibernate: 
    /* select
        m 
    from
        Member m 
    where
        m.username like '%kiim%' */ select
            member0_.member_id as member_i1_1_,
            member0_.locker_id as locker_i3_1_,
            member0_.team_id as team_id4_1_,
            member0_.username as username2_1_ 
        from
            Member member0_ 
        where
            member0_.username like '%kiim%'
```
>주석으로 JPQL이 보인다   
"select m from Member m where m.username like '%kiim%'"은 엔티티를 대상으로 쿼리를 한 것이다   
실제 SQL로 번역이 되서 실행된다   
엔티티의 매핑정보를 확인해서 적절한 SQL은 만들어낸다   
* ### _Criteria 소개_   
위에서 친 "select m from Member m where m.username like '%kiim%'"코드는 인텔리제이가 도와줘서 그런것이지 사실은 단순한 String 이다   
단순 문자이므로 동적 쿼리를 만들기 엄청 어렵다   
이런 난관의 대안법이 Criteria 이다. 꼭 이 난관 뿐 아니라도 다른 장점들도 존재한다    
```Java
try {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Member> query = criteriaBuilder.createQuery(Member.class);

    Root<Member> m = query.from(Member.class);

    CriteriaQuery<Member> cq = query.select(m).where(criteriaBuilder.equal(m.get("username"), "kim"));
    List<Member> result = entityManager.createQuery(cq).getResultList();

    transaction.commit();
}
```
>쿼리를 코드로 짜고있다   
```SQL
Hibernate: 
    /* select
        generatedAlias0 
    from
        Member as generatedAlias0 
    where
        generatedAlias0.username=:param0 */ select
            member0_.member_id as member_i1_1_,
            member0_.locker_id as locker_i3_1_,
            member0_.team_id as team_id4_1_,
            member0_.username as username2_1_ 
        from
            Member member0_ 
        where
            member0_.username=?
```
자바 코드로 SQL문을 짜기 떄문에 엄청난 장점이 있다   
예를 들어 오타가 발생하면 컴파일러 레벨에서 오류를 잡을 수 있다   
또하나의 장점은 조건문 등을 황용해서 동적 쿼리를 짜기에 훨신 수월하다   
하지만 단점은 SQL스럽지 못하다는 점이다   
실무에서는 잘 사용하지 않는다.. 코드를 보기 어려우며 유지보수가 힘들다   
* ### _QueryDSL 소개_   
* ### _네이티브 SQL 소개_   
```Java
try {
    entityManager.createNativeQuery("select member_id, locker_id, team_id, username from MEMBER")
            .getResultList();

    transaction.commit();
}
```
```SQL
Hibernate: 
    /* dynamic native SQL query */ select
        member_id,
        locker_id,
        team_id,
        username 
    from
        MEMBER
```   
* ### _JDBC 직접 사용, SpringJdbcTemplate 등_   
단 영속성 컨텍스트를 적절한 시점에 강제로 플러시 필요   
영속석 컨텍스트는 entityManager.flush()를 호출해야 쿼리를 날려서 DB에 데이터가 저장되게 된다   
```Java
try {
    Member member = new Member();
    member.setUsername("memberA");

    entityManager.persist(member);

    //flush()가 호출되는 시점 --> commit()호출시, Query 날라갈때
    System.out.println(" ===========================");
    List<Member> resultList = entityManager.createNativeQuery("select member_id, locker_id, team_id, username from MEMBER", Member.class)
            .getResultList();
    System.out.println(" ===========================");

    for (Member member1 : resultList) {
        System.out.println("member1 = " + member1);
    }

    transaction.commit();
}
```
```SQL
===========================
Hibernate: 
    /* insert hellojpa.relationmapping.Member
        */ insert 
        into
            Member
            (locker_id, team_id, username, member_id) 
        values
            (?, ?, ?, ?)
```
>flush()가 먼저 되고 Query가 호출된다   
flush()는 commit()직전에 호출되기도 하지만 entityManager를 통해서 Query가 날라갈때고 flush()가 호출된다   
JPA관련 기술들을 사용할 떄는 이렇게 flush()호출이 AUTO 인데 만약 JPA 관련기술이 아닌 기술을 사용한다면...   
commti() 이전에 쿼리를 날린다면 flush()가 호출되지 않아 DB에 데이터는 null 인된다   
따라서 JPA 관련 기술이 아닌 것을 사용할떄 Query문을 날리기 전에 강제로 flush()를 호출해서 사용하도록 한다   

```SQL
Hibernate: 
    /* dynamic native SQL query */ select
        member_id,
        locker_id,
        team_id,
        username 
    from
        MEMBER
Hibernate: 
    select
        team0_.team_id as team_id1_4_0_,
        team0_.name as name2_4_0_ 
    from
        Team team0_ 
    where
        team0_.team_id=?
===========================
member1 = hellojpa.relationmapping.Member@310aee0b
member1 = hellojpa.relationmapping.Member@1f1ff879
```   
## _JPQL(Java Persistence Query Language)_   
## _JPQL - 기본 문법과 기능_   
* ### _JPQL 소개_   
`JPQL은 SQL을 추강화해서 특정데이터베이스 SQL에 의존하지 않는다.`   
`JPQL은 결국 매핑 정보랑 방언이 조합이 되서 SQL로 변환돼 실행된다.`
결국 DB는 SQL만 받을 수 있느니까   
사진 첨부하고 새 프로젝트에 Member, Team, JpaMain, Orders, Product 코드 작성   
* ### _JPQL문법_   
엔티이 이름: @Entity(name ="MM") default: Class Name   
관례상 default를 사용   
* ### _TypeQuery, Query_   
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    //두 번째 파라미터에 응답 클래스에 대한 타입 정보를 중 수 있다(타입 정보는 아무거나 줄 수없으며 기본적으로 엔티티를 줘야한다
    TypedQuery<Member> query1 = entityManager.createQuery("select m From Member m", Member.class);
    TypedQuery<String> query2 = entityManager.createQuery("select m.username From Member m", String.class);
    //m.username 은 String, m.age 는 int 로 타입이 서로 달라 두 번째 파라미터에 타입을 명시할 수 없다.
    //이럴때는 반환타입을 Query를 사용해야 한다(타입 정보를 방을 수 없을 경우)
    Query query3 = entityManager.createQuery("select m.username, m.age From Member m");

    transaction.commit();
}
```   
* ### _결과 조회 API_   
결과가 하나 이상일 때, 즉 결과가 컬렉션일 경우 getResultList()를 사용한다   
결과가 없으면 빈 리스트를 반환하므로 nullPointException에 대해서는 걱정할 필요가 없다     
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    TypedQuery<Member> query = entityManager.createQuery("select m From Member m", Member.class);
    List<Member> resultList = query.getResultList(); //컬렉션을 반환한다

    for (Member member1 : resultList) {
        System.out.println("member1 = " + member1);
    }

    transaction.commit();
}
```   
결과가 정확히 하나   
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    TypedQuery<Member> query = entityManager.createQuery("select m From Member m", Member.class);
    Member result = query.singleResult();

    System.out.println("result = " + result);

    transaction.commit();
```    
결과가 없는으면   
```Java
try {
    TypedQuery<Member> query = entityManager.createQuery("select m From Member m", Member.class);

    Member result = query.getSingleResult();
    System.out.println("result = " + result);

    transaction.commit();
}
```
```
javax.persistence.NoResultException: No entity found for query
```   
* ### _파라미터 바인딩 - 이름 기분, 위치 기준_   
이름 기반 파라미터 바인딩   
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    TypedQuery<Member> query = entityManager.createQuery("select m From Member m where m.username = :username ", Member.class);
    query.setParameter("username", "member1");
    Member result = query.getSingleResult();
    System.out.println("result = " + result.getUsername());

    transaction.commit();
} 
```   
일반적으로는 메서드 체인을 이용   
```Java
Member result = entityManager.createQuery("select m From Member m where m.username = :username ", Member.class)
        .setParameter("username", "member1")
        .getSingleResult();

System.out.println("result = " + result.getUsername());

transaction.commit();
```
위치 기반 파라미터 바인딩   
위치기반은 웬만하면 사용하지 않는다   
왜냐하면 숫자로 표기되어 있는데  중간에 추가 되거나 삭제 된다면  구분하기 힘들어진다   
* ### _프로젝션_   
엔티티 프로젝션     
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    //엔티티들이 반환이 됐는데 List<Member>에서 Member 는 영속성 컨텍스트에 관리가 될것인가 안될것이기?
    List<Member> result = entityManager.createQuery("select m From Member m", Member.class)
            .getResultList();

    Member findMember = result.get(0);
    findMember.setAge(20); //바뀌면 영속성 컨텍스트에서 관리가 되는것이고, 바뀌지 않으면 영속성 컨텍스트에서 관리가 되지 않는것이다

    transaction.commit();
}
```
```SQL
Hibernate: 
    /* update
        jpql.Member */ update
            Member 
        set
            age=?,
            team_id=?,
            username=? 
        where
            member_id=?
```
>엔티티 프로젝션을 사용하면 "select m From Member m"의 select 절에 대상이 전부다 영속성 컨텍스트에서 관리가 된다   

select m.team from Member m: Member 엔티티에 연관된 Team 엔티티 프로젝션 
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    List<Team> result = entityManager.createQuery("select m.team From Member m", Team.class)
            .getResultList();

    transaction.commit();
}
```
```SQL
Hibernate: 
    /* select
        m.team 
    From
        Member m */ select
            team1_.team_id as team_id1_3_,
            team1_.name as name2_3_ 
        from
            Member member0_ 
        inner join
            Team team1_ 
                on member0_.team_id=team1_.team_id
```   
>inner join 해서 team 이랑 조인을 하고 있다   
조인 쿼리가 나가는 이유   
JPQL은 select m.team From Member m" 이렇게 생겼지만 SQL 입장에서는 Member에 연관된 Team 테이블을 조인해서 찾아야 한다     

임베디드 타입 프로젝션   
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    entityManager.createQuery("select o.address From Order o", Address.class)
            .getResultList();

    transaction.commit();
}
```
```SQL
Hibernate: 
    /* select
        o.address 
    From
        
    Order o */ select
        order0_.city as col_0_0_,
        order0_.street as col_0_1_,
        order0_.zipcode as col_0_2_ from
            ORDERS order0_
```
>이베디드 타입 프로젝션의 한계는 임베디드 타입만으로는 안되며, 엔티티로부터 시작을 해야한다   

스칼라 타입 프로젝션   
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    entityManager.createQuery("select distinct m.username, m.age From Member m")
            .getResultList();

    transaction.commit();
} 
```
```SQL
Hibernate: 
    /* select
        distinct m.username,
        m.age 
    From
        Member m */ select
            distinct member0_.username as col_0_0_,
            member0_.age as col_1_0_ 
        from
            Member member0_
```
>스칼라 타입 프로젝션이 일반 SQL의 select 프로젝션과 똑같다고 볼 수 있다   

* ### _프로젝션 - 여러 값 조회_   
Query 타입으로 조회
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    List resultList = entityManager.createQuery("select m.username, m.age From Member m")
            .getResultList();

    Object o = resultList.get(0);
    //내부적으로는 Object 배열이 들어간다(m.username, m.age 가 배열의 첫 번쨰, 두 번째)
    Object[] result = (Object[]) o; //타입 캐스팅
    System.out.println("result = " + result[0]);
    System.out.println("result = " + result[1]);

    transaction.commit();
}
```
```
result = member1
result = 20
```
>타입을 명시할 수 없으니 Object로 반환하는 것이다   

Object[] 타입으로 조회   
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    List<Object[]> resultList = entityManager.createQuery("select m.username, m.age From Member m")
            .getResultList();

    Object[] result = resultList.get(0);
    System.out.println("result = " + result[0]);
    System.out.println("result = " + result[1]);

    transaction.commit();
}
```
>제네릭에 Object[] 를 선언하는 방법   

new 명령어로 조회(가장 깔끔한 방법)   
```Java
        try {
            Member member = new Member();
            member.setUsername("member1");
            member.setAge(10);
            entityManager.persist(member);

            entityManager.flush();
            entityManager.clear();

            //생성자를 통해서 호출이 된다
            List<MemberDTO> result = entityManager.createQuery("select new jpql.MemberDTO(m.username, m.age) From Member m", MemberDTO.class)
                    .getResultList();

            MemberDTO memberDTO = result.get(0);
            System.out.println("memberDTO = " + memberDTO.getUsername());
            System.out.println("memberDTO = " + memberDTO.getAge());

            transaction.commit();
        }
```

* ### _페이징 API_   
* ### _페이징 API 예시_   
```Java
try {
    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    List<Member> result = entityManager.createQuery("select m from Member m order by m.age desc", Member.class)
            .setFirstResult(1)
            .setMaxResults(10)
            .getResultList();

    System.out.println("result.size = " + result.size());
    //Member 클레스에 toString
    for (Member member1 : result) {
        System.out.println("member1 = " + member1);
    }

    transaction.commit();
} 
```
```SQL
Hibernate: 
    /* select
        m 
    from
        Member m 
    order by
        m.age desc */ select
            member0_.member_id as member_i1_0_,
            member0_.age as age2_0_,
            member0_.team_id as team_id4_0_,
            member0_.username as username3_0_ 
        from
            Member member0_ 
        order by
            member0_.age desc limit ? offset ?
result.size = 8
```
>desc limit ? offset ?   

* ### _조인_   
inner join (inner)생량 가능   
```Java
try {
    Team team = new Team();
    team.setName("teamA");

    entityManager.persist(team);

    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    member.setTeam(team);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    String query = "select m from Member m inner join m.team t";
    List<Member> result = entityManager.createQuery(query, Member.class)
            .getResultList();

    transaction.commit();
}
```
```SQL
Hibernate: 
    /* select
        m 
    from
        Member m 
    inner join
        m.team t */ select
            member0_.member_id as member_i1_0_,
            member0_.age as age2_0_,
            member0_.team_id as team_id4_0_,
            member0_.username as username3_0_ 
        from
            Member member0_ 
        inner join
            Team team1_ 
                on member0_.team_id=team1_.team_id
```
> fetch LAZY 설정   

* ### _1. 조인 대상 필터링_   
on 은 join할 때 조건   
* ### _서브 쿼리_   
서브쿼리와 메인 쿼리는 전혀 관계가 없다   
* ### _ JPQL 타입 표현식_   
```Java
try {
    Team team = new Team();
    team.setName("teamA");

    entityManager.persist(team);

    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    member.setTeam(team);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    String query = "select m.username, 'HELLO', true from Member m ";
    List<Object[]> result = entityManager.createQuery(query)
            .getResultList();

    for (Object[] objects : result) {
        System.out.println("objects = " + objects[0]);
        System.out.println("objects = " + objects[1]);
        System.out.println("objects = " + objects[2]);
    }

    transaction.commit();
}
```
```
objects = member1
objects = HELLO
objects = true
```
ENUM   
```Java
try {
    Team team = new Team();
    team.setName("teamA");

    entityManager.persist(team);

    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    member.setTeam(team);
    member.setType(MemberType.ADMIN);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    String query = "select m.username, 'HELLO', true from Member m " +
                    "where m.type = jpql.MemberType.ADMIN";
    List<Object[]> result = entityManager.createQuery(query)
            .getResultList();

    for (Object[] objects : result) {
        System.out.println("objects = " + objects[0]);
        System.out.println("objects = " + objects[1]);
        System.out.println("objects = " + objects[2]);
    }

    transaction.commit();
} 
```
파라미터 바인딩을 해준다   
```Java

String query = "select m.username, 'HELLO', true from Member m " +
                "where m.type = :userType";
List<Object[]> result = entityManager.createQuery(query)
        .setParameter("userType", MemberType.ADMIN)
        .getResultList();
```
* ### _조건식 - CASE식_   
기본 CASE식   
```Java
try {
    Team team = new Team();
    team.setName("teamA");

    entityManager.persist(team);

    Member member = new Member();
    member.setUsername("member1");
    member.setAge(10);
    member.setTeam(team);
    member.setType(MemberType.ADMIN);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    String query = "select " +
                           "case when m.age <= 10 then '학생요금' " +
                           "     when m.age >= 60 then '경로요금' " +
                           "     else '일반요금' " +
                           "end " +
                   "from Member m";
    List<String> result = entityManager.createQuery(query, String.class)
            .getResultList();

    for (String s : result) {
        System.out.println("s = " + s);
    }

    transaction.commit();
}
```   
COALESCE   
```Java
try {
    Team team = new Team();
    team.setName("teamA");

    entityManager.persist(team);

    Member member = new Member();
    member.setUsername(null);
    member.setAge(10);
    member.setTeam(team);
    member.setType(MemberType.ADMIN);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    String query = "select coalesce(m.username, '이름 없는 회원') from Member m ";
    List<String> result = entityManager.createQuery(query, String.class)
            .getResultList();

    for (String s : result) {
        System.out.println("s = " + s);
    }

    transaction.commit();
}
```
NULLIF   
```Java
try {
    Team team = new Team();
    team.setName("teamA");

    entityManager.persist(team);

    Member member = new Member();
    member.setUsername("관리자");
    member.setAge(10);
    member.setTeam(team);
    member.setType(MemberType.ADMIN);
    entityManager.persist(member);

    entityManager.flush();
    entityManager.clear();

    String query = "select nullif(m.username, '관리자') from Member m ";
    List<String> result = entityManager.createQuery(query, String.class)
            .getResultList();

    for (String s : result) {
        System.out.println("s = " + s);
    }

    transaction.commit();
}
```
* ### _JPQL 기본 함수_   
SIZE --> select size(t.Members) from Team t   
컬렉션의 크기를 반환한다    
* ### _사용자 정의 함수 호출_   
```Java
            String query = "select function('group_concat', m.username) from Member m ";
            List<String> result = entityManager.createQuery(query, String.class)
                    .getResultList();

            for (String s : result) {
                System.out.println("s = " + s);
            }

            transaction.commit();
```
injectLanguage 설정을 Hibernate로 바꾼다면   
select group_concat(m.username) from Member m    
## _JPQL - 경로 표현식_   
* ### _경로 표현식_   
세가지 경로 필드가 존재한다(상태 필드, 단일 값 연괄 필드, 컬렉션 값 연관 필드)   
어떤 결로 필드로 탐색하느냐에 따라 내부적으로 동장하는 방식 즉 결과가 달라진다  
즉 이 세가지를 꼭 구분해서 이해해야 한다   
select m.username -> 상태 필드로 객체 그래프를 탐색했다   
* ### _결로 표현식 특징_   
상태 필드는 경로 탐색의 끝 이므로 더이상 탐색이 불가능하다  
단일 값 연관 경로는 `묵시적 내부 조인이 발생한다`   
```Java
            String query = "select m.team From Member m";
            List<Team> result = entityManager.createQuery(query, Team.class)
                    .getResultList();

            for (Team s : result) {
                System.out.println("s = " + s);
            }

            transaction.commit();
```
```SQL
Hibernate: 
    /* select
        m.team 
    From
        Member m */ select
            team1_.team_id as team_id1_3_,
            team1_.name as name2_3_ 
        from
            Member member0_ 
        inner join
            Team team1_ 
                on member0_.team_id=team1_.team_id
```
JPQL은 select m.team 이지만 SQL은 join Team team 을 해서 team을 select 프로젝션에 나열했다   
객체 입장에서는 .을 이요해서 탐색을 하면 되지만 DB입장에서는 Join을 해서 탐색을 해야한다   
이떄 발생한 Join을 묵시적 내부 조인 이라 한다   
실무에서는 JPQL을 묵시적 내부조인이 발생하지 않도록 작성해야 한다   
컬렉션 값 연관 경로는 묵시적 내부 조인이 발생하며 탐색이 불가능하다   
따라서 From 절에 명시적 조인을 통해서 별칭을 얻으면 별칭을 통해 탐색한다   
select m.username From Team t Join t.members m   
From 절에서 명시적 조인을 하면 별칭을 얻을 수 있다   
영한이가 권장하는 방법은 위의 것들을 다 무시하고 묵시적 조인을 사용하지 않는것이다   
명시족 조인을 사용해야 한다  실제 쿼리 튜닝하기가 쉽다   
* ### _명시적 조인, 묵시적 조인_   
묵시적 조인: 경로 표현식에 의해 묵시적으로 SQL 조인 발생   
내부조인(innerJoin)만 가능 
외부 조인은 불가능하다 외부조인을 하고싶다면 명시적 조인을 하면 된다   
Select m From Member m left join m.team t   
* ### _경로 표현식 - 예제_   
select o.member.team From Order o   
join이 두 번 발생하다   
select t.members from Team   
members가 컬렉션이지만 끝을 냈으므로 성공, 더 들어가게 되면 탐색 불가로 실패   
select t.members.username From Team t   
members를 그대로 가져오거나 .size() 정도만 가능하다   
## _JPQL - 페치 조인(fetch join)_   
* ### _엔티티 페치 조인_   
SQL   
즉시로딩 SQL이랑 똑같은 쿼리문   
하지만 페치 조인은 쿼리로 내가 원하는 어떤 객체 그래프를 한 번에 조회할것이라는 것을  내가 직접 명시적으로 동적인 타이밍에 정할 수 있다   
* ### _테이블 세팅_   
```Java
        try {
            Team teamA = new Team();
            teamA.setName("팀A");

            entityManager.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");

            entityManager.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);

            entityManager.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);

            entityManager.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);

            entityManager.persist(member3);

            entityManager.flush();
            entityManager.clear();

            String query = "select m From Member m";
            List<Member> result = entityManager.createQuery(query, Member.class)
                    .getResultList();

            for (Member member : result) {
                System.out.println("member = " + member);
            }

            transaction.commit();
        }
``` 
```SQL
Hibernate: 
    /* select
        m 
    From
        Member m */ select
            member0_.member_id as member_i1_0_,
            member0_.age as age2_0_,
            member0_.team_id as team_id5_0_,
            member0_.type as type3_0_,
            member0_.username as username4_0_ 
        from
            Member member0_
member = Member{id=3, username='회원1', age=0}
member = Member{id=4, username='회원2', age=0}
member = Member{id=5, username='회원3', age=0}
```
* ### _페치 조인 사용 코드_   
Member 와 Team 의 연관관계가 ManyToOne이며 지연로딩(LAZY)설정   
```Java
@Entity
public class Member {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private List<Team> team;


}
```
member의 이름과 member와 연관관계 매핑된 team 의 이름을 같이 출력하는 코드 작성   
```Java
            for (Member member : result) {
                System.out.println("member = " + member.getUsername() + ", " + member.getTeam().getName());
                //회원1, 팀A(SQL)
                //회원2, 팀A(1차 캐시)
                //회원3, 팀B(SQL)
            }

```
Team 은 프록시로 들어왔다가 member.getTeam().getName() 호출 시점에 DB에 쿼리를 날린다    




## 다형성 쿼리
## 엔티티 직접 사용
## Named 쿼리
## 벌크 연산
