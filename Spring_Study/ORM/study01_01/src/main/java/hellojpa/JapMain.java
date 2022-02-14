package hellojpa;

import hellojpa.cascade.Child;
import hellojpa.cascade.Parent;
import hellojpa.relationmapping.Member;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class JapMain {

    public static void main(String[] args) {

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("hello");

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

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
        } catch (Exception e) {
            transaction.rollback();
            System.out.println("e = " + e);
        } finally {
            entityManager.close();
        }
        entityManagerFactory.close();
    }
}
