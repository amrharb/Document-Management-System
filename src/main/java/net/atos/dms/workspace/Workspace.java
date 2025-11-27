package net.atos.dms.workspace;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;

/**
 * Workspace entity for MongoDB.
 * Minimal edit: mark the getter as WRITE_ONLY so ownerNid is stored but not returned in JSON.
 */
@Document(collection = "workspaces")
public class Workspace {

    @Id
    private String id;

    /**
     * Owner identifier (nid) — will hold the user's email (set in controller from Authentication).
     * Keep field as-is; we'll hide it from JSON by annotating the getter.
     */
    @Email
    @NotNull
    private String ownerNid;

    private String name;

    private Instant createdAt;

    public Workspace() {
        this.createdAt = Instant.now();
    }

    public Workspace(String id, String ownerNid, String name, Instant createdAt) {
        this.id = id;
        this.ownerNid = ownerNid;
        this.name = name;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    // --- getters & setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Hide ownerNid from JSON serialization while keeping it writable.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getOwnerNid() {
        return ownerNid;
    }

    public void setOwnerNid(String ownerNid) {
        this.ownerNid = ownerNid;
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

    // equals/hashCode/toString (ownerNid intentionally omitted from toString to avoid accidental leaks)

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
