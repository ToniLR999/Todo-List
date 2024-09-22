package com.tonilr.ToDoList.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.Entities.Role;
import com.tonilr.ToDoList.Exceptions.NotFoundException;
import com.tonilr.ToDoList.Repos.RoleRepo;


@Service
public class RoleServices {

	@Autowired
	private final RoleRepo roleRepo;

	public RoleServices(RoleRepo roleRepo) {
		this.roleRepo = roleRepo;

	}

	public Role addRole(Role role) {
		return roleRepo.save(role);
	}

	public List<Role> findAllRoles() {
		return roleRepo.findAll();
	}

	public Role updateRole(Role role) {
		return roleRepo.save(role);
	}

	public Role findRoleById(Long id) {
		return roleRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Role by id " + id + " was not found"));

	}

	public void deleteRole(Long id) {
		roleRepo.deleteById(id);
	}
	
}
