# _API 개발 기본_

<br>

## _회원 등록 API_

<br>

* ### _V1 엔티티를 Request Body에 직접 매핑_
  * 문제점
    * 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
    * 엔티티에 API 검증을 위한 로직이 들어간다(@NotEmpty 등...)
    * 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어렵다.
    * 엔티티가 변경되면 API 스펙이 변한다.   

    ```Java
    package jpabook.jpashop.api;

    @RestController
    @RequiredArgsConstructor
    public class MemberApiController {

        private final MemberService memberService;

        /*
        * 등록 V1: 요청 값으로 Member 엔티티를 직접 받는다.
        * 문제점
        * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
        * - 엔티티에 API 검증을 위한 로직이 들어간다. (@NotEmpty 등...)
        * - 실무에서는 회원 엔티티를 위한 API가 다양하게 만들어 지는데, 한 엔티티에 각각의 API를 위한 모든 요청 요구사항을 담기는 어렵다.
        * - 엔티티가 변경되면 API 스펙이 변한다.
        * 결론
        * - API 요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.
        */

        @PostMapping("/api/v1/members")
        public CreateMemberResponse saveMember(@RequestBody @Valid Member member) {
            Long id = memberService.join(member);
            
            return new CreateMemberResponse(id);
        }

        @Data
        static class CreateMemberResponse {

            private Long id;

            public CreateMemberResponse(Long id) {
                this.id = id;
            }
        }
    }
    ```
  * 결론 
    * API요청 스펙에 맞추어 별도의 DTO를 파라미터로 받는다.

<br>

* ### _V2 엔티티 대신에 DTO를 RequestBody에 매핑_
    ```Java
    package josbook.jpashop.api;

    @RestController
    @RequiredArgsConstructor
    public class MemberApiService {

        @PostMapping("/api/v2/members")
        public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest requset) {
            Member member = new Member();
            member.setName(request.getName());

            Long savedId = memberService.join(member);

            return new CreateMemberResponse(savedId);
        }

        @Data
        static class CreateMemberRequest {

            @NotEmpty
            private String name;
        }
    }
    ```
  * `CreateMemberRequest`를 `Member`엔티티 대신에 RequestBody와 매핑한다.
  * 엔티티와 프레젠테이션 계층을 위한 로직을 분리할 수 있다.
  * 엔티티와 API 스펙을 명확하게 분리할 수 있다.
  * 엔티티가 변해도 API 스펙이 변하지 않는다.
  * 참고: `실무에서는 엔티티를 API 스펙에 노출하면 안된다!!`
  
<br>

## _회원 수정 API_

<br>

* ### _회원 수정 API_ 
    ```Java
    package jpabook.jpashop.api;

    @Service
    @RequiredArgsConstructor
    public class MemberApiService {

        @PutMapping("/api/v2/members/{id}")
        public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid UpdateMemberRequest request) {
            memberService.update(id, request.getName());
            Member updateMember = memberService.find(id);

            return new UpdateMemberResponse(updateMember.getId(), updateMember.getName());
        }

        @Date
        @AllArgsConstructor
        static class UpdateMemberResponse {

            private Long id;
            private String name;
        }

        @Date
        static class UpdateMemberRequest {

            private String name;
        }
    }
    ```
  * 회원 수정도 DTO를 요청 파라미터에 매핑
    ```Java
    package jpabook.jpashop.service;

    @Service
    @Transactional(readOnly = true)
    @RequiredArgsConstructor
    public class MemberService {
        
        private final MemberRespository memberRepository;

        @Transactional
        public void update(Long id, String name) {
            Member member = memberRepository.find(id);
            member.setName(name);
        }
    }
    ```
  * 변경 감지를 사용해서 테이터를 수정
  * 오류 정정
    >회원 수정 API `updateMemberV2`은 회원 정보를 부분 업데이트 한다.    
    여기서 PUT 방식을 사용했는데, PUT은 전체 업데이트를 할 때 사용하는 것이 맞다.   
    부분 업데이트를 하려면 PATCH를 사용하거나 POST를 사용하는 것이 REST 스타일에 맞다.   

<br>

## _회원 조회 API_

<br>

