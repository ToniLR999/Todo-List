package com.tonilr.ToDoList.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.Entities.Comment;
import com.tonilr.ToDoList.Exceptions.NotFoundException;
import com.tonilr.ToDoList.Repos.CommentRepo;

@Service
public class CommentServices {

	@Autowired
	private final CommentRepo commentRepo;

	public CommentServices(CommentRepo commentRepo) {
		this.commentRepo = commentRepo;

	}

	public Comment addComment(Comment comment) {
		return commentRepo.save(comment);
	}

	public List<Comment> findAllComments() {
		return commentRepo.findAll();
	}

	public Comment updateComment(Comment comment) {
		return commentRepo.save(comment);
	}

	public Comment findCommentById(Long id) {
		return commentRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Comment by id " + id + " was not found"));

	}

	public void deleteComment(Long id) {
		commentRepo.deleteById(id);
	}
}
