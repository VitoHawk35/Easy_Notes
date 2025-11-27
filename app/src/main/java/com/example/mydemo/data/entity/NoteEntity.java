package com.example.mydemo.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "note")
public class NoteEntity {
    @PrimaryKey(autoGenerate = true)
    private Integer id;
    @ColumnInfo
    private String title;
    @ColumnInfo
    private String content;
    @ColumnInfo(name = "tag_id")
    private Integer tagId;
    @ColumnInfo(name = "create_time")
    private Long createTime;
    @ColumnInfo(name = "update_time")
    private Long updateTime;
    @ColumnInfo(name = "is_favorite")
    private Boolean isFavorite;

    public NoteEntity() {
    }

    @Ignore
    public NoteEntity(String title, String content, Integer tagId, Long createTime, Long updateTime, Boolean isFavorite) {
        this.title = title;
        this.content = content;
        this.tagId = tagId;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.isFavorite = isFavorite;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean favorite) {
        isFavorite = favorite;
    }
}
