# _Builder Pattern_

<br>

### _빌더 패턴 구현 방법_   
1. 빌더 클래스는 `Static Nested Class로 생성`한다.
   * 관례적으로 생성하고자 하는 `클래스 이름 뒤에 Builder를 붙인다`.
2. 빌더 클래스의 `생성자는 public`으로 하며, `필수 값은 생성자의 파라미터로 받는다`. 
3. Optional한 값들에 대해서는 각각의 속성마다 메소드로 제공하며. 이때 `리턴 값이 빌더 객체 자신`이어야 한다.
4. 빌더 클래스 내에 `build()` 메서드를 정의하여 클라이언트 프로그램에게 최종 생성된 결과물을 제공한다.

    ```Java
    @Getter
    public class Member {

        private Long id;
        private String username;
        private Integet age;

        private Member(MemberBuilder builder) {
            this.id = builder.id;
            this.username = builder.username;
            this.age = builder.age;
        }
    } 

    //Nested Builder Class
    public static class MemberBuilder {

        private Long id;
        private String username;
        private Integer age;

        //필수 값들은 생성자의 파라미터로
        public MemberBuilder (Long id) {
            this.id = id;
        }

        public MemberBuilder setUsername(String age) {
            this.username = username;
            return this;
        }

        public MemberBuilder setAge(Integer age) {
            this.age = age;
            return age;
        }

        public Member bulid() {
            return new Member(this);
        }

    }    
    ```
* MemberBuilder 클래스를 보면 id값은 필수 값 이므로 생성자의 파라미터를 통해 받고, 나머지 필드 값들은 각각의 속성마다 메서드를 생성했다.
  
<br>

### _빌드 패턴을 통해 얻을 수 있는 장점_
1. 경우에 따라 필요 없는 파라미터들에 대해 일일이 null 값을 넘겨주지 않아도 된다.
2. 객체를 생성할 때 파라미터의 순서를 신경쓰지 않아도 된다.
* 추가로 Member 클래스를 보면 public 생성자가 없는 것을 확인할 수 있다.
  * Member 객체를 생성하기 위해서는 오직 MemberBuilder 클래스를 통해서만 가능하다.

<br>

### Member 객체 생성
* MemberBuilder를 통해 Member객체를 생성할때 Optional한 username, age에 대해서는 필요 없는 값이면 메서드를 사용하지 않으면 된다.
* setUsername, setAge의 메서드 순서가 바뀌어도 상관없다.   


    ```Java
    //new Member(1L, "Jeon", 10);

    Member member = new MemberBuilder(1L)
            .setUsername("Jeon")
            .setAge(29)
            .build();
    ```
>빌더 패턴은 객체를 생성하는데 장점이 있지만 위와같은 추가적인 많은 코드가 필요하다.   
위의 예시는 필드가 3개뿐이지만 만약 필드가 많아지면 그만큼 코드의 양도 증가하게 되고 객체가 Member뿐만 아니라 여러개의 객체가 있다고 생각하면 필요한 코드의 양은 엄청나게 늘어나는 단점이 있다.   

<br>

### _단점 해경을 의한 Lombok의 @Bulider 애노테이션 사용_   
* `@Builder` 애노테이션을 사용하면 기존 코드의 Nested Builder Class를 생성한것과 똑같은 효과를 갖는다.
    ```Java
    @Builder
    @Getter
    public class Member {

        private Long id;
        private String username;
        private Integer age;
    }
    ```

<br>

### _Member 객체 생성 - @Builder 사용_
* `@Builder`는 `builder`라는 클래스 명을 사용하며 메서드의 이름은 기존 필드명을 그래도 사용한다.
    ```Java
    Member member = Member.builder()
            .id(1L)
            .username("Jeon")
            .age(29)
            .build();
    ```

<br>

### _@Builder 애노테이션을 class 레벨에서 사용하는 것을 권장하지 않는다_
* `@Builder`를 Class 레벨에 적용시키면 생성자의 접근 레벨이 default
  * 동일한 패키지 내에서 해당 생성자를 호출할 수 있는 문제
* 모든 멤버 필드에 대한 매개변수를 받는 생성자를 만든다.
  * `@AllArgsConstructor`와 같은 효과 발생
  * Member 객체에서 id값이 데이터베이스 PK 생성전략에 의존하고 있다고 가정한다면 생성할 때 id값을 넘겨받지 않아야 한다.
  * Class 레벨에 @Builder를 적용하면 객체 생성에 제한을 두기 어렵다.

<br>

