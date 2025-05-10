package bank.testovoe.config;

import bank.testovoe.model.Card;
import bank.testovoe.model.CardStatus;
import bank.testovoe.model.User;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class CardSpecification {
    public static Specification<Card> ownerIs(User user) {
        return (root, query, cb) -> cb.equal(root.get("owner"), user);
    }

    public static Specification<Card> statusIs(CardStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Card> balanceGreaterThanOrEqual(BigDecimal min) {
        return (root, query, cb) -> cb.ge(root.get("balance"), min);
    }

    public static Specification<Card> balanceLessThanOrEqual(BigDecimal max) {
        return (root, query, cb) -> cb.le(root.get("balance"), max);
    }
}

