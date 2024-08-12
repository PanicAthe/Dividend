package panicathe.dividend.persist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import panicathe.dividend.model.MemberEntity;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    @Query("SELECT m FROM MEMBER m JOIN FETCH m.roles WHERE m.username = :username")
    Optional<MemberEntity> findByUsernameWithRoles(@Param("username") String username);

    Optional<MemberEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
