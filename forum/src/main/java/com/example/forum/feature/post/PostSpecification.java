package com.example.forum.feature.post;

import com.example.forum.domain.Enum.PostStatus;
import com.example.forum.domain.PostEntity;
import com.example.forum.domain.Tag;
import com.example.forum.feature.post.dto.PostFilterRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PostSpecification {

    public static Specification<PostEntity> getFilterSpec(PostFilterRequest request){
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList();

            predicates.add(criteriaBuilder.equal(root.get("status"), PostStatus.PUBLISHED));
            predicates.add(criteriaBuilder.equal(root.get("isArchived"),false ));

            if(request.getKeyword() != null && !request.getKeyword().trim().isEmpty()){
                String likePattern = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("postContent")),
                        likePattern));
            }

            if(request.getTags()!= null && !request.getTags().isEmpty()){
                Join<PostEntity, Tag> joinTags = root.join("tags");
                predicates.add(joinTags.get("tagId").in(request.getTags()));
                // Mẹo chống lặp bài: Nếu query trả về nhiều dòng giống nhau do JOIN, báo JPA lọc trùng
                query.distinct(true);
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