* ### _회원 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출_ 
    ```Java
    package jpabook.jpashop.api;

    /*
    * 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출한다.
    * 문제점
    * - 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
    * - 기본적으로 엔티티의 모든 값이 노출된다.
    * - 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
    * - 실무에는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
    * - 엔티티가 변경되면 API 스펙이 변한다.
    * - 추가로 컬렉션을 직접 반환하면 향후 API 스펙을 변경하기 어렵다.(별도의 Result 클래스 생성으로 해결)
    * 결론
    * - API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
    */

    @RestController
    @RequiredArgsConstructor
    public class MemberService {

        private final MemberService memberService;

        //조회 V1: 안 좋은 버전, 모든 엔티티가 노출, @JsonIgnore -> 이건 정말 최악, API가 이거 하나인가! 화면에 종속적이지 마라!
        @GetMapping("/api/v1/members")
        public List<Member> memberV1() {
            return memberService.findMembers();
        }
    }
    ```
  * 조회 V1: 응답 값으로 엔티티를 직접 외부에 노출
    * 문제점
      * 엔티티에 프레젠테이션 계층을 위한 로직이 추가된다.
      * 기본적으로 엔티티의 모든 값이 노출된다.
      * 응답 스펙을 맞추기 위해 로직이 추가된다.(@JsonIgnord, 별도의 뷰 로직 등...)
      * 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
      * 엔티티가 변경되면 API 스펙이 변한다.
      * 추가로 컬렉션을 직접 반환하면 향후 API 스펙을 변경하기 어련다.
        * 별도의 Result 클래스 생성으로 해결
    * 결론
      * API 응답 스펙에 맞추어 별도의 DTO를 반환한다.
  * 참고: 엔티티를 외부에 노출하지 마시오!
    >실무에서는 `member`엔티티의 데이터가 필요한 API가 계속 증가하게 된다.   
    어떤 API는 `name`필드가 필요하지만, 어떤 API는 `name`필드가 필요없을 수 있다.   
    결론적으로 엔티티 대신에 API 스펙에 맞는 별도의 DTO를 노출해야 한다.

<br>

* ### _회원 조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO사용_
    ```Java
    package jpabook.jpashop.api;

    /*
    * 조회 V2: 응답 값으로 엔티티가 아닌 별도의 DTO를 반환한다.
    */
    @GetMapping("/api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findAll();
        List<MemberDto> collect = findMembers.stream()
            .map(m -> new MemberDto(m.getName()));
            .collect(Collectors.toList());
        
        return new Result(collect);
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {

        private String name;
    }

    @Data
    @AllArgsConstructor
    static class Result<t> {
        
        private T data;
    }
    ```
  * 엔티티를 DTO로 변환해서 반환한다.
  * 엔티티가 변해도 API 스펙이 변경되지 않는다.
  * 추가로 `Result`클래스로 컬렉션을 감싸서 향후 필요한 필드를 추가할 수 있다.

<br>
<br>
<br>

# _API 개발 고급 - 준비_

<br>

## _조회용 샘플 데이터 입력_
* ### _API 개발 고급 설명을 위해 샘플 데이터를 입력하자._
  * userA
    * JPA1 BOOK
    * JAP2 BOOK
  * userB
    * SPRING1 BOOK
    * SPIRNG2 BOOK
    ```Java
    package japbook.jpashop;

    @Component
    @RequiredArgsConstructor
    public class InitDb {
        
        private final InitService initService;

        @PostConstruct
        public void init() {
            initService.dbInit1();
            initService.dbInit2();
        }

        @Conponent
        @Transactional
        @RequiredArgsConstructor
        static class InitService {

            private final EntityManager entityManager;

            public void dbInit1() {
                Member member = new Member("userA", "서울", "1", "1111");
                entityManager.persist(member);

                Book book1 = new Book("JAP1 BOOK", 10000, 10);
                entityManager.persist(book1);
                Book book2 = new Book("JAP2 BOOK", 20000, 100);
                entityManager.persist(book2);

                OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
                OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 2);

                Order order = Order.createOrder(member, createDelivery(member)), orderItem1, orderItem2);
                entityManger.persist(order);
            }

            public void dbInit2() {
                Member member = new Member("userB", "진주", "2", "2222");
                entityManager.persist(member);

                Book book1 = createBook("SPRING1 BOOK", 20000, 200);
                entityManger.presist(book1);
                Book book2 = createBook("SPRING2 BOOK", 40000, 300);
                entityManger.presist(book2);

                Delivery delivery = createDelivery(member);

                OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
                OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

                Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
                entityManger.persist(order);
            }

            private Member createMember(String name, String city, String street, String zipcode) {
                Member member = new Member();
                member.setName(name);
                member.setAddress(new Address(city, street, zipcode));

                return member;
            }

            private Book createBook(String name, int price, int stockQuantity) {
                Book book = new Book();
                book.setName(name);
                book.setPrice(price);
                book.setStockQuantity(stockQuantity);

                return book;
            }

            private Delivery createDelivery(Member member) {
                Delivery delivery = new Delivery();
                delivery.setAddress(member.getAddress());

                return delivery;
            }
        }
    }
    ```
  * 참고
    >주문 내역 화면에서는 회원당 주문 내열을 하나만 출력했으므로 하나만 노출된다.    

