package days15.message.command;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import days15.message.service.GetMessageListService;
import days15.message.service.MessageListView;
import days15.message.mvc.command.*;

public class GetMessageListHandler implements CommandHandler{
	private static final String FORM_VIEW = "/days15/view/list.jsp";

	@Override
	public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pageNumberStr = request.getParameter("page");
		int pageNumber = 1;
		if (pageNumberStr != null) {
			pageNumber = Integer.parseInt(pageNumberStr);
		}
		GetMessageListService messageListService = 
				GetMessageListService.getInstance();
		MessageListView viewData = 
				messageListService.getMessageList(pageNumber);
		request.setAttribute("viewData", viewData);
		return FORM_VIEW;
	}
}