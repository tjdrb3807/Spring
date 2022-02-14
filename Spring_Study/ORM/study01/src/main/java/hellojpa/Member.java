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

    public Member() {
    }

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

