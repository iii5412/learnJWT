package com.portfolio.main.menu.dto.program;

import com.portfolio.main.common.PageDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.List;

@Getter
@Setter
public class SearchProgram extends PageDto {
    private Long id;
    private String programName;
    private String url;

    @Builder
    public SearchProgram(Long id, String programName, String url, Integer page, Integer size, List<String> sortFields, List<Sort.Direction> sort) {
        this.id = id;
        this.programName = programName;
        this.url = url;
        this.setPage(page == null ? 0 : page);
        this.setSize(size == null ? 0 : size);

        if (sortFields != null && !sortFields.isEmpty())
            this.setSortFields(sortFields);
        else
            this.setSortFields(List.of("updatedAt"));

        if (sort != null)
            this.setSorts(sort);
        else
            this.setSorts(List.of(Sort.Direction.ASC));
    }
}