package com.tonilr.ToDoList.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tonilr.ToDoList.Entities.Reminder;
import com.tonilr.ToDoList.Exceptions.NotFoundException;
import com.tonilr.ToDoList.Repos.ReminderRepo;

@Service
public class ReminderServices {
	
	@Autowired
	private final ReminderRepo reminderRepo;

	public ReminderServices(ReminderRepo reminderRepo) {
		this.reminderRepo = reminderRepo;

	}

	public Reminder addReminder(Reminder reminder) {
		return reminderRepo.save(reminder);
	}

	public List<Reminder> findAllReminders() {
		return reminderRepo.findAll();
	}

	public Reminder updateReminder(Reminder reminder) {
		return reminderRepo.save(reminder);
	}

	public Reminder findReminderById(Long id) {
		return reminderRepo.findById(id)
				.orElseThrow(() -> new NotFoundException("Reminder by id " + id + " was not found"));

	}

	public void deleteReminder(Long id) {
		reminderRepo.deleteById(id);
	}

}
