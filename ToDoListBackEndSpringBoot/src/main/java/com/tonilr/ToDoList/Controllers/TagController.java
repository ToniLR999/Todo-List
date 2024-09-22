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
import com.tonilr.ToDoList.Entities.Tag;
import com.tonilr.ToDoList.Services.TagServices;

@Controller
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/tag")
public class TagController {
	
	
	@Autowired
	private final TagServices tagService;

	
	public TagController(TagServices tagService) {
		this.tagService = tagService;
	}

	
	@GetMapping("/all")
	public ResponseEntity<List<Tag>> getAllTags() {
		List<Tag> tags = tagService.findAllTags();
		return new ResponseEntity<>(tags, HttpStatus.OK);
	}

	@GetMapping("/find/{id}")
	public ResponseEntity<Tag> getTagById(@PathVariable("id") Long id) {
		Tag tag = tagService.findTagById(id);
		return new ResponseEntity<>(tag, HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<Tag> addTag(@RequestBody Tag tag) {
		Tag newTag = tagService.addTag(tag);
		return new ResponseEntity<>(newTag, HttpStatus.CREATED);
	}

	@PutMapping("/update")
	public ResponseEntity<Tag> updateTag(@RequestBody Tag tag) {
		Tag updateTag = tagService.updateTag(tag);
		return new ResponseEntity<>(updateTag, HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteTag(@PathVariable("id") Long id) {
		tagService.deleteTag(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
