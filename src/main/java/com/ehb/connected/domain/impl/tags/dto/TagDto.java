package com.ehb.connected.domain.impl.tags.dto;

public class TagDto {

    private Long id;
    private String name;

    public TagDto() {
    }

    public TagDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
