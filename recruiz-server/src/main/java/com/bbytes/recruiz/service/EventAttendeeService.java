package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.EventAttendee;
import com.bbytes.recruiz.repository.EventAttendeeRepository;

@Service
public class EventAttendeeService extends AbstractService<EventAttendee, Long> {

    private EventAttendeeRepository eventAttendeeRepository;

    @Autowired
    public EventAttendeeService(EventAttendeeRepository eventAttendeeRepository) {
	super(eventAttendeeRepository);
	this.eventAttendeeRepository = eventAttendeeRepository;
    }

    public List<EventAttendee> getAttendeeByEmail(String email) {
	return eventAttendeeRepository.findByEmail(email);
    }

}
