package com.example.Test.controller;

import com.example.Test.entity.Post;
import com.example.Test.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PostController {

    @Autowired
    private PostService PostService;

    @GetMapping("/posts")
    public ResponseEntity getAllPost(){
        List<Post> listPost = PostService.findAll();
        return ResponseEntity.ok(listPost);
    }

    @DeleteMapping("/posts")
    public ResponseEntity<String> delete(){
        PostService.deleteAll();
        return ResponseEntity.ok("Delete all");
    }
}
