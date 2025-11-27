package net.atos.dms.workspace;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

@Document(collection = "workspaces")
public class Workspace {

    @Id
    private String id;

    @Email
    @NotNull
    private String ownerEmail;

    private String name;

    private Instant createdAt;

    public Workspace() {
        this.createdAt = Instant.now();
    }

    public Workspace(String id, String ownerEmail, String name, Instant createdAt) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.name = name;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        if (createdAt == null) createdAt = Instant.now();
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Workspace workspace = (Workspace) o;
        return Objects.equals(id, workspace.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Workspace{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", createdAt=" + createdAt + '}';
    }
}
