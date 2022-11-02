package com.example.Test.service;

import com.example.Test.entity.Post;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PostService {

    List<Post> findAll();

    void deleteAll();
}
