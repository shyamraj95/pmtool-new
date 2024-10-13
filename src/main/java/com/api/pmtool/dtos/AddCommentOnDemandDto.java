package com.api.pmtool.dtos;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AddCommentOnDemandDto {
    private UUID demandId;
    private String comment;
    private List<MultipartFile> multipartFiles;
}

