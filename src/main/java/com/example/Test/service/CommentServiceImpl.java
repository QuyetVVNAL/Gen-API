package com.example.Test.service;

import com.example.Test.entity.Comment;
import com.example.Test.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository CommentRepository;
    @Override
    public List<Comment> findAll() {
        return CommentRepository.findAll();
    }

    @Override
    public void deleteAll() {
        CommentRepository.deleteAll();
    }
}