<br>
<br>
<br>

# _API 개발 고급 - 지연 로딩과 조회 성능 최적화_    
주문 + 배송정보 + 회원을 조회하는 API를 만들자.   
지연 로딩 때문에 발생하는 성능 문제를 단계적으로 해결해보자.   
 
<br>

## _간단한 주문 조회 V1: 엔티티를 직접 노출_
* ### _OrderSimpleApiController_
    ```Java
    package jpabook.jpashop.api;

    @RestController
    @RequiredArgsConstructor
    public class OrderSimpleController {

        private final OrderRepository orderRepositroy;

        /*
        * V1. 엔티티 직접 노출
        * - Hibernate5Module 모듈 등록, LAZY = null 처리
        * - 양방향 관계 문제 발생 -> @JsonIgnore
        */
        @GetMapptin("/api/v1/simple-orders")
        public List<Order> ordersV1() {
            List<Order> all = orderRepository.findAllByString(new OrderSearch());
            for (Order order : all) {
                order.getMember().getName();  //LAZY 강제 초기화
                order.getDelivery.getAddress(); //LAZY 강제 초기화
            }
            return all;
        }
    }
    ``` 
  * 엔티티를 직접 노출하는 것을 좋지 않다.
  * `order` -> `member`와 `ordre` -> `address`는 지연로딩이다.
    * 실제 엔티티 대신에 프록시 존재
  * jackson 라이브러리는 기본적으로 이 프록시 객체를 json으로 어떻게 생성해야 하는지 모른다 -> 예외 발생
  * `Hibernate5Module`을 스프링 빈으로 등록하면 해결(스프링 부트 사용중)

<br>

* ### _Hibernate5Module 등록_
  * `JpashopApplication`에 다음 코드 추가
    ```Java
    @Bean
    Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }
    ```
  * 기본적으로 초기화 된 프록시 객체만 노출, 초기화 되지 않은 프록시 객체는 노출하지 않느다.
  * 참고:`build.gradle`에 다음 라이브러리 추가
    >`implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'` 
  * 다음과 같이 설정하면 강제로 지연 로딩 가능
    ```Java
    @Bean
    Hibernate5Module hibernate5Module() {
        Hibernate5Module hibernate5Module = new Hibernate5Module();

        //강제 지연 로딩 설정
        hibernate5Modeult.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);

        return hibernate5Module;
    }
    ```
  * 이 옵션을 키면 `orders -> member`, `member -> orders` 양방향 연관관계를 계속 로딩하게 된다.
  * 따라서 `@JsonIgnore`옵션을 한곳에 주어야 한다.
  * 주의
    >엔티티를 직접 노출할 때는 양방향 연관관계가 걸린 곳은 꼭! 한 곳을 `@JsonIgnore`처리 해야한다.   
    안그러면 양쪽을 서로 호출하여 무한 루프가 걸린다.   
  * 참고
    >앞에서 계속 강조했듯이 정말 간단한 애플리케이션이 아니라면 엔티티를 API응답으로 노출하는 것은 좋지 않다.    
    따라서 `Hibernate5Module`를 사용하기 보다는 DTO로 변환해서 반환하는 것이 더 좋은 방법이다.   
  * 주의
    >지연 로딩(LAZY)을 피하기 위해 즉시 로딩(EAGER)으로 설정하면 안된다!   
    즉시 로딩 때문에 연관관계가 필요 없는 경우에도 데이터를 항향 조회해서 성능 문제가 발생할 수 있다.   
    즉시 로딩으로 설정하면 성능 튜닝이 매우 어려워 진다.      
     
