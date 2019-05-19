package days15.message.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import days15.message.service.DeleteMessageService;
import days15.message.service.InvalidPassowrdException;
import days15.message.mvc.command.*;


public class DeleteMessageHandler implements CommandHandler{
	private static final String FORM_VIEW = "/days15/view/comfirmDeletion.jsp";
	@Override
	public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (request.getMethod().equalsIgnoreCase("GET")) {
			System.out.println("LoginHandler preocess GET");
			return processForm(request, response);
		}
		else if (request.getMethod().equalsIgnoreCase("POST")) {
			System.out.println("LoginHandler preocess POST");
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
		int messageId = Integer.parseInt(request.getParameter("messageId"));
		String password = request.getParameter("password");
		boolean invalidPassowrd = false;
		try {
			DeleteMessageService deleteService = 
					DeleteMessageService.getInstance();
			deleteService.deleteMessage(messageId, password);
		} catch(InvalidPassowrdException ex) {
			invalidPassowrd = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		request.setAttribute("invalidPassowrd", invalidPassowrd);
		return "/days15/view/deleteMessage.jsp";
	}
}
