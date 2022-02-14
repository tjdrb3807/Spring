package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
class OrderServiceTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void 상품주문() throws Exception {
        //given
        Member member = createMember("회원1");

        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER, getOrder.getStatus());
        assertEquals(1, getOrder.getOrderItems().size());
        assertEquals(10000 * orderCount, getOrder.getTotalPrice());
        assertEquals(8, book.getStockQuantity());
    }

//    @Test
//    void 상품주문_재고수량초과() throws Exception {
//        //given
//        Member member = createMember("회원1");
//        Item item = createBook("시골 JPA", 10000, 10);
//
//        int orderCount = 11;
//
//        //when
//        orderService.order(member.getId(), item.getId(), orderCount);
//
//        //then
//        fail("재고 수량 부족 예외가 발생해야 한다.");
//    }

    @Test
    void 주문취소() throws Exception {
        //given
        Member member = createMember("회원1");
        Book item = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL, getOrder.getStatus());
        assertEquals(10, item.getStockQuantity());
    }

    private Book createBook(String name, int pri, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(pri);
        book.setStockQuantity(stockQuantity);

        entityManager.persist(book);
        return book;
    }

    private Member createMember(String name) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(new Address("서울", "강가", "123-123"));

        entityManager.persist(member);
        return member;
    }
}