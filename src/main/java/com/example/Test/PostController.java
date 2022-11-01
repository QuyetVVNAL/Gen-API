package com.example.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/")
    public ResponseEntity getAllPost(){
        List<Post> listPost = postRepository.findAll();
        return ResponseEntity.ok(listPost);
    }

    @GetMapping("/AAAA")
    public ResponseEntity getAllPostA(){
        List<Post> listPost = postRepository.findAll();
        return ResponseEntity.ok(listPost);
    }
}
