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
import com.tonilr.ToDoList.Entities.Reminder;
import com.tonilr.ToDoList.Services.ReminderServices;

@Controller
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/reminder")
public class ReminderController {

	
	@Autowired
	private final ReminderServices reminderService;

	
	public ReminderController(ReminderServices reminderService) {
		this.reminderService = reminderService;
	}

	
	@GetMapping("/all")
	public ResponseEntity<List<Reminder>> getAllReminders() {
		List<Reminder> reminders = reminderService.findAllReminders();
		return new ResponseEntity<>(reminders, HttpStatus.OK);
	}

	@GetMapping("/find/{id}")
	public ResponseEntity<Reminder> getReminderById(@PathVariable("id") Long id) {
		Reminder reminder = reminderService.findReminderById(id);
		return new ResponseEntity<>(reminder, HttpStatus.OK);
	}

	@PostMapping("/add")
	public ResponseEntity<Reminder> addReminder(@RequestBody Reminder reminder) {
		Reminder newReminder = reminderService.addReminder(reminder);
		return new ResponseEntity<>(newReminder, HttpStatus.CREATED);
	}

	@PutMapping("/update")
	public ResponseEntity<Reminder> updateReminder(@RequestBody Reminder reminder) {
		Reminder updateReminder = reminderService.updateReminder(reminder);
		return new ResponseEntity<>(updateReminder, HttpStatus.OK);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteReminder(@PathVariable("id") Long id) {
		reminderService.deleteReminder(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
}
