package net.atos.dms.folder;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FolderRepository extends MongoRepository<Folder, String> {
    List<Folder> findByWorkspaceIdAndParentIdAndDeletedFalse(String workspaceId, String parentId);

    List<Folder> findByAncestorsContainsAndDeletedFalse(String folderId);
}
