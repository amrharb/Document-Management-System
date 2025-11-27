package net.atos.dms.workspace;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkspaceRepository extends MongoRepository<Workspace, String> {
    List<Workspace> findByOwnerEmail(String ownerEmail);
}
