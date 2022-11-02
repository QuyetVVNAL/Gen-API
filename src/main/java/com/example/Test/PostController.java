package com.example.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PostController {

    @Autowired
    private PostRepository PostRepository;

    @GetMapping("/posts")
    public ResponseEntity getAllPost(){
        List<Post> listPost = PostRepository.findAll();
        return ResponseEntity.ok(listPost);
    }

    @DeleteMapping("/posts")
    public ResponseEntity<String> delete(){
        PostRepository.deleteAll();
        return ResponseEntity.ok("Delete all");
    }
}