<br>

## _간단한 주문 조회 V2: 엔티티를 DTO로 변환_
* ### _OrderSimpleApiController - 추기_
    ```Java
    package jpabook.jpashop.api;

    @RestController
    @RequiredArgsConstructor
    public class OrderSimpleController {

        private final OrderReapository orderRepository;

        /*
        * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
        * - 단점: 지연로딩으로 쿼리 N번 호출
        */
        @GetMapptin("/api/v2/simple-orders")
        public List<SimpleOrderDte> ordersV2() {
            List<Order> orders = orderRepository.findAllByString(new OrderSearch());
            List<SimpleOrderDte> result = order.stream()
                    .map(o -> new SimpleOrderDto(o));
                    .collect(Collector.toList());
            
            return result;
        }

        @Date
        static class SimpleOrderDto {

            private Long id;
            private String name;
            private LocalDateTime orderDate;
            private OrderStatus status;
            private Address address;

            public SimpleOrderDto(Order order) {
                id = order.getId();
                name = order.getMember().getName();  //Proxy Member Entity Init
                orderDate = order.getOrderDate;
                status = order.getOrderStatus;
                address = order.getDelivery().getAddress();  //Proxy Delivery Entity Init
            }   
        }
    }
    ```
  * 엔티티를 DTO로 변환하는 일반적인 방법이다.
  * 쿼리가 총 1 + N + N번 실행된다.(V1과 쿼리수 결과는 같다)
    * `order`조회 1번(order 조회 결과 수가 N이 된다.)
    * `order -> member` 지연 로딩 조회 N번
    * `order -> delivery`지연 로딩 조회 N번
    * $ex.$ order의 결과가 4개면 최악의 경우 1 + 4 + 4번 샐행된다.
      * 지연로딩은 영속성 컨텍스트에서 조회하므로, 이미 조회된 경우 쿼리를 생량한다.

<br>

## _간단한 주문 조회 V3: 엔티티를 DTO로 변환 - 페치 조인 최적화_
* ### _OrderSimpleApiController - 추가_
    ```Java
    package jpabook.jpashop.api;

    @RestController
    @RequiredArgsConstructor
    public class OrderSimpleApiController {

        private final OrderRespository orderRepository;

        @GetMapping("/api/v3/simple-orders")
        public List<SimpleOrderDto> ordersV3() {
            List<Order> orders = orderRepository.findAllWithMemberDelivery();
            List<SimpleOrderDte> result = orders.stream()
                    .map(o -> new SimpleOrderDto(o))
                    .collect(Collector.toList());
            
            return result;
        }
    }
    ```
<br>

* ### _OrderRepository - 추가 코드_ 
    ```Java
    package jpabook.jpashop.repository;

    @Repository
    @RequiredArgsConstructor
    public class OrderRepository {

        private final EntityManager entityManager;

        public List<Order> findAllWithMemberDelivery() {
            return entityManager.createQuery(
                    "select o from Order o" +
                    " join fetch o.member m" +
                    " join fetch o.delivery d", Order.class)
                .getResultList();
        }
    }
    ```
  * 엔티티를 페치 조인(fetch join)을 사용해서 쿼리 1번에 조회
  * 페치 조인으로 `order -> member`, `order -> delivery`는 이미 조회 된 상태 이므로 지연로딩X

<br>

## _간단한 주문 조회 V4: JPA에서 DTO로 바로 조회_
* ### _OrderSimpleApiController - 추가_
    ```Java
    package jpabook.jpashop.api;

    @RestController
    @RequiredArgsConstructor
    public class OrderSimpleApiController {

        private final OrderRepository orderRepository;
        private final OrderSimpleQueryRepository orderSimpleQueryRepository;

        /*
        * V4. JPA에서 DTO로 바로 조회
        * - 쿼리 1번 호출
        * - select 절에서 원하는 데이터만 선택해서 조회
        */
        @GetMapping("/api/v4/simple-orders")
        public List<OrderSimpleQueryDto> ordersV4() {
            return orderSimpleQueryRespository.findOrderDtos();
        }
    }
    ```

<br>

