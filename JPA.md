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

<br>

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
* ### _기능 목록_
  * 회원 기능
    * 회원 등록
    * 회원 조회
  * 상품 기능
    * 상품 등록
    * 상품 수정
    * 상품 조회
  * 주문 기능
    * 상품 주문
    * 주문 내역 조회
    * 주문 취소
  * 기타 요구사항
    * 상품은 재고 관리가 필요하다.
    * 상품의 종류는 도서, 음반, 영화가 있다.
    * 상품을 카테고리로 구분할 수 있다.
    * 상품 주문시 배송 정보를 입력할 수 있다.   

<br>

## _도메인 모델과 테이블 설계_
* ### _도메인 모델과 테이블 설계_    
    ![](img/img334.png) 
  * `회원, 주문, 상품의 관계`
    >회원은 여러 상품을 주문할 수 있다. 그리고 한 번 주문할 때 여러 상품을 선택할 수 있으므로 주문과 상품은 다대다 관계다.   
    하지만 이런 다대다 관계는 관계형 데이터베이스는 물론이고 엔티티에서도 거의 사용하지 않는다.   
    따라서 그림처럼 주문상품이라는 엔티티를 추가해서 다대다 관계를 일대다, 다대일 관계로 풀어냈다.   
  * `상품 분류`
    >상품은 도서, 음반, 연화로 구분되는데 상품이라는 공통 속성을 사용하므로 상속 구조로 표현했다.   
* ### _엔티티 분석_  
    ![](img/img335.png)
  * `회원(Member)`
    * 이름과 임베디드 타입인 주소(`Adress`), 그리고 주문(`order`) 리스트를 갖는다.
  * `주문(order)`
    * 한 번 주문시 여러 상품을 주문할 수 있으므로 주문과 주문상품(`OrderItem`)은 일대다 관계다. 
    * 주문은 상품을 주문한 회원과 배송 정보, 주문 날짜, 주문 상태(`status`)를 가지고 있다. 
    * 주문 상태는 열거형을 사용했는데 주문(`ORDER`), 취소(`CANCEL`)을 표현할 수 있다.
  * `주문 상품(OrderItem)`
    * 주문한 상품 정보와 주문 금액(`orderPrice`), 주문 수량(`count`)정보를 가지고 있다.
    * 보통 `OrderLine`, `LineItem`으로 많이 표현한다.
  * `배송(Delivery)`
    * 주문시 하나의 배공 정보를 생성한다. 
    * 주문과 배송은 일대일 관계다.
  * `카테고리(Category)`
    * 상품과 다대다 관계를 맺는다. 
    * `parent`, `child`로 부모, 자식 카테고리를 연결한다.
  * `주소(Address)`
    * 값 타입(임베디드)이다. 
    * 회원과 배송(Delivery)에서 사용한다.
  * 참고   
    >회원이 주문을 하기 때문에, 회원이 주문리스트를 가지는 것은 얼핏 보면 잘 설계한 것 같지만, 객체 세상은 실제 세계와는 다르다.   
    실무에서는 회원이 주문을 참조하지 않고, 주문이 회원을 참조하는 것으로 충분하다.   
    여기서는 일대다, 다대일의 양방향 연관관계를 설명하기 위해서 추가했다.   

<br>

* ### _테이블 분석_   
    ![](img/img336.pnf.png)
  * `MEMBER` 
    * 회원 엔티티의 `Address`임베디드 타입 정보가 회원 테이블에 그대로 들어갔다. 
    * 이것은 `DELIVERY`테이블도 마찬가지다.
  * `ITEM`
    * 앨범, 도서, 영화 타입을 통합해서 하나의 테이블로 만들었다.
    * `EDTYP`컬럼으로 타입을 구분한다.
    * 참고
      >테이블명이 `ORDER`가 아니라 `ORDERS`인 것은 데이터베이스가 `order by`때문에 예약어로 잡고 있는 경우가 많다.   
      따라서 관례상 `ORDERS`를 많이 사용한다.   
    * 참고: `실제 코드에서는 DB에 소문자 + _(언터스코어) 스타일을 사용한다.`
      >데이터베이스 테이블명, 컬럼명에 대한 관례는 회사마다 다르다.   
      보통은 대문자 + _(언더스코어)나 소문자 + _(언더스코어) 방식 중에 하나를 지정해서 일관성 있게 사용한다.

