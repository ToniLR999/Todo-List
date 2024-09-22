package com.tonilr.ToDoList.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tonilr.MyRoutines.entitys.Rutina;
import com.tonilr.MyRoutines.services.RutinaServices;
import com.tonilr.ToDoList.Entities.Comment;
import com.tonilr.ToDoList.Services.CommentServices;

@Controller
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/comment")
public class CommentController {
	
	@Autowired
	private final CommentServices commentService;

	
	public CommentController(CommentServices commentService) {
		this.commentService = commentService;
	}

	
	@GetMapping("/all")
	public ResponseEntity<List<Comment>> getAllComments() {
		List<Comment> comments = commentService.findAllComments();
		return new ResponseEntity<>(comments, HttpStatus.OK);
	}

	@GetMapping("/find/{id}")
	public ResponseEntity<Comment> getCommentById(@PathVariable("id") Long id) {
		Comment comment = commentService.findCommentById(id);
		return new ResponseEntity<>(comment, HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<Comment> addComment(@RequestBody Comment comment) {
		Comment newComment = commentService.addComment(comment);
		return new ResponseEntity<>(newComment, HttpStatus.CREATED);
	}

	@PutMapping("/update")
	public ResponseEntity<Comment> updateComment(@RequestBody Comment comment) {
		Comment updateComment = commentService.updateComment(comment);
		return new ResponseEntity<>(updateComment, HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteComment(@PathVariable("id") Long id) {
		commentService.deleteComment(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