* ### -OrderSimpleQueryRepository 조회 전용 리포지토리-
    ```Java
    package jpabook.jpashop.respository.order.simplequery;

    @Respository
    @RequiredArgsConstructor
    public class OrderSimpleQueryRespository {

        private final EntityManager entityManager;

        public List<OrderSimpleQueryDto> findOrderDtes() {
            return entityManager.createQuery(
                    "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                    " from Order o" +
                    " join o.member m" +
                    " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
        }
    }
    ```

<br>

* ### _OrderSimpleQueryDto 리포지토리에서 DTO 직접 조회_
    ```Java
    package jpabook.jpashop.repository.order.simplequery;

    @Data
    @AllArgsConstructor
    public class OrderSimpleQueryDto {

        private Long id;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus status;
        private Address address;
    }
    ```
  * 일반적인 SQL을 사용할 때 처럼 원하는 값을 선택해서 조회
  * `new`명령어를 사용해서 JPQL의 결과를 DTO로 즉시 변환
  * SELECT 절에서 원하는 데이터를 직접 선택하므로 DB -> 애플리케이션 네트웍 용량 최적화(생각보다 미비)
  * 리포지토리 재사용성 떨어짐, API스펙에 맞춘 코드가 리포지토리에 들어가는 단점

<br>

* ### _정리_   
엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다.   
둘중 상황에 따라서 더 나은 방법을 선택하면 된다.   
엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발도 단순해진다.   
따라서 권장하는 방법은 다음과 같다.   
  * 쿼리 방식 선택 권장 순서
    1. 우선 엔티티를 DTO로 변환하는 방법은 선택한다.
    2. 필요하면 페치 조인으로 성능 최적화 한다. -> 대부분 성능 이슈가 해결된다.
    3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
    4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 JDBC Template을 사용해서 SQL을 직접 사용한다.    

<br>
<br>
<br>

# _API 개발 고급 - 컬렉션 조회 최적화_   
주문내역에서 추가로 주문한 상품 정보를 추가로 조회하자.   
Order 기준으로 컬렉션인 `OrderItem`와 `Item`이 필요하다.   
앞의 예제에서는 toOne(OneToOne, ManyToOne)관계만 있었다. 이번에는 컬렉션인 일대다 관계(OneToMany)를 조회하고, 최적화하는 방법을 알아보자.   

<br>

## _주문 조회 V1: 엔티티 직접 노출_
* ### _코드_
    ```Java
    package jpabook.jpashop.api;

    @RestController
    @RequiredArgsConstructor
    public class OrderApiController {

        private final OrderRepository orderRepository;

        /*
        * V1. 엔티티 직접 노출
        * - Hibernate5Module 모듈 등록, LAZY = null 처리
        * - 양방향 관계 문제 발생 -> @JsonIgnore
        */
        @GetMapping("/api/v1/orders")
        public List<Order> orderV1() {
            List<Order> all = orderRepository.findAll();
            for (Order order : all) {
                order.getMember().getName(); //LAZY 강제 초기화
                order.getDelivery().getAddress(); //LAZY 강제 초기화
                List<OrderItem> orderItems = order.getOrderItems();
                orderItems.stream()
                    .forEach(o -> o.getItem().getName()); //LAZY 강제 초기화
            }
            return all;
        }
    }
    ```
  * `orderItem`, `item`관계를 직접 초기화 하면 `Hibernate5Modul`설정에 의해 엔티티를 JSON으로 생성한다.
  * 양방향 연관관계면 무한 루프에 걸리지 않게 한 곳에 `@JsonIgnore`를 추가해야 한다.
  * 엔티티를 직접 노출하므로 좋은 방법은 아니다.

<br>

## _주문 조회 V2: 엔티티를 DTO로 변환_

<br>

## _주문 조회 V3: 엔티티를 DTO로 변환 - 페치 조인 최적화_

<br>

## _주문 조회 V3.1: 엔티티를 DTO로 변환 - 페이징과 한계 돌파_

<br>

## _주문 조회 V4: JPA에서 DTO 직접 조회_

<br>

## _주문 조회 V5: JPA에서 DTO 직접 조회 - 컬렉션 조회 최적화_

<br>

## _주문 조회 V6: JPA에서 DTO 직접 조회, 플렛 데이터 최적화_

<br>

## _API 개발 고급 정리_

<br>
<br>
<br>

# _API 개발 고급 - 실무 필수 최적화_

<br>

## _OSIV와 성능 최적화_
  