<br>

* ### _연관관계 매핑 분석_   
  * `회원과 주문`
    * 일대다, 다대일 양방향 관계
    * 왜래 키가 존재하는 테이블(`ORDERS`)과 매핑된 엔티티(`Order`)를 연관관계의 주인으로 지정한다.
    * `Order.member`를 `ORDERS.MEMBER_ID`외래 키와 매핑한다. 
  * `주문상품과 주문`
    * 다대일 양방향 관계
    * 외래 키가 존재하는 테이블(`ORDER_ITEM`)과 매핑된 엔티티(`OrderItem`)를 연관관계의 주인으로 지정한다.
    * `OrderItem.order`를 `ORDER_ITEM.ORDER_ID`외래 키와 매핑한다.
  * `주문상품과 상품`
    * 다대일 단방향 관계
    * `OrderItem.item`을 `ORDER_ITEM.ITEM_ID`외래 키와 매핑한다.
  * `주문과 배송`
    * 일대일 양방향 관계
    * `Order.delivery`를 `ORDERS.DELIVERY_ID`왜래 키와 매핑한다.
  * `카테고리와 상품`
    * `@ManyToMany`를 사용해서 매핑한다.
    * 실무에서는 @ManyToMany는 사용하지 않는다.
  * 참고: 외래 키가 있는 곳을 연관관계의 주인으로 정해라.
    >연관관계의 주인은 단순히 외래 키를 누가 관리하냐의 문제이지 비즈니스상 우위에 있다고 주인으로 정하면 안된다.   
    예를 들어서 자동차와 바퀴가 있으면, 일대다 관계에서 항상 다쪽에 외래 키가 있으므로 외래 키가 있는 바퀴를 연관관계의 주인으로 정하면 된다.   
    물론 자동차를 연관관계의 주인으로 정하는 것이 불가능 한 것은 아니지만, 자동차를 연관관계의 주인으로 정하면 자동차가 관리하지 않는 바퀴 테이블의 외래 키 값이 업테이트 되므로 관리와 유지보수가 어렵고, 추가적으로 별도의 업테이트 쿼리가 발생하는 성능 문제도 있다.    


<br>

## _엔티티 클래스 개발_
* 실무에서는 가급적 Getter는 열어두고, Setter는 꼭 필요한 경우에만 사용하는 것을 추천한다.
* 참고
  >이론적으로는 Getter, Setter모두 제공하지 않고, 꼭 필요한 별도의 메서드를 제공하는게 가장 이상적이다.   
  하지만 실무에서 엔티티의 데이터는 조회할 일이 너무 많으므로, Getter의 경우 모두 열어두는 것이 편리하다.   
  Getter는 아무리 호출해도 호출 하는 것 만으로는 어떤 일이 발생하지 않는다.   
  하지만 Setter는 문제가 다르다.   
  Setter를 호출하면 데이터가 변한다.   
  Setter를 막 열어두면 가까운 미래에 엔티티가 도대체 왜 변경되는지 추적하기 점점 힘들어진다.   
  따라서 엔티티를 변경할 떄는 Setter 대신에 변경 지점이 명확하도록 변경을 위한 비즈니스 메서드를 별도로 제공해야 한다.   

<br>

* ### _회원 엔티티_
  ```Java
  pacage jpabook.jpashop.domain

  @Entity
  @Getter @Setter
  public class Member {

      @Id @GeneratedValue
      @Column(name ="member_id")
      private Long id;

      private String namel

      @Embedded
      private Address address;

      @OneToMany(mappedBy = "order_id")
      private List<Order> orders = new ArrayList<>();
  }
  ```  
