package hellojpa.relationmapping;

import javax.persistence.*;

@Entity
public class Locker {

    @Id
    @Column(name = "locker_id")
    @GeneratedValue
    private Long id;

    @OneToOne(mappedBy = "locker")
    private Member member;

    private String name;

    public Locker() {
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

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
