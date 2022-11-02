package com.example.Test.controller;

import com.example.Test.entity.Comment;
import com.example.Test.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CommentController {

    @Autowired
    private CommentService CommentService;

    @GetMapping("/Comments")
    public ResponseEntity getAllComment(){
        List<Comment> listComment = CommentService.findAll();
        return ResponseEntity.ok(listComment);
    }

    @DeleteMapping("/Comments")
    public ResponseEntity<String> delete(){
        CommentService.deleteAll();
        return ResponseEntity.ok("Delete all");
    }
}
