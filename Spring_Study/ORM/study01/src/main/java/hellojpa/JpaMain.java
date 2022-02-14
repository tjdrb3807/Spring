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

            //homeCity --> newCity
//            findMember.getHomeAddress().setCity("newCity"); //값 타입은 사이드 이펙트가 발생할 수 있으므로 이런식으로 코드를 작성하면 안된다

            Address a = findMember.getHomeAddress();
            findMember.setHomeAddress(new Address("newCity", a.getStreet(), a.getZipcode()));  //값타입은 어드레스 인스턴스 자체를 통체로 갈아 끼워야 한다

            //값타입 컬렉션 업데이트
            //치킨 --> 한식
            //스트링 자체가 값타입이므로 스트링은 통체로 갈아 끼워야 한다 업데이트라는 것 자체를 하면 안된다(할 수도 없고,,,)
            findMember.getFavoriteFood().remove("치킨");
            findMember.getFavoriteFood().add(("한식"));

            //값타입이기떄문에 address 인스턴스를 통으로 갈아 끼워야 한다
            //컬렉션마다 다르긴 하지만 기본적으로 컬렉션들을 대상을 찾을 떄 equals를 사용한다
            //따라서 equals, hashcode 가 제대로 구현되어었지 않으면 망한다(기본값을 쓰자)
            findMember.getAddressHistory().remove(new Address("old1", "street", "10000"));
            findMember.getAddressHistory().add(new Address("newCity1", "street", "10000"));


            System.out.println("-=======================");


            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            System.out.println("e = " + e);
        } finally {
            em.close();
        }
        emf.close();
    }
}
