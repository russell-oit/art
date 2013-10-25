/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package art.schedule;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class ScheduleServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(ScheduleServlet.class);

	/**
	 * Processes requests for both HTTP
	 * <code>GET</code> and
	 * <code>POST</code> methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			/* TODO output your page here. You may use following sample code. */
			out.println("<!DOCTYPE html>");
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Servlet ScheduleServlet</title>");
			out.println("</head>");
			out.println("<body>");
			out.println("<h1>Servlet ScheduleServlet at " + request.getContextPath() + "</h1>");
			out.println("</body>");
			out.println("</html>");
		} finally {
			out.close();
		}
	}

	/**
	 * Handles the HTTP
	 * <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");

		String action = request.getParameter("action");
		String id=request.getParameter("id");

		ScheduleDao scheduleDao = new ScheduleDao();

		if (StringUtils.equalsIgnoreCase(action, "list")) {
			request.setAttribute("schedules", scheduleDao.getAllSchedules());

			request.getRequestDispatcher("/WEB-INF/jsp/schedules.jsp").forward(request, response);
		} else if (StringUtils.equalsIgnoreCase(action, "delete")) {
			scheduleDao.deleteSchedule(id);
//			int scheduleId=NumberUtils.toInt(id);
//			if(scheduleId>0){
//				scheduleDao.deleteSchedule(id);
//			} else {
//				logger.warn("Invalid schedule to delete: id={}",id);
//			}
			
			response.sendRedirect(request.getContextPath() + "/admin/schedules?action=list");
		} else if (StringUtils.equalsIgnoreCase(action, "edit")) {
			Schedule schedule=scheduleDao.getSchedule(id);
			request.setAttribute("schedule", schedule);
			request.getRequestDispatcher("/WEB-INF/jsp/editSchedule.jsp").forward(request, response);
			
		}
	}

	/**
	 * Handles the HTTP
	 * <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}
}