### _private 생성자를 구현해서 @Builder를 지정한다_   
* 생성자에 @builder 지정
    ```Java
    @Getter
    public class Member {

        private Long id;
        private String username;
        private Integer age;

        @Builder
        private Member(String username, Integer age) {
            this.username = username;
            this.age = age;
        }
    }
    ```

<br>

### _Member 객체 생성_
* `@Builder` 생성자가 username, age 파라미터만 갖고 있으므로, Member 객체를 생성할 때 id값을 넘겨받지 못하게 제한할 수 있다.
    ```Java
    Member member = Member.bulider()
            .username("Jeon")
            .age(29)
            .build();
    ```

<br>

### _@Builder 기능_
* 코드 작성
    ```Java
    class CustomTest {

        @Test
        void builderTest() {
            Custom custom = Custom.builder()
                    .name("Custom")
                    .age(20)
                    .time(LocalDateTime.now())
                    .check(true)
                    .jobs(Arrays.asList("직업"))
                    .gender(Gender.MAN)
                    .build();

            System.out.println("custom = " + custom);
            assertThat(custom.getName()).isEqualTo("Custom");
            assertThat(custom.getAge()).isEqualTo(20);
            assertThat(custom.getCheck()).isEqualTo(true);
        }
    }
    ```
    ```
    custom = Custom(name=Custom, age=20, time=2022-02-13T22:25:34.572296, check=true, jobs=[직업], gender=MAN)
    ```

<br>

### _Builder.Default_
* 빌더는 개발자가 좀 더 능독적이게 객체를 생성할 수 있게 도와준다.
    ```Java
    class CustomTest {

        @Test
        void builderTest() {
            Custom custom = Custom.builder()
                    .age(20)
                    .check(true)
                    .jobs(Arrays.asList("직업"))
                    .build();

            System.out.println("custom = " + custom);
            assertThat(custom.getCheck()).isEqualTo(true);
        }
    }
    ```

* 위의 코드를 보면 처음 Custom 객체를 생성할때와 달리 일부 필드를 제외한고 생성하였다.
    ```
    custom = Custom(name=null, age=20, time=null, check=true, jobs=[직업], gender=null)
    ```
* 제외된 필드에는 null값이 들어가게 된다.
* 즉, `Builder는 값을 설정하지 않으면 자동으로 null을 채워준다.`
  * Integer나 Boolean과 같이 Wrapper 타입으로 선언하면 null이 채워지며, int, boolean 같은 Primitive 타입으로 선언한다면 int는 0을 boolea은 false를 기본값으로 채워준다.
* 사용자가 직접 기본 값을 설정해주고 싶다면 `@Builder.Default`를 사용하여 기본 값을 정해줄 수 있다.
  ```Java
  @Builder
  @Getter
  @ToString
  public class Custom {

      private String name;
      @Builder.Default
      private Integer age = 0;

      @Builder.Default
      private LocalDateTime time = LocalDateTime.now();
      private List<String> jobs;

      @Budiler.Default
      private Gender gender = Gender.MAN;
  }
  ``` 
* `@Builder.Default`를 사용하는 방법은 필드에 애노테이션을 추가한 뒤 필드의 기본값을 직접 지정해주면 된다.

<br>

### _객체 생성 - Default적용_
* 테스트 코드 
    ```Java
    @Test
    void builderDefaultTest() {

        Custom custom = Custom.builder() {
            .name("Custom")
            .jobs(Array.asList("직업"))
            .build();
        }

        System.out.pritln("Custom = " + custom);
    }
    ```
    ```
    custom = Custom(name=Custom, age=0, time=2021-10-31T23:59:39.079739800, check=false, jobs=[직업], gender=MAN)
    ```

<br>

### _Builder Singular_
* `@Singular`를 통해서 컬렉션을 하번에 하나씩 값 목록을 작성할 수 있다.
    ```Java
    @Singular
    private List<String> jobs;
    ```
    ```Java
    @Test
    void builderSingular() {

        Custom custom = Custom.builder() {
                .name("Custom")
                .job("직업1")
                .job("직업2")
                .build();
        }

        System.out.pritln("Custom = " + custom);
    }
    ```
    ```
    custom = Custom(name=Custom, age=0, time=2021-11-01T01:04:05.271057900, check=false, jobs=[직업1, 직업2], gender=MAN)
    ```
* 여기서는 `@Singular`가 java.util.List로 작업을 했지만 Set, Map 자료주고도 가능하다.
* 주의할점은 `@Singular를 사용하면 인수가 단수형식으로 전달`된다는 점이다.
* 위의경우 jobs이므로 job의 형태로 전달된다.
* 만약 단수형이 애매한경우 직접 이름을 지정해줄 수도 있다.
  * `@Singular("job")`


