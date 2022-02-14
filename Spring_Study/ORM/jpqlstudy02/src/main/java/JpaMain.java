import jpql.*;

import javax.persistence.*;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("hello");

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        try {
            Team singleResult = entityManager.createQuery("select t from Team t where t.name = :teamName", Team.class)
                    .setParameter("teamName", "teamB")
                    .getSingleResult();

            Member member = new Member();
            member.setUsername("Kim");
            member.setAge(29);
            member.setTeam(singleResult);
            member.setType(MemberType.ADMIN);

            String query = "select m.username, 'hello', true from Member m where m.type = jpql.MemberType.ADMIN";

            List<Object[]> singleResult1 = entityManager.createQuery(query)
                    .getResultList();





            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        entityManagerFactory.close();
    }
}
