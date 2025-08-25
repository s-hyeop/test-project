package com.example.test_project.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoPatchRequest {

    private Boolean completed;

    private Integer sequence;

}
