package com.tonilr.ToDoList.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.Entities.Tag;
import com.tonilr.ToDoList.Exceptions.NotFoundException;
import com.tonilr.ToDoList.Repos.TagRepo;


@Service
public class TagServices {

	@Autowired
	private final TagRepo tagRepo;

	public TagServices(TagRepo tagRepo) {
		this.tagRepo = tagRepo;

	}

	public Tag addTag(Tag tag) {
		return tagRepo.save(tag);
	}

	public List<Tag> findAllTags() {
		return tagRepo.findAll();
	}

	public Tag updateTag(Tag tag) {
		return tagRepo.save(tag);
	}

	public Tag findTagById(Long id) {
		return tagRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Tag by id " + id + " was not found"));

	}

	public void deleteTag(Long id) {
		tagRepo.deleteById(id);
	}
}
