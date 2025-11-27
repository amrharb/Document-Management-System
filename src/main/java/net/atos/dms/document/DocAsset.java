package net.atos.dms.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "doc_assets")
public class DocAsset {
    @Id
    private String id;
    @Indexed
    private String workspaceId;
    @Indexed
    private String folderId;
    @Indexed
    private String ownerEmail;
    private String name;
    private String contentType;
    private long size;
    private String fileKey;
    private boolean deleted = false;
    private int version = 1;
    private List<String> tags = new ArrayList<>();
    @Indexed
    private List<String> ngrams = new ArrayList<>();
    private List<String> ancestors = new ArrayList<>();
    private Instant createdAt = Instant.now();
    private String createdBy;
}
