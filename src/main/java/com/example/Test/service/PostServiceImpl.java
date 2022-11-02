package com.example.Test.service;

import com.example.Test.entity.Post;
import com.example.Test.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository PostRepository;
    @Override
    public List<Post> findAll() {
        return PostRepository.findAll();
    }

    @Override
    public void deleteAll() {
        PostRepository.deleteAll();
    }
}
