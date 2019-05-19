package days15.message.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import days15.message.model.Message;
import days15.message.service.WriteMessageService;
import days15.message.mvc.command.*;

public class WriteMessageHandler implements CommandHandler{
	private static final String FORM_VIEW = "/days15/view/list.jsp";
	WriteMessageService writeService = WriteMessageService.getInstance();
	@Override
	public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (request.getMethod().equalsIgnoreCase("GET")) {
			return processForm(request, response);
		}
		else if (request.getMethod().equalsIgnoreCase("POST")) {
			return processSubmit(request, response);
		}
		else {
			response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			return null;
		}
	}
	private String processForm(HttpServletRequest request, HttpServletResponse response) {
		return FORM_VIEW;
	}
	private String processSubmit(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("WriteMessageHandler processSubmit");
		Message message = new Message();
		message.setGuest_name(request.getParameter("guest_name"));
		message.setPassword(request.getParameter("password"));
		message.setMessage(request.getParameter("message"));
		System.out.println("message: " + message.toString());
		
		writeService.write(message);
		return "/days15/view/writeMessage.jsp";
	}
}
