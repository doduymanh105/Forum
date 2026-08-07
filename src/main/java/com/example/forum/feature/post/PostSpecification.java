package com.example.forum.feature.post;

import com.example.forum.domain.Enum.PostStatus;
import com.example.forum.domain.PostEntity;
import com.example.forum.domain.Tag;
import com.example.forum.feature.post.dto.PostFilterRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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
//                Join<PostEntity, Tag> joinTags = root.join("tags");
//                predicates.add(joinTags.get("tagId").in(request.getTags()));
//                // Mẹo chống lặp bài: Nếu query trả về nhiều dòng giống nhau do JOIN, báo JPA lọc trùng
//                query.distinct(true);

                for(Long tagId: request.getTags()){

                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<PostEntity> subRoot = subquery.from(PostEntity.class);

                    Join<PostEntity, Tag> subJoin = subRoot.join("tags");

                    subquery.select(subRoot.get("postId"))
                            .where(criteriaBuilder.equal(subJoin.get("tagId"), tagId));

                    // separate condition then link by "and"-> mean: postId must be in this list post_ids which both in provided tags
                    predicates.add(root.get("postId").in(subquery));

                }
            }

            if(request.getStartDate()!= null){
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), request.getStartDate()));
            }

            if(request.getEndDate()!= null){
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), request.getEndDate()));
            }

            if (request.getMinUpvote() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("upvotes"), request.getMinUpvote()));
            }

            if (request.getAuthorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("creator").get("userId"), request.getAuthorId()));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
