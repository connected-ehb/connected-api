package com.ehb.connected.domain.impl.projects.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ResearcherProjectDetailsDto extends ProjectDetailsDto {
    private String courseName;
    private String assignmentName;



    public ResearcherProjectDetailsDto(ProjectDetailsDto base) {
        this.setId(base.getId());
        this.setGid(base.getGid());
        this.setTitle(base.getTitle());
        this.setDescription(base.getDescription());
        this.setShortDescription(base.getShortDescription());
        this.setStatus(base.getStatus());
        this.setRepositoryUrl(base.getRepositoryUrl());
        this.setBoardUrl(base.getBoardUrl());
        this.setBackgroundImage(base.getBackgroundImage());
        this.setTeamSize(base.getTeamSize());
        this.setAssignmentId(base.getAssignmentId());
        this.setTags(base.getTags());
        this.setCreatedBy(base.getCreatedBy());
        this.setProductOwner(base.getProductOwner());
        this.setMembers(base.getMembers());
    }
}