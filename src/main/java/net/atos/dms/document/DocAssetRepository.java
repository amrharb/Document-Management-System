package net.atos.dms.document;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocAssetRepository extends MongoRepository<DocAsset, String> {
    Page<DocAsset> findByWorkspaceIdAndFolderIdAndDeleted(String workspaceId, String folderId, boolean deleted, Pageable p);

    Page<DocAsset> findByWorkspaceIdAndFolderId(String workspaceId, String folderId, Pageable p);

    Page<DocAsset> findByWorkspaceIdAndFolderIdAndDeletedAndNameRegex(String workspaceId, String folderId, boolean deleted, String regex, Pageable p);

    Page<DocAsset> findByWorkspaceIdAndFolderIdAndNameRegex(String workspaceId, String folderId, String regex, Pageable p);

    List<DocAsset> findByTagsIn(List<String> tags);

    List<DocAsset> findByNgramsIn(List<String> grams);

    long countByAncestorsContainsAndDeletedFalse(String folderId);
}
