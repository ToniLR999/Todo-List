package com.tonilr.ToDoList.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.Entities.Category;
import com.tonilr.ToDoList.Exceptions.NotFoundException;
import com.tonilr.ToDoList.Repos.CategoryRepo;

@Service
public class CategoryServices {

	@Autowired
	private final CategoryRepo categoryRepo;

	public CategoryServices(CategoryRepo categoryRepo) {
		this.categoryRepo = categoryRepo;

	}

	public Category addCategory(Category category) {
		return categoryRepo.save(category);
	}

	public List<Category> findAllCategorys() {
		return categoryRepo.findAll();
	}

	public Category updateCategory(Category category) {
		return categoryRepo.save(category);
	}

	public Category findCategoryById(Long id) {
		return categoryRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Category by id " + id + " was not found"));

	}

	public void deleteCategory(Long id) {
		categoryRepo.deleteById(id);
	}
}