* 참고
  >엔티티의 식별자는 `id`를 사용하고 PK 컬럼명은 `member_id`를 사용했다.   
  엔티티는 타임(여기서는 `Member`)이 있으므로 `id`필드만으로 쉽게 구문할 수 있다.   
  테이블은 타입이 없으므로 구분이 어렵다.   
  그리고 테이블은 관례상 `테이블 명 + id`를 많이 사용한다.   
  참고로 객체에서 `id`대신에 `memberId`를 사용해도 된다.   
  중요한 것은 일관성이다.   

<br>

* ### _주문 엔티티_   
   ```Java
   package jpabook.jpashop.domain;

   @Entity
   @Table(name = "orders")
   @Getter @Setter
   public class Order {

       @Id @GeneratedValue
       @Column(name = "order_id")
       private Long id;

       @ManyToOne(fetch = FetchType.LAZY)
       @JoinColumn(name = "member_id")
       private Mamver member;  //주문 회원

       @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
       private List<OrderItem> orderItems = new ArrayList<>();

       @OneToOne(fetch = FetchType.LAZY)
       @JoinColumn(name = "delivery_id")
       private Delivery delivery;   //배송 정보

       private LocalDateTime orderDate;  //주문 시간

       @Enumerated(EnumType.STRING)
       private OrderStatus status;  //주문 상태[ORDER, CANCEL]

       //==== 연관관계 메서드 =====//
       public void setMember(Member member) {
           this.member = member;
           member.getOrders().add(this);
       }

       public void addOrderItem(OrderItem orderItem) {
           orderItems.add(orderItem);
           orderItem.setOrder(this);
       }

       public void setDelivery(Delivery delivery) {
           this.delivery = delivery;
           delivery.setOrder(this);
       }
   }
   ```

<br>

* ### _주문 상태_
    ```Java
    package jpabook.jpashop.domain;

    public enum OrderStatus {

        ORDER, CANCEL
    }
    ```

<br>

* ### _주문상품 엔티티_   
    ```Java
    package jpabook.jpashop.domain;

    @Entity
    @Getter @Setter
    public class OrderItem {

        @Id @GeneratedValue
        @Column(name = "order_item_id")
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "item_id")
        private Item item;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "order_id")
        private Order order;

        private int orderPrice;
        private int count;
    }
    ```

<br>

* ### _상품 엔티티_
    ```Java
    package jpabook.jpashop.domain.item;

    @Entity
    @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    @DiscriminatorColumn(name = "dtype")
    @Getter @Setter
    public abstract class Item {

        @Id @GeneratedValue
        @Column(name = "item_id")
        private Long id;

        @ManyToMany(mappedBy = "items")
        private List<Category> categories = new ArrayList<>();

        private String name;
        private int price;
        private int stockQuantity;
    }
    ```

<br>

* ### _상품-도서 엔티티_
    ```Java
    package jpabook.jpashop.domain.item;

    @Entity
    @DiscriminatorValue("B")
    @Getter @Setter
    public class Book extends Item {

        private String author;
        private String isbn;
    }
    ```

<br>

* ### _상품-음반 엔티티_
    ```Java
    package jpabook.jpashop.domain.item;

    @Entity
    @DiscriminatorValue("A")
    @Getter @Setter
    public class Album extends Item {

        private String artist;
        private String etc;
    }
    ```

<br>

* ### _상품-영화 엔티티_
    ```Java
    package jpabook.jpashop.domain.item;

    @Entity
    @DiscriminatorValue("M")
    @Getter @Setter
    public class Movie extends Item {

        private String director;
        private String actor;
    }
    ```

<br>

* ### _배송 엔티티_
    ```Java
    package jpabook.jpashop.domain;

    @Entity
    @Getter @Setter
    public class Delivery {
        
        @Id @GeneratedValue
        @Column(name = "delivery_id")
        private Long id;

        @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
        private Order order;

        @Embedded
        private Address address;

        @Enumerated(EnumType.STRING)
        private DeliveryStatus status;
    }
    ```

<br>

* ### _배송 상태_
    ```Java
    package jpabook.jpashop.domain;

    public enum DeliveryStatus {

        READY, COMP
    }
    ```

