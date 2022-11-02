package com.example.Test.service;

import com.example.Test.entity.Comment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {
    
    List<Comment> findAll();
    
    void deleteAll();
}
