package com.example.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CommentController {

    @Autowired
    private CommentRepository CommentRepository;

    @GetMapping("/Comments")
    public ResponseEntity getAllComment(){
        List<Comment> listComment = CommentRepository.findAll();
        return ResponseEntity.ok(listComment);
    }

    @DeleteMapping("/Comments")
    public ResponseEntity<String> delete(){
        CommentRepository.deleteAll();
        return ResponseEntity.ok("Delete all");
    }
}