<br>

* ### _카테고리 엔티티_
    ```Java
    @Entity
    @Getter @Setter
    public class Category {

        @Id @GeneratedValue
        @Column(name = "category_id")
        private Long id;

        private String name;

        @ManyToMany
        @JoinTable(name = "category_item", 
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
        private List<Item> items = new ArrayList<>();

        @ManyToOne(fetch = FetchType.LAZY)
        private Category parent;

        @OneToMany(mappedBy = "parent")
        private List<Category> child = new ArrayList<>();

        //==== 연관관계 메서드 ====//
        public void addChildCategory(Category child) {
            this.child.add(child);
            child.setParent(this);
        }
    }
    ```
  * 참고
    >실무에서는 `@ManyToMany`를 사용하지 말자   
    `@ManyToMany`는 편리한 것 같지만, 중간 테이블(`CATEGORY_ITEM`)에 컬럼을 추가할 수 없고, 세밀하게 쿼리를 실행하기 어렵기 때문에 실문에서 사용하기에는 한계가 있다.   
    중간 엔티티(`CategoryItem`)를 만들고 `@ManyToOne`, `@OneToMany`로 매핑해서 사용하자.   
    정리하자면 다대다 매핑을 일대다, 다대일 매핑으로 풀어내서 사용하자.    

<br>

* ### _주소 값 타입_   
    ```Java
    package jpabook.jpashop.domain;

    @Embeddable
    @Getter
    public class Address {

        private String city;
        private String street;
        private String zipcode;

        protected Address(){}

        public Address(String city, String street, String zipcode) {
            this.city = city;
            this.street = street;
            this.zipcode = zipcode;
        }
    }
    ```
  * 참고: 값 타입은 변경 불가능하게 설계해야 한다.
    >`@Setter`를 제거하고, 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스를 만든다.   
    JPA 스펙상 엔티티나 임베디드 타임(`@Embeddable`)은 자바 기본 생성사(default constructor)를 `public`또는 `protected`로 설정해야 한다.    
    `public`으로 두는 것 보다는 `protected` 로 설정하는 것이 그나마 더 안전하다.   
    JPA가 이런 제약을 두는 이유는 JPA 구현 라이브러리가 객체를 생성할 떄 리플렉션 같은 기술을 사용할 수 있도록 지원해야 하기 때문이다.   

<br>

## _엔티티 설계시 주의점_
* ### _엔티티에는 가급적 Setter를 사용하지 말자_
  * Setter가 모두 열려있다면 변경 포인트가 너무 많아서 유지보수가 어렵다. 
  * 나중에 리펙토링으로 Setter 제거

<br>

* ### _모든 연관관계는 지연로딩으로 설정!_
  * 즉시 로딩(`EAGER`)은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다. 특히 JPQL을 실행할 떄 N + 1 문제가 자주 발생한다.
  * 실무에서 모든 연관관계는 지연로딩(`LAZY`)으로 설정해야 한다.
  * 연관된 엔티티를 함꼐 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용한다.
  * @XToOne(OneToOne, ManyToOne)관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.   

<br>

* ### _컬렉션은 필드에서 초기화 하자_
  * 컬렉션은 필드에서 바로 초기화 하는 것이 안전하다.
  * `null`문제에서 안전하다.
  * 하이버네이트는 엔티티를 영속화 할 떄, 컬렉션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다.
  * `getOrders()`처럼 임의의 메서드에서 컬렉션을 잘못 생성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다.
  * 필드 레벨에서 생성하는 것이 가장 안전하고 코드도 간결하다.
    ```Java
    Member member = new Member();
    System.out.println(member.getOrders().getClass());
    entityManger.persist(team);
    System.out.prtinln(member.getOrders().getClass());
    ```
  * 출력 결과
    >class java.unit.ArrayList   
     class org.hibernate.collection.internal.PersistentBag   

<br>
<br>
<br>

# _애플리케이션 구현 준비_

<br>

## _애플리케이션 아키텍처_
![](img/img337.png)


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






