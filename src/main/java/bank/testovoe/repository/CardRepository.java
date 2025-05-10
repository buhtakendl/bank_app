package bank.testovoe.repository;

import bank.testovoe.model.Card;
import bank.testovoe.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {

    Page<Card> findAll(Specification<Card> spec, Pageable pageable);

    List<Card> findByOwner(User owner);

    Optional<Card> findByIdAndOwner(Long id, User owner);

    Optional<Card> findByEncryptedCardNumberAndOwner(String encryptedCardNumber, User owner);

    Optional<Card> findByEncryptedCardNumber(String encryptedCardNumber);

    boolean existsByEncryptedCardNumber(String encryptedCardNumber);

}
