package com.example.temp.follow.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFromIdAndToId(long fromId, Long toId);

    List<Follow> findAllByFromIdAndStatus(long fromId, FollowStatus status);
}
