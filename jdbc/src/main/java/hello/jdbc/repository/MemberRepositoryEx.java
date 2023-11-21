package hello.jdbc.repository;


import hello.jdbc.domain.Member;

public interface MemberRepositoryEx {
    Member save(Member member) throws Exception;

    Member findById(String memberId) throws Exception;

    void update(String memberId, int money) throws Exception;

    void delete(String memberId) throws Exception;
}
