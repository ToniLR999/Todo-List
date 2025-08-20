package com.tonilr.ToDoList.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tonilr.ToDoList.service.ScheduleService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class MaintenanceFilter extends OncePerRequestFilter {

	private final ScheduleService scheduleService;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public MaintenanceFilter(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// Permitir siempre preflight y endpoints de estado/autenticación
		String path = request.getRequestURI();
		if (HttpMethod.OPTIONS.matches(request.getMethod()) ||
				path.startsWith("/api/app-status") ||
				path.startsWith("/api/auth") ||
				path.startsWith("/actuator")) {
			filterChain.doFilter(request, response);
			return;
		}

		// Si la aplicación está inactiva (fuera de horario en prod), responder 503
		if (!scheduleService.isApplicationActive()) {
			response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
			response.setContentType("application/json");
			Map<String, Object> body = new HashMap<>();
			body.put("status", "MAINTENANCE");
			body.put("message", "Servicio no disponible fuera del horario laboral (10:00-19:00 Europe/Madrid)");
			body.put("scheduleStatus", scheduleService.getScheduleStatus());
			objectMapper.writeValue(response.getWriter(), body);
			return;
		}

		filterChain.doFilter(request, response);
	}
}


