package com.example.mydemo.data.model;


import com.example.mydemo.data.entity.NoteEntity;
import com.example.mydemo.data.entity.TagEntity;

public class NoteModel {

    private Integer id;

    private String title;

    private String content;

    private Long createTime;

    private Long updateTime;

    private Boolean isFavorite;

    private String tagName;

    private String tagColor;

    public NoteModel(Integer id, String title, String content, Long createTime, Long updateTime, Boolean isFavorite, String tagName, String tagColor) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.isFavorite = isFavorite;
        this.tagName = tagName;
        this.tagColor = tagColor;
    }

    public NoteModel() {
    }

    public NoteModel(NoteEntity noteEntity, TagEntity tagEntity) {
        this.id = noteEntity.getId();
        this.title = noteEntity.getTitle();
        this.content = noteEntity.getContent();
        this.createTime = noteEntity.getCreateTime();
        this.updateTime = noteEntity.getUpdateTime();
        this.isFavorite = noteEntity.getIsFavorite();
        if (tagEntity != null) {
            this.tagName = tagEntity.getName();
            this.tagColor = tagEntity.getColor();
        }
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

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagColor() {
        return tagColor;
    }

    public void setTagColor(String tagColor) {
        this.tagColor = tagColor;
    }

    @Override
    public String toString() {
        return "model{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isFavorite=" + isFavorite +
                ", tagName='" + tagName + '\'' +
                ", tagColor='" + tagColor + '\'' +
                '}';
    }
}
