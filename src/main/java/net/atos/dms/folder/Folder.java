package net.atos.dms.folder;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "folders")
public class Folder {
    @Id
    private String id;
    @Indexed
    private String workspaceId;
    private String ownerEmail;
    private String name;
    private String parentId;
    @Indexed
    private List<String> ancestors = new ArrayList<>();
    private boolean deleted = false;
    private Instant createdAt = Instant.now();
}
