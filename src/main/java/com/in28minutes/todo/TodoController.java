package com.in28minutes.todo;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.in28minutes.exception.ExceptionController;

@Controller
public class TodoController {

	@Autowired
	TodoService service;
	
	private Log logger = LogFactory.getLog(ExceptionController.class);
	
	@InitBinder
	protected void initBInder(WebDataBinder binder) 
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
	}
	
	// get list-todos
	@RequestMapping(value="/list-todos", method = RequestMethod.GET)
	public String showTodosList(ModelMap model)
	{
		model.addAttribute("todos", service.retrieveTodos(retrieveLoggedinUserName()));
		return "list-todos";
	}
	
	
	// Start : private methods -------------------------
	private String retrieveLoggedinUserName() 
	{
		Object principal = SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();
		
		if (principal instanceof UserDetails)
			return ((UserDetails) principal).getUsername();
		
		return principal.toString();
	}
	// End -------------------------
	
	
	// get add-todo
	@RequestMapping(value="/add-todo", method = RequestMethod.GET)
	public String showTodoPage(ModelMap model)
	{
		//throw new RuntimeException("Dummy Exception");		
		model.addAttribute("todo", new Todo());
		return "todo";
		
	}
	
	// post add-todo
	@RequestMapping(value = "/add-todo", method = RequestMethod.POST)
	public String addTodo(ModelMap model, @Valid Todo todo, BindingResult result) 
	{
		if (result.hasErrors())
			return "todo";

		service.addTodo(retrieveLoggedinUserName(), todo.getDesc(), new Date(),false);
		model.clear();// to prevent request parameter "name" to be passed
		return "redirect:/list-todos";
	}

	// get delete-todo
	@RequestMapping(value="/delete-todo", method = RequestMethod.GET)
	public String deleteTodo(ModelMap model, @RequestParam int id)
	{
		service.deleteTodo(id);
		model.clear();
		return "redirect:list-todos"; // need to do redirect to todosList
	}
	
	// get update-todo
	@RequestMapping(value="/update-todo", method = RequestMethod.GET)
	public String updateTodo(ModelMap model, @RequestParam int id)
	{
		Todo todoToUpdate = service.retrieveTodo(id);
		model.addAttribute("todo", todoToUpdate);	
		
		return "todo"; 
	}
	
	// post update-todo
	@RequestMapping(value="/update-todo", method = RequestMethod.POST)
	public String updateTodo(ModelMap model, @Valid Todo todo, BindingResult result)
	{
		if (result.hasErrors())
			return "todo";
		

		todo.setUser(retrieveLoggedinUserName()); //TODO:Remove Hardcoding Later
		service.updateTodo(todo);

		model.clear();// to prevent request parameter "name" to be passed
		
		return "redirect:list-todos"; 
	}
	
	// specific exception handling for this controller
	@ExceptionHandler(value = Exception.class)
	public String handleError(HttpServletRequest req, Exception exception) {
		logger.error("Request: " + req.getRequestURL() + " raised " + exception);
		return "error-todo";
	}
}
