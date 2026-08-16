package com.example.bookstore.mapper;

import com.example.bookstore.domain.Category;
import com.example.bookstore.dto.CategoryPatchRequest;
import com.example.bookstore.dto.CategoryRequest;
import com.example.bookstore.dto.CategoryResponse;
import org.mapstruct.*;


@Mapper(componentModel = "spring")

public interface CategoryMapper {
    @Mapping(source = "id", target = "categoryId")
    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void patchEntityFromRequest(CategoryPatchRequest request, @MappingTarget Category category);
}
