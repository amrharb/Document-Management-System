package net.atos.dms.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);

    boolean existsByNid(String nid);

    Optional<User> findByEmail(String email);

    Optional<User> findByNid(String nid);
}
