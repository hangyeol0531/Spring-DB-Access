package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("start transaction");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("start transaction commit");
        txManager.commit(status);
        log.info("complete transaction commit");
    }

    @Test
    void rollback() {
        log.info("start transaction");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("start transaction rollback");
        txManager.rollback(status);
        log.info("complete transaction rollback");
    }

    @Test
    void double_commit() {
        log.info("start transaction1");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("start transaction1 commit");
        txManager.commit(tx1);

        // 위, 아래 트랜잭션에서 사용한 커넥션의 물리적인 부분은 같지만 사용된 hikari proxy connection 은 다르다.
        log.info("start transaction2");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("start transaction2 commit");
        txManager.commit(tx2);
    }

    @Test
    void double_commit_rollback() {
        log.info("start transaction1");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("start transaction1 commit");
        txManager.commit(tx1);

        log.info("start transaction2");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("start transaction2 rollback");
        txManager.rollback(tx2);
    }

    @Test
    void inner_commit() {
        log.info("outer transaction start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNetTransaction()={}", outer.isNewTransaction());

        log.info("inner transaction start");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("inner.isNetTransaction()={}", inner.isNewTransaction());
        log.info("inner transaction commit");
        txManager.commit(inner);

        log.info("outer transaction commit");
        txManager.commit(outer);
    }

    @Test
    void outer_rollback() {
        log.info("outer transaction start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("inner transaction start");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("inner transaction commit");
        txManager.commit(inner);

        log.info("outer transaction rollback");
        txManager.rollback(outer);
    }

    @Test
    void inner_rollback() {
        log.info("outer transaction start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());

        log.info("inner transaction start");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("inner transaction rollback");
        txManager.rollback(inner);

        log.info("outer transaction commit");

        assertThatThrownBy(() -> txManager.commit(outer))
            .isInstanceOf(UnexpectedRollbackException.class);
    }

    @Test
    void inner_rollback_requires_new() {
        log.info("outer transaction start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionDefinition());
        log.info("outer.isNetTransaction()={}", outer.isNewTransaction());

        log.info("inner transaction start");
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = txManager.getTransaction(definition);
        log.info("inner.isNetTransaction()={}", inner.isNewTransaction()); // true

        log.info("inner transaction rollback");
        txManager.rollback(inner);
        log.info("outer transaction commit");
        txManager.commit(outer);
    }

}